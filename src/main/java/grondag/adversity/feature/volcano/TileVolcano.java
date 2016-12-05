package grondag.adversity.feature.volcano;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.HashSet;
import java.util.Queue;

import grondag.adversity.Adversity;
import grondag.adversity.config.Config;
import grondag.adversity.feature.volcano.BlockManager.BlockPlacement;
import grondag.adversity.feature.volcano.lava.LavaBlockUpdate;
import grondag.adversity.feature.volcano.lava.LavaManager2;
import grondag.adversity.library.NeighborBlocks.HorizontalFace;
import grondag.adversity.library.Useful;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import grondag.adversity.niceblock.support.BaseMaterial;
import grondag.adversity.simulator.Simulator;
import grondag.adversity.simulator.VolcanoManager.VolcanoNode;

//FIX/TEST


//TODOS
//simulation integration & control from simulation
//volcano wand
//tune lava flow shape
//detection item
//world gen

//biome
//smoke
//haze
//ash

//create falling-block explosion w/ configurable crater radius
//explosion of mound
//ignite surrounding blocks
//sound effects for volcano
//sound for lava
//water rendering



public class TileVolcano extends TileEntity implements ITickable{

    // Activity cycle.
    private VolcanoStage stage = VolcanoStage.NEW;
    /** max Y level of the volcano */
    private int						level;
    /** current Y level for placing blocks */
    private int						buildLevel;
    private int						groundLevel;
    private int backtrackLimit = 0;
    private int ticksActive = 0;
    private int lavaCounter = 0;
    private int lavaCooldownTicks = 0;
    private int placementCountdown = 0;

    /** simulation delegate */
    private VolcanoNode             node;
    /** 
     * id for VolcanoNode 
     * Set during ReadNBT so it can be obtained from simulation after ticks have started
     */
    private int nodeId = -1;
    private static final int NODE_NOT_FOUND = -1;

    /**
     * Spaces potentially available for flowing lava.
     */
//    private SpaceManager spaceManager;

    /**
     * Placed lava blocks that need to be cooled when flowing is done or paused.
     */
//    private BlockManager lavaBlocks;

    /**
     * Top lava blocks that should only be cooled once flowing is done for this level.
     */
//    private BlockManager topBlocks;

    /**
     * Blocks ready to be cooled at any time, irrespective of state.
     */
    private BlockManager coolingBlocks;

    //private final VolcanoHazeMaker	hazeMaker		= new VolcanoHazeMaker();
    
    /**
     * Blocks that need to be melted or checked for filler after placement.
     * Not saved to NBT because should be fully processed and cleared every tick.
     */
    private HashSet<BlockPos> adjustmentList;


    /**
     * Fluid simulator for lava blocks
     */
    private LavaManager lavaManager;

    /**
     * lavaManager wants access to World, which will not work during ReadNBT.
     * Save persistence data here for load on first tick.
     */
    private int[] lavaManagerData;
    
    
    private LavaManager2 cellManager;
    
    private int						hazeTimer		= 60;

    public static enum VolcanoStage
    {
        NEW,
        FLOWING,
        /** Flow temporarily stopped to allow for cooling. */
        COOLING,
        DORMANT,
        DEAD
    }


    private void makeHaze() {
        if (this.hazeTimer > 0) {
            --this.hazeTimer;
        } else {
            this.hazeTimer = 5;
            //this.hazeMaker.update(this.worldObj, this.pos.getX(), this.pos.getZ());

            // if(worldObj.rand.nextInt(3)==0){
            // worldObj.setBlock(xCoord+2-worldObj.rand.nextInt(5), level+2, zCoord+2-worldObj.rand.nextInt(5),
            // Adversity.blockHazeRising, 0, 2);
            // worldObj.scheduleBlockUpdate(xCoord, level+2, zCoord, Adversity.blockHazeRising, 15);
            // }
        }
    }



    private boolean isWithinBore(BlockPos posIn)
    {
        return posIn.distanceSq(this.pos.getX(), posIn.getY(), this.pos.getZ()) <= Config.volcano().boreRadiusSquared;
    }


//    private boolean canDisplace(IBlockState state, BlockPos pos, Block block, boolean allowLava)
//    {
//        Material material = state.getMaterial();
//
//        if(material == Material.AIR) return true;
//
//        //can only displace core lava at top
//        if(block == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK)
//        {
//            if(this.isWithinBore(pos))
//            {
//                return allowLava && pos.getY() == this.level;
//            }
//            else
//            {
//                return allowLava;
//            }
//            
//        }
//
//        if(pos.getY() == this.level && isWithinBore(pos)) return true;
//
//        if (IFlowBlock.isFlowFiller(state.getBlock())) return true;
//
//        if (material == Material.CLAY) return false;
//        if (material == Material.DRAGON_EGG ) return false;
//        if (material == Material.GROUND ) return false;
//        if (material == Material.IRON ) return false;
//        if (material == Material.SAND ) return false;
//        if (material == Material.PORTAL ) return false;
//        if (material == Material.ROCK ) return false;
//        if (material == Material.ANVIL ) return false;
//        if (material == Material.GRASS ) return false;
//
//        // Volcanic lava don't give no shits about your stuff.
//        return true;        
//    };

    // True if block at this position cannot be displaced and is not already lava.
    // Lava within bore counts as supporting.
//    private boolean isSupportingBlock(BlockPos pos)
//    {
//        IBlockState state = this.worldObj.getBlockState(pos);
//        if(state.getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK)
//            return isWithinBore(pos);
//        else
//            return !canDisplace(state, pos, state.getBlock());
//    }


    @Override
    public void update() 
    {
        boolean isNodeUpdateNeeded = false;

        if(this.worldObj.isRemote) return;

        if(this.node == null)
        {
            if(!Simulator.instance.isRunning()) return;

            if(this.stage == VolcanoStage.NEW)
            {
                Adversity.log.info("setting up new Volcano @" + this.pos.toString());

                this.node = Simulator.instance.getVolcanoManager().createNode();
                this.node.setLocation(this.pos,this.worldObj.provider.getDimension());
                this.stage = VolcanoStage.DORMANT;
                this.level = this.pos.getY();
                this.lavaManager = new LavaManager(this);
//                this.spaceManager = new SpaceManager(this.pos);
//                this.lavaBlocks = new BlockManager(this.pos, true);
//                this.topBlocks = new BlockManager(this.pos, true);
                this.coolingBlocks = new BlockManager(this.pos, false);
                this.adjustmentList = new HashSet<BlockPos>();
                this.lavaCooldownTicks = 0;
                int moundRadius = Config.volcano().moundRadius;
                this.groundLevel = Useful.getAvgHeight(this.worldObj, this.pos, moundRadius, moundRadius * moundRadius / 10);
            }
            else
            {
                Adversity.log.info("retrieving Volcano node @" + this.pos.toString());

                this.node = Simulator.instance.getVolcanoManager().findNode(this.nodeId);
                if(this.node == null)
                {
                    Adversity.log.warn("Unable to load volcano simulation node for volcano at " + this.pos.toString()
                    + ". Created new simulation node.  Simulation state was lost.");
                    this.node = Simulator.instance.getVolcanoManager().createNode();
                    this.node.setLocation(this.pos,this.worldObj.provider.getDimension());
                }
            }
            isNodeUpdateNeeded = true;
            
            //Needs to happen after tile entity and chunk it is in are fully loaded
            //because LavaManager needs access to the world. 
            //Using node==null as trigger for 1st-time load.
            //NB: tried this in onload - doesn't work because chunk isn't loaded 
            //and recurses trying to retrieve the chunk containing the tile entity
            if(this.lavaManager == null && this.lavaManagerData != null)
            {
                this.lavaManager = new LavaManager(this, this.lavaManagerData);
                this.lavaManagerData = null;
            }
        }

        ticksActive++;

        VolcanoStage oldStage = this.stage;
        
        
        if(cellManager == null)
        {
            cellManager = new LavaManager2(pos, worldObj);
        }
        
        Queue<LavaBlockUpdate> blockUpdates = cellManager.getBlockUpdates();
        
        LavaBlockUpdate update = blockUpdates.poll();       
        while(update != null)
        {
            worldObj.setBlockState(update.pos, IFlowBlock.stateWithFlowHeight(NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK.getDefaultState(), update.level));
            this.adjustmentList.add(update.pos);
            update = blockUpdates.poll();
        }
        
        
        //doBlockUpdates();
        
        doAdjustments();

        isNodeUpdateNeeded = isNodeUpdateNeeded || this.stage != oldStage;

        if(isNodeUpdateNeeded || (this.ticksActive & 0xF) == 0xF )
        {
            node.updateWorldState(ticksActive + 1000, level, stage);
//            Adversity.log.info("updating Volcano status " + stage.name() + " @" + this.pos.toString());
        }

        this.markDirty();
    }

    private void doBlockUpdates()
    {

        switch(this.stage)
        {
        case DORMANT:
            if(this.level >= Config.volcano().maxYLevel)
            {
                this.stage = VolcanoStage.DEAD;
            }
            else if (this.node.isActive())
            {
                startFlowingAtNewLevel();
                this.stage = VolcanoStage.FLOWING;
            }
            break;

        case FLOWING:
            //skip flowing if have too many cooling blocks - to wait for blocks to cool
//            if(this.coolingBlocks.getCount() > Config.volcano().blockTrackingMax) break;
            
            if(this.placementCountdown < 10)
            {
                this.placementCountdown++;
            }
            else
            {
                this.placementCountdown = 0;
                for(int i = 0; i < Config.volcano().blockOperationsPerTick; i++)
                {
                    this.lavaManager.doFrame();
                }
                
          
                for(LavaManager.LavaFlow flow : lavaManager.pollUpdates())
                {
                    IBlockState state = worldObj.getBlockState(flow.getPos());
                    if(state.getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK)
                    {
                        if(IFlowBlock.getFlowHeightFromState(state) != flow.getVisibleFlowLevel())
                        {
                            worldObj.setBlockState(flow.getPos(), 
                                    IFlowBlock.stateWithFlowHeight(state, flow.getVisibleFlowLevel()));
                            adjustmentList.add(flow.getPos());
                        }
                    }
                    else if(LavaManager.canDisplace(state))
                    {
                        worldObj.setBlockState(flow.getPos(), 
                                IFlowBlock.stateWithFlowHeight(NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK.getDefaultState(), flow.getVisibleFlowLevel()));
                    
                        adjustmentList.add(flow.getPos());
                    }
                }
                
                if(lavaManager.isBlocked())
                {
                    for(LavaManager.LavaFlow flow : lavaManager.values())
                    {
                        //TODO: track lava ticks?
                        coolingBlocks.add(flow.getPos(), this.ticksActive);
                    }
                    lavaManager.clear();
                    this.stage = VolcanoStage.COOLING;
                }
            }
//            //if no more spaces, allow cooling of top blocks
//            if(spaceManager.getCount() == 0)
//            {
//                while(topBlocks.getCount() > 0)
//                {
//                    this.lavaBlocks.add(topBlocks.pollLastEntry().getPos(), lavaCounter++);
//                }
//                this.stage = VolcanoStage.COOLING;
//            }
//            else
//            {
//                //if back at top, enable cooling of blocks so far
//                if(this.buildLevel < this.level 
//                        && this.lavaBlocks.getCount() > 0
//                        && spaceManager.peekFirst().getPos().getY() == this.level)
//                {
//                    this.buildLevel = this.level;
//                    this.backtrackLimit = this.level + 1;
//                    this.stage = VolcanoStage.COOLING;
//                }
//                else
//                {
//                    if(Config.volcano().blockOperationsPerTick > 1)
//                    {
//                        doFlowing();
//                    }
//                    else if(placementCountdown <= 0) 
//                    {
//                        doFlowing();
//                        placementCountdown = getRandomBlockTicks();
//                    }
//                    else
//                    {
//                        placementCountdown--;
//                    }
//                }
//            }
            break;

        case COOLING:
            for(int i = 0; i < Config.volcano().blockOperationsPerTick; i++)
            {
//                doLavaCooling();
                doCooling();
                if(this.coolingBlocks.isEmpty())
                {
                    //continue with current level if still have spaces
                    if(this.lavaManager.size() > 0)
                    {
                        this.stage = VolcanoStage.FLOWING;
                    }
                    //if no more spaces and still active, go up a level
                    else if(this.node.isActive() && this.level < Config.volcano().maxYLevel)
                    {
                        startFlowingAtNewLevel();
                        this.stage = VolcanoStage.FLOWING;
                    }
                    else
                    {
                        this.stage = VolcanoStage.DORMANT;
                    }
                    break;
                }
            }
            break;

        case DEAD:
        case NEW:
        default:
            //NOOP
            //new case is handled above
            break;
        }


//        if(coolingBlocks.getCount() != 0)// && placedLava.lastEntry().getValue().worldTick < this.worldObj.getWorldTime())
//        {
//            doCooling();
//        }
    }

    private void startFlowingAtNewLevel()
    {
        this.level++;
        this.buildLevel = this.level;
        backtrackLimit = level + 1;
        BlockPos startingPos = new BlockPos(this.getPos().getX(), this.level, this.getPos().getZ());
        IBlockState targetState = IFlowBlock.stateWithFlowHeight(NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK.getDefaultState(), FlowHeightState.BLOCK_LEVELS_INT);
        this.worldObj.setBlockState(startingPos, targetState);
        lavaManager.trackLavaAt(startingPos, startingPos, FlowHeightState.BLOCK_LEVELS_INT, true);
    }

//    private void doFlowing()
//    {
//        OpenSpace place = spaceManager.pollFirst();
//        int y = place.getPos().getY();
//        if(y < this.backtrackLimit)
//        {
//            placeIfPossible(place.getPos(), place.getOrigin(), place.isDescending());
//            this.buildLevel = Math.min(buildLevel, y);
//            this.backtrackLimit = Math.min(backtrackLimit, y + Config.volcano().backtrackIncrement);
//        }
//    }

    private void doCooling()
    {
        BlockPlacement placement = coolingBlocks.pollFirstReadyEntry(this.ticksActive);

        while(placement != null)
        {
            BlockPos target = placement.getPos();

            IBlockState oldState = this.worldObj.getBlockState(target);

            if(IFlowBlock.isFlowBlock(oldState.getBlock()))
            {
                NiceBlock oldBlock = (NiceBlock)oldState.getBlock();

                //lava blocks should only be cooled as part of lava cooling
         
                NiceBlock newBlock = getNextCoolingBlock(oldBlock);
                int meta = oldState.getValue(NiceBlock.META);

                if(newBlock != null)
                {
                    if(newBlock == NiceBlockRegistrar.COOL_FLOWING_BASALT_HEIGHT_BLOCK
                            && IFlowBlock.shouldBeFullCube(oldState, worldObj, target))
                    {
                        //                            Adversity.log.info("doCooling: set block from " 
                        //                                    + worldObj.getBlockState(target).getBlock().getRegistryName() + " to " 
                        //                                    + NiceBlockRegistrar.COOL_SQUARE_BASALT_BLOCK.getRegistryName() + " @ " + target.toString());

                        this.worldObj.setBlockState(target, NiceBlockRegistrar.COOL_SQUARE_BASALT_BLOCK.getDefaultState()
                                .withProperty(NiceBlock.META, meta));
                    }
                    else
                    {
                        //                            Adversity.log.info("doCooling: set block from " 
                        //                                    + worldObj.getBlockState(target).getBlock().getRegistryName() + " to " 
                        //                                    + newBlock.getRegistryName() + " @ " + target.toString());

                        this.worldObj.setBlockState(target, newBlock.getDefaultState().withProperty(NiceBlock.META, meta));
                    }

                    if(!(newBlock == NiceBlockRegistrar.COOL_FLOWING_BASALT_HEIGHT_BLOCK
                            || newBlock == NiceBlockRegistrar.COOL_FLOWING_BASALT_FILLER_BLOCK))
                    {
                        coolingBlocks.add(target, ticksActive + Config.volcano().coolingLagTicks);
                    } 
                }
            }

            placement = coolingBlocks.pollFirstReadyEntry(this.ticksActive);
        }
    }

    private int getRandomBlockTicks()
    {
        if(Config.volcano().blockOperationsPerTick > 1) return 0;

        return Config.volcano().baseTicksPerBlock
                + (Config.volcano().randTicksPerBlock > 0 ? Useful.SALT_SHAKER.nextInt(Config.volcano().randTicksPerBlock) : 0);
    }

//    private void doLavaCooling()
//    {
//        if(lavaCooldownTicks > 0 && Config.volcano().blockOperationsPerTick == 1)
//        {
//            lavaCooldownTicks--;
//        }
//        else if(!lavaBlocks.isEmpty())
//        {
//            lavaCooldownTicks = getRandomBlockTicks();
//
//            BlockPlacement placement = lavaBlocks.pollLastEntry();
//
//            if(placement != null)
//            {
//                BlockPos target = placement.getPos();
//
//                IBlockState oldState = this.worldObj.getBlockState(target);
//
//                if(oldState.getBlock() instanceof NiceBlock && !isWithinBore(target))
//                {
//                    NiceBlock oldBlock = (NiceBlock)oldState.getBlock();
//
//                    if(oldBlock == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK
//                            || oldBlock == NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK)
//                    {   
//                        NiceBlock newBlock = oldBlock == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK
//                                ? NiceBlockRegistrar.HOT_FLOWING_BASALT_3_HEIGHT_BLOCK
//                                        :NiceBlockRegistrar.HOT_FLOWING_BASALT_3_FILLER_BLOCK;
//
//                        int meta = oldState.getValue(NiceBlock.META);
//                        IBlockState newState = newBlock.getDefaultState().withProperty(NiceBlock.META, meta);
//
//                        //                        Adversity.log.info("doLavaCooling: set block from " 
//                        //                                + worldObj.getBlockState(target).getBlock().getRegistryName() + " to " 
//                        //                                + newBlock.getRegistryName() + " @ " + target.toString());
//
//                        this.worldObj.setBlockState(target, newState);
//                        this.coolingBlocks.add(target, this.ticksActive + Config.volcano().coolingLagTicks);                      
//                    }
//                }
//            }
//        }
//    }

    private NiceBlock getNextCoolingBlock(Block blockIn)
    {
        if(blockIn == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK)
            return NiceBlockRegistrar.HOT_FLOWING_BASALT_3_HEIGHT_BLOCK;
        else if(blockIn == NiceBlockRegistrar.HOT_FLOWING_BASALT_3_HEIGHT_BLOCK)
            return NiceBlockRegistrar.HOT_FLOWING_BASALT_2_HEIGHT_BLOCK;
        else if(blockIn == NiceBlockRegistrar.HOT_FLOWING_BASALT_2_HEIGHT_BLOCK)
            return NiceBlockRegistrar.HOT_FLOWING_BASALT_1_HEIGHT_BLOCK;
        else if(blockIn == NiceBlockRegistrar.HOT_FLOWING_BASALT_1_HEIGHT_BLOCK)
            return NiceBlockRegistrar.HOT_FLOWING_BASALT_0_HEIGHT_BLOCK;
        else if(blockIn == NiceBlockRegistrar.HOT_FLOWING_BASALT_0_HEIGHT_BLOCK)
            return NiceBlockRegistrar.COOL_FLOWING_BASALT_HEIGHT_BLOCK;
        
        else if(blockIn == NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK)
            return NiceBlockRegistrar.HOT_FLOWING_BASALT_3_FILLER_BLOCK;
        else if(blockIn == NiceBlockRegistrar.HOT_FLOWING_BASALT_3_FILLER_BLOCK)
            return NiceBlockRegistrar.HOT_FLOWING_BASALT_2_FILLER_BLOCK;
        else if(blockIn == NiceBlockRegistrar.HOT_FLOWING_BASALT_2_FILLER_BLOCK)
            return NiceBlockRegistrar.HOT_FLOWING_BASALT_1_FILLER_BLOCK;
        else if(blockIn == NiceBlockRegistrar.HOT_FLOWING_BASALT_1_FILLER_BLOCK)
            return NiceBlockRegistrar.HOT_FLOWING_BASALT_0_FILLER_BLOCK;
        else if(blockIn == NiceBlockRegistrar.HOT_FLOWING_BASALT_0_FILLER_BLOCK)
            return NiceBlockRegistrar.COOL_FLOWING_BASALT_FILLER_BLOCK;

        else return null;

    }


//    private void placeIfPossible(BlockPos pPos, BlockPos pOrigin, boolean isDescending)
//    {
//        boolean inBore = isWithinBore(pPos);
//        if(inBore)
//        {
//            clearBore(pPos);
//        }
//        
////        //don't place on top of flowing lava unless descending from a height block or at top of vocano
////        if(pPos.getY() < this.level 
////                && this.worldObj.getBlockState(pPos.up()).getBlock() != NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK
////                && this.worldObj.getBlockState(pPos.down()).getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK)
////        {
////            return;
////        }
//
//        if(this.canDisplace(pPos, true))
//        {
//            int distance = (int) Math.round(Math.sqrt(pPos.distanceSq(pOrigin)));
//            int maxDistance = isDescending ? FlowHeightState.BLOCK_LEVELS_INT : FlowHeightState.BLOCK_LEVELS_INT - 1;
//            if(distance > maxDistance) return;
//
//            // don't place lava if already there, but do the rest as if we had.
//            // necessary for placement to propagate properly from all origin blocks
//            if(worldObj.getBlockState(pPos).getBlock() != NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK)
//            {
//                IBlockState targetState = IFlowBlock.stateWithFlowHeight(NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK.getDefaultState(), 1);
//                this.worldObj.setBlockState(pPos, targetState);
//                trackLavaBlock(pPos);
//                adjustmentList.add(pPos);
//            }
//
//            raiseLavaToOrigin(pPos, pPos, pOrigin);
//       
//            BlockPos posDown = pPos.down();
//            if(canDisplace(posDown, true))
//            {
//                // don't spread sideways if can flow down or if already flowing down unless adjacent block is supported
//                spaceManager.add(posDown, posDown, !this.canDisplace(posDown.down(), true), false, true);
//            }
//            else if(distance < maxDistance)
//            {
//                // if still have height to spread, spread sideways
//                flowSideways(pPos, pOrigin, false);
//            }
//            else
//            {
//                // if not, try to flow down if basalt is below with an open side face
//                meltAndFlowSideways(posDown);
//            }
//        }
//    }

    /**
     * Recursively elevates lava blocks on line between fromPos and origin, including origin,
     * starting at posting startPos, based on distance to origin. From block is assumed to be one high.
     * EachOrigin should be distance high, up to the max. 
     * If lava block is already as high as distance would indicate, does not affect it.
     */
    private void raiseLavaToOrigin(BlockPos fromPos, BlockPos startPos, BlockPos origin)
    {
        //methods below wants 3d not 3i, and also need to go from middle of blocks
        Vec3d from3d = new Vec3d(0.5 + fromPos.getX(), 0.5 + fromPos.getY(), 0.5 + fromPos.getZ());
        Vec3d to3d = new Vec3d(0.5 + origin.getX(), 0.5 + origin.getY(), 0.5 + origin.getZ());
        
        int distanceSquaredToOrigin = Useful.squared(origin.getX() - startPos.getX()) 
                + Useful.squared(origin.getZ() - startPos.getZ());
        
        Vec3d direction = to3d.subtract(from3d);
        for(int i = 0; i < HorizontalFace.values().length; i++)
        {
            BlockPos testPos = startPos.add(HorizontalFace.values()[i].directionVector);
            
            //block has to be closer to origin than the starting position
            if(distanceSquaredToOrigin > Useful.squared(origin.getX() - testPos.getX()) 
                    + Useful.squared(origin.getZ() - testPos.getZ()))
            {
                //Use AABB slightly larger than block to handle case of 45deg angle
                //Otherwise would need special handling for diagonals and adjacent in that case.
                AxisAlignedBB box = new AxisAlignedBB(-0.1 + testPos.getX(), testPos.getY(), -0.1 + testPos.getZ(),
                1.1 + testPos.getX(), 1 + testPos.getY(), 1.1 + testPos.getZ());
                if(Useful.doesRayIntersectAABB(from3d, direction, box))
                {
                    IBlockState state = this.worldObj.getBlockState(testPos);
                    if(state.getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK)
                    {
                        int distance = (int) Math.round(Math.sqrt(fromPos.distanceSq(testPos)));
                        int newHeight = Math.max(IFlowBlock.getFlowHeightFromState(state), Math.min(distance + 1,  FlowHeightState.BLOCK_LEVELS_INT));
                        this.worldObj.setBlockState(testPos, IFlowBlock.stateWithFlowHeight(state, newHeight));
                        adjustmentList.add(testPos);
    
                        if(!testPos.equals(origin))
                        {
                            raiseLavaToOrigin(fromPos, testPos, origin);
                        }
                    }
                }
            }
        }
    }
    
    private void doAdjustments()
    {
        HashSet<BlockPos> targets = new HashSet<BlockPos>();
    
        for(BlockPos changed : adjustmentList)
        {
            targets.add(changed.east());
            targets.add(changed.west());
            targets.add(changed.north());
            targets.add(changed.south());
            targets.add(changed.north().east());
            targets.add(changed.south().east());
            targets.add(changed.north().west());
            targets.add(changed.south().west());
        }

        adjustmentList.clear();
        
        for(BlockPos target : targets)
        {
            for(int y = -4; y <= 4; y++)
            {
                BlockPos p = target.add(0, y, 0);
                if(!adjustHeightBlockIfNeeded(p));
                {
                    adjustFillIfNeeded(p);
                }
            }
        }
    }
    
    /** 
     * Flow into adjacent sides that are lower than the block supporting this one
     */
//    private void flowSideways(BlockPos pos, BlockPos origin, boolean isDescending)
//    {
//        int myLevel = IFlowBlock.getFlowHeightFromState(worldObj.getBlockState(pos));
//        double minDistance = -0.5 + Math.sqrt(Useful.squared(pos.getX() - origin.getX()) + Useful.squared(pos.getZ() - origin.getZ()));
//        int neighborLevels[] = new int[4];
//        int minLevel = Integer.MIN_VALUE;
//        
//        for(int i = 0; i < HorizontalFace.values().length; i++)
//        {
//            BlockPos target = pos.add(HorizontalFace.values()[i].directionVector);
//            double targetDistance = Math.sqrt(Useful.squared(target.getX() - origin.getX()) + Useful.squared(target.getZ() - origin.getZ()));
//            
//            //force tall height on blocks that are in direction of origin so that  we don't flow into them
//            if(targetDistance < minDistance)
//            {
//                neighborLevels[i] = Integer.MAX_VALUE;
//            }
//            else
//            {
//                neighborLevels[i] =  getSupportingLevel(pos.add(HorizontalFace.values()[i].directionVector));
//                minLevel = Math.max(minLevel, neighborLevels[i]);
//            }
//        }
//        
//        int salt = Useful.SALT_SHAKER.nextInt(4);
//        for(int i = 0; i < HorizontalFace.values().length; i++)
//        {
//            int saltyIndex = (i + salt) % 4;
//            BlockPos target = pos.add(HorizontalFace.values()[saltyIndex].directionVector);
//            
//            //prefer places where we can flow down a level
//            //failing that, just make sure lower than current
//            if((minLevel >= 0 && neighborLevels[saltyIndex] <= myLevel) || neighborLevels[saltyIndex] < 0)
//            {
//                spaceManager.add(target, origin, this.canDisplace(target.down(), false), pos.getY() == this.level, true);
//            }
//        }
//    }
    
//    /**
//     * Retrieves flow height for purpose of determining flow direction
//     */
//    private int getSupportingLevel(BlockPos pos)
//    {
//        IBlockState myState = this.worldObj.getBlockState(pos);
//        if(IFlowBlock.isFlowBlock(myState.getBlock()))
//        {
//            return IFlowBlock.getFlowHeightFromState(myState);
//        }
//        else
//        {
//            BlockPos downPos = pos.down();
//            IBlockState downState = this.worldObj.getBlockState(downPos);
//            if(IFlowBlock.isFlowBlock(downState.getBlock()))
//            {
//                return IFlowBlock.getFlowHeightFromState(downState) - FlowHeightState.BLOCK_LEVELS_INT;
//            }
//            else
//            {
//                //if non-displacable block below, count as full block
//                return canDisplace(downPos, false) ? -FlowHeightState.BLOCK_LEVELS_INT : 0;
//            }
//        }
//    }
    
    /**
     * If given block is basalt and has open adjacent spaces, turn it into a lava block and fly sideways
     */
//    private void meltAndFlowSideways(BlockPos pos)
//    {
//        IBlockState state = this.worldObj.getBlockState(pos);
//        Block block = state.getBlock();
//        if(block instanceof NiceBlock && ((NiceBlock)block).material == BaseMaterial.BASALT)
//        {
//            if(canDisplace(pos.east(), true) || canDisplace(pos.west(), true) 
//                    || canDisplace(pos.north(), true) || canDisplace(pos.south(), true))
//            {
//                worldObj.setBlockState(pos, 
//                        IFlowBlock.stateWithFlowHeight(NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK.getDefaultState(),
//                        12));
//                trackLavaBlock(pos);
//                adjustmentList.add(pos);
//                flowSideways(pos, pos, true);
//            }
//        }
//    }
    
    private void clearBore(BlockPos clearPos)
    {
        if(!this.worldObj.isAirBlock(clearPos) 
                && !(this.worldObj.getBlockState(clearPos).getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK))
        {
            //            Adversity.log.info("clearBore: set block from "
            //                    + worldObj.getBlockState(clearPos).getBlock().getRegistryName() 
            //                    + " to air @ " + clearPos.toString());

            this.worldObj.setBlockToAir(clearPos);
            if(clearPos.getY() < this.groundLevel)
            {
                buildMound();
            }
        }
    }

    private void buildMound()
    {
        BlockPos top = findMoundSpot();
        if(top == null) return;

        //allow drops of trees and such
        this.worldObj.destroyBlock(top.up(), true); 

        IBlockState state = worldObj.getBlockState(top);

        while(!LavaManager.canDisplace(state) && state.getBlock() != Blocks.BEDROCK
                && top.getY() >= 0)
        {
            //            Adversity.log.info("buildMound: set block from " 
            //                    + worldObj.getBlockState(top.up()).getBlock().getRegistryName() + " to " 
            //                    + state.getBlock().getRegistryName() + " @ " + top.up().toString());

            this.worldObj.setBlockState(top.up(), state);
            top = top.down();
            state = this.worldObj.getBlockState(top);
        }
        //avoid duplication of valuable blocks by clever nerds

        //        Adversity.log.info("buildMound: set block from " 
        //                + worldObj.getBlockState(top.up()).getBlock().getRegistryName() + " to " 
        //                + state.getBlock().getRegistryName() + " @ " + top.up().toString());

        this.worldObj.setBlockState(top.up(), worldObj.getBiome(top).fillerBlock);
    }

    private BlockPos findMoundSpot()
    {
        int dx = (int) (Useful.SALT_SHAKER.nextGaussian() * Config.volcano().moundRadius);
        int dz = (int) (Useful.SALT_SHAKER.nextGaussian() * Config.volcano().moundRadius);
        double lastDistance = 0;
        BlockPos center = new BlockPos(this.pos.getX(), this.level, this.pos.getZ());
        BlockPos best = null;
        for(int i = 0; i < 20; i++)
        {
            dx = (int) (Useful.SALT_SHAKER.nextGaussian() * Config.volcano().moundRadius);
            dz = (int) (Useful.SALT_SHAKER.nextGaussian() * Config.volcano().moundRadius);
            BlockPos candidate = this.worldObj.getHeight(this.pos.east(dx).north(dz));
            while(candidate.getY() > 0 && !isVolcanoBlock(worldObj.getBlockState(candidate).getBlock()) 
                    && LavaManager.canDisplace(worldObj.getBlockState(candidate)))
            {
                candidate = candidate.down();
            }
            if(!isVolcanoBlock(worldObj.getBlockState(candidate).getBlock()))
            {
                if(best == null)
                {
                    best = candidate;
                    lastDistance = candidate.distanceSq(center);
                }
                else
                {
                    double newDistance = candidate.distanceSq(center);
                    if(newDistance < lastDistance)
                    {
                        lastDistance = newDistance;
                        best = candidate;
                    }
                }
            }
        }
        return best;
    }

    private boolean isVolcanoBlock(Block block)
    {
        if(!(block instanceof NiceBlock)) return false;
        BaseMaterial material = ((NiceBlock)block).material;
        return (material == BaseMaterial.BASALT || material == BaseMaterial.VOLCANIC_LAVA);
    }

    /**
     * Melts static height blocks if geometry would be different from current.
     * And turns full cube dynamic blocks into static cube blocks.
     * Returns true if is a height block, even if no adjustement was needed.
     */
    private boolean adjustHeightBlockIfNeeded(BlockPos targetPos)
    {

        if(targetPos == null) return false;

        //        Adversity.log.info("meltExposedBasalt @" + targetPos.toString());        

        IBlockState state = worldObj.getBlockState(targetPos);
        if(!(state.getBlock() instanceof NiceBlock)) return false;

        NiceBlock block = (NiceBlock)state.getBlock();

        if(!IFlowBlock.isFlowHeight(block)) return false;

        boolean isFullCube = IFlowBlock.shouldBeFullCube(state, worldObj, targetPos);


        if(isFullCube)
        {
            if(block == NiceBlockRegistrar.COOL_FLOWING_BASALT_HEIGHT_BLOCK)
            {
                //                Adversity.log.info("adjustHeightBlockIfNeeded: set block from " 
                //                        + worldObj.getBlockState(targetPos).getBlock().getRegistryName() + " to " 
                //                        + NiceBlockRegistrar.COOL_SQUARE_BASALT_BLOCK.getRegistryName() + " @ " + targetPos.toString());
                this.worldObj.setBlockState(targetPos, NiceBlockRegistrar.COOL_SQUARE_BASALT_BLOCK.getDefaultState()
                        .withProperty(NiceBlock.META, state.getValue(NiceBlock.META)));
            }
        }
        else if (block == NiceBlockRegistrar.COOL_STATIC_BASALT_HEIGHT_BLOCK 
                || block == NiceBlockRegistrar.COOL_SQUARE_BASALT_BLOCK 
                || block == NiceBlockRegistrar.COOL_FLOWING_BASALT_HEIGHT_BLOCK)
        {

            //            Adversity.log.info("adjustHeightBlockIfNeeded: set block from " 
            //                    + worldObj.getBlockState(targetPos).getBlock().getRegistryName() + " to " 
            //                    + NiceBlockRegistrar.COOL_FLOWING_BASALT_HEIGHT_BLOCK.getRegistryName() + " @ " + targetPos.toString());


            this.worldObj.setBlockState(targetPos, NiceBlockRegistrar.HOT_FLOWING_BASALT_3_HEIGHT_BLOCK.getDefaultState()
                    .withProperty(NiceBlock.META, state.getValue(NiceBlock.META)));
            this.coolingBlocks.add(targetPos, ticksActive + Config.volcano().coolingLagTicks);
        }

        return true;
    }

//    private void trackLavaBlock(BlockPos lavaPos)
//    {
//        //blocks within bore never cool so no reason to track them
//        if(isWithinBore(lavaPos)) return;
//
//        if(lavaPos.getY() >= level)
//            this.topBlocks.add(lavaPos, ++lavaCounter);
//        else
//            this.lavaBlocks.add(lavaPos, ++lavaCounter);
//    }


    /**
     * Adds or removes filler blocks as needed.
     * Also replaces static filler blocks with dynamic version.
     * @param basePos
     */
    private void adjustFillIfNeeded(BlockPos basePos)
    {
        final int SHOULD_BE_AIR = -1;

        IBlockState baseState = this.worldObj.getBlockState(basePos);
        Block baseBlock = baseState.getBlock();
        NiceBlock fillBlock = null;

        int targetMeta = SHOULD_BE_AIR;

        /**
         * If space is occupied with a non-displaceable block, will be ignored.
         * Otherwise, possible target states: air, fill +1, fill +2
         * 
         * Should be fill +1 if block below is a heightblock and needs a fill >= 1;
         * Should be a fill +2 if block below is not a heightblock and block
         * two below needs a fill = 2;
         * Otherwise should be air.
         */
        IBlockState stateBelow = this.worldObj.getBlockState(basePos.down());
        if(IFlowBlock.isFlowHeight(stateBelow.getBlock()) 
                && IFlowBlock.topFillerNeeded(stateBelow, worldObj, basePos.down()) > 0)
        {
            targetMeta = 0;
            fillBlock = NiceBlockRegistrar.getFillerBlock(stateBelow.getBlock());
        }
        else 
        {
            IBlockState stateTwoBelow = this.worldObj.getBlockState(basePos.down(2));
            if((IFlowBlock.isFlowHeight(stateTwoBelow.getBlock()) 
                    && IFlowBlock.topFillerNeeded(stateTwoBelow, worldObj, basePos.down(2)) == 2))
            {
                targetMeta = 1;
                fillBlock = NiceBlockRegistrar.getFillerBlock(stateTwoBelow.getBlock());
            }
        }

        if(IFlowBlock.isFlowFiller(baseBlock))
        {

            if(targetMeta == SHOULD_BE_AIR)
            {
                worldObj.setBlockToAir(basePos);
            }
            else if(baseState.getValue(NiceBlock.META) != targetMeta || baseBlock != fillBlock && fillBlock != null)
            {
             
                worldObj.setBlockState(basePos, fillBlock.getDefaultState()
                        .withProperty(NiceBlock.META, targetMeta));

//                if(fillBlock == NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK)
//                {
//                    trackLavaBlock(basePos);
//                }
//                else
//                {
                    //TODO: don't add to cooling if not needed
                    coolingBlocks.add(basePos, ticksActive + Config.volcano().coolingLagTicks);
//                }
            }
            //confirm filler needed and adjust/remove if needed
        }
        else if(targetMeta != SHOULD_BE_AIR && LavaManager.canDisplace(baseState) && fillBlock != null)
        {
            worldObj.setBlockState(basePos, fillBlock.getDefaultState()
                    .withProperty(NiceBlock.META, targetMeta));

//            if(fillBlock == NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK)
//            {
//                trackLavaBlock(basePos);
//            }
//            else
//            {
                //TODO: don't add to cooling if not needed
                coolingBlocks.add(basePos, ticksActive + Config.volcano().coolingLagTicks);
//            }
            //            }
        }

    }

