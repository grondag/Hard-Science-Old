package grondag.hard_science.matter;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import grondag.exotic_matter.init.IItemModelRegistrant;
import grondag.exotic_matter.serialization.NBTDictionary;
import grondag.hard_science.HardScience;
import grondag.hard_science.crafting.BulkItemInput;
import grondag.hard_science.matter.MassUnits.Mass;
import grondag.hard_science.simulator.resource.BulkResource;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;

/**
 * Package sizes for solid material blocks
 * and fluid containers. 
 */
@Deprecated
public class BulkItem extends Item  implements IItemModelRegistrant
{
    private static final String NBT_BULK_ITEM_NL = NBTDictionary.claim("bulkItemNL");

    private static final ListMultimap<BulkResource, BulkItem> resourceMap = ArrayListMultimap.create();
    
    public static List<BulkItem> itemsForResource(BulkResource resource)
    {
        return resourceMap.get(resource);
    }
    
    public final long maxNanoLiters;
    public final long maxNanoGrams;
    public final BulkResource matter;
    
    public BulkItem(String itemName, BulkResource matter, long maxNanoLiters)
    {
        super();
        this.matter = matter;
        this.maxNanoLiters = maxNanoLiters;
        this.maxNanoGrams = (long) (matter.density() * this.maxNanoLiters * 1000);
        this.setCreativeTab(HardScience.tabMod);
        this.setRegistryName(itemName);
        this.setUnlocalizedName(itemName);
        this.setMaxDamage(1000000);
        resourceMap.put(matter, this);
        BulkItemInput.add(new BulkItemInput.Container(this));
    }
    
    public BulkItem(String itemName, BulkResource matter, Mass mass)
    {
        this(itemName, matter, (long) (mass.nanoGrams() / matter.density() / 1000));
    }

    /**
     * Does NOT multiply by stack quantity.
     */
    public long getNanoLiters(ItemStack stack)
    {
        if(stack.getItem() != this) return 0;
        return stack.hasTagCompound()
                ? stack.getTagCompound().getLong(NBT_BULK_ITEM_NL)
                : this.maxNanoLiters;
    }
    
    public void setNanoLiters(ItemStack stack, long nl)
    {
        if(stack.getItem() != this) return;
        NBTTagCompound tag = stack.getTagCompound();
        if(tag == null)
        {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        tag.setLong(NBT_BULK_ITEM_NL, nl);
    }
    
    public long getNanoGrams(ItemStack stack)
    {
        if(stack.getItem() != this) return 0;
        return (long) (this.matter.density() * this.getNanoLiters(stack) * 1000);
    }
    
    public void setNanoGrams(ItemStack stack, long ng)
    {
        this.setNanoLiters(stack, (long) (ng / matter.density() / 1000));
    }
    
    @Override
    public int getDamage(@Nonnull ItemStack stack)
    {
        return (int) (1000000 * (this.maxNanoLiters - this.getNanoLiters(stack)) / this.maxNanoLiters);
    }

    @Override
    public int getRGBDurabilityForDisplay(@Nonnull ItemStack stack)
    {
        return 0x80FFFF;
//        return super.getRGBDurabilityForDisplay(stack);
//        return 0xFFFFFF & this.matter.color;
    }

    @Override
    public String getItemStackDisplayName(@Nonnull ItemStack stack)
    {
        return I18n.translateToLocal("matter." + this.matter.systemName().toLowerCase()).trim();
    }
    
    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn)
    {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if(this.matter.phase() == MatterPhase.SOLID)
            tooltip.add(MassUnits.formatMass(this.getNanoGrams(stack), false));
        else
            tooltip.add(VolumeUnits.formatVolume(this.getNanoLiters(stack), false));
        
    }

    @Override
    public boolean showDurabilityBar(@Nonnull ItemStack stack)
    {
        return true;
    }

    public ItemStack withNanoLiters(long nanoLiters)
    {
        ItemStack result = new ItemStack(this);
        this.setNanoLiters(result, nanoLiters);
        return result;
    }

    @Override
    public void handleBake(ModelBakeEvent event)
    {
        event.getModelRegistry().putObject(new ModelResourceLocation(this.getRegistryName(), "inventory"),
                new MatterCubeItemModel1((BulkItem) this));        
    }
    
    
    
}
