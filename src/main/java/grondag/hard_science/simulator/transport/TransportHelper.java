package grondag.hard_science.simulator.transport;

import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.endpoint.ITransportNode;

public class TransportHelper
{
    public static <T extends StorageType<T>> AbstractResourceWithQuantity<T> immediateExtract(
            AbstractResourceWithQuantity<T> cargo,
            ITransportNode<T> fromNode, 
            ITransportNode<T> toNode)
    {
        return null;
    }
}
