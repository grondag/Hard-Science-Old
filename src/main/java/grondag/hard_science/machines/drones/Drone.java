package grondag.hard_science.machines.drones;

import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.world.Location;
import grondag.hard_science.machines.base.IMachine;
import grondag.hard_science.simulator.base.AssignedNumber;
import grondag.hard_science.simulator.base.DomainManager;
import grondag.hard_science.simulator.base.DomainManager.Domain;
import grondag.hard_science.simulator.base.IIdentified;
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
        return AssignedNumber.MACHINE;
    }

    @Override
    public Domain getDomain()
    {
        if(this.domain == null && this.domainID != IIdentified.UNASSIGNED_ID)
        {
            this.domain = DomainManager.INSTANCE.getDomain(this.domainID);
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


    private void setDirty()
    {
        // TODO Auto-generated method stub
        
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
