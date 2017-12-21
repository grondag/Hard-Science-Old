package grondag.hard_science.machines.base;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.hard_science.Log;
import grondag.hard_science.init.ModConnectors;
import grondag.hard_science.simulator.device.blocks.IDeviceBlockManager;
import grondag.hard_science.simulator.device.blocks.SimpleBlockHandler;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.transport.endpoint.TransportNode;
import net.minecraft.util.EnumFacing;

/**
 * Base class for single-block machines.
 *
 */
public class AbstractSimpleMachine extends AbstractMachine
{

    /**
     * Front (display) face of machine, if non-null.
     * Used to determine placement of connectors.
     */
    @Nullable
    protected EnumFacing frontFace;
    
    @Override
    protected IDeviceBlockManager createBlockManager()
    {
        SimpleBlockHandler result = new SimpleBlockHandler(this)
        {
            @Override
            protected void beforeNodeDestruction(List<TransportNode> nodes)
            {
                Log.info("beforeNodeDestruction");
            }
    
            @Override
            protected void afterNodeCreation(List<TransportNode> nodes)
            {
                Log.info("afterNodeCreation");
            }
    
            @Override
            protected long handleTransportProduce(IResource<?> resource, long quantity, boolean allowPartial, boolean simulate)
            {
                Log.info("handleTransportProduce");
                return 0;
            }
    
            @Override
            protected long handleTransportConsume(IResource<?> resource, long quantity, boolean allowPartial, boolean simulate)
            {
                Log.info("handleTransportConsume");
                return 0;
            }
        };
        
        assert this.frontFace != null
                : "Simple machine missing front face during block manager initializaiton";
        
        for(EnumFacing face : EnumFacing.VALUES)
        {
            if(face != this.frontFace) result.setConnector(face, ModConnectors.powered_item_low);
        }
        
        return result;
    }
    
    @Override
    public boolean hasFront() { return true; }
    
    @Override
    public void setFront(@Nonnull EnumFacing frontFace)
    {
        this.frontFace = frontFace;
        
        // TODO: if changed after connected, 
        // then need to remove and replace device blocks
        // OR ensure this use case doesn't happen
    }
    
    @Nullable
    @Override
    public EnumFacing getFront()
    {
        return this.frontFace;
    }    
}
