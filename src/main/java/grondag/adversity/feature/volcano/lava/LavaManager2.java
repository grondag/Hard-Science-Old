package grondag.adversity.feature.volcano.lava;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LavaManager2
{
    //TODO make configurable
    private static final int EXPANSION_RADIUS = 64;
    
    public final BlockPos origin;
    public final World world;  
    public final CellTracker cellTracker;
    
    private LinkedList<LavaCell> flowUpdates = new LinkedList<LavaCell>();
    private LinkedList<LavaBlockUpdate> blockUpdates = new LinkedList<LavaBlockUpdate>();
    private final LavaCell core;
    
    public LavaManager2(BlockPos origin, World world)
    {
        this.origin = origin;
        this.world = world;
        this.cellTracker = new CellTracker(origin, EXPANSION_RADIUS, world);
        cellTracker.initializeSpaces();
         
        core = cellTracker.getCellForBlockPos(origin.up());
        
    }
    
    public void flow()
    {
        core.addLava(1);
        
        for(LavaCell cell : cellTracker.getLavaCells())
        {
             cell.flow();
        }
        
        cellTracker.applyPendingLavaCellAdds();
        
        for(LavaCell cell : cellTracker.getLavaCells())
        {
            blockUpdates.addAll(cell.getBlockUpdates());
        }
        
        cellTracker.applyPendingLavaCellRemovals();
        
//        flowUpdates.addFirst(core);
//        int updateCount = flowUpdates.size();
//        for(int i = 0; i < updateCount; i++)
//        {
//            LavaCell flow = flowUpdates.poll();
//            flowUpdates.addAll(flow.flow(cellTracker, world));
//            blockUpdates.addAll(flow.getBlockUpdates());
//        }
        
    }
    
    public Queue<LavaBlockUpdate> getBlockUpdates()
    {
        return this.blockUpdates;
    }
}
