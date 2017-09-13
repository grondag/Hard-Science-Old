package grondag.hard_science.machines.support;

import grondag.hard_science.library.serialization.IReadWriteNBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
public interface IMachinePowerProvider extends IReadWriteNBT
{
    /**
     * Energy currently stored in this provider.
     */
    public long availableEnergyJoules();
    
    /**
     * Formated {@link #availableEnergyJoules()} cached on client for rendering.
     */
    @SideOnly(Side.CLIENT)
    public String formatedAvailableEnergyJoules();
    
    /**
     * Maximum energy that can be stored in this provider.
     */
    public long maxEnergyJoules();
    
    /**
     * Highest possible continuous rate of power input for this provider. 
     * For capacitors and batteries, this is max recharge rate from an outside power source.
     * For fuel cells, all of which include a battery, this is the max internal regenerative capacity.
     */
    public long maxPowerInputWatts();
    
    /**
     * Max discrete energy input per tick implied by {@link #maxPowerInputWatts()}
     * Effective limit for {@link #receiveEnergy(long, boolean, boolean)}.  In joules.
     */
    public long maxEnergyInputPerTick();
    
    /**
     * Highest possible continuous rate of power draw from this provider. 
     */
    public long maxPowerOutputWatts();
    
    /**
     * Max discrete energy output per tick implied by {@link #maxPowerOutputWatts()}
     * Effective limit for {@link #provideEnergy(long, boolean, boolean)}.  In joules.
     */
    public long maxEnergyOutputPerTick();
    
    /**
     * Higher value of {@link #maxPowerInputWatts()} and {@link #maxPowerOutputWatts()}.
     * Provided as convenience for rendering routines.
     */
    public long maxPowerInOrOutWatts();
    
    /**
     * Average recent energy input level. In watts.
     */
    public long avgPowerInputWatts();
    
    /**
     * {@link #avgPowerInputWatts()} scaled from 0 to 180 for display. Only valid client-side.
     */
    public int logAvgPowerInputDegrees();

    /**
     * Average recent energy output level. In watts.
     */
    public long avgPowerOutputWatts();

    /**
     * {@link #avgPowerOutputWatts()} scaled from 0 to 180 for display. Only valid client-side.
     */
    public int avgPowerOutputDegress();

    /**
     * Average recent power net in/out. Will be negative if outflow exceeds inflow. '
     * Computation is simply {@link #avgPowerInputWatts()} - {@link #avgPowerOutputWatts()}.
     */
    public long avgNetPowerGainLoss();
    
    /**
     * Formated {@link #avgNetPowerGainLoss()} cached on client for rendering.
     */
    @SideOnly(Side.CLIENT)
    public String formattedAvgNetPowerGainLoss();
    
    /**
     * True if provider is actually able to provide power right now.
     * If false, any attempt to extract power will receive a zero result.
     */
    public boolean canProvidePower();

    /**
     * True if provider is able to receive power right now.
     * Will always be false for fuel cells or other closed providers.
     * If false, any attempt to input power will receive a zero result.
     */
    public boolean canReceivePower();
    
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
     long receiveEnergy(long maxInput, boolean allowPartial, boolean simulate);

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
     long provideEnergy(long maxOutput, boolean allowPartial, boolean simulate);
     
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
     
     
     /**
      * Restores state based on array from {@link #serializeToArray()}
      */
     public void deserializeFromArray(int[] values);
     

     /** 
      * Returns an array for packet serialization.
      */
     public int[] serializeToArray();

     /**
      * On server, regenerates power from PE. 
      * Returns true if internal state was modified and should be sent to client and/or persisted.
     * @return 
      */
     boolean tick(MaterialBuffer PEBuffer);
    
}
