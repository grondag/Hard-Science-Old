package grondag.hard_science.machines.support;

import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.machines.base.AbstractMachine;

public abstract class AbstractPowerComponent implements IPowerComponent
{
    /**
     * Set by subclasses via {@link #setMaxOutputJoulesPerTick(long)}
     */
    private long maxEnergyOutputPerTick;
    
    /** total of all energy provided during the current tick. */
    private long outputThisTick;
    
    /**
     * total of all energy provided last tick.
     */
    private long outputLastTick;
    
    /**
     * Must be called in subclasses during initialization and NBT deserialization.
     * Does NOT need to be called during packet deserialization.
     */
    protected final void setMaxOutputJoulesPerTick(long maxJoules)
    {
        this.maxEnergyOutputPerTick = maxJoules;
    }
    
    
    @Override
    public float powerOutputWatts()
    {
        return this.outputLastTick * TimeUnits.TICKS_PER_SIMULATED_SECOND;
    }

//    @Override
//    public long energyOutputCurrentTickJoules()
//    {
//        return this.outputThisTick;
//    }
    
    @Override
    public long maxEnergyOutputJoulesPerTick()
    {
        return this.maxEnergyOutputPerTick;
    }

    @Override
    public boolean canProvideEnergy(AbstractMachine machine)
    {
        return this.outputThisTick < this.maxEnergyOutputPerTick;
    }
    
    @Override
    public final long provideEnergy(AbstractMachine machine, long maxOutput, boolean allowPartial, boolean simulate)
    {
        // prevent shenannigans/derpage
        if(maxOutput <= 0) return 0;
        
        long result = Useful.clamp(maxOutput, 0L, this.maxEnergyOutputJoulesPerTick() - this.outputThisTick);

        if(!(allowPartial || result == maxOutput)) return 0;

        result = this.provideEnergyImplementation(machine, result, allowPartial, simulate);
        
        if(!(result == 0 || simulate)) 
        {
            this.outputThisTick += result;
        }
        
        return result;
    }
    
    /**
     * Implementation can assume maxOutput has already been tested/adjusted against per-tick max.
     * Machine is the machine containing the power component - in case it needs access to 
     * other machine components.
     */
    protected abstract long provideEnergyImplementation(AbstractMachine machine, long maxOutput, boolean allowPartial, boolean simulate);
   
    @Override
    public void advanceIOTracking()
    {
        this.outputLastTick = this.outputThisTick;
        this.outputThisTick = 0;
    }
}
