package grondag.adversity.feature.volcano;

import java.util.Map;

import grondag.adversity.config.Config;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.block.FlowDynamicBlock;
import grondag.adversity.niceblock.block.FlowStaticBlock;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import grondag.adversity.niceblock.support.BaseMaterial;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;


public class TerrainWand extends Item
{
    public TerrainWand() 
    {
        setRegistryName("terrain_wand"); 
        setUnlocalizedName("terrain_wand");
        this.setMaxStackSize(1);
    }

    private static final String MODE_TAG = "mode";

    private enum TerrainMode
    {
        HEIGHT,
        STATE;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        if(!worldIn.isRemote)
        {
            TerrainMode newMode = TerrainMode.STATE;
            NBTTagCompound tag;

            if(itemStackIn.hasTagCompound())
            {
                tag = itemStackIn.getTagCompound();
                if(tag.getString(MODE_TAG) == TerrainMode.STATE.name())
                {
                    newMode = TerrainMode.HEIGHT;
                }
            }
            else
            {
                tag = new NBTTagCompound();

            }

            tag.setString(MODE_TAG, newMode.name());
            itemStackIn.setTagCompound(tag);

            //TODO: localize
            playerIn.addChatComponentMessage(new TextComponentString("Mode targetPos to " + newMode.toString()));

        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
    }    

    public TerrainMode getMode(ItemStack itemStackIn)
    {
        if(itemStackIn.hasTagCompound() && itemStackIn.getTagCompound().getString(MODE_TAG) == TerrainMode.STATE.name())
        {
            return TerrainMode.STATE;
        }
        else
        {
            return TerrainMode.HEIGHT;
        }

    }


    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if(worldIn.isRemote) return EnumActionResult.SUCCESS;
        
        if(getMode(stack) == TerrainMode.HEIGHT)
        {
            return handleUseHeightMode(stack, playerIn, worldIn, pos);
        }
        else
        {
            return handleUseStateMode(stack, playerIn, worldIn, pos);
        }
    }

    public EnumActionResult handleUseStateMode(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos)
    {
        IBlockState stateIn = worldIn.getBlockState(pos);
        Block blockIn = stateIn.getBlock();

        if(IFlowBlock.isFlowBlock(blockIn))
        {
            
            if(blockIn == NiceBlockRegistrar.COOL_STATIC_BASALT_HEIGHT_BLOCK)
            {
                IBlockState targetState = NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK.getDefaultState()
                        .withProperty(NiceBlock.META, stateIn.getValue(NiceBlock.META));
                worldIn.setBlockState(pos, targetState);
             
            }
            else if(blockIn == NiceBlockRegistrar.COOL_STATIC_BASALT_FILLER_BLOCK)
            {
                IBlockState targetState = NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK.getDefaultState()
                        .withProperty(NiceBlock.META, stateIn.getValue(NiceBlock.META));
                worldIn.setBlockState(pos, targetState);
            }
            else if(blockIn instanceof FlowDynamicBlock)
            {
                ((FlowDynamicBlock)blockIn).makeStatic(stateIn, worldIn, pos);
            }
            else if(blockIn instanceof FlowStaticBlock)
            {
                ((FlowStaticBlock)blockIn).makeDynamic(stateIn, worldIn, pos);
            }
        }
        
        return EnumActionResult.SUCCESS;
    }
    
