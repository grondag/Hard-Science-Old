package grondag.hard_science.simulator.machine;

import grondag.hard_science.library.world.Location;
import grondag.hard_science.simulator.base.DomainManager.Domain;
import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractMachine implements IMachine
{
    protected Location location;
    protected int id;
    protected MachineManager owner = null;
    
    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        this.deserializeID(tag);
        this.deserializeLocation(tag);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        this.serializeID(tag);
        this.serializeLocation(tag);
    }

    @Override
    public Location getLocation()
    {
        return this.location;
    }

    @Override
    public Domain getDomain()
    {
        return this.owner == null ? null : owner.getDomain();
    }

    @Override
    public int getId()
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
    public void setLocation(Location loc)
    {
        this.location = loc;
        this.setDirty();
    }

    public void setDirty()
    {
        if(this.owner != null) this.owner.setDirty();
    }

}
