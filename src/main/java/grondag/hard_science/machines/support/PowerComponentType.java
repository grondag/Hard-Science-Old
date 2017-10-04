package grondag.hard_science.machines.support;

public enum PowerComponentType
{
    /**
     * Connection to an external power source.
     */
    EXTERNAL,
    
    /**
     * Energy is stored in the component and is finite.
     */
    STORED,
    
    /**
     * The component generates power from fuel that can be re-stocked from an external source
     */
    GENERATOR;
    
    public boolean hasStoredEnergy()
    {
        return this == STORED;
    }
    
    public boolean canGeneratePower()
    {
        return this == GENERATOR;
    }
}
