package grondag.hard_science.simulator.transport.L2;

import grondag.hard_science.simulator.resource.StorageType;

public abstract class TransportPTPLink<T extends StorageType<T>> extends TransportLink<T>
{
    protected TransportPTPLink(long capacityPerTick)
    {
        super(capacityPerTick);
    }
}
