package grondag.adversity.feature.volcano.lava.cell;

import java.util.concurrent.atomic.AtomicInteger;

import grondag.adversity.feature.volcano.lava.cell.builder.CellColumn;
import grondag.adversity.feature.volcano.lava.cell.builder.CellStackBuilder;
import grondag.adversity.feature.volcano.lava.cell.builder.ColumnChunkBuffer;
import grondag.adversity.library.PackedBlockPos;
/**
 * Container for all cells in a world chunk.
 * When a chunk is loaded (or updated) all cells that can exist in the chunk are created.
 * 
 * Lifecycle notes
 * ---------------------------------------
 * when a chunk gets lava for the first time
 *       is created
 *       becomes active
 *       retains neighboring chunks
 *       must be loaded
 *       
 * when a chunk gets retained for the first time
 *      is created
 *      must be loaded
 *      
 * chunks can be unloaded when
 *      they are not active
 *      AND they are not retained           
 */
class CellChunk
{
    
    public final long packedChunkPos;
    
    private final LavaCell2[] entryCells = new LavaCell2[256];
    
    /** number of cells in the chunk */
    private final AtomicInteger entryCount = new AtomicInteger(0);
    
    /** Reference to the cells collection in which this chunk lives. */
    public final LavaCells cells;

    /** count of cells in this chunk containing lava */
    private final AtomicInteger activeCount = new AtomicInteger(0);
    
    /** count of neighboring active chunks that have requested this chunk to remain loaded*/
    private final AtomicInteger retainCount = new AtomicInteger(0);
    
    /** tracks first time load */
    private boolean isLoaded = false;
    
    CellChunk(long packedChunkPos, LavaCells cells)
    {
        this.packedChunkPos = packedChunkPos;
        this.cells = cells;
    }
    
    /**
     * Creates cells for the given chunk if it is not already loaded.
     * If chunk is already loaded, validates against the chunk data provided.
     */
    public void loadOrValidateChunk(ColumnChunkBuffer chunkBuffer)
    {
        synchronized(this)
        {
            CellStackBuilder builder = new CellStackBuilder();
            CellColumn columnBuffer = new CellColumn();
            
            for(int x = 0; x < 16; x++)
            {
                for(int z = 0; z < 16; z++)
                {
                    columnBuffer.loadFromChunkBuffer(chunkBuffer, x, z);
                    LavaCell2 entryCell = this.getEntryCell(x, z);
                    
                    if(entryCell == null)
                    {
                        this.setEntryCell(x, z, builder.buildNewCellStack(cells, columnBuffer, x, z));
                    }
                    else
                    {
                        this.setEntryCell(x, z, builder.updateCellStack(cells, columnBuffer, entryCell, x, z));
                    }
                }
            }
            this.isLoaded = true;
        }
    }
    
    /**
     * Call when any cell in this chunk becomes active.
     * The chunk must already exist at this point but will force it to be and stay loaded.
     * Will also cause neighboring chunks to be loaded so that lava can flow into them.
     */
    public void incrementActiveCount()
    {
        if(this.activeCount.incrementAndGet()  == 1)
        {
            // force load if hasn't been loaded already due to retention
            if(this.retainCount.get() == 0)
            {
                this.cells.sim.cellChunkLoader.markChunk(this.packedChunkPos);
            }
            
            // create (if needed) and retain all neighbors
            int myX = PackedBlockPos.getChunkXPos(this.packedChunkPos);
            int myZ = PackedBlockPos.getChunkZPos(this.packedChunkPos);
            
            this.cells.getOrCreateCellChunk(myX + 1, myZ).retain();
            this.cells.getOrCreateCellChunk(myX - 1, myZ).retain();
            this.cells.getOrCreateCellChunk(myX, myZ + 1).retain();
            this.cells.getOrCreateCellChunk(myX, myZ - 1).retain();
        }
    }
    
    /**
     * Call when any cell in this chunk becomes inactive.
     * When no more cells are active will allow this and neighboring chunks to be unloaded.
     */
    public void decrementActiveCount()
    {
       if(this.activeCount.decrementAndGet() == 0)
       {
           // release all neighbors
           int myX = PackedBlockPos.getChunkXPos(this.packedChunkPos);
           int myZ = PackedBlockPos.getChunkZPos(this.packedChunkPos);
           
           this.cells.getOrCreateCellChunk(myX + 1, myZ).release();
           this.cells.getOrCreateCellChunk(myX - 1, myZ).release();
           this.cells.getOrCreateCellChunk(myX, myZ + 1).release();
           this.cells.getOrCreateCellChunk(myX, myZ - 1).release();
       }
    }
    
    /**
     * Call when a neighboring chunk becomes active (has active cells) to force this
     * chunk to be and stay loaded. (Getting a reference to this chunk to call retain() will cause it to be created.)
     * This creates connections and enables lava to flow into this chunk if it should.
     */
    public void retain()
    {
        if(this.retainCount.incrementAndGet() == 1 && this.activeCount.get() == 0)
        {
            this.cells.sim.cellChunkLoader.markChunk(this.packedChunkPos);
        }
    }
    
    /**
     * Call when a neighboring chunk no longer has active cells. 
     * Allow this chunk to be unloaded if no other neighbors are retaining it and it has no active cells.
     */
    public void release()
    {
        this.retainCount.decrementAndGet();
    }
    
    public boolean isLoaded()
    {
        return this.isLoaded;
    }
    
    public boolean canUnload()
    {
        return this.activeCount.get() == 0 && this.retainCount.get() == 0;
    }
    
    /** 
     * Returns the starting cell for the stack of cells located at x, z.
     * Returns null if no cells exist at that location.
     * Thread safe.
     */
    LavaCell2 getEntryCell(int x, int z)
    {
        return this.entryCells[getIndex(x, z)];
    }
    
    /**
     * Sets the entry cell for the stack of cells located at x, z.
     * Should be thread safe if not accessing same x, z.
     */
    void setEntryCell(int x, int z, LavaCell2 entryCell)
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
    public int getEntryCount()
    {
        return this.entryCount.get();
    }
    
    private static int getIndex(int x, int z)
    {
        return ((x & 15) << 4) | (z & 15);
    }
}