package grondag.hard_science.simulator.wip;

import java.util.function.Predicate;


/**
 * Set up like an enum but using classes to enable generic-based type safety for resource classes.
 * Probably a better way to do this but I don't think a regular enum will do.
 */
public abstract class StorageType<T extends StorageType<T>>
{
    public abstract int ordinal();
    public final Predicate<IResource<T>> MATCH_ANY;
    
    private StorageType()
    {
        this.MATCH_ANY = new Predicate<IResource<T>>()
        {
            @Override
            public boolean test(IResource<T> t) { return true; }
        };
    }
    
    /** 
     * Resources that must be consumed as they are produced - storage is not possible.
     */    
    public static final StorageTypeNone NONE = new StorageTypeNone();
    public static class StorageTypeNone extends StorageType<StorageTypeNone> { public int ordinal() { return 0; }}
    
    /**
     * Materials stored as item stacks. AbstractStorage managers for other storage types that can be encapsulated
     * as item stacks will use the item stack storage manager as a subsystem.
     */
    public static final StorageTypeStack ITEM = new StorageTypeStack();
    public static class StorageTypeStack extends StorageType<StorageTypeStack> { public int ordinal() { return 1; }}
    
    /**
     * Has to be encapsulated or stored in a tank or basin.
     */
    public static final StorageTypeFluid FLUID = new StorageTypeFluid();
    public static class StorageTypeFluid extends StorageType<StorageTypeFluid> { public int ordinal() { return 2; }}
    
    /**
     * Like fluid, but can't be stored in an open basin.
     */
    public static final StorageTypeGas GAS = new StorageTypeGas();
    public static class StorageTypeGas extends StorageType<StorageTypeGas> { public int ordinal() { return 3; }}
    
    /**
     * Must be stored in a battery.  Note that fuel is not counted as power 
     * because making power from fuel is a non-trivial production step. 
     */
    public static final StorageTypePower POWER = new StorageTypePower();
    public static class StorageTypePower extends StorageType<StorageTypePower> { public int ordinal() { return 4; }}
    
    public static interface ITypedStorage<V extends StorageType<V>>
    {
        public V storageType();
    }
    
   
}
