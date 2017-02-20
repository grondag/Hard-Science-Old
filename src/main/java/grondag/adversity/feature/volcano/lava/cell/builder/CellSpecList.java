package grondag.adversity.feature.volcano.lava.cell.builder;

import java.util.ArrayList;

import grondag.adversity.feature.volcano.lava.AbstractLavaSimulator;
import grondag.adversity.feature.volcano.lava.cell.LavaCell2;
import grondag.adversity.niceblock.base.IFlowBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.chunk.Chunk;

class CellSpecList extends ArrayList<CellSpec>
{
    private static final long serialVersionUID = 6127414709319039427L;
    
    private CellSpec currentSpec = null;
    
    /** reads an existing collection of cells into cell specs for validation against the world */
    public CellSpecList(LavaCell2 startingCell)
    {
        if(startingCell != null)
        {
            LavaCell2 currentCell = startingCell.firstCell();
            while(currentCell != null)
            {
                readCell(currentCell);
                currentCell = currentCell.aboveCell();
            }
        }
    }
    
    public CellSpecList(Chunk chunk, int x, int z)
    {
      
        BlockType lastType = BlockType.BARRIER;
        
        for(int y = 0; y < 256; y++)
        {
            IBlockState state = chunk.getBlockState(x, y, z);
            BlockType currentType = BlockType.getBlockTypeFromBlockState(state);
            int currentFlowHeight = currentType.isFlow ? IFlowBlock.getFlowHeightFromState(state) : 0;
            
            switch(currentType)
            {
                case BARRIER:
                {
                    // Close cell if one is open
                    // Otherwise no action.
                    if(this.isCellStarted()) this.completeCell(y * AbstractLavaSimulator.LEVELS_PER_BLOCK);
                    break;
                }
                    
                case LAVA:
                {
                    // start new cell if this is the first open space
                    int blockFloor = y * AbstractLavaSimulator.LEVELS_PER_BLOCK;
                    
                    if(!this.isCellStarted()) this.startCell(blockFloor, lastType == BlockType.SOLID_FLOW);
                    
                    // update lava level
                    this.setLavaLevel(blockFloor + currentFlowHeight);
                    break;
                }
                
                case SOLID_FLOW:
                {
                    // Close cell if one is open
                    if(this.isCellStarted()) this.completeCell(y * AbstractLavaSimulator.LEVELS_PER_BLOCK);
                    
                    // start new cell if not full height
                    if(currentFlowHeight < AbstractLavaSimulator.LEVELS_PER_BLOCK) 
                    {
                        this.startCell(y * AbstractLavaSimulator.LEVELS_PER_BLOCK + currentFlowHeight, true);
                    }
                    break;
                }
                
                case SPACE:
                {
                    // start new cell if this is the first open space
                    if(!this.isCellStarted()) this.startCell(y * AbstractLavaSimulator.LEVELS_PER_BLOCK, lastType == BlockType.SOLID_FLOW);
                    break;
                }
                    
                default:
                    //NOOP - not real
                    break;
            
            }
            lastType = currentType;
        }
        
        // if got all the way to the top of the world with an open cell, close it
        if(this.isCellStarted()) this.completeCell(256 * AbstractLavaSimulator.LEVELS_PER_BLOCK);
    }
    
    private void readCell(LavaCell2 cell)
    {
        this.startCell(cell.getFloor(), cell.isBottomFlow());
        this.setLavaLevel(cell.fluidSurfaceLevel());
        this.completeCell(cell.getCeiling());
       
    }
    void startCell(int floor, boolean isFlowFloor)
    {
        currentSpec = new CellSpec();
        currentSpec.floor = floor;
        currentSpec.isFlowFloor = isFlowFloor;
    }
    
    void setLavaLevel(int lavaLevel)
    {
        currentSpec.lavaLevel = lavaLevel;
    }
    
    void completeCell(int ceiling)
    {
        currentSpec.ceiling = ceiling;
        this.add(currentSpec);
        currentSpec = null;
    }
    
    boolean isCellStarted()
    {
        return currentSpec != null;
    }
}