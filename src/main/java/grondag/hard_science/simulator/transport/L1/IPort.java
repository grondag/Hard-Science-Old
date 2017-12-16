package grondag.hard_science.simulator.transport.L1;

import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.resource.ITypedStorage;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.L2.ITransportNode;

/**
 * Represents physical aspect of a transport node.
 */
public interface IPort<T extends StorageType<T>> extends ITypedStorage<T>
{
    public ITransportNode<T> node();
    
    public default IDevice device() { return this.node().device(); }
}
