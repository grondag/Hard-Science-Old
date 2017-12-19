package grondag.hard_science.simulator.transport.carrier;

import grondag.hard_science.simulator.resource.StorageType;

/**
 * Device that connects two different physical topologies,
 * allowing them to exchange packets. Bridge, router, switch, etc.
 */
public abstract class Gateway<T extends StorageType<T>> extends TransportCarrier<T>
{
    protected Gateway(long capacityPerTick)
    {
        super(capacityPerTick);
    }
}
