package grondag.adversity.feature.volcano.lava;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.minecraft.world.World;

public class CoreNode extends FlowNode
{
    private final LavaCell cell;
    
    private int blockedCount = 0;
    
    public CoreNode(LavaManager2 lavaManager, LavaCell cell)
    {
        super(lavaManager);
        this.cell = cell;
        cell.setFlowNode(this);
        float startingLevel = Math.min(cell.getCeiling(), cell.getFloor() + 1);
        cell.setLevel(startingLevel);
        this.setLevel(startingLevel);
    }

    @Override
    public List<FlowNode> flow(LavaManager2 lavaManager, World world)
    {
        //core processes every cycle, but nothing to do if not blocked
        if(!this.isBlocked())
        {
            this.blockedCount = 0;
            return Collections.singletonList(this);
        }
        
        blockedCount++;
        
        LinkedList<FlowNode> result = new LinkedList<FlowNode>();
        result.add(this);
        
        if(this.isBlocked())
        {
            if(this.getOutputs().isEmpty() || this.blockedCount > DEFAULT_BLOCKED_RETRY_COUNT)
            {
                //look first for new outputs
                List<LavaCell> neighbors = lavaManager.cellTracker
                        .getAdjacentCells(cell.x, cell.z, cell.getFloor(), cell.getCeiling(), LavaCell.MINIMUM_OPENING);
                
                for(LavaCell n : neighbors)
                {
                    FlowNode node = n.getFlowNode();
                    if(node == null)
                    {
                        //TODO: FINISH
                    }
                }
                
                //TODO: go higher if can't create new outputs
            }
            
        }
        return result;
    }

    @Override
    public boolean isCutOff()
    {
        // Core node never stops flowing
        return false;
    }

    @Override
    public Set<LavaCell> getCells()
    {
        return Collections.singleton(cell);
    }

    @Override
    public float minimumLevelAccepted()
    {
        //no node should ever flow into core
        return 256F;
    }

    @Override
    public boolean canAcceptAtCurrentLevel()
    {
        return false;
    }
}
