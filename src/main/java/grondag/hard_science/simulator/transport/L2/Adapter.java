package grondag.hard_science.simulator.transport.L2;

import grondag.hard_science.simulator.resource.StorageType;

public abstract class Adapter<T extends StorageType<T>> extends TransportLink<T>
{
    protected Adapter(long capacityPerTick)
    {
        super(capacityPerTick);
    }
}
