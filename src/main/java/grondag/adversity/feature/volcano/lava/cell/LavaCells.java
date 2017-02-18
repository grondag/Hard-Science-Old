package grondag.adversity.feature.volcano.lava.cell;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.lava.LavaSimulatorNew;
import grondag.adversity.library.PackedBlockPos;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;

public class LavaCells
{
    private final Long2ObjectOpenHashMap<CellChunk> cellChunks = new Long2ObjectOpenHashMap<CellChunk>();
    
    /** contains all extant cells - used to process all cells with simple iteration */
    private LavaCell2 cells[] = new LavaCell2[0x10000];
    
    private AtomicInteger size = new AtomicInteger(0);
    
    /**
     * Retrieves cell at the given block position.
     * Returns null if the given location does not contain a cell.
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
     * Thread safe.
     */
    LavaCell2 getEntryCell(int x, int z)
    {
        CellChunk chunk = cellChunks.get(PackedBlockPos.getPackedChunkPos(x, z));
        return chunk == null ? null : chunk.getEntryCell(x, z);
    }
    
    /**
     * Sets the entry cell for the stack of cells located at x, z.
     * Probably thread safe for most use cases.
     */
    void setEntryCell(int x, int z, LavaCell2 entryCell)
    {
        CellChunk chunk = cellChunks.get(PackedBlockPos.getPackedChunkPos(x, z));
        if(chunk == null)
        {
            synchronized(this)
            {
                //confirm not added by another thread
                chunk = cellChunks.get(PackedBlockPos.getPackedChunkPos(x, z));
                if(chunk == null)
                {
                    chunk = new CellChunk(PackedBlockPos.getPackedChunkPos(x, z));
                    this.cellChunks.put(chunk.packedChunkpos, chunk);
                }
            }
        }
        chunk.setEntryCell(x, z, entryCell);
    }
    
    /** 
     * Adds cell to the storage array. 
     * Does not add to locator list.
     * Thread-safe.
     */
    void addCellToProcessingList(LavaCell2 cell)
    {
        cells[size.getAndIncrement()] = cell;
    }
    
    /** 
     * Removes deleted cells from the storage array. 
     * Does not remove them from cell stacks in locator.
     * Call after already cell has been unlinked from other 
     * cells in column and removed (and if necessary replaced) in locator.
     * NOT Thread-safe.
     */
    public void clearDeletedCells()
    {
        int i = 0;
        while(i < this.size.get())
        {
            if(cells[i].isDeleted())
            {
                cells[i] = cells[this.size.decrementAndGet()];
                cells[this.size.get()] = null;
            }
            else
            {
                i++;
            }
        }
    }
    
    /** 
     * Ensures processing array has sufficient storage.  
     * Should be called periodically to prevent overflow 
     * and free unused memory.
     */
    public void manageCapacity()
    {
        int capacity = this.cells.length - this.size.get();
        if(capacity < 0x8000)
        {
            this.cells = Arrays.copyOf(cells, this.cells.length + 0x10000);
        }
        else if(capacity >= 0x20000)
        {
            this.cells = Arrays.copyOf(cells, this.cells.length - 0x10000);
        }
    }
    
    public void writeNBT(NBTTagCompound nbt)
    {
        Adversity.log.info("Saving " + size.get() + " lava cells.");
        int[] saveData = new int[size.get() * LavaCell2.LAVA_CELL_NBT_WIDTH];
        int i = 0;

        for(int j = 0; j < this.size.get(); j++)
        {
            if(cells[j] != null) cells[j].writeNBT(saveData, i);
            
            // Java parameters are always pass by value, so have to advance index here
            i += LavaCell2.LAVA_CELL_NBT_WIDTH;
        }         
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
            this.size.set(saveData.length / LavaCell2.LAVA_CELL_NBT_WIDTH);
            
            this.cells = new LavaCell2[this.size.get() + 0x10000];
            
            int i = 0;
            int c = 0;
            while(i < saveData.length)
            {
                LavaCell2 cell = new LavaCell2(saveData, i);
                
             // Java parameters are always pass by value, so have to advance index here
                i += LavaCell2.LAVA_CELL_NBT_WIDTH;
                        
                cell.clearBlockUpdate(sim);
                
                this.cells[c++] = cell;
                
                LavaCell2 startingCell = this.getEntryCell(cell.x(), cell.z());
                
                if(startingCell == null)
                {
                    this.setEntryCell(cell.x(), cell.z(), cell);
                }
                else
                {
                    startingCell.addCellToColumn(cell);
                }
                
            }
         
            Adversity.log.info("Loaded " + this.size.get() + " lava cells.");
        }
    }
    
    private static class CellChunk
    {
        private final long packedChunkpos;
        
        private LavaCell2[] entryCells = new LavaCell2[256];
        
        private CellChunk(long packedChunkPos)
        {
            this.packedChunkpos = packedChunkPos;
        }

        private AtomicInteger entryCount = new AtomicInteger(0);
        
        /** 
         * Returns the starting cell for the stack of cells located at x, z.
         * Returns null if no cells exist at that location.
         * Thread safe.
         */
        private LavaCell2 getEntryCell(int x, int z)
        {
            return this.entryCells[getIndex(x, z)];
        }
        
        /**
         * Sets the entry cell for the stack of cells located at x, z.
         * Should be thread safe if not accessing same x, z.
         */
        private void setEntryCell(int x, int z, LavaCell2 entryCell)
        {
            int i = getIndex(x, z);
            boolean wasNull = this.entryCells[i] == null;
            
            this.entryCells[i] = entryCell;
            
            if(wasNull)
            {
                if(entryCell != null) this.entryCount.incrementAndGet();
            }
            else
            {
                if(entryCell == null) this.entryCount.decrementAndGet();
            }
        }
        
        /** How many x. z locations in this chunk have at least one cell? */
        int getEntryCount()
        {
            return this.getEntryCount();
        }
        
        private static int getIndex(int x, int z)
        {
            return ((x & 15) << 4) | (z & 15);
        }
    }
}
