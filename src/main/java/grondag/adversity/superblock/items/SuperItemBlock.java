package grondag.adversity.superblock.items;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import grondag.adversity.Adversity;
import grondag.adversity.gui.AdversityGuiHandler;
import grondag.adversity.superblock.block.SuperBlock;
import grondag.adversity.superblock.block.SuperBlockNBTHelper;
import grondag.adversity.superblock.block.SuperBlockPlus;
import grondag.adversity.superblock.block.SuperModelTileEntity;
import grondag.adversity.superblock.block.SuperTileEntity;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.placement.IPlacementHandler;
import grondag.adversity.superblock.support.BlockSubstance;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
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
        if(!playerIn.capabilities.allowEdit) return EnumActionResult.FAIL;
        
        ItemStack stackIn = playerIn.getHeldItem(hand);

        if (stackIn.isEmpty()) return EnumActionResult.FAIL;
        
        ModelState modelState = getModelState(stackIn);
        
        if(modelState == null) return EnumActionResult.FAIL;
        
        IPlacementHandler placementHandler = modelState.getShape().getPlacementHandler();
        
        List<Pair<BlockPos, ItemStack>> placements = placementHandler.getPlacementResults(playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ, stackIn);

        if(placements.isEmpty()) return EnumActionResult.FAIL;
        
        SoundType soundtype = SuperItemBlock.getStackSubstance(stackIn).soundType;
        boolean didPlace = false;
        
        for(Pair<BlockPos, ItemStack> p : placements)
        {
            ItemStack placedStack = p.getRight();
            ModelState placedModelState = SuperItemBlock.getModelState(placedStack);
            BlockPos placedPos = p.getLeft();
            AxisAlignedBB axisalignedbb = placedModelState.getShape().meshFactory().collisionHandler().getCollisionBoundingBox(placedModelState);

            if(worldIn.checkNoEntityCollision(axisalignedbb.offset(placedPos)))
            {
                IBlockState placedState = this.block.getStateForPlacement(worldIn, placedPos, facing, hitX, hitY, hitZ, placedStack.getMetadata(), playerIn, hand);
                if (placeBlockAt(placedStack, playerIn, worldIn, placedPos, facing, hitX, hitY, hitZ, placedState))
                {
                    didPlace = true;
                    worldIn.playSound(playerIn, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    stackIn.shrink(1);
                    if(stackIn.isEmpty()) break;
                }
            }
        }

        return didPlace ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
     
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

        if(newState.getBlock() instanceof SuperBlockPlus)
        {
            SuperTileEntity blockTE = (SuperTileEntity)world.getTileEntity(pos);
            if (blockTE != null) 
            {
                if(blockTE instanceof SuperModelTileEntity)
                {
                    SuperModelTileEntity superTE = (SuperModelTileEntity)blockTE;
                    superTE.setLightValue(SuperItemBlock.getStackLightValue(stack));
                    superTE.setSubstance(SuperItemBlock.getStackSubstance(stack));
                }

                blockTE.setModelState(getModelState(stack));
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

    public static void setStackLightValue(ItemStack stack, int lightValue)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if(tag == null){
            tag = new NBTTagCompound();
        }
        SuperBlockNBTHelper.writeLightValue(tag, lightValue);
        stack.setTagCompound(tag);
    }
    
    public static byte getStackLightValue(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null 
                ? 0
                : SuperBlockNBTHelper.readLightValue(tag);
    }

    public static void setStackSubstance(ItemStack stack, BlockSubstance substance)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if(tag == null){
            tag = new NBTTagCompound();
        }
        SuperBlockNBTHelper.writeSubstance(tag, substance);
        stack.setTagCompound(tag);
    }
    
    public static BlockSubstance getStackSubstance(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null 
                ? BlockSubstance.FLEXSTONE
                : SuperBlockNBTHelper.readSubstance(tag);
    }
    
}
