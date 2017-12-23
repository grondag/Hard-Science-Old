package grondag.hard_science.simulator.transport.management;

import java.util.Iterator;

import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.endpoint.TransportNode;
import grondag.hard_science.simulator.transport.routing.IItinerary;

/**
 * Transport manager for single carrier
 */
public class SimpleTransportManager<T extends StorageType<T>> implements ITransportManager<T>, Iterable<TransportNode>
{
    private final IDevice owner;
    
    private SimpleUnorderedArrayList<TransportNode> nodes
        = new SimpleUnorderedArrayList<TransportNode>();
    
    public SimpleTransportManager(IDevice owner)
    {
        this.owner = owner;
    }
    
    public IItinerary<T> send(IResource<T> resource, long quantity, IDevice recipient, boolean connectedOnly, boolean simulate)
    {
        if(this.nodes.isEmpty()) return null;
        return null;
    }

    @Override
    public IDevice device()
    {
        return this.owner;
    }

    @Override
    public void removeTransportNode(TransportNode node)
    {
        this.nodes.removeIfPresent(node);
    }

    @Override
    public void addTransportNode(TransportNode node)
    {
        this.nodes.addIfNotPresent(node);
    }

    @Override
    public Iterator<TransportNode> iterator()
    {
        return this.nodes.iterator();
    }

    @Override
    public boolean hasNodes()
    {
        return !this.nodes.isEmpty();
    }
}
