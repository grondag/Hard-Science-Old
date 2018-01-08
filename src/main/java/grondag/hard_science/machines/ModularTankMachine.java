package grondag.hard_science.machines;

import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.simulator.storage.FluidStorage;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.endpoint.PortType;
import net.minecraft.nbt.NBTTagCompound;

public class ModularTankMachine extends AbstractSimpleMachine
{
    protected final FluidStorage fluidStorage;
    
    public ModularTankMachine()
    {
        super(CarrierLevel.BOTTOM, PortType.CARRIER);
        this.fluidStorage = new FluidStorage(this);
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
    public FluidStorage fluidStorage()
    {
        return this.fluidStorage;
    }
}
