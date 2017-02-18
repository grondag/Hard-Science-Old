package grondag.adversity.feature.volcano.lava.cell;

import grondag.adversity.niceblock.modelstate.FlowHeightState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.chunk.Chunk;
import scala.actors.threadpool.Arrays;

/**
 * this class exists to create cells from a givem x, z position in the world
 * and to validate cells that already exist
 *
 */
public class CellBuilder
{
    
    // possible cell content
    private enum BlockType
    {
        SOLID_FLOW,
        SPACE,
        LAVA,
        BARRIER
    }
    
    private static byte BARRIER_INFO = (byte) (BlockType.BARRIER.ordinal() << 4);
    private static int NO_CELL = -1;
    
    private byte[] blockInfo = new byte[255];
    private int[] cellID = new int[255];
    
    void clear()
    {
        
        
    }
    
    /** reads an existing collection of cells into the array for validation against the world */
    void loadFromCells(LavaCell2 startingCell)
    {
        Arrays.fill(blockInfo, BARRIER_INFO);
        Arrays.fill(cellID, NO_CELL);
        
        readCell(startingCell);
        
        LavaCell2 nextCell = startingCell.above;
        while(nextCell != null)
        {
            readCell(nextCell);
            nextCell = nextCell.above;
        }
        
        nextCell = startingCell.below;
        while(nextCell != null)
        {
            readCell(nextCell);
            nextCell = nextCell.below;
        }
    }
    
    void loadFromChunkInfo(ChunkInfo chunk, int x, int z)
    {
        int start = ChunkInfo.getIndex(x & 15, 0, z & 15);
        System.arraycopy(chunk.blockInfo, start, this.blockInfo, 0, 256);
    }
    
    void loadFromWorldChunk(Chunk chunk, int x, int z)
    {
        for(int y = 0; y < 256; y++)
        {
            this.blockInfo[y] = getBlockInfoFromBlockState(chunk.getBlockState(x, y, z));
        }
    }
    
    /** 
     * Returns the starting cell for a new and/or updated list of cells from a populated cell column.
     * Creates new cells as needed and deletes cells that no longer exist.
     * starting locatorCell can be null if new or no cells previously
     * retuns null if there are no cells.
     */
    LavaCell2 getUpdatedCells(LavaCells cells, LavaCell2 startingCell)
    {
        //as we go up, can encounter following change in column data
        // cell lava, w/ or w/o lava, lava barroer, cell ceiling
        LavaCell2 currentCell = startingCell == null ? null : startingCell.firstCell();
        LavaCell2 lastCell = null;
        BlockType currentType = BlockType.BARRIER;
        BlockType lastType = null;
        int currentID = NO_CELL;
        int lastID = NO_CELL;
        
        for(int y = 0; y < 256; y++)
        {
            lastID = currentID;
            lastType = currentType;
            currentType = getBlockTypeFromBlockInfo(this.blockInfo[y]);
            currentID = this.cellID[y];
            
            switch(currentType)
            {
            case BARRIER:
                // if we have a cell at this location, need to remove this space from it
                break;
                
            case LAVA:
                // if no cell currently at this location, need to add space for it
                // also should confirm lava level
                break;
                
            case SOLID_FLOW:
                // if no cell currently at this location, need to add space for it
                // should also confirm presence of floor and no lava
                break;
                
            case SPACE:
                // if no cell currently at this location, need to add space for it
                // should also confirm no lava
                break;
                
            default:
                //NOOP - not real
                break;
            
            }
            if(currentID != lastID)
            {
                
            }
            
        }
        
        
        // if column already had a starting cell, keep it, otherwise give uppermost cell
        
        //TODO - which cell to return here?
        return startingCell == null ? currentCell : startingCell;
    }
    
    private byte getBlockInfo(BlockType type, int level)
    {
        return (byte) (type.ordinal() << 4 | (level & 0xF));
    }
    
    private int getLevelFromBlockInfo(byte blockInfo)
    {
        return blockInfo & 0xF;
    }
    
    private BlockType getBlockTypeFromBlockInfo(byte blockInfo)
    {
        return BlockType.values()[blockInfo >> 4];
    }
    
    private void setBlockInfoAtIndex(int index, BlockType type, int level)
    {
        this.blockInfo[index] = getBlockInfo(type, level);
    }
    
