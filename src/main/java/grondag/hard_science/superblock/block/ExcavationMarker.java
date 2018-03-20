package grondag.hard_science.superblock.block;

import grondag.exotic_matter.model.ISuperBlock;
import grondag.exotic_matter.placement.FilterMode;
import grondag.exotic_matter.placement.PlacementItemFeature;
import grondag.exotic_matter.placement.RegionOrientation;
import grondag.exotic_matter.placement.TargetMode;
import grondag.hard_science.superblock.blockmovetest.PlacementItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 *  Base class for generic tool to mark real-world blocks for removal. 
 *  Has several selection modes.
    All actions are immediately submitted as jobs.
    Re-skin within mods to match theme of mod and add features if appropriate.
 */
public class ExcavationMarker extends Item implements PlacementItem
{
    public static final int FEATURE_FLAGS = PlacementItem.BENUMSET_FEATURES.getFlagsForIncludedValues(
            PlacementItemFeature.FIXED_REGION,
            PlacementItemFeature.REGION_SIZE,
            PlacementItemFeature.FILTER_MODE);
    
    public ExcavationMarker(String name)
    {
        setRegistryName(name); 
        setUnlocalizedName(name);
        this.setMaxStackSize(1);
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        return PlacementItem.super.onItemRightClick(worldIn, playerIn, hand);
    }    

    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        return PlacementItem.super.onItemUse(playerIn, worldIn, pos, hand, facing, hitZ, hitZ, hitZ);
    }

    @Override
    public ISuperBlock getSuperBlock()
    {
        return null;
    }

    @Override
    public int guiOrdinal()
    {
        // TODO enable feature
        return 0;
    }
    
    @Override
    public TargetMode getTargetMode(ItemStack stack)
    {
        return TargetMode.FILL_REGION;
    }

    
    @Override
    public RegionOrientation getRegionOrientation(ItemStack stack)
    {
        return RegionOrientation.XYZ;
    }

    @Override
    public FilterMode getFilterMode(ItemStack stack)
    {
        FilterMode result = PlacementItem.super.getFilterMode(stack);
        return result == FilterMode.REPLACE_ALL || result == FilterMode.REPLACE_SOLID
                ? result : FilterMode.REPLACE_SOLID;
    }

    @Override
    public boolean cycleFilterMode(ItemStack stack, boolean reverse)
    {
        boolean done = false;
        do
        {
          PlacementItem.super.cycleFilterMode(stack, reverse);
          FilterMode result = PlacementItem.super.getFilterMode(stack);
          done = result == FilterMode.REPLACE_ALL || result == FilterMode.REPLACE_SOLID;
        } while(!done);
        return true;
    }

    @Override
    public int featureFlags(ItemStack stack)
    {
        return FEATURE_FLAGS;
    }

    @Override
    public boolean isExcavator(ItemStack placedStack)
    {
        return true;
    }

    @Override
    public boolean isVirtual(ItemStack stack)
    {
        return false;
    }
    
}
