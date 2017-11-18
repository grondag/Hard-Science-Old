package grondag.hard_science.superblock.placement;

import grondag.hard_science.library.world.WorldMap;
import net.minecraft.util.math.BlockPos;

public class VirtualStateManager extends WorldMap<VirtualWorld>
{
    public static final VirtualStateManager INSTANCE = new VirtualStateManager();
    
    /**
     * 
     */
    private static final long serialVersionUID = -3277074863079131351L;

    private VirtualStateManager() {}
    
    @Override
    protected VirtualWorld load(int dimension)
    {
        return new VirtualWorld(dimension);
    }

    public void startTrackingBuild(Build build)
    {
        // TODO Auto-generated method stub
        
    }

    public void stopTrackingBuild(Build build)
    {
        // TODO Auto-generated method stub
        
    }

    public void startBuildTracking(Build build, BlockPos pos)
    {
        // TODO Auto-generated method stub
        
    }

    public void stopBuildTracking(Build build, BlockPos pos)
    {
        // TODO Auto-generated method stub
        
    }
    
    
}
