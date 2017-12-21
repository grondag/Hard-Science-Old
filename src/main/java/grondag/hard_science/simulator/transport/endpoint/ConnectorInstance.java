package grondag.hard_science.simulator.transport.endpoint;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.library.world.PackedBlockPos;
import grondag.hard_science.simulator.device.DeviceManager;
import grondag.hard_science.simulator.device.IDeviceComponent;
import grondag.hard_science.simulator.device.blocks.IDeviceBlock;
import net.minecraft.util.EnumFacing;

/**
 * 
 * Instance of a connector/connection on a device block.
 * Serialization only captures the connector state,
 * not the connection state, because connections are 
 * always dynamically determined at run time.<p>
 * 
 * Serialization does include configured port instances.
 */
public abstract class ConnectorInstance implements IDeviceComponent
{
    private final Connector connector;
    private Connection connection;
    private List<TransportNode> nodes = Collections.emptyList();
    private byte channel = 0;
    
    protected ConnectorInstance(Connector connector)
    {
        this.connector = connector;
    }
    
    public Connector connector()
    {
        return connector;
    }

    /**
     * Null if no mate available, disabled,
     * or not initialized/disconnected from world.
     */
    public Connection getConnection()
    {
        return connection;
    }
    
    /**
     * Called by DeviceWorldManager to form or remove
     * connections with other devices.<p>
     * 
     * If connection is different from current, initializes nodes 
     * and signals to owner via {@link #beforeNodeDestruction()},
     * as applicable.
     */
    public void setConnection(@Nullable Connection connection)
    {
        if(this.connection == connection) return;
        
        // signal to owner that nodes about to be discarded
        if(!this.nodes.isEmpty())
        {
            this.beforeNodeDestruction();
            for(TransportNode node : this.nodes)
            {
                node.disconnect();
            }
        }
        
        this.connection = connection;
        if(connection == null)
        {
            this.nodes = Collections.emptyList();
        }
        else
        {
            ImmutableList.Builder<TransportNode> builder = ImmutableList.builder();
            
            for(Port port : connection.ports)
            {
                builder.add(this.createNode(port));
            }
            
            this.nodes = builder.build();
        }
        
        // signal to owner that we have new nodes
        if(!this.nodes.isEmpty()) this.afterNodeCreation();

    }
    
    /**
     * Called before any existing nodes are discarded as a result
     * of {@link #setConnection(Connection)} or {@link #setChannel(int)}.
     */
    protected abstract void beforeNodeDestruction();
    
    /**
     * Called after any nodes are created as a result
     * of {@link #setConnection(Connection)} or {@link #setChannel(int)}.
     */
    protected abstract void afterNodeCreation();
    
    
    public List<TransportNode> nodes()
    {
        return this.nodes;
    }
    
    /**
     * Called to create new nodes when connection is established via {@link #setConnection(Connection)}
     */
    protected abstract TransportNode createNode(Port port);

    public int getChannel()
    {
        return channel;
    }

    /**
     * Values limited 0-15
     */
    public void setChannel(int channel)
    {
        this.channel = (byte) (channel | 0xF);
    }
    
//    /**
//     * Null for wireless/drones.
//     */
//    @Nullable
//    public abstract EnumFacing face();
    
    @Nonnull
    public abstract IDeviceBlock block();
    
    @Nullable
    public ConnectorInstance getNeighbor(EnumFacing face)
    {
        return DeviceManager.blockManager().getConnector(
                this.block().dimensionID(), 
                PackedBlockPos.offset(this.block().packedBlockPos(), face),
                face.getOpposite());
    }
}
