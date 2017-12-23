package grondag.hard_science.simulator.device.blocks;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.library.world.PackedBlockPos;
import grondag.hard_science.simulator.device.DeviceManager;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.device.IDeviceComponent;
import grondag.hard_science.simulator.transport.endpoint.Port;
import grondag.hard_science.simulator.transport.endpoint.PortState;
import grondag.hard_science.simulator.transport.management.ConnectionHelper;
import net.minecraft.util.EnumFacing;

/**
 * Block/block manager implementation for single-block machines.
 */
public class SimpleBlockHandler implements IDeviceBlock, IDeviceBlockManager, IDeviceComponent
{
    private final IDevice owner;
    private final long packedBlockPos;

    private final Collection<IDeviceBlock> collection;

    @SuppressWarnings("unchecked")
    private final SimpleUnorderedArrayList<PortState>[] ports = new SimpleUnorderedArrayList[6];

    /**
     * Should not be called until device has a location.
     */
    public SimpleBlockHandler(IDevice owner)
    {
        this.owner = owner;
        this.collection = ImmutableList.of(this);
        this.packedBlockPos = PackedBlockPos.pack(owner.getLocation());
    }

    //FIXME: remove or repair
    /**
     * IMPORTANT: device should disconnect before calling and reconnect after.
     */
//    public void addPort(EnumFacing face, Port port, int channel)
//    {
//        assert !owner.isConnected() : "Device connector changed while connected.";
//        
//        PortState portInstance = new PortState(this.owner, port);
//        portInstance.setChannel(channel);
//        
//        SimpleUnorderedArrayList<PortState> list = this.ports[face.ordinal()];
//        if(list == null)
//        {
//            list = new SimpleUnorderedArrayList<PortState>();
//        }
//        list.add(portInstance);
//    }

    @Override
    public Iterable<PortState> getPorts(EnumFacing face)
    {
        return this.ports[face.ordinal()];
    }

    @Override
    public PortState getPort(Port port, EnumFacing face)
    {
        if(this.ports[face.ordinal()] == null) return null;
        
        for(PortState pi : this.ports[face.ordinal()])
        {
            if(pi.port() == port) return pi;
        }
        
        return null;
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
            if(this.ports[face.ordinal()] == null) continue;

            IDeviceBlock neighbor = this.getNeighbor(face);
            if(neighbor == null) continue;
            
            for(PortState pi : this.ports[face.ordinal()])
            {
                PortState mate = neighbor.getPort(pi.port(), face.getOpposite());
                
                if(mate != null) ConnectionHelper.connect(pi, mate);
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
            if(this.ports[face.ordinal()] == null) continue;

            IDeviceBlock neighbor = this.getNeighbor(face);
            if(neighbor == null) continue;
            
            for(PortState pi : this.ports[face.ordinal()])
            {
                if(pi.isAttached()) pi.detach();
            }
        }
    }
}
