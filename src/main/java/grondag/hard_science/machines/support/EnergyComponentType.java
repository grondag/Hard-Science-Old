package grondag.hard_science.machines.support;

public enum EnergyComponentType
{
    /**
     * Connection to an external power source.
     */
    EXTERNAL,
    
    /**
     * Energy is stored in the component for use by this machine only.
     * Energy in a buffer is owned by this machine and not managed by the energy service
     * manager. Most machines will have a small buffer to hold energy that is
     * to be used or sent to storage. 
     */
    BUFFER,
    
    /**
     * Energy is stored in the component for use by this and possibly other machines.
     * All in/out for a storage component must be governed by the power service
     * manager and must occur on the service thread. Typically only present on 
     * power storage and production machines.
     */
    STORAGE,
    
    /**
     * The component generates power from fuel that can be re-stocked from an external source
     */
    GENERATOR;
    
    public boolean hasStoredEnergy()
    {
        return this == STORAGE || this == BUFFER;
    }
    
    public boolean canGeneratePower()
    {
        return this == GENERATOR;
    }
}
