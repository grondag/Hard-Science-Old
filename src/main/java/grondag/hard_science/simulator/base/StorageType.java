package grondag.hard_science.simulator.base;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;


/**
 * Set up like an enum but using classes to enable generic-based type safety for resource classes.
 * Probably a better way to do this but I don't think a regular enum will do.
 */
public abstract class StorageType<T extends StorageType<T>>
{
    public static enum EnumStorageType
    {
        NONE,
        ITEM,
        FLUID,
        GAS,
        POWER
    }
    
    public static StorageType<?> fromEnum(EnumStorageType e)
    {
        switch(e)
        {
        case FLUID:
            return StorageType.FLUID;
        case GAS:
            return StorageType.GAS;
        case ITEM:
            return StorageType.ITEM;
        case POWER:
            return StorageType.POWER;
        default:
        case NONE:
            return StorageType.NONE;
        }
    }
    
    public  final EnumStorageType enumType;
    public final IResource<T> emptyResource;
    public final int ordinal;
    public final Predicate<IResource<T>> MATCH_ANY;
    
    private StorageType(EnumStorageType enumType)
    {
        this.enumType = enumType;
        this.ordinal = enumType.ordinal();
        this.emptyResource = this.makeResource(null);
        this.MATCH_ANY = new Predicate<IResource<T>>()
        {
            @Override
            public boolean test(IResource<T> t) { return true; }
        };
    }
    
    @Nullable
    public abstract IResource<T> makeResource(NBTTagCompound nbt);
    
    /** 
     * Resources that must be consumed as they are produced - storage is not possible.
     */    
    public static final StorageTypeNone NONE = new StorageTypeNone();
    public static class StorageTypeNone extends StorageType<StorageTypeNone> 
    { 
        private StorageTypeNone() {super(EnumStorageType.NONE);}
        
        @Override
        public IResource<StorageTypeNone> makeResource(NBTTagCompound nbt)
        {
            //TODO
            return null;
        }
    }
    
    /**
     * Materials stored as item stacks. AbstractStorage managers for other storage types that can be encapsulated
     * as item stacks will use the item stack storage manager as a subsystem.
     */
    public static final StorageTypeStack ITEM = new StorageTypeStack();
    public static class StorageTypeStack extends StorageType<StorageTypeStack> 
    { 
        private StorageTypeStack() {super(EnumStorageType.ITEM);}
        
        @Override
        public IResource<StorageTypeStack> makeResource(NBTTagCompound nbt) 
        {
            return new ItemResource(nbt);
        }
    }
            
    /**
     * Has to be encapsulated or stored in a tank or basin.
     */
    public static final StorageTypeFluid FLUID = new StorageTypeFluid();
    public static class StorageTypeFluid extends StorageType<StorageTypeFluid>
    {
        private StorageTypeFluid() {super(EnumStorageType.FLUID);}

        @Override
        public IResource<StorageTypeFluid> makeResource(NBTTagCompound nbt)
        {
            // TODO Auto-generated method stub
            return null;
        }
    }
    
    /**
     * Like fluid, but can't be stored in an open basin.
     */
    public static final StorageTypeGas GAS = new StorageTypeGas();
    public static class StorageTypeGas extends StorageType<StorageTypeGas>
    {
        private StorageTypeGas() {super(EnumStorageType.GAS);}
    
        @Override
        public IResource<StorageTypeGas> makeResource(NBTTagCompound nbt)
        {
            // TODO Auto-generated method stub
            return null;
        }
    }
    
    /**
     * Must be stored in a battery.  Note that fuel is not counted as power 
     * because making power from fuel is a non-trivial production step. 
     */
    public static final StorageTypePower POWER = new StorageTypePower();
    public static class StorageTypePower extends StorageType<StorageTypePower>
    {
        private StorageTypePower() {super(EnumStorageType.POWER);}
       

        @Override
        public IResource<StorageTypePower> makeResource(NBTTagCompound nbt)
        {
            // TODO Auto-generated method stub
            return null;
        }
    }
    
    public static interface ITypedStorage<V extends StorageType<V>>
    {
        public V storageType();
    }
    
   
}
