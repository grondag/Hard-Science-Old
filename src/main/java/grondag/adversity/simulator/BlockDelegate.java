package grondag.adversity.simulator;

import grondag.adversity.simulator.base.Delegate;
import grondag.adversity.simulator.base.NodeEvent;
import net.minecraft.util.math.BlockPos;

public class BlockDelegate extends Delegate{
	
	private BlockPos pos;
	private int dimensionId;
	
	public BlockDelegate(BlockPos pos, int dimensionId)
	{
		this.pos = pos;
		this.dimensionId = dimensionId;
	}
	
	public BlockPos getPos() { return pos; }
	public int getDimensionID() { return dimensionId; }

    @Override
    public void postEvent(NodeEvent event)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public NodeEvent[] pullEvents()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
