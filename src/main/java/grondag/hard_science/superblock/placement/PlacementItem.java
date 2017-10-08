package grondag.hard_science.superblock.placement;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.items.SuperItemBlock;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.varia.BlockSubstance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;

/**
 * Intended control layout for placement items
 *
 *   Click 
 *       normal - not selecting: break block
 *       normal - selecting: cancel selection
 *       alt - undo last placement
 *       ctrl - excavate region
 *   
 *   Right Click
 *       normal - not selecting: place according to current slection
 *       normal - selecting, complete and place selection
 *       alt - place with different species
 *       ctrl - start new selection
 *       
 *   R - cycle block orientation
 *       alt: reverse
 *       ctrl: cycle region orientation
 *       ctrl+alt: reverse cycle region orientation
 *       
 *   V - normal: cycle region/shape history
 *       alt: reverse 
 *       
 *   B - toggle placement mode  SINGLE/FILL REGION/FOLLOW
 */
public interface PlacementItem
{
    public static void setOrientationAxis(ItemStack stack, PlacementOrientationAxis orientation)
    {
        orientation.serializeNBT(Useful.getOrCreateTagCompound(stack));
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
            return getOrientationAxis(stack).axis;
            
        case FACE:
            return getOrientationFace(stack).face.getAxis();

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
            return getOrientationFace(stack).face.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE;

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
            return getOrientationAxis(stack) == PlacementOrientationAxis.DYNAMIC;
            
        case CORNER:
            return getOrientationCorner(stack) == PlacementOrientationCorner.DYNAMIC;
            
        case EDGE:
            return getOrientationEdge(stack) == PlacementOrientationEdge.DYNAMIC;
            
        case FACE:
            return getOrientationFace(stack) == PlacementOrientationFace.DYNAMIC;

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
            return getOrientationAxis(stack).isFixed();
            
        case CORNER:
            return getOrientationCorner(stack).isFixed();
            
        case EDGE:
            return getOrientationEdge(stack).isFixed();
            
        case FACE:
            return getOrientationFace(stack).isFixed();

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
            return getOrientationAxis(stack) == PlacementOrientationAxis.MATCH_CLOSEST;
            
        case CORNER:
            return getOrientationCorner(stack) == PlacementOrientationCorner.MATCH_CLOSEST;
            
        case EDGE:
            return getOrientationEdge(stack) == PlacementOrientationEdge.MATCH_CLOSEST;
            
        case FACE:
            return getOrientationFace(stack) == PlacementOrientationFace.MATCH_CLOSEST;

        case NONE:
        default:
            return false;
        }
    }
    
    public static PlacementOrientationAxis getOrientationAxis(ItemStack stack)
    {
        return PlacementOrientationAxis.DYNAMIC.deserializeNBT(stack.getTagCompound());
    }
    
    public static void cycleOrientationAxis(ItemStack stack, boolean reverse)
    {
        setOrientationAxis(stack, reverse ? Useful.prevEnumValue(getOrientationAxis(stack)) : Useful.nextEnumValue(getOrientationAxis(stack)));
    }
    
    public static void setOrientationFace(ItemStack stack, PlacementOrientationFace orientation)
    {
        orientation.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }

    public static PlacementOrientationFace getOrientationFace(ItemStack stack)
    {
        return PlacementOrientationFace.DYNAMIC.deserializeNBT(stack.getTagCompound());
    }
    
    public static void cycleOrientationFace(ItemStack stack, boolean reverse)
    {
        setOrientationFace(stack, reverse ? Useful.prevEnumValue(getOrientationFace(stack)) : Useful.nextEnumValue(getOrientationFace(stack)));
    }
    
    public static void setOrientationEdge(ItemStack stack, PlacementOrientationEdge orientation)
    {
        orientation.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }

    public static PlacementOrientationEdge getOrientationEdge(ItemStack stack)
    {
        return PlacementOrientationEdge.DYNAMIC.deserializeNBT(stack.getTagCompound());
    }
    
    public static void cycleOrientationEdge(ItemStack stack, boolean reverse)
    {
        setOrientationEdge(stack, reverse ? Useful.prevEnumValue(getOrientationEdge(stack)) : Useful.nextEnumValue(getOrientationEdge(stack)));
    }
    
    public static void setOrientationCorner(ItemStack stack, PlacementOrientationCorner orientation)
    {
        orientation.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }

    public static PlacementOrientationCorner getOrientationCorner(ItemStack stack)
    {
        return PlacementOrientationCorner.DYNAMIC.deserializeNBT(stack.getTagCompound());
    }
    
    public static void cycleOrientationCorner(ItemStack stack, boolean reverse)
    {
        setOrientationCorner(stack, reverse ? Useful.prevEnumValue(getOrientationCorner(stack)) : Useful.nextEnumValue(getOrientationCorner(stack)));
    }
    
    /**
     * Context-sensitive version - calls appropriate cycle method based on shape type
     */
    public static void cycleOrientation(ItemStack stack, boolean reverse)
    {
        switch(getStackModelState(stack).orientationType())
        {
        case AXIS:
            cycleOrientationAxis(stack, reverse);
            break;
            
        case CORNER:
            cycleOrientationCorner(stack, reverse);
            break;
            
        case EDGE:
            cycleOrientationEdge(stack, reverse);
            break;
            
        case FACE:
            cycleOrientationFace(stack, reverse);
            break;

        case NONE:
        default:
            break;
        }
    }
    
    /**
     * Context-sensitive localized name of current orientation.
     */
    public static String orientationLocalizedName(ItemStack stack)
    {
        switch(getStackModelState(stack).orientationType())
        {
        case AXIS:
            return getOrientationAxis(stack).localizedName();
            
        case CORNER:
            return getOrientationCorner(stack).localizedName();
            
        case EDGE:
            return getOrientationEdge(stack).localizedName();
            
        case FACE:
            return getOrientationFace(stack).localizedName();

        case NONE:
        default:
            return I18n.translateToLocal("placement.orientation.none");
        }
    }
    
    public static void setMode(ItemStack stack, PlacementMode mode)
    {
        mode.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }
    
    public static PlacementMode getMode(ItemStack stack)
    {
        return PlacementMode.FILL_REGION.deserializeNBT(stack.getTagCompound());
    }
    
    public static void cycleMode(ItemStack stack, boolean reverse)
    {
        setMode(stack, reverse ? Useful.prevEnumValue(getMode(stack)) : Useful.nextEnumValue(getMode(stack)));
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

    /**
     * Returns true if caused a change
     */
    public static boolean cycleHistory(ItemStack stack, boolean reverse)
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    public static void selectRegionStart(ItemStack stack, BlockPos pos, boolean isCenter)
    {
        NBTTagCompound tag = Useful.getOrCreateTagCompound(stack);
        tag.setBoolean(ModNBTTag.PLACEMENT_REGION_IN_PROGRESS, true);
        tag.setLong(ModNBTTag.PLACEMENT_REGION_START_POSITION, pos.toLong());
    }
    
    public static void selectRegionCancel(ItemStack stack)
    {
        NBTTagCompound tag = Useful.getOrCreateTagCompound(stack);
        tag.setBoolean(ModNBTTag.PLACEMENT_REGION_IN_PROGRESS, false);
        tag.removeTag(ModNBTTag.PLACEMENT_REGION_START_POSITION);
    }
    
    public static void selectRegionFinish(ItemStack stack, BlockPos pos, boolean isCenter)
    {
        NBTTagCompound tag = Useful.getOrCreateTagCompound(stack);
        BlockPos startPos = BlockPos.fromLong(tag.getLong(ModNBTTag.PLACEMENT_REGION_START_POSITION));
        BlockPos selectedPos = new BlockPos(1 + Math.abs(pos.getX() - startPos.getX()), 1 + Math.abs(pos.getY() - startPos.getY()), 1 + Math.abs(pos.getZ() - startPos.getZ()));
        if(selectedPos.getX() == 1 && selectedPos.getY() == 1 && selectedPos.getZ() == 1 )
        {
            PlacementMode.SINGLE_BLOCK.serializeNBT(tag);
        }
        else
        {
            PlacementMode.FILL_REGION.serializeNBT(tag);
            tag.setLong(ModNBTTag.PLACEMENT_REGION_SELECTED_POSITION, selectedPos.toLong());
        }
   
        tag.setBoolean(ModNBTTag.PLACEMENT_REGION_IN_PROGRESS, false);
        tag.removeTag(ModNBTTag.PLACEMENT_REGION_START_POSITION);
    }
    
    public static boolean isSelectRegionInProgress(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null ? false : tag.getBoolean(ModNBTTag.PLACEMENT_REGION_IN_PROGRESS);
    }
    
    public static BlockPos selectedRegion(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null ? new BlockPos(1, 1, 1) : BlockPos.fromLong(tag.getLong(ModNBTTag.PLACEMENT_REGION_SELECTED_POSITION));
    }
    
    public static BlockPos selectedRegionStart(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null 
                ? null 
                : tag.getBoolean(ModNBTTag.PLACEMENT_REGION_IN_PROGRESS) 
                    ? BlockPos.fromLong(tag.getLong(ModNBTTag.PLACEMENT_REGION_SELECTED_POSITION))
                    : null;
    }
    
//    /**
//     * Meant for use by packet handler
//     */
//    public static void selectedRegionUpdate(ItemStack stack, BlockPos selectedPos)
//    {
//        NBTTagCompound tag = Useful.getOrCreateTagCompound(stack);
//        if(selectedPos == null)
//        {
//            tag.removeTag(ModNBTTag.PLACEMENT_REGION_SELECTED_POSITION);
//        }
//        else
//        {
//            tag.setLong(ModNBTTag.PLACEMENT_REGION_SELECTED_POSITION, selectedPos.toLong());
//        }
//    }
    
    public static String selectedRegionLocalizedName(ItemStack stack)
    {
        switch(getMode(stack))
        {
        case FILL_REGION:
            BlockPos pos = selectedRegion(stack);
            return I18n.translateToLocalFormatted("placement.message.region_box", pos.getX(), pos.getY(), pos.getZ());
            
        case ADD_TO_EXISTING:
            return I18n.translateToLocal("placement.message.region_additive");

        case SINGLE_BLOCK:
        default:
            return I18n.translateToLocal("placement.message.region_single");
        
        }
    }
}
