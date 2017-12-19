package grondag.hard_science.simulator.device;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.hard_science.simulator.transport.endpoint.Connector;
import net.minecraft.util.EnumFacing;

/**
 * Tracks in-world manifestation of devices at a given block
 * position. Is not owned by any device, but is instead a 
 * container for connectors and perhaps other thing in the
 * future.<p>
 * 
 */
public class DeviceBlock
{
    public final long packedBlockPos;
    
    private final Connector[] connectors = new Connector[6];
    private int connectionCount = 0;
    
    public DeviceBlock(long packedBlockPos)
    {
        this.packedBlockPos = packedBlockPos;
    }
    
    /**
     * Get connector on the given face.  Returns null if none.
     */
    @Nullable
    public Connector getConnector(EnumFacing face)
    {
        return this.connectors[face.ordinal()];
    }
    
    public synchronized void setConnector(@Nonnull EnumFacing face, @Nullable Connector connector)
    {
        if(this.connectors[face.ordinal()] == null)
        {
            if(connector != null) this.connectionCount++;
        }
        else
        {
            if(connector == null) this.connectionCount--;
        }
        
        this.connectors[face.ordinal()] = connector;
    }
    
    public int connectionCount()
    {
        return this.connectionCount;
    }
    
    public boolean isEmpty()
    {
        return this.connectionCount == 0;
    }
}
