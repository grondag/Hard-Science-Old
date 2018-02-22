package grondag.hard_science.machines.support;

import grondag.hard_science.simulator.Simulator;

/**
 * Handles constraints and accounting for bulkResource containers.
 * @author grondag
 *
 */
public abstract class ThroughputRegulator
{
    /**
     * Has no internal state, enforces no limits and does no accounting.
     * Use when you don't want to regulate and don't want to check for null.
     */
    public static final ThroughputRegulator DUMMY = new Dummy();
    /**
     * If this regulator has input constraints, 
     * limits the requested amount to those constraints
     * and updates internal accounting if applicable. <p>
     * 
     * @param requested    desired input amount
     * @param isSimulated  if false, will not update internal accounting
     * @param allowPartial if false will return zero if constraints don't permit full amount
     * @return input amount that fits within constraints
     */
    public long limitInput(long requested, boolean isSimulated, boolean allowPartial)
    {
        return requested;
    }
    
    /**
     * If this regulator has output constraints, 
     * limits the requested amount to those constraints
     * and updates internal accounting if applicable. <p>
     * 
     * @param requested    desired output amount
     * @param isSimulated  if false, will not update internal accounting
     * @param allowPartial if false will return zero if constraints don't permit full amount
     * @return output amount that fits within constraints
     */
    public long limitOutput(long requested, boolean isSimulated, boolean allowPartial)
    {
        return requested;
    }
    
    public long maxOutputPerTick()
    {
        return Long.MAX_VALUE;
    }
    
    public long maxInputPerTick()
    {
        return Long.MAX_VALUE;
    }
    
    public long inputLastTick()
    {
        return 0;
    }
    
    public long outputLastTick()
    {
        return 0;
    }
    
    /**
     * Has no limits and does no accounting.
     */
    private static class Dummy extends ThroughputRegulator
    {
        
    }
    
    public boolean isFailureCause()
    {
        return false;
    }
    
    public void blame() {};
    
    public void forgive() {};

    /**
     * Has no limits but tracks input/output per tick.
     */
    public static class Tracking extends ThroughputRegulator
    {
        protected long inputLastTick;
        
        /** total resources input during the current tick. */
        protected long inputThisTick;
        
        protected long outputLastTick;
        
        /** total resources input during the current tick. */
        protected long outputThisTick;
        
        protected int lastTickSeen = Integer.MIN_VALUE;
        
        protected boolean isFailureCause = false;

        protected synchronized void updateTracking()
        {
            int now = Simulator.instance().getTick();
            if(now == lastTickSeen) return;
            
            if (now == lastTickSeen + 1)
            {
                this.outputLastTick = this.outputThisTick;
                this.inputLastTick = this.inputThisTick;
            }
            else
            {
                this.outputLastTick = 0;
                this.inputLastTick = 0;
            }
            this.inputThisTick = 0;
            this.outputThisTick = 0;
            this.lastTickSeen = now;
        }
        
        @Override
        public synchronized long limitInput(long requested, boolean isSimulated, boolean allowPartial)
        {
            this.updateTracking();
            if(!isSimulated)
            {
                this.inputThisTick += requested;
            }
            return requested;
        }

        @Override
        public synchronized long limitOutput(long requested, boolean isSimulated, boolean allowPartial)
        {
            this.updateTracking();
            if(!isSimulated)
            {
                this.outputThisTick += requested;
            }
            return requested;
        }

        @Override
        public long inputLastTick()
        {
            this.updateTracking();
            return this.inputLastTick;
        }

        @Override
        public long outputLastTick()
        {
            this.updateTracking();
            return this.outputLastTick;
        }

        @Override
        public boolean isFailureCause()
        {
            return this.isFailureCause;
        }

        @Override
        public void blame()
        {
            this.isFailureCause = true;
        }

        @Override
        public void forgive()
        {
            this.isFailureCause = false;
        }
    }
    
    /**
     * Has input/output limits and also does accounting
     */
    public static class Limited extends ThroughputRegulator.Tracking
    {
        private final long maxInputPerTick;
        
        private final long maxOutputPerTick;
        
        public Limited(long maxInputPerTick, long maxOutputPerTick)
        {
            this.maxInputPerTick = maxInputPerTick;
            this.maxOutputPerTick = maxOutputPerTick;
        }
        
        @Override
        public synchronized long limitInput(final long requested, boolean isSimulated, boolean allowPartial)
        {
            this.updateTracking();
            long result = Math.min(requested, this.maxInputPerTick - this.inputThisTick);
            
            if(result != requested && !allowPartial) return 0;
            
            if(!isSimulated)
            {
                this.inputThisTick += result;
            }
            return result;
        }

        @Override
        public synchronized long limitOutput(final long requested, boolean isSimulated, boolean allowPartial)
        {
            this.updateTracking();
            
            long result = Math.min(requested, this.maxOutputPerTick - this.outputThisTick);
            
            if(result != requested && !allowPartial) return 0;
            
            if(!isSimulated)
            {
                this.outputThisTick += result;
            }
            return result;
        }

        @Override
        public long maxOutputPerTick()
        {
            return this.maxOutputPerTick;
        }

        @Override
        public long maxInputPerTick()
        {
            return this.maxInputPerTick;
        }
    }
}
