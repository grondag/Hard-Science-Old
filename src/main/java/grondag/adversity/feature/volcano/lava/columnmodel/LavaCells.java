package grondag.adversity.feature.volcano.lava.columnmodel;

import java.util.Iterator;
import java.util.stream.Stream;
import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.lava.AbstractLavaSimulator;
import grondag.adversity.library.PackedBlockPos;
import grondag.adversity.library.SimpleConcurrentList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;


public class LavaCells extends SimpleConcurrentList<LavaCell2>
{
    private final Long2ObjectOpenHashMap<CellChunk> cellChunks = new Long2ObjectOpenHashMap<CellChunk>();
    
    private final static int CAPACITY_INCREMENT = 0x10000;
    
    /** 
     * Reference to the simulation in which this cells collection lives.
     */
    public final LavaSimulatorNew sim;
    
    
    public LavaCells(LavaSimulatorNew sim)
    {
        super(CAPACITY_INCREMENT);
        this.sim = sim;
    }
    
    /**
     * Use operations on all cells.  
     * May include deleted cells.
     */
    @Override
    public Stream<LavaCell2> stream(boolean isParallel)
    {
        return super.stream(isParallel);
    }
    
    private static final int MAX_CHUNKS_PER_TICK = 4;
    
    public void validateOrBufferChunks()
    {
        int doneCount = 0;
        
        for(CellChunk chunk : this.cellChunks.values())
        {
            if(chunk.needsFullLoadOrValidation())
            {
                // completely new chunks don't count against the max loads per tick
                if(doneCount <= MAX_CHUNKS_PER_TICK || chunk.getEntryCount() != 0) 
                {
                    doneCount++;
                    this.sim.cellChunkLoader.queueChunks(this.sim.worldBuffer, chunk.packedChunkPos);
                }
            }
            else if(doneCount <= MAX_CHUNKS_PER_TICK)
            {
                if(chunk.validateMarkedCells()) doneCount++;
            }
        };        
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
            LavaCell2 cell = chunk.getEntryCell(x, z);
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
    public LavaCell2 getCellIfExists(int x, int y, int z)
    {
        CellChunk chunk = cellChunks.get(PackedBlockPos.getPackedChunkPos(x, z));
        if(chunk == null) return null;
        LavaCell2 entryCell = chunk.getEntryCell(x, z);
        return entryCell == null ? null : entryCell.getCellIfExists(y);
    }   
    
    /** 
     * Returns the starting cell for the stack of cells located at x, z.
     * Returns null if no cells exist at that location.
     * Also returns null if chunk has not been loaded.
     * Thread safe.
     */
    public LavaCell2 getEntryCell(int x, int z)
    {
        CellChunk chunk = cellChunks.get(PackedBlockPos.getPackedChunkPos(x, z));
        return chunk == null ? null : chunk.getEntryCell(x, z);
    }
    
    /**
     * Sets the entry cell for the stack of cells located at x, z.
     * Probably thread safe for most use cases.
     */
    private void setEntryCell(int x, int z, LavaCell2 entryCell)
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
    @Override
    public void add(LavaCell2 cell)
    {
        super.add(cell);
    }
    
    /** 
     * Removes deleted cells from the storage array. 
     * Does not remove them from cell stacks in locator.
     * Call after already cell has been unlinked from other 
     * cells in column and removed (and if necessary replaced) in locator.
     * NOT Thread-safe and not intended for concurrency.
     */
    public void clearDeletedCells()
    {
        synchronized(this)
        {
            this.setMode(ListMode.MAINTAIN);
            this.removeDeletedItems();
            this.setMode(ListMode.ADD);
        }
    }
    
    /**
     * Releases chunks that no longer need to remain loaded.
     */
    public void unloadInactiveCellChunks()
    {
        synchronized(this)
        {
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
    
    /** 
     * Ensures processing array has sufficient storage.  
     * Should be called periodically to prevent overflow 
     * and free unused memory.
     * Not intended for concurrency.
     */
    public void manageCapacity()
    {
        synchronized(this)
        {
            if(this.availableCapacity() < CAPACITY_INCREMENT / 2)
            {
                this.setMode(ListMode.MAINTAIN);
                this.resize(this.capacity() + CAPACITY_INCREMENT);
                this.setMode(ListMode.ADD);
            }
            else if(this.availableCapacity() >= CAPACITY_INCREMENT * 2)
            {
                this.setMode(ListMode.MAINTAIN);
                this.resize(this.capacity() - CAPACITY_INCREMENT);
                this.setMode(ListMode.ADD);
            }
        }
    }
    
    public void writeNBT(NBTTagCompound nbt)
    {
      
        int[] saveData = new int[this.size() * LavaCell2.LAVA_CELL_NBT_WIDTH];
        int i = 0;

        this.setMode(ListMode.INDEX);
        for(LavaCell2 cell : this)
        {
            if(cell.getFluidUnits() > 0 && !cell.isDeleted())
            {
                cell.writeNBT(saveData, i);
                
                // Java parameters are always pass by value, so have to advance index here
                i += LavaCell2.LAVA_CELL_NBT_WIDTH;
            }
        }
        
        Adversity.log.info("Saving " + i / LavaCell2.LAVA_CELL_NBT_WIDTH + " lava cells.");
        this.setMode(ListMode.ADD);
        
        nbt.setIntArray(LavaCell2.LAVA_CELL_NBT_TAG, saveData);
    }
    
    public void readNBT(LavaSimulatorNew sim, NBTTagCompound nbt)
    {
        this.cellChunks.clear();
        
        // LOAD LAVA CELLS
        int[] saveData = nbt.getIntArray(LavaCell2.LAVA_CELL_NBT_TAG);
        
        //confirm correct size
        if(saveData == null || saveData.length % LavaCell2.LAVA_CELL_NBT_WIDTH != 0)
        {
            Adversity.log.warn("Invalid save data loading lava simulator. Lava blocks may not be updated properly.");
        }
        else
        {
            int count = saveData.length / LavaCell2.LAVA_CELL_NBT_WIDTH;
            int newCapacity = (count / CAPACITY_INCREMENT + 1) * CAPACITY_INCREMENT;
            if(newCapacity < CAPACITY_INCREMENT / 2) newCapacity += CAPACITY_INCREMENT;
            
            this.setMode(ListMode.MAINTAIN);
            this.clear();
            this.resize(newCapacity);
            this.setMode(ListMode.ADD);
            
            int i = 0;
            
            while(i < saveData.length)
            {
                int x = saveData[i++];
                int z = saveData[i++];
                
                LavaCell2 newCell;
                
                LavaCell2 startingCell = this.getEntryCell(x, z);
                
                if(startingCell == null)
                {
                    newCell = new LavaCell2(this, x, z, 0, 0, 0, false);
                    newCell.readNBTArray(saveData, i);
                    this.setEntryCell(x, z, newCell);
                }
                else
                {
                    newCell = new LavaCell2(startingCell, 0, 0, 0, false);
                    newCell.readNBTArray(saveData, i);
                    startingCell.addCellToColumn(startingCell);
                }

                newCell.clearBlockUpdate();
                
                // Java parameters are always pass by value, so have to advance index here
                // subtract two because we incremented for x and z values already
                i += LavaCell2.LAVA_CELL_NBT_WIDTH - 2;
            }
         
            // Prevent massive retention update from occurring during first world tick
            
            // Raw retention should be mostly current, but compute for any cells
            // that were awaiting computation at last world save.
            this.stream(false).forEach(c -> c.updateRawRetentionIfNeeded());
            
            // Smoothed retention will need to be computed for all cells, but can be parallel.
            AbstractLavaSimulator.LAVA_THREAD_POOL.submit(() ->
                this.stream(true).forEach(c -> c.updatedSmoothedRetentionIfNeeded())).join();
            
            Adversity.log.info("Loaded " + this.size() + " lava cells.");
        }
    }
    
    public void logDebugInfo()
    {
        Adversity.log.info(this.cellChunks.size() + " loaded cell chunks");
        for(CellChunk chunk : this.cellChunks.values())
        {
            Adversity.log.info("xStart=" + PackedBlockPos.getChunkXStart(chunk.packedChunkPos)
                + " zStart=" + PackedBlockPos.getChunkZStart(chunk.packedChunkPos)
                + " activeCount=" + chunk.getActiveCount() + " entryCount=" + chunk.getEntryCount()
                    );
            
        }
    }
}
