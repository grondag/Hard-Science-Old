package grondag.adversity.feature.volcano.lava;

import java.util.List;
import java.util.Set;

import net.minecraft.world.World;

public class SpreadNode extends FlowNode
{

    public SpreadNode(LavaManager2 lavaManager)
    {
        super(lavaManager);
        // TODO Auto-generated constructor stub
    }

    @Override
    public List<FlowNode> flow(LavaManager2 lavaManager, World world)
    {
        // TODO
        // Asign orign to newly added cells by choosing the nearest origin of any neighbor.  
        // Update if acquires new neighbors in same spread
        // Distance measured by pythagoras if have line of sight to origin, or by block count otherwise.
        return null;
    }

    @Override
    public Set<LavaCell> getCells()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean canAcceptAtCurrentLevel()
    {
        // TODO Auto-generated method stub
        return false;
    }

}
