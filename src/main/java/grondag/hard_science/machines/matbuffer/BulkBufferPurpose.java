package grondag.hard_science.machines.matbuffer;

import grondag.hard_science.simulator.resource.FluidResource;

public enum BulkBufferPurpose
{
    FUEL,
    PRIMARY_INOUT,
    SECONDARY_INOUT,
    
    // value used during deserialization to indicate bad read
    INVALID;
    
    public final FluidResource fluidResource;
    
    private BulkBufferPurpose()
    {
        this(null);
    }
    
    private BulkBufferPurpose(FluidResource fluidResource)
    {
        this.fluidResource = fluidResource;
    }
    
}
