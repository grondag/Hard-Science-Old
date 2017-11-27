package grondag.hard_science.simulator.resource;


import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Objects;

import grondag.hard_science.Log;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

/**
 * Identifier for resources.
 * Instances with same inputs will have same hashCode and return true for equals().
 */
public class ItemResource extends AbstractResource<StorageType.StorageTypeStack>
{
    private Item item;
    private NBTTagCompound tag;
    private NBTTagCompound caps;
    private int meta;
    private int hash = -1;
    
    // lazy instantiate and cache
    private ItemStack stack;
    
    /** 
     * This version is mainly for unit testing.
     */
    public ItemResource(Item item, int meta, NBTTagCompound tag, NBTTagCompound caps)
    {
        this.item = item;
        this.meta = meta;
        this.tag = tag;
        this.caps = caps;
        this.stack = null;
    }
    
    /**
     * Use this for resources saved via {@link #serializeNBT()}
     */
    public ItemResource(@Nullable NBTTagCompound nbt)
    {
        super(nbt);
    }
   
    /**
     * Does NOT keep a reference to the given stack.
     */
    public static ItemResource fromStack(ItemStack stack)
    {
        if(stack == null || stack.isEmpty()) return (ItemResource) StorageType.ITEM.emptyResource;
        
        Item item = stack.getItem();
        int meta = stack.getMetadata();
        NBTTagCompound tag = stack.getTagCompound();

        NBTTagCompound caps;
        if(stack.areCapsCompatible(ItemStack.EMPTY))
        {
            caps = null;
        }
        {
            // See what you make us do, Lex?
            NBTTagCompound lookForCaps = stack.serializeNBT();
            caps = lookForCaps.hasKey("ForgeCaps") ? lookForCaps.getCompoundTag("ForgeCaps") : null;
        }
        
        return new ItemResource(item, meta, tag, caps);
    }
    
    /**
     * Returns a new stack containing one of this item.
     * For performance sake, may be the same instance returned by earlier calls.
     */
    public ItemStack sampleItemStack()
    {
        ItemStack stack = this.stack;
        if(stack == null)
        {
            stack = new ItemStack(this.item, 1, this.meta, this.caps);
            if(this.tag != null) stack.setTagCompound(this.tag);
            this.stack = stack;
        }
        return stack;
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

    @Override
    public int computeResourceHashCode()
    {
        int h = this.hash;
        if(h == -1)
        {
            h = this.item == null ? 0 : this.item.hashCode();
            
            if(this.tag != null)
            {
                h = h * 7919 + this.tag.hashCode();
            }
            
            if(this.caps != null)
            {
                h = h * 7919 + this.caps.hashCode();
            }
            
            if(this.meta != 0) 
            {
                h = h * 7919 + this.meta;
            }
            
            this.hash = h;
        }
        return h;
    }

    @Override
    public boolean isResourceEqual(IResource<StorageTypeStack> other)
    {
        if(other == this) return true;
        if(other == null) return false;
        if(other instanceof ItemResource)
        {
            ItemResource resource = (ItemResource)other;
            return resource.item == this.item
                && resource.meta == this.meta
                && Objects.equal(resource.tag, this.tag)
                && Objects.equal(resource.caps, this.caps);
        }
        return false;
    }

    @Override
    public void serializeNBT(@Nonnull NBTTagCompound nbt)
    {
        nbt.setInteger(ModNBTTag.ITEM_RESOURCE_ITEM, Item.getIdFromItem(this.item));
        nbt.setInteger(ModNBTTag.ITEM_RESOURCE_META, this.meta);
        if(this.tag != null) nbt.setTag(ModNBTTag.ITEM_RESOURCE_STACK_TAG, this.tag);
        if(this.caps != null) nbt.setTag(ModNBTTag.ITEM_RESOURCE_STACK_CAPS, this.caps);
    }

    @Override
    public void deserializeNBT(@Nonnull NBTTagCompound nbt)
    {
        this.item = Item.getItemById(nbt.getInteger(ModNBTTag.ITEM_RESOURCE_ITEM));
        this.meta = nbt.getInteger(ModNBTTag.ITEM_RESOURCE_META);
        this.tag = nbt.hasKey(ModNBTTag.ITEM_RESOURCE_STACK_TAG) ? nbt.getCompoundTag(ModNBTTag.ITEM_RESOURCE_STACK_TAG) : null;
        this.caps = nbt.hasKey(ModNBTTag.ITEM_RESOURCE_STACK_CAPS) ? nbt.getCompoundTag(ModNBTTag.ITEM_RESOURCE_STACK_CAPS) : null;
        this.hash = -1;
        this.stack = null;
    }

    @Override
    public void fromBytes(PacketBuffer buf)
    {
        this.item = Item.getItemById(buf.readInt());
        this.meta = buf.readInt();
        try
        {
            this.tag = buf.readCompoundTag();
        }
        catch (IOException e)
        {
            Log.warn("Error reading storage packet");
            e.printStackTrace();
            this.tag = null;
        }
        try
        {
            this.caps = buf.readCompoundTag();
        }
        catch (IOException e)
        {
            Log.warn("Error reading storage packet");
            e.printStackTrace();
            this.caps = null;
        }
        this.hash = -1;
        this.stack = null;
    }

    @Override
    public void toBytes(PacketBuffer buf)
    {
        buf.writeInt(Item.getIdFromItem(this.item));
        buf.writeInt(this.meta);
        buf.writeCompoundTag(this.tag);
        buf.writeCompoundTag(this.caps);
    }
    
    @Override
    public String displayName()
    {
        return this.sampleItemStack().getDisplayName();
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
}
