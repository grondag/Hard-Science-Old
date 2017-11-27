package grondag.hard_science.simulator.resource;

import java.util.Comparator;

import javax.annotation.Nonnull;

import grondag.hard_science.library.serialization.IMessagePlus;
import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.simulator.resource.StorageType.ITypedStorage;
import grondag.hard_science.simulator.storage.IStorage;
import grondag.hard_science.simulator.storage.StorageWithResourceAndQuantity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

public abstract class AbstractResourceWithQuantity<V extends StorageType<V>> implements IReadWriteNBT, IMessagePlus, ITypedStorage<V>
{
    private final IResource<V> resource;
    protected long quantity;
     
    public AbstractResourceWithQuantity(@Nonnull IResource<V> resource, long quantity)
    {
        this.resource = resource;
        this.quantity = quantity;
    }
    
    // needed for IMessage support
    public AbstractResourceWithQuantity()
    {
        this.resource = this.storageType().makeResource(null);
    }
    
    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        this.resource.serializeNBT(tag);
        tag.setLong(ModNBTTag.RESOURCE_QUANTITY, this.quantity);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        this.quantity = nbt.getLong(ModNBTTag.RESOURCE_QUANTITY);
        this.resource.deserializeNBT(nbt);
    }

    @Override
    public void fromBytes(PacketBuffer pBuff)
    {
        this.quantity = pBuff.readLong();
        this.resource.fromBytes(pBuff);
    }

    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeLong(this.quantity);
        this.resource.toBytes(pBuff);
    }
    
    public IResource<V> resource()
    {
        return this.resource;
    }

    public long getQuantity()
    {
        return this.quantity;
    }
    
    public void setQuanity(long quantity)
    {
        this.quantity = quantity;
    }
    
    /**
     * returns new value
     */
    public long changeQuantity(long delta)
    {
        this.quantity += delta;
        return this.quantity;
    }

    public boolean isEmpty()
    {
        return this.quantity == 0;
    }

    /**
     * Takes up to limit from this stack and returns how many were actually taken.
     * Intended to be thread-safe.
     */
    public synchronized long takeUpTo(long limit)
    {
        if(limit < 1) return 0;
        
        long taken = Math.min(this.quantity, limit);
        this.quantity -= taken;
        return taken;
    }

    /**
     * Increases quantity and returns quantity actually added.
     * Intended to be thread-safe.
     */
    public synchronized long add(long howMany)
    {
        if(howMany < 1) return 0;
        
        this.quantity += howMany;
        
        return howMany;
    }

    public StorageWithResourceAndQuantity<V> withStorage(IStorage<V> storage)
    {
        return new StorageWithResourceAndQuantity<V>(storage, this.resource, this.quantity);
    }

    @Override
    public String toString()
    {
        return String.format("%,d x ", this.getQuantity()) + this.resource.toString();
    }
    
    @Override
    public AbstractResourceWithQuantity<V> clone()
    {
        return this.resource.withQuantity(quantity);
    }
    
    /////////////////////////////////////////
    // SORTING UTILITIES
    /////////////////////////////////////////
    
    public static final Comparator<AbstractResourceWithQuantity<?>> SORT_BY_NAME_ASC = new Comparator<AbstractResourceWithQuantity<?>>()
    {
        @Override
        public int compare(AbstractResourceWithQuantity<?> o1, AbstractResourceWithQuantity<?> o2)
        {
            if(o1 == null)
            {
                if(o2 == null) 
                {
                    return 0;
                }
                return 1;
            }
            else if(o2 == null) 
            {
                return -1;
            }
            
            String s1 = o1.resource().displayName();
            String s2 = o2.resource().displayName();
            return s1.compareTo(s2);
        }
    };
    
    public static final Comparator<AbstractResourceWithQuantity<?>> SORT_BY_NAME_DESC = new Comparator<AbstractResourceWithQuantity<?>>()
    {
        @Override
        public int compare(AbstractResourceWithQuantity<?> o1, AbstractResourceWithQuantity<?> o2)
        {
            return SORT_BY_NAME_ASC.compare(o2, o1);
        }
    };
    
    public static final Comparator<AbstractResourceWithQuantity<?>> SORT_BY_QTY_ASC = new Comparator<AbstractResourceWithQuantity<?>>()
    {
        @Override
        public int compare(AbstractResourceWithQuantity<?> o1, AbstractResourceWithQuantity<?> o2)
        {   
            if(o1 == null)
            {
                if(o2 == null) 
                {
                    return 0;
                }
                return  1;
            }
            else if(o2 == null) 
            {
                return -1;
            }
            int result = Long.compare(o1.getQuantity(), o2.getQuantity());
            return result == 0 ? SORT_BY_NAME_ASC.compare(o1, o2) : result;
        }
    };
    
    public static final Comparator<AbstractResourceWithQuantity<?>> SORT_BY_QTY_DESC = new Comparator<AbstractResourceWithQuantity<?>>()
    {
        @Override
        public int compare(AbstractResourceWithQuantity<?> o1, AbstractResourceWithQuantity<?> o2)
        {
            return SORT_BY_QTY_ASC.compare(o2, o1);
        }
    };
    
    //FIXME: localize
    public static final int SORT_COUNT = 4;
    public static final String[] SORT_LABELS = {"A-Z", "Z-A", "1-2-3", "3-2-1" };
    @SuppressWarnings("rawtypes")
    public static final Comparator[] SORT = { SORT_BY_NAME_ASC, SORT_BY_NAME_DESC, SORT_BY_QTY_ASC, SORT_BY_QTY_DESC };
}
