package grondag.hard_science.machines.support;

import grondag.hard_science.Log;
import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.machines.base.AbstractMachine;
import grondag.hard_science.simulator.storage.PowerContainer;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Power supplies have components that occur in the following 
 * combinations:<p>
 * <li>Battery - power storage devices</li>
 * <li>Input Buffer - power consuming devices</li>
 * <li>Generator, Output Buffer - power producing devices</li>
 * <li>Input Buffer, Generator, Output Buffer - power consuming devices
 * that contain an integrated generator</li><p>
 * 
 * Input buffer - necessary if this device consumes
 * power. Device draws power from the buffer during
 * the device tick.  Power is replenished from the
 * local output buffer (if there is one) or from other
 * output buffers or batteries on the power network if
 * the device is connected and local output is unavailable.<p>
 * 
 * Generator - a fuel cell, PE cell, or other component
 * within the device that can generate energy. If present,
 * power supply must have an output buffer to accept the
 * generated energy during the device tick.<p>
 * 
 * Output buffer - necessary if this device houses any
 * kind of generator.  Accepts generated energy during the
 * device tick. Tasks on the power service thread will then
 * redistribute the energy as needed, either locally within
 * the device (if it has an input buffer) or within the power 
 * storage network if the device is connected and demand exists.<p>
 * 
 * Battery - Will only be present if this device <em>is</em> a battery
 * and that is the only function it serves. In that case, the battery
 * will be the only power component in the device/power supply.
 * All transfers in and out will happen on the power service thread.
 * Any device that consumes or generates power will use input and/or
 * output buffer(s) instead of a battery so that device operations 
 * can complete during device tick without waiting for the service thread.<p>
 * 
 * GENERAL NOTES<p>
 * 
 * All stored energy is represented in Joules (aka Watt-seconds).<br>
 * All power input/output represents as Watts.<p>
 * 
 * Why not use IEnergyStorage or similar?<br>
 * 1) Explicitly want to use standard power/energy units. (watts/joules) <br>
 * 2) Want to support larger quanities (long vs int) <br>
 * 3) Want ability to reject partial input/output
 */
public class DeviceEnergyManager implements IReadWriteNBT
{
    private FuelCell fuelCell;
    private PowerContainer battery;
    
    /**
     * Set to true if this provider's (low) level has recently caused a 
     * processing failure.  Sent to clients for rendering to inform player
     * but not saved to world because is transient information.
     */
    private boolean isFailureCause;
    
    private final float maxPowerOutputWatts;
    private final long maxEnergyOutputPerTick;
    
    private float powerOutputWatts;

    public DeviceEnergyManager(FuelCell fuelCell, PowerContainer battery)
    {
        super();
        this.fuelCell = fuelCell;
        this.battery = battery;
        
        this.maxPowerOutputWatts = 
                  (fuelCell == null ? 0 : fuelCell.maxPowerOutputWatts())
                + (battery == null ? 0 : battery.maxPowerOutputWatts());      
        
        this.maxEnergyOutputPerTick = 
                (fuelCell == null ? 0 : fuelCell.maxEnergyOutputJoulesPerTick())
              + (battery == null ? 0 : battery.maxEnergyOutputJoulesPerTick());  
    }
 
    public FuelCell fuelCell() { return this.fuelCell; }
    public PowerContainer battery() { return this.battery; };
 
    /**
     * True if provider is actually able to provide power right now.
     * If false, any attempt to extract power will receive a zero result.
     */
    public boolean canProvideEnergy(AbstractMachine machine)
    {
        return      (this.fuelCell != null && this.fuelCell.canProvideEnergy(machine))
                ||  (this.battery != null && this.battery.canProvideEnergy(machine));
    }
    
    /**
     * Recent energy consumption level. In watts. Does not include power used to charge the battery.
     */
    public float powerOutputWatts()
    {
        return this.powerOutputWatts;
    }

    /**
     * 
     * TODO: Add an optional per-machine limit so that display make sense?
     * For example, why show max at 2000W if machine can only use 20W?
     * 
     * Highest possible continuous rate of power draw from this power
     * supply for use within the local device.  Peak rate from the
     * input buffer could be somewhat higher.  Limited by the following:
     * <li>Input buffer max output
     * <li>Fuel cell max output - if has local generator and not connected to power
     * <li>Power network max draw - if connected to power network
     * </li>
     */
    public float maxPowerOutputWatts()
    {
        return this.maxPowerOutputWatts;
    }

    /**
     * Max discrete energy output per tick implied by {@link #maxPowerOutputWatts()}
     * Effective limit for {@link #provideEnergy(long, boolean, boolean)}.  In joules.
     */
    public long maxEnergyOutputPerTick()
    {
        return this.maxEnergyOutputPerTick;
    }
  
    /**
     * True if power manager is able to receive power right now.
     * Will always be false for fuel cells or other closed providers.
     * If false, any attempt to input power will receive a zero result.
     */
    public boolean canAcceptEnergy()
    {
        return this.battery != null && this.battery.canAcceptEnergy();
    }

    /**
     * Adds energy to this provider. 
     * 
     * While conceptually this is power, is handled as energy due to the
     * quantized nature of time in Minecraft. Intended to be called each tick.<br><br>
     *
     * @param maxInput
     *            Maximum amount of energy to be inserted, in joules.<br>
     *            Limited by {@link #maxEnergyInputPerTick()}.
     *            
     * @param allowPartial
     *            If false, no energy will be input unless the entire requested amount can be accepted.
     *            
     * @param simulate
     *            If true, result will be simulated and no state change occurs.
     *            
     * @return Energy accepted (or that would have been have been accepted, if simulated) in joules.
     */
    public long acceptEnergy(long maxInput, boolean allowPartial, boolean simulate)
    {
        return this.battery.acceptEnergy(maxInput, allowPartial, simulate);
    }
   
    /**
     * Consumes energy from this provider.<p>
     * 
     * While conceptually this is power, is handled as energy due to the
     * quantized nature of time in Minecraft. Intended to be called each tick.<p>
     *
     * Tries to draw power from grid start (if available), then from fuel cell, then from battery.
     * Will combine output from multiple sources if necessary.<p>
     * 
     * @param maxOutput
     *            Maximum amount of energy to be extracted, in joules.<br>
     *            Limited by {@link #maxEnergyOutputPerTick()}.
     *            
     * @param allowPartial
     *            If false, no energy will be extracted unless the entire requested amount can be provided.
     *            
     * @param simulate
     *            If true, result will be simulated and no state change occurs.
     *            
     * @return Energy extracted (or that would have been have been extracted, if simulated) in joules.
     */
    public long provideEnergy(AbstractMachine machine, long maxOutput, boolean allowPartial, boolean simulate)
    {
        // prevent shenannigans/derpage
        if(maxOutput <= 0) return 0; 
        
        long result = 0;
        
        // if not accepting partial fulfillment, have to simulate all results start
        if(!allowPartial)
        {
            result = provideEnergyInner(machine, maxOutput, true);
            if(result != maxOutput) return 0;
            if(!simulate) result = provideEnergyInner(machine, maxOutput, false);
        }
        else
        {
            result = provideEnergyInner(machine, maxOutput, simulate);
        }
        
        // forgive self for prior failures if managed to do something this time
        if(result > 0 && !simulate && this.isFailureCause) this.setFailureCause(false);
        
        return result;
    }
    
    private long provideEnergyInner(AbstractMachine machine, long maxOutput, boolean simulate)
    {
        long result = this.fuelCell == null ? 0 : this.fuelCell.provideEnergy(machine, maxOutput, true, simulate);
        
        if(result < maxOutput && this.battery != null)
        {
            result += this.battery.provideEnergy(machine, maxOutput - result, true, simulate);
        }
        
        return result;        
    }
    
    /**
     * True if this provider's (low) level has recently caused a 
     * processing failure.  Sent to clients for rendering to inform player
     * but not saved to world because is transient information.
     */
    public boolean isFailureCause()
    {
        return isFailureCause;
    }

    /**
     * See {@link #isFailureCause()}
     */
    public void setFailureCause(boolean isFailureCause)
    {
        this.isFailureCause = isFailureCause;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        this.fuelCell = tag.hasKey(ModNBTTag.MACHINE_FUEL_CELL_PLATE_SIZE) ? new PolyethyleneFuelCell(tag) : null;
        //FIXME: move serialization back to here? or remove
//        if(this.battery != null) this.battery.deserializeNBT(tag);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        if(this.fuelCell != null) this.fuelCell.serializeNBT(tag);
//        if(this.battery != null) this.battery.serializeNBT(tag);
    }

    /**
     * On server, regenerates power from PE and handles other housekeeping.
     * Returns true if internal state was modified and should be sent to client and/or persisted.
     */
    public boolean tick(AbstractMachine machine, long tick)
    {
        boolean didChange = false;
        boolean canBatteryCharge = this.battery != null && this.battery.canAcceptEnergy();
        
        // if machine is off, nothing to do unless we are going to recharge the battery
        if(canBatteryCharge || machine.isOn())
        {
            // Generate power to recharge battery if needed.
            // Do this before we clear per-tick limits so we don't 
            // consume capacity that is needed for production. 
            if(canBatteryCharge)
            {
                long joulesNeeded = this.battery.acceptEnergy(this.battery.maxEnergyInputJoulesPerTick(), true, true);
                
                if(joulesNeeded > 0)
                {
                    //TODO: transfer energy in here
                    
                    if(joulesNeeded > 0 && this.fuelCell != null)
                    {
                        long joulesAvailable = this.fuelCell.provideEnergy(machine, joulesNeeded, true, false);
                        if(joulesAvailable > 0)
                        {
                            didChange = true;
                            if(this.battery.acceptEnergy(joulesAvailable, true, false) != joulesAvailable && Log.DEBUG_MODE)
                            {
                                Log.info("Battery energy acceptance mismatch during recharging. This is a bug.  Some energy was lost.");
                            }
                        }
                    }
                }
            }
            
            // update tracking totals
            
            float powerLastTick = 0;
            
            if(this.battery != null)
            {
                powerLastTick += this.battery.powerOutputWatts();
                powerLastTick -= this.battery.powerInputWatts();
            }
            
            if(this.fuelCell != null)
            {
                this.fuelCell.advanceIOTracking();
                powerLastTick += this.fuelCell.powerOutputWatts();
            }
            
            this.powerOutputWatts = powerLastTick;
            
        }
        return didChange;
    }

//    private String formatedAvailableEnergyJoules;
//    
//    @Override
//    @SideOnly(Side.CLIENT)
//    public String formatedAvailableEnergyJoules()
//    {
//        if(this.formatedAvailableEnergyJoules == null)
//        {
//            this.formatedAvailableEnergyJoules = MachinePower.formatEnergy(this.availableEnergyJoules(), false);
//        }
//        return this.formatedAvailableEnergyJoules;
//    }
//
//    private String formattedAvgNetPowerGainLoss;
//    
//    @Override
//    @SideOnly(Side.CLIENT)
//    public String formattedAvgNetPowerGainLoss()
//    {
//        if(this.formattedAvgNetPowerGainLoss == null)
//        {
//            this.formattedAvgNetPowerGainLoss = MachinePower.formatPower(this.avgNetPowerGainLoss(), true);
//        }
//        return this.formattedAvgNetPowerGainLoss;
//    }

}
