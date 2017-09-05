package grondag.hard_science.superblock.items;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import grondag.hard_science.HardScience;
import grondag.hard_science.gui.ModGuiHandler;
import grondag.hard_science.init.ModBlocks;
import grondag.hard_science.init.ModItems;
import grondag.hard_science.library.serialization.SerializationManager;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.block.SuperModelTileEntity;
import grondag.hard_science.superblock.block.SuperTileEntity;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.placement.IPlacementHandler;
import grondag.hard_science.superblock.placement.PlacementItem;
import grondag.hard_science.superblock.varia.BlockSubstance;
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

    public SuperItemBlock(SuperBlock block) {
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

    /** static version */
    public static ModelState getStackModelState(ItemStack stack)
    {
        if(stack.getItem() instanceof SuperItemBlock)
        {
            NBTTagCompound tag = stack.getTagCompound();
            //WAILA or other mods might create a stack with no NBT
            if(tag != null)
            {
                ModelState modelState = SuperTileEntity.SERIALIZER_MODEL_STATE.deserializeNBT(tag);
                if(modelState != null) return modelState;
            }
            return ((SuperBlock)((SuperItemBlock)stack.getItem()).block).getDefaultModelState();
        }
        return null;
    }
    
    public static void setStackModelState(ItemStack stack, ModelState modelState)
    {
        if(stack.getItem() instanceof SuperItemBlock)
        {
            
            NBTTagCompound tag = stack.getTagCompound();
            if(tag == null)
            {
                tag = new NBTTagCompound();
            }
            SuperTileEntity.SERIALIZER_MODEL_STATE.serializeNBT(modelState, tag);
            stack.setTagCompound(tag);
        }
    }

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
        return SerializationManager.withoutServerTag(super.getNBTShareTag(stack));
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        if (world.isRemote) 
        {
            BlockPos blockpos = Minecraft.getMinecraft().objectMouseOver.getBlockPos();
            //if trying to place a block but too close, is annoying to get GUI
            //so only display if clicking on air
            if (blockpos != null 
                    && world.getBlockState(blockpos).getBlock() != ModBlocks.virtual_block
                    && world.getBlockState(blockpos).getMaterial() == Material.AIR 
                    && ((SuperBlock)this.block).hasAppearanceGui())
            {
                player.openGui(HardScience.INSTANCE, ModGuiHandler.ModGui.SUPERMODEL_ITEM.ordinal(), player.world, (int) player.posX, (int) player.posY, (int) player.posZ);
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
        
        ModelState modelState = getStackModelState(stackIn);
        
        if(modelState == null) return EnumActionResult.FAIL;
        
        IPlacementHandler placementHandler = modelState.getShape().getPlacementHandler();
        
        List<Pair<BlockPos, ItemStack>> placements = placementHandler.getPlacementResults(playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ, stackIn);

        if(placements.isEmpty()) return EnumActionResult.FAIL;
        
        SoundType soundtype = getPlacementSound(stackIn);
        boolean didPlace = false;
        
        for(Pair<BlockPos, ItemStack> p : placements)
        {
            ItemStack placedStack = p.getRight();
            ModelState placedModelState = SuperItemBlock.getStackModelState(placedStack);
            BlockPos placedPos = p.getLeft();
            AxisAlignedBB axisalignedbb = placedModelState.getShape().meshFactory().collisionHandler().getCollisionBoundingBox(placedModelState);

            if(worldIn.checkNoEntityCollision(axisalignedbb.offset(placedPos)))
            {
                IBlockState placedState = this.block.getStateForPlacement(worldIn, placedPos, facing, hitX, hitY, hitZ, placedStack.getMetadata(), playerIn, hand);
                if (placeBlockAt(placedStack, playerIn, worldIn, placedPos, facing, hitX, hitY, hitZ, placedState))
                {
                    didPlace = true;
                    worldIn.playSound(playerIn, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    if(!(playerIn.isCreative() || isVirtual(stackIn)))
                    {
                        stackIn.shrink(1);
                        if(stackIn.isEmpty()) break;
                    }
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
        SuperModelTileEntity.SERIALIZER_LIGHT_VALUE.serializeNBT((byte)lightValue, tag);
        stack.setTagCompound(tag);
    }
    
    public static byte getStackLightValue(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null 
                ? 0
                : SuperModelTileEntity.SERIALIZER_LIGHT_VALUE.deserializeNBT(tag);
    }

    public static void setStackSubstance(ItemStack stack, BlockSubstance substance)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if(tag == null){
            tag = new NBTTagCompound();
        }
        SuperModelTileEntity.SERIALIZER_SUBSTANCE.serializeNBT(substance, tag);
        stack.setTagCompound(tag);
    }
    
    public static BlockSubstance getStackSubstance(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null 
                ? BlockSubstance.FLEXSTONE
                : SuperModelTileEntity.SERIALIZER_SUBSTANCE.deserializeNBT(tag);
    }
    
    private static SoundType getPlacementSound(ItemStack stack)
    {
        return getStackSubstance(stack).soundType;
//        return isVirtual(stack) ? VirtualBlock.VIRTUAL_BLOCK_SOUND : getStackSubstance(stack).soundType;
    }
    
    public static boolean isVirtual(ItemStack stack)
    {
        return stack.getItem() == ModItems.virtual_block;
    }
    
}
