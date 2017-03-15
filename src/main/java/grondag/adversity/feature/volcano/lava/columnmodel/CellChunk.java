package grondag.adversity.feature.volcano.lava.columnmodel;

import java.util.concurrent.atomic.AtomicInteger;

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
public class CellChunk
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

    /** count of cells that have requested validation since last validation occurred */
    private final AtomicInteger validationCount = new AtomicInteger(0);

    //    /** Set to true after first loaded. Also set true by NBTLoad.  */
//    private boolean isLoaded = false;

    /** Set to true when chunk is unloaded and should no longer be processed */
    private boolean isUnloaded = false;

    /** If true, chunk needs full validation. Should always be true if isLoaded = False. */
    private boolean needsFullValidation = true;

    CellChunk(long packedChunkPos, LavaCells cells)
    {
        this.packedChunkPos = packedChunkPos;
        this.cells = cells;
    }

//    /** for use when reading from NBT */
//    public void setLoaded()
//    {
//        this.isLoaded = true;
//    }
    
    /** 
     * Marks this chunk for full validation.  
     * Has no effect if it already so or if chunk is unloaded.
     */
    public void requestFullValidation()
    {
        if(!this.isUnloaded) this.needsFullValidation = true;
    }
    
    /**
     * True if chunk needs to be loaded for first time or a full revalidation has been requested.
     * Will also be true if more than 1/4 of the cells in the chunk are individually marked for validation.
     */
    public boolean needsFullLoadOrValidation()
    {
        return (this.needsFullValidation || this.validationCount.get() > 64) && !this.isUnloaded;
    }
    
    /**
     * Validates any cells that have been marked for individual validation.
     * 
     * Will return without doing any validation if a full validation is already needed.
     * @param worldBuffer
     * 
     * @return true if any cells were validated.
     */
    public boolean validateMarkedCells()
    {
        if(this.isUnloaded || this.needsFullLoadOrValidation() || this.validationCount.get() == 0) return false;

        synchronized(this)
        {
            CellStackBuilder builder = new CellStackBuilder();
            CellColumn columnBuffer = new CellColumn();

            for(int x = 0; x < 16; x++)
            {
                for(int z = 0; z < 16; z++)
                {
                    LavaCell2 entryCell = this.getEntryCell(x, z);

                    if(entryCell != null && entryCell.isValidationNeeded())
                    {
                        columnBuffer.loadFromWorldStateBuffer(this.cells.sim.worldBuffer, x, z);
                        entryCell = builder.updateCellStack(cells, columnBuffer, entryCell, x, z);
                        entryCell.setValidationNeeded(false);
                        this.setEntryCell(x, z, entryCell);
                    }
                }
            }
        }
        
        return true;
    }

    /**
     * Creates cells for the given chunk if it is not already loaded.
     * If chunk is already loaded, validates against the chunk data provided.
     */
    public void loadOrValidateChunk(ColumnChunkBuffer chunkBuffer)
    {
        synchronized(this)
        {
            if(this.isUnloaded) return;

            CellStackBuilder builder = new CellStackBuilder();
            CellColumn columnBuffer = new CellColumn();

            int xStart = PackedBlockPos.getChunkXStart(chunkBuffer.getPackedChunkPos());
            int zStart = PackedBlockPos.getChunkZStart(chunkBuffer.getPackedChunkPos());
            
            for(int x = 0; x < 16; x++)
            {
                for(int z = 0; z < 16; z++)
                {
                    columnBuffer.loadFromChunkBuffer(chunkBuffer, x, z);
                    LavaCell2 entryCell = this.getEntryCell(x, z);

                    if(entryCell == null)
                    {
                        this.setEntryCell(x, z, builder.buildNewCellStack(cells, columnBuffer, xStart + x, zStart + z));
                    }
                    else
                    {
                        this.setEntryCell(x, z, builder.updateCellStack(cells, columnBuffer, entryCell, xStart + x, zStart + z));
                    }
                }
            }

            //  this.isLoaded = true;
            this.needsFullValidation = false;
        }
    }

    /**
     * Call from any cell column when the first cell in that column
     * is marked for validation after the last validation of that column.
     */
    public void incrementValidationCount()
    {
        if(this.isUnloaded) return;

        this.validationCount.incrementAndGet();
    }
    
    /**
     * Call when any cell in this chunk becomes active.
     * The chunk must already exist at this point but will force it to be and stay loaded.
     * Will also cause neighboring chunks to be loaded so that lava can flow into them.
     */
    public void incrementActiveCount()
    {
        if(this.isUnloaded) return;

        if(this.activeCount.incrementAndGet()  == 1)
        {
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
        if(this.isUnloaded) return;
        
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
        if(!this.isUnloaded) this.retainCount.incrementAndGet();
    }

    /**
     * Call when a neighboring chunk no longer has active cells. 
     * Allow this chunk to be unloaded if no other neighbors are retaining it and it has no active cells.
     */
    public void release()
    {
        if(!this.isUnloaded) this.retainCount.decrementAndGet();
    }

    public boolean canUnload()
    {
        return this.activeCount.get() == 0 && this.retainCount.get() == 0 && !this.isUnloaded;
    }

    public void unload()
    {
        if(this.isUnloaded) return;

        for(int x = 0; x < 16; x++)
        {
            for(int z = 0; z < 16; z++)
            {
                LavaCell2 entryCell = this.getEntryCell(x, z);

                if(entryCell != null)
                {
                    entryCell = entryCell.firstCell();
                    do
                    {
                        LavaCell2 nextCell = entryCell.aboveCell();
                        entryCell.setDeleted();
                        entryCell = nextCell;
                    }
                    while(entryCell != null);

                    this.setEntryCell(x, z, null);
                }
            }
        }
        this.isUnloaded = true;
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
        if(this.isUnloaded) return;

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