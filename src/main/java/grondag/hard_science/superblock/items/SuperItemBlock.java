package grondag.hard_science.superblock.items;

import javax.annotation.Nullable;

import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.block.SuperTileEntity;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.placement.BlockOrientationHandler;
import grondag.hard_science.superblock.placement.PlacementItem;
import grondag.hard_science.superblock.placement.PlacementItemFeature;
import grondag.hard_science.superblock.placement.PlacementPosition;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
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

    /**
    * Called to actually place the block, after the location is determined
    * and all permission checks have been made.
    *
    * @param stack The item stack that was used to place the block. This can be changed inside the method.
    * @param player The player who is placing the block. Can be null if the block is not being placed by a player.
    * @param side The side the player (or machine) right-clicked on.
    */
   @Override
   public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState)
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
   public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
   {
       return new ActionResult<ItemStack>(EnumActionResult.PASS, player.getHeldItem(hand));
   }
   
   @Override
   public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
   {
       IBlockState currentState = worldIn.getBlockState(pos);
       Block block = currentState.getBlock();

       if (!block.isReplaceable(worldIn, pos))
       {
           pos = pos.offset(facing);
       }

       ItemStack stackIn = playerIn.getHeldItem(hand);

       if (stackIn.isEmpty() || !playerIn.canPlayerEdit(pos, facing, stackIn)) 
           return EnumActionResult.FAIL;
       
   
       ModelState modelState = PlacementItem.getStackModelState(stackIn);
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
           BlockOrientationHandler.applyDynamicOrientation(placedStack, playerIn, 
                   new PlacementPosition(playerIn, pos, facing, new Vec3d(hitX, hitY, hitZ), placedStack));
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
