package grondag.adversity.niceblock.base;

import java.util.List;

import grondag.adversity.Adversity;
import grondag.adversity.gui.AdversityGuiHandler;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.modelstate.ModelColorMapComponent.ModelColorMap;
import grondag.adversity.niceblock.modelstate.ModelFlowJoinComponent.ModelFlowJoin;
import grondag.adversity.niceblock.modelstate.ModelStateSet;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

/**
 * Provides sub-items and handles item logic for NiceBlocks.
 */
public class NiceItemBlock extends ItemBlock
{
	private static String ITEM_MODEL_KEY_TAG = "AMS";
	
	public NiceItemBlock(NiceBlock block) {
		super(block);
		setHasSubtypes(true);
       // let registrar know to register us when appropriate
        NiceBlockRegistrar.allItems.add(this);
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
        if(stack.getItem() instanceof NiceItemBlock)
        {
            //WAILA or other mods might create a stack with no NBT, which may not work in dispatcher.
            if(stack.getTagCompound() == null)
            {
                List<ItemStack> subItems =  ((NiceBlock)((NiceItemBlock)stack.getItem()).block).getSubItems();
                ItemStack defaultStack = subItems.get(Math.min(stack.getMetadata(), subItems.size()-1));
                if(defaultStack.getTagCompound() == null)
                {
                    //if not tag provided, must be a dispatcher that doesn't care
                    return 0L;
                }
                else
                {
                    return defaultStack.getTagCompound().getLong(NiceItemBlock.ITEM_MODEL_KEY_TAG);
                }
            }
            else
            {
                return stack.getTagCompound().getLong(NiceItemBlock.ITEM_MODEL_KEY_TAG);
            }
        }
        else
        {
            return 0L;
        }
    }
    
    public static void setModelStateKey(ItemStack stack, long key)
    {
        if(stack.getItem() instanceof NiceItemBlock)
        {
            
            NBTTagCompound tag = stack.getTagCompound();
            if(tag == null)
            {
                tag = new NBTTagCompound();
                stack.setTagCompound(tag);

            }
            tag.setLong(NiceItemBlock.ITEM_MODEL_KEY_TAG, key);
        }
    }
    
    public int getColorMapID(ItemStack stack)
    {
        ModelDispatcher dispatch = ((NiceBlock)this.block).dispatcher;
        ModelStateSet stateSet = dispatch.getStateSet();
        return stateSet.getSetValueFromBits(getModelStateKey(stack)).getValue(stateSet.getFirstColorMapComponent()).ordinal;
    }
    
    public void setColorMapID(ItemStack stack, int colorMapID)
    {
    
        ModelDispatcher dispatch = ((NiceBlock)this.block).dispatcher;
        ModelStateSet stateSet = dispatch.getStateSet();
        ModelColorMap colorMap = stateSet.getFirstColorMapComponent().createValueFromBits(colorMapID);
    //ModelComponent
        long oldModelKey = getModelStateKey(stack);
        long newModelKey = stateSet.getValueWithUpdates(stateSet.getSetValueFromBits(oldModelKey), colorMap).getKey();
        if(oldModelKey != newModelKey)
        {
            setModelStateKey(stack, newModelKey);
        }
    }
    /**
     * Used by flow block to preserve shape in silk touch or pick-block drops
     */
    public void setFlowState(ItemStack stack, ModelFlowJoin flowJoin)
    {
        ModelDispatcher dispatch = ((NiceBlock)this.block).dispatcher;
        ModelStateSet stateSet = dispatch.getStateSet();
        long oldModelKey = getModelStateKey(stack);
        long newModelKey = stateSet.getValueWithUpdates(stateSet.getSetValueFromBits(oldModelKey), flowJoin).getKey();
        if(oldModelKey != newModelKey)
        {
            setModelStateKey(stack, newModelKey);
        }
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand)
    {
        if (world.isRemote) 
        {
            BlockPos blockpos = Minecraft.getMinecraft().objectMouseOver.getBlockPos();
            //if trying to place a block but too close, is annoying to get GUI
            //so only display if clicking on air
            if (blockpos != null && world.getBlockState(blockpos).getMaterial() == Material.AIR && ((NiceBlock)this.block).hasAppearanceGui())
            {
                player.openGui(Adversity.instance, AdversityGuiHandler.GUI_NICE_BLOCK_ITEM, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
                return new ActionResult<>(EnumActionResult.SUCCESS, stack);
            }
        }
        return new ActionResult<>(EnumActionResult.PASS, stack);
    }
    
    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        boolean isAdditive = false;
        
        BlockPos placedPos = worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos) || (isAdditive = ((NiceBlock)block).isItemUsageAdditive(worldIn, pos, stack))
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
            int meta = ((NiceBlock) this.block).getMetaForPlacedBlockFromStack(worldIn, placedPos, pos, facing, stack, playerIn);
            IBlockState placedState = this.block.onBlockPlaced(worldIn, placedPos, facing, hitX, hitY, hitZ, meta, playerIn);
            if (placeBlockAt(stack, playerIn, worldIn, placedPos, facing, hitX, hitY, hitZ, placedState))
            {
                worldIn.playSound((double)((float)placedPos.getX() + 0.5F), (double)((float)placedPos.getY() + 0.5F), (double)((float)placedPos.getZ() + 0.5F), this.block.getSoundType().getPlaceSound(), null, (this.block.getSoundType().getVolume() + 1.0F) / 2.0F, this.block.getSoundType().getPitch() * 0.8F, true);
                --stack.stackSize;
            }

            return EnumActionResult.SUCCESS;
        }
        
        else if (worldIn.canBlockBePlaced(this.block, placedPos, false, facing, (Entity)null, stack))            
        {
            int meta = ((NiceBlock) this.block).getMetaForPlacedBlockFromStack(worldIn, placedPos, pos, facing, stack, playerIn);
            IBlockState iblockstate1 = this.block.onBlockPlaced(worldIn, placedPos, facing, hitX, hitY, hitZ, meta, playerIn);

            if (placeBlockAt(stack, playerIn, worldIn, placedPos, facing, hitX, hitY, hitZ, iblockstate1))
            {
                worldIn.playSound((double)((float)placedPos.getX() + 0.5F), (double)((float)placedPos.getY() + 0.5F), (double)((float)placedPos.getZ() + 0.5F), this.block.getSoundType().getPlaceSound(), null, (this.block.getSoundType().getVolume() + 1.0F) / 2.0F, this.block.getSoundType().getPitch() * 0.8F, true);
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

        if(newState.getBlock() instanceof NiceBlockPlus)
        {
            NiceTileEntity niceTE = (NiceTileEntity)world.getTileEntity(pos);
            if (niceTE != null) 
            {
//                Adversity.log.info("calling setModelKey from NiceItemBlock.placeBlockAt");

                niceTE.setModelKey(getModelStateKey(stack));

                //handle any block-specific transfer from stack to TE
                ((NiceBlockPlus)newState.getBlock()).updateTileEntityOnPlacedBlockFromStack(stack, player, world, pos, newState, niceTE);
                
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
        // TODO Auto-generated method stub
        return ((NiceBlock)this.block).getItemStackDisplayName(stack);
    }
    
    
}