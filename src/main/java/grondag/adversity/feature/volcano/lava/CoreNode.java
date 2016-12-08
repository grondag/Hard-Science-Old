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
        this.setLevel(startingLevel);
    }
    
    @Override
    public void updateCellLevel(float newLevel)
    {
        cell.setLevel(newLevel);
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
        
        if(this.isBlocked() && (this.getOutputs().isEmpty() || this.blockedCount > DEFAULT_BLOCKED_RETRY_COUNT))
        {
            //give new output(s) or level a chance to unblock us
            this.blockedCount = 0;
            
            //look first for new outputs
            boolean foundNewOutput = false;

            List<LavaCell> neighbors = lavaManager.cellTracker
                    .getAdjacentCells(cell.x, cell.z, cell.getFloor(), this.getLeve(), LavaCell.MINIMUM_OPENING);
            
            for(LavaCell n : neighbors)
            {
                if(!this.isCellAnOutput(n))
                {
                    foundNewOutput = true;
                    
                    FlowNode openNode = n.getFlowNode();
                    if(openNode == null)
                    {
                        openNode = FlowFactory.createFlowFromCell(lavaManager, n, this);
                    }
                    
                    result.add(openNode);
                }
            }
            
            // Go higher if can't create new outputs.
            if(!foundNewOutput)
            {
                this.setLevel(this.getLeve() + FlowNode.MINIMUM_LEVEL_INCREMENT);
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
