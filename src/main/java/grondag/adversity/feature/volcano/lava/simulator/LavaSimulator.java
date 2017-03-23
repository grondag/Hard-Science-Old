package grondag.adversity.feature.volcano.lava.simulator;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.CoolingBlock;
import grondag.adversity.feature.volcano.lava.AgedBlockPos;
import grondag.adversity.feature.volcano.lava.EntityLavaParticle;
import grondag.adversity.feature.volcano.lava.LavaTerrainHelper;
import grondag.adversity.feature.volcano.lava.WorldStateBuffer;
import grondag.adversity.feature.volcano.lava.simulator.LavaConnections.SortBucket;
import grondag.adversity.library.PackedBlockPos;
import grondag.adversity.library.SimpleConcurrentList.ListMode;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import grondag.adversity.simulator.base.NodeRoots;
import grondag.adversity.simulator.base.SimulationNode;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class LavaSimulator extends SimulationNode
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

    public final WorldStateBuffer worldBuffer;
    public final LavaTerrainHelper terrainHelper;
    
    /** Basalt blocks that are awaiting cooling */
    protected final Set<AgedBlockPos> basaltBlocks = ConcurrentHashMap.newKeySet();
    protected final static String BASALT_BLOCKS_NBT_TAG = "basaltblock"; 
    protected static final int BASALT_BLOCKS_NBT_WIDTH = 4;
    
    //TODO: remove when new model is implemented - no longer used/needed
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
    protected long connectionProcessTime;
    public AtomicInteger connectionProcessCount = new AtomicInteger(0);
    protected long coolingTime;
    protected long cellUpdateTime = 0;
    protected long particleTime;
    protected long validationTime;
    protected long blockUpdateProvisionTime;
    protected long blockUpdateApplicationTime;

    public final LavaCells cells = new LavaCells(this);
    private final LavaConnections connections = new LavaConnections();
    public final CellChunkLoader cellChunkLoader = new CellChunkLoader();
    
    private final BlockEventList lavaBlockPlacementEvents = new BlockEventList("lavaPlaceEvents", 10)
    {
        @Override
        protected boolean processEvent(int x, int y, int z, int amount)
        {
            if(amount < 0 && amount >= -LEVELS_PER_BLOCK)
            {
                // Lava destroyed
                // Should be able to find a loaded chunk and post a pending event to handle during validation
                // If the chunk is not loaded, is strange, but not going to load it just to tell it to delete lava
                LavaCell target = cells.getCellIfExists(x, y, z);
                if(target != null)
                {
                    target.changeLevel(getTickIndex(), amount * FLUID_UNITS_PER_LEVEL);
                    target.setRefreshRange(y, y);
                }
                return true;
            }
            else if(amount > 0 && amount <= LEVELS_PER_BLOCK)
            {
                LavaCell target = cells.getCellIfExists(x, y, z);
                if(target == null)
                {
                    target = cells.getEntryCell(x, z);
                    
                    if(target != null)
                    {
                        // if chunk has an entry cell for that column but not for the given space, mark it for validation
                        target.setValidationNeeded(true);
                    }
                    else
                    {
                        // mark entire chunk for validation
                        // Will already be so if we just created it, but handle strange
                        // case where chunk is already loaded but somehow no cells exist at x, z.
                        cells.getOrCreateCellChunk(x, z).requestFullValidation();
                    }
                    // event not complete until we can tell cell to add lava
                    return false;
                }
                else
                {
                    target.addLavaAtLevel(LavaSimulator.this.getTickIndex(), y * LEVELS_PER_BLOCK + 1, amount * FLUID_UNITS_PER_LEVEL);
                    target.setRefreshRange(y, y);
                    return true;
                }
            }
            
            // would have to be an unhandled event type
            if(Adversity.DEBUG_MODE)
                Adversity.log.warn("Detected unhandled block event type in event processing");
            
            return true;
        }
    };
    
    
    private final BlockEventList lavaAddEvents = new BlockEventList("lavaAddEvents", 10)
    {
        @Override
        protected boolean processEvent(int x, int y, int z, int amount)
        {
            LavaCell target = cells.getCellIfExists(x, y, z);
            
            if(target == null)
            {
                // retry - maybe validation needs to catch up
                return false;
            }
            else
            {
                target.addLavaAtLevel(LavaSimulator.this.getTickIndex(), y * LEVELS_PER_BLOCK + LEVELS_PER_HALF_BLOCK, amount);
                return true;
            }
        }
    };
    
    /** incremented each step, multiple times per tick */
    private int stepIndex;
    
    public LavaSimulator(World world)
    {
        super(NodeRoots.LAVA_SIMULATOR.ordinal());
        this.worldBuffer = new WorldStateBuffer(world);
        this.terrainHelper = new LavaTerrainHelper(worldBuffer);
    }
 
    
    /**
    * Signal to let volcano know should switch to cooling mode.
    * 1 or higher means overloaded.
    */
    public float loadFactor()
    {
        return Math.max((float)this.connections.size() / 100000F, (float)this.cells.size() / 50000F);
    }
    
       /** adds lava to the surface of the cell containing the given block position */
    public void addLava(long packedBlockPos, int amount, boolean shouldResynchToWorldBeforeAdding)
    {
        // make sure chunk will be loaded when we later process the event
        cells.getOrCreateCellChunk(PackedBlockPos.getX(packedBlockPos), PackedBlockPos.getZ(packedBlockPos));
        
        // queue event for processing during tick
        this.lavaAddEvents.addEvent(packedBlockPos, amount);
    }
    
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
 
     * Tags column of caller for validation.
     * Also tags four adjacent columns.
     */
    public void notifyLavaNeighborChange(World worldIn, BlockPos pos, IBlockState state)
    {
        if(itMe) return;
        
        int x = pos.getX();
        int z = pos.getZ();
        
        this.cells.markCellsForValidation(x, z);
        this.cells.markCellsForValidation(x + 1, z);
        this.cells.markCellsForValidation(x - 1, z);
        this.cells.markCellsForValidation(x, z + 1);
        this.cells.markCellsForValidation(x, z - 1);
    }
    
    /**
     * Update simulation from world when blocks are removed via creative mode or other methods.
     * Unfortunately, this will ALSO be called by our own block updates, 
     * so ignores call if visible level already matches.
     */
    public void unregisterDestroyedLava(World worldIn, BlockPos pos, IBlockState state)
    {
        if(itMe) return;

        // synchronize world buffer with world
        this.worldBuffer.clearBlockState(pos);
        
        if(state.getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK)
        {
            this.lavaFillers.remove(PackedBlockPos.pack(pos));
            this.setSaveDirty(true);
        }
        else if(state.getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK)
        {
            this.lavaBlockPlacementEvents.addEvent(pos, -IFlowBlock.getFlowHeightFromState(state));
            this.setSaveDirty(true);
        }
    }
    
    /**
     * Update simulation from world when blocks are placed via creative mode or other methods.
     * Unfortunately, this will ALSO be called by our own block updates, 
     * so ignores call if we are currently placing blocks.
     */
    public void registerPlacedLava(World worldIn, BlockPos pos, IBlockState state)
    {
        if(itMe) return;

        if(state.getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK)
        {
            this.lavaFillers.add(PackedBlockPos.pack(pos));
            this.setSaveDirty(true);
        }
        else if(state.getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK)
        {
            this.lavaBlockPlacementEvents.addEvent(pos, IFlowBlock.getFlowHeightFromState(state));
            
            // remove blocks placed by player so that simulation can place lava in the appropriate place
            this.itMe = true;
            this.worldBuffer.realWorld.setBlockState(pos, Blocks.AIR.getDefaultState());
            this.itMe = false;
            
            // synchronize world buffer with world
            this.worldBuffer.clearBlockState(pos);
            
            this.setSaveDirty(true);
        }
    }
    

    
//    /** true if the given position cannot contain lava */
//    protected abstract boolean isBlockLavaBarrier(long packedBlockPos);
//    
//    /** true if the given space is high enough above lava surface to be worth spawing a particle */
//    protected abstract boolean isHighEnoughForParticle(long packedBlockPos);
    
    /** use to maintain all the cells each tick after steps have executed */
    protected void updateCells()
    {
        // this part needs to be done during tick
        // -------------------------------
        
        // Apply pending lava block placements
        // These will either cause chunks to be loaded (and the lava thus discovered)
        // or if the chunk is loaded will try to update the loaded cell directly.
        this.lavaBlockPlacementEvents.processAllEvents();

        // For chunks that require a minority of cells to be validated, 
        // validate individual cells right now. 
        // For chunks that require full validation, buffer entire chunk state.
        // Actual load/validation for full chunks can be performed post=tick.
        this.cells.validateOrBufferChunks();
         
        // Add or update cells from world as needed
        // could be concurrent, but not yet implemented as such
        ColumnChunkBuffer buffer = this.cellChunkLoader.poll();
        while(buffer != null)
        {
            this.cells.loadOrValidateChunk(buffer);
            buffer = this.cellChunkLoader.poll();
        }
        
        // Apply world events that may depend on new chunks that were just loaded
        this.lavaAddEvents.processAllEvents();

        // update connections as needed, handle pressure propagation, or other housekeeping
        // this is also where connection sorting happens
        this.cells.setMode(ListMode.INDEX);
        LAVA_THREAD_POOL.submit( () ->
            this.cells.stream(true).forEach(c -> c.update(this, cells, connections)
        )).join();
        
        // connection sorting - prioritize all outbound connections - notify connections collection to update sort order if needed
        // Needs to happen in a separate pass from update because new connections are formed there
        LAVA_THREAD_POOL.submit( () ->
        this.cells.stream(true).forEach(c -> c.prioritizeOutboundConnections(connections))).join();
       
        this.cells.setMode(ListMode.ADD);
        
        //TODO: remove
//        this.connections.setMode(ListMode.INDEX);
//        Iterator<LavaConnection2> it = connections.stream(false).iterator();
//        while(it.hasNext())
//        {
//            LavaConnection2 c = it.next();
//            if(c.isActive() && c.getSortBucket() == null)
//            {
//                Adversity.log.info("derp");
//            }
//        }
//        this.connections.setMode(ListMode.ADD);
    }
    
    /** handle periodic (not every tick) cell validation */
    protected void doCellValidation()
    {
        
        // unload cell chunks that are no longer necessary
        this.cells.unloadInactiveCellChunks();
        
        // clear out cells no longer needed
        // NON-CONCURRENT
        this.cells.clearDeletedCells();
        this.cells.manageCapacity();
    }
    
    /** handle periodic (not every tick) connection validation */
    protected void doConnectionValidation()
    {
        this.connections.validateConnections();
        this.connections.manageCapacity();
    }
    
    public int getTickIndex()
    {
        return this.tickIndex;
    }
    
   
    protected void doParticles()
    {
        //reserved
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
        }

    }
    
    public void readFromNBT(NBTTagCompound nbt)
    {

        lavaFillers.clear();
        basaltBlocks.clear();
        
        this.tickIndex = nbt.getInteger(TICK_INDEX_NBT_TAG);
        
        this.worldBuffer.readFromNBT(nbt);
        
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
    
    public int getStepIndex()
    {
        return this.stepIndex;
    }

    public int getCellCount()
    {
        return this.cells.size();
    }

    public int getConnectionCount()
    {
        return this.connections.size();
    }

    public void saveLavaNBT(NBTTagCompound nbt)
    {
        this.cells.writeNBT(nbt);
        this.lavaBlockPlacementEvents.writeNBT(nbt);
        this.lavaAddEvents.writeNBT(nbt);
    }

    public void readLavaNBT(NBTTagCompound nbt)
    {
        cells.readNBT(this, nbt);
        
        this.connections.setMode(ListMode.MAINTAIN);
        this.connections.resize(cells.capacity() * 6);
        
        this.connections.setMode(ListMode.ADD);
        LAVA_THREAD_POOL.submit(() -> cells.stream(true).forEach(c -> c.updateConnectionsIfNeeded(cells, connections))).join();
        
        this.connections.setMode(ListMode.MAINTAIN);
        this.connections.manageCapacity();
        this.connections.setMode(ListMode.ADD);
        
        this.lavaBlockPlacementEvents.readNBT(nbt);
        this.lavaAddEvents.readNBT(nbt);
    }

   

 
    
    public void notifyBlockChange(World worldIn, BlockPos pos)
    {
        if(itMe) return;
        LavaCell entry = this.cells.getEntryCell(pos.getX(), pos.getZ());
        if(entry != null) entry.setValidationNeeded(true);      
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
                        + " for " + connectionProcessCount + " links @ " + ((connectionProcessCount.get() > 0) ? (float)connectionProcessTime / connectionProcessCount.get() : "n/a") + " each");
                connectionProcessCount.set(0);
                connectionProcessTime = 0;
                
                Adversity.log.info("Cell update time this sample = " + cellUpdateTime / 1000000);
                cellUpdateTime = 0;

                Adversity.log.info("totalCells=" + this.getCellCount() 
                        + " connections=" + this.getConnectionCount() + " basaltBlocks=" + this.basaltBlocks.size() + " loadFactor=" + this.loadFactor());
                
//                this.cells.logDebugInfo();
            }
         
//            this.connectionProcessCount += this.getConnectionCount();
            
            long startTime;

            // Must be within tick
            this.worldBuffer.isMCWorldAccessAppropriate = true;
            
            startTime = System.nanoTime();
            this.doParticles();
            this.particleTime += (System.nanoTime() - startTime);
            
            startTime = System.nanoTime();
            this.doBlockUpdateProvision();
            blockUpdateProvisionTime += (System.nanoTime() - startTime);
            
            startTime = System.nanoTime();
            this.doBlockUpdateApplication();
            blockUpdateApplicationTime += (System.nanoTime() - startTime);
            
            startTime = System.nanoTime();
            this.updateCells();
            cellUpdateTime  += (System.nanoTime() - startTime);

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

            // After this could be post-tick
            this.worldBuffer.isMCWorldAccessAppropriate = false;
            
            startTime = System.nanoTime();
            // force processing on non-dirty connection at least once per tick
            this.doFirstStep();
            this.doStep();
            this.doStep();
            this.doStep();
            this.doStep();
            this.doStep();
            this.doStep();
            this.doLastStep();
            this.connectionProcessTime += (System.nanoTime() - startTime);

            startTime = System.nanoTime();
            this.doCellValidation();
            this.doConnectionValidation();
            this.validationTime += (System.nanoTime() - startTime);
        
            this.setSaveDirty(true);
        }
    }

    protected void doFirstStep()
    {
        this.stepIndex++;
        for(SortBucket bucket : SortBucket.values())
        {
            LAVA_THREAD_POOL.submit(() ->
                this.connections.getSortStream(bucket, true).forEach(c -> c.doFirstStep(this))).join();
        }
    }

    protected void doStep()
    {
        this.stepIndex++;
        for(SortBucket bucket : SortBucket.values())
        {
            LAVA_THREAD_POOL.submit(() ->
                this.connections.getSortStream(bucket, true).forEach(c -> c.doStep(this))).join();
        }
    }

    protected void doLastStep()
    {
        this.stepIndex++;
        this.doStep();
    }

    protected void doBlockUpdateProvision()
    {
        this.cells.setMode(ListMode.INDEX);
        LAVA_THREAD_POOL.submit(() ->
            this.cells.stream(true).forEach(c -> c.provideBlockUpdateIfNeeded(this))).join();      
        this.cells.setMode(ListMode.ADD);
    }

    protected void doBlockUpdateApplication()
    {
        this.itMe = true;
        this.worldBuffer.applyBlockUpdates(1, this);
        this.itMe = false;
    }
    
    protected void doLavaCooling()
    {
        this.cells.setMode(ListMode.INDEX);
        LAVA_THREAD_POOL.submit( () ->
        this.cells.stream(true).forEach(c -> 
             {
                 if(c.canCool(this.tickIndex)) this.coolCell(c);
             })).join();
        this.cells.setMode(ListMode.ADD);
    }

    private void coolCell(LavaCell cell)
    {
        int x = cell.x();
        int z = cell.z();
        
        // check two above cell top to catch filler blocks
        for(int y = cell.bottomY(); y <= cell.fluidSurfaceY() + 2; y++)
        {
            this.coolLava(PackedBlockPos.pack(x, y, z));
        }
        cell.coolAndShrink();
    }
}