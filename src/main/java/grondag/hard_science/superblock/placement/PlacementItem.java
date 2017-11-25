package grondag.hard_science.superblock.placement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.realmsclient.util.Pair;

import grondag.hard_science.HardScience;
import grondag.hard_science.init.ModBlocks;
import grondag.hard_science.init.ModSuperModelBlocks;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.BinaryEnumSet;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.library.world.PackedBlockPos;
import grondag.hard_science.library.world.Rotation;
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
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

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
 *       select mode - none: complete selection
 *       select mode - ctrl: restart selection
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
    /////////////////////////////////////////////////////
    // STATIC MEMBERS
    /////////////////////////////////////////////////////
    
    public static BinaryEnumSet<PlacementItemFeature> BENUMSET_FEATURES = new BinaryEnumSet<PlacementItemFeature>(PlacementItemFeature.class);
    
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
    
    public static boolean isPlacementItem(ItemStack stack)
    {
        return stack.getItem() instanceof PlacementItem;
    }
    
    public static PlacementItem getPlacementItem(ItemStack stack)
    {
        return isPlacementItem(stack) ? (PlacementItem)stack.getItem() : null;
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
    
    /////////////////////////////////////////////////////
    // ABSTRACT MEMBERS
    /////////////////////////////////////////////////////

    /**
     * Returns the SuperBlock instance of the item implementing this interface,
     * if it is an ItemBlock.  Could be null if it isn't an item block.
     * @return
     */
    public SuperBlock getSuperBlock();
    
    /** True if item places air blocks or carves empty space in CSG blocks */
    public boolean isExcavator(ItemStack placedStack);
    
    /** True if only places/affects virtual blocks. */
    public boolean isVirtual(ItemStack stack);
    
    /////////////////////////////////////////////////////
    // DEFAULT MEMBERS
    /////////////////////////////////////////////////////
    
    /**
     * Used with {@link #BENUMSET_FEATURES} to know what features are supported.
     * @param stack 
     */
    public default int featureFlags(ItemStack stack)
    {
        return 0xFFFFFFFF;
    }
    
    /** won't be called unless gui feature is supported */
    public default int guiOrdinal()
    {
        return 0;
    }
    
    public default boolean isGuiSupported(ItemStack stack)
    {
        return BENUMSET_FEATURES.isFlagSetForValue(PlacementItemFeature.GUI, this.featureFlags(stack));
    }
    
    public default void displayGui(EntityPlayer player)
    {
        if(!isGuiSupported(PlacementItem.getHeldPlacementItem(player))) return;
        player.openGui(HardScience.INSTANCE, this.guiOrdinal(), player.world, (int) player.posX, (int) player.posY, (int) player.posZ);
    }
    
    public default boolean isBlockOrientationSupported(ItemStack stack)
    {
        return BENUMSET_FEATURES.isFlagSetForValue(PlacementItemFeature.BLOCK_ORIENTATION, this.featureFlags(stack));
    }
    
    public default void setBlockOrientationAxis(ItemStack stack, BlockOrientationAxis orientation)
    {
        if(!isBlockOrientationSupported(stack)) return;
        
        orientation.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }
    
    /**
     * Hides what type of shape we are using and just lets us know the axis.
     * Returns UP/DOWN if not applicable.
     */
    public default EnumFacing.Axis getBlockPlacementAxis(ItemStack stack)
    {
        if(!isBlockOrientationSupported(stack)) return EnumFacing.Axis.Y;

        switch(getStackModelState(stack).orientationType())
        {
        case AXIS:
            return this.getBlockOrientationAxis(stack).axis;
            
        case FACE:
            return this.getBlockOrientationFace(stack).face.getAxis();

        case EDGE:
            //TODO
        case CORNER:
            //TODO
            
        case NONE:
        default:
            return EnumFacing.Axis.Y;
        }    
    }
    
    /**
     * Hides what type of shape we are using and just lets us know the axis.
     * Returns false if not applicable.
     */
    public default boolean getBlockPlacementAxisIsInverted(ItemStack stack)
    {
        if(!isBlockOrientationSupported(stack)) return false;

        switch(getStackModelState(stack).orientationType())
        {
        case AXIS:
            return false;
            
        case FACE:
            return this.getBlockOrientationFace(stack).face.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE;

        case EDGE:
            // FIXME: is this right?
            return this.getBlockOrientationEdge(stack).edge.face1.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE;
        
        case CORNER:
            //TODO

        case NONE:
        default:
            return false;
        }    
    }
    
    public default Rotation getBlockPlacementRotation(ItemStack stack)
    {
        if(!isBlockOrientationSupported(stack)) return Rotation.ROTATE_NONE;

        switch(getStackModelState(stack).orientationType())
        {
            case EDGE:
                return this.getBlockOrientationEdge(stack).edge.modelRotation;
                
            case CORNER:
                //TODO

            case NONE:
            case FACE:
            case AXIS:
            default:
                return Rotation.ROTATE_NONE;
        }
    }
    
    /**
     * Hides what type of shape we are using.
     */
    public default boolean isBlockOrientationDynamic(ItemStack stack)
    {
        if(!isBlockOrientationSupported(stack)) return false;

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
    public default boolean isBlockOrientationFixed(ItemStack stack)
    {
        if(!isBlockOrientationSupported(stack)) return false;

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
    public default boolean isBlockOrientationMatchClosest(ItemStack stack)
    {
        if(!isBlockOrientationSupported(stack)) return false;

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
    
    public default BlockOrientationAxis getBlockOrientationAxis(ItemStack stack)
    {
        return BlockOrientationAxis.DYNAMIC.deserializeNBT(stack.getTagCompound());
    }
    
    /**
     * Return false if this item doesn't support this feature.
     */
    public default boolean cycleBlockOrientationAxis(ItemStack stack, boolean reverse)
    {
        if(!isBlockOrientationSupported(stack)) return false;

        setBlockOrientationAxis(stack, reverse ? Useful.prevEnumValue(getBlockOrientationAxis(stack)) : Useful.nextEnumValue(getBlockOrientationAxis(stack)));
        return true;
    }
    
    public default void setBlockOrientationFace(ItemStack stack, BlockOrientationFace orientation)
    {
        if(!isBlockOrientationSupported(stack)) return;
        orientation.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }

    public default BlockOrientationFace getBlockOrientationFace(ItemStack stack)
    {
        return BlockOrientationFace.DYNAMIC.deserializeNBT(stack.getTagCompound());
    }
    
    /**
     * Return false if this item doesn't support this feature.
     */
    public default boolean cycleBlockOrientationFace(ItemStack stack, boolean reverse)
    {
        if(!isBlockOrientationSupported(stack)) return false;
        
        setBlockOrientationFace(stack, reverse ? Useful.prevEnumValue(getBlockOrientationFace(stack)) : Useful.nextEnumValue(getBlockOrientationFace(stack)));
        return true;
    }
    
    public default void setBlockOrientationEdge(ItemStack stack, BlockOrientationEdge orientation)
    {
        if(!isBlockOrientationSupported(stack)) return;
        
        orientation.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }

    public default BlockOrientationEdge getBlockOrientationEdge(ItemStack stack)
    {
        return BlockOrientationEdge.DYNAMIC.deserializeNBT(stack.getTagCompound());
    }
    
    /**
     * Return false if this item doesn't support this feature.
     */
    public default boolean cycleBlockOrientationEdge(ItemStack stack, boolean reverse)
    {
        if(!isBlockOrientationSupported(stack)) return false;
        
        setBlockOrientationEdge(stack, reverse ? Useful.prevEnumValue(getBlockOrientationEdge(stack)) : Useful.nextEnumValue(getBlockOrientationEdge(stack)));
        return true;
    }
    
    public default void setBlockOrientationCorner(ItemStack stack, BlockOrientationCorner orientation)
    {
        if(!isBlockOrientationSupported(stack)) return;
        
        orientation.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }

    public default BlockOrientationCorner getBlockOrientationCorner(ItemStack stack)
    {
        return BlockOrientationCorner.DYNAMIC.deserializeNBT(stack.getTagCompound());
    }
    
    /**
     * Return false if this item doesn't support this feature.
     */
    public default boolean cycleBlockOrientationCorner(ItemStack stack, boolean reverse)
    {
        if(!isBlockOrientationSupported(stack)) return false;
        
        setBlockOrientationCorner(stack, reverse ? Useful.prevEnumValue(getBlockOrientationCorner(stack)) : Useful.nextEnumValue(getBlockOrientationCorner(stack)));
        return true;
    }
    
    /**
     * Context-sensitive version - calls appropriate cycle method based on shape type.
     * Return false if this item doesn't support this feature.
     */
    public default boolean cycleBlockOrientation(ItemStack stack, boolean reverse)
    {
        if(!isBlockOrientationSupported(stack)) return false;
        
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
        return true;
    }
    
    /**
     * Context-sensitive localized name of current orientation.
     */
    public default String blockOrientationLocalizedName(ItemStack stack)
    {
        if(!isBlockOrientationSupported(stack)) return "NOT SUPPORTED";
        
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
    
    public default boolean isRegionOrientationSupported(ItemStack stack)
    {
        return BENUMSET_FEATURES.isFlagSetForValue(PlacementItemFeature.REGION_ORIENTATION, this.featureFlags(stack));
    }
    
    public default void setRegionOrientation(ItemStack stack, RegionOrientation orientation)
    {
        if(!isRegionOrientationSupported(stack)) return; 
        
        orientation.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }

    /**
     * Always returns XYZ during selection operations because display wouldn't match what user is doing otherwise.
     */
    public default RegionOrientation getRegionOrientation(ItemStack stack)
    {
        return isFixedRegionSelectionInProgress(stack) ? RegionOrientation.XYZ : RegionOrientation.XYZ.deserializeNBT(stack.getTagCompound());
    }
    
    /**
     * Return false if this item doesn't support this feature.
     */
    public default boolean cycleRegionOrientation(ItemStack stack, boolean reverse)
    {
        if(!isRegionOrientationSupported(stack)) return false;
        
        setRegionOrientation(stack, reverse ? Useful.prevEnumValue(getRegionOrientation(stack)) : Useful.nextEnumValue(getRegionOrientation(stack)));
        return true;
    }
    
    public default boolean isTargetModeSupported(ItemStack stack)
    {
        return BENUMSET_FEATURES.isFlagSetForValue(PlacementItemFeature.TARGET_MODE, this.featureFlags(stack));
    }
    
    public default void setTargetMode(ItemStack stack, TargetMode mode)
    {
        if(!this.isTargetModeSupported(stack)) return;
        
        mode.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }
    
    public default TargetMode getTargetMode(ItemStack stack)
    {
        return TargetMode.FILL_REGION.deserializeNBT(stack.getTagCompound());
    }
    
    /**
     * Return false if this item doesn't support this feature.
     */
    public default boolean cycleTargetMode(ItemStack stack, boolean reverse)
    {
        if(!this.isTargetModeSupported(stack)) return false;
        
        setTargetMode(stack, reverse ? Useful.prevEnumValue(getTargetMode(stack)) : Useful.nextEnumValue(getTargetMode(stack)));
        return true;
    }
    
    public default boolean isFilterModeSupported(ItemStack stack)
    {
        return BENUMSET_FEATURES.isFlagSetForValue(PlacementItemFeature.FILTER_MODE, this.featureFlags(stack));
    }
    
    public default void setFilterMode(ItemStack stack, FilterMode mode)
    {
        if(!this.isFilterModeSupported(stack)) return;
        
        mode.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }
    
    public default FilterMode getFilterMode(ItemStack stack)
    {
        return FilterMode.FILL_REPLACEABLE.deserializeNBT(stack.getTagCompound());
    }
    
    /**
     * Return false if this item doesn't support this feature.
     */
    public default boolean cycleFilterMode(ItemStack stack, boolean reverse)
    {
        if(!this.isFilterModeSupported(stack)) return false;
        
        setFilterMode(stack, reverse ? Useful.prevEnumValue(getFilterMode(stack)) : Useful.nextEnumValue(getFilterMode(stack)));
        return true;
    }
    
    public default boolean isSpeciesModeSupported(ItemStack stack)
    {
        return BENUMSET_FEATURES.isFlagSetForValue(PlacementItemFeature.SPECIES_MODE, this.featureFlags(stack));
    }
    
    public default void setSpeciesMode(ItemStack stack, SpeciesMode mode)
    {
        if(!this.isSpeciesModeSupported(stack)) return;
        
        mode.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }
    
    public default SpeciesMode getSpeciesMode(ItemStack stack)
    {
        return SpeciesMode.MATCH_CLICKED.deserializeNBT(stack.getTagCompound());
    }
    
    /**
     * Return false if this item doesn't support this feature.
     */
    public default boolean cycleSpeciesMode(ItemStack stack, boolean reverse)
    {
        if(!this.isSpeciesModeSupported(stack)) return false;
        
        setSpeciesMode(stack, reverse ? Useful.prevEnumValue(getSpeciesMode(stack)) : Useful.nextEnumValue(getSpeciesMode(stack)));
        return true;
    }
    
    /**
     * Gets the appropriate super block to place from a given item stack if it is
     * a SuperItemBlock stack. Null otherwise.
     * May be different than the stack block because SuperModel in-world blocks are dependent on substance and other properties stored in the stack.
     */
    public default IBlockState getPlacementBlockStateFromStack(ItemStack stack)
    {
        // supermodel blocks may need to use a different block instance depending on model/substance
        // handle this here by substituting a stack different than what we received
        Item item = stack.getItem();
        
        if(item instanceof SuperItemBlock)
        {
            ModelState modelState = getStackModelState(stack);
            if(modelState == null) return null;

            SuperBlock targetBlock = ((SuperBlock)((SuperItemBlock)stack.getItem()).getBlock());
            
            if(!targetBlock.isVirtual() && targetBlock instanceof SuperModelBlock)
            {
                BlockSubstance substance = getStackSubstance(stack);
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

//    public default void toggleDeleteMode(ItemStack stack)
//    {
//        setDeleteModeEnabled(stack, !isDeleteModeEnabled(stack));
//    }
//    
//    public default boolean isDeleteModeEnabled(ItemStack stack)
//    {
//        return Useful.getOrCreateTagCompound(stack).getBoolean(ModNBTTag.PLACEMENT_DELETE_ENABLED);
//    }
//    
//    public default void setDeleteModeEnabled(ItemStack stack, boolean enabled)
//    {
//        Useful.getOrCreateTagCompound(stack).setBoolean(ModNBTTag.PLACEMENT_DELETE_ENABLED, enabled);
//    }
    
    public default boolean isFixedRegionSupported(ItemStack stack)
    {
        return BENUMSET_FEATURES.isFlagSetForValue(PlacementItemFeature.FIXED_REGION, this.featureFlags(stack));
    }
    
    /**
     * Return false if this item doesn't support this feature.
     * Turning off cancels any region selection in progress.
     */
    public default boolean toggleFixedRegionEnabled(ItemStack stack)
    {
        if(!this.isFixedRegionSupported(stack)) return false;
        
        boolean current = this.isFixedRegionEnabled(stack);
        
        if(current && this.isFixedRegionSelectionInProgress(stack))
            this.fixedRegionCancel(stack);
        
        setFixedRegionEnabled(stack, !current);
        return true;
    }
    
    /**
     * If true, any region target method should use the fixed
     * endpoints from {@link #fixedRegionFinish(ItemStack, EntityPlayer, BlockPos, boolean)}
     * @param stack
     * @return
     */
    public default boolean isFixedRegionEnabled(ItemStack stack)
    {
        if(!this.isFixedRegionSupported(stack)) return false;
        
        return Useful.getOrCreateTagCompound(stack).getBoolean(ModNBTTag.PLACEMENT_FIXED_REGION_ENABLED);
    }
    
    public default void setFixedRegionEnabled(ItemStack stack, boolean isFixedRegion)
    {
        if(!this.isFixedRegionSupported(stack)) return;
        
        Useful.getOrCreateTagCompound(stack).setBoolean(ModNBTTag.PLACEMENT_FIXED_REGION_ENABLED, isFixedRegion);
    }

    public default FixedRegionBounds getFixedRegion(ItemStack stack)
    {
       return new FixedRegionBounds(Useful.getOrCreateTagCompound(stack));
    }
    
    /**
     * Sets fixed region in the stack. Does not enable fixed region.
     */
    public default void setFixedRegion(FixedRegionBounds bounds, ItemStack stack)
    {
        if(!this.isFixedRegionSupported(stack)) return;
        
        bounds.saveToNBT(Useful.getOrCreateTagCompound(stack));
    }
    
    /**
     * Sets the begining point for a fixed region. 
     * Does not change the current fixed region.
     */
    public default void fixedRegionStart(ItemStack stack, BlockPos pos, boolean isCenter)
    {
        if(!this.isFixedRegionSupported(stack)) return;
        
        if(!this.isFixedRegionEnabled(stack)) 
            this.setFixedRegionEnabled(stack, true);
        
        NBTTagCompound tag = Useful.getOrCreateTagCompound(stack);
        
        tag.setLong(ModNBTTag.PLACEMENT_FIXED_REGION_SELECT_POS, PackedBlockPos.pack(pos, isCenter ? 1 : 0));
        
        TargetMode currentMode = getTargetMode(stack);
        // assume user wants to fill a region  and
        // change mode to region fill if not already set to FILL_REGION or HOLLOW_FILL
        if(!currentMode.usesSelectionRegion) TargetMode.FILL_REGION.serializeNBT(tag);
    }
    
    public default boolean isFixedRegionSelectionInProgress(ItemStack stack)
    {
        if(!this.isFixedRegionSupported(stack) || !this.isFixedRegionEnabled(stack)) return false;
        
        return Useful.getOrCreateTagCompound(stack).hasKey(ModNBTTag.PLACEMENT_FIXED_REGION_SELECT_POS);
    }
    
    public default void fixedRegionCancel(ItemStack stack)
    {
        if(!this.isFixedRegionSupported(stack)) return;
        
        NBTTagCompound tag = Useful.getOrCreateTagCompound(stack);
        tag.removeTag(ModNBTTag.PLACEMENT_FIXED_REGION_SELECT_POS);
        
        //disable fixed region if we don't have one
        if(!FixedRegionBounds.isPresentInTag(tag))
            tag.setBoolean(ModNBTTag.PLACEMENT_FIXED_REGION_ENABLED, false);
    }
    
    /**
     * If fixed region selection in progress, returns the starting point
     * that was set by {@link #fixedRegionStart(ItemStack, BlockPos, boolean)}
     * Boolean valus is true if point is centered.
     */
    @Nullable
    public default Pair<BlockPos, Boolean> fixedRegionSelectionPos(ItemStack stack)
    {
        if(!this.isFixedRegionSupported(stack)) return null;
        
        NBTTagCompound tag = Useful.getOrCreateTagCompound(stack);
        
        if(tag.hasKey(ModNBTTag.PLACEMENT_FIXED_REGION_SELECT_POS))
        {
            long packed = tag.getLong(ModNBTTag.PLACEMENT_FIXED_REGION_SELECT_POS);
            return Pair.of(PackedBlockPos.unpack(packed), PackedBlockPos.getExtra(packed) == 1);
        }
        else
        {
            return null;
        }
    }
    
    public default void fixedRegionFinish(ItemStack stack, EntityPlayer player, BlockPos pos, boolean isCenter)
    {
        if(!this.isFixedRegionSupported(stack)) return;
        
        Pair<BlockPos, Boolean> fromPos = fixedRegionSelectionPos(stack);

        // if somehow missing start position, still want to cancel selection operation
        NBTTagCompound tag = Useful.getOrCreateTagCompound(stack);
        
        if(fromPos == null) return;

        tag.removeTag(ModNBTTag.PLACEMENT_FIXED_REGION_SELECT_POS);
        
        setFixedRegion(new FixedRegionBounds(fromPos.first(), fromPos.second(), pos, isCenter), stack);
        
        setFixedRegionEnabled(stack,true);

        TargetMode currentMode = getTargetMode(stack);
        // assume user wants to fill a region  and
        // change mode to region fill if not already set to FILL_REGION or HOLLOW_FILL
        if(!currentMode.usesSelectionRegion) TargetMode.FILL_REGION.serializeNBT(tag);
        
    }
    
//    /**
//     * See {@link #placementRegionPosition(ItemStack)} for some explanation.
//     */
//    public static BlockPos getPlayerRelativeRegionFromEndPoints(BlockPos from, BlockPos to, EntityPlayer player)
//    {
//        BlockPos diff = to.subtract(from); 
//        BlockPos selectedPos = new BlockPos(
//                diff.getX() >= 0 ? diff.getX() + 1 : diff.getX() -1,
//                diff.getY() >= 0 ? diff.getY() + 1 : diff.getY() -1,
//                diff.getZ() >= 0 ? diff.getZ() + 1 : diff.getZ() -1);
//        return(player.getHorizontalFacing().getAxis() == EnumFacing.Axis.Z)
//                ? selectedPos
//                : new BlockPos(selectedPos.getZ(), selectedPos.getY(), selectedPos.getX());
//    }
    
    public default boolean isRegionSizeSupported(ItemStack stack)
    {
        return BENUMSET_FEATURES.isFlagSetForValue(PlacementItemFeature.REGION_SIZE, this.featureFlags(stack));
    }
    
    /**
     * For cubic selection regions.
     * X is left/right relative to player and Z is depth in direction player is facing.<br>
     * Y is always vertical height.<br>
     * All are always positive numbers.<br>
     * Region rotation is or isn't applied according to parameter.<br>
     */
    @Nonnull
    public default BlockPos getRegionSize(ItemStack stack, boolean applyRegionRotation)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if(tag == null || !tag.hasKey(ModNBTTag.PLACEMENT_REGION_SIZE)) return new BlockPos(1, 1, 1);
        
        BlockPos result = BlockPos.fromLong(tag.getLong(ModNBTTag.PLACEMENT_REGION_SIZE));
        
        return applyRegionRotation ? getRegionOrientation(stack).rotatedRegionPos(result) : result;
    }
    
    /**
     * See {@link #getRegionSize(ItemStack, boolean)}
     */
    @Nonnull
    public default void setRegionSize(ItemStack stack, BlockPos pos)
    {
        if(!this.isRegionSizeSupported(stack)) return;
        
        NBTTagCompound tag = stack.getTagCompound();
        tag.setLong(ModNBTTag.PLACEMENT_REGION_SIZE, pos.toLong());
    }
    
    /**
     * See {@link #getRegionSize(ItemStack, boolean)}
     * Returns false if feature not supported.
     */
    @Nonnull
    public default boolean changeRegionSize(ItemStack stack, int dx, int dy, int dz)
    {
        if(!this.isRegionSizeSupported(stack)) return false;
        
        NBTTagCompound tag = Useful.getOrCreateTagCompound(stack);
        BlockPos oldPos = BlockPos.fromLong(tag.getLong(ModNBTTag.PLACEMENT_REGION_SIZE));
       
        BlockPos newPos = new BlockPos(
                MathHelper.clamp(oldPos.getX() + dx, 1, 9),
                MathHelper.clamp(oldPos.getY() + dy, 1, 9),
                MathHelper.clamp(oldPos.getZ() + dz, 1, this.isExcavator(stack) ? 64 : 9)
                );
        tag.setLong(ModNBTTag.PLACEMENT_REGION_SIZE, newPos.toLong());
        return true;
    }
    
//    /**
//     * Meant for use by packet handler
//     */
//    public static void selectedRegionUpdate(ItemStack stack, BlockPos selectedPos)
//    {
//        NBTTagCompound tag = Useful.getOrCreateTagCompound(stack);
//        if(selectedPos == null)
//        {
//            tag.removeTag(ModNBTTag.PLACEMENT_REGION_SIZE);
//        }
//        else
//        {
//            tag.setLong(ModNBTTag.PLACEMENT_REGION_SIZE, selectedPos.toLong());
//        }
//    }
    
    public default String selectedRegionLocalizedName(ItemStack stack)
    {
        switch(getTargetMode(stack))
        {
        case FILL_REGION:
            if(!this.isRegionSizeSupported(stack)) return "";
            BlockPos pos = getRegionSize(stack, false);
            return I18n.translateToLocalFormatted("placement.message.region_box", pos.getX(), pos.getY(), pos.getZ());
            
        case ON_CLICKED_SURFACE:
            return I18n.translateToLocal("placement.message.region_additive");

        case ON_CLICKED_FACE:
        default:
            return I18n.translateToLocal("placement.message.region_single");
        
        }
    }
    
    public default boolean isSelectionRangeSupported(ItemStack stack)
    {
        return BENUMSET_FEATURES.isFlagSetForValue(PlacementItemFeature.SELECTION_RANGE, this.featureFlags(stack));
    }
    
    /**
     * 0 means non-floating
     */
    public default void setSelectionTargetRange(ItemStack stack, int range)
    {
        if(!this.isSelectionRangeSupported(stack)) return;
        
        // subtract because external values jump from 0 to 2
        if(range > 0) range--;
        Useful.getOrCreateTagCompound(stack).setByte(ModNBTTag.PLACEMENT_REGION_FLOATING_RANGE, (byte)range);
    }
    
    /**
     * 0 means non-floating
     */
    public default int getFloatingSelectionRange(ItemStack stack)
    {
        if(!this.isSelectionRangeSupported(stack)) return 0;

        NBTTagCompound tag = stack.getTagCompound();
        int range = tag == null ? 0 : MathHelper.clamp(tag.getByte(ModNBTTag.PLACEMENT_REGION_FLOATING_RANGE), 0, 4);
        return range == 0 ? 0 : range + 1;
    }
    
    /**
     * Return false if this item doesn't support this feature.
     */
    public default boolean cycleSelectionTargetRange(ItemStack stack, boolean reverse)
    {
        if(!this.isSelectionRangeSupported(stack)) return false;

        NBTTagCompound tag = Useful.getOrCreateTagCompound(stack);
        int range = tag.getByte(ModNBTTag.PLACEMENT_REGION_FLOATING_RANGE) + (reverse ? -1 : 1);
        if(range > 4) range = 0;
        if(range < 0) range = 4;
        tag.setByte(ModNBTTag.PLACEMENT_REGION_FLOATING_RANGE, (byte)range);
        return true;
    }
    
    public default boolean isFloatingSelectionEnabled(ItemStack stack)
    {
        if(!this.isSelectionRangeSupported(stack)) return false;

        return getFloatingSelectionRange(stack) != 0;
    }
    
    /**
     * Will return a meaningless result if floating selection is disabled.
     * 
     * TODO: remove - replaced by PlacementPosition
     */
    public default BlockPos getFloatingSelectionBlockPos(ItemStack stack, EntityLivingBase entity)
    {
        int range = getFloatingSelectionRange(stack);
        
        Vec3d look = entity.getLookVec();
        int blockX = MathHelper.floor(look.x * range + entity.posX);
        int blockY = MathHelper.floor(look.y * range + entity.posY + entity.getEyeHeight());
        int blockZ = MathHelper.floor(look.z * range + entity.posZ);

        return new BlockPos(blockX, blockY, blockZ);
    }
    
    /**
     * Default implementation of item method.
     * Still need to override in subclass because will defer to Item/ItemBlock implementation.
     */
    public default EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if(playerIn == null) return EnumActionResult.FAIL;
        
        ItemStack stackIn = playerIn.getHeldItem(hand);
        if (stackIn.isEmpty() || stackIn.getItem() != this) return EnumActionResult.FAIL;
        
        PlacementResult result = PlacementHandler.doRightClickBlock(playerIn, pos, facing, new Vec3d(hitX, hitY, hitZ), stackIn, this);
        
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
     * Default implementation of item method.
     * Still need to override in subclass because will defer to Item/ItemBlock implementation.
     */    
    public default ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        if(player == null) return new ActionResult<>(EnumActionResult.PASS, null);
        
        ItemStack stackIn = player.getHeldItem(hand);
        
        if (stackIn.isEmpty() || stackIn.getItem() != this) return new ActionResult<>(EnumActionResult.PASS, stackIn);
        
        PlacementResult result = PlacementHandler.doRightClickBlock(player, null, null, null, stackIn, this);
        
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
                    && this.isVirtual(stackIn))
            {
                this.displayGui(player);
                return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
            }
        }
        
        return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
    }
}
