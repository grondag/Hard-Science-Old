package grondag.adversity.feature.volcano;

import java.util.HashMap;
import grondag.adversity.Adversity;
import grondag.adversity.library.NeighborBlocks.HorizontalFace;
import grondag.adversity.library.RelativeBlockPos;
import grondag.adversity.library.Useful;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LavaManager extends HashMap<BlockPos, LavaManager.LavaFlow>
{
    private static final long serialVersionUID = -4637241090412818925L;
    private final TileVolcano volcano;
    private HashMap<BlockPos, LavaFlow> newFlows = new HashMap<BlockPos, LavaFlow>();
    private int updateCount = 0;
    
    /** set false at start of each cycle and then true if any source block is able to donate lava */
    private boolean didAnySourceBlockDonate;
    private int blockedFrameCount = 0;
    
    
    public LavaManager(TileVolcano volcano)
    {
        super();
        this.volcano = volcano;
    }


    public LavaManager(TileVolcano volcano, int[] values)
    {
        this(volcano);

        //to be valid, must match layout in getArray()
        if(values.length % 4 != 0)
        {
            Adversity.log.warn("Invalid open space data loading volcano at " + volcano.getPos().toString()
            + ". Volcano may not place lava properly.");
            return;
        }

        int i = 0;
        while(i < values.length)
        {
            BlockPos flowPos = RelativeBlockPos.getPos(values[i++], volcano.getPos());
            BlockPos flowOrigin = RelativeBlockPos.getPos(values[i++], volcano.getPos());
            float level = Float.intBitsToFloat(values[i++]);
            int flags = values[i++];
            IBlockState state = volcano.getWorld().getBlockState(flowPos);
            
            //skip blocks that aren't actually lava blocks
            if(state.getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK)
            {
                int volume = IFlowBlock.getFlowHeightFromState(volcano.getWorld().getBlockState(flowPos));
                LavaFlow value = new LavaFlow(flowPos, flowOrigin, level, volume, (flags & LavaFlow.SOURCE_FLAG) == LavaFlow.SOURCE_FLAG);
                this.put(flowPos, value);
            }
        }
    }
    
    public void trackLavaAt(BlockPos location, BlockPos origin, int level, boolean isSource)
    {
        this.put(location, new LavaFlow(location, origin, level, isSource));
    }

    public void doFrame()
    {
        //set state for source block tracking
        didAnySourceBlockDonate = false;
        
        //process all blocks
        for(LavaFlow flow : this.values())
        {
            flow.process();
        }
        
        if(this.didAnySourceBlockDonate == false)
        {
            blockedFrameCount++;
        }
        else
        {
            blockedFrameCount = 0;
        }
        
        //apply deltas
        for(LavaFlow flow : this.values())
        {
            flow.applyAndClearDelta();;
        }
        
        //incorporate added blocks
        if(!newFlows.isEmpty())
        {
            this.putAll(newFlows);
            this.updateCount += newFlows.size();
        }
        
        // update shortest path
        //TODO
        
        //clean up
        newFlows.clear();
    }
    
    /**
     * Creates flow at location with given level if it does not exist.
     * Adds amount to flow directly if already exists.
     * Flow not added to collection until all existing nodes have processed.
     */
    private void addNewFlow(BlockPos location, BlockPos origin, float level)
    {
        LavaFlow newFlow = newFlows.get(location);
        if(newFlow == null)
        {
            newFlow = new LavaFlow(location, origin, level, false);
            newFlow.needsBlockUpdate = true;
            newFlows.put(location, newFlow);
        }
        else
        {
            newFlow.setFlowLevel(newFlow.getFlowLevel() + level, newFlow.volume);
        }
    }
    
    public LavaFlow[] pollUpdates()
    {
        LavaFlow[] result = new LavaFlow[this.updateCount];
        if(this.updateCount > 0)
        {
            int i = 0;
            for(LavaFlow flow : this.values())
            {
                if(flow.needsBlockUpdate)
                {
                    flow.needsBlockUpdate = false;
                    result[i++] = flow;
                }
            }
        }
        this.updateCount = 0;
        return result;
    }

    public boolean isBlocked()
    {
        //TODO: make configurable?
        return this.blockedFrameCount > 20;
    }
    
    public void clearBlocked()
    {
        this.blockedFrameCount = 0;
    }
    
    @Override 
    public void clear()
    {
        super.clear();
        this.clearBlocked();
        this.updateCount = 0;
        this.newFlows.clear();
    }
    
    public int[] getArray()
    {
        int[] result = new int[this.size() * 4];
        int i = 0;

        for(LavaFlow space: this.values())
        {
            result[i++] = RelativeBlockPos.getKey(space.pos, LavaManager.this.volcano.getPos());
            result[i++] = RelativeBlockPos.getKey(space.origin, LavaManager.this.volcano.getPos());
            result[i++] = Float.floatToIntBits(space.flowLevel);
            result[i++] = (space.isSource ? LavaFlow.SOURCE_FLAG : 0);
        }	    
        return result;
    }

    public static boolean canDisplace(IBlockState state)
    {
        Material material = state.getMaterial();

        if(material == Material.AIR) return true;
        if (material == Material.CLAY) return false;
        if (material == Material.DRAGON_EGG ) return false;
        if (material == Material.GROUND ) return false;
        if (material == Material.IRON ) return false;
        if (material == Material.SAND ) return false;
        if (material == Material.PORTAL ) return false;
        if (material == Material.ROCK ) return false;
        if (material == Material.ANVIL ) return false;
        if (material == Material.GRASS ) return false;

        Block block = state.getBlock();
        
        //can only displace core lava at top
        if(block == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK)
        {
           return false;
        }

        if (IFlowBlock.isFlowFiller(block)) return true;


        // Volcanic lava don't give no shits about your stuff.
        return true;        
    };
    
    private static int computeNominalVolume(BlockPos pos, BlockPos origin)
    {
        return Math.max(0, FlowHeightState.BLOCK_LEVELS_INT - 2 * (int) Math.sqrt(Useful.squared(pos.getX() - origin.getX()) 
                + Useful.squared(pos.getZ() - origin.getZ())));
    }

    public class LavaFlow 
    {
        private final BlockPos pos;
        private final BlockPos origin;
        private float flowLevel;
        private float flowDelta = 0;
        private int volumeDelta = 0;
        
        private int volume;
        private boolean isSource;
        private boolean needsBlockUpdate = false;
        
        /** bits set to true if we've already tested that we can't fit into the block on that face*/
        private byte[] sideFlags = new byte[4];
        private boolean blockDown;
        
        private static final int SOURCE_FLAG = 0x1;
        
        private static final byte SIDE_IS_SOURCE = 0x1;
        private static final byte SIDE_IS_BLOCKED = 0x2;
        
        private LavaFlow(BlockPos pos, BlockPos origin, float flowLevel, boolean isSource)
        {
            this(pos, origin, flowLevel, computeNominalVolume(pos, origin), isSource);
        }
        
        private LavaFlow(BlockPos pos, BlockPos origin, float flowLevel, int volume, boolean isSource)
        {
            this.pos = pos;
            this.origin = origin;
            this.volume = volume;
            this.flowLevel = flowLevel;
            this.isSource = isSource;
        }
        
        private void process()
        {
            float remaining = tryFlowDown(flowLevel);
            remaining = tryFlowSideways(remaining);
            if(remaining != flowLevel)
            {
                flowDelta += remaining - flowLevel;
            }
        }
        
        /**
         * Tries to flow into block below
         */
        private float tryFlowDown(float startingLevel)
        {
            BlockPos downPos = this.pos.down();
            
            //skip if already determined can't happen
            if(!this.blockDown)
            {
                
                float availableAmount = this.isSource ? FlowHeightState.BLOCK_LEVELS_INT : Math.min(FlowHeightState.BLOCK_LEVELS_INT, this.flowLevel - 1);

                if(availableAmount < 1) return startingLevel;
                
                LavaFlow downFlow = LavaManager.this.get(downPos);
                
                if(downFlow != null)
                {
                    //don't flow into source blocks
                    if(downFlow.isSource)
                    {
                        return startingLevel;
                    }
                    float max = FlowHeightState.BLOCK_LEVELS_INT - downFlow.flowLevel;
                    float amount = Math.min(availableAmount, max);
                    downFlow.changeFlow(amount, 0);
                    if(this.isSource) didAnySourceBlockDonate = true;
                    return this.isSource ? this.flowLevel : startingLevel - amount;
                }
                
                if(canDisplace(volcano.getWorld().getBlockState(downPos)))
                {
                    LavaManager.this.addNewFlow(downPos, this.origin, availableAmount);
                    if(this.isSource) didAnySourceBlockDonate = true;
                    return this.isSource ? this.flowLevel : startingLevel - availableAmount;
                }
                
                // prevent retry in future frames
                this.blockDown = true;
            }
                
            return startingLevel;
        }

        private float tryFlowSideways(float startingLevel)
        {
            
//            if(this.pos.getX() == 589 && this.pos.getY() == 4 && this.pos.getZ() == 801)
//            {
//                Adversity.log.info("boop");
//            }
            
            //temporary hack
            didAnySourceBlockDonate = true;
            
//            float available = this.isSource? FlowHeightState.BLOCK_LEVELS_FLOAT : startingLevel - this.volume;
            float available;
            
            if(this.isSource)
                available = FlowHeightState.BLOCK_LEVELS_FLOAT;
            else
                available = startingLevel - this.volume;
            
            if(available <= 0) return startingLevel;
            
            int sideCount = 0;
            float totalSideFill = 0;
            float totalSideCapacity = 0;
            int totalSideVolume = 0;
            final float myPressureRatio = this.isSource ? 2 : startingLevel / this.volume;
            
            int tallSideCount = 0;
            
            LavaFlow sideFlows[] = new LavaFlow[4];
            float fill[] = new float[4];
            boolean include[] = new boolean[4];
            
            int volume[] = new int[4];
            
            for(int i = 0; i < 4; i++)
            {
                if(this.sideFlags[i] == 0)
                {
                    final BlockPos sidePos = this.pos.add(HorizontalFace.values()[i].directionVector);
                    final LavaFlow flow = LavaManager.this.get(sidePos);
                            
                    if(flow != null)
                    {
                        if(flow.isSource)
                        {
                            // prevent retry in future frames
                            this.sideFlags[i] |= SIDE_IS_SOURCE;
                            tallSideCount++;
                        }
                        else
                        {
                            if(flow.flowLevel/flow.volume < myPressureRatio)
                            {
                                sideFlows[i] = flow;
                                include[i] = true;
                                sideCount++;
                                fill[i] = flow.getFlowLevel();
                                volume[i] = flow.volume;
                                totalSideCapacity += Math.max(0, volume[i] - fill[i]);
                                totalSideFill += fill[i];
                                totalSideVolume += volume[i];
                            }
                            if(flow.volume > this.volume) tallSideCount++;
;
                        }
                    }
                    else if(canDisplace(volcano.getWorld().getBlockState(sidePos)))
                    {
                        int newVolume = computeNominalVolume(sidePos, this.origin);
                        volume[i] = newVolume;
                        totalSideVolume += newVolume;
                        totalSideCapacity += newVolume;
                        include[i] = true;
                        sideCount++;
                    }
                    else
                    {
                        // prevent retry in future frames
                        this.sideFlags[i] |= SIDE_IS_BLOCKED;
                        tallSideCount++;
                    }
                }
                else
                {
                    tallSideCount++;
                }
            }

            final float totalNormalFlow = Math.min(available, totalSideCapacity);
            float totalPressureFlow = 0;

            if(sideCount > 0)
            {
                //include this block in lava distribution unless it is a source block
          
                final float totalVolume = totalSideVolume + (this.isSource ? 0 : this.volume);
                final float totalFill = totalSideFill + startingLevel; //(this.isSource ? 12 : startingLevel);
                final float totalPressure = Math.max(0, totalFill - totalVolume);
                
                for(int i = 0; i < 4; i++)
                {   
                    if(include[i])
                    {
                        float delta = 0;
                        LavaFlow flow = sideFlows[i];
                        
                        if(totalNormalFlow > 0)
                        {
                            float capacity = volume[i] - fill[i];
                            if(capacity > 0)
                            {
                                delta += totalNormalFlow * capacity / totalSideCapacity;
                            }
                        }
                        if(totalPressure > 0)
                        {
                            float targetPressure = totalPressure * volume[i] / totalVolume;
                            float forecastedPressure = Math.max(0, (fill[i] + delta) - volume[i]);
                            float pressureFlow = targetPressure - forecastedPressure;
                            if(pressureFlow > 0)
                            {
                                delta += pressureFlow;
                                totalPressureFlow += pressureFlow;
                            }
                        }
     
                        if(delta > 0)
                        {
                            if(flow == null)
                            {
                                LavaManager.this.addNewFlow(this.pos.add(HorizontalFace.values()[i].directionVector), this.origin, delta);
                            
                            }
                            else
                            {
                                flow.changeFlow(delta, 0);
                            }
                        }
                        if(this.isSource) didAnySourceBlockDonate = true;
                    
                    }
                }
                
                float result;
                
                if(this.isSource)
                {
                    result = startingLevel;
                }
                else
                {
                    result = startingLevel - totalNormalFlow - totalPressureFlow;
                    
                      Adversity.log.info("pressure=" + result / this.volume 
                      + " total flow=" + (totalNormalFlow + totalPressureFlow)
                      + " distance=" + Math.sqrt(Useful.squared(pos.getX() - origin.getX()) 
                              + Useful.squared(pos.getZ() - origin.getZ()))
                      + " @" + this.pos.toString());
                    
                    if(result / this.volume > 1.5)
                    {
                        if(tallSideCount > 1)
                        {
                            this.changeFlow(0, 1);
                            Adversity.log.info("increase volume from" + this.volume 
                                    + " distance=" + Math.sqrt(Useful.squared(pos.getX() - origin.getX()) 
                                            + Useful.squared(pos.getZ() - origin.getZ()))
                                    + " @" + this.pos.toString());
                        }
                    }
                }
                
//                Adversity.log.info("Source total pressure=" + totalFill / totalVolume 
//                        + " total flow=" + (totalNormalFlow + totalPressureFlow)
//                        + " @" + this.pos.toString());
                return result;
            }
        
            return startingLevel;
        }
        
    
        private void applyAndClearDelta()
        {
//            if(this.pos.getX() == 589 && this.pos.getY() == 4 && this.pos.getZ() == 801)
//            {
//                Adversity.log.info("boop");
//            }
            
            this.setFlowLevel(flowLevel + flowDelta, volume + volumeDelta);
            this.flowDelta = 0;
            this.volumeDelta = 0;
        }
        
        private void changeFlow(float flowDelta, int volumeDelta)
        {
//            if(this.pos.getX() == 589 && this.pos.getY() == 4 && this.pos.getZ() == 801)
//            {
//                Adversity.log.info("boop");
//            }
//            
            this.flowDelta += flowDelta;
            this.volumeDelta += volumeDelta;
        }
        
        private float getFlowLevel()
        {
            return flowLevel;
        }

        public void setFlowLevel(float level, int volume)
        {
//            if(this.pos.getX() == 589 && this.pos.getY() == 4 && this.pos.getZ() == 801)
//            {
//                Adversity.log.info("boop");
//            }
            
            if(!this.needsBlockUpdate 
                    && (this.volume != volume ||
                        Math.min(FlowHeightState.BLOCK_LEVELS_INT, this.flowLevel) 
                        !=  Math.min(FlowHeightState.BLOCK_LEVELS_INT, level)) )
            {
                this.needsBlockUpdate = true;
                LavaManager.this.updateCount++;
            }
            this.flowLevel = level;
            this.volume = volume;
        }
        
        public int getVisibleFlowLevel()
        {
//            if(this.pos.getX() == 589 && this.pos.getY() == 4 && this.pos.getZ() == 801)
//            {
//                Adversity.log.info("boop");
//            }
            return Math.max(1, Math.min(this.volume, Math.round(this.flowLevel)));
        }
        
        public boolean getSource()
        {
            return isSource;
        }
        
        public void setSource(boolean isSource)
        {
            this.isSource = isSource;
        }

        public BlockPos getPos()
        {
            return this.pos;
        }

        public BlockPos getOrigin()
        {
            return this.origin;
        }
    }
}
