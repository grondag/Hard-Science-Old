package grondag.hard_science.simulator.transport.carrier;

import grondag.hard_science.simulator.resource.StorageType;

public abstract class TransportPTPLink<T extends StorageType<T>> extends TransportCarrier<T>
{
    protected TransportPTPLink(long capacityPerTick)
    {
        super(capacityPerTick);
    }
}
