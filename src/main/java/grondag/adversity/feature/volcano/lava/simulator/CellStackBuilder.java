package grondag.adversity.feature.volcano.lava.simulator;

import grondag.adversity.Adversity;

/** Builds a new cell stack from a CellColumn */
public class CellStackBuilder
{
    /** true if we've identified a space in world column that should be a cell */
    private boolean isCellStarted = false;
    
    /** floor of cell currently in construction - in fluid levels relative to world floor*/
    private int floor;
  
    private boolean isFlowFloor;
    
    private LavaCell entryCell;
    
    private void startCell(int floor, boolean isFlowFloor)
    {
        this.isCellStarted = true;
        this.floor = floor;
        this.isFlowFloor = isFlowFloor;
    }
    
    private void completeCell(LavaCells cells, CellColumn column, int x, int z, int ceiling)
    {
        
        if(this.entryCell == null)
        {
            this.entryCell = new LavaCell(cells, x, z, this.floor, ceiling, this.isFlowFloor);
        }
        else
        {
            this.entryCell.linkAbove(new LavaCell(this.entryCell, this.floor, ceiling, this.isFlowFloor));
            this.entryCell = this.entryCell.aboveCell();
        }
        
        // necessary to prevent cell from getting confused over world state
//        this.entryCell.clearBlockUpdate();
        
        this.isCellStarted = false;
        
    }
    
    /** 
     * Updates the cell stack with given entry cell based on contents of provided CellColum.
     * Expands, splits, adds, deletes or merges cells as needed to match world data on CellColumn.
     * If entry cell is null, functions identically to buildNewCellStack().
     */
    public LavaCell updateCellStack(LavaCells cells, CellColumn worldColumn, LavaCell simEntryCell, int x, int z)
    {
        int y = 0;
        
        /** used to know when a space has a flow floor */
        BlockType lastBlockType = null;
        
        do
        {
            // if at any point we remove or merge cells and there are no more cells left,
            // need to divert to buildNewCellStack to prevent NPE (plus is simpler logic that way)
            // Highly unlikely though that this will ever happen: implies solid blocks from 0 to world height...
            if(simEntryCell == null) return this.buildNewCellStack(cells, worldColumn, x, z);
            
            BlockType blockType = worldColumn.getBlockType(y);
            
            if(blockType.isBarrier)
            {
                simEntryCell = simEntryCell.addOrConfirmBarrier(y, blockType.isFlow);
            }
            else
            {
                int floor = blockType.isSolid ? blockType.flowHeight : 0;
                
                boolean isFlowFloor = (blockType.isSolid && blockType.isFlow) 
                        || ((blockType == BlockType.SPACE || blockType.isLava) && lastBlockType == BlockType.SOLID_FLOW_12);
                
                simEntryCell = simEntryCell.addOrConfirmSpace(y, floor, isFlowFloor);
            }
            
            lastBlockType = blockType;
            
        } while(++y < 256);
        
        if(Adversity.DEBUG_MODE)
        {
            // validate no cell overlap
            LavaCell testCell1 = simEntryCell.firstCell();
            while(testCell1 != null)
            {
                LavaCell testCell2 = simEntryCell.firstCell();
                while(testCell2 != null)
                {
                    if(testCell1 != testCell2)
                    {
                        if(testCell1.intersectsWith(testCell2))
                            Adversity.log.warn("Found interesecting cells in same column after rebuild. Should never happen. ");
                        
                        if(testCell1.isVerticallyAdjacentTo(testCell2))
                            Adversity.log.warn("Found vertically adjacent cells in same column after rebuild. Should never happen. ");
                    }
                    
                    testCell2 = testCell2.above;
                }
                
                testCell1 = testCell1.above;
            }
        }
      
        return simEntryCell;
        
    }
    
//    /**
//     * Returns Block Type if it can be inferred from the given cell at world level Y. 
//     * Otherwise returns barrier.  This logic assumes that the given cell is the closest cell to Y
//     * and caller should ensure this before calling either by checking that y is within the cell
//     * or by calling LavaCell2.findCellNearestY().
//     */
//    private static BlockType getBlockTypeWithinCell(LavaCell2 cell, int y)
//    {
//        if(y > cell.topY()) return BlockType.BARRIER;
//        
//        if(y < cell.bottomY())
//        {
//            return y == cell.bottomY() - 1 && cell.isBottomFlow() 
//                    ? BlockType.SOLID_FLOW_12 : BlockType.BARRIER;
//        }
//        
//        // if get to this point, y is within the cell
//        
//        // detect special case of flow floor within the cell
//        if(y == cell.bottomY() && cell.isBottomFlow() && cell.getFluidUnits() == 0 && cell.floorFlowHeight() < LavaSimulator.LEVELS_PER_BLOCK )
//        {
//            return BlockType.SOLID_FLOW_STATES[cell.floorFlowHeight()];
//        }
//        
//        // above lava surface, must be space
//        if(y > cell.fluidSurfaceY()) return BlockType.SPACE;
//        
//        // below lava surface, must be lava
//        if(y < cell.fluidSurfaceY()) return BlockType.LAVA_12;
//        
//        // if get here, implies at lava surface
//        return BlockType.LAVA_STATES[cell.fluidSurfaceFlowHeight()];
//    }
    
