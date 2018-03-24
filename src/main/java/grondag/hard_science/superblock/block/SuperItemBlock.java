package grondag.hard_science.superblock.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.exotic_matter.block.SuperBlock;
import grondag.exotic_matter.block.SuperBlockStackHelper;
import grondag.exotic_matter.block.SuperTileEntity;
import grondag.exotic_matter.model.ISuperBlock;
import grondag.exotic_matter.model.ISuperModelState;
import grondag.exotic_matter.placement.PlacementItemFeature;
import grondag.exotic_matter.placement.PlacementPosition;
import grondag.hard_science.superblock.placement.spec.BlockOrientationHandler;
import grondag.hard_science.superblock.placement.spec.PlacementItem;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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
    public boolean canPlaceBlockOnSide(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumFacing side, @Nonnull EntityPlayer player, @Nonnull ItemStack stack)
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
    public final NBTTagCompound getNBTShareTag(@Nonnull ItemStack stack)
    {
        return SuperTileEntity.withoutServerTag(super.getNBTShareTag(stack));
    }
    
    @Override
    public String getItemStackDisplayName(@Nonnull ItemStack stack)
    {
        return ((ISuperBlock)this.block).getItemStackDisplayName(stack);
    }
    
    @Override
    public boolean isVirtual(ItemStack stack)
    {
        return false;
    }

    @Override
    public ISuperBlock getSuperBlock()
    {
        return (ISuperBlock) this.block;
    }

    @Override
    public boolean isExcavator(ItemStack placedStack)
    {
        return false;
    }

    /**
    * Called to actually place the block, after the location is determined
    * and all permission checks have been made.
    *
    * @param stack The item stack that was used to place the block. This can be changed inside the method.
    * @param player The player who is placing the block. Can be null if the block is not being placed by a player.
    * @param side The side the player (or machine) right-clicked on.
    */
   @Override
   public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull IBlockState newState)
   { 
       // world.setBlockState returns false if the state was already the requested state
       // this is OK normally, but if we need to update the TileEntity it is the opposite of OK
       boolean wasUpdated = world.setBlockState(pos, newState, 3)
               || world.getBlockState(pos) == newState;
           
       if(!wasUpdated) 
           return false;
       
       this.block.onBlockPlacedBy(world, pos, newState, player, stack);
       return true;
   }

   @Override
   public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, @Nonnull EntityPlayer player, @Nonnull EnumHand hand)
   {
       return new ActionResult<ItemStack>(EnumActionResult.PASS, player.getHeldItem(hand));
   }
   
   @Override
   public EnumActionResult onItemUse(@Nonnull EntityPlayer playerIn, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ)
   {
       ItemStack stackIn = playerIn.getHeldItem(hand);

       // NB: block is to limit scope of vars
       {
           IBlockState currentState = worldIn.getBlockState(pos);
           Block block = currentState.getBlock();
    
           if (!block.isReplaceable(worldIn, pos))
           {
               if(currentState.getBlockFaceShape(worldIn, pos, facing) 
                       == BlockFaceShape.UNDEFINED && !playerIn.isSneaking())
                   return EnumActionResult.FAIL;
               
               pos = pos.offset(facing);
           }
    
           currentState = worldIn.getBlockState(pos);
           block = currentState.getBlock();
    
           // this check will probably need to be adjusted for additive blocks
           // but not having this for normal blocks means we replace cables or
           // other block that don't fully occlude the targeted face.
           if(!block.isReplaceable(worldIn, pos)) return EnumActionResult.FAIL;
           
           if (stackIn.isEmpty() || !playerIn.canPlayerEdit(pos, facing, stackIn)) 
               return EnumActionResult.FAIL;
           
       }
       
   
       ISuperModelState modelState = SuperBlockStackHelper.getStackModelState(stackIn);
       if(modelState == null) return EnumActionResult.FAIL;

       AxisAlignedBB axisalignedbb = modelState.getShape().meshFactory().collisionHandler()
               .getCollisionBoundingBox(modelState);
       
       if(!worldIn.checkNoEntityCollision(axisalignedbb.offset(pos)))
           return EnumActionResult.FAIL;

       IBlockState placedState = PlacementItem.getPlacementBlockStateFromStackStatically(stackIn);
       
       /**
        * Adjust block rotation if supported.
        */
       ItemStack placedStack = stackIn.copy();
       if(!modelState.isStatic())
       {
           PlacementItem item = (PlacementItem)placedStack.getItem();
           
           BlockOrientationHandler.applyDynamicOrientation(placedStack, playerIn, 
                   new PlacementPosition(playerIn, pos, facing, new Vec3d(hitX, hitY, hitZ), item.getFloatingSelectionRange(placedStack), item.isExcavator(placedStack)));
       }
       
       if (placeBlockAt(placedStack, playerIn, worldIn, pos, facing, hitX, hitY, hitZ, placedState))
       {
           placedState = worldIn.getBlockState(pos);
           SoundType soundtype = placedState.getBlock().getSoundType(placedState, worldIn, pos, playerIn);
           worldIn.playSound(playerIn, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
           if(!(playerIn.isCreative())) stackIn.shrink(1);
       }

       return EnumActionResult.SUCCESS;
   }
}
