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

/**
 * Will need to split this implementation when introducing
 * non-chemical energy storage.
 */
public class PowerStorage extends AbstractResourceStorage<StorageTypePower, AbstractSingleResourceContainer<StorageTypePower>>
{
    private BatteryChemistry chemistry;

    public PowerStorage(IDevice owner)
    {
        super(owner);
    }

    @Override
    protected AbstractSingleResourceContainer<StorageTypePower> createContainer(IDevice owner)
    {
        AbstractSingleResourceContainer<StorageTypePower> result = new AbstractSingleResourceContainer<StorageTypePower>(owner)
        {
            @Override
            public StorageTypePower storageType() { return StorageType.POWER; }
        };
        result.setFixedResource(PowerResource.JOULES);
        return result;
    }
    
    public void configure(long volumeNanoliters, BatteryChemistry chemistry)
    {
        this.setCapacity(chemistry.capacityForNanoliters(volumeNanoliters));
        this.chemistry = chemistry;
        this.wrappedContainer.regulator = new ThroughputRegulator.Limited(
                chemistry.maxChargeJoulesPerTick(this.wrappedContainer.capacity),
                chemistry.maxDischargeJoulesPerTick(this.wrappedContainer.capacity));
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
        this.configure(
                this.wrappedContainer.capacity,
                Useful.safeEnumFromOrdinal(tag.getInteger(ModNBTTag.MACHINE_BATTERY_CHEMISTRY), BatteryChemistry.SILICON));
    }

    /** for Battery wrapper */
    public long maxEnergyInputJoulesPerTick()
    {
        return this.wrappedContainer.regulator.maxInputPerTick();
    }

    /** for Battery wrapper */
    public float powerInputWatts()
    {
        return this.wrappedContainer.regulator.inputLastTick() * TimeUnits.TICKS_PER_SIMULATED_SECOND;
    }
    
    /** for Battery wrapper */
    public long energyInputCurrentTickJoules()
    {
        return this.wrappedContainer.regulator.inputLastTick();
    }
    
    /** for Battery wrapper */
    public long maxEnergyOutputJoulesPerTick()
    {
        return this.wrappedContainer.regulator.maxOutputPerTick();
    }
    
    public BatteryChemistry getChemistry()
    {
        return chemistry;
    }
}