package grondag.hard_science.crafting;

import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;

import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.matter.BulkItem;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.resource.BulkResource;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.resource.ItemResourceWithQuantity;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.placement.PlacementItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;

/**
 * Represents conversion from a non-fluid, non-bulk item to a bulk resource.
 *
 */
@Deprecated
public abstract class BulkItemInput
{
    private static final ListMultimap<BulkResource, BulkItemInput> inputs = ArrayListMultimap.create();

    public static void add(BulkItemInput input)
    {
        inputs.put(input.bulkResource(), input);
    }
    
    public static List<BulkItemInput> inputsForResource(BulkResource resource)
    {
        return inputs.get(resource);
    }
    
    public static List<BulkItemInput> inputsForItem(IResource<StorageTypeStack> resource)
    {
        return inputsForItem(((ItemResource)resource).sampleItemStack());
    }
    
    public static List<BulkItemInput> inputsForItem(ItemStack stack)
    {
        return inputs.values().stream()
                .filter(i -> i.ingredient.apply(stack))
                .collect(ImmutableList.toImmutableList());
    }
    
    protected final BulkResource bulkResource;
    protected final Ingredient ingredient;
    
    protected BulkItemInput(
            BulkResource resource, 
            Ingredient ingredient)
    {
        this.bulkResource = resource;
        this.ingredient = ingredient;
    }
    
    public BulkResource bulkResource()
    {
        return this.bulkResource;
    }
    
    public Ingredient ingredient()
    {
        return this.ingredient;
    }

    
    /**
     * Returns the nanoLiters loaded into a bulk container 
     * for the given input.
     */
    public abstract long input(ItemResourceWithQuantity rwq);
    
    /**
     * Use when all ingredient matches have the same conversion value.
     */
    public static class PerItem extends BulkItemInput
    {
        private final long nlPerItem;
        
        public PerItem(BulkResource bulkResource, Ingredient ingredient, long nlPerItem)
        {
            super(bulkResource, ingredient);
            this.nlPerItem = nlPerItem;
        }

        @Override
        public long input(ItemResourceWithQuantity rwq)
        {
            return this.ingredient.apply(rwq.toStack())
                    ? rwq.getQuantity() * this.nlPerItem
                    : 0;
        }
    }
    
    /**
     * Converts terrain blocks (obtained with silk touch)
     * to given bulk resource based on volume of the block.
     */
    public static class Terrain extends BulkItemInput
    {
        public Terrain(BulkResource bulkResource, Item fromItem)
        {
            super(bulkResource, Ingredient.fromItem(fromItem));
        }

        @Override
        public long input(ItemResourceWithQuantity rwq)
        {
            if(!this.ingredient.apply(rwq.toStack())) return 0;
            
            double volume = 0;
            ModelState modelState = PlacementItem.getStackModelState(rwq.toStack());
            if(modelState == null) return 0;
            for(AxisAlignedBB box : modelState.getShape().meshFactory().collisionHandler().getCollisionBoxes(modelState))
            {
                volume += Useful.volumeAABB(box);
            }
            return (long) (MathHelper.clamp(volume, 0, 1) * VolumeUnits.KILOLITER.nL);
        }
    }
    
    /**
     * Converts bulk container items 
     * to given bulk resource based on volume in the item.
     */
    public static class Container extends BulkItemInput
    {
        private final BulkItem item;
        public Container(BulkItem item)
        {
            super(item.matter, Ingredient.fromItem(item));
            this.item = item;
        }

        @Override
        public long input(ItemResourceWithQuantity rwq)
        {
            if(!this.ingredient.apply(rwq.toStack())) return 0;
            
            return this.item.getNanoLiters(rwq.toStack()) * rwq.getQuantity();
        }
    }
}
