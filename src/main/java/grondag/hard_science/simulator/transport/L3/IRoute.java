package grondag.hard_science.simulator.transport.L3;

import grondag.hard_science.simulator.resource.ITypedStorage;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.TransportMode;
import grondag.hard_science.simulator.transport.L2.ITransportVector;

public interface IRoute<T extends StorageType<T>> extends ITypedStorage<T>, ITransportVector<T>
{
    public int cost();
    public boolean isLocal();
    public TransportMode mode();
}
