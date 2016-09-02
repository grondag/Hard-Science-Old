package grondag.adversity.niceblock.base;

import grondag.adversity.niceblock.NiceBlockRegistrar2;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

/**
 * Provides sub-items and handles item logic for NiceBlocks.
 */
public class NiceItemBlock2 extends ItemBlock
{
	private static String ITEM_MODEL_KEY_TAG = "AMS";
	
	public NiceItemBlock2(NiceBlock2 block) {
		super(block);
		setHasSubtypes(true);
       // let registrar know to register us when appropriate
        NiceBlockRegistrar2.allItems.add(this);
	}

	public void registerSelf()
	{
	    registerItemBlock(block, this);
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

    public static long getModelStateKey(ItemStack stack)
    {
        if(stack.getItem() instanceof NiceItemBlock2)
        {
            return stack.getTagCompound().getLong(NiceItemBlock2.ITEM_MODEL_KEY_TAG);
        }
        else
        {
            return 0L;
        }
    }
    
    public static void setModelStateKey(ItemStack stack, long key)
    {
        if(stack.getItem() instanceof NiceItemBlock2)
        {
            
            NBTTagCompound tag = stack.getTagCompound();
            if(tag == null)
            {
                tag = new NBTTagCompound();
                stack.setTagCompound(tag);

            }
            tag.setLong(NiceItemBlock2.ITEM_MODEL_KEY_TAG, key);
        }
    }
    
    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        boolean isAdditive = false;
        BlockPos placedPos = block.isReplaceable(worldIn, pos) || (isAdditive = ((NiceBlock2)block).isItemUsageAdditive(worldIn, pos, stack))
                ? pos : pos.offset(facing);
        
        if (stack.stackSize == 0)
        {
            return EnumActionResult.FAIL;
        }        
        else if (!playerIn.canPlayerEdit(placedPos, facing, stack))
        {
            return EnumActionResult.FAIL;
        }
        
        else if (isAdditive)
        {
            IBlockState state = worldIn.getBlockState(pos);
            AxisAlignedBB axisalignedbb = state.getSelectedBoundingBox(worldIn, placedPos);
            if(!worldIn.checkNoEntityCollision(axisalignedbb.offset(pos), playerIn)) return EnumActionResult.FAIL;
            int meta = ((NiceBlock2) this.block).getMetaForPlacedBlockFromStack(worldIn, placedPos, pos, facing, stack, playerIn);
            IBlockState placedState = this.block.onBlockPlaced(worldIn, placedPos, facing, hitX, hitY, hitZ, meta, playerIn);
            if (placeBlockAt(stack, playerIn, worldIn, placedPos, facing, hitX, hitY, hitZ, placedState))
            {
                worldIn.playSound((double)((float)placedPos.getX() + 0.5F), (double)((float)placedPos.getY() + 0.5F), (double)((float)placedPos.getZ() + 0.5F), this.block.getStepSound().getPlaceSound(), null, (this.block.getStepSound().getVolume() + 1.0F) / 2.0F, this.block.getStepSound().getPitch() * 0.8F, true);
                --stack.stackSize;
            }

            return EnumActionResult.SUCCESS;
        }
        
        else if (worldIn.canBlockBePlaced(this.block, placedPos, false, facing, (Entity)null, stack))            
        {
            int meta = ((NiceBlock2) this.block).getMetaForPlacedBlockFromStack(worldIn, placedPos, pos, facing, stack, playerIn);
            IBlockState iblockstate1 = this.block.onBlockPlaced(worldIn, placedPos, facing, hitX, hitY, hitZ, meta, playerIn);

            if (placeBlockAt(stack, playerIn, worldIn, placedPos, facing, hitX, hitY, hitZ, iblockstate1))
            {
                worldIn.playSound((double)((float)placedPos.getX() + 0.5F), (double)((float)placedPos.getY() + 0.5F), (double)((float)placedPos.getZ() + 0.5F), this.block.getStepSound().getPlaceSound(), null, (this.block.getStepSound().getVolume() + 1.0F) / 2.0F, this.block.getStepSound().getPitch() * 0.8F, true);
                --stack.stackSize;
            }

            return EnumActionResult.SUCCESS;
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

        if(newState.getBlock() instanceof NiceBlockPlus2)
        {
            NiceTileEntity2 niceTE = (NiceTileEntity2)world.getTileEntity(pos);
            if (niceTE != null) 
            {
                niceTE.setModelKey(getModelStateKey(stack));

                //handle any block-specific transfer from stack to TE
                ((NiceBlockPlus2)newState.getBlock()).updateTileEntityOnPlacedBlockFromStack(stack, player, world, pos, newState, niceTE);
                
                if(world.isRemote)
                {
                    world.markBlockRangeForRenderUpdate(pos, pos);
                }
            }
        }
        
        this.block.onBlockPlacedBy(world, pos, newState, player, stack);
        return true;
    }
}