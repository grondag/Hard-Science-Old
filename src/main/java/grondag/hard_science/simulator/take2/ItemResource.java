package grondag.hard_science.simulator.take2;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.library.cache.ObjectSimpleCacheLoader;
import grondag.hard_science.library.render.QuadBakery;
import grondag.hard_science.library.render.RawQuad;
import grondag.hard_science.library.render.SimpleItemBlockModel;
import grondag.hard_science.simulator.take2.StorageType.StorageTypeStack;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;

public class ItemResource extends AbstractResource<StorageType.StorageTypeStack>
{
    
    private static final 
    
    private class ItemResourceLoader implements ObjectSimpleCacheLoader<ItemResource, ItemResource>
    {      
        @Override
        public ItemResource load(ItemResource key)
        {
            return key;
        }       
    }
    
    private final Item item;
    private final NBTTagCompound tag;
    private final int meta;
    private int hash = -1;
    
    private ItemResource(AbstractResourceBroker<StorageTypeStack> broker, Item item, int meta, NBTTagCompound tag)
    {
        super(broker);
        this.item = item;
        this.meta = meta;
        this.tag = tag;
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
        if(this.hash == -1)
        {
            int h = this.item == null ? 0 : this.item.hashCode();
            
            if(this.tag != null)
            {
                h = h * 7919 + this.tag.hashCode();
            }
            
            if(this.meta != 0) 
            {
                h = h * 7919 + this.meta;
            }
            
            this.hash = h;
            return h;
        }
        else
        {
            return this.hash;
        }
    }

    @Override
    public boolean isResourceEqual(IResource<StorageTypeStack> other)
    {
        if(other == this) return true;
        if(other instanceof ItemResource)
        {
            ItemResource resource = (ItemResource)other;
            if(resource.item != this.item) return false;
            if(resource.meta != this.meta) return false;
            if(this.tag == null)
            {
                if(resource.tag != null) return false;
            }
            else
            {
                if(!this.tag.equals(resource.tag)) return false;
            }
            return true;
        }
        return false;
    }

}
