package grondag.hard_science.simulator.base;

import javax.annotation.Nonnull;

import grondag.hard_science.simulator.base.StorageType.StorageTypeStack;
import net.minecraft.item.ItemStack;

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
}
