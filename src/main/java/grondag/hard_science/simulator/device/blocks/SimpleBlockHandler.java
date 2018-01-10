package grondag.hard_science.simulator.device.blocks;

import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.Configurator;
import grondag.hard_science.Log;
import grondag.hard_science.init.ModPorts;
import grondag.hard_science.library.world.PackedBlockPos;
import grondag.hard_science.simulator.device.DeviceManager;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.device.IDeviceComponent;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.carrier.CarrierCircuit;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.endpoint.CarrierPortGroup;
import grondag.hard_science.simulator.transport.endpoint.DirectPortState;
import grondag.hard_science.simulator.transport.endpoint.Port;
import grondag.hard_science.simulator.transport.endpoint.PortState;
import grondag.hard_science.simulator.transport.endpoint.PortType;
import grondag.hard_science.simulator.transport.management.LogisticsService;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

/**
 * Basic combined device block & block manager implementation for single-block machines.
 */
public class SimpleBlockHandler implements IDeviceBlock, IDeviceBlockManager, IDeviceComponent
{
    private final IDevice owner;
    private final long packedBlockPos;

    private final Collection<IDeviceBlock> collection;

    /**
     * Item ports by face. This implementation assumes/allows
     * only one port per face.
     */
    private final PortState[] itemPorts;
    
    /**
     * Power ports by face. This implementation assumes/allows
     * only one port per face.
     */
    private final PortState[] powerPorts;

    /**
     * Manages shared circuit for item ports.  Not face-aware
     * because is general-purpose class that can handle wireless
     * and/or multiblock.
     */
    private final CarrierPortGroup itemGroup;

    /**
     * Manages shared circuit for power ports.  Not face-aware
     * because is general-purpose class that can handle wireless
     * and/or multiblock.
     */
private final CarrierPortGroup powerGroup; 
    
    /**
     * Sets up ports and parents.
     * Should not be called until device has a location.<p>
     * 
     * @param owner     Device associated with this device block
     * @param channel   Channel for circuit segregation. Ignored for top-level devices.
     * @param level     Transport level of the internal carrier for bridge/carrier port types.
     * Level of port itself for direct ports. 
     * @param portType  Type of port offered for each support storage type.  
     * Assumes all faces offer same ports. 
     */
    public SimpleBlockHandler(
            IDevice owner, 
            int channel, 
            CarrierLevel level, 
            PortType portType)
    {
        this.owner = owner;
        this.collection = ImmutableList.of(this);
        this.packedBlockPos = PackedBlockPos.pack(owner.getLocation());

        boolean hasItemPorts = owner.hasTransportManager(StorageType.ITEM);
        boolean hasPowerPorts = owner.hasTransportManager(StorageType.POWER);
        this.itemPorts = hasItemPorts ?  new PortState[6] : null;
        this.powerPorts = hasPowerPorts ?  new PortState[6] : null;
        
        BlockPos pos = PackedBlockPos.unpack(this.packedBlockPos);
        
        if(portType == PortType.DIRECT)
        {
            // create individual direct ports - no common internal circuit
            
            Port itemPort = hasItemPorts
                ? ModPorts.find( StorageType.ITEM, level, PortType.DIRECT) : null;
             
            Port powerPort = hasPowerPorts
                ? ModPorts.find( StorageType.POWER, level, PortType.DIRECT) : null;
            
            for(EnumFacing face : EnumFacing.VALUES)
            {
                if(hasItemPorts)
                    this.itemPorts[face.ordinal()]= new DirectPortState(itemPort, owner, pos, face);
                
                if(hasPowerPorts)
                    this.powerPorts[face.ordinal()]= new DirectPortState(powerPort, owner, pos, face);
            }
            this.itemGroup = null;
            this.powerGroup = null;
        }
        else
        {
            // bridge or carrier ports - all share common internal circuit
            
            CarrierPortGroup itemGroup = null;
            CarrierPortGroup powerGroup = null;
            if(hasItemPorts)
            {
                itemGroup = new CarrierPortGroup(owner, StorageType.ITEM, level);
                if(!level.isTop()) itemGroup.setConfiguredChannel(channel);
            }
            if(hasPowerPorts)
            {
                powerGroup = new CarrierPortGroup(owner, StorageType.POWER, level);
                if(!level.isTop()) powerGroup.setConfiguredChannel(channel);
            }
            
            for(EnumFacing face : EnumFacing.VALUES)
            {
                if(itemGroup != null)
                    this.itemPorts[face.ordinal()] = itemGroup.createPort(portType == PortType.BRIDGE, pos, face);

                if(powerGroup != null)
                    this.powerPorts[face.ordinal()] = powerGroup.createPort(portType == PortType.BRIDGE, pos, face);
            }
            
            this.itemGroup = itemGroup;
            this.powerGroup = powerGroup;
        }
        
        
      
        

    }

    @Override
    public Iterable<PortState> getPorts(StorageType<?> storageType, EnumFacing face)
    {
        if(face == null) return Collections.emptyList();
        
        switch(storageType.enumType)
        {
        case ITEM:
            return portListForFaceFromArray(face, itemPorts);
        
        case POWER:
            return portListForFaceFromArray(face, powerPorts);

        case FLUID:
        default:
            return Collections.emptyList();
        }
    }
    
    private Iterable<PortState> portListForFaceFromArray(EnumFacing face, PortState[] ports)
    {
        if(ports == null) return Collections.emptyList();
        PortState result = ports[face.ordinal()];
        return result == null 
                ? Collections.emptyList() 
                : ImmutableList.of(result);
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
        if(Configurator.logDeviceChanges)
            Log.info("SimpleBlockHandler.connect: " + this.description());

        DeviceManager.blockManager().addOrUpdateDelegate(this);
        this.connectToNeighbors();
    }

    protected void connectToNeighbors()
    {
        if(Configurator.logDeviceChanges)
            Log.info("SimpleBlockHandler.connectToNeighbors: " + this.description());

        boolean hasItems = this.itemPorts != null;
        boolean hasPower = this.powerPorts != null;
        
        if(!(hasItems || hasPower)) return;
        
        for(EnumFacing face : EnumFacing.VALUES)
        {
            IDeviceBlock neighbor = this.getNeighbor(face);
            if(neighbor == null) continue;

            EnumFacing oppositeFace = face.getOpposite();
            
            if(hasItems)
            {
                PortState myPort = this.itemPorts[face.ordinal()];
                if(myPort == null) break;
                
                assert !myPort.isAttached() : "Connection attempt on attached port";
                for(PortState mate : neighbor.getPorts(StorageType.ITEM, oppositeFace))
                {
                    LogisticsService.ITEM_SERVICE.connect(myPort, mate);
                }
            }
            
            if(hasPower)
            {
                PortState myPort = this.powerPorts[face.ordinal()];
                if(myPort == null) break;
                
                assert !myPort.isAttached() : "Connection attempt on attached port";
                for(PortState mate : neighbor.getPorts(StorageType.POWER, oppositeFace))
                {
                    LogisticsService.POWER_SERVICE.connect(myPort, mate);
                }
            }
        }
    }

    @Override
    public void disconnect()
    {
        if(Configurator.logDeviceChanges)
            Log.info("SimpleBlockHandler.disconnect: " + this.description());
        
        // NB: will call back to onRemoval(), which contains logic 
        // for breaking connections
        DeviceManager.blockManager().removeDelegate(this);
    }

    @Override
    public void onRemoval()
    {
        if(Configurator.logDeviceChanges)
            Log.info("SimpleBlockHandler.onRemoval: " + this.description());

        boolean hasItems = this.itemPorts != null;
        boolean hasPower = this.powerPorts != null;
        
        if(!(hasItems || hasPower)) return;
        
        for(EnumFacing face : EnumFacing.VALUES)
        {
            IDeviceBlock neighbor = this.getNeighbor(face);
            if(neighbor == null) continue;
            
            if(hasItems)
            {
                PortState ps = this.itemPorts[face.ordinal()];
                if(ps.isAttached()) LogisticsService.ITEM_SERVICE.disconnect(ps);
            }
            
            if(hasPower)
            {
                PortState ps = this.powerPorts[face.ordinal()];
                if(ps.isAttached()) LogisticsService.POWER_SERVICE.disconnect(ps);
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
