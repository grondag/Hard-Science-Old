package grondag.hard_science.superblock.placement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.hard_science.HardScience;
import grondag.hard_science.init.ModSuperModelBlocks;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.block.SuperModelBlock;
import grondag.hard_science.superblock.items.SuperItemBlock;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.varia.BlockSubstance;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;

/**
 * Intended control layout for placement items
 *
 *   Click 
 *    Done
 *       normal - not selecting: break blocks in region
 *       alt - undo last placement
 *       ctrl - select filter block
 *       normal - selecting: cancel selection
 *   
 *   Right Click
 *       select mode - ctrl: complete selection without placing
 *       select mode - none: selecting, complete and place selection
 *       normal mode - none: place according to current mode/selection
 *       normal mode - ctrl: start new selection
 *       normal mode - alt:  place with different species      
 *       
 *   R - normal: cycle block orientation
 *       ctrl: cycle selection floating / range
 *       alt: cycle region orientation / assist / fixed
 *       shift: reverse
 *
 *   B - normal: show block menu
 *       ctrl: don't use - activates MC narrator
 *       alt: toggle delete mode
 *       shift: reverse 
 *       
 *   V - ctrl: cycle filter mode 
 *       alt: cycle species handling
 *       normal: cycle selection mode
 *       shift: reverse
 *       
 */
public interface PlacementItem
{
    /**
     * Returns the SuperBlock instance of the item implementing this interface,
     * if it is an ItemBlock.  Could be null if it isn't an item block.
     * @return
     */
    public SuperBlock getSuperBlock();
    
    public int guiOrdinal();
    
    public default void displayGui(EntityPlayer player)
    {
        player.openGui(HardScience.INSTANCE, this.guiOrdinal(), player.world, (int) player.posX, (int) player.posY, (int) player.posZ);
    }
    
    public static void setBlockOrientationAxis(ItemStack stack, BlockOrientationAxis orientation)
    {
        orientation.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }

    /**
     * Returns PlacementItem held by player in either hand, or null if player isn't holding one.
     * If player is holding a PlacementItem in both hands, returns item in primary hand.
     */
    @Nullable
    public static ItemStack getHeldPlacementItem(EntityPlayer player)
    {
        ItemStack stack = Minecraft.getMinecraft().player.getHeldItemMainhand();
        
        if(stack.getItem() instanceof PlacementItem) return stack;
        
        
        stack = Minecraft.getMinecraft().player.getHeldItemOffhand();
        
        if(stack.getItem() instanceof PlacementItem) return stack;
        
        return null;
    }
    
    /**
     * Hides what type of shape we are using and just lets us know the axis.
     * Returns UP/DOWN if not applicable.
     */
    public static EnumFacing.Axis getBlockPlacementAxis(ItemStack stack)
    {
        switch(getStackModelState(stack).orientationType())
        {
        case AXIS:
            return getBlockOrientationAxis(stack).axis;
            
        case FACE:
            return getBlockOrientationFace(stack).face.getAxis();

        case NONE:
        case EDGE:
        case CORNER:
        default:
            return EnumFacing.Axis.Y;
        }    
    }
    
    /**
     * Hides what type of shape we are using and just lets us know the axis.
     * Returns false if not applicable.
     */
    public static boolean getBlockPlacementAxisIsInverted(ItemStack stack)
    {
        switch(getStackModelState(stack).orientationType())
        {
        case AXIS:
            return false;
            
        case FACE:
            return getBlockOrientationFace(stack).face.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE;

        case NONE:
        case EDGE:
        case CORNER:
        default:
            return false;
        }    
    }
    
    /**
     * Hides what type of shape we are using.
     */
    public static boolean isBlockOrientationDynamic(ItemStack stack)
    {
        switch(getStackModelState(stack).orientationType())
        {
        case AXIS:
            return getBlockOrientationAxis(stack) == BlockOrientationAxis.DYNAMIC;
            
        case CORNER:
            return getBlockOrientationCorner(stack) == BlockOrientationCorner.DYNAMIC;
            
        case EDGE:
            return getBlockOrientationEdge(stack) == BlockOrientationEdge.DYNAMIC;
            
        case FACE:
            return getBlockOrientationFace(stack) == BlockOrientationFace.DYNAMIC;

        case NONE:
        default:
            return false;
        }
    }
    
