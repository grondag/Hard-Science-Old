package grondag.adversity.feature.volcano.lava;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.minecraft.world.World;

public class DropNode extends FlowNode
{
    private final LavaCell cell;
    
    //prevent reporting blocked when first created so has chance to establish outputs
    private boolean isNew = true;
    
    public DropNode(LavaManager2 lavaManager, LavaCell cell, FlowNode firstInput)
    {
        super(lavaManager);
        this.cell = cell;
        cell.setFlowNode(this);
        adjustLevel(lavaManager);
        this.addInput(firstInput);
    }

    private void adjustLevel(LavaManager2 lavaManager)
    {
        
        
        float maxInputLevel = cell.getFloor() + LavaCell.MINIMUM_OPENING;
        float maxInputFloor = 0;
        float minAdjacentFloor = cell.getFloor();
        
        for(LavaCell neighbor : lavaManager.cellTracker.getAdjacentCells(cell.x, cell.z, cell.getFloor(), cell.getCeiling(), LavaCell.MINIMUM_OPENING))
        {
            if(isCellAnInput(neighbor))         
            {
                maxInputFloor = Math.max(maxInputFloor, neighbor.getFloor());
                maxInputLevel = Math.max(maxInputLevel, neighbor.getLevel());
            }
            minAdjacentFloor = Math.min(minAdjacentFloor, neighbor.getFloor());
        }
        
        float idealLevel = cell.getFloor() + LavaCell.MINIMUM_OPENING;
        
        //idealLevel += (1 - idealLevel) * 
                
        //TODO
        //Add isFlowBottom to LavaCell
        //Make height logic for drops depend on isFlowBottom
        //If highest input is not isFlowBottom have to go up to floor of highest input
        //If highest input is flowBottom and difference is > 1, then go up to highest floor - some magic number
        //Otherwise use a number that depends on slope (see excel worksheet for forumula)
        
                
        //must be at least as high as floor of highest input
        idealLevel = Math.max(idealLevel, maxInputFloor);
        
        //can't be higher than highest adjacent input level
        //but this is set to at least my floor + minimum opening in case we have a strange case
        idealLevel = Math.min(idealLevel, maxInputLevel);
        
        //can't be higher than my ceiling
        idealLevel = Math.min(idealLevel,cell.getCeiling());
        
        //level can't decrease
        if(idealLevel > this.getLeve())
        {
            cell.setLevel(idealLevel);
            this.setLevel(idealLevel);
        }
    }
    
    @Override
    public List<FlowNode> flow(LavaManager2 lavaManager, World world)
    {
        isNew = false;
        adjustLevel(lavaManager);
        
        //TODO: look for new outputs, 
        //TODO: merge with pool or spread if appropriate
        return null;
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
    public boolean isBlocked()
    {
        if(isNew)
            return false;
        else
            return super.isBlocked();
    }

}
