package grondag.hard_science.machines;

import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.simulator.storage.ContainerUsage;
import grondag.hard_science.simulator.storage.FluidContainer;
import net.minecraft.nbt.NBTTagCompound;

public class ModularTankMachine extends AbstractSimpleMachine
{
    protected final FluidContainer fluidStorage;
    
    public ModularTankMachine()
    {
        super();
        this.fluidStorage = new FluidContainer(this, ContainerUsage.STORAGE);
    }
    
    @Override
    public boolean hasOnOff()
    {
        return true;
    }

    @Override
    public boolean hasRedstoneControl()
    {
        return false;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        super.deserializeNBT(tag);
        this.fluidStorage.deserializeNBT(tag);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        super.serializeNBT(tag);
        this.fluidStorage.serializeNBT(tag);
    }

    @Override
    public void onConnect()
    {
        super.onConnect();
        this.fluidStorage.onConnect();
    }

    @Override
    public void onDisconnect()
    {
        this.fluidStorage.onDisconnect();
        super.onDisconnect();
    }
    
    @Override
    public FluidContainer fluidStorage()
    {
        return this.fluidStorage;
    }
}
