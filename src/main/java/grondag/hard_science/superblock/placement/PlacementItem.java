package grondag.hard_science.superblock.placement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.realmsclient.util.Pair;

import grondag.hard_science.HardScience;
import grondag.hard_science.init.ModSuperModelBlocks;
import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.serialization.ModNBTTag;
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
    public static boolean getBlockPlacementAxisIsInverted(ItemStack stack)
    {
        switch(getStackModelState(stack).orientationType())
        {
        case AXIS:
            return false;
            
        case FACE:
            return getBlockOrientationFace(stack).face.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE;

        case EDGE:
            // FIXME: is this right?
            return PlacementItem.getBlockOrientationEdge(stack).edge.face1.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE;
        
        case CORNER:
            //TODO

        case NONE:
        default:
            return false;
        }    
    }
    
    public static Rotation getBlockPlacementRotation(ItemStack stack)
    {
        switch(getStackModelState(stack).orientationType())
        {
            case EDGE:
                return PlacementItem.getBlockOrientationEdge(stack).edge.modelRotation;
                
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
        return isFixedRegionSelectionInProgress(stack) ? RegionOrientation.XYZ : RegionOrientation.XYZ.deserializeNBT(stack.getTagCompound());
    }
    
    public static void cycleRegionOrientation(ItemStack stack, boolean reverse)
    {
        setRegionOrientation(stack, reverse ? Useful.prevEnumValue(getRegionOrientation(stack)) : Useful.nextEnumValue(getRegionOrientation(stack)));
    }
    
    public static void setTargetMode(ItemStack stack, TargetMode mode)
    {
        mode.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }
    
    public static TargetMode getTargetMode(ItemStack stack)
    {
        return TargetMode.FILL_REGION.deserializeNBT(stack.getTagCompound());
    }
    
    public static void cycleTargetMode(ItemStack stack, boolean reverse)
    {
        setTargetMode(stack, reverse ? Useful.prevEnumValue(getTargetMode(stack)) : Useful.nextEnumValue(getTargetMode(stack)));
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
    
    /**
     * Data carrier for fixed region definition to reduce number of
     * methods and method calls for fixed regions.  Fixed regions 
     * can be arbitrarily large and don't have to be cubic - shape
     * depends on interpretation by the placement builder.
     */
    public static class FixedRegionBounds
    {
        public final BlockPos fromPos;
        public final boolean fromIsCentered;
        public final BlockPos toPos;
        public final boolean toIsCentered;
        
        public FixedRegionBounds(BlockPos fromPos, boolean fromIsCentered, BlockPos toPos, boolean toIsCentered)
        {
            this.fromPos = fromPos;
            this.fromIsCentered = fromIsCentered;
            this.toPos = toPos;
            this.toIsCentered = toIsCentered;
        }

        private FixedRegionBounds(NBTTagCompound tag)
        {
            final long from = tag.getLong(ModNBTTag.PLACEMENT_FIXED_REGION_START_POS);
            this.fromPos = PackedBlockPos.unpack(from);
            this.fromIsCentered = PackedBlockPos.getExtra(from) == 1;
            final long to = tag.getLong(ModNBTTag.PLACEMENT_FIXED_REGION_END_POS);
            this.toPos = PackedBlockPos.unpack(to);
            this.toIsCentered = PackedBlockPos.getExtra(to) == 1;
        }

        private void saveToNBT(NBTTagCompound tag)
        {
            tag.setLong(ModNBTTag.PLACEMENT_FIXED_REGION_START_POS, PackedBlockPos.pack(this.fromPos, this.fromIsCentered ? 1 : 0));
            tag.setLong(ModNBTTag.PLACEMENT_FIXED_REGION_END_POS, PackedBlockPos.pack(this.toPos, this.toIsCentered ? 1 : 0));
        }
        
        private static boolean isPresentInTag(NBTTagCompound tag)
        {
            return tag.hasKey(ModNBTTag.PLACEMENT_FIXED_REGION_START_POS) 
                    && tag.hasKey(ModNBTTag.PLACEMENT_FIXED_REGION_END_POS);
        }
    }
    
    public static void toggleFixedRegionEnabled(ItemStack stack)
    {
        setFixedRegionEnabled(stack, !isFixedRegionEnabled(stack));
    }
    
    /**
     * If true, any region target method should use the fixed
     * endpoints from {@link #fixedRegionFinish(ItemStack, EntityPlayer, BlockPos, boolean)}
     * @param stack
     * @return
     */
    public static boolean isFixedRegionEnabled(ItemStack stack)
    {
        return Useful.getOrCreateTagCompound(stack).getBoolean(ModNBTTag.PLACEMENT_FIXED_REGION_ENABLED);
    }
    
    public static void setFixedRegionEnabled(ItemStack stack, boolean isFixedRegion)
    {
        Useful.getOrCreateTagCompound(stack).setBoolean(ModNBTTag.PLACEMENT_FIXED_REGION_ENABLED, isFixedRegion);
    }

    public static FixedRegionBounds getFixedRegion(ItemStack stack)
    {
       return new FixedRegionBounds(Useful.getOrCreateTagCompound(stack));
    }
    
    /**
     * Sets fixed region in the stack. Does not enable fixed region.
     */
    public static void setFixedRegion(FixedRegionBounds bounds, ItemStack stack)
    {
        bounds.saveToNBT(Useful.getOrCreateTagCompound(stack));
    }
    
    /**
     * Sets the begining point for a fixed region. 
     * Does not change the current fixed region.
     */
    public static void fixedRegionStart(ItemStack stack, BlockPos pos, boolean isCenter)
    {
        NBTTagCompound tag = Useful.getOrCreateTagCompound(stack);
        
        tag.setLong(ModNBTTag.PLACEMENT_FIXED_REGION_SELECT_POS, PackedBlockPos.pack(pos, isCenter ? 1 : 0));
        
        TargetMode currentMode = getTargetMode(stack);
        // assume user wants to fill a region  and
        // change mode to region fill if not already set to FILL_REGION or HOLLOW_FILL
        if(!currentMode.usesSelectionRegion) TargetMode.FILL_REGION.serializeNBT(tag);
    }
    
    public static boolean isFixedRegionSelectionInProgress(ItemStack stack)
    {
        return Useful.getOrCreateTagCompound(stack).hasKey(ModNBTTag.PLACEMENT_FIXED_REGION_SELECT_POS);
    }
    
    public static void fixedRegionCancel(ItemStack stack)
    {
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
    public static Pair<BlockPos, Boolean> fixedRegionSelectionPos(ItemStack stack)
    {
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
    
    public static void fixedRegionFinish(ItemStack stack, EntityPlayer player, BlockPos pos, boolean isCenter)
    {
        
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
    
    /**
     * For cubic selection regions.
     * X is left/right relative to player and Z is depth in direction player is facing.<br>
     * Y is always vertical height.<br>
     * All are always positive numbers.<br>
     * Region rotation is or isn't applied according to parameter.<br>
     */
    @Nonnull
    public static BlockPos getRegionSize(ItemStack stack, boolean applyRegionRotation)
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
    public static void setRegionSize(ItemStack stack, BlockPos pos)
    {
        NBTTagCompound tag = stack.getTagCompound();
        tag.setLong(ModNBTTag.PLACEMENT_REGION_SIZE, pos.toLong());
    }
    
    /**
     * See {@link #getRegionSize(ItemStack, boolean)}
     */
    @Nonnull
    public static void changeRegionSize(ItemStack stack, int dx, int dy, int dz)
    {
        NBTTagCompound tag = Useful.getOrCreateTagCompound(stack);
        BlockPos oldPos = BlockPos.fromLong(tag.getLong(ModNBTTag.PLACEMENT_REGION_SIZE));
       
        BlockPos newPos = new BlockPos(
                MathHelper.clamp(oldPos.getX() + dx, 1, 9),
                MathHelper.clamp(oldPos.getY() + dy, 1, 9),
                MathHelper.clamp(oldPos.getZ() + dz, 1, 9)
                );
        tag.setLong(ModNBTTag.PLACEMENT_REGION_SIZE, newPos.toLong());
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
    
    public static String selectedRegionLocalizedName(ItemStack stack)
    {
        switch(getTargetMode(stack))
        {
        case FILL_REGION:
            BlockPos pos = getRegionSize(stack, false);
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
     * 
     * TODO: remove - replaced by PlacementPosition
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
