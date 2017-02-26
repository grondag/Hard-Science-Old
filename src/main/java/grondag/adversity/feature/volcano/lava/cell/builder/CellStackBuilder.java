package grondag.adversity.feature.volcano.lava.cell.builder;

import grondag.adversity.feature.volcano.lava.AbstractLavaSimulator;
import grondag.adversity.feature.volcano.lava.cell.LavaCell2;
import grondag.adversity.feature.volcano.lava.cell.LavaCells;

/** Builds a new cell stack from a CellColumn */
public class CellStackBuilder
{
    /** true if we've identified a space in world column that should be a cell */
    private boolean isCellStarted = false;
    
    private int floor;
    private int lavaLevel;
    private boolean isFlowFloor;
    private int ceiling;
    
    private static final int NOT_SET = -1;
    
    /** used to check for suspended lava */
    private int maxLavaY;
    /** used to check for suspended lava */
    private int minSpaceY;
    
    private LavaCell2 entryCell;
    
    private void startCell(int floor, boolean isFlowFloor)
    {
        this.isCellStarted = true;
        this.maxLavaY = NOT_SET;
        this.minSpaceY = NOT_SET;
        this.ceiling = NOT_SET;
        this.lavaLevel = NOT_SET;
        this.floor = floor;
        this.isFlowFloor = isFlowFloor;
    }
    
    private void setLavaLevel(int lavaLevel)
    {
        this.lavaLevel = lavaLevel;
    }
    
    private void completeCell(LavaCells cells, CellColumn column, int x, int z, int ceiling)
    {
        //TODO: handle matching cell
        
        if(this.entryCell == null)
        {
            this.entryCell = new LavaCell2(cells, x, z, this.floor, this.ceiling, this.lavaLevel, this.isFlowFloor);
        }
        else
        {
            this.entryCell.linkAbove(new LavaCell2(cells, this.entryCell, this.floor, this.ceiling, this.lavaLevel, this.isFlowFloor));
            this.entryCell = this.entryCell.aboveCell();
        }
        
        if(maxLavaY > minSpaceY)
        {
            //TODO: check for and handle suspended lava
            // Do by two steps: 
            // 1 register particles in cell
            // 2 set clearing level to max lava level - will let cell know to check for lava blocks and set to air
        }
        
        this.isCellStarted = false;
        
    }
    
    /** 
     * Updates the cell stack with given entry cell based on contents of provided CellColum.
     * Expands, splits, adds, deletes or merges cells as needed to match world data on CellColumn.
     * If entry cell is null, functions identically to buildNewCellStack().
     */
    public LavaCell2 updateCellStack(LavaCells cells, CellColumn worldColumn, LavaCell2 simEntryCell, int x, int z)
    {
        if(simEntryCell == null) return this.buildNewCellStack(cells, worldColumn, x, z);
        
        
        BlockType lastWorldType = BlockType.BARRIER;
        BlockType lastSimType = BlockType.BARRIER;
        
        LavaCell2 lastCell = null;
        LavaCell2 closestCell = simEntryCell;
        
        /**
         * TODO
         * 
         * if at any point we remove or merge cells and there are no more cells left,
         * need to divert to buildNewCellStack to prevent NPE (plus is simpler logic that way)
         * Highly unlikely though that this will ever happen: implies solid blocks from 0 to world height...
         */
        
        for(int y = 0; y < 256; y++)
        {
            closestCell = closestCell.findCellNearestY(y);
            
            BlockType currentWorldType = worldColumn.getBlockType(y);
            BlockType currentSimType = getBlockTypeWithinCell(closestCell, y);
            
            switch(currentWorldType)
            {
                case BARRIER:
                {
                    /**
                     * Four possibilities if we have a cell in the sim
                     * 
                     * 1) Cell floor has been raised.
                     * 
                     * 2) The cell no longer exists at all and must be deleted.
                     * Can't know this until we get to the top of the existing cell. 
                     * 
                     * 3) Is the upper part of a cell where ceiling has been lowered. Cannot distinguish from 4 until
                     * or unless we are at the top of the cell.  But we can handle same as 4 and 
                     * rest of split cell is filled in will be handled by logic for case 2.
                     * 
                     * 4) Barrier has been added within the cell and it needs to be split. 
                     */

                    // Close cell if one is open
                    // Otherwise no action.
                    
                    //TODO: pass matching cell
                    if(this.isCellStarted) this.completeCell(cells, worldColumn, x, z, y * AbstractLavaSimulator.LEVELS_PER_BLOCK);
                    
                    break;
                }
                    
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
                
                    
                    break;
                }
                
                case SPACE:
                {
                
                }
                    
                default:
                    //NOOP - not real
                    break;
            
            }
            lastWorldType = currentWorldType;
            lastSimType = currentSimType;
        }
        
