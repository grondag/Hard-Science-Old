package grondag.adversity.feature.volcano.lava.simulator;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.Executor;

import grondag.adversity.Output;
import grondag.adversity.library.CountedJob;
import grondag.adversity.library.CountedJob.CountedJobTask;
import grondag.adversity.simulator.Simulator;
import grondag.adversity.library.Job;
import grondag.adversity.library.PackedBlockPos;
import grondag.adversity.library.PerformanceCounter;
import grondag.adversity.library.SimpleConcurrentList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;


public class LavaCells
{
    private final SimpleConcurrentList<LavaCell> cellList;
    
    private final Long2ObjectOpenHashMap<CellChunk> cellChunks = new Long2ObjectOpenHashMap<CellChunk>();
    
    private final static int CAPACITY_INCREMENT = 0x10000;
    
    /** 
     * Reference to the simulation in which this cells collection lives.
     */
    public final LavaSimulator sim;
    
    // TODO: consider combining on-tick/off-tick jobs once optimization is complete
  
    // on-tick tasks
    private final CountedJobTask<LavaCell> provideBlockUpdateTask =  new CountedJobTask<LavaCell>() 
    {
        @Override
        public void doJobTask(LavaCell operand)
        {
            operand.provideBlockUpdateIfNeeded(sim);
        }
    };
            
    private final CountedJobTask<LavaCell> updateRetentionTask = new CountedJobTask<LavaCell>()
    {

        @Override
        public void doJobTask(LavaCell operand)
        {
            operand.updateRawRetentionIfNeeded();
        }
    };
    
    private final CountedJobTask<LavaCell> updateSmoothedRetentionTask = new CountedJobTask<LavaCell>()
    {

        @Override
        public void doJobTask(LavaCell operand)
        {
            operand.updatedSmoothedRetentionIfNeeded();
        }
    };
    
    private final CountedJobTask<LavaCell> doCoolingTask = new CountedJobTask<LavaCell>()
    {
        @Override
        public void doJobTask(LavaCell operand)
        {
            if(operand.canCool(Simulator.INSTANCE.getTick()))
            {
                sim.coolCell(operand);
                if(operand.isDeleted())
                {
                    int x = operand.x();
                    int z = operand.z();
                    
                    getOrCreateCellChunk(x, z).setEntryCell(x, z, operand.selectStartingCell());
                }
            }
        }    
    };
            
    // off-tick tasksan
    private final CountedJobTask<LavaCell> updateStuffTask = new CountedJobTask<LavaCell>()
    {
        @Override
        public void doJobTask(LavaCell operand)
        {
            if(operand.isBoreCell())
            {
                operand.addLava(LavaSimulator.FLUID_UNITS_PER_TICK);
            }
            operand.updateActiveStatus();
            operand.updateConnectionsIfNeeded(sim);
        }
    };
    
    private final CountedJobTask<LavaCell> prioritizeConnectionsTask = new CountedJobTask<LavaCell>() 
    {

        @Override
        public void doJobTask(LavaCell operand)
        {
            operand.prioritizeOutboundConnections(sim.connections);
        }
    };
    
    private final CountedJobTask<CellChunk> doChunkValidationTask = new CountedJobTask<CellChunk>()
    {
        @Override
        public void doJobTask(CellChunk operand)
        {
            if(operand.needsFullLoadOrValidation())
            {
                sim.cellChunkLoader.queueChunks(sim.worldBuffer, operand.packedChunkPos);
            }
            else 
            {
                operand.validateMarkedCells();
            }
        }    
    };

    //TODO: make configurable
    private final static int BATCH_SIZE = 4096;
    
    public final Job provideBlockUpdateJob;   
    public final Job updateRetentionJob;   
    public final Job doCoolingJob;   
    public final Job validateChunksJob;
    
    public final Job updateSmoothedRetentionJob;  
    public final Job updateStuffJob;
    public final Job prioritizeConnectionsJob;
    
    
   private static final int MAX_CHUNKS_PER_TICK = 4;
    
   // performance counting for removal disabled because list is cleared each passed
   private SimpleConcurrentList<CellChunk> processChunks = SimpleConcurrentList.create(false, "", null);
   
   PerformanceCounter perfCounterValidationPrep;
   
    public LavaCells(LavaSimulator sim)
    {
        this.sim = sim;
        cellList = SimpleConcurrentList.create(LavaSimulator.ENABLE_PERFORMANCE_COUNTING, "Lava Cells", sim.perfCollectorOffTick);

        perfCounterValidationPrep = PerformanceCounter.create(LavaSimulator.ENABLE_PERFORMANCE_COUNTING, "Chunk validation prep", sim.perfCollectorOnTick);
        
        // on-tick jobs
        provideBlockUpdateJob = new CountedJob<LavaCell>(this.cellList, provideBlockUpdateTask, BATCH_SIZE, 
                LavaSimulator.ENABLE_PERFORMANCE_COUNTING, "Block Update Provision", sim.perfCollectorOnTick);    
        
        updateRetentionJob = new CountedJob<LavaCell>(this.cellList, updateRetentionTask, BATCH_SIZE, 
                LavaSimulator.ENABLE_PERFORMANCE_COUNTING, "Raw Retention Update", sim.perfCollectorOnTick);   
        
        doCoolingJob = new CountedJob<LavaCell>(this.cellList, doCoolingTask, BATCH_SIZE, 
                LavaSimulator.ENABLE_PERFORMANCE_COUNTING, "Lava Cell Cooling", sim.perfCollectorOnTick);  
        
       validateChunksJob = new CountedJob<CellChunk>(processChunks, doChunkValidationTask, 1, 
               LavaSimulator.ENABLE_PERFORMANCE_COUNTING, "Chunk Validation", sim.perfCollectorOnTick); 
        
        // off-tick jobs
        updateSmoothedRetentionJob = new CountedJob<LavaCell>(this.cellList, updateSmoothedRetentionTask, BATCH_SIZE, 
                LavaSimulator.ENABLE_PERFORMANCE_COUNTING, "Smoothed Retention Update", sim.perfCollectorOffTick);   
        
        updateStuffJob = new CountedJob<LavaCell>(this.cellList, updateStuffTask, BATCH_SIZE, 
                LavaSimulator.ENABLE_PERFORMANCE_COUNTING, "Cell Upkeep", sim.perfCollectorOffTick);
        
        prioritizeConnectionsJob = new CountedJob<LavaCell>(this.cellList, prioritizeConnectionsTask, BATCH_SIZE, 
                LavaSimulator.ENABLE_PERFORMANCE_COUNTING, "Connection Prioritization", sim.perfCollectorOffTick);
   }

   public void validateOrBufferChunks(Executor executor)
   {
        this.perfCounterValidationPrep.startRun();
        
        int size = this.cellChunks.size();
        
        if(size== 0) return;
        
        this.processChunks.clear();
        
        final Object[] candidates = this.cellChunks.values().toArray();
        
        Arrays.sort(candidates, new Comparator<Object>()
        {
            @Override
            public int compare(Object o1, Object o2)
            {
                return Integer.compare(((CellChunk)o2).validationPriority(), ((CellChunk)o1).validationPriority());
            }
        });
        
        for(Object chunk : candidates)
        {
            CellChunk c = (CellChunk)chunk;
            if(c.isNew() || (this.processChunks.size() < MAX_CHUNKS_PER_TICK && c.validationPriority() > 0))
            {
                this.processChunks.add(c);
            }
            else
            {
                break;
            }
        }
       
       this.perfCounterValidationPrep.endRun();
       
       if(this.processChunks.size() > 0)
       {
            validateChunksJob.runOn(executor);
       }
    }
    
 
    
    /**
     * Marks cells in x, z column to be validated vs. world state.
     * If chunk containing x, z is not loaded, causes chunk to be loaded.
     * Has no effect if chunk is already marked for full validation.
     * 
     * @param x
     * @param z
     */
    public void markCellsForValidation(int x, int z)
    {
        CellChunk chunk = this.getOrCreateCellChunk(x, z);
        if(!chunk.needsFullLoadOrValidation())
        {
            LavaCell cell = chunk.getEntryCell(x, z);
            if(cell == null)
            {
                // really strange to have world column completely full
                // so re-validate entire chunk
                chunk.requestFullValidation();
            }
            else
            {
                cell.setValidationNeeded(true);
            }
        }
    }
    
