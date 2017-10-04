package grondag.hard_science.machines.support;

import grondag.hard_science.library.serialization.IMessagePlus;
import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.machines.base.MachineTileEntity;

public interface IPowerComponent extends IReadWriteNBT, IMessagePlus
{
    /**
     * Governs display and informs interpretation of other attributes
     */
    public PowerComponentType componentType();
    
    /**
     * If this component can store energy, the current stored energy.
     * Always zero if component cannot store energy.
     */
    public default long storedEnergyJoules() { return 0; }
    
    /**
     * If this component can store energy, the maximum stored energy.
     * Always zero if component cannot store energy.
     */
    public default long maxStoredEnergyJoules() { return 0; };
    
    /**
     * Approximate recent level of power coming into the component. 
     * May be an average, only occasionally updated and/or smoothed.
     * Always zero or positive. <br><br>
     * 
     * If the component stores power, this represents charging.
     * A non-zero value implies {@link #powerOutputWatts()} will be zero.<br><br>
     * 
     * If the component is an external power source, this is the power
     * available in the external grid, if known, or will be the same as {@link #powerOutputWatts()}<br><br>
     * 
     * If the component is a generator, will always be zero.
     */
    public float powerInputWatts();
    
    /**
     * Running total of energy input in the current tick.
     */
    public long energyInputCurrentTickJoules();

    /**
     * Maximum possible value of {@link #powerInputWatts()}. 
     * May be an estimate if not known exactly.
     */
    public float maxPowerInputWatts();

    /**
     * Same semantics as {@link #maxPowerInputWatts()} but represented 
     * as joules per tick for use by machine internal logic.
     */
    public long maxEnergyInputJoulesPerTick();
    
    
    /**
     * Level of power coming out of the component
     * (presumably) due to power consumption during the last tick. 
     * Always zero or positive. <br><br>
     * 
     * If the component stores power, this represents discharge. 
     * A non-zero value implies {@link #powerInputWatts()} will be zero.<br><br>
     * 
     * If the component is an external power source, this is the power
     * drawn from the external grid. <br><br>
     * 
     * If the component is a generator, represents generator output.
     */
    public float powerOutputWatts();

    /**
     * Running total of energy output in the current tick.
     */
    public long energyOutputCurrentTickJoules();

    
    /**
     * Maximum possible value of {@link #powerOutputWatts()}. 
     * May be an estimate if not known exactly.
     */
    public float maxPowerOutputWatts();
    
    /**
     * Same semantics as {@link #maxPowerOutputWatts()} but represented 
     * as joules per tick for use by machine internal logic.
     */
    public long maxEnergyOutputJoulesPerTick();
    
    /**
     * True if component is able to provide energy right now.
     * If false, any attempt to extract energy will receive a zero result.
     * @param mte TODO
     */
    public boolean canProvideEnergy(MachineTileEntity mte);
    
    /**
     * Consumes energy from this component. 
     * 
     * While conceptually this is power, is handled as energy due to the
     * quantized nature of time in Minecraft. Intended to be called each tick.<br><br>
     * @param mte TODO
     * @param maxOutput
     *            Maximum amount of energy to be extracted, in joules.<br>
    *            Limited by {@link #maxEnergyOutputPerTick()}.
     * @param allowPartial
     *            If false, no energy will be extracted unless the entire requested amount can be provided.
     * @param simulate
     *            If true, result will be simulated and no state change occurs.
     *
     * @return Energy extracted (or that would have been have been extracted, if simulated) in joules.
     */
    public long provideEnergy(MachineTileEntity mte, long maxOutput, boolean allowPartial, boolean simulate);
    
    
    /**
     * True if provider is able to receive energy right now.
     * Will always be false for external power sources and internal generators.
     * If false, any attempt to input energy will receive a zero result.
     */
    public default boolean canAcceptEnergy() { return false; }
    
    /**
     * Adds energy to this component. 
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
     public default long acceptEnergy(long maxInput, boolean allowPartial, boolean simulate)  { return 0; }
     
     /**
      * Called by power manager to move current tick total(s) to last tick, and start a new per-tick total.
      */
     public abstract void advanceIOTracking();
}
