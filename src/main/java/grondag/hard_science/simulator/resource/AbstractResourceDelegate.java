package grondag.hard_science.simulator.resource;

import java.io.IOException;
import java.util.Comparator;

import grondag.hard_science.library.serialization.IMessagePlus;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

/**
 * Used for client representation of resources. Test match
 * with server-side resources using the handle.
 * 
 * Includes a quantityIn directly instead of in a sub class
 * because uses cases for resource delegates typically require it.
 */
public abstract class AbstractResourceDelegate<V extends StorageType<V>> implements IMessagePlus, ITypedStorage<V>
{
    /**
     * For IMessage support
     */
    protected AbstractResourceDelegate() {};
    
    protected AbstractResourceDelegate(int handle, long quantity, ItemStack displayStack)
    {
        this.handle = handle;
        this.quantity = quantity;
        this.displayStack = displayStack == null ? ItemStack.EMPTY : displayStack.copy();
    }
    
    private long quantity = 0;
    private int handle;
    private ItemStack displayStack;
    
    /**
     * Delegate equality test is by handle instead of by instance and
     * does include quantityIn.
     */
    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object other)
    {
        return this == other
                || (other != null && other.getClass() == this.getClass() 
                && (   ((AbstractResourceDelegate)other).handle() == this.handle()
                    && ((AbstractResourceDelegate)other).quantity == this.quantity));
    }

    public long quantity()
    {
        return quantity;
    }

    public int handle()
    {
        return this.handle;
    }
    
    @Override
    public int hashCode()
    {
        return this.handle();
    }
    
    @Override
    public void fromBytes(PacketBuffer pBuff)
    {
        this.quantity = pBuff.readLong();
        this.handle = pBuff.readInt();
        try
        {
            this.displayStack = pBuff.readItemStack();
        }
        catch (IOException e)
        {
            this.displayStack = ItemStack.EMPTY;
        }
    }

    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeLong(this.quantity);
        pBuff.writeInt(this.handle);
        pBuff.writeItemStack(this.displayStack);
    }

    public ItemStack displayStack()
    {
        return this.displayStack;
    }
    
    /////////////////////////////////////////
    // SORTING UTILITIES
    /////////////////////////////////////////
    
    public static final Comparator<AbstractResourceDelegate<?>> SORT_BY_NAME_ASC = new Comparator<AbstractResourceDelegate<?>>()
    {
        @Override
        public int compare(AbstractResourceDelegate<?> o1, AbstractResourceDelegate<?> o2)
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
            
            String s1 = o1.displayStack.getDisplayName();
            String s2 = o2.displayStack.getDisplayName();
            return s1.compareTo(s2);
        }
    };
    
    public static final Comparator<AbstractResourceDelegate<?>> SORT_BY_NAME_DESC = new Comparator<AbstractResourceDelegate<?>>()
    {
        @Override
        public int compare(AbstractResourceDelegate<?> o1, AbstractResourceDelegate<?> o2)
        {
            return SORT_BY_NAME_ASC.compare(o2, o1);
        }
    };
    
    public static final Comparator<AbstractResourceDelegate<?>> SORT_BY_QTY_ASC = new Comparator<AbstractResourceDelegate<?>>()
    {
        @Override
        public int compare(AbstractResourceDelegate<?> o1, AbstractResourceDelegate<?> o2)
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
            int result = Long.compare(o1.quantity, o2.quantity);
            return result == 0 ? SORT_BY_NAME_ASC.compare(o1, o2) : result;
        }
    };
    
    public static final Comparator<AbstractResourceDelegate<?>> SORT_BY_QTY_DESC = new Comparator<AbstractResourceDelegate<?>>()
    {
        @Override
        public int compare(AbstractResourceDelegate<?> o1, AbstractResourceDelegate<?> o2)
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