    /** 
     * Returns the starting cell for a new list of cells at the given location from the provided column data.
     * Retuns null if there are no spaces for cells in the column data provided.
     */
    public LavaCell buildNewCellStack(LavaCells cells, CellColumn column, int x, int z)
    {
        BlockType lastType = BlockType.BARRIER;
        this.entryCell = null;
        
        for(int y = 0; y < 256; y++)
        {
            BlockType currentType = column.getBlockType(y);
            
            switch(currentType)
            {
                case BARRIER:
                {
                    // Close cell if one is open
                    // Otherwise no action.
                    if(this.isCellStarted) this.completeCell(cells, column, x, z, y * LavaSimulator.LEVELS_PER_BLOCK);
                    break;
                }
                    
                case SOLID_FLOW_1:
                case SOLID_FLOW_2:
                case SOLID_FLOW_3:
                case SOLID_FLOW_4:
                case SOLID_FLOW_5:
                case SOLID_FLOW_6:
                case SOLID_FLOW_7:
                case SOLID_FLOW_8:
                case SOLID_FLOW_9:
                case SOLID_FLOW_10:
                case SOLID_FLOW_11:
                case SOLID_FLOW_12:
                {
                    // Close cell if one is open
                    if(this.isCellStarted) this.completeCell(cells, column, x, z, y * LavaSimulator.LEVELS_PER_BLOCK);
                    
                    // start new cell if not full height
                    if(currentType.flowHeight < LavaSimulator.LEVELS_PER_BLOCK) 
                    {
                        this.startCell(y * LavaSimulator.LEVELS_PER_BLOCK + currentType.flowHeight, true);
                    }
                    break;
                }
                
                case SPACE:
                case LAVA_1:
                case LAVA_2:
                case LAVA_3:
                case LAVA_4:
                case LAVA_5:
                case LAVA_6:
                case LAVA_7:
                case LAVA_8:
                case LAVA_9:
                case LAVA_10:
                case LAVA_11:
                case LAVA_12:
                {
                    // start new cell if this is the first open space
                    if(!this.isCellStarted) this.startCell(y * LavaSimulator.LEVELS_PER_BLOCK, lastType.isFlow);
                    break;
                }
                    
                default:
                    //NOOP - not real
                    break;
            
            }
            lastType = currentType;
        }
        
        // if got all the way to the top of the world with an open cell, close it
        if(this.isCellStarted) this.completeCell(cells, column, x, z, 256 * LavaSimulator.LEVELS_PER_BLOCK);
        
        
        return this.entryCell == null ? null : this.entryCell.selectStartingCell();
    }
}
