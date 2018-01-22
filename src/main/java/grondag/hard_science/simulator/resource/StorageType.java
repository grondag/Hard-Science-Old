package grondag.hard_science.simulator.resource;

import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.hard_science.HardScience;
import grondag.hard_science.init.ModRegistries;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.machines.energy.MachinePower;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.storage.FluidStorageEvent;
import grondag.hard_science.simulator.storage.IStorageEventFactory;
import grondag.hard_science.simulator.storage.ItemStorageEvent;
import grondag.hard_science.simulator.storage.PowerStorageEvent;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.management.LogisticsService;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;


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
            
        case PRIVATE:
            assert false : "Unsupported private storage type reference";
            return null;
            
        default:
            assert false : "Missing enum mapping for storage type";
            return null;
        }
    }
    
    public  final EnumStorageType enumType;
    public final IResource<T> emptyResource;
    public final int ordinal;
    public final Predicate<IResource<T>> MATCH_ANY;
    
    private StorageType(EnumStorageType enumType, IResource<T> emptyResource)
    {
        this.enumType = enumType;
        this.ordinal = enumType.ordinal();
        this.emptyResource = emptyResource;
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
    
    @Nonnull
    public abstract IStorageEventFactory<T> eventFactory();
    
    @Nonnull
    public abstract LogisticsService<T> service();
    
    /**
     * Units per tick throughput for the given level on storage
     * transport networks of this storage type.  Channel only
     * matter for fluid networks, which have fixed channels for
     * each type of transportable fluid. For other storage types
     * (power and items) this depends solely on level.
     */       
    public abstract long transportCapacity(CarrierLevel level, int channel);
    
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
     * If true, connections from this carrier
     * at different levels must share the same
     * channel.  Only true for fluids.
     * See {@link Channel#Channel()}.
     */
     public boolean channelsSpanLevels()
     {
         return this == StorageType.FLUID;
     }
     
    /**
     * Materials stored as item stacks. AbstractStorage managers for other storage types that can be encapsulated
     * as item stacks will use the item stack storage manager as a subsystem.
     */
    public static final StorageTypeStack ITEM = new StorageTypeStack();
    public static class StorageTypeStack extends StorageType<StorageTypeStack> 
    { 
        private StorageTypeStack()
        {
            super(EnumStorageType.ITEM, 
            new ItemResource(ItemStack.EMPTY.getItem(), ItemStack.EMPTY.getMetadata(), null, null));
        }
        
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

        @Override
        public IStorageEventFactory<StorageTypeStack> eventFactory()
        {
            return ItemStorageEvent.INSTANCE;
        }

        @Override
        public LogisticsService<StorageTypeStack> service()
        {
            return LogisticsService.ITEM_SERVICE;
        }

        @Override
        public long transportCapacity(CarrierLevel level, int channel)
        {
            //TODO: make configurable
            switch(level)
            {
            case BOTTOM:
                return 1;
            case MIDDLE:
                return 4;
            case TOP:
                return 16;
                
            default:
                assert false: "Unhandled enum mapping";
                return 0;
            }
        }
    }
            
    /**
     * Has to be encapsulated or stored in a tank or basin.
     */
    public static final StorageTypeFluid FLUID = new StorageTypeFluid();
    public static class StorageTypeFluid extends StorageType<StorageTypeFluid>
    {
        private StorageTypeFluid()
        {
            super(EnumStorageType.FLUID, new FluidResource(null, null));
        }

        @Override
        public IResource<StorageTypeFluid> fromNBT(NBTTagCompound nbt)
        {
            if(nbt == null) return this.emptyResource;
            return FluidResource.fromStack(FluidStack.loadFluidStackFromNBT(nbt));
        }

        @Override
        public NBTTagCompound toNBT(IResource<StorageTypeFluid> resource)
        {
            NBTTagCompound tag = new NBTTagCompound();
            return ((FluidResource)resource).sampleFluidStack().writeToNBT(tag);
        }

        @Override
        public AbstractResourceWithQuantity<StorageTypeFluid> fromNBTWithQty(NBTTagCompound nbt)
        {
            return new FluidResourceWithQuantity(nbt);
        }

        @Override
        public IStorageEventFactory<StorageTypeFluid> eventFactory()
        {
            return FluidStorageEvent.INSTANCE;
        }

        @Override
        public LogisticsService<StorageTypeFluid> service()
        {
            return LogisticsService.FLUID_SERVICE;
        }

        @Override
        public long transportCapacity(CarrierLevel level, int channel)
        {
            //TODO: make dependent on viscosity and other factors
            switch(level)
            {
            case BOTTOM:
                return VolumeUnits.LITER.nL * 10;
            case MIDDLE:
                return VolumeUnits.KILOLITER.nL;
            case TOP:
                return VolumeUnits.KILOLITER.nL * 16;
                
            default:
                assert false: "Unhandled enum mapping";
                return 0;
            }
        }
    }
    
    
    /**
     * Must be stored in a battery.  Note that fuel is not counted as power 
     * because making power from fuel is a non-trivial production step. 
     */
    public static final StorageTypePower POWER = new StorageTypePower();
    public static class StorageTypePower extends StorageType<StorageTypePower>
    {
        private StorageTypePower()
        {
            super(EnumStorageType.POWER, new PowerResource("empty"));
        }

        @Override
        public IResource<StorageTypePower> fromNBT(NBTTagCompound nbt)
        {
            return PowerResource.JOULES;
        }

        @Override
        public NBTTagCompound toNBT(IResource<StorageTypePower> resource)
        {
            return new NBTTagCompound();
        }

        @Override
        public AbstractResourceWithQuantity<StorageTypePower> fromNBTWithQty(NBTTagCompound nbt)
        {
            return new PowerResourceWithQuantity(nbt);
        }

        @Override
        public IStorageEventFactory<StorageTypePower> eventFactory()
        {
            return PowerStorageEvent.INSTANCE;
        }

        @Override
        public LogisticsService<StorageTypePower> service()
        {
            return LogisticsService.POWER_SERVICE;
        }

        @Override
        public long transportCapacity(CarrierLevel level, int channel)
        {
            //TODO: make configurable
            switch(level)
            {
            case BOTTOM:
                return MachinePower.POWER_BUS_JOULES_PER_TICK;
            case MIDDLE:
                return MachinePower.POWER_BUS_JOULES_PER_TICK * 1000;
            case TOP:
                return MachinePower.POWER_BUS_JOULES_PER_TICK * 1000000;
                
            default:
                assert false: "Unhandled enum mapping";
                return 0;
            }
        }
    }
   
    /**
     * Must be stored in a battery.  Note that fuel is not counted as power 
     * because making power from fuel is a non-trivial production step. 
     */
    public static final StorageTypeBulk PRIVATE = new StorageTypeBulk();
    public static class StorageTypeBulk extends StorageType<StorageTypeBulk>
    {
        private StorageTypeBulk()
        {
            super(EnumStorageType.PRIVATE, new BulkResource(new ResourceLocation(HardScience.prefixResource("empty"))));
        }

        @Override
        public IResource<StorageTypeBulk> fromNBT(NBTTagCompound nbt)
        {
            return nbt != null && nbt.hasKey(ModNBTTag.RESOURCE_IDENTITY)
                ? ModRegistries.bulkResourceRegistry.getValue(new ResourceLocation(nbt.getString(ModNBTTag.RESOURCE_IDENTITY)))
                : this.emptyResource;
        }

        @Override
        public NBTTagCompound toNBT(IResource<StorageTypeBulk> resource)
        {
            NBTTagCompound result = new NBTTagCompound();
            result.setString(ModNBTTag.RESOURCE_IDENTITY, ((BulkResource)resource).getRegistryName().toString());
            return result;
        }

        @Override
        public AbstractResourceWithQuantity<StorageTypeBulk> fromNBTWithQty(NBTTagCompound nbt)
        {
            return new BulkResourceWithQuantity(nbt);
        }

        @Override
        public IStorageEventFactory<StorageTypeBulk> eventFactory()
        {
            return null;
        }

        @Override
        public LogisticsService<StorageTypeBulk> service()
        {
            return null;
        }

        @Override
        public long transportCapacity(CarrierLevel level, int channel)
        {
            return 0;
        }
    }
}
