package grondag.hard_science.machines;

import javax.annotation.Nullable;

import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.machines.support.BatteryChemistry;
import grondag.hard_science.machines.support.MachinePower;
import grondag.hard_science.machines.support.MachinePowerSupply;
import grondag.hard_science.machines.support.PowerReceiver;
import grondag.hard_science.simulator.storage.ContainerUsage;
import grondag.hard_science.simulator.storage.PowerContainer;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.endpoint.PortType;
import net.minecraft.nbt.NBTTagCompound;

public class ChemicalBatteryMachine extends AbstractSimpleMachine
{
    protected final PowerContainer powerStorage;
    
    public ChemicalBatteryMachine()
    {
        super(CarrierLevel.BOTTOM, PortType.CARRIER);
        this.powerStorage = new PowerContainer(this, ContainerUsage.STORAGE);
        this.powerStorage.configure(775193798450L, BatteryChemistry.SILICON);
    }

    @Override
    protected @Nullable MachinePowerSupply createPowerSuppy()
    {
        return new MachinePowerSupply(
                null, 
                this.powerStorage, 
                new PowerReceiver(MachinePower.POWER_BUS_JOULES_PER_TICK));
    }
    
    @Override
    public boolean hasOnOff()
    {
        return true;
    }

    @Override
    public boolean hasRedstoneControl()
    {
        return true;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        super.deserializeNBT(tag);
        this.powerStorage.deserializeNBT(tag);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        super.serializeNBT(tag);
        this.powerStorage.serializeNBT(tag);
    }

    @Override
    public void onConnect()
    {
        super.onConnect();
        this.powerStorage.onConnect();
    }

    @Override
    public void onDisconnect()
    {
        this.powerStorage.onDisconnect();
        super.onDisconnect();
    }
    
    @Override
    public PowerContainer powerStorage()
    {
        return this.powerStorage;
    }
}
