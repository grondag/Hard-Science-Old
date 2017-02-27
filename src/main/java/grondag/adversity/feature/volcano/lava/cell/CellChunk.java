package grondag.adversity.feature.volcano.lava.cell;

import java.util.concurrent.atomic.AtomicInteger;

import grondag.adversity.feature.volcano.lava.cell.builder.CellColumn;
import grondag.adversity.feature.volcano.lava.cell.builder.CellStackBuilder;
import grondag.adversity.feature.volcano.lava.cell.builder.ColumnChunkBuffer;

class CellChunk
{
    final long packedChunkpos;
    
    private LavaCell2[] entryCells = new LavaCell2[256];
    
    CellChunk(long packedChunkPos)
    {
        this.packedChunkpos = packedChunkPos;
    }

    private AtomicInteger entryCount = new AtomicInteger(0);
    
    /**
     * Creates cells for the given chunk if it is not already loaded.
     * If chunk is already loaded, validates against the chunk data provided.
     */
    public void loadOrValidateChunk(LavaCells cells, ColumnChunkBuffer chunkBuffer)
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
        }
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