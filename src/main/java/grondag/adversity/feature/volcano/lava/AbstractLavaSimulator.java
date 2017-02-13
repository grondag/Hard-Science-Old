package grondag.adversity.feature.volcano.lava;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.CoolingBlock;
import grondag.adversity.feature.volcano.lava.ParticleManager.ParticleInfo;
import grondag.adversity.library.PackedBlockPos;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import grondag.adversity.simulator.base.NodeRoots;
import grondag.adversity.simulator.base.SimulationNode;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public abstract class AbstractLavaSimulator extends SimulationNode
{
    public static final ForkJoinPool LAVA_THREAD_POOL = new ForkJoinPool();
    
    public static final byte LEVELS_PER_BLOCK = FlowHeightState.BLOCK_LEVELS_INT;
    public static final byte LEVELS_PER_QUARTER_BLOCK = FlowHeightState.BLOCK_LEVELS_INT / 4;
    public static final byte LEVELS_PER_HALF_BLOCK = FlowHeightState.BLOCK_LEVELS_INT / 2;
    public static final byte LEVELS_PER_BLOCK_AND_A_QUARTER = LEVELS_PER_BLOCK + LEVELS_PER_QUARTER_BLOCK;
    public static final byte LEVELS_PER_BLOCK_AND_A_HALF = LEVELS_PER_BLOCK + LEVELS_PER_HALF_BLOCK;
    public static final byte LEVELS_PER_TWO_BLOCKS = LEVELS_PER_BLOCK * 2;
    public static final int FLUID_UNITS_PER_LEVEL = 1000;
    public static final int FLUID_UNITS_PER_BLOCK = FLUID_UNITS_PER_LEVEL * LEVELS_PER_BLOCK;
    public static final int FLUID_UNTIS_PER_HALF_BLOCK = FLUID_UNITS_PER_BLOCK / 2;
    protected static final int BLOCK_COOLING_DELAY_TICKS = 20;

    protected final WorldStateBuffer worldBuffer;
    protected final LavaTerrainHelper terrainHelper;
    protected final ParticleManager particles = new ParticleManager();
    
    /** Basalt blocks that are awaiting cooling */
    protected final Set<AgedBlockPos> basaltBlocks = ConcurrentHashMap.newKeySet();
    protected final static String BASALT_BLOCKS_NBT_TAG = "basaltblock"; 
    protected static final int BASALT_BLOCKS_NBT_WIDTH = 4;
    
    //TODO: consider switching back to BlockPos - if going to be going back to world or checks would avoid reinstantiation each time
    /** Filler lava blocks that need to be cooled with lava cells but aren't involved in the fluid simulation. */
    protected final Set<Long> lavaFillers = ConcurrentHashMap.newKeySet();
    protected final static String LAVA_FILLER_NBT_TAG = "lavafillers"; 
    protected static final int LAVA_FILLER_NBT_WIDTH = 2;
    
    /** Set true when doing block placements so we known not to register them as newly placed lava. */
    protected boolean itMe = false;

    protected int tickIndex = 0;
    protected final static String TICK_INDEX_NBT_TAG = "tickindex"; 
    
    // performance counters
    // TODO: remove for release
    private long connectionProcessTime;
    private int connectionProcessCount;
    private long coolingTime;
    private long cellUpdateTime = 0;
    private long particleTime;
    private long validationTime;
    private long blockUpdateProvisionTime;
    private long blockUpdateApplicationTime;

    public AbstractLavaSimulator(World world)
    {
        super(NodeRoots.LAVA_SIMULATOR.ordinal());
        this.worldBuffer = new WorldStateBuffer(world);
        this.terrainHelper = new LavaTerrainHelper(world);
    }

    /**
    * Signal to let volcano know should switch to cooling mode.
    * 1 or higher means overloaded.
    */
    public abstract float loadFactor();
    
    public abstract int getCellCount();
    public abstract int getConnectionCount();

    public abstract void saveLavaNBT(NBTTagCompound nbt);
    public abstract void readLavaNBT(NBTTagCompound nbt);

    /** adds lava to the surface of the cell containing the given block position */
    public abstract void addLava(long packedBlockPos, int amount, boolean shouldResynchToWorldBeforeAdding);
    
    /**
     * Adds lava in or on top of the given cell.
     * TODO: handle when not all the lava can be used.
     * Should only force resynch with world when you know you removed a barrier and simulation
     * needs to know the cell is now open.  Otherwise if this addition is occurs
     * after an earlier one but before block update resynch will cause earlier addition to be lost.
     */
    public void addLava(BlockPos pos, int amount, boolean shouldResynchToWorldBeforeAdding)
    {
        this.addLava(PackedBlockPos.pack(pos), amount, shouldResynchToWorldBeforeAdding);
    }
    
    /**
     * Update simulation from world when a block next to a lava block is changed.
     * Does this by creating or validating (if already existing) cells for 
     * the notified block and all adjacent blocks.
     * Unfortunately, this will ALSO be called by our own block updates, 
     * so ignores call if visible level already matches.
     */
    public abstract void notifyLavaNeighborChange(World worldIn, BlockPos pos, IBlockState state);

    /**
     * Update simulation from world when blocks are removed via creative mode or other methods.
     * Unfortunately, this will ALSO be called by our own block updates, 
     * so ignores call if visible level already matches.
     */
    public abstract void unregisterDestroyedLava(World worldIn, BlockPos pos, IBlockState state);

    /**
     * Update simulation from world when blocks are placed via creative mode or other methods.
     * Unfortunately, this will ALSO be called by our own block updates, 
     * so ignores call if we are currently placing blocks.
     */
    public abstract void registerPlacedLava(World worldIn, BlockPos pos, IBlockState state);
    
    protected abstract void doFirstStep();
    protected abstract void doStep();
    protected abstract void doLastStep();
    protected abstract void doBlockUpdateProvision();
    protected abstract void doLavaCooling();
    
    /** true if the given position cannot contain lava */
    protected abstract boolean isBlockLavaBarrier(long packedBlockPos);
    
    /** true if the given space is high enough above lava surface to be worth spawing a particle */
    protected abstract boolean isHighEnoughForParticle(long packedBlockPos);
    
    /** use to maintain all the cells each tick after steps have executed */
    protected abstract void updateCells();
    
    /** handle periodic (not every tick) cell validation */
    protected abstract void doCellValidation();
    
    /** handle periodic (not every tick) connection validation */
    protected abstract void doConnectionValidation();

    public int getTickIndex()
    {
        return this.tickIndex;
    }
    
    /**
     * Updates fluid simulation for one game tick, provided the game clock has advanced at least one tick since last call.
     * Tick index is used internally to track which cells have changed and to control frequency of upkeep tasks.
     * Due to computationally intensive nature, does not do more work if game clock has advanced more than one tick.
     * To make lava flow more quickly, place more lava when clock advances.
     */
    public void doTick(int newLastTickIndex)
    {
        if(this.tickIndex < newLastTickIndex)
        {
            this.tickIndex++;
            if((this.tickIndex & 0xFF) == 0xFF)
            {
                Adversity.log.info("Particle time this sample = " + particleTime / 1000000);
                Adversity.log.info("Live particle count = " + EntityLavaParticle.getLiveParticleCount(this.worldBuffer.realWorld.getMinecraftServer()));
                particleTime = 0;

                Adversity.log.info("Cooling time this sample = " + coolingTime / 1000000);
                coolingTime = 0;
                
                Adversity.log.info("Validation time this sample = " + validationTime / 1000000);
                validationTime = 0;

                int provisionCount = this.worldBuffer.getStateSetCount();
                Adversity.log.info("Block update provision time this sample = " + blockUpdateProvisionTime / 1000000 
                        + " for " + provisionCount + " updates @ " + ((provisionCount > 0) ? (float)blockUpdateProvisionTime / provisionCount : "n/a") + " each");
                blockUpdateProvisionTime = 0;

                int applicationCount = this.worldBuffer.getStateApplicationCount();
                Adversity.log.info("Block update application time this sample = " + blockUpdateApplicationTime / 1000000 
                        + " for " + applicationCount + " updates @ " + ((applicationCount > 0) ? (float)blockUpdateApplicationTime / applicationCount : "n/a") + " each");
                blockUpdateApplicationTime = 0;
                
                this.worldBuffer.clearStatistics();
                
                Adversity.log.info("Connection flow proccessing time this sample = " + connectionProcessTime / 1000000 
                        + " for " + connectionProcessCount + " links @ " + ((connectionProcessCount > 0) ? (float)connectionProcessTime / connectionProcessCount : "n/a") + " each");
                connectionProcessCount = 0;
                connectionProcessTime = 0;
                
                Adversity.log.info("Cell update time this sample = " + cellUpdateTime / 1000000);
                cellUpdateTime = 0;

                Adversity.log.info("totalCells=" + this.getCellCount() 
                        + " connections=" + this.getConnectionCount() + " basaltBlocks=" + this.basaltBlocks.size() + " loadFactor=" + this.loadFactor());
            }
         
            this.connectionProcessCount += this.getConnectionCount();
            long startTime = System.nanoTime();
            // force processing on non-dirty connection at least once per tick
            this.doFirstStep();
            this.doStep();
            this.doStep();
            this.doStep();
            this.doStep();
            this.doStep();
            this.doStep();
            // update sort keys on last pass for resort next tick
            this.doLastStep();
            this.connectionProcessTime += (System.nanoTime() - startTime);

            startTime = System.nanoTime();
            this.updateCells();
            cellUpdateTime  += (System.nanoTime() - startTime);
            
            startTime = System.nanoTime();
            this.doParticles();
            this.particleTime += (System.nanoTime() - startTime);
            
            startTime = System.nanoTime();
            this.doBlockUpdateProvision();
            blockUpdateProvisionTime += (System.nanoTime() - startTime);
            
            startTime = System.nanoTime();
            this.doBlockUpdateApplication();
            blockUpdateApplicationTime += (System.nanoTime() - startTime);
            
            int tickSelector = this.tickIndex & 0xF;
         
            // do these on alternate ticks to help avoid ticks that are too long
            if(tickSelector == 4)
            {
                startTime = System.nanoTime();
                this.doLavaCooling();
                this.coolingTime += (System.nanoTime() - startTime);
            }
            else if(tickSelector == 8)
            {
                startTime = System.nanoTime();
                this.doBasaltCooling();
                this.coolingTime += (System.nanoTime() - startTime);
            }
            else if(tickSelector == 12)
            {
                startTime = System.nanoTime();
                this.doCellValidation();
                this.doConnectionValidation();
                this.validationTime += (System.nanoTime() - startTime);
            }
             this.setSaveDirty(true);
        }
    }

    protected void doParticles()
    {
       //TODO: make particle limit configurable
        int capacity =  10 - EntityLavaParticle.getLiveParticleCount(this.worldBuffer.realWorld.getMinecraftServer());
        
        if(capacity <= 0) return;
        
        Collection<ParticleInfo> particles = this.particles.pollEligible(this, capacity);
        
        if(particles != null && !particles.isEmpty())
        {
            for(ParticleInfo p : particles)
            {
            
                // abort on strangeness
                if(this.isBlockLavaBarrier(p.packedBlockPos)) continue;
                
                if(this.isHighEnoughForParticle(p.packedBlockPos))
                {
                    // Spawn in world, discarding particles that have aged out and aren't big enough to form a visible lava block
                    if(p.getFluidUnits() >= AbstractLavaSimulator.FLUID_UNITS_PER_LEVEL)
                    {
                        EntityLavaParticle elp = new EntityLavaParticle(this.worldBuffer.realWorld, p.getFluidUnits(), 
                              new Vec3d(
                                      PackedBlockPos.getX(p.packedBlockPos) + 0.5, 
                                      PackedBlockPos.getY(p.packedBlockPos) + 0.4, 
                                      PackedBlockPos.getZ(p.packedBlockPos) + 0.5
                                  ),
                              Vec3d.ZERO);
                        
                        worldBuffer.realWorld.spawnEntityInWorld(elp);
                    }
                }
                else 
                {
                    this.addLava(p.packedBlockPos, p.getFluidUnits(), false);
                }
            }
        }
    }
      
    protected void doBasaltCooling()
    {
        final int lastEligibleTick = this.tickIndex - BLOCK_COOLING_DELAY_TICKS;

        LAVA_THREAD_POOL.submit( () -> this.basaltBlocks.parallelStream().forEach(apos ->
        {
            if(apos.getTick() <= lastEligibleTick)
            {
                IBlockState state = this.worldBuffer.getBlockState(apos.pos);
                Block block = state.getBlock();
                if(block instanceof CoolingBlock)
                {
                    switch(((CoolingBlock)block).tryCooling(this.worldBuffer, apos.pos, state))
                    {
                        case PARTIAL:
                            // will be ready to cool again after delay
                            apos.setTick(this.tickIndex);
                            break;
                            
                        case UNREADY:
                            // do nothing and try again later
                            break;
                            
                        case COMPLETE:
                        case INVALID:
                        default:
                            //notify to remove from collection
                            basaltBlocks.remove(apos);
                    }
                }
                else
                {
                    basaltBlocks.remove(apos);
                }
            };
        })).join();     
    }
        
    protected void doBlockUpdateApplication()
    {
        this.itMe = true;
        this.worldBuffer.applyBlockUpdates(1, this);
        this.itMe = false;
    }
    
    /** used by world update to notify when fillers are placed */
    public void trackLavaFiller(BlockPos pos)
    {
        this.lavaFillers.add(PackedBlockPos.pack(pos));
    }
    
    /** used by world update to notify when fillers are placed */
    public void trackCoolingBlock(BlockPos pos)
    {
        this.basaltBlocks.add(new AgedBlockPos(pos, this.tickIndex));
    }
    
    /**
     * Update simulation from world when blocks are placed via creative mode or other methods.
     * Also called by random tick on cooling blocks so that they can't get permanently orphaned
     */
    public void registerCoolingBlock(World worldIn, BlockPos pos)
    {
        if(itMe) return;
        this.basaltBlocks.add(new AgedBlockPos(pos, this.tickIndex));
        this.setSaveDirty(true);
    }
    
    public void queueParticle(long packedBlockPos, int amount)
    {
//        Adversity.log.info("queueParticle amount=" + amount +" @"+ pos.toString());
        this.particles.addLavaForParticle(this, packedBlockPos, amount);
    }
    
    /**
     * Returns value to show if lava can cool based on world state alone. Does not consider age.
     */
    protected boolean canLavaCool(long packedBlockPos)
    {
        BlockPos pos = PackedBlockPos.unpack(packedBlockPos);
        
        Block block = worldBuffer.getBlockState(pos).getBlock();
        
        if(block == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK || block == NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK)
        {
            int hotNeighborCount = 0;
            BlockPos.MutableBlockPos nPos = new BlockPos.MutableBlockPos();
            
            for(EnumFacing face : EnumFacing.VALUES)
            {
                Vec3i vec = face.getDirectionVec();
                nPos.setPos(pos.getX() + vec.getX(), pos.getY() + vec.getY(), pos.getZ() + vec.getZ());
                
                block = worldBuffer.getBlockState(nPos).getBlock();
                if(block == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK || block == NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK)
                {
                    // don't allow top to cool until bottom does
                    if(face == EnumFacing.DOWN) return false;
                    
                    hotNeighborCount++;
                }
            }
            
            return hotNeighborCount < 4;
        }
        else
        {
            // Might be invisible lava (not big enough to be visible in world)
            return true;
        }
    }
    
    protected void coolLava(long packedBlockPos)
    {
        final IBlockState priorState = this.worldBuffer.getBlockState(packedBlockPos);
        Block currentBlock = priorState.getBlock();
        NiceBlock newBlock = null;
        if(currentBlock == NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK)
        {
            newBlock = NiceBlockRegistrar.HOT_FLOWING_BASALT_3_FILLER_BLOCK;
        }
        else if(currentBlock == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK)
        {
            newBlock = NiceBlockRegistrar.HOT_FLOWING_BASALT_3_HEIGHT_BLOCK;
        }

        if(newBlock != null)
        {
//            Adversity.log.info("Cooling lava @" + pos.toString());
            //should not need these any more due to world buffer
//            this.itMe = true;
            this.worldBuffer.setBlockState(packedBlockPos, newBlock.getDefaultState().withProperty(NiceBlock.META, priorState.getValue(NiceBlock.META)), priorState);
//            this.itMe = false;
            this.basaltBlocks.add(new AgedBlockPos(PackedBlockPos.unpack(packedBlockPos), this.tickIndex));
        }
    }
    
    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        
        nbt.setInteger(TICK_INDEX_NBT_TAG, this.tickIndex);
        
        this.saveLavaNBT(nbt);
        
        // SAVE FILLER CELLS
        {
            Adversity.log.info("Saving " + lavaFillers.size() + " filler cells.");
            int[] saveData = new int[lavaFillers.size() * LAVA_FILLER_NBT_WIDTH];
            int i = 0;
            for(long packedPos: lavaFillers)
            {
                saveData[i++] = (int) (packedPos & 0xFFFFFFFFL);
                saveData[i++] = (int) (packedPos >> 32);
            }       
            nbt.setIntArray(LAVA_FILLER_NBT_TAG, saveData);
        }
        
        // SAVE BASALT BLOCKS
        {
            Adversity.log.info("Saving " + basaltBlocks.size() + " cooling basalt blocks.");
            int[] saveData = new int[basaltBlocks.size() * BASALT_BLOCKS_NBT_WIDTH];
            int i = 0;
            for(AgedBlockPos apos: basaltBlocks)
            {
                saveData[i++] = apos.pos.getX();
                saveData[i++] = apos.pos.getY();
                saveData[i++] = apos.pos.getZ();
                saveData[i++] = apos.getTick();
            }       
            nbt.setIntArray(BASALT_BLOCKS_NBT_TAG, saveData);
            
            this.worldBuffer.writeToNBT(nbt);
            this.particles.writeToNBT(nbt);
        }

    }
    
    public void readFromNBT(NBTTagCompound nbt)
    {

        lavaFillers.clear();
        basaltBlocks.clear();
        
        this.tickIndex = nbt.getInteger(TICK_INDEX_NBT_TAG);
        
        this.worldBuffer.readFromNBT(nbt);
        this.particles.readFromNBT(nbt);
        
        this.readLavaNBT(nbt);
        
        // LOAD FILLER CELLS
        int[] saveData = nbt.getIntArray(LAVA_FILLER_NBT_TAG);

        //confirm correct size
        if(saveData == null || saveData.length % LAVA_FILLER_NBT_WIDTH != 0)
        {
            Adversity.log.warn("Invalid save data loading lava simulator. Filler blocks may not be updated properly.");
        }
        else
        {
            int i = 0;
            while(i < saveData.length)
            {
                this.lavaFillers.add((saveData[i++] & 0xFFFFFFFFL) | ((long)saveData[i++] << 32));
            }
            Adversity.log.info("Loaded " + lavaFillers.size() + " filler cells.");
        }
        
        // LOAD BASALT BLOCKS
        saveData = nbt.getIntArray(BASALT_BLOCKS_NBT_TAG);

        //confirm correct size
        if(saveData == null || saveData.length % BASALT_BLOCKS_NBT_WIDTH != 0)
        {
            Adversity.log.warn("Invalid save data loading lava simulator. Cooling basalt blocks may not be updated properly.");
        }
        else
        {
            int i = 0;
            while(i < saveData.length)
            {
                this.basaltBlocks.add(new AgedBlockPos(new BlockPos(saveData[i++], saveData[i++], saveData[i++]), saveData[i++]));
            }
            Adversity.log.info("Loaded " + basaltBlocks.size() + " cooling basalt blocks.");
        }

    }
}