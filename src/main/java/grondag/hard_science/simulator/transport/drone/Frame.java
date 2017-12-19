package grondag.hard_science.simulator.transport.drone;

import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.StoragePacket;
import grondag.hard_science.simulator.transport.endpoint.ITransportNode;

public class Frame<T extends StorageType<T>>
{
    public final ITransportNode<T> fromNode;
    public final ITransportNode<T> toNode;
    public final IFrameHandler frameHandler;
    public final StoragePacket<T> payload;
    
    public Frame(ITransportNode<T> fromNode, ITransportNode<T> toNode, IFrameHandler handler, StoragePacket<T> payload)
    {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.frameHandler = handler;
        this.payload = payload;
    }
}
