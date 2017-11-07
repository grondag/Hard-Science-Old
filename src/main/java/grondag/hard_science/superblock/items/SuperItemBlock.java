package grondag.hard_science.superblock.items;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import grondag.hard_science.gui.ModGuiHandler;
import grondag.hard_science.init.ModBlocks;
import grondag.hard_science.init.ModItems;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.block.SuperTileEntity;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.placement.PlacementHandler;
import grondag.hard_science.superblock.placement.PlacementItem;
import grondag.hard_science.superblock.placement.PlacementResult;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
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
        if(this.block == ModBlocks.virtual_block)
        {
            this.setMaxDamage(0);
            this.setMaxStackSize(1);
        }
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
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        if(player == null) return new ActionResult<>(EnumActionResult.PASS, null);
        
        ItemStack stackIn = player.getHeldItem(hand);
        
        if (stackIn.isEmpty()) return new ActionResult<>(EnumActionResult.PASS, stackIn);
        
        PlacementResult result = PlacementHandler.doRightClickBlock(player, null, null, null, stackIn);
        
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
                    && world.getBlockState(blockpos).getBlock() != ModBlocks.virtual_block
                    && world.getBlockState(blockpos).getMaterial().isReplaceable()
                    && ((SuperBlock)this.block).isVirtual())
            {
                this.displayGui(player);
                return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
            }
        }
        
        return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
    }
    
    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if(playerIn == null) return EnumActionResult.FAIL;
        
        ItemStack stackIn = playerIn.getHeldItem(hand);
        if (stackIn.isEmpty()) return EnumActionResult.FAIL;
        
        PlacementResult result = PlacementHandler.doRightClickBlock(playerIn, pos, facing, new Vec3d(hitX, hitY, hitZ), stackIn);
        
        result.apply(stackIn, playerIn);
        
//        if(result.isExcavationOnly() && !playerIn.isCreative())
//        {
//            // FIXME: use the server-side thing
//            if(worldIn.isRemote) ExcavationRenderTracker.INSTANCE.add(playerIn, new ExcavationRenderEntry(playerIn, result));
//        }
//        else if(result.hasPlacementList())
//        {
//            return this.doPlacements(result, stackIn, worldIn, playerIn) ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
//        }
        
        // we don't return pass because don't want GUI displayed or other events to process
        return EnumActionResult.SUCCESS;
    }
    /**
     * true if successful
     * FIXME: remove when done transfering logic
     */
    @Deprecated
    private boolean doPlacements(PlacementResult result, ItemStack stackIn, World worldIn, EntityPlayer playerIn)
    {
        if(!playerIn.capabilities.allowEdit) return false;
        
//        ModelState modelState = PlacementItem.getStackModelState(stackIn);
//        if(modelState == null) return false;

        // supermodel blocks may need to use a different block instance depending on model/substance
        // handle this here by substituting a stack different than what player is holding, but don't
        // change what is in player's hand.
//        SuperBlock targetBlock = (SuperBlock) this.block;
//
//        if(!targetBlock.isVirtual() && targetBlock instanceof SuperModelBlock)
//        {
//            BlockSubstance substance = PlacementItem.getStackSubstance(stackIn);
//            if(substance == null) return false;
//            targetBlock = ModSuperModelBlocks.findAppropriateSuperModelBlock(substance, modelState);
//            
//            if(targetBlock != this.block)
//            {
//                ItemStack tempStack = new ItemStack(targetBlock);
//                tempStack.setCount(stackIn.getCount());
//                tempStack.setItemDamage(stackIn.getItemDamage());
//                tempStack.setTagCompound(stackIn.getTagCompound());
//                stackIn = tempStack;
//            }
//        }
        
        List<Pair<BlockPos, ItemStack>> placements = result.placements(); // placementHandler.getPlacementResults(playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ, stackIn);
        
        if(placements.isEmpty()) return false;
        
        SoundType soundtype = getPlacementSound(stackIn);
        boolean didPlace = false;
        
        for(Pair<BlockPos, ItemStack> p : placements)
        {
            ItemStack placedStack = p.getRight();
            BlockPos placedPos = p.getLeft();
          
            ModelState placedModelState = PlacementItem.getStackModelState(placedStack);
            
            AxisAlignedBB axisalignedbb = placedModelState == null ? Block.FULL_BLOCK_AABB : placedModelState.getShape().meshFactory().collisionHandler().getCollisionBoundingBox(placedModelState);


            if(worldIn.checkNoEntityCollision(axisalignedbb.offset(placedPos)))
            {
                IBlockState placedState = PlacementItem.getPlacementBlockStateFromStack(placedStack);
                        //targetBlock.getStateFromMeta(placedStack.getMetadata());
                if (placeBlockAt(placedStack, playerIn, worldIn, placedPos, null, 0, 0, 0, placedState))
                {
                    didPlace = true;
                    worldIn.playSound(playerIn, placedPos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    if(!(playerIn.isCreative() || isVirtual(stackIn)))
                    {
                        stackIn.shrink(1);
                        if(stackIn.isEmpty()) break;
                    }
                }
            }
        }
        return didPlace;
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
    public String getItemStackDisplayName(ItemStack stack)
    {
        return ((SuperBlock)this.block).getItemStackDisplayName(stack);
    }
    
    private static SoundType getPlacementSound(ItemStack stack)
    {
        return PlacementItem.getStackSubstance(stack).soundType;
//        return isVirtual(stack) ? VirtualBlock.VIRTUAL_BLOCK_SOUND : getStackSubstance(stack).soundType;
    }
    
    public static boolean isVirtual(ItemStack stack)
    {
        return stack.getItem() == ModItems.virtual_block;
    }

    @Override
    public SuperBlock getSuperBlock()
    {
        return (SuperBlock) this.block;
    }

    @Override
    public int guiOrdinal()
    {
        return ModGuiHandler.ModGui.SUPERMODEL_ITEM.ordinal();
    }
}
