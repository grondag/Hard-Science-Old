package grondag.hard_science.virtualblock;

import grondag.hard_science.library.world.PerChunkBlockPosQueue;
import grondag.hard_science.library.world.WorldMap;

public class VirtualBlockTracker extends WorldMap<PerChunkBlockPosQueue>
{
    /**
     * 
     */
    private static final long serialVersionUID = -1476913252689559797L;
    
    public static final VirtualBlockTracker INSTANCE = new VirtualBlockTracker();
    
    @Override
    protected PerChunkBlockPosQueue load(int dimension)
    {
        return new PerChunkBlockPosQueue();
    }
    
    @Override
    public void clear()
    {
        this.forEach((k, v) -> { v.clear(); } ); 
        super.clear();
    }
}
