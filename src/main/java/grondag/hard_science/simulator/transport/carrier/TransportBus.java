package grondag.hard_science.simulator.transport.carrier;

import grondag.hard_science.simulator.resource.StorageType;

public abstract class TransportBus<T extends StorageType<T>> extends TransportCarrier<T>
{
    protected TransportBus(long capacityPerTick)
    {
        super(capacityPerTick);
    }
}
