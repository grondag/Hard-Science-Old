package grondag.hard_science.simulator.transport.L2;

import grondag.hard_science.simulator.resource.StorageType;

public interface ITransportVector<T extends StorageType<T>>
{
    public ITransportNode<T> fromNode();
    public ITransportNode<T> toNode();
}
