package grondag.hard_science.simulator.device;

import javax.annotation.Nullable;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.world.Location;
import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.domain.DomainManager;
import grondag.hard_science.simulator.persistence.IIdentified;
import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractSimpleDevice implements IDevice
{
    private int id;
    private Location location;
    
    private int domainID = IIdentified.UNASSIGNED_ID;
    
    /** do not access directly - lazy lookup after deserialization */
    private Domain domain = null;
    
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
            this.domain = DomainManager.INSTANCE.getDomain(this.domainID);
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
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        this.serializeID(tag);
        Location.saveToNBT(location, tag);
        tag.setInteger(ModNBTTag.DOMAIN_ID, this.domainID);
    }
}
