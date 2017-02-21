package grondag.adversity.feature.volcano.lava.cell.builder;

import grondag.adversity.feature.volcano.lava.cell.LavaCell2;
import grondag.adversity.feature.volcano.lava.cell.LavaCells;
import net.minecraft.world.chunk.Chunk;

/**
 * this class exists to create cells from a givem x, z position in the world
 * and to validate cells that already exist
 *
 */
public class CellBuilder
{
    /** 
     * Returns the starting cell for a new list of cells at the given location within the chunk.
     * retuns null if there is no space for cells at the given world coordinates
     */
    public static LavaCell2 getNewCells(LavaCells cells, Chunk worldChunk, int x, int z)
    {
    
        CellSpecList specs = new CellSpecList(worldChunk, z, z);
        
        if(specs.isEmpty()) return null;
        
        LavaCell2 entryCell = null;
        
        for(CellSpec spec : specs)
        {
            if(entryCell == null)
            {
                entryCell = new LavaCell2(x, z, spec.floor, spec.ceiling, spec.lavaLevel, spec.isFlowFloor);
            }
            else
            {
                entryCell.linkAbove(new LavaCell2(entryCell, spec.floor, spec.ceiling, spec.lavaLevel, spec.isFlowFloor));
                entryCell = entryCell.aboveCell();
            }
            cells.addCellToProcessingList(entryCell);
        }

        return entryCell.selectStartingCell();
    }
    
    /** 
     * Returns the starting cell for a new and/or updated list of cells from a populated cell column.
     * Creates new cells as needed and deletes cells that no longer exist.
     * starting locatorCell can be null if new or no cells previously
     * retuns null if there are no cells.
     */
    public static LavaCell2 getUpdatedCells(LavaCells cells, Chunk worldChunk, int x, int z, LavaCell2 startingCell)
    {
        if(startingCell == null) return getNewCells(cells, worldChunk, x, z);
        
        CellSpecList worldSpec = new CellSpecList(worldChunk, z, z);
        CellSpecList simSpec = new CellSpecList(startingCell);
        
        
        /**
         * For each cell in world, relate to cells already in sim
         */
        //TODO - stub
        return null;
    }

    /** 
     * Adds new cells in this column or expands or merges existing cells as needed.
     * Ceiling is inclusive, floor is not.
     * Returns cell that should be used as starting reference for this column.
     */
    public LavaCell2 addOrConfirmSpace(int floor, int ceiling, int lavaLevel, boolean isFlowFloor)
    {
        synchronized(this.locator)
        {
            // find the cell below this space, if there is one
            LavaCell2 below = this.findNearestCellBelowLeve(floor);
            
            if(below == null)
            {
                // No existing cell below this space, so result depends 
                // on where space stands related to the first (lowest) existing cell.
                LavaCell2 first = this.locator.firstCell;
                
                if(first.floor > ceiling)
                {
                    // space is below the first cell, and thus the new first (lowest) cell
                    LavaCell2 newCell = new LavaCell2(this, floor, ceiling, lavaLevel, isFlowFloor);
                    newCell.above = this.locator.firstCell;
                    newCell.above.below = newCell;
                    this.locator.firstCell = newCell;
                    return this;
                }
                else if(first.floor == ceiling)
                {
                    // space is below and adjacent to the first cell and thus should expand it
                    first.setFloor(floor, isFlowFloor);
                    first.fluidUnits += Math.max(0, lavaLevel - floor);
                    return this;
                    
                }
                else
                {
                    // space intersects with the first cell
                    // so expand first cell to include entire region
                    
                    // if first cell now intersects with cells above it, merge them in also
                    return first.mergeUpInRegsion()
                } 
            }
        
        
            // if intersects or is adjacent to two cells, expand and merge those cells
            // lower cell wins
            // confirm lava level in merged cell
            
            // if intersects or is adjacent to a single cell, expand that cell
            // confirm lava level in expanded cell
            
            // if no intersection, insert a new cell in the chain
            
            
            if(this.isVerticallyAdjacentTo(floor, ceiling) || this.intersectsWith(floor, ceiling))
            {
                this.setCeiling(Math.max(this.getCeiling(), ceiling));
                this.setFloor(Math.min(this.getFloor(), floor));
                return true;
            }
            else
            {
                return false;
            }
        }
    }
}
