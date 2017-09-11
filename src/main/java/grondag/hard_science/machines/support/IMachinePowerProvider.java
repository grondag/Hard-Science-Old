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
     * Maximum energy that can be stored in this provider.
     */
    public long maxEnergyJoules();
    
    /**
     * Highest possible rate of power input for this provider. 
     * For capacitors and batteries, this is max recharge rate from an outside power source.
     * For fuel cells, all of which include a battery, this is the max internal regenerative capacity.
     */
    public long maxPowerInputWatts();
    
    /**
     * Highest possible rate of power draw from this provider.
     */
    public long maxPowerOutputWatts();
    
    /**
     * Higher value of {@link #maxPowerInputWatts()} and {@link #maxPowerOutputWatts()}.
     * Provided as convenience for rendering routines.
     */
    public long maxPowerInOrOutWatts();
    
    /**
     * Average recent energy input level over last period prior to client update.
     * Because this is for client use, counters on server are be reset by {@link #serializeToArray()}.
     */
    @SideOnly(Side.CLIENT)
    public long avgPowerInputWatts();
    
    /**
     * Average recent energy output level over last period prior to client update.
     * Because this is for client use, counters on server are be reset by {@link #serializeToArray()}.
     */
    @SideOnly(Side.CLIENT)
    public long avgPowerOutputWatts();
    
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
     * quantized nature of time in Minecraft. <br><br>
     *
     * @param maxInput
     *            Maximum amount of energy to be inserted, in joules.
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
      * quantized nature of time in Minecraft. <br><br>
      *
      * @param maxOutput
      *            Maximum amount of energy to be extracted, in joules.
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
      */
     void tick(MaterialBuffer PEBuffer);
    
}
