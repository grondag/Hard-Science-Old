package grondag.adversity.feature.volcano;

import grondag.adversity.feature.volcano.lava.LavaTerrainHelper;
import grondag.adversity.init.ModBlocks;
import grondag.adversity.niceblock.base.TerrainBlock;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import grondag.adversity.superblock.block.SuperBlock;
import grondag.adversity.superblock.block.TerrainDynamicBlock;
import grondag.adversity.superblock.block.TerrainStaticBlock;
import grondag.adversity.superblock.terrain.TerrainBlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
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
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        ItemStack stack = playerIn.getHeldItem(hand);
        
        if(!worldIn.isRemote)
        {
            TerrainMode newMode = TerrainMode.STATE;
            NBTTagCompound tag;

            if(stack.hasTagCompound())
            {
                tag = stack.getTagCompound();
                if(tag.getString(MODE_TAG).equals(TerrainMode.STATE.name()))
                {
                    newMode = TerrainMode.HEIGHT;
                }
            }
            else
            {
                tag = new NBTTagCompound();

            }

            tag.setString(MODE_TAG, newMode.name());
            stack.setTagCompound(tag);

            String message = I18n.format("misc.mode_set", newMode.toString());
            playerIn.sendMessage(new TextComponentString(message));

        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }    

    public TerrainMode getMode(ItemStack itemStackIn)
    {
        if(itemStackIn.hasTagCompound() && itemStackIn.getTagCompound().getString(MODE_TAG).equals(TerrainMode.STATE.name()))
        {
            return TerrainMode.STATE;
        }
        else
        {
            return TerrainMode.HEIGHT;
        }

    }


    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if(worldIn.isRemote) return EnumActionResult.SUCCESS;
        
        ItemStack stack = playerIn.getHeldItem(hand);
        
        if(getMode(stack) == TerrainMode.HEIGHT)
        {
//            if(playerIn.isSneaking())
//            {
//                return handleUseSmoothMode(stack, playerIn, worldIn, pos);
//            }
//            else
//            {
                return handleUseHeightMode(stack, playerIn, worldIn, pos);
//            }
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

        if(TerrainBlock.isFlowBlock(blockIn))
        {
            
            if(blockIn == ModBlocks.basalt_cool_static_height)
            {
                IBlockState targetState = ModBlocks.lava_dynamic_height.getDefaultState()
                        .withProperty(SuperBlock.META, stateIn.getValue(SuperBlock.META));
                worldIn.setBlockState(pos, targetState);
             
            }
            else if(blockIn == ModBlocks.basalt_cool_static_filler)
            {
                IBlockState targetState = ModBlocks.lava_dynamic_filler.getDefaultState()
                        .withProperty(SuperBlock.META, stateIn.getValue(SuperBlock.META));
                worldIn.setBlockState(pos, targetState);
            }
            else if(blockIn instanceof TerrainDynamicBlock)
            {
                ((TerrainDynamicBlock)blockIn).makeStatic(stateIn, worldIn, pos);
            }
            else if(blockIn instanceof TerrainStaticBlock)
            {
                ((TerrainStaticBlock)blockIn).makeDynamic(stateIn, worldIn, pos);
            }
        }
        
        return EnumActionResult.SUCCESS;
    }
    
    /** for testing box filter smoothing on flowing terrain - not for release */
    public EnumActionResult handleUseSmoothMode(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos)
    {
        int height[][] = new int[33][33];
        
        for(int x = 0; x < 33; x++)
        {
            for(int z = 0; z < 33; z++)
            {
                height[x][z] = getHeightAt(worldIn, pos.getX() - 16 + x, pos.getY(), pos.getZ() - 16 + z);
            }
        }
        
        for(int x = 1; x < 32; x++)
        {
            for(int z = 1; z < 32; z++)
            {
                int avg = (height[x - 1][z] + height[x - 1][z] + height[x - 1][z + 1]
                        + height[x][z] + height[x][z] + height[x][z + 1]
                        + height[x + 1][z] + height[x + 1][z] + height[x + 1][z + 1]) / 9;
                
                int currentLevel = height[x][z];
                

                int currentY = (int) Math.floor((currentLevel - 1) / FlowHeightState.BLOCK_LEVELS_FLOAT);
                BlockPos targetPos = new BlockPos(pos.getX() - 16 + x, currentY, pos.getZ() - 16 + z);
                IBlockState currentState = worldIn.getBlockState(targetPos);
                
                if(TerrainBlock.isFlowHeight(currentState.getBlock()))
                {
                    if(avg > currentLevel)
                    {
                        int newLevel = Math.min(currentLevel + FlowHeightState.BLOCK_LEVELS_INT, avg);
                        int newY = (int) Math.floor((newLevel - 1) / FlowHeightState.BLOCK_LEVELS_FLOAT);
                        
                        if(newY == currentY)
                        {
                            worldIn.setBlockState(targetPos, TerrainBlock.stateWithDiscreteFlowHeight(currentState, newLevel - (newY * FlowHeightState.BLOCK_LEVELS_INT)));
                        }
                        else
                        {
                            worldIn.setBlockState(targetPos, TerrainBlock.stateWithDiscreteFlowHeight(currentState, FlowHeightState.BLOCK_LEVELS_INT));
                            worldIn.setBlockState(targetPos.up(), TerrainBlock.stateWithDiscreteFlowHeight(currentState, newLevel - (newY * FlowHeightState.BLOCK_LEVELS_INT)));
                        }
                    }
                    else if(avg < currentLevel)
                    {
                        int newLevel = Math.max(currentLevel - FlowHeightState.BLOCK_LEVELS_INT, avg);
                        int newY = (int) Math.floor((newLevel - 1) / FlowHeightState.BLOCK_LEVELS_FLOAT);
                        
                        if(newY == currentY)
                        {
                            worldIn.setBlockState(targetPos, TerrainBlock.stateWithDiscreteFlowHeight(currentState, newLevel - (newY * FlowHeightState.BLOCK_LEVELS_INT)));
                        }
                        else
                        {
                            worldIn.setBlockToAir(targetPos);
                            worldIn.setBlockState(targetPos.down(), TerrainBlock.stateWithDiscreteFlowHeight(currentState, newLevel - (newY * FlowHeightState.BLOCK_LEVELS_INT)));
                        }
                    }
                }
            }
        }
        return EnumActionResult.SUCCESS;
    }
    
    private static int getHeightAt(World world, int x, int y, int z)
    {
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = world.getBlockState(pos);
        int h = TerrainBlock.getFlowHeightFromState(state);
        
        if(h != 0) return y * FlowHeightState.BLOCK_LEVELS_INT + h;
        
        if(state.getMaterial().isReplaceable())
        {
            //go down
            int downCount = 1;
            state = world.getBlockState(pos.down(downCount));
            
            while(y - downCount > 0 && (state.getMaterial().isReplaceable() || TerrainBlock.isFlowFiller(state.getBlock())))
            {
                downCount++;
                state = world.getBlockState(pos.down(downCount));
            }
            h = TerrainBlock.getFlowHeightFromState(state);
            return (y - downCount) * FlowHeightState.BLOCK_LEVELS_INT + h;
        }
        else
        {
            // go up
            int upCount = 1;
            state = world.getBlockState(pos.up(upCount));
            h = TerrainBlock.getFlowHeightFromState(state);
            
            while(h == 0 && y + upCount < 255 && !(state.getMaterial().isReplaceable() || TerrainBlock.isFlowFiller(state.getBlock())))
            {
                upCount++;
                state = world.getBlockState(pos.up(upCount));
                h = TerrainBlock.getFlowHeightFromState(state);
            }
            return (y + upCount) * FlowHeightState.BLOCK_LEVELS_INT + h;
        }
            
    }
    
    @SuppressWarnings("deprecation")
    public EnumActionResult handleUseHeightMode(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos)
    {

        if(TerrainBlock.isFlowHeight(worldIn.getBlockState(pos.up()).getBlock()))
        {
            return handleUseHeightMode(stack, playerIn, worldIn, pos.up());
        }

        IBlockState stateIn = worldIn.getBlockState(pos);
        Block blockIn = stateIn.getBlock();

        IBlockState targetState = null;
        BlockPos targetPos = null;

        int level = TerrainBlock.getFlowHeightFromState(stateIn);
        if(level > 0)
        {
            if(playerIn.isSneaking())
            {	
                if(level > 1)
                {	
                    targetPos = pos;
                    targetState = TerrainBlock.stateWithDiscreteFlowHeight(stateIn, level - 1);
                    playerIn.sendMessage(new TextComponentString("Level " + (level - 1)));

                }
                else if(TerrainBlock.isFlowHeight(worldIn.getBlockState(pos.down()).getBlock()))
                {
                    targetPos = pos;
                    targetState = Blocks.AIR.getDefaultState();
                    playerIn.sendMessage(new TextComponentString("Level 0 (removed a block)"));
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
                    targetState = TerrainBlock.stateWithDiscreteFlowHeight(stateIn, level + 1);
                    playerIn.sendMessage(new TextComponentString("Level " + (level + 1)));
                }
                else if(worldIn.getBlockState(pos.up()).getBlock().isReplaceable(worldIn, pos.up())
                        || TerrainBlock.isFlowFiller(worldIn.getBlockState(pos.up()).getBlock()))
                {
                    targetPos = pos.up();
                    targetState = TerrainBlock.stateWithDiscreteFlowHeight(stateIn, 1);
                    playerIn.sendMessage(new TextComponentString("Level 1 (added new block)"));
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
                blockIn.getSoundType().getPlaceSound(), SoundCategory.BLOCKS, 
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
            SuperBlock fillBlock = null;
            
            //don't adjust static blocks
            if(baseBlock instanceof TerrainStaticBlock) continue;
            
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
            if(TerrainBlock.isFlowHeight(stateBelow.getBlock()) 
                    && TerrainBlock.topFillerNeeded(stateBelow, worldObj, basePos.down()) > 0)
            {
                targetMeta = 0;
                fillBlock = (SuperBlock) TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.getFillerBlock(stateBelow.getBlock());
            }
            else 
            {
                IBlockState stateTwoBelow = worldObj.getBlockState(basePos.down(2));
                if((TerrainBlock.isFlowHeight(stateTwoBelow.getBlock()) 
                        && TerrainBlock.topFillerNeeded(stateTwoBelow, worldObj, basePos.down(2)) == 2))
                {
                    targetMeta = 1;
                    fillBlock = (SuperBlock) TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.getFillerBlock(stateTwoBelow.getBlock());
                }
            }

            if(TerrainBlock.isFlowFiller(baseBlock))
            {

                if(targetMeta == SHOULD_BE_AIR)
                {
                    worldObj.setBlockToAir(basePos);
                }
                else if(baseState.getValue(SuperBlock.META) != targetMeta || baseBlock != fillBlock && fillBlock != null)
                {

                    worldObj.setBlockState(basePos, fillBlock.getDefaultState()
                            .withProperty(SuperBlock.META, targetMeta));

                }

            }
            else if(targetMeta != SHOULD_BE_AIR && LavaTerrainHelper.canLavaDisplace(baseState) && fillBlock != null)
            {
                worldObj.setBlockState(basePos, fillBlock.getDefaultState()
                        .withProperty(SuperBlock.META, targetMeta));

            }
        }
    }
}