    private void readCell(LavaCell2 cell)
    {
        // handle bottom cell and bottom floor (if needed)
        int floorY = cell.floorY();
        int fluidSurfaceY  = cell.fluidSurfaceY();
        if(cell.isBottomFlow())
        {
            // if cell has a flow-type bottom, bottom barrier depends on floor level and presence of lava:
            int floorHeight = cell.floorFlowHeight();
            if(floorHeight == 0)
            {
                // if cell floor is at a block boundary, then the block *below* the cell should be a full-height flow block
                if(floorY > 0) setBlockInfoAtIndex(floorY - 1, BlockType.SOLID_FLOW, FlowHeightState.BLOCK_LEVELS_INT);
            }
            else
            {
                // if cell floor is within a block, then the lowest block in cell depends on presence of lava
                if(cell.getFluidUnits() == 0)
                {
                    // if floor cell has no lava then the lowest block in the cell should be a solid flow block
                    setBlockInfoAtIndex(floorY, BlockType.SOLID_FLOW, floorHeight);
                }
                else
                {
                    // cell has lava and thus solid portion has melted
                    // cell below should already be a barrier (default fill value) 
                    // later logic will cause the cell to expand downward until it finds a barrier or merges with a lower cell
                    setBlockInfoAtIndex(floorY, BlockType.LAVA, fluidSurfaceY == floorY ? cell.fluidSurfaceFlowHeight() : FlowHeightState.BLOCK_LEVELS_INT);
                }
            }
        }
        else
        {            
            // if cell does not have a flow-type bottom, world blocks depend only on presence of lava
            // Default value in array will already show block below as a barrier
            if(cell.getFluidUnits() == 0)
            {
                // if floor cell has no lava then the lowest block in the cell should be a solid flow block
                setBlockInfoAtIndex(floorY, BlockType.SPACE, 0);
                this.cellID[floorY] = cell.id;
            }
            else
            {
                // cell has lava and thus solid portion has melted
                // cell below should already be a barrier (default fill value) 
                // later logic will cause the cell to expand downward until it finds a barrier or merges with a lower cell
                setBlockInfoAtIndex(floorY, BlockType.LAVA, fluidSurfaceY == floorY ? cell.fluidSurfaceFlowHeight() : FlowHeightState.BLOCK_LEVELS_INT);
                this.cellID[floorY] = cell.id;
            }
        }
        
        // iterate remaining blocks within cell, setting as lava or space as appropriate
        for(int y = floorY + 1; y < cell.topY(); y++)
        {
            if(y > fluidSurfaceY)
            {
                setBlockInfoAtIndex(y, BlockType.SPACE, 0);
            }
            else 
            {
                setBlockInfoAtIndex(y, BlockType.LAVA, y == fluidSurfaceY ? cell.fluidSurfaceFlowHeight() : FlowHeightState.BLOCK_LEVELS_INT);
            }
            this.cellID[y] = cell.id;
        }
    }
    
    
    
    static byte getBlockInfoFromBlockState(IBlockState state)
    {
        //TODO
        return 0;
    }
    
    /** buffers block info for an entire chunk to improve locality of reference */
    static class ChunkInfo
    {
        private byte blockInfo[] = new byte[0xFFFF];
        
        private int xPosition;
        private int zPosition;
        
        void readChunk(Chunk chunk)
        {
            this.xPosition = chunk.xPosition;
            this.zPosition = chunk.zPosition;
            
            //chunk data is optimized for horizontal plane access
            //we are optimized for column access
            for(int y = 0; y < 256; y++)
            {
                for(int x = 0; x < 16; x++)
                {
                    for(int z = 0; z < 16; z++)
                    {
                            this.setBlockInfo(x, y, z, getBlockInfoFromBlockState(chunk.getBlockState(x, y, z)));
                    }
                }
            }
        }
        
        private void setBlockInfo(int x, int y, int z, byte blockInfo)
        {
            this.blockInfo[getIndex(x, y, z)] = blockInfo;
        }
        
        byte getBlockInfo(int x, int y, int z)
        {
            return this.blockInfo[getIndex(x & 15, y, z & 15)];
        }
        
        byte getBlockInfo(int index)
        {
            return this.blockInfo[index];
        }
        
        static int getIndex(int x, int y, int z)
        {
            return x << 12 | z << 8 | y;
        }
    }
}
