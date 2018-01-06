package grondag.hard_science.simulator.resource;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.Useful;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;


/**
 * Set up like an enum but using classes to enable generic-based type safety for resource classes.
 * Probably a better way to do this but I don't think a regular enum will do.
 */
public abstract class StorageType<T extends StorageType<T>>
{
    public static StorageType<?> fromEnum(EnumStorageType e)
    {
        switch(e)
        {
        case FLUID:
            return StorageType.FLUID;
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
    
    @SuppressWarnings("unchecked")
    private StorageType(EnumStorageType enumType)
    {
        this.enumType = enumType;
        this.ordinal = enumType.ordinal();
        this.emptyResource = (IResource<T>) new ItemResource(ItemStack.EMPTY.getItem(), ItemStack.EMPTY.getMetadata(), null, null);
        this.MATCH_ANY = new Predicate<IResource<T>>()
        {
            @Override
            public boolean test(IResource<T> t) { return true; }
        };
    }
    
    @Nullable
    public abstract IResource<T> fromNBT(NBTTagCompound nbt);
    
    @Nullable
    public abstract NBTTagCompound toNBT(IResource<T> resource);
    
    
    @Nullable
    public abstract AbstractResourceWithQuantity<T> fromNBTWithQty(NBTTagCompound nbt);
    
    public static <V extends StorageType<V>> NBTTagCompound toNBTWithType(IResource<V> resource)
    {
        NBTTagCompound result = resource.storageType().toNBT(resource);
        Useful.saveEnumToTag(result, ModNBTTag.RESOURCE_TYPE, resource.storageType().enumType);
        return result;
    }
    
    @SuppressWarnings("unchecked")
    public static <V extends StorageType<V>> IResource<V> fromNBTWithType(NBTTagCompound tag)
    {
        StorageType<?> sType = StorageType
                .fromEnum(Useful.safeEnumFromTag(tag, ModNBTTag.RESOURCE_TYPE, EnumStorageType.ITEM));
        return (IResource<V>) sType.fromNBT(tag);
    }
    
    /** 
     * Resources that must be consumed as they are produced - storage is not possible.
     */    
    public static final StorageTypeNone NONE = new StorageTypeNone();
    public static class StorageTypeNone extends StorageType<StorageTypeNone> 
    { 
        private StorageTypeNone() {super(EnumStorageType.NONE);}
        
        @Override
        public IResource<StorageTypeNone> fromNBT(NBTTagCompound nbt)
        {
            return null;
        }

        @Override
        public NBTTagCompound toNBT(IResource<StorageTypeNone> resource)
        {
            return null;
        }

        @Override
        public AbstractResourceWithQuantity<StorageTypeNone> fromNBTWithQty(NBTTagCompound nbt)
        {
            // TODO Auto-generated method stub
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
        
        /**
         * Note that this expects to get an ItemStack NBT, which
         * is what ItemResource serialization outputs.
         */
        @Override
        public IResource<StorageTypeStack> fromNBT(NBTTagCompound nbt) 
        {
            if(nbt == null) return this.emptyResource;
            
            return ItemResource.fromStack(new ItemStack(nbt));
        }

        @Override
        public NBTTagCompound toNBT(IResource<StorageTypeStack> resource)
        {
            return ((ItemResource)resource).sampleItemStack().serializeNBT();
        }

        @Override
        public AbstractResourceWithQuantity<StorageTypeStack> fromNBTWithQty(NBTTagCompound nbt)
        {
            return new ItemResourceWithQuantity(nbt);
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
        public IResource<StorageTypeFluid> fromNBT(NBTTagCompound nbt)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public NBTTagCompound toNBT(IResource<StorageTypeFluid> resource)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public AbstractResourceWithQuantity<StorageTypeFluid> fromNBTWithQty(NBTTagCompound nbt)
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
        public IResource<StorageTypePower> fromNBT(NBTTagCompound nbt)
        {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public NBTTagCompound toNBT(IResource<StorageTypePower> resource)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public AbstractResourceWithQuantity<StorageTypePower> fromNBTWithQty(NBTTagCompound nbt)
        {
            // TODO Auto-generated method stub
            return null;
        }
    }
   
}
