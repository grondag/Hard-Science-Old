package grondag.adversity.feature.volcano.lava;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
    
    private LinkedList<FlowNode> flowUpdates = new LinkedList<FlowNode>();
    private HashSet<FlowNode> flows = new HashSet<FlowNode>();
    private LinkedList<LavaBlockUpdate> blockUpdates = new LinkedList<LavaBlockUpdate>();
    
    public LavaManager2(BlockPos origin, World world)
    {
        this.origin = origin;
        this.world = world;
        this.cellTracker = new CellTracker(origin, EXPANSION_RADIUS, world);
        cellTracker.initializeSpaces();
         
        LavaCell cell = cellTracker.getCellForBlockPos(origin.up());
        
        CoreNode core = new CoreNode(this, cell);
        flowUpdates.addLast(core);
        flows.add(core);
        blockUpdates.addAll(cell.getBlockUpdates());

    }
    
    public void registerFlow(FlowNode flow)
    {
        flows.add(flow);
    }
    
    public void unregisterFlow(FlowNode flow)
    {
        flows.remove(flow);
    }
    
    public void flow()
    {
        
        int updateCount = flowUpdates.size();
        if(updateCount > 0)
        {
            for(int i = 0; i < updateCount; i++)
            {
                FlowNode flow = flowUpdates.poll();
                flowUpdates.addAll(flow.flow(this, world));
                for(LavaCell cell : flow.getCells())
                {
                    blockUpdates.addAll(cell.getBlockUpdates());
                }
            }
        }
    }
    
    public Queue<LavaBlockUpdate> getBlockUpdates()
    {
        return this.blockUpdates;
    }
}
