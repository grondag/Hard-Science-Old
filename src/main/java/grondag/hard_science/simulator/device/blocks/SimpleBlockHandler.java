package grondag.hard_science.simulator.device.blocks;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.init.ModConnections;
import grondag.hard_science.library.world.PackedBlockPos;
import grondag.hard_science.simulator.device.DeviceManager;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.device.IDeviceComponent;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.transport.endpoint.Connection;
import grondag.hard_science.simulator.transport.endpoint.Connector;
import grondag.hard_science.simulator.transport.endpoint.ConnectorInstance;
import grondag.hard_science.simulator.transport.endpoint.Port;
import grondag.hard_science.simulator.transport.endpoint.TransportNode;
import net.minecraft.util.EnumFacing;

/**
 * Block/block manager implementation for single-block machines.
 */
public abstract class SimpleBlockHandler implements IDeviceBlock, IDeviceBlockManager, IDeviceComponent
{
    private final IDevice owner;
    private final long packedBlockPos;

    private final Collection<IDeviceBlock> collection;

    private final ConnectorImpl[] connectors = new ConnectorImpl[6];

    /**
     * Should not be called until device has a location.
     */
    public SimpleBlockHandler(IDevice owner)
    {
        this.owner = owner;
        this.collection = ImmutableList.of(this);
        this.packedBlockPos = PackedBlockPos.pack(owner.getLocation());
    }

    /**
     * IMPORTANT: device should disconnect before calling and reconnect after.
     */
    public void setConnector(EnumFacing face, Connector connector)
    {
        assert !owner.isConnected() : "Device connector changed while connected.";
        this.connectors[face.ordinal()] = new ConnectorImpl(connector);
    }

    @Override
    public ConnectorInstance getConnector(EnumFacing face)
    {
        return this.connectors[face.ordinal()];
    }

    @Override
    public Collection<IDeviceBlock> blocks()
    {
        return this.collection;
    }

    @Override
    public long packedBlockPos()
    {
        return this.packedBlockPos;
    }

    @Override
    public int dimensionID()
    {
        return this.owner.getLocation().dimensionID();
    }

    @Override
    public IDevice device()
    {
        return this.owner;
    }

    @Override
    public void connect()
    {
        DeviceManager.blockManager().addOrUpdateDelegate(this);
        this.connectToNeighbors();
    }

    protected void connectToNeighbors()
    {
        for(EnumFacing face : EnumFacing.VALUES)
        {
            ConnectorImpl connector = this.connectors[face.ordinal()];
            if(connector != null)
            {
                ConnectorInstance neighbor = connector.getNeighbor(face);
                
                if(neighbor != null)
                {
                    Connection connection = 
                            ModConnections.getConnectionPairing(connector.connector(), neighbor.connector());
    
                    if(connection != null)
                    {
                        connector.setConnection(connection);
                        neighbor.setConnection(connection);
                    }
                }
            }
        }
    }

    @Override
    public void disconnect()
    {
        // NB: will call back to onRemoval(), which contains logic 
        // for breaking connections
        DeviceManager.blockManager().removeDelegate(this);
    }

    @Override
    public void onRemoval()
    {
        for(EnumFacing face : EnumFacing.VALUES)
        {
            ConnectorImpl connector = this.connectors[face.ordinal()];
            if(connector != null && connector.getConnection() != null)
            {
                ConnectorInstance neighbor = connector.getNeighbor(face);
                
                if(neighbor != null)
                {
                    neighbor.setConnection(null);
                }
                
                connector.setConnection(null);
            }
        }
    }

    private class NodeImpl extends TransportNode
    {
        protected NodeImpl(Port port)
        {
            super(port);
        }

        @Override
        public IDevice device()
        {
            return owner;
        }

        @Override
        public long produce(IResource<?> resource, long quantity, boolean allowPartial, boolean simulate)
        {
            return handleTransportProduce(resource, quantity, allowPartial, simulate);
        }

        @Override
        public long consume(IResource<?> resource, long quantity, boolean allowPartial, boolean simulate)
        {
            return handleTransportConsume(resource, quantity, allowPartial, simulate);
        }

    }

    private class ConnectorImpl extends ConnectorInstance
    {
        protected ConnectorImpl(Connector connector)
        {
            super(connector);
        }

        @Override
        public IDevice device()
        {
            return owner;
        }

        @Override
        protected TransportNode createNode(Port port)
        {
            return new NodeImpl(port);
        }

        @Override
        protected void beforeNodeDestruction()
        {
            SimpleBlockHandler.this.beforeNodeDestruction(this.nodes());
        }

        @Override
        protected void afterNodeCreation()
        {
            SimpleBlockHandler.this.afterNodeCreation(this.nodes());
        }

        @Override
        public IDeviceBlock block()
        {
            return SimpleBlockHandler.this;
        }
    }

    /**
     * See {@link ConnectorInstance#beforeNodeDestruction()}
     */
    protected abstract void beforeNodeDestruction(List<TransportNode> nodes);

    /**
     * See {@link ConnectorInstance#afterNodeCreation()}
     */
    protected abstract void afterNodeCreation(List<TransportNode> nodes);

    /**
     * Device-level handler for {@link TransportNode#produce(IResource, long, boolean, boolean)}
     */
    protected abstract long handleTransportProduce(IResource<?> resource, long quantity, boolean allowPartial, boolean simulate);

    /**
     * Device-level handler for {@link TransportNode#consume(IResource, long, boolean, boolean)}
     */
    protected abstract long handleTransportConsume(IResource<?> resource, long quantity, boolean allowPartial, boolean simulate);

}
