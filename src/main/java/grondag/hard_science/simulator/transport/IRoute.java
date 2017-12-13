package grondag.hard_science.simulator.transport;

import grondag.hard_science.simulator.domain.IDomainMember;
import grondag.hard_science.simulator.resource.ITypedStorage;
import grondag.hard_science.simulator.resource.StorageType;

public interface IRoute<T extends StorageType<T>> extends ITypedStorage<T>, IDomainMember
{
    public ITransportNode<T> fromNode();
    public ITransportNode<T> toNode();
    public int cost();
    public boolean isLocal();
    public TransportMode mode();
}
