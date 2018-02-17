package grondag.hard_science.matter;

/**
 * Represents a bulk resource within a machine buffer or process.
 */
public class MatterStack
{
    public final Matter matter;
    public final long nanoLiters;
    
    public MatterStack(Matter matter, long nanoLiters)
    {
        this.matter = matter;
        this.nanoLiters = nanoLiters;
    }
    
    public String systemName()
    {
        return this.matter.systemName();
    }
    
    public String displayName()
    {
        return this.matter.displayName() + ", "
                + (this.matter.phase() == MatterPhase.SOLID
                    ? MassUnits.formatMass((long) (nanoLiters * this.matter.density()), false)
                    : VolumeUnits.formatVolume(nanoLiters, false));
        
    }
}
