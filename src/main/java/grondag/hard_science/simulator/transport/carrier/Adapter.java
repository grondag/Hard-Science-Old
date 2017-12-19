package grondag.hard_science.simulator.transport.carrier;

import grondag.hard_science.simulator.resource.StorageType;

public abstract class Adapter<T extends StorageType<T>> extends TransportCarrier<T>
{
    protected Adapter(long capacityPerTick)
    {
        super(capacityPerTick);
    }
}
