package grondag.hard_science.machines.support;

import grondag.hard_science.library.serialization.IMessagePlus;
import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.machines.base.AbstractMachine;

//FIXME: remove cruft

/**
 * All stored energy is represented in Joules (aka Watt-seconds).<br>
 * All power input/output represents as Watts.<br><br>
 * 
 * Why not use IEnergyStorage or similar?<br>
 * 1) Explicitly want to use standard power/energy units. (watts/joules) <br>
 * 2) Want to support larger quanities (long vs int) <br>
 * 3) Want ability to reject partial input/output
 * 
 */
public interface IMachinePowerProvider extends IReadWriteNBT, IMessagePlus
{
     /**
     * Highest possible continuous rate of power draw from this provider, 
     * including all component power sources.
     */
    public float maxPowerOutputWatts();
    
    /**
     * Max discrete energy output per tick implied by {@link #maxPowerOutputWatts()}
     * Effective limit for {@link #provideEnergy(long, boolean, boolean)}.  In joules.
     */
    public long maxEnergyOutputPerTick();

    /**
     * True if provider is actually able to provide power right now.
     * If false, any attempt to extract power will receive a zero result.
     */
    public boolean canProvideEnergy(AbstractMachine machine);
    
    /**
     * Consumes energy from this provider. 
     * 
     * While conceptually this is power, is handled as energy due to the
     * quantized nature of time in Minecraft. Intended to be called each tick.<br><br>
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
    long provideEnergy(AbstractMachine machine, long maxOutput, boolean allowPartial, boolean simulate);

    /**
     * True if this provider's (low) level has recently caused a 
     * processing failure.  Sent to clients for rendering to inform player
     * but not saved to world because is transient information.
     */
    public boolean isFailureCause();
    
    /**
     * See {@link #isFailureCause()}
     */
    public void setFailureCause(boolean isFailureCause);

//    /**
//     * Energy currently stored in this provider.
//     * Will be zero unless provider contains a battery, capacitor, flywheel, etc.
//     * Does NOT consider fuel for open fuel cells because fuel is handled by material manager.
//     * Closed fuel cells ARE included because the fuel is part of the power system.
//     */
//    public long storedEnergyJoules();
//    
//    /**
//     * Maximum possible value of {@link #storedEnergyJoules()}.
//     */
//    public long maxStoredEnergyJoules();
//    
//    /**
//     * Formated {@link #availableEnergyJoules()} cached on client for rendering.
//     */
//    @SideOnly(Side.CLIENT)
//    public String formatedStoredEnergyJoules();

    //////////
    
    
    //FIXME: should rename input/output to charge/discharge, because all refer to battery status
    
    /**
     * If this power supply has a battery, this is the max continuous rate of recharge.
     * It will also be limited by the max power receive rate or power generation rate of the power receiver or fuel cell in the unit.
     * Used to set the upper bound of recharge rate when displaying power status.
     */
//    public long maxPowerInputWatts();
    
    /**
     * Max discrete energy input per tick implied by {@link #maxPowerInputWatts()}
     * Effective limit for {@link #receiveEnergy(long, boolean, boolean)}.  In joules.
     */
//    public long maxEnergyInputPerTick();
    
    
    /**
     * Higher value of {@link #maxPowerInputWatts()} and {@link #maxPowerOutputWatts()}.
     * Provided as convenience for rendering routines.
     */
//    public long maxPowerInOrOutWatts();
    
    /**
     * Average recent energy input level. In watts.
     */
//    public long avgPowerInputWatts();
    
    /**
     * {@link #avgPowerInputWatts()} scaled from 0 to 180 for display. Only valid client-side.
     */
//    public int logAvgPowerInputDegrees();

    /**
     * Recent energy consumption level. In watts. Does not include power used to charge the battery.
     */
    public float powerOutputWatts();

    /**
     * {@link #avgPowerOutputWatts()} scaled from 0 to 180 for display. Only valid client-side.
     */
//    public int avgPowerOutputDegress();

    /**
     * Average recent power net in/out. Will be negative if outflow exceeds inflow. '
     * Computation is simply {@link #avgPowerInputWatts()} - {@link #avgPowerOutputWatts()}.
     */
//    public long avgNetPowerGainLoss();
    
    /**
     * Formated {@link #avgNetPowerGainLoss()} cached on client for rendering.
     */
//    @SideOnly(Side.CLIENT)
//    public String formattedAvgNetPowerGainLoss();
    
    /**
     * True if provider is able to receive power right now.
     * Will always be false for fuel cells or other closed providers.
     * If false, any attempt to input power will receive a zero result.
     */
    public boolean canAcceptEnergy();
    
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
     long acceptEnergy(long maxInput, boolean allowPartial, boolean simulate);

 
     /**
      * On server, regenerates power from PE and handles other housekeeping.
      * Returns true if internal state was modified and should be sent to client and/or persisted.
      */
     boolean tick(AbstractMachine machine, long tick);
}