    /**
     * Hides what type of shape we are using.
     */
    public static boolean isBlockOrientationFixed(ItemStack stack)
    {
        switch(getStackModelState(stack).orientationType())
        {
        case AXIS:
            return getBlockOrientationAxis(stack).isFixed();
            
        case CORNER:
            return getBlockOrientationCorner(stack).isFixed();
            
        case EDGE:
            return getBlockOrientationEdge(stack).isFixed();
            
        case FACE:
            return getBlockOrientationFace(stack).isFixed();

        case NONE:
        default:
            return false;
        }
    }
    
    /**
     * Hides what type of shape we are using.
     */
    public static boolean isBlockOrientationMatchClosest(ItemStack stack)
    {
        switch(getStackModelState(stack).orientationType())
        {
        case AXIS:
            return getBlockOrientationAxis(stack) == BlockOrientationAxis.MATCH_CLOSEST;
            
        case CORNER:
            return getBlockOrientationCorner(stack) == BlockOrientationCorner.MATCH_CLOSEST;
            
        case EDGE:
            return getBlockOrientationEdge(stack) == BlockOrientationEdge.MATCH_CLOSEST;
            
        case FACE:
            return getBlockOrientationFace(stack) == BlockOrientationFace.MATCH_CLOSEST;

        case NONE:
        default:
            return false;
        }
    }
    
    public static BlockOrientationAxis getBlockOrientationAxis(ItemStack stack)
    {
        return BlockOrientationAxis.DYNAMIC.deserializeNBT(stack.getTagCompound());
    }
    
    public static void cycleBlockOrientationAxis(ItemStack stack, boolean reverse)
    {
        setBlockOrientationAxis(stack, reverse ? Useful.prevEnumValue(getBlockOrientationAxis(stack)) : Useful.nextEnumValue(getBlockOrientationAxis(stack)));
    }
    
    public static void setBlockOrientationFace(ItemStack stack, BlockOrientationFace orientation)
    {
        orientation.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }

    public static BlockOrientationFace getBlockOrientationFace(ItemStack stack)
    {
        return BlockOrientationFace.DYNAMIC.deserializeNBT(stack.getTagCompound());
    }
    
    public static void cycleBlockOrientationFace(ItemStack stack, boolean reverse)
    {
        setBlockOrientationFace(stack, reverse ? Useful.prevEnumValue(getBlockOrientationFace(stack)) : Useful.nextEnumValue(getBlockOrientationFace(stack)));
    }
    
    public static void setBlockOrientationEdge(ItemStack stack, BlockOrientationEdge orientation)
    {
        orientation.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }

    public static BlockOrientationEdge getBlockOrientationEdge(ItemStack stack)
    {
        return BlockOrientationEdge.DYNAMIC.deserializeNBT(stack.getTagCompound());
    }
    
    public static void cycleBlockOrientationEdge(ItemStack stack, boolean reverse)
    {
        setBlockOrientationEdge(stack, reverse ? Useful.prevEnumValue(getBlockOrientationEdge(stack)) : Useful.nextEnumValue(getBlockOrientationEdge(stack)));
    }
    
    public static void setBlockOrientationCorner(ItemStack stack, BlockOrientationCorner orientation)
    {
        orientation.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }

    public static BlockOrientationCorner getBlockOrientationCorner(ItemStack stack)
    {
        return BlockOrientationCorner.DYNAMIC.deserializeNBT(stack.getTagCompound());
    }
    
    public static void cycleBlockOrientationCorner(ItemStack stack, boolean reverse)
    {
        setBlockOrientationCorner(stack, reverse ? Useful.prevEnumValue(getBlockOrientationCorner(stack)) : Useful.nextEnumValue(getBlockOrientationCorner(stack)));
    }
    
    /**
     * Context-sensitive version - calls appropriate cycle method based on shape type
     */
    public static void cycleBlockOrientation(ItemStack stack, boolean reverse)
    {
        switch(getStackModelState(stack).orientationType())
        {
        case AXIS:
            cycleBlockOrientationAxis(stack, reverse);
            break;
            
        case CORNER:
            cycleBlockOrientationCorner(stack, reverse);
            break;
            
        case EDGE:
            cycleBlockOrientationEdge(stack, reverse);
            break;
            
        case FACE:
            cycleBlockOrientationFace(stack, reverse);
            break;

        case NONE:
        default:
            break;
        }
    }
    
    /**
     * Context-sensitive localized name of current orientation.
     */
    public static String blockOrientationLocalizedName(ItemStack stack)
    {
        switch(getStackModelState(stack).orientationType())
        {
        case AXIS:
            return getBlockOrientationAxis(stack).localizedName();
            
        case CORNER:
            return getBlockOrientationCorner(stack).localizedName();
            
        case EDGE:
            return getBlockOrientationEdge(stack).localizedName();
            
        case FACE:
            return getBlockOrientationFace(stack).localizedName();

        case NONE:
        default:
            return I18n.translateToLocal("placement.orientation.none");
        }
    }
    
    public static void setRegionOrientation(ItemStack stack, RegionOrientation orientation)
    {
        orientation.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }

    /**
     * Always returns XYZ during selection operations because display wouldn't match what user is doing otherwise.
     */
    public static RegionOrientation getRegionOrientation(ItemStack stack)
    {
        return operationInProgress(stack) == PlacementOperation.NONE ? RegionOrientation.XYZ.deserializeNBT(stack.getTagCompound()) : RegionOrientation.XYZ;
    }
    
    public static void cycleRegionOrientation(ItemStack stack, boolean reverse)
    {
        setRegionOrientation(stack, reverse ? Useful.prevEnumValue(getRegionOrientation(stack)) : Useful.nextEnumValue(getRegionOrientation(stack)));
    }
    
    public static void setSelectionMode(ItemStack stack, SelectionMode mode)
    {
        mode.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }
    
    public static SelectionMode getSelectionMode(ItemStack stack)
    {
        return SelectionMode.FILL_REGION.deserializeNBT(stack.getTagCompound());
    }
    
    public static void cycleSelectionMode(ItemStack stack, boolean reverse)
    {
        setSelectionMode(stack, reverse ? Useful.prevEnumValue(getSelectionMode(stack)) : Useful.nextEnumValue(getSelectionMode(stack)));
    }
    
    public static void setFilterMode(ItemStack stack, FilterMode mode)
    {
        mode.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }
    
    public static FilterMode getFilterMode(ItemStack stack)
    {
        return FilterMode.FILL_REPLACEABLE.deserializeNBT(stack.getTagCompound());
    }
    
    public static void cycleFilterMode(ItemStack stack, boolean reverse)
    {
        setFilterMode(stack, reverse ? Useful.prevEnumValue(getFilterMode(stack)) : Useful.nextEnumValue(getFilterMode(stack)));
    }
    
    public static void setSpeciesMode(ItemStack stack, SpeciesMode mode)
    {
        mode.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }
    
    public static SpeciesMode getSpeciesMode(ItemStack stack)
    {
        return SpeciesMode.MATCH_CLICKED.deserializeNBT(stack.getTagCompound());
    }
    
    public static void cycleSpeciesMode(ItemStack stack, boolean reverse)
    {
        setSpeciesMode(stack, reverse ? Useful.prevEnumValue(getSpeciesMode(stack)) : Useful.nextEnumValue(getSpeciesMode(stack)));
    }
    
    public static void setStackLightValue(ItemStack stack, int lightValue)
    {
        Useful.getOrCreateTagCompound(stack).setByte(ModNBTTag.SUPER_MODEL_LIGHT_VALUE, (byte)lightValue);
    }
    
    public static byte getStackLightValue(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null ? 0 : tag.getByte(ModNBTTag.SUPER_MODEL_LIGHT_VALUE);
    }

    public static void setStackSubstance(ItemStack stack, BlockSubstance substance)
    {
        if(substance != null) substance.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }
    
    public static BlockSubstance getStackSubstance(ItemStack stack)
    {
        return BlockSubstance.FLEXSTONE.deserializeNBT(stack.getTagCompound());
    }
    
    /**
     * Gets the appropriate super block to place from a given item stack if it is
     * a SuperItemBlock stack. Null otherwise.
     * May be different than the stack block because SuperModel in-world blocks are dependent on substance and other properties stored in the stack.
     */
    public static IBlockState getPlacementBlockStateFromStack(ItemStack stack)
    {

        // supermodel blocks may need to use a different block instance depending on model/substance
        // handle this here by substituting a stack different than what we received
        Item item = stack.getItem();
        
        if(item instanceof SuperItemBlock)
        {
            ModelState modelState = PlacementItem.getStackModelState(stack);
            if(modelState == null) return null;

            SuperBlock targetBlock = ((SuperBlock)((SuperItemBlock)stack.getItem()).getBlock());
            
            if(!targetBlock.isVirtual() && targetBlock instanceof SuperModelBlock)
            {
                BlockSubstance substance = PlacementItem.getStackSubstance(stack);
                if(substance == null) return null;
                targetBlock = ModSuperModelBlocks.findAppropriateSuperModelBlock(substance, modelState);
            }
            
            return targetBlock.getStateFromMeta(stack.getMetadata());
        }
        else if(item instanceof ItemBlock)
        {
            Block targetBlock = (Block)((ItemBlock)stack.getItem()).getBlock();
            return targetBlock.getStateFromMeta(stack.getMetadata());
        }
        else
        {
            return Blocks.AIR.getDefaultState();
        }
            
    }
    
    public static ModelState getStackModelState(ItemStack stack)
    {
        ModelState stackState = ModelState.deserializeFromNBTIfPresent(stack.getTagCompound());
        
        //WAILA or other mods might create a stack with no NBT
        if(stackState != null) return stackState;
        
        if(stack.getItem() instanceof SuperItemBlock)
        {
            return ((SuperBlock)((SuperItemBlock)stack.getItem()).getBlock()).getDefaultModelState();
        }
        
        return null;
    }
    
    public static void setStackModelState(ItemStack stack, ModelState modelState)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if(modelState == null)
        {
            if(tag != null) tag.removeTag(ModNBTTag.MODEL_STATE);
            return;
        }
        
        if(tag == null)
        {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        
        modelState.serializeNBT(tag);
    }
    
    public static boolean isPlacementItem(ItemStack stack)
    {
        return stack.getItem() instanceof PlacementItem;
    }

    public static void toggleDeleteMode(ItemStack stack)
    {
        setDeleteModeEnabled(stack, !isDeleteModeEnabled(stack));
    }
    
    public static boolean isDeleteModeEnabled(ItemStack stack)
    {
        return Useful.getOrCreateTagCompound(stack).getBoolean(ModNBTTag.PLACEMENT_DELETE_ENABLED);
    }
    
    public static void setDeleteModeEnabled(ItemStack stack, boolean enabled)
    {
        Useful.getOrCreateTagCompound(stack).setBoolean(ModNBTTag.PLACEMENT_DELETE_ENABLED, enabled);
    }
    
    public static void selectPlacementRegionStart(ItemStack stack, BlockPos pos, boolean isCenter)
    {
        NBTTagCompound tag = Useful.getOrCreateTagCompound(stack);
        PlacementOperation.SELECTING.serializeNBT(tag);
        RegionOrientation.XYZ.serializeNBT(tag);
        
        SelectionMode currentMode = getSelectionMode(stack);
        // assume user wants to fill a region  and
        // change mode to region fill if not already set to FILL_REGION or HOLLOW_FILL
        if(!currentMode.usesSelectionRegion) SelectionMode.FILL_REGION.serializeNBT(tag);
        
        tag.setLong(ModNBTTag.PLACEMENT_REGION_OPERATION_POSITION, pos.toLong());
    }
    
    public static void selectPlacementRegionCancel(ItemStack stack)
    {
        NBTTagCompound tag = Useful.getOrCreateTagCompound(stack);
        PlacementOperation.NONE.serializeNBT(tag);
        tag.removeTag(ModNBTTag.PLACEMENT_REGION_OPERATION_POSITION);
    }
    
    /**
     * See {@link #placementRegionPosition(ItemStack)} for some explanation.
     */
    public static BlockPos getPlayerRelativeRegionFromEndPoints(BlockPos from, BlockPos to, EntityPlayer player)
    {
        BlockPos selectedPos = new BlockPos(1 + Math.abs(from.getX() - to.getX()), 1 + Math.abs(from.getY() - to.getY()), 1 + Math.abs(from.getZ() - to.getZ()));
        return(player.getHorizontalFacing().getAxis() == EnumFacing.Axis.Z)
                ? selectedPos
                : new BlockPos(selectedPos.getZ(), selectedPos.getY(), selectedPos.getX());
    }
    
    public static void selectPlacementRegionFinish(ItemStack stack, EntityPlayer player, BlockPos pos, boolean isCenter)
    {
        NBTTagCompound tag = Useful.getOrCreateTagCompound(stack);
        BlockPos startPos = BlockPos.fromLong(tag.getLong(ModNBTTag.PLACEMENT_REGION_OPERATION_POSITION));
        BlockPos selectedPos = getPlayerRelativeRegionFromEndPoints(pos, startPos, player);
        RegionOrientation.XYZ.serializeNBT(tag);
        
        if(selectedPos.getX() == 1 && selectedPos.getY() == 1 && selectedPos.getZ() == 1 )
        {
            SelectionMode.ON_CLICKED_FACE.serializeNBT(tag);
        }
        else
        {
            tag.setLong(ModNBTTag.PLACEMENT_REGION_PLACEMENT_POSITION, selectedPos.toLong());
        }
   
        PlacementOperation.NONE.serializeNBT(tag);
        tag.removeTag(ModNBTTag.PLACEMENT_REGION_OPERATION_POSITION);
    }
    
    public static PlacementOperation operationInProgress(ItemStack stack)
    {
        return PlacementOperation.NONE.deserializeNBT(stack.getTagCompound());
    }
 
    /**
     * X is left/right relative to player and Z is depth in direction player is facing.<br>
     * Y is always vertical height.<br>
     * All are always positive numbers.<br>
     * Region rotation is or isn't applied according to parameter.<br>
     */
    @Nonnull
    public static BlockPos placementRegionPosition(ItemStack stack, boolean applyRegionRotation)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if(tag == null || !tag.hasKey(ModNBTTag.PLACEMENT_REGION_PLACEMENT_POSITION)) return new BlockPos(1, 1, 1);
        
        BlockPos result = BlockPos.fromLong(tag.getLong(ModNBTTag.PLACEMENT_REGION_PLACEMENT_POSITION));
        
        return applyRegionRotation ? getRegionOrientation(stack).rotatedRegionPos(result) : result;
    }
    
    @Nullable
    public static BlockPos operationPosition(ItemStack stack)
    {
        if (operationInProgress(stack) == PlacementOperation.NONE) return null;
                        
        NBTTagCompound tag = stack.getTagCompound();
        if(tag == null || !tag.hasKey(ModNBTTag.PLACEMENT_REGION_OPERATION_POSITION)) return null;
        
        return BlockPos.fromLong(tag.getLong(ModNBTTag.PLACEMENT_REGION_OPERATION_POSITION));
    }
    
//    /**
//     * Meant for use by packet handler
//     */
//    public static void selectedRegionUpdate(ItemStack stack, BlockPos selectedPos)
//    {
//        NBTTagCompound tag = Useful.getOrCreateTagCompound(stack);
//        if(selectedPos == null)
//        {
//            tag.removeTag(ModNBTTag.PLACEMENT_REGION_PLACEMENT_POSITION);
//        }
//        else
//        {
//            tag.setLong(ModNBTTag.PLACEMENT_REGION_PLACEMENT_POSITION, selectedPos.toLong());
//        }
//    }
    
    public static String selectedRegionLocalizedName(ItemStack stack)
    {
        switch(getSelectionMode(stack))
        {
        case FILL_REGION:
            BlockPos pos = placementRegionPosition(stack, false);
            return I18n.translateToLocalFormatted("placement.message.region_box", pos.getX(), pos.getY(), pos.getZ());
            
        case ON_CLICKED_SURFACE:
            return I18n.translateToLocal("placement.message.region_additive");

        case ON_CLICKED_FACE:
        default:
            return I18n.translateToLocal("placement.message.region_single");
        
        }
    }
    
    /**
     * 0 means non-floating
     */
    public static void setSelectionTargetRange(ItemStack stack, int range)
    {
        // subtract because external values jump from 0 to 2
        if(range > 0) range--;
        Useful.getOrCreateTagCompound(stack).setByte(ModNBTTag.PLACEMENT_REGION_FLOATING_RANGE, (byte)range);
    }
    
    /**
     * 0 means non-floating
     */
    public static int getFloatingSelectionRange(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        int range = tag == null ? 0 : MathHelper.clamp(tag.getByte(ModNBTTag.PLACEMENT_REGION_FLOATING_RANGE), 0, 4);
        return range == 0 ? 0 : range + 1;
    }
    
    public static void cycleSelectionTargetRange(ItemStack stack, boolean reverse)
    {
        NBTTagCompound tag = Useful.getOrCreateTagCompound(stack);
        int range = tag.getByte(ModNBTTag.PLACEMENT_REGION_FLOATING_RANGE) + (reverse ? -1 : 1);
        if(range > 4) range = 0;
        if(range < 0) range = 4;
        tag.setByte(ModNBTTag.PLACEMENT_REGION_FLOATING_RANGE, (byte)range);
    }
    
    public static boolean isFloatingSelectionEnabled(ItemStack stack)
    {
        return getFloatingSelectionRange(stack) != 0;
    }
    
    /**
     * Will return a meaningless result if floating selection is disabled.
     */
    public static BlockPos getFloatingSelectionBlockPos(ItemStack stack, EntityLivingBase entity)
    {
        int range = getFloatingSelectionRange(stack);
        
        Vec3d look = entity.getLookVec();
        int blockX = MathHelper.floor(look.x * range + entity.posX);
        int blockY = MathHelper.floor(look.y * range + entity.posY + entity.getEyeHeight());
        int blockZ = MathHelper.floor(look.z * range + entity.posZ);

        return new BlockPos(blockX, blockY, blockZ);
    }
}