    //	@Override
    //	public void updateOld() {
    //	    if(this.worldObj.isRemote) return;
    //	    
    //	    
    //	    // dead volcanoes don't do anything
    //	    if(stage == VolcanoStage.DEAD) return;
    //        
    //        if(weight < Integer.MAX_VALUE) weight++;
    //	    
    //        // everything after this point only happens 1x every 16 ticks.
    //        if ((this.worldObj.getTotalWorldTime() & 15) != 15) return;
    //
    //        this.markDirty();
    //
    // //       if (node.isActive()) {
    //
    //            this.markDirty();
    //
    //			if ((this.worldObj.getTotalWorldTime() & 255) == 255) {
    //				Adversity.log.info("Volcanot State @" + this.pos.toString() + " = " + this.stage);
    //			}
    //
    //			this.makeHaze();
    //			
    //			if (this.stage == VolcanoStage.NEW) 
    //			{
    //				this.level = this.getPos().getY();
    //
    //				++this.level;
    //
    //				this.levelsTilDormant = 80 - this.level;
    //				this.stage = VolcanoStage.NEW_LEVEL;
    //			}
    //			else if (this.stage == VolcanoStage.DORMANT) 
    //			{
    //
    //			    // Simulation shouldn't reactivate a volcano that is already at build limit
    //			    // but deactivate if it somehow does.
    //				if (this.level >= VolcanoManager.VolcanoNode.MAX_VOLCANO_HEIGHT) 
    //				{
    //				    this.weight = 0;
    //				    this.stage = VolcanoStage.DEAD;
    //					this.node.deActivate();
    //					return;
    //				}
    //				
    //				int window = VolcanoManager.VolcanoNode.MAX_VOLCANO_HEIGHT - this.level;
    //				int interval = Math.min(1, window / 10);
    //				
    //                // always grow at least 10% of the available window
    //                // plus 0 to 36% with a total average around 28%
    //				this.levelsTilDormant = Math.min(window,
    //				        interval
    //				        + this.worldObj.rand.nextInt(interval)
    //				        + this.worldObj.rand.nextInt(interval)
    //				        + this.worldObj.rand.nextInt(interval)
    //				        + this.worldObj.rand.nextInt(interval));
    //
    //				if (this.level >= 70) {
    //					this.blowOut(4, 5);
    //				}
    //				this.stage = VolcanoStage.BUILDING_INNER;
    //			} 
    //			else if (this.stage == VolcanoStage.NEW_LEVEL) 
    //			{
    //				if (!this.areInnerBlocksOpen(this.level)) {
    //				    this.worldObj.createExplosion(null, this.pos.getX(), this.level - 1, this.pos.getZ(), 5, true);
    //				}
    //				this.stage = VolcanoStage.BUILDING_INNER;
    //			} 
    //			else if (this.stage == VolcanoStage.BUILDING_INNER) 
    //			{
    //				if (this.buildLevel <= this.pos.getY() || this.buildLevel > this.level) {
    //					this.buildLevel = this.pos.getY() + 1;
    //				}
    //				Useful.fill2dCircleInPlaneXZ(this.worldObj, this.pos.getX(), this.buildLevel, this.pos.getZ(), 3,
    //						Volcano.blockVolcanicLava.getDefaultState());
    //				if (this.buildLevel < this.level) {
    //					++this.buildLevel;
    //				} else {
    //					this.buildLevel = 0;
    //					this.stage = VolcanoStage.TESTING_OUTER;
    //				}
    //			}
    //			else if (this.stage == VolcanoStage.TESTING_OUTER)
    //			{
    //				if (this.areOuterBlocksOpen(this.level))
    //				{
    //					this.stage = VolcanoStage.BUILDING_INNER;
    //				}
    //				else if (this.levelsTilDormant == 0)
    //				{
    //					this.stage = VolcanoStage.DORMANT;
    //					if (this.level >= VolcanoManager.VolcanoNode.MAX_VOLCANO_HEIGHT) 
    //	                {
    //	                    this.weight = 0;
    //	                    this.stage = VolcanoStage.DEAD;
    //	                }
    //					this.node.deActivate();
    //				}
    //				else 
    //				{
    //					++this.level;
    //					--this.levelsTilDormant;
    //					this.stage = VolcanoStage.NEW_LEVEL;
    //				}
    //			}
    ////		}
    //        
    //        node.updateWorldState(this.weight, this.level);
    //	}

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) 
    {
        super.readFromNBT(tagCompound);

        Adversity.log.info("readNBT volcanoTile");

        this.stage = VolcanoStage.values()[tagCompound.getInteger("stage")];
        this.level = tagCompound.getInteger("level");
        this.buildLevel = tagCompound.getInteger("buildLevel");
        this.groundLevel = tagCompound.getInteger("groundLevel");
        this.ticksActive = tagCompound.getInteger("ticksActive");
        this.backtrackLimit = tagCompound.getInteger("backtrackLimit");
        this.lavaCounter = tagCompound.getInteger("lavaCounter");
        this.lavaCooldownTicks = tagCompound.getInteger("cooldownTicks");

        this.lavaManager = null;
        this.lavaManagerData = tagCompound.getIntArray("lavaManager");
//        this.spaceManager = new SpaceManager(this.pos, tagCompound.getIntArray("spaceManager"));
//        this.lavaBlocks = new BlockManager(this.pos, true, tagCompound.getIntArray("lavaBlocks"));
//        this.topBlocks = new BlockManager(this.pos, true, tagCompound.getIntArray("topBlocks"));
        this.coolingBlocks = new BlockManager(this.pos, false, tagCompound.getIntArray("coolingBlocks"));
        this.adjustmentList = new HashSet<BlockPos>();

        this.nodeId = tagCompound.getInteger("nodeId");


        //this.hazeMaker.readFromNBT(tagCompound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) 
    {        
        tagCompound.setInteger("stage", this.stage.ordinal());
        tagCompound.setInteger("level", this.level);
        tagCompound.setInteger("buildLevel", this.buildLevel);
        tagCompound.setInteger("groundLevel", this.groundLevel);
        tagCompound.setInteger("ticksActive", this.ticksActive);
        tagCompound.setInteger("backtrackLimit", this.backtrackLimit);
        tagCompound.setInteger("lavaCounter", this.lavaCounter);
        tagCompound.setInteger("cooldownTicks", lavaCooldownTicks);

//        tagCompound.setIntArray("spaceManager", this.spaceManager.getArray());
//        tagCompound.setIntArray("lavaBlocks", this.lavaBlocks.getArray());
//        tagCompound.setIntArray("topBlocks", this.topBlocks.getArray());
        tagCompound.setIntArray("lavaManager", this.lavaManager.getArray());

        tagCompound.setIntArray("coolingBlocks", this.coolingBlocks.getArray());      

        if(this.node != null) tagCompound.setInteger("nodeId", this.node.getID());
        return super.writeToNBT(tagCompound);

        //this.hazeMaker.writeToNBT(tagCompound);

    }

    /**
     * Client doesn't do anything.
     */
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return null;
    }

}