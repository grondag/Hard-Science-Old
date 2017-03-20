package grondag.adversity.feature.volcano.lava.columnmodel;

import java.util.Iterator;

import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.lava.AbstractLavaSimulator;
import grondag.adversity.feature.volcano.lava.EntityLavaParticle;
import grondag.adversity.feature.volcano.lava.columnmodel.LavaConnections.SortBucket;
import grondag.adversity.library.PackedBlockPos;
import grondag.adversity.library.SimpleConcurrentList.ListMode;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.base.IFlowBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LavaSimulatorNew extends AbstractLavaSimulator
{
    private final LavaCells cells = new LavaCells(this);
    private final LavaConnections connections = new LavaConnections();
    public final CellChunkLoader cellChunkLoader = new CellChunkLoader();
    
    private final BlockEventList lavaBlockPlacementEvents = new BlockEventList("lavaPlaceEvents", 10)
    {
        @Override
        protected boolean processEvent(int x, int y, int z, int amount)
        {
            if(amount == 0)
            {
                // Lava destroyed
                // Should be able to find a loaded chunk and post a pending event to handle during validation
                // If the chunk is not loaded, is strange, but not going to load it just to tell it to delete lava
                LavaCell2 target = cells.getCellIfExists(x, y, z);
                if(target != null)
                {
                    target.notifyDestroyedLava(y);
                    return true;
                }
            }
            else if(amount > 0 && amount <= AbstractLavaSimulator.LEVELS_PER_BLOCK)
            {
                LavaCell2 target = cells.getCellIfExists(x, y, z);
                if(target == null)
                {
                    target = cells.getEntryCell(x, z);
                    
                    if(target != null)
                    {
                        // if chunk has an entry cell for that column, mark it for validation
                        target.setValidationNeeded(true);
                    }
                    else
                    {
                        // mark entire chunk for validation
                        // Will already be so if we just created it, but handle strange
                        // case where chunk is already loaded but somehow no cells exist at x, z.
                        cells.getOrCreateCellChunk(x, z).requestFullValidation();
                    }
                    // event not complete until we can tell cell to 
                    return false;
                }
                else
                {
                    target.notifyPlacedLava(LavaSimulatorNew.this.getTickIndex(), y, amount);
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
            LavaCell2 target = cells.getCellIfExists(x, y, z);
            
            if(target == null)
            {
                // retry - maybe validation needs to catch up
                return false;
            }
            else
            {
                target.addLavaAtLevel(LavaSimulatorNew.this.getTickIndex(), y * LEVELS_PER_BLOCK + LEVELS_PER_HALF_BLOCK, amount);
                return true;
            }
        }
    };
    
    /** incremented each step, multiple times per tick */
    private int stepIndex;
    
    public LavaSimulatorNew(World world)
    {
        super(world);
    }
    
    public int getStepIndex()
    {
        return this.stepIndex;
    }

    @Override
    public float loadFactor()
    {
        return Math.max((float)this.connections.size() / 10000F, (float)this.cells.size() / 5000F);
    }

    @Override
    public int getCellCount()
    {
        return this.cells.size();
    }

    @Override
    public int getConnectionCount()
    {
        return this.connections.size();
    }

    @Override
    public void saveLavaNBT(NBTTagCompound nbt)
    {
        this.cells.writeNBT(nbt);
        this.lavaBlockPlacementEvents.writeNBT(nbt);
        this.lavaAddEvents.writeNBT(nbt);
    }

    @Override
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

    @Override
    public void addLava(long packedBlockPos, int amount, boolean shouldResynchToWorldBeforeAdding)
    {
        // make sure chunk will be loaded when we later process the event
        cells.getOrCreateCellChunk(PackedBlockPos.getX(packedBlockPos), PackedBlockPos.getZ(packedBlockPos));
        
        // queue event for processing during tick
        this.lavaAddEvents.addEvent(packedBlockPos, amount);
    }

    /**
     * Tags column of caller for validation.
     * Also tags four adjacent columns.
     */
    @Override
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
    
    public void notifyBlockChange(World worldIn, BlockPos pos)
    {
        if(itMe) return;
        LavaCell2 entry = this.cells.getEntryCell(pos.getX(), pos.getZ());
        if(entry != null) entry.setValidationNeeded(true);      
    }

    @Override
    public void unregisterDestroyedLava(World worldIn, BlockPos pos, IBlockState state)
    {
        if(itMe) return;

        if(state.getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK)
        {
            this.lavaFillers.remove(PackedBlockPos.pack(pos));
            this.setSaveDirty(true);
        }
        else if(state.getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK)
        {
            this.lavaBlockPlacementEvents.addEvent(pos, 0);
            this.setSaveDirty(true);
        }
    }

    @Override
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
            
            // always remove blocks placed by player from world - otherwise lava will be re-added to sim
            // by validation until simulation state catches up with world
            this.worldBuffer.setBlockState(pos.getX(), pos.getY(), pos.getZ(), Blocks.AIR.getDefaultState(), state);
            this.setSaveDirty(true);
        }
    }
    
    /**
     * Updates fluid simulation for one game tick, provided the game clock has advanced at least one tick since last call.
     * Tick index is used internally to track which cells have changed and to control frequency of upkeep tasks.
     * Due to computationally intensive nature, does not do more work if game clock has advanced more than one tick.
     * To make lava flow more quickly, place more lava when clock advances.
     */
    @Override
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
                
                this.cells.logDebugInfo();
            }
         
            this.connectionProcessCount += this.getConnectionCount();
            
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
//            this.doFirstStep();
//            this.doStep();
//            this.doStep();
//            this.doStep();
//            this.doStep();
//            this.doStep();
//            this.doStep();
//            this.doLastStep();
            this.connectionProcessTime += (System.nanoTime() - startTime);

            startTime = System.nanoTime();
            this.doCellValidation();
            this.doConnectionValidation();
            this.validationTime += (System.nanoTime() - startTime);
        
            this.setSaveDirty(true);
        }
    }

    @Override
    protected void doFirstStep()
    {
        this.stepIndex++;
        for(SortBucket bucket : SortBucket.values())
        {
            LAVA_THREAD_POOL.submit(() ->
                this.connections.getSortStream(bucket, true).forEach(c -> c.doFirstStep(this))).join();
        }
    }

    @Override
    protected void doStep()
    {
        this.stepIndex++;
        for(SortBucket bucket : SortBucket.values())
        {
            LAVA_THREAD_POOL.submit(() ->
                this.connections.getSortStream(bucket, true).forEach(c -> c.doStep(this))).join();
        }
    }

    @Override
    protected void doLastStep()
    {
        this.stepIndex++;
        this.doStep();
    }

    @Override
    protected void doBlockUpdateProvision()
    {
        this.cells.setMode(ListMode.INDEX);
        LAVA_THREAD_POOL.submit(() ->
            this.cells.stream(true).forEach(c -> c.provideBlockUpdateIfNeeded(this))).join();      
        this.cells.setMode(ListMode.ADD);
    }

    @Override
    protected void doBlockUpdateApplication()
    {
        this.itMe = true;
        this.worldBuffer.applyBlockUpdates(1, this);
        this.itMe = false;
    }
    
    @Override
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

    private void coolCell(LavaCell2 cell)
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
    
    @Override
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
        
        // connection sorting - prioritize all outbound connections
        // Needs to happen in a separate pass from update because new connections are formed there
        LAVA_THREAD_POOL.submit( () ->
        this.cells.stream(true).forEach(c -> c.prioritizeOutboundConnections())).join();
       
        this.cells.setMode(ListMode.ADD);

        
        //TODO: remove
        this.connections.setMode(ListMode.INDEX);
        Iterator<LavaConnection2> it = connections.stream(false).iterator();
        while(it.hasNext())
        {
            LavaConnection2 c = it.next();
            if(c.isActive() && c.getSortBucket() == null)
            {
                Adversity.log.info("derp");
            }
        }
        this.connections.setMode(ListMode.ADD);
    }

    @Override
    protected void doCellValidation()
    {
        
        // unload cell chunks that are no longer necessary
        this.cells.unloadInactiveCellChunks();
        
        // clear out cells no longer needed
        // NON-CONCURRENT
        this.cells.clearDeletedCells();
        this.cells.manageCapacity();
    }

    @Override
    protected void doConnectionValidation()
    {
        this.connections.validateConnections();
        this.connections.manageCapacity();
    }
 }
