package grondag.hard_science.simulator.transport.carrier;

import javax.annotation.Nullable;

public enum CarrierLevel
{
    /**
     * Sub-sonic bus, moderate volume/power.
     */
    BASE,
    
    /**
     * Sub-sonic, high capacity, multi-path bus.
     */
    INTERMEDIATE,
    
    /**
     * Supersonic/superconducting switches and interconnects.
     */
    TOP;
    
    public boolean isBottom()
    {
        return this == BASE;
    }
    
    public boolean isTop()
    {
        return this == TOP;
    }
    
    @Nullable 
    public CarrierLevel above()
    {
        switch(this)
        {
        case BASE:
            return INTERMEDIATE;
            
        case INTERMEDIATE:
            return TOP;
            
        case TOP:
        default:
            return null;
        }
    }
    
    @Nullable 
    public CarrierLevel below()
    {
        switch(this)
        {
        case TOP:
            return INTERMEDIATE;
            
        case INTERMEDIATE:
            return BASE;
            
        case BASE:
        default:
            return null;
        }
    }
}
