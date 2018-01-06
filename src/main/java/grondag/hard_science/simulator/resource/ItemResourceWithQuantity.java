package grondag.hard_science.simulator.resource;

import java.util.Comparator;

import javax.annotation.Nonnull;

import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemResourceWithQuantity extends AbstractResourceWithQuantity<StorageType.StorageTypeStack>
{

    public ItemResourceWithQuantity(@Nonnull ItemResource resource, long quantity)
    {
        super(resource, quantity);
    }
    
    public ItemResourceWithQuantity()
    {
        super();
    }
    
    public ItemResourceWithQuantity(NBTTagCompound tag)
    {
        super(tag);
    }
    
    public static ItemResourceWithQuantity fromStack(ItemStack stack)
    {
        if(stack == null || stack.isEmpty()) return (ItemResourceWithQuantity) StorageType.ITEM.emptyResource.withQuantity(0);
        return new ItemResourceWithQuantity(ItemResource.fromStack(stack), stack.getCount());
    }
    
    @Override
    public StorageTypeStack storageType()
    {
        return StorageType.ITEM;
    }
    
    public static final Comparator<? super AbstractResourceWithQuantity<StorageTypeStack>> 
        SORT_BY_QTY_ASC = new Comparator<AbstractResourceWithQuantity<StorageTypeStack>>()
    {
        @Override
        public int compare(AbstractResourceWithQuantity<StorageTypeStack> o1, AbstractResourceWithQuantity<StorageTypeStack> o2)
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
            return result == 0 ? o1.resource().displayName().compareTo(o2.resource().displayName()) : result;
        }
    };
    
    public static final Comparator<? super AbstractResourceWithQuantity<StorageTypeStack>> 
        SORT_BY_QTY_DESC = new Comparator<AbstractResourceWithQuantity<StorageTypeStack>>()
    {
        @Override
        public int compare(AbstractResourceWithQuantity<StorageTypeStack> o1, AbstractResourceWithQuantity<StorageTypeStack> o2)
        {
            return SORT_BY_QTY_ASC.compare(o2, o1);
        }
        
    };
}
