package grondag.adversity.feature.volcano.lava;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.minecraft.world.World;

public class DropNode extends FlowNode
{
    private final LavaCell cell;
    
    public DropNode(LavaManager2 lavaManager, LavaCell cell, FlowNode firstInput)
    {
        super(lavaManager);
        this.cell = cell;
        cell.setFlowNode(this);
        adjustLevel(lavaManager);
        this.addInput(firstInput);
    }

    /**
     * Sets level of this drop block according to following rules.
     * Level will be within 1.0 of the floor of the highest input that is not cut off.
     * "Highest" is determined by floor, not level.
     * If the floor of this cell is more than 1.0 below the floor of the highest input
     * then depth of this cell may be more than 1.0. 
     * 
     * If this cell and highest inputs are all resting on flow blocks,
     * then the level ranges between 0 and 1 below the floor of the highest input
     * that is not cut off, with steeper slopes giving a lower level.
     * 
     * If this cell and the highest inputs are not all flow blocks, then
     * height is always the floor of the highest input.
     * 
     * In all cases, level of this flow  cannot be higher than the level of the highest input,
     * and must be at least 0.5 higher than floor of this cell.
     * 
     * Also, level of this cell can increase but lava never drains (level never decreases.)
     */
    private void adjustLevel(LavaManager2 lavaManager)
    {
        float maxInputLevel = 0;
        float maxInputFloor = 0;
        boolean areAllMaxInputsFlowBlocks = false;
        for(LavaCell neighbor : lavaManager.cellTracker.getAdjacentCells(cell.x, cell.z, cell.getFloor(), cell.getCeiling(), LavaCell.MINIMUM_OPENING))
        {
            if(isCellAnInput(neighbor) && !neighbor.getFlowNode().isCutOff())         
            {
                if(neighbor.getFloor() > maxInputFloor)
                {
                    maxInputFloor = neighbor.getFloor();
                    areAllMaxInputsFlowBlocks = neighbor.hasFlowBlockFloor();
                }
                else if(neighbor.getFloor() == maxInputFloor)
                {
                    areAllMaxInputsFlowBlocks = areAllMaxInputsFlowBlocks && neighbor.hasFlowBlockFloor();
                }
                maxInputLevel = Math.max(maxInputLevel, neighbor.getLevel());
            }
        }
        
        float newLevel;
        
        if(this.cell.hasFlowBlockFloor() && areAllMaxInputsFlowBlocks)
        {
            // if all have flowblock floors, new level is the average of the floors
            // clamped so that overall drop is never more than 1.0
            // Later logic will ensure total depth is at minimum flow depth.
            
            //Examples (inputfloor -> thisfloor = new level)
            //2 -> 1  = 1.5
            //2 -> 1.5 = 1.75 
            //2 -> 0 = 1
            //5 -> 0 = 4
            
            float drop = maxInputFloor - this.cell.getFloor();
            newLevel = maxInputFloor - Math.min(1F, drop / 2F);
        }
        else
        {
            newLevel = maxInputFloor;
        }
        
        //cannot be higher than level of highest input level
        newLevel = Math.min(newLevel, maxInputLevel);

        //can't be higher than my ceiling
        newLevel = Math.min(newLevel, cell.getCeiling());
        
        // must be at least 0.5 above my floor
        newLevel = Math.max(newLevel, this.cell.getFloor() + LavaCell.MINIMUM_OPENING);
        
        //level can't decrease
        if(newLevel > this.getLeve())
        {
            this.setLevel(newLevel);
        }
    }
    
    @Override
    public List<FlowNode> flow(LavaManager2 lavaManager, World world)
    {
        //NB - not checking for new inputs here because expected
        //that any nodes flowing into this instance will add themselves
        //as inputs and then add this instance to the flow queue.
        
        adjustLevel(lavaManager);
        
        //check for new outputs if not cut off
        if(!this.isCutOff())
        {
            List<LavaCell> neighbors = lavaManager.cellTracker
                    .getAdjacentCells(cell.x, cell.z, cell.getFloor(), this.getLeve(), LavaCell.MINIMUM_OPENING);
            
            Set<LavaCell> lowestNeighbors = new HashSet<LavaCell>();
            float lowestFloor = 256F;
            
            for(LavaCell n : neighbors)
            {
                if(n.getFloor() < this.cell.getFloor())
                {
                    if(n.getFloor() < lowestFloor)
                    {
                        lowestFloor = n.getFloor();
                        lowestNeighbors.clear();
                        lowestNeighbors.add(n);
                    }
                    else if(n.getFloor() == lowestFloor)
                    {
                        lowestNeighbors.add(n);
                    }
                }
            }
            
            for(LavaCell lowest : lowestNeighbors)
            {
                FlowNode lowestNode  = lowest.getFlowNode();
                if(lowestNode == null)
                {
                    
                    result.add(FlowFactory.createFlowFromCell(lavaManager, lowest, this));
                }
                else if(!this.getOutputs().contains(lowestNode))
                {
                    this.addOutput(lowestNode);
                    result.add(lowestNode);
                }
            }
   
        }
        
        //TODO: if this is cut off but not blocked, start to drain 
        //remove lava from parts of the cell that have no adjacent solid faces
        
        //TODO: if has no outputs and is blocked, convert to pool node
        
        //TODO: merge with pool or spread if at same level and has no other outputs
        
        return Collections.unmodifiableList(result);
    }

    @Override
    public Set<LavaCell> getCells()
    {
        return Collections.singleton(cell);
    }

    @Override
    public boolean canAcceptAtCurrentLevel()
    {
        return true;
    }
    
    @Override
    protected void updateCellLevel(float newLevel)
    {
        cell.setLevel(newLevel);
    }

}
