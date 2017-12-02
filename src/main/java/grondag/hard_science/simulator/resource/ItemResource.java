package grondag.hard_science.simulator.resource;


import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import com.google.common.base.Objects;

import grondag.hard_science.library.concurrency.SimpleConcurrentList;
import grondag.hard_science.library.varia.ItemHelper;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Identifier for resources.
 * Instances with same inputs will have same hashCode and return true for equals().
 * Moreover, due to caching, instances with same inputs should always be the same instance.
 */
public class ItemResource extends AbstractResource<StorageType.StorageTypeStack>
{
    private static AtomicInteger nextHandle = new AtomicInteger(0);
    private static final SimpleConcurrentList<ItemResource> ALL_ITEM_RESOURCES = SimpleConcurrentList.create(null);
    public static ItemResource byHandle(int handle)
    {
        if(handle < 0 || handle >= ALL_ITEM_RESOURCES.size()) 
            return ItemResourceCache.fromStack(ItemStack.EMPTY);
        
        return ALL_ITEM_RESOURCES.get(handle);
    }
    
    private final int handle = nextHandle.getAndIncrement();
    private Item item;
    private NBTTagCompound tag;
    private NBTTagCompound caps;
    private int meta;
    
    // lazy instantiate and cache
    private ItemStack stack;
    
    /** 
     * NEVER USE DIRECTLY EXCEPT FOR UNIT TESTING.
     * Use cached instances from ItemResourceCache.
     */
    public ItemResource(Item item, int meta, NBTTagCompound tag, NBTTagCompound caps)
    {
        this.item = item;
        this.meta = meta;
        this.tag = tag;
        this.caps = caps;
        this.stack = null;
        ALL_ITEM_RESOURCES.add(this);
    }
    
    /**
     * Returns a new stack containing one of this item.
     * Will always be a new instance/copy.     */
    public ItemStack sampleItemStack()
    {
        ItemStack stack = this.stack;
        if(stack == null)
        {
            stack = new ItemStack(this.item, 1, this.meta, this.caps);
            if(this.tag != null) stack.setTagCompound(this.tag);
            this.stack = stack;
        }
        return stack.copy();
    }
    
    public Item getItem()
    {
        return this.item;
    }

    public boolean hasTagCompound()
    {
        return this.tag != null;
    }
    
    @Nullable
    public NBTTagCompound getTagCompound()
    {
        return this.tag;
    }

    public int getMetadata()
    {
        return this.meta;
    }
   
    @Override
    public StorageTypeStack storageType()
    {
        return StorageType.ITEM;
    }

    public boolean isStackEqual(ItemStack stack)
    {
        if(stack == null || stack.isEmpty()) return this == StorageType.ITEM.emptyResource;
        
        return stack.getItem() == this.item
            && stack.getMetadata() == this.meta
            && Objects.equal(stack.getTagCompound(), this.tag)
            && Objects.equal(ItemHelper.itemCapsNBT(stack), this.caps);
    }
    
    @Override
    public String displayName()
    {
        return this.stack == null 
                ? this.sampleItemStack().getDisplayName()
                : this.stack.getDisplayName();
    }

    @Override
    public ItemResourceWithQuantity withQuantity(long quantity)
    {
        return new ItemResourceWithQuantity(this, quantity);
    }
    
    @Override
    public String toString()
    {
        return this.displayName();
    }

    @Override
    public int handle()
    {
        return this.handle;
    }

    @Override
    public AbstractResourceDelegate<StorageTypeStack> getDelegate()
    {
        return null;
    }
}
