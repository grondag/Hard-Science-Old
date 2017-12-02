package grondag.hard_science.simulator.resource;

import java.util.Arrays;
import java.util.HashMap;

import grondag.hard_science.library.varia.ItemHelper;
import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Tracks all unique instances of ItemStacks within managed storage.
 * Allows efficient comparison and memory usage for item
 * resources by eliminating references to duplicate item stacks.<p>
 * 
 * Dynamically reconstructed on load - Item resource must still 
 * be serialized with NBT.
 * 
 * Not needed for other resource types because they are completely 
 * fungible. (No NBT on them, basically.)
 */
public class ItemResourceCache
{
    private static int size = 1024;
    
    private static MetaResourceMap[] items = new MetaResourceMap[size];
    
    /**
     * Resized array so that it is large enough to contain the given index.
     */
    private static void growArray(int toContain)
    {
        synchronized(items)
        {
            while(toContain >= size)
            {
                size *= 2;
            }
            items = Arrays.copyOf(items, size);
        }
    }

    /**
     * Retrieves common instance for this stack, creating it if
     * necessary.  Instance will have a unique transient ID.<p>
     * 
     * Thread-safe, assuming item registry isn't modified
     * after initialization.
     */
    public static ItemResource fromStack(ItemStack stack)
    {
        if(stack == null || stack.isEmpty()) return (ItemResource) StorageType.ITEM.emptyResource;
        
        int itemID = Item.getIdFromItem(stack.getItem());
        if(itemID >= size)
        {
            growArray(itemID);
        }
        return fromItemID(itemID, stack);
    }
    
    private static ItemResource fromItemID(int itemID, ItemStack stack)
    {
        MetaResourceMap metaReference = items[itemID];
        if(metaReference == null)
        {
            synchronized(items)
            {
                metaReference = items[itemID];
                if(metaReference == null)
                {
                    metaReference = new MetaResourceMap(stack);
                    items[itemID] = metaReference;
                }
            }
        }
        
        return metaReference.fromStack(stack);
    }
    
    private static class MetaResourceMap
    {
        Object reference;
        
        int singleReferenceMeta = -1;
        
        private MetaResourceMap(ItemStack stack)
        {
            this.reference = new ItemResourceContainer(stack);
            this.singleReferenceMeta = stack.getMetadata();
        }
        
        
        
        private ItemResource fromStack(ItemStack stack)
        {
            int meta = stack.getMetadata();
            ItemResourceContainer irc = null;
            
            synchronized(this)
            {
                if(this.reference instanceof ItemResourceContainer)
                {
                    // storing a single reference instead of a map
                    
                    if(this.singleReferenceMeta == meta)
                    {
                        // matches single meta value that we reference directly
                        irc = (ItemResourceContainer)this.reference;
                    }
                    else
                    {
                        // need to upgrade to a hashmap
                        Int2ObjectOpenHashMap<ItemResourceContainer> metaMap 
                            = new Int2ObjectOpenHashMap<ItemResourceContainer>();
                        metaMap.put(this.singleReferenceMeta, (ItemResourceContainer)this.reference);
                        this.reference = metaMap;
                        
                        irc = new ItemResourceContainer(stack);
                        metaMap.put(meta, irc);
                    }
                }
                else
                {
                    @SuppressWarnings("unchecked")
                    Int2ObjectOpenHashMap<ItemResourceContainer> metaMap 
                        = (Int2ObjectOpenHashMap<ItemResourceContainer>)this.reference;
                    
                    irc = metaMap.get(meta);
                    if(irc == null)
                    {
                        irc = new ItemResourceContainer(stack);
                        metaMap.put(meta, irc);
                    }
                }
            }
            return irc.fromStack(stack);
        }
    }
    
    
    /**
     * Uses type ambiguity to store references in different
     * structure depending on how many unique item stacks exist
     * for this item/metadata combination. <p>
     * 
     * A single stack instance is stored as a simple reference.
     * 
     * For 2 - 8 instances, we use a simple list with brute 
     * force searching.
     *
     * For more than 8 unique stacks, we use a hash map.
     *
     */
    private static class ItemResourceContainer
    {
        /**
         * Type will depend on how many unique references we contain.
         */
        Object reference;
        
        int uniqueCount = 1;
        
        private ItemResourceContainer(ItemStack stack)
        {
            ItemResource res = resourcefromStack(stack);
            this.reference = res;
        }
        
        private synchronized ItemResource fromStack(ItemStack stack)
        {
            if(uniqueCount == 1)
            {
                final ItemResource res = (ItemResource) this.reference;
                if(res.isStackEqual(stack))
                {
                    // matches the one we have - return existing
                    return res;
                }
                else
                {
                    // not what we have - upgrade to list
                    SimpleUnorderedArrayList<ItemResource> list = new SimpleUnorderedArrayList<ItemResource>();
                    list.add(res);
                    
                    // create and add new item
                    final ItemResource newRes = resourcefromStack(stack);
                    list.add(newRes);
                    this.reference = list;
                    this.uniqueCount++;
                    return newRes;
                }
            }
            else if(uniqueCount < 9)
            {
                // we are using a list - search list for this stack
                @SuppressWarnings("unchecked")
                SimpleUnorderedArrayList<ItemResource> list = (SimpleUnorderedArrayList<ItemResource>) this.reference;
                for(ItemResource res : list)
                {
                    if(res.isStackEqual(stack)) return res;
                }
                
                // not found, create new instance
                final ItemResource newRes = resourcefromStack(stack);
                this.uniqueCount++;
                
                //do we have room to add?
                if(this.uniqueCount < 9)
                {
                    // yup, add to list
                    list.add(newRes);
                }
                else
                {
                    // out of room in list, upgrade to hash set
                    HashMap<ItemResourceKey, ItemResource> map = new HashMap<ItemResourceKey, ItemResource>();
                    for(ItemResource res : list)
                    {
                        map.put(new ItemResourceKey(res.sampleItemStack()), res);
                    }
                    map.put(new ItemResourceKey(newRes.sampleItemStack()), newRes);
                    this.reference = map;
                }
                return newRes;
            }
            else
            {
                // 9 or more, so using hash map
                @SuppressWarnings("unchecked")
                HashMap<ItemResourceKey, ItemResource> map = (HashMap<ItemResourceKey, ItemResource>) this.reference;
                
                ItemResourceKey key = new ItemResourceKey(stack);
                ItemResource res = map.get(key);
                if(res == null)
                {
                    res = resourcefromStack(stack);
                    map.put(key, res);
                    this.uniqueCount++;
                }
                return res;
            }
        }
    }
    
    public static void clear()
    {
        items = new MetaResourceMap[size];
    }
    
    /**
     * Does NOT keep a reference to the given stack.
     */
    private static ItemResource resourcefromStack(ItemStack stack)
    {
        if(stack == null || stack.isEmpty()) return (ItemResource) StorageType.ITEM.emptyResource;
        
        Item item = stack.getItem();
        int meta = stack.getMetadata();
        NBTTagCompound tag = stack.getTagCompound();
        NBTTagCompound caps = ItemHelper.itemCapsNBT(stack);
        
        return new ItemResource(item, meta, tag, caps);
    }
}