    /**
     * Creates cells for the given chunk if it is not already loaded.
     * If chunk is already loaded, validates against the chunk data provided.
     */
    public void loadOrValidateChunk(ColumnChunkBuffer chunkBuffer)
    {
        CellChunk cellChunk;
        
        synchronized(this)
        {
            // create new cell chunk if not already loaded
            cellChunk = this.cellChunks.get(chunkBuffer.getPackedChunkPos());
            if(cellChunk == null)
            {
                cellChunk = new CellChunk(chunkBuffer.getPackedChunkPos(), this);
                this.cellChunks.put(cellChunk.packedChunkPos, cellChunk);
            }
        }
    
        // remaining updates within the chunk do not need to be synchronized
        cellChunk.loadOrValidateChunk(chunkBuffer);
 
    }
    
    
//    /** checks for chunk being loaded using packed block coordinates */
//    public boolean isChunkLoaded(long packedBlockPos)
//    {
//        CellChunk cellChunk = this.cellChunks.get(PackedBlockPos.getPackedChunkPos(packedBlockPos));
//        return cellChunk == null ? false : cellChunk.isLoaded();
//    }
//    
//    /** checks for chunk being loaded using BlockPos coordinates*/
//    public boolean isChunkLoaded(BlockPos pos)
//    {
//        CellChunk cellChunk = this.cellChunks.get(PackedBlockPos.getPackedChunkPos(pos));
//        return cellChunk == null ? false : cellChunk.isLoaded();
//    }
//
//    /** checks for chunk being loaded using x, z block coordinates*/
//    public boolean isChunkLoaded(int x, int z)
//    {
//        CellChunk cellChunk = this.cellChunks.get(PackedBlockPos.getPackedChunkPos(x, z));
//        return cellChunk == null ? false : cellChunk.isLoaded();
//    }
    
    /**
     * Retrieves cell at the given block position.
     * Returns null if the given location does not contain a cell.
     * Also returns NULL if cell chunk has not yet been loaded.
     * Thread safe.
     */
    public LavaCell getCellIfExists(int x, int y, int z)
    {
        CellChunk chunk = cellChunks.get(PackedBlockPos.getPackedChunkPos(x, z));
        if(chunk == null) return null;
        LavaCell entryCell = chunk.getEntryCell(x, z);
        return entryCell == null ? null : entryCell.getCellIfExists(y);
    }   
    
    /** 
     * Returns the starting cell for the stack of cells located at x, z.
     * Returns null if no cells exist at that location.
     * Also returns null if chunk has not been loaded.
     * Thread safe.
     */
    public LavaCell getEntryCell(int x, int z)
    {
        CellChunk chunk = cellChunks.get(PackedBlockPos.getPackedChunkPos(x, z));
        return chunk == null ? null : chunk.getEntryCell(x, z);
    }
    
    /**
     * Sets the entry cell for the stack of cells located at x, z.
     * Probably thread safe for most use cases.
     */
    private void setEntryCell(int x, int z, LavaCell entryCell)
    {
        this.getOrCreateCellChunk(x, z).setEntryCell(x, z, entryCell);
    }
    
    /**
     * Does what is says.
     * Thread-safe.
     * x and z are BLOCK coordinates, not chunk coordinates
     */
    public CellChunk getOrCreateCellChunk(int xBlock, int zBlock)
    {
        CellChunk chunk = cellChunks.get(PackedBlockPos.getPackedChunkPos(xBlock, zBlock));
        if(chunk == null)
        {
            synchronized(this)
            {
                //confirm not added by another thread
                chunk = cellChunks.get(PackedBlockPos.getPackedChunkPos(xBlock, zBlock));
                if(chunk == null)
                {
                    chunk = new CellChunk(PackedBlockPos.getPackedChunkPos(xBlock, zBlock), this);
                    this.cellChunks.put(chunk.packedChunkPos, chunk);
                }
            }
        }
        return chunk;
    }
    
    /** 
     * Adds cell to the storage array. 
     * Does not add to locator list.
     * Thread-safe if mode = ListMode.ADD. Disallowed otherwise.
     */
    public void add(LavaCell cell)
    {
        this.cellList.add(cell);
    }
    
    public int size()
    {
        return this.cellList.size();
    }
    
    /** 
     * Removes deleted cells from the storage array. 
     * Does not remove them from cell stacks in locator.
     * Call after already cell has been unlinked from other 
     * cells in column and removed (and if necessary replaced) in locator.
     * NOT Thread-safe and not intended for concurrency.
     */
    public void removeDeletedItems()
    {
        this.cellList.removeDeletedItems();
    }
    
    /**
     * Releases chunks that no longer need to remain loaded.
     */
    public void unloadInactiveCellChunks()
    {
        synchronized(this)
        {
//            Adversity.log.info("CHUNK UNLOAD REPORT");
            Iterator<Entry<CellChunk>> chunks = this.cellChunks.long2ObjectEntrySet().fastIterator();
            
            while(chunks.hasNext())
            {
                Entry<CellChunk> entry = chunks.next();
                if(entry.getValue().canUnload())
                {
                    entry.getValue().unload();
                    chunks.remove();
                }
            }
        }
    }
    
 
    
    public void writeNBT(NBTTagCompound nbt)
    {
      
        int[] saveData = new int[this.cellList.size() * LavaCell.LAVA_CELL_NBT_WIDTH];
        int i = 0;

        for(LavaCell cell : this.cellList)
        {
            if(!cell.isDeleted())
            {
                cell.writeNBT(saveData, i);
                
                // Java parameters are always pass by value, so have to advance index here
                i += LavaCell.LAVA_CELL_NBT_WIDTH;
            }
        }
        
        Output.getLog().info("Saving " + i / LavaCell.LAVA_CELL_NBT_WIDTH + " lava cells.");
        
        nbt.setIntArray(LavaCell.LAVA_CELL_NBT_TAG, Arrays.copyOfRange(saveData, 0, i));
    }
    
    public void readNBT(LavaSimulator sim, NBTTagCompound nbt)
    {
        this.cellChunks.clear();
        
        // LOAD LAVA CELLS
        int[] saveData = nbt.getIntArray(LavaCell.LAVA_CELL_NBT_TAG);
        
        //confirm correct size
        if(saveData == null || saveData.length % LavaCell.LAVA_CELL_NBT_WIDTH != 0)
        {
            Output.getLog().warn("Invalid save data loading lava simulator. Lava blocks may not be updated properly.");
        }
        else
        {
            int count = saveData.length / LavaCell.LAVA_CELL_NBT_WIDTH;
            int newCapacity = (count / CAPACITY_INCREMENT + 1) * CAPACITY_INCREMENT;
            if(newCapacity < CAPACITY_INCREMENT / 2) newCapacity += CAPACITY_INCREMENT;
            
            this.cellList.clear();
            
            int i = 0;
            
            while(i < saveData.length)
            {
                int x = saveData[i++];
                int z = saveData[i++];
                
                LavaCell newCell;
                
                LavaCell startingCell = this.getEntryCell(x, z);
                
                if(startingCell == null)
                {
                    newCell = new LavaCell(this, x, z, 0, 0, false);
                    newCell.readNBTArray(saveData, i);
                    this.setEntryCell(x, z, newCell);
                }
                else
                {
                    newCell = new LavaCell(startingCell, 0, 0, false);
                    newCell.readNBTArray(saveData, i);
                    startingCell.addCellToColumn(newCell);
                }

                newCell.clearBlockUpdate();
                
                // Java parameters are always pass by value, so have to advance index here
                // subtract two because we incremented for x and z values already
                i += LavaCell.LAVA_CELL_NBT_WIDTH - 2;
            }
         
            // Prevent massive retention update from occurring during first world tick
            
            // Raw retention should be mostly current, but compute for any cells
            // that were awaiting computation at last world save.
            this.sim.worldBuffer.isMCWorldAccessAppropriate = true;
            this.updateRetentionJob.runOn(this.sim.LAVA_THREAD_POOL);
            this.sim.worldBuffer.isMCWorldAccessAppropriate = false;
            
            // Smoothed retention will need to be computed for all cells, but can be parallel.
            this.updateSmoothedRetentionJob.runOn(this.sim.LAVA_THREAD_POOL);
            
            // Make sure other stuff is up to date
            this.updateStuffJob.runOn(this.sim.LAVA_THREAD_POOL);
            
            Output.getLog().info("Loaded " + this.cellList.size() + " lava cells.");
        }
    }
    
    public void logDebugInfo()
    {
        Output.getLog().info(this.cellChunks.size() + " loaded cell chunks");
        for(CellChunk chunk : this.cellChunks.values())
        {
            Output.getLog().info("xStart=" + PackedBlockPos.getChunkXStart(chunk.packedChunkPos)
                + " zStart=" + PackedBlockPos.getChunkZStart(chunk.packedChunkPos)
                + " activeCount=" + chunk.getActiveCount() + " entryCount=" + chunk.getEntryCount()
                    );
            
        }
    }
}
