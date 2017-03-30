package grondag.adversity.feature.volcano.lava.simulator;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.CoolingBlock;
import grondag.adversity.feature.volcano.lava.AgedBlockPos;
import grondag.adversity.feature.volcano.lava.LavaTerrainHelper;
import grondag.adversity.feature.volcano.lava.WorldStateBuffer;
import grondag.adversity.feature.volcano.lava.simulator.BlockEventList.BlockEvent;
import grondag.adversity.feature.volcano.lava.simulator.BlockEventList.BlockEventHandler;
import grondag.adversity.feature.volcano.lava.simulator.LavaConnections.SortBucket;
import grondag.adversity.library.PackedBlockPos;
import grondag.adversity.library.SimplePerformanceCounter;
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
    private final Set<AgedBlockPos> basaltBlocks = ConcurrentHashMap.newKeySet();
    private final SimplePerformanceCounter basaltPerf = new SimplePerformanceCounter();
    private final static String BASALT_BLOCKS_NBT_TAG = "basaltblock"; 
    private static final int BASALT_BLOCKS_NBT_WIDTH = 4;
    
    /** used to schedule intermittent cooling jobs */
    private int nextCoolTick = 0;
    /** use to control which period cooling job runs next */
    private boolean nextCoolTickIsLava = true;

    private int nextStatTick = 0;
            
    /** Set true when doing block placements so we known not to register them as newly placed lava. */
    protected boolean itMe = false;

    protected int tickIndex = 0;
    protected final static String TICK_INDEX_NBT_TAG = "tickindex"; 
    
    public final LavaCells cells = new LavaCells(this);
    public final LavaConnections connections = new LavaConnections(this);
    public final CellChunkLoader cellChunkLoader = new CellChunkLoader();
    
    private final BlockEventHandler placementHandler = new BlockEventHandler()
    {
        @Override
        public boolean handleEvent(BlockEvent event)
        {
            if(event.amount < 0 && event.amount >= -LEVELS_PER_BLOCK)
            {
                // Lava destroyed
                // Should be able to find a loaded chunk and post a pending event to handle during validation
                // If the chunk is not loaded, is strange, but not going to load it just to tell it to delete lava
                LavaCell target = cells.getCellIfExists(event.x, event.y, event.z);
                if(target != null)
                {
                    target.changeLevel(event.amount * FLUID_UNITS_PER_LEVEL);
                    target.updateTickIndex(getTickIndex());
                    target.setRefreshRange(event.y, event.y);
                }
                return true;
            }
            else if(event.amount > 0 && event.amount <= LEVELS_PER_BLOCK)
            {
                LavaCell target = cells.getCellIfExists(event.x, event.y, event.z);
                if(target == null)
                {
                    target = cells.getEntryCell(event.x, event.z);
                    
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
                        cells.getOrCreateCellChunk(event.x, event.z).requestFullValidation();
                    }
                    // event not complete until we can tell cell to add lava
                    return false;
                }
                else
                {
                    target.addLavaAtLevel(LavaSimulator.this.getTickIndex(), event.y * LEVELS_PER_BLOCK + 1, event.amount * FLUID_UNITS_PER_LEVEL);
                    target.setRefreshRange(event.y, event.y);
                    return true;
                }
            }
            
            // would have to be an unhandled event type
            if(Adversity.DEBUG_MODE)
                Adversity.log.warn("Detected unhandled block event type in event processing");
            
            return true;
        }
    };
    
    private final BlockEventList lavaBlockPlacementEvents = new BlockEventList(10, "lavaPlacementEvents", placementHandler);
    
    private final BlockEventHandler lavaAddEventHandler = new BlockEventHandler()
    {
        @Override
        public boolean handleEvent(BlockEvent event)
        {
            LavaCell target = cells.getCellIfExists(event.x, event.y, event.z);
            
            if(target == null)
            {
                // retry - maybe validation needs to catch up
                return false;
            }
            else
            {
                target.addLavaAtLevel(LavaSimulator.this.getTickIndex(), event.y * LEVELS_PER_BLOCK + LEVELS_PER_HALF_BLOCK, event.amount);
                return true;
            }
        }
    };
    
    private final BlockEventList lavaAddEvents = new BlockEventList(10, "lavaAddEvents", lavaAddEventHandler);
    
            
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
        
        // ignore fillers
        if(state.getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK)
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
        
        
        // ignore fillers - they have no effect on simulation
        if(state.getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK)
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
    
    public int getTickIndex()
    {
        return this.tickIndex;
    }
    
      
    protected void doBasaltCooling()
    {
        if(this.basaltBlocks.isEmpty()) return;
        
        this.basaltPerf.startRun();
        this.basaltPerf.addCount(this.basaltBlocks.size());
        
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
        
        this.basaltPerf.endRun();
    }

    
    /** used by world update to notify when fillers are placed */
    public void trackCoolingBlock(BlockPos pos)
    {
        this.basaltBlocks.add(new AgedBlockPos(pos, this.tickIndex));
        this.setSaveDirty(true);
    }
    
    /**
     * Update simulation from world when blocks are placed via creative mode or other methods.
     * Also called by random tick on cooling blocks so that they can't get permanently orphaned
     */
    public void registerCoolingBlock(World worldIn, BlockPos pos)
    {
        if(!itMe) trackCoolingBlock(pos);
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

        basaltBlocks.clear();
        
        this.tickIndex = nbt.getInteger(TICK_INDEX_NBT_TAG);
        
        this.worldBuffer.readFromNBT(nbt);
        
        this.readLavaNBT(nbt);
        
        
        // LOAD BASALT BLOCKS
        int[] saveData = nbt.getIntArray(BASALT_BLOCKS_NBT_TAG);

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
        LAVA_THREAD_POOL.submit(() -> cells.stream(true).forEach(c -> c.updateConnectionsIfNeeded(this))).join();
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
        long startTime = System.nanoTime();
        
        if(this.tickIndex < newLastTickIndex)
        {
            this.tickIndex++;
         
            this.doOnTick();
            
            this.doOffTick();
            
            this.doStats();

            this.setSaveDirty(true);
        }
        
        long duration = System.nanoTime() - startTime;
        if(duration > 100000000)
            Adversity.log.info("tick duration =" + duration);
    }
    
    private SimplePerformanceCounter perfOnTick = new SimplePerformanceCounter();
    
    private SimplePerformanceCounter perfFocus = new SimplePerformanceCounter();
    
    /**
     * Tasks that should occur during the server tick.
     * All tasks the require direct MC world access go here.
     * Any mutating world access should be single threaded.
     */
    public void doOnTick()
    {
        perfOnTick.startRun();
        
        // Enable detection of improper world access 
        this.worldBuffer.isMCWorldAccessAppropriate = true;
        
        //TODO: particle processing goes here
        
        // This job can access world objects concurrently, however all access is 
        // read only and is synchronized by the worldBuffer.
        this.cells.provideBlockUpdateJob.runOn(LAVA_THREAD_POOL);
        
        
        this.itMe = true;
        this.worldBuffer.applyBlockUpdates(1, this);
        this.itMe = false;
        
            // For chunks that require a minority of cells to be validated, 
        // validate individual cells right now. 
        // For chunks that require full validation, buffer entire chunk state.
        // Actual load/validation for full chunks can be performed post=tick.
        this.cells.validateOrBufferChunks(LAVA_THREAD_POOL);
         
        this.cells.updateRetentionJob.runOn(LAVA_THREAD_POOL);
   
        
        // do these on alternate ticks to help avoid ticks that are too long
        if(this.tickIndex >= this.nextCoolTick)
        {
            this.nextCoolTick = this.tickIndex + 10;
            if(this.nextCoolTickIsLava)
            {
                this.nextCoolTickIsLava = false;
                this.cells.doCoolingJob.runOn(LAVA_THREAD_POOL);
            }
            else
            {
                this.nextCoolTickIsLava = true;
                this.doBasaltCooling();
            }
        }
        // After this could be post-tick
        this.worldBuffer.isMCWorldAccessAppropriate = false;
        
        perfOnTick.endRun();
        perfOnTick.addCount(1);
    }
    
    private SimplePerformanceCounter perfOffTick = new SimplePerformanceCounter();
    
