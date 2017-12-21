package grondag.hard_science.simulator.transport;

import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.carrier.TransportCarrier;
import grondag.hard_science.simulator.transport.endpoint.TransportNode;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

/**
 * Describes a resource in transit within a connected, wireless or drone network.
 * If resources must cross over network boundaries will require multiple packets.
 * @see IItinerary.
 */
public class StoragePacket<T extends StorageType<T>>
{
    public final IResource<T> resource;
    public final long quantity;
    
    /**
     * Device from which this packet originates. 
     */
    public final IDevice sender;
    
    /**
     * Destination device for this packet. 
     */
    public final IDevice recipient;
    
    private TransportNode fromNode;

    private TransportNode toNode;
    
    /**
     * Contains intermediate node-to-node vectors between
     * {@link #fromNode} and {@link #toNode}.  Will be
     * empty if there is a direct route.
     */
    private final Object2LongOpenHashMap<TransportCarrier<T>> links
        = new Object2LongOpenHashMap<TransportCarrier<T>>();
    
    /**
     * Total accumulated transport cost for this packet.
     */
    private long totalCost = 0;
    
    public StoragePacket(IResource<T> resource, long quantity, IDevice fromDevice, IDevice toDevice)
    {
        this.resource = resource;
        this.quantity = quantity;
        this.sender = fromDevice;
        this.recipient = toDevice;
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
    public synchronized void updateLinkCost(TransportCarrier<T> link, long costDelta)
    {
        this.links.addTo(link, costDelta);
    }
    
    /**
     * Refunds cost from given link, (does actually update the link)
     * and removes link from link cost list.
     */
    public synchronized void refundLink(TransportCarrier<T> link)
    {
        long cost = this.links.removeLong(link);
        if(cost != 0) link.refund(cost);
    }
    
    /**
     * Refunds all costs charged by links for this packet. 
     * Updates each link and then clears links in this packet.  
     * Call when packet transport must be aborted for any reason.
     */
    public synchronized void refundAllLinks()
    {
        if(!this.links.isEmpty())
        {
            for(Object2LongMap.Entry<TransportCarrier<T>> entry : this.links.object2LongEntrySet())
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

    /**
     * Node on the sender used to send the packet. Start of the route.
     */
    public TransportNode fromNode()
    {
        return fromNode;
    }

    public void setFromNode(TransportNode fromNode)
    {
        this.fromNode = fromNode;
    }

    /**
     * Node on the receiving device to which the packet will
     * be delivered.  End of the route.
     */
    public TransportNode toNode()
    {
        return toNode;
    }

    public void setToNode(TransportNode toNode)
    {
        this.toNode = toNode;
    }
  
}