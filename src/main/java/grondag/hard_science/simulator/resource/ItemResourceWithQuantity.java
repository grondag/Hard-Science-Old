package grondag.hard_science.simulator.resource;

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
        return new ItemResourceWithQuantity(ItemResourceCache.fromStack(stack), stack.getCount());
    }
    
    @Override
    public StorageTypeStack storageType()
    {
        return StorageType.ITEM;
    }

    @Override
    public AbstractResourceDelegate<StorageTypeStack> toDelegate()
    {
        return new ItemResourceDelegate((ItemResource) this.resource(), this.quantity);
    }
}
