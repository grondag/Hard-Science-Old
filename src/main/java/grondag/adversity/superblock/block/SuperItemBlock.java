package grondag.adversity.superblock.block;

import grondag.adversity.Adversity;
import grondag.adversity.gui.AdversityGuiHandler;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
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
import net.minecraft.world.World;

/**
 * Provides sub-items and handles item logic for NiceBlocks.
 */
public class SuperItemBlock extends ItemBlock
{
    public SuperItemBlock(SuperBlock block) {
        super(block);
        setHasSubtypes(true);
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

    public static ModelState getModelState(ItemStack stack)
    {
        if(stack.getItem() instanceof SuperItemBlock)
        {
            NBTTagCompound tag = stack.getTagCompound();
            //WAILA or other mods might create a stack with no NBT
            if(tag != null)
            {
                ModelState modelState = SuperBlockNBTHelper.readModelState(tag);
                if(modelState != null) return modelState;
            }
        }
        return ((SuperBlock)((SuperItemBlock)stack.getItem()).block).getDefaultModelState();
    }
    
    public static void setModelState(ItemStack stack, ModelState modelState)
    {
        if(stack.getItem() instanceof SuperItemBlock)
        {
            
            NBTTagCompound tag = stack.getTagCompound();
            if(tag == null)
            {
                tag = new NBTTagCompound();
            }
            SuperBlockNBTHelper.writeModelState(tag, modelState);
            stack.setTagCompound(tag);
        }
    }
 
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        if (world.isRemote) 
        {
            BlockPos blockpos = Minecraft.getMinecraft().objectMouseOver.getBlockPos();
            //if trying to place a block but too close, is annoying to get GUI
            //so only display if clicking on air
            if (blockpos != null && world.getBlockState(blockpos).getMaterial() == Material.AIR && ((SuperBlock)this.block).hasAppearanceGui())
            {
                player.openGui(Adversity.INSTANCE, AdversityGuiHandler.GUI_SUPERMODEL_ITEM, player.world, (int) player.posX, (int) player.posY, (int) player.posZ);
                return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
            }
        }
        return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
    }
    
    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        ItemStack stackIn = playerIn.getHeldItem(hand);

        //TODO: always false, put back logic or remove
        boolean isAdditive = false;
        
        BlockPos placedPos = worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos) || (isAdditive = ((SuperBlock)block).isItemUsageAdditive(worldIn, pos, stackIn))
                ? pos : pos.offset(facing);
        
        
        if (stackIn.isEmpty())
        {
            return EnumActionResult.FAIL;
        }       
        
        if (!playerIn.canPlayerEdit(placedPos, facing, stackIn))
        {
            return EnumActionResult.FAIL;
        }
        
        ItemStack stack = ((SuperBlock)this.block).updatedStackForPlacement(worldIn, placedPos, pos, facing, stackIn, playerIn);
        int meta = ((SuperBlock)this.block).getMetaFromModelState(getModelState(stack));
        
        if (isAdditive)
        {
            IBlockState state = worldIn.getBlockState(pos);
            AxisAlignedBB axisalignedbb = state.getSelectedBoundingBox(worldIn, placedPos);
            if(!worldIn.checkNoEntityCollision(axisalignedbb.offset(pos), playerIn)) return EnumActionResult.FAIL;
            IBlockState placedState = this.block.getStateForPlacement(worldIn, placedPos, facing, hitX, hitY, hitZ, meta, playerIn, hand);
            if (placeBlockAt(stack, playerIn, worldIn, placedPos, facing, hitX, hitY, hitZ, placedState))
            {
                SoundType soundtype = worldIn.getBlockState(pos).getBlock().getSoundType(worldIn.getBlockState(pos), worldIn, pos, playerIn);
                worldIn.playSound(playerIn, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                stack.shrink(1);
            }

            return EnumActionResult.SUCCESS;
        }
        
        else if (worldIn.mayPlace(this.block, placedPos, false, facing, (Entity)null))            
        {
            IBlockState iblockstate1 = this.block.getStateForPlacement(worldIn, placedPos, facing, hitX, hitY, hitZ, meta, playerIn, hand);
            if (placeBlockAt(stack, playerIn, worldIn, placedPos, facing, hitX, hitY, hitZ, iblockstate1))
            {
                SoundType soundtype = worldIn.getBlockState(pos).getBlock().getSoundType(worldIn.getBlockState(pos), worldIn, pos, playerIn);
                worldIn.playSound(playerIn, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
  
                stack.shrink(1);
                
                return EnumActionResult.SUCCESS;
            }
            else
            {
                return EnumActionResult.FAIL;
            }


        }
        else
        {
            return EnumActionResult.FAIL;
        }
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
        if (!world.setBlockState(pos, newState, 3)) return false;

        if(newState.getBlock() instanceof SuperModelBlock)
        {
            SuperTileEntity niceTE = (SuperTileEntity)world.getTileEntity(pos);
            if (niceTE != null) 
            {
//                Output.getLog().info("calling setModelKey from SuperItemBlock.placeBlockAt @" + pos.toString());

                niceTE.setModelState(getModelState(stack));

                //handle any block-specific transfer from stack to TE
                ((SuperModelBlock)newState.getBlock()).updateTileEntityOnPlacedBlockFromStack(stack, player, world, pos, newState, niceTE);
                
                if(world.isRemote)
                {
                    niceTE.updateClientRenderState();
                }
            }
        }
        
        this.block.onBlockPlacedBy(world, pos, newState, player, stack);
        return true;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        return ((SuperBlock)this.block).getItemStackDisplayName(stack);
    }
    
    
    public static void setStackPlacementShape(ItemStack stack, int placementShape)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if(tag == null){
            tag = new NBTTagCompound();
        }
        SuperBlockNBTHelper.writePlacementShape(tag, placementShape);
        stack.setTagCompound(tag);
    }
    
    public static int getStackPlacementShape(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null 
                ? 0
                : SuperBlockNBTHelper.readPlacementShape(tag);
    }
    
    public static void setStackLightValue(ItemStack stack, int lightValue)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if(tag == null){
            tag = new NBTTagCompound();
        }
        SuperBlockNBTHelper.writeLightValue(tag, lightValue);
        stack.setTagCompound(tag);
    }
    
    public static byte getLightValue(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null 
                ? 0
                : SuperBlockNBTHelper.readLightValue(tag);
    }
    
}
