package grondag.hard_science.machines;

import javax.annotation.Nullable;

import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.machines.support.BatteryChemistry;
import grondag.hard_science.machines.support.DeviceEnergyManager;
import grondag.hard_science.machines.support.VolumeUnits;
import grondag.hard_science.simulator.storage.ContainerUsage;
import grondag.hard_science.simulator.storage.PowerContainer;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.endpoint.PortType;

public class ChemicalBatteryMachine extends AbstractSimpleMachine
{
    
    public ChemicalBatteryMachine()
    {
        super(CarrierLevel.BOTTOM, PortType.CARRIER);
    }

    @Override
    protected @Nullable DeviceEnergyManager createEnergyManager()
    {
        PowerContainer battery = new PowerContainer(this, ContainerUsage.STORAGE);
        battery.configure(VolumeUnits.LITER.nL * 750L, BatteryChemistry.SILICON);
        
        return new DeviceEnergyManager(
                this,
                null,
                null, 
                battery);
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
    public void onConnect()
    {
        super.onConnect();
    }

    @Override
    public void onDisconnect()
    {
        super.onDisconnect();
    }
}