    public EnumActionResult handleUseHeightMode(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos)
    {

        if(IFlowBlock.isFlowHeight(worldIn.getBlockState(pos.up()).getBlock()))
        {
            return handleUseHeightMode(stack, playerIn, worldIn, pos.up());
        }

        IBlockState stateIn = worldIn.getBlockState(pos);
        Block blockIn = stateIn.getBlock();

        IBlockState targetState = null;
        BlockPos targetPos = null;

        if(IFlowBlock.isFlowHeight(blockIn))
        {
            int level = IFlowBlock.getFlowHeightFromState(stateIn);

            if(playerIn.isSneaking())
            {	
                if(level > 1)
                {	
                    targetPos = pos;
                    targetState = IFlowBlock.stateWithFlowHeight(stateIn, level - 1);
                    playerIn.addChatComponentMessage(new TextComponentString("Level " + (level - 1)));

                }
                else if(IFlowBlock.isFlowHeight(worldIn.getBlockState(pos.down()).getBlock()))
                {
                    targetPos = pos;
                    targetState = Blocks.AIR.getDefaultState();
                    playerIn.addChatComponentMessage(new TextComponentString("Level 0 (removed a block)"));
                }
                else
                {
                    //prevent mode change
                    return EnumActionResult.SUCCESS;
                }
            }
            else
            {
                if(level < FlowHeightState.BLOCK_LEVELS_INT)
                {
                    targetPos = pos;
                    targetState = IFlowBlock.stateWithFlowHeight(stateIn, level + 1);
                    playerIn.addChatComponentMessage(new TextComponentString("Level " + (level + 1)));
                }
                else if(worldIn.getBlockState(pos.up()).getBlock().isReplaceable(worldIn, pos.up())
                        || IFlowBlock.isFlowFiller(worldIn.getBlockState(pos.up()).getBlock()))
                {
                    targetPos = pos.up();
                    targetState = IFlowBlock.stateWithFlowHeight(stateIn, 1);
                    playerIn.addChatComponentMessage(new TextComponentString("Level 1 (added new block)"));
                }
                else
                {
                    //prevent mode change
                    return EnumActionResult.SUCCESS;
                }
            }
        }

        if(targetPos == null || targetState == null)
        {
            return EnumActionResult.FAIL;
        }

        AxisAlignedBB axisalignedbb = targetState.getSelectedBoundingBox(worldIn, targetPos);
        if(!worldIn.checkNoEntityCollision(axisalignedbb.offset(pos), playerIn)) return EnumActionResult.FAIL;

        worldIn.setBlockState(targetPos, targetState);

        adjustFillIfNeeded(targetPos, worldIn);
        adjustFillIfNeeded(targetPos.east(), worldIn);
        adjustFillIfNeeded(targetPos.west(), worldIn);
        adjustFillIfNeeded(targetPos.north(), worldIn);
        adjustFillIfNeeded(targetPos.south(), worldIn);
        adjustFillIfNeeded(targetPos.north().east(), worldIn);
        adjustFillIfNeeded(targetPos.south().east(), worldIn);
        adjustFillIfNeeded(targetPos.north().west(), worldIn);
        adjustFillIfNeeded(targetPos.south().west(), worldIn);

        worldIn.playSound((double)((float)targetPos.getX() + 0.5F), 
                (double)((float)targetPos.getY() + 0.5F), 
                (double)((float)targetPos.getZ() + 0.5F), 
                blockIn.getSoundType().getPlaceSound(), null, 
                (blockIn.getSoundType().getVolume() + 1.0F) / 2.0F, blockIn.getSoundType().getPitch() * 0.8F, true);

        return EnumActionResult.SUCCESS;

    }

    /**
     * Adds or removes filler blocks as needed.
     * @param basePos
     */
    public static void adjustFillIfNeeded(BlockPos posIn, World worldObj)
    {
        final int SHOULD_BE_AIR = -1;

        for(int y = -4; y <= 4; y++)
        {
            BlockPos basePos = posIn.add(0, y, 0);



            IBlockState baseState = worldObj.getBlockState(basePos);
            Block baseBlock = baseState.getBlock();
            NiceBlock fillBlock = null;
            
            //don't adjust static blocks
            if(baseBlock instanceof FlowStaticBlock) continue;
            
            int targetMeta = SHOULD_BE_AIR;

            /**
             * If space is occupied with a non-displaceable block, will be ignored.
             * Static flow blocks are also ignored.
             * Otherwise, possible target states: air, fill +1, fill +2
             * 
             * Should be fill +1 if block below is a heightblock and needs a fill >= 1;
             * Should be a fill +2 if block below is not a heightblock and block
             * two below needs a fill = 2;
             * Otherwise should be air.
             */
            IBlockState stateBelow = worldObj.getBlockState(basePos.down());
            if(IFlowBlock.isFlowHeight(stateBelow.getBlock()) 
                    && IFlowBlock.topFillerNeeded(stateBelow, worldObj, basePos.down()) > 0)
            {
                targetMeta = 0;
                fillBlock = NiceBlockRegistrar.getFillerBlock(stateBelow.getBlock());
            }
            else 
            {
                IBlockState stateTwoBelow = worldObj.getBlockState(basePos.down(2));
                if((IFlowBlock.isFlowHeight(stateTwoBelow.getBlock()) 
                        && IFlowBlock.topFillerNeeded(stateTwoBelow, worldObj, basePos.down(2)) == 2))
                {
                    targetMeta = 1;
                    fillBlock = NiceBlockRegistrar.getFillerBlock(stateTwoBelow.getBlock());
                }
            }

            if(IFlowBlock.isFlowFiller(baseBlock))
            {

                if(targetMeta == SHOULD_BE_AIR)
                {
                    worldObj.setBlockToAir(basePos);
                }
                else if(baseState.getValue(NiceBlock.META) != targetMeta || baseBlock != fillBlock && fillBlock != null)
                {

                    worldObj.setBlockState(basePos, fillBlock.getDefaultState()
                            .withProperty(NiceBlock.META, targetMeta));

                }

            }
            else if(targetMeta != SHOULD_BE_AIR && LavaManager.canDisplace(baseState) && fillBlock != null)
            {
                worldObj.setBlockState(basePos, fillBlock.getDefaultState()
                        .withProperty(NiceBlock.META, targetMeta));

            }
        }
    }
}