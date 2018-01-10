package grondag.hard_science.simulator.device;

import javax.annotation.Nullable;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.world.Location;
import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.device.blocks.IDeviceBlockManager;
import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.domain.DomainManager;
import grondag.hard_science.simulator.persistence.IIdentified;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import grondag.hard_science.simulator.resource.StorageType.StorageTypePower;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.transport.management.ITransportManager;
import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractDevice implements IDevice
{
    private int id;
    private Location location;
    
    /**
     * True if {@link #onConnect()} has run and {@link #onDisconnect()} has not.
     */
    private boolean isConnected = false;
    
    private int channel = 0;
    
    private int domainID = IIdentified.UNASSIGNED_ID;
    
    /** do not access directly - lazy lookup after deserialization */
    private Domain domain = null;
    
    /**
     * Initialized during just before super connect, set null after super disconnect
     */
    protected IDeviceBlockManager blockManager = null; 
    
    protected final ITransportManager<StorageTypeStack> itemTransportManager;
    protected final ITransportManager<StorageTypePower> powerTransportManager;
    
    protected AbstractDevice()
    {
        this.itemTransportManager = this.createItemTransportManager();
        this.powerTransportManager = this.createPowerTransportManager();
    }
    
    /**
     * Override to enable item transport
     */
    protected ITransportManager<StorageTypeStack> createItemTransportManager()
    {
        return null;
    }

    /**
     * Override to enable power transport
     */
    protected ITransportManager<StorageTypePower> createPowerTransportManager()
    {
        return null;
    }
    
    /**
     * Override to implement block manager functionality
     */
    protected IDeviceBlockManager createBlockManager()
    {
        return null;
    }
    
    @Override
    public final IDeviceBlockManager blockManager()
    {
        return this.blockManager;
    }
    
    @Override
    public ITransportManager<?> tranportManager(StorageType<?> storageType)
    {
        switch(storageType.enumType)
        {
        case ITEM:
            return this.itemTransportManager;
            
        case POWER:
            return this.powerTransportManager;
            
        default:
            return null;
        }
    }

    @Override
    public final boolean isConnected()
    {
        return this.isConnected;
    }
    
    @Override
    public int getIdRaw()
    {
        return this.id;
    }

    @Override
    public void setId(int id)
    {
        this.id = id;
    }

    @Override
    public Location getLocation()
    {
        return this.location;
    }

    @Override
    public void setLocation(Location loc)
    {
        this.location = loc;
    }

    @Nullable
    @Override
    public Domain getDomain()
    {
        if(this.domain == null)
        {
            if(this.domainID != IIdentified.UNASSIGNED_ID)
                this.domain = DomainManager.instance().getDomain(this.domainID);
        }
        return this.domain;
    }
    
    /**
     * MUST NOT BE CALLED WHILE CONNECTED.
     * Storages, connections, etc. all rely on domain remaining
     * static while connected to transport network. If domain
     * is to be changed, disconnect, make the change, and reconnect.
     */
    public void setDomain(@Nullable Domain domain)
    {
        if(this.isConnected) 
            throw new UnsupportedOperationException("Attempt to change device domain while connected.");
        
        this.domainID = domain == null ? IIdentified.UNASSIGNED_ID : domain.getId();
        this.domain = domain;
        this.setDirty();
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        this.deserializeID(tag);
        this.location = Location.fromNBT(tag);
        
        // would cause problems with devices that have already posted events to a domain
        assert this.domain == null
                : "Non-null domain during device deserialization.";
        
        this.domainID = tag.getInteger(ModNBTTag.DOMAIN_ID);
        this.channel = tag.getInteger(ModNBTTag.DEVICE_CHANNEL);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        this.serializeID(tag);
        Location.saveToNBT(location, tag);
        tag.setInteger(ModNBTTag.DOMAIN_ID, this.domainID);
        tag.setInteger(ModNBTTag.DEVICE_CHANNEL, this.channel);
    }
    
    @Override
    public void onConnect()
    {
        this.blockManager = this.createBlockManager();
        IDevice.super.onConnect();
        this.isConnected = true;
    }
    
    @Override
    public void onDisconnect()
    {
        IDevice.super.onDisconnect();
        this.blockManager = null;
        this.isConnected = false;
    }

    public int getChannel()
    {
        return channel;
    }

    public void setChannel(int channel)
    {
        this.channel = channel;
    }
    
    /**
     * Override if your device handles transport requests. 
     * Base implementation handles service check.<p>
     * 
     * See {@link #onProduce(IResource, long, boolean, boolean)}
     */
    @SuppressWarnings("unchecked")
    protected long onProduceImpl(IResource<?> resource, long quantity, boolean simulate, @Nullable IProcurementRequest<?> request)
    { 
        switch(resource.storageType().enumType)
        {
        case FLUID:
            if(this.hasFluidStorage()) 
                return this.fluidStorage().takeUpTo((IResource<StorageTypeFluid>)resource, quantity, simulate, (IProcurementRequest<StorageTypeFluid>)request);
        
        case ITEM:
            if(this.hasItemStorage()) 
                return this.itemStorage().takeUpTo((IResource<StorageTypeStack>)resource, quantity, simulate, (IProcurementRequest<StorageTypeStack>)request);

        case POWER:
            if(this.hasPowerStorage()) 
                return this.powerStorage().takeUpTo((IResource<StorageTypePower>)resource, quantity, simulate, (IProcurementRequest<StorageTypePower>)request);

        default:
            assert false : "Unhandled enum mapping";
        }
        return 0;
    }
    
    @Override
    public final long onProduce(IResource<?> resource, long quantity, boolean simulate, @Nullable IProcurementRequest<?> request)
    {
        assert resource.confirmServiceThread() 
            : "Transport logic running outside logistics service"; 
        return this.onProduceImpl(resource, quantity, simulate, request);
    }

    /**
     * Override if your device handles transport requests. 
     * Base implementation handles service check.<p>
     * 
     * See {@link #onConsume(IResource, long, boolean, boolean)}
     */
    @SuppressWarnings("unchecked")
    protected long onConsumeImpl(IResource<?> resource, long quantity, boolean simulate, @Nullable IProcurementRequest<?> request)
    {
        switch(resource.storageType().enumType)
        {
        case FLUID:
            if(this.hasFluidStorage()) 
                return this.fluidStorage().add((IResource<StorageTypeFluid>)resource, quantity, simulate, (IProcurementRequest<StorageTypeFluid>)request);
  
        case ITEM:
            if(this.hasItemStorage()) 
                return this.itemStorage().add((IResource<StorageTypeStack>)resource, quantity, simulate, (IProcurementRequest<StorageTypeStack>)request);

        case POWER:
            if(this.hasPowerStorage()) 
                return this.powerStorage().add((IResource<StorageTypePower>)resource, quantity, simulate, (IProcurementRequest<StorageTypePower>)request);

        default:
            assert false : "Unhandled enum mapping";
        }
        return 0;
    }
    
    @Override
    public final long onConsume(IResource<?> resource, long quantity, boolean simulate, @Nullable IProcurementRequest<?> request)
    {
        assert resource.confirmServiceThread() 
            : "Transport logic running outside logistics service"; 
        return this.onConsumeImpl(resource, quantity, simulate, request);
    }

}
