package grondag.hard_science.simulator.transport.L3;

import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.L2.ITransportNode;
import grondag.hard_science.simulator.transport.L2.ITransportVector;
import grondag.hard_science.simulator.transport.L2.TransportLink;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

public class Packet<T extends StorageType<T>> implements ITransportVector<T>
{
    public final IResource<T> resource;
    public final long quantity;
    private final ITransportNode<T> fromNode;
    private final ITransportNode<T> toNode;
    
    private final Object2LongOpenHashMap<TransportLink<T>> links
        = new Object2LongOpenHashMap<TransportLink<T>>();
    
    private final long totalCost = 0;
    
    public Packet(IResource<T> resource, long quantity, ITransportNode<T> fromNode, ITransportNode<T> toNode)
    {
        this.resource = resource;
        this.quantity = quantity;
        this.fromNode = fromNode;
        this.toNode = toNode;
    }
    
    /**
     * Standardized cost of transporting this packet. Typically based on 
     * size of the payload.  Used by lower transport layers to compute
     * cost of the transport on specific media.
     */
    public long basePacketCost()
    {
        return this.quantity;
    }
    
    /**
     * Updates the accumulated total cost for transporting this packet, 
     * by transport media.  Tracked for each link separately so that
     * can refund costs incurred at each link if the transport cannot
     * be completed. 
     */
    public synchronized void updateLinkCost(TransportLink<T> link, long costDelta)
    {
        this.links.addTo(link, costDelta);
    }
    
    /**
     * Refunds cost from given link, (does actually update the link)
     * and removes link from link cost list.
     */
    public synchronized void refundLinkCost(TransportLink<T> link)
    {
        long cost = this.links.removeLong(link);
        if(cost != 0) link.refund(cost);
    }
    
    /**
     * Refunds all costs charged by links for this packet. 
     * Does update each link.  Call when packet transport
     * must be aborted for any reason.
     */
    public synchronized void refundAllCosts()
    {
        if(!this.links.isEmpty())
        {
            for(Object2LongMap.Entry<TransportLink<T>> entry : this.links.object2LongEntrySet())
            {
                entry.getKey().refund(entry.getLongValue());
            }
            this.links.clear();
        }
    }
    
    /**
     * Accumulated cost of transporting this packet. Will not be final
     * until packet has reached destination.
     * @return
     */
    public long totalCost()
    {
        return this.totalCost;
    }

    @Override
    public ITransportNode<T> fromNode()
    {
        return this.fromNode;
    }
    
    @Override
    public ITransportNode<T> toNode()
    {
        return this.toNode;
    }
}
