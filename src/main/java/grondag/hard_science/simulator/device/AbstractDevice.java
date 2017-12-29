package grondag.hard_science.simulator.device;

import javax.annotation.Nullable;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.world.Location;
import grondag.hard_science.simulator.device.blocks.IDeviceBlockManager;
import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.domain.DomainManager;
import grondag.hard_science.simulator.persistence.IIdentified;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypePower;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.transport.management.ITransportManager;
import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractDevice implements IDevice
{
    private int id;
    private Location location;
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
        if(this.domain == null && this.domainID != IIdentified.UNASSIGNED_ID)
        {
            this.domain = DomainManager.instance().getDomain(this.domainID);
        }
        return this.domain;
    }
    
    public void setDomain(@Nullable Domain domain)
    {
        this.domainID = domain == null ? IIdentified.UNASSIGNED_ID : domain.getId();
        this.domain = domain;
        this.setDirty();
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        this.deserializeID(tag);
        this.location = Location.fromNBT(tag);
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
}
