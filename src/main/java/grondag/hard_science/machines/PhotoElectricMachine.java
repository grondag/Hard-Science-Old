package grondag.hard_science.machines;

import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.machines.support.BatteryChemistry;
import grondag.hard_science.machines.support.DeviceEnergyManager;
import grondag.hard_science.machines.support.PhotoElectricCell;
import grondag.hard_science.machines.support.VolumeUnits;
import grondag.hard_science.simulator.storage.ContainerUsage;
import grondag.hard_science.simulator.storage.PowerContainer;

public class PhotoElectricMachine extends AbstractSimpleMachine
{
    public PhotoElectricMachine()
    {
        super();
    }
    
    @Override
    protected DeviceEnergyManager createEnergyManager()
    {
        // Want to use a capacitor so that we don't have energy
        // loss of a battery if the energy can be used immediately.
        PowerContainer output = new PowerContainer(this, ContainerUsage.BUFFER_OUT);
        output.configure(VolumeUnits.LITER.nL, BatteryChemistry.CAPACITOR);
        
        return new DeviceEnergyManager(
                this,
                new PhotoElectricCell(this), 
                null,
                output);
    }
}