        // if got all the way to the top of the world with an open cell, close it
        if(this.isCellStarted) this.completeCell(cells, worldColumn, 256 * AbstractLavaSimulator.LEVELS_PER_BLOCK, z, z);
        
        
        return this.entryCell == null ? null : this.entryCell.selectStartingCell();
        
    }
    
    /**
     * Returns Block Type if it can be inferred from the given cell at world level Y. 
     * Otherwise returns barrier.  This logic assumes that the given cell is the closest cell to Y
     * and caller should ensure this before calling either by checking that y is within the cell
     * or by calling LavaCell2.findCellNearestY().
     */
    private static BlockType getBlockTypeWithinCell(LavaCell2 cell, int y)
    {
        if(y > cell.topY()) return BlockType.BARRIER;
        
        if(y < cell.bottomY())
        {
            return y == cell.bottomY() - 1 && cell.isBottomFlow() 
                    ? BlockType.SOLID_FLOW_12 : BlockType.BARRIER;
        }
        
        // if get to this point, y is within the cell
        
        // detect special case of flow floor within the cell
        if(y == cell.bottomY() && cell.isBottomFlow() && cell.getFluidUnits() == 0 && cell.floorFlowHeight() < AbstractLavaSimulator.LEVELS_PER_BLOCK )
        {
            return BlockType.SOLID_FLOW_STATES[cell.floorFlowHeight()];
        }
        
        // above lava surface, must be space
        if(y > cell.fluidSurfaceY()) return BlockType.SPACE;
        
        // below lava surface, must be lava
        if(y < cell.fluidSurfaceY()) return BlockType.LAVA_12;
        
        // if get here, implies at lava surface
        return BlockType.LAVA_STATES[cell.fluidSurfaceFlowHeight()];
    }
    
    /** 
     * Returns the starting cell for a new list of cells at the given location from the provided column data.
     * Retuns null if there are no spaces for cells in the column data provided.
     */
    public LavaCell2 buildNewCellStack(LavaCells cells, CellColumn column, int x, int z)
    {
        BlockType lastType = BlockType.BARRIER;
        
        for(int y = 0; y < 256; y++)
        {
            BlockType currentType = column.getBlockType(y);
            
            switch(currentType)
            {
                case BARRIER:
                {
                    // Close cell if one is open
                    // Otherwise no action.
                    if(this.isCellStarted) this.completeCell(cells, column, x, z, y * AbstractLavaSimulator.LEVELS_PER_BLOCK);
                    break;
                }
                    
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
                    int blockFloor = y * AbstractLavaSimulator.LEVELS_PER_BLOCK;
                    
                    if(!this.isCellStarted) this.startCell(blockFloor, lastType.isFlow);
                    this.maxLavaY = y;
                  
                    if(this.minSpaceY == NOT_SET)
                    {
                        // update lava level if lava is not suspended
                        this.setLavaLevel(blockFloor + currentType.flowHeight);
                    }
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
                    if(this.isCellStarted) this.completeCell(cells, column, x, z, y * AbstractLavaSimulator.LEVELS_PER_BLOCK);
                    
                    // start new cell if not full height
                    if(currentType.flowHeight < AbstractLavaSimulator.LEVELS_PER_BLOCK) 
                    {
                        this.startCell(y * AbstractLavaSimulator.LEVELS_PER_BLOCK + currentType.flowHeight, true);
                        
                        // counts as open space - any lava subsequently found in this cell must be suspended
                        this.minSpaceY = y;
                    }
                    break;
                }
                
                case SPACE:
                {
                    // start new cell if this is the first open space
                    if(!this.isCellStarted) this.startCell(y * AbstractLavaSimulator.LEVELS_PER_BLOCK, lastType.isFlow);
                    
                    // any lava found above this cell must be suspended
                    if(this.minSpaceY == NOT_SET) this.minSpaceY = y;
                    break;
                }
                    
                default:
                    //NOOP - not real
                    break;
            
            }
            lastType = currentType;
        }
        
        // if got all the way to the top of the world with an open cell, close it
        if(this.isCellStarted) this.completeCell(cells, column, x, z, 256 * AbstractLavaSimulator.LEVELS_PER_BLOCK);
        
        
        return this.entryCell == null ? null : this.entryCell.selectStartingCell();
    }
}
