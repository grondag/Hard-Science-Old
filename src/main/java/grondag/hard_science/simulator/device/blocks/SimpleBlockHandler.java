package grondag.hard_science.simulator.device.blocks;

import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.library.world.PackedBlockPos;
import grondag.hard_science.simulator.device.DeviceManager;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.device.IDeviceComponent;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.carrier.CarrierCircuit;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.endpoint.CarrierPortGroup;
import grondag.hard_science.simulator.transport.endpoint.Port;
import grondag.hard_science.simulator.transport.endpoint.PortState;
import grondag.hard_science.simulator.transport.management.ConnectionManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

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

    private final CarrierPortGroup itemGroup;
    private final CarrierPortGroup powerGroup; 
    
    /**
     * Should not be called until device has a location.
     */
    public SimpleBlockHandler(IDevice owner)
    {
        this.owner = owner;
        this.collection = ImmutableList.of(this);
        this.packedBlockPos = PackedBlockPos.pack(owner.getLocation());
        this.itemGroup = new CarrierPortGroup(owner, StorageType.ITEM, CarrierLevel.BASE);
        this.powerGroup = new CarrierPortGroup(owner, StorageType.POWER, CarrierLevel.BASE);
        
        BlockPos pos = PackedBlockPos.unpack(this.packedBlockPos);
        
        for(EnumFacing face : EnumFacing.VALUES)
        {
            SimpleUnorderedArrayList<PortState> list = new SimpleUnorderedArrayList<PortState>();
            this.ports[face.ordinal()] = list;
            list.add(itemGroup.createPort(false, pos, face));
            //FIXME: put back
//            list.add(powerGroup.createPort(false, pos, face));
        }
    }

    @Override
    public Iterable<PortState> getPorts(EnumFacing face)
    {
        if(face == null) return Collections.emptyList();
        
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
            
            for(PortState myPort : this.ports[face.ordinal()])
            {
                assert !myPort.isAttached() : "Connection attempt on attached port";
                
                for(PortState mate : neighbor.getPorts(face.getOpposite()))
                {
                    if(ConnectionManager.isConnectionPossible(myPort, mate))
                        ConnectionManager.connect(myPort, mate);
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
            if(this.ports[face.ordinal()] == null) continue;

            IDeviceBlock neighbor = this.getNeighbor(face);
            if(neighbor == null) continue;
            
            for(PortState pi : this.ports[face.ordinal()])
            {
                if(pi.isAttached()) ConnectionManager.disconnect(pi);
            }
        }
    }
    
    @Override
    public CarrierCircuit itemCircuit()
    { 
        return this.itemGroup.internalCircuit(); 
    }
    
    @Override
    public CarrierCircuit powerCircuit()
    { 
        return this.powerGroup.internalCircuit(); 
    }
}
