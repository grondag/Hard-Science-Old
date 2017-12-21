package grondag.hard_science.simulator.transport;

import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.endpoint.TransportNode;

public class TransportHelper
{
    public static <T extends StorageType<T>> AbstractResourceWithQuantity<T> immediateExtract(
            AbstractResourceWithQuantity<T> cargo,
            TransportNode fromNode, 
            TransportNode toNode)
    {
        return null;
    }
}
