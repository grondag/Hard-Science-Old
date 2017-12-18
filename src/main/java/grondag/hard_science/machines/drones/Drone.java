package grondag.hard_science.machines.drones;

import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.world.Location;
import grondag.hard_science.machines.base.IMachine;
import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.domain.DomainManager;
import grondag.hard_science.simulator.persistence.AssignedNumber;
import grondag.hard_science.simulator.persistence.IIdentified;
import net.minecraft.nbt.NBTTagCompound;

public class Drone implements IMachine, IReadWriteNBT
{
    private int id = IIdentified.UNASSIGNED_ID;
    
    private Location location;
    
    private int domainID = IIdentified.UNASSIGNED_ID;
    
    /** don't reference directly */
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
        this.setDirty();
    }

    @Override
    public AssignedNumber idType()
    {
        return AssignedNumber.DEVICE;
    }

    @Override
    public Domain getDomain()
    {
        if(this.domain == null && this.domainID != IIdentified.UNASSIGNED_ID)
        {
            this.domain = DomainManager.instance().getDomain(this.domainID);
        }
        return this.domain;
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
        this.setDirty();
    }


    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        this.deserializeID(tag);
        this.deserializeLocation(tag);
        this.domainID = tag.getInteger(ModNBTTag.DOMAIN_ID);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        this.serializeID(tag);
        this.serializeLocation(tag);
        tag.setInteger(ModNBTTag.DOMAIN_ID, this.domainID);
    }
}
