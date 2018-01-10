package grondag.hard_science.machines.support;

import grondag.hard_science.machines.base.AbstractMachine;
import grondag.hard_science.simulator.resource.PowerResource;
import grondag.hard_science.simulator.storage.PowerStorage;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Battery is implemented as a wrapper around the machine's power storage.
 * This is done so that power storage notification events will be
 * fired when we use the battery internally.
 */
public class Battery extends AbstractPowerComponent
{
    /**
     * Only populated on server. Null on client.
     */
    private final PowerStorage powerStorage;
    
    /**
     * Only populated on client
     */
    private float powerInputWatts;
    
    /**
     * Only populated on client
     */
    private long maxStoredEnergyJoules;
    
    /**
     * Only populated on client
     */
    private long storedEnergyJoules;
    
    public Battery()
    {
        this.powerStorage = null;
    }
    
    public Battery(PowerStorage powerStorage)
    {
        this.powerStorage = powerStorage;
        this.setMaxOutputJoulesPerTick(this.powerStorage.maxEnergyOutputJoulesPerTick());
    }

    @Override
    public long acceptEnergy(long maxInput, boolean allowPartial, boolean simulate)
    {        
        return this.powerStorage.add(PowerResource.JOULES, maxInput, simulate, allowPartial, null);
    }

    @Override
    public long storedEnergyJoules()
    {
        // on server comes direct from storage
        // on client is sent through packet
        return this.powerStorage == null 
                ? this.storedEnergyJoules 
                : this.powerStorage.usedCapacity();
    }

    @Override
    public long maxStoredEnergyJoules()
    {
        // on server comes direct from storage
        return this.powerStorage == null
                ? this.maxStoredEnergyJoules
                : this.powerStorage.getCapacity();
    }
    
    @Override
    public boolean canAcceptEnergy()
    {
        return this.powerStorage.availableCapacity() > 0;
    }
    
    @Override
    public EnergyComponentType componentType()
    {
        return EnergyComponentType.STORAGE;
    }

    @Override
    public long maxEnergyInputJoulesPerTick()
    {
        return this.powerStorage.maxEnergyInputJoulesPerTick();
    }

    @Override
    public float powerInputWatts()
    {
        // on server comes direct from storage
        // on client is sent through packet
        return this.powerStorage == null
                ? powerInputWatts
                : this.powerStorage.powerInputWatts();
    }

    @Override
    protected long provideEnergyImplementation(AbstractMachine mte, long maxOutput, boolean allowPartial, boolean simulate)
    {
        // note that update tracking, and check against per-tick max has already been done by caller
        return this.powerStorage.takeUpTo(PowerResource.JOULES, maxOutput, simulate, allowPartial, null);
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        //NOOP - handled by power storage serialization
        
        // refresh in case it changed during deserialize
        // implies storage deserialize should come before this
        this.setMaxOutputJoulesPerTick(this.powerStorage.maxEnergyOutputJoulesPerTick());
    }
    
    public void serializeNBT(NBTTagCompound tag)
    {
        //NOOP - handled by power storage serialization
    }
}