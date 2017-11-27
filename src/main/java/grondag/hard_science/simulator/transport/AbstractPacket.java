package grondag.hard_science.simulator.transport;

import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.StorageType;

/**
 * Describes a resource in transit.
 */
public abstract class AbstractPacket<V extends StorageType<V>> extends AbstractResourceWithQuantity<V>
{

}
