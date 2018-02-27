package grondag.hard_science.simulator.storage;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.machines.energy.BatteryChemistry;
import grondag.hard_science.machines.energy.IEnergyComponent;
import grondag.hard_science.machines.support.ThroughputRegulator;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.resource.PowerResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypePower;
import net.minecraft.nbt.NBTTagCompound;

public class PowerContainer extends ResourceContainer<StorageTypePower> implements IEnergyComponent
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
        super.setCapacity(chemistry.capacityForNanoliters(volumeNanoliters));
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

    public BatteryChemistry getChemistry()
    {
        return chemistry;
    }

    @Override
    public long maxEnergyInputJoulesPerTick()
    {
        return this.getRegulator().maxInputPerTick();
    }
    
    @Override
    public long energyInputLastTickJoules()
    {
        return this.getRegulator().inputLastTick();
    }
    
    @Override
    public long maxEnergyOutputJoulesPerTick()
    {
        return this.getRegulator().maxOutputPerTick();
    }

    /**
     * Power storage components can only provide energy
     * to the device if this is an input or isolated buffer,
     * or if the caller is running on the power service thread.<p>
     * 
     * {@inheritDoc}
     */
    @Override
    public boolean canProvideEnergy()
    {
        return this.isThreadOK() && this.usedCapacity() > 0;
    }

    @Override
    public long provideEnergy(long maxOutput, boolean allowPartial, boolean simulate)
    {
        return this.takeUpTo(PowerResource.JOULES, maxOutput, simulate, allowPartial, null);
    }

    @Override
    public long energyOutputLastTickJoules()
    {
        return this.getRegulator().outputLastTick();
    }

    /**
     * Power storage components can only accept energy
     * from the device if this is an output or isolated buffer,
     * or if the caller is running on the power service thread.<p>
     * 
     * {@inheritDoc}
     */
    @Override
    public boolean canAcceptEnergy()
    {
        return this.confirmServiceThread() && this.availableCapacity() > 0;
    }

    @Override
    public long acceptEnergy(long maxInput, boolean allowPartial, boolean simulate)
    {
        return this.add(PowerResource.JOULES, maxInput, simulate, allowPartial, null);
    }
    
    @Override
    public long storedEnergyJoules()
    {
        return this.usedCapacity();
    }
    
    @Override
    public long maxStoredEnergyJoules()
    {
        return this.getCapacity();
    }
}
