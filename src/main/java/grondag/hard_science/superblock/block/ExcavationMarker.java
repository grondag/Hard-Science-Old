package grondag.hard_science.superblock.block;

import javax.annotation.Nonnull;

import grondag.exotic_matter.block.ISuperBlock;
import grondag.exotic_matter.placement.FilterMode;
import grondag.exotic_matter.placement.IPlacementItem;
import grondag.exotic_matter.placement.PlacementItemFeature;
import grondag.exotic_matter.placement.RegionOrientation;
import grondag.exotic_matter.placement.TargetMode;
import grondag.hard_science.superblock.placement.spec.PlacementHandler;
import grondag.hard_science.superblock.placement.spec.PlacementResult;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 *  Base class for generic tool to mark real-world blocks for removal. 
 *  Has several selection modes.
    All actions are immediately submitted as jobs.
    Re-skin within mods to match theme of mod and add features if appropriate.
 */
public class ExcavationMarker extends Item implements IPlacementItem
{
    public static final int FEATURE_FLAGS = IPlacementItem.BENUMSET_FEATURES.getFlagsForIncludedValues(
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
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, @Nonnull EntityPlayer player, @Nonnull EnumHand hand)
    {
       //TODO: logic here is too elaborate for what this item does, and probably needed by Virtual Block
        
       if(player == null) return new ActionResult<>(EnumActionResult.PASS, null);
        
        ItemStack stackIn = player.getHeldItem(hand);
        
        if (stackIn.isEmpty() || stackIn.getItem() != this) return new ActionResult<>(EnumActionResult.PASS, stackIn);
        
        PlacementResult result = PlacementHandler.doRightClickBlock(player, null, null, null, stackIn, this);
        
        if(!result.shouldInputEventsContinue()) 
        {
            result.apply(stackIn, player);
//            this.doPlacements(result, stackIn, world, player);
            return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
        }
        
        if (world.isRemote) 
        {
            BlockPos blockpos = Minecraft.getMinecraft().objectMouseOver.getBlockPos();
            //if trying to place a block but too close, is annoying to get GUI
            //so only display if clicking on air
            if (blockpos != null 
                    && !ISuperBlock.isVirtualBlock(world.getBlockState(blockpos).getBlock())
                    && world.getBlockState(blockpos).getMaterial().isReplaceable()
                    && this.isVirtual(stackIn))
            {
                this.displayGui(player);
                return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
            }
        }
        
        return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
    }    

    @Override
    public EnumActionResult onItemUse(@Nonnull EntityPlayer playerIn, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ)
    {
      //TODO: logic here is too elaborate for what this item does, and probably needed by Virtual Block
        
        if(playerIn == null) return EnumActionResult.FAIL;
        
        ItemStack stackIn = playerIn.getHeldItem(hand);
        if (stackIn.isEmpty() || stackIn.getItem() != this) return EnumActionResult.FAIL;
        
        PlacementResult result = PlacementHandler.doRightClickBlock(playerIn, pos, facing, new Vec3d(hitX, hitY, hitZ), stackIn, this);
        
        result.apply(stackIn, playerIn);
        
        // we don't return pass because don't want GUI displayed or other events to process
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ISuperBlock getSuperBlock()
    {
        return null;
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
        FilterMode result = IPlacementItem.super.getFilterMode(stack);
        return result == FilterMode.REPLACE_ALL || result == FilterMode.REPLACE_SOLID
                ? result : FilterMode.REPLACE_SOLID;
    }

    @Override
    public boolean cycleFilterMode(ItemStack stack, boolean reverse)
    {
        boolean done = false;
        do
        {
          IPlacementItem.super.cycleFilterMode(stack, reverse);
          FilterMode result = IPlacementItem.super.getFilterMode(stack);
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
