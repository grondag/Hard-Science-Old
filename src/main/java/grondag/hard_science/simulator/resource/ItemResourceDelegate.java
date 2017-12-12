package grondag.hard_science.simulator.resource;

import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import net.minecraft.item.ItemStack;

public class ItemResourceDelegate extends AbstractResourceDelegate<StorageType.StorageTypeStack>
{
    public static final ItemResourceDelegate EMPTY 
     = new ItemResourceDelegate(-1, ItemResource.fromStack(ItemStack.EMPTY), 0);    
    
    public ItemResourceDelegate()
    {
        super();
    }
    
    public ItemResourceDelegate(int handle, ItemResource resource, long quantity)
    {
        super(handle, quantity, resource.sampleItemStack());
    }
    
    @Override
    public StorageTypeStack storageType()
    {
        return StorageType.ITEM;
    }

    @Override
    public String toString()
    {
        return String.format("%,d x ", this.quantity()) + this.displayStack().getDisplayName();
    }
}
