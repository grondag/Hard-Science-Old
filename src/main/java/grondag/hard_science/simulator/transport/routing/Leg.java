package grondag.hard_science.simulator.transport.routing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.hard_science.simulator.transport.carrier.CarrierCircuit;

/**
 * Describes one half of a route - path from a starting
 * circuit to an ending circuit.  End circuit is always
 * the highest level circuit if more than one circuit.
 * To form a route, end circuits of two legs must be the same.
 */
public abstract class Leg
{
    /**
     * Lowest-level circuit in the leg. 
     * Will be same as {@link #end()} for direct routes.
     */
    @Nonnull
    public abstract CarrierCircuit start();
    
    /**
     * Highest-level circuit in the leg. 
     * Will be same as {@link #start()} for direct routes.
     */
    @Nonnull
    public abstract CarrierCircuit end();
    
    /**
     * If leg has three circuits, the circuit between
     * {@link #start()} and {@link #end()}. Null otherwise.
     */
    @Nullable
    public abstract CarrierCircuit inner();
    
    /**
     * Number of circuits in the leg. 
     * Possible values are 1, 2 or 3.
     */
    public abstract int size();
    
    /**
     * Returns a new leg with end circuit as the given
     * circuit.  Only valid if size < 3 and provided circuit
     * is one level higher than current end.
     */
    public abstract Leg append(CarrierCircuit newEnd);
    
    protected final CarrierCircuit start;
    
    private Leg(CarrierCircuit first)
    {
        this.start = first;
    }
    
    @Nonnull 
    public static Leg create(@Nonnull CarrierCircuit single)
    {
        return new SingleLeg(single);
    }
    
    @Nonnull 
    public static Leg create(@Nonnull CarrierCircuit start, @Nonnull CarrierCircuit end)
    {
        return new DoubleLeg(start, end);
    }
    
    @Nonnull 
    public static Leg create(@Nonnull CarrierCircuit start, @Nonnull CarrierCircuit inner, @Nonnull CarrierCircuit end)
    {
        return new TripleLeg(start, inner, end);
    }
    
    private static class SingleLeg extends Leg
    {
        private SingleLeg(@Nonnull CarrierCircuit single)
        {
            super(single);
        }

        @Override
        public CarrierCircuit start()
        {
            return this.start;
        }

        @Override
        @Nonnull 
        public CarrierCircuit end()
        {
            return this.start;
        }

        @Override
        public CarrierCircuit inner()
        {
            return null;
        }

        @Override
        @Nonnull 
        public int size()
        {
            return 1;
        }

        @Override
        public Leg append(@Nonnull CarrierCircuit newEnd)
        {
            return new DoubleLeg(this.start, newEnd) ;
        }
        
        @Override
        public String toString()
        {
            return Integer.toString(this.start.carrierAddress());
        }
    }
    
    private static class DoubleLeg extends Leg
    {
        protected final CarrierCircuit end;
        
        private DoubleLeg(@Nonnull CarrierCircuit start, @Nonnull CarrierCircuit end)
        {
            super(start);
            this.end = end;
            
            // allow for gap of bottom/top because will see that in triple subclass
            assert start.carrier.level.ordinal() < end.carrier.level.ordinal()
                    : "Circuit level mismatch in leg constructor.";
            
            assert start.carrier.storageType == end.carrier.storageType
                    : "Circuit type mismatch in leg constructor.";
        }

        @Override
        @Nonnull 
        public CarrierCircuit start()
        {
            return this.start;
        }

        @Override
        @Nonnull 
        public CarrierCircuit end()
        {
            return this.end;
        }

        @Override
        public CarrierCircuit inner()
        {
            return null;
        }

        @Override
        public int size()
        {
            return 2;
        }

        @Override
        @Nonnull 
        public Leg append(@Nonnull CarrierCircuit newEnd)
        {
            return new TripleLeg(this.start, this.end, newEnd);
        }
        
        @Override
        public String toString()
        {
            return String.format("%d.%d", this.start.carrierAddress(), this.end.carrierAddress());
        }
    }
    
    private static class TripleLeg extends DoubleLeg
    {
        protected final CarrierCircuit inner;
        
        private TripleLeg(@Nonnull CarrierCircuit start, @Nonnull CarrierCircuit inner, @Nonnull CarrierCircuit end)
        {
            super(start, end);
            this.inner = inner;
            
            assert start.carrier.level == inner.carrier.level.below()
                    && inner.carrier.level == end.carrier.level.below()
                    : "Circuit level mismatch in leg constructor.";
            
            
            assert start.carrier.storageType == inner.carrier.storageType
                    && inner.carrier.storageType  == end.carrier.storageType
                    : "Circuit type mismatch in leg constructor.";
        }

        @Override
        @Nonnull 
        public CarrierCircuit start()
        {
            return this.start;
        }

        @Override
        @Nonnull 
        public CarrierCircuit end()
        {
            return this.end;
        }

        @Override
        @Nonnull 
        public CarrierCircuit inner()
        {
            return this.inner;
        }

        @Override
        public int size()
        {
            return 3;
        }
        
        @Override
        public Leg append(@Nonnull CarrierCircuit newEnd)
        {
            throw new UnsupportedOperationException("Cannot append circuit to a leg with three circuits.");
        }
        
        @Override
        public String toString()
        {
            return String.format("%d.%d.%d",
                    this.start.carrierAddress(), 
                    this.inner.carrierAddress(), 
                    this.end.carrierAddress());
        }
    }
}