//    private int[] flowTotals = new int[8];
    
    public void doOffTick()
    {
        perfOffTick.startRun();
        
        // Add or update cells from world as needed
        // could be concurrent, but not yet implemented as such
        ColumnChunkBuffer buffer = this.cellChunkLoader.poll();
        while(buffer != null)
        {
            this.cells.loadOrValidateChunk(buffer);
            this.cellChunkLoader.returnUsedBuffer(buffer);
            buffer = this.cellChunkLoader.poll();
        }
     
        // Apply world events that may depend on new chunks that were just loaded
        this.lavaAddEvents.processAllEventsOn(LAVA_THREAD_POOL);

        // update connections as needed, handle pressure propagation, or other housekeeping
        this.cells.updateStuffJob.runOn(LAVA_THREAD_POOL);
       
        // unload cell chunks that are no longer necessary
        // important that this run right after cell update so that
        // chunk active/inactive accounting is accurate and we don't have improper unloading
        this.cells.unloadInactiveCellChunks();
        
        // clear out cells no longer needed
        // validates that chunk cell is in has been unloaded, so should happen after chunk unload
        this.cells.removeDeletedItems();
        
        this.connections.removeDeletedItems();

        // connection sorting must happen AFTER all connections are updated/formed
        this.cells.prioritizeConnectionsJob.runOn(LAVA_THREAD_POOL);

        this.connections.setupTickJob.runOn(LAVA_THREAD_POOL);
        
        perfFocus.startRun();
        this.connections.refreshSortBucketsIfNeeded(LAVA_THREAD_POOL);
        perfFocus.endRun();
        perfFocus.addCount(1);
        
        this.doFirstStep();
        
        this.doStep();
        this.doStep();
        this.doStep();
        this.doStep();
        this.doStep();
        this.doStep();
        this.doLastStep();
        
        // Apply pending lava block placements
        // These will either cause chunks to be loaded (and the lava thus discovered)
        // or if the chunk is loaded will try to update the loaded cell directly.
        //
        // Doing this off-tick after all chunks are loaded means we may wait an 
        // extra tick to fully handle block placement events.
        // However, lava blocks are not normally expected to be placed or broken except by the simulation
        // which does not rely on world events for that purpose.
        this.lavaBlockPlacementEvents.processAllEventsOn(LAVA_THREAD_POOL);
        
        perfOffTick.endRun();
        perfOffTick.addCount(1);
    }
    
    public void doStats()
    {
        if((this.tickIndex >= this.nextStatTick))
        {
            this.nextStatTick = this.tickIndex + 200;

            Adversity.log.info("WorldBuffer state sets this sample = " + this.worldBuffer.getStateSetCount());
            Adversity.log.info(this.worldBuffer.stats());
            this.worldBuffer.clearStatistics();
            
//            Adversity.log.info("Cell chunk validation " + cells.validateChunksJob.perfCounter.stats());
//            cells.validateChunksJob.perfCounter.clearStats();
//            
//            Adversity.log.info("Cell block update provision " + cells.provideBlockUpdateJob.perfCounter.stats());
//            cells.provideBlockUpdateJob.perfCounter.clearStats();
//            
//            Adversity.log.info("Cell retention update " + cells.updateRetentionJob.perfCounter.stats());
//            cells.updateRetentionJob.perfCounter.clearStats();
//            
//            Adversity.log.info("Cell lava cooling " + cells.doCoolingJob.perfCounter.stats());
//            cells.doCoolingJob.perfCounter.clearStats();
//            
//            Adversity.log.info("Basalt cooling " + this.basaltPerf.stats());
//            basaltPerf.clearStats();
//            
//            Adversity.log.info("Cell connection update " + cells.updateStuffJob.perfCounter.stats());
//            cells.updateStuffJob.perfCounter.clearStats();
//            
//            Adversity.log.info("Connection prioritization " + cells.prioritizeConnectionsJob.perfCounter.stats());
//            cells.prioritizeConnectionsJob.perfCounter.clearStats();
//            
//            Adversity.log.info("Connection sort refresh " + connections.sortRefreshPerf.stats());
//            connections.sortRefreshPerf.clearStats();
//            
//            Adversity.log.info("Connection first step " + connections.firstStepJob[0].perfCounter.stats());
//            connections.firstStepJob[0].perfCounter.clearStats();
//            
//            Adversity.log.info("Connection normal step " + connections.stepJob[0].perfCounter.stats());
//            connections.stepJob[0].perfCounter.clearStats();
            
//            Adversity.log.info("Connection getFlowRate " + LavaConnection.perfFlowRate.stats());
//            LavaConnection.perfFlowRate.clearStats();
//       
//            Adversity.log.info("Connection flowAcross " + LavaConnection.perfFlowAcross.stats());
//            LavaConnection.perfFlowAcross.clearStats();

//            Adversity.log.info("Connection focus " + LavaConnection.perfFocus.stats());
//            LavaConnection.perfFocus.clearStats();

//          for(int i = 0; i < 8; i++)
//          {
//              Adversity.log.info(String.format("Flow total for step %1$d = %2$d ", i, this.flowTotals[i]));
//              this.flowTotals[i] = 0;
//          }
          
          Adversity.log.info("On-Tick time " + this.perfOnTick.stats());
          this.perfOnTick.clearStats();
            
          Adversity.log.info("Off-Tick time " + this.perfOffTick.stats());
          this.perfOffTick.clearStats();
            
          Adversity.log.info("Focus time " + this.perfFocus.stats());
          this.perfFocus.clearStats();
          
//            Adversity.log.info("Connection try success rate = " + LavaConnection.successCount.get() * 100 / (LavaConnection.tryCount.get() + 1) + "%");
//            LavaConnection.successCount.set(0);
//            LavaConnection.tryCount.set(0);
            
//            Adversity.log.info("Connection locking overhead = " + (LavaConnection.outerTime.get() - LavaConnection.innerTime.get()) * 100 / LavaConnection.outerTime.get() + "%");
            
            Adversity.log.info("totalCells=" + this.getCellCount() 
                    + " connections=" + this.getConnectionCount() + " basaltBlocks=" + this.basaltBlocks.size() + " loadFactor=" + this.loadFactor());
            
//                this.cells.logDebugInfo();
        }
    }

    protected void doFirstStep()
    {
        this.stepIndex = 0;
        

        for(SortBucket bucket : SortBucket.values())
        {
            this.connections.firstStepJob[bucket.ordinal()].runOn(LAVA_THREAD_POOL);
        }
        
//        this.flowTotals[0] += LavaConnection.totalFlow.get();
//        LavaConnection.totalFlow.set(0);
        
    }

    protected void doStep()
    {
        this.stepIndex++;
        for(SortBucket bucket : SortBucket.values())
        {
            this.connections.stepJob[bucket.ordinal()].runOn(LAVA_THREAD_POOL);
        }
//        this.flowTotals[stepIndex] += LavaConnection.totalFlow.get();
//        LavaConnection.totalFlow.set(0);
    }

    protected void doLastStep()
    {
        this.doStep();
    }

    public void coolCell(LavaCell cell)
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