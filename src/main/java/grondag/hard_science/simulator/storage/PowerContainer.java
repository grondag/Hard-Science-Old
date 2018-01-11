package grondag.hard_science.simulator.storage;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.machines.support.BatteryChemistry;
import grondag.hard_science.machines.support.ThroughputRegulator;
import grondag.hard_science.machines.support.TimeUnits;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.resource.PowerResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypePower;
import net.minecraft.nbt.NBTTagCompound;

public class PowerContainer extends ResourceContainer<StorageTypePower>
{
    private BatteryChemistry chemistry;
    
    public PowerContainer(IDevice owner, ContainerUsage usage)
    {
        super(new PowerInner(owner, usage));
    }
    
    private static class PowerInner extends AbstractSingleResourceContainer<StorageTypePower>
    {
        public PowerInner(IDevice owner, ContainerUsage usage)
        {
            super(owner, usage);
            this.setFixedResource(PowerResource.JOULES);
        }

        @Override
        public StorageTypePower storageType()
        {
            return StorageType.POWER;
        }
    }
    
    public void configure(long volumeNanoliters, BatteryChemistry chemistry)
    {
        this.setCapacity(chemistry.capacityForNanoliters(volumeNanoliters));
        this.chemistry = chemistry;
        this.configureRegulator();
    }
    
    private void configureRegulator()
    {
        this.setRegulator( new ThroughputRegulator.Limited(
                chemistry.maxChargeJoulesPerTick(this.getCapacity()),
                chemistry.maxDischargeJoulesPerTick(this.getCapacity())));
    }

    @Override
    public void setCapacity(long capacity)
    {
        throw new UnsupportedOperationException("Attempt to set power storage capacity. Power capacity is determined by battery subsystem.");
    }
    
    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        //NB: super saves capacity, contents
        super.serializeNBT(tag);
        tag.setInteger(ModNBTTag.MACHINE_BATTERY_CHEMISTRY, this.chemistry.ordinal());
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        //NB: super saves capacity, contents
        super.deserializeNBT(tag);
        this.chemistry = Useful.safeEnumFromOrdinal(tag.getInteger(ModNBTTag.MACHINE_BATTERY_CHEMISTRY), BatteryChemistry.SILICON);
        this.configureRegulator();
    }

    /** for Battery wrapper */
    public long maxEnergyInputJoulesPerTick()
    {
        return this.getRegulator().maxInputPerTick();
    }

    /** for Battery wrapper */
    public float powerInputWatts()
    {
        return this.getRegulator().inputLastTick() * TimeUnits.TICKS_PER_SIMULATED_SECOND;
    }
    
    /** for Battery wrapper */
    public long energyInputCurrentTickJoules()
    {
        return this.getRegulator().inputLastTick();
    }
    
    /** for Battery wrapper */
    public long maxEnergyOutputJoulesPerTick()
    {
        return this.getRegulator().maxOutputPerTick();
    }
    
    public BatteryChemistry getChemistry()
    {
        return chemistry;
    }
}
