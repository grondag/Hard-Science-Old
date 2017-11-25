package grondag.hard_science.superblock.items;

import javax.annotation.Nullable;

import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.block.SuperTileEntity;
import grondag.hard_science.superblock.placement.PlacementItem;
import grondag.hard_science.superblock.placement.PlacementItemFeature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Provides sub-items and handles item logic for NiceBlocks.
 */
public class SuperItemBlock extends ItemBlock implements PlacementItem
{
    
    public static final int FEATURE_FLAGS = PlacementItem.BENUMSET_FEATURES.getFlagsForIncludedValues(
            PlacementItemFeature.BLOCK_ORIENTATION,
            PlacementItemFeature.SPECIES_MODE);
    
    /**
     * Called client-side before {@link #onItemUse(EntityPlayer, World, BlockPos, EnumHand, EnumFacing, float, float, float)}.  
     * If returns false for an itemBlock that method will never be called.
     * We do all of our "can we put there here" checks in that method, so we always return true.
     */
    @Override
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack)
    {
        return true;
    }

    public SuperItemBlock(SuperBlock block)
    {
        super(block);
        setHasSubtypes(true);
    }
    
    @Override
    public int featureFlags(ItemStack stack)
    {
        return FEATURE_FLAGS;
    }
    
    @Override
    public int getMetadata(int damage)
    {
        return damage;
    }
    
//    public List<String> getWailaBody(ItemStack itemStack, List<String> current tip, IWailaDataAccessor accessor, IWailaConfigHandler config)
//    {
//        List<String> retVal = new ArrayList<String>();
//        this.addInformation(itemStack, null, retVal, false);
//        return retVal;
//    }



    /**
     * <i>Grondag: don't want to overflow size limits or burden 
     * network by sending details of embedded storage that will 
     * never be used on the client anyway.</i><br><br>
     * 
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public final NBTTagCompound getNBTShareTag(ItemStack stack)
    {
        return SuperTileEntity.withoutServerTag(super.getNBTShareTag(stack));
    }
    
    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        return ((SuperBlock)this.block).getItemStackDisplayName(stack);
    }
    
    @Override
    public boolean isVirtual(ItemStack stack)
    {
        return false;
    }

    @Override
    public SuperBlock getSuperBlock()
    {
        return (SuperBlock) this.block;
    }

    @Override
    public boolean isExcavator(ItemStack placedStack)
    {
        return false;
    }

    // override is necessary because will use ItemBlock implementation
    // instead of PlacementItem implementation without override
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        return PlacementItem.super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }

    // override is necessary because will use ItemBlock implementation
    // instead of PlacementItem implementation without override
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
    {
        return PlacementItem.super.onItemRightClick(worldIn, playerIn, handIn);
    }
    
    
}
