package grondag.hard_science.superblock.placement;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import grondag.hard_science.Log;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.library.world.HorizontalFace;
import grondag.hard_science.player.ModPlayerCaps;
import grondag.hard_science.player.ModPlayerCaps.ModifierKey;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * TODO: Placement WIP
 * Species handling modes
 * Deletion Mode - creative
 * Deletion Rendering
 * Fixed block orientation
 * Dynamic block orientation
 * Match closest block orientation
 * Follow mode
 * Block History
 * Region History
 * Deletion Mode - survival
 * Temporary deletion drone service
 * Placement Undo
 * Deletion Undo
 */


public interface IPlacementHandler
{
    /**
     * Called client-side by overlay renderer to know
     * what should be rendered for player. If no operation is in progress,
     * assumes player will click the right mouse button.
     * If an operation is in progress, assumes user will click the button
     * that completes the operation.
     */
    @SideOnly(Side.CLIENT)
    @Nonnull
    public static PlacementResult predictPlacementResults(EntityPlayerSP player, ItemStack stack, PlacementItem item)
    {
        // prevent nonsense
        if(stack == null || stack.getItem() != item) return PlacementResult.EMPTY_RESULT_CONTINUE;

        /* if player is in range to a solid block and floating selection is off, 
         * the block against which we would place our block(s).  Null if out of range or floating
         * selection is on.
         */
        BlockPos onPos = null;

        /**
         * Face that will be clicked. Null if onPos is null
         */
        EnumFacing onFace = null;

        /**
         * Hit vector for block that will be clicked. Null if onPos is null
         */
        Vec3d hitVec = null;

        if(!PlacementItem.isFloatingSelectionEnabled(stack))
        {
            // if floating selection enabled, there is no "placed on" position
            // No floating selection - so look for block placed on.

            Minecraft mc = Minecraft.getMinecraft();

            RayTraceResult target = mc.objectMouseOver;

            if(target.typeOfHit == RayTraceResult.Type.BLOCK)
            {
                onPos = target.getBlockPos();

                // if block out of range there will be no "placed on" position
                //FIXME: what about water?
                if(onPos.distanceSq(onPos) > Useful.squared(mc.playerController.getBlockReachDistance() + 1)
                        || player.world.isAirBlock(onPos))
                {
                    onPos = null;
                }
                else
                {
                    onFace = target.sideHit;
                    hitVec = target.hitVec;
                }
            }
        }

        if(PlacementItem.operationInProgress(stack) == PlacementOperation.DELETING)
        {
            // assume user will click the left mouse button
            // but can only happen if in range
            return onPos == null ? PlacementResult.EMPTY_RESULT_CONTINUE : doLeftClickBlock(player, onPos, onFace, hitVec, stack);
        }
        else
        {
            // assume user will click the right mouse button
            return doRightClickBlock(player, onPos, onFace, hitVec, stack);
        }
    }

    /**
     * Call when item is left clicked on a block.
     * Returns true if the click was handled and input processing should stop.
     */
    public default boolean handleLeftClickBlock(EntityPlayer player, BlockPos onPos, EnumFacing face, Vec3d hitVec)
    {
        return true;
    }


    /**
     * Determines outcome when player left clicks on a block.
     * DOES NOT UPDATE STATE.
     * @param stack 
     */
    public static PlacementResult doLeftClickBlock(EntityPlayer player, BlockPos onPos, EnumFacing onFace, Vec3d hitVec, ItemStack stack)
    {
        switch(PlacementItem.operationInProgress(stack))
        {
            case DELETING:
            {
                // need operation start position for this to work
                BlockPos startPos = PlacementItem.operationPosition(stack);
                if(startPos == null) return new PlacementResult(
                        null, 
                        null, 
                        null, 
                        null, 
                        null,
                        PlacementEvent.CANCEL_EXCAVATION_REGION);
    
                if(ModPlayerCaps.isModifierKeyPressed(player, ModifierKey.CTRL_KEY))
                {
                    // Ctrl left-click while deleting - set region without excavation
                    return new PlacementResult(
                            null, 
                            null, 
                            blockBoundaryAABB(startPos, onPos), 
                            null, 
                            onPos,
                            PlacementEvent.SET_EXCAVATION_REGION);
                }
                else
                {
                    // left-click while deleting - set region and excavate it 
                    return new PlacementResult(
                            null, 
                            null, 
                            blockBoundaryAABB(startPos, onPos), 
                            positionsInRegion(startPos, onPos), 
                            onPos,
                            PlacementEvent.EXCAVATE_AND_SET_REGION);
                }
            }
            
            case SELECTING:
            {
                // any left click while selecting cancels the operation;
                return new PlacementResult(
                        null, 
                        null, 
                        null, 
                        null, 
                        null,
                        PlacementEvent.CANCEL_PLACEMENT_REGION);
            }
            
            case NONE:
            default:
            {
                if(ModPlayerCaps.isModifierKeyPressed(player, ModifierKey.ALT_KEY))
                {
                    if(ModPlayerCaps.isModifierKeyPressed(player, ModifierKey.CTRL_KEY))
                    {
                        // Alt + Ctrl left click: undo block excavation
                        //TODO
                        Log.info("UNDO EXCAVATION LOGIC HAPPENS NOW");
                        return new PlacementResult(
                                null, 
                                null, 
                                null, 
                                null, 
                                onPos,
                                PlacementEvent.UNDO_EXCAVATION);
                    }
                    else
                    {
                        // Alt+left click: undo block placement
    
                        //TODO
                        Log.info("UNDO PLACEMENT LOGIC HAPPENS NOW");
                        return new PlacementResult(
                                null, 
                                null, 
                                null, 
                                null, 
                                onPos,
                                PlacementEvent.UNDO_PLACEMENT);
                    }
                }
                else if(ModPlayerCaps.isModifierKeyPressed(player, ModifierKey.CTRL_KEY))
                {
                    // Ctrl + left click: start new excavation region
                    return new PlacementResult(
                            null, 
                            null, 
                            blockBoundaryAABB(onPos, onPos),
                            null, 
                            onPos,
                            PlacementEvent.START_EXCAVATION_REGION);
                }
                else
                {
                    // normal left click on block - check for a multi-block excavation region
                    BlockPos deletionPos = PlacementItem.deletionRegionPosition(stack, true);
    
                    if(deletionPos == null)
                    {
                        // no excavation region, let normal MC behavior occur
                        return PlacementResult.EMPTY_RESULT_CONTINUE;
                    }
                    else
                    {
                        // excavate
                        BlockPos startPos = onPos;
                        BlockPos endPos = getPlayerRelativeOffset(startPos, deletionPos, player, onFace, OffsetPosition.FLIP_NONE);
                        return new PlacementResult(
                                null, 
                                null, 
                                blockBoundaryAABB(startPos, endPos), 
                                positionsInRegion(startPos, endPos), 
                                onPos,
                                PlacementEvent.EXCAVATE);
                    }
                }
            }
        }
    }

    public static Set<BlockPos> positionsInRegion(BlockPos fromPos, BlockPos toPos)
    {
        int swap;
        int xFrom = fromPos.getX();
        int xTo = toPos.getX();
        if(xFrom > xTo)
        {
            swap = xFrom;
            xFrom = xTo;
            xTo = swap;
        }

        int yFrom = fromPos.getY();
        int yTo = toPos.getY();
        if(yFrom > yTo)
        {
            swap = yFrom;
            yFrom = yTo;
            yTo = swap;
        }

        int zFrom = fromPos.getZ();
        int zTo = toPos.getZ();
        if(zFrom > zTo)
        {
            swap = zFrom;
            zFrom = zTo;
            zTo = swap;
        }        

        ImmutableSet.Builder<BlockPos> builder = ImmutableSet.builder();
        for(int x = xFrom; x <= xTo; x++)
        {
            for(int y = yFrom; y <= yTo; y++)
            {
                for(int z = zFrom; z <= zTo; z++)
                {
                    builder.add(new BlockPos(x, y, z));
                }
            }
        }
        return builder.build();
    }

    /**
     * Find the position offset for the placement/deletion position values in PlacementItem
     * relative to the player's current orientation and starting location.
     * @param onFace if non-null, assumes startPos is against this face, and should extend in the opposite direction.
     * OffsetPosition alters box placements and is used to find alternate regions that might avoid obstacles.
     */
    public static BlockPos getPlayerRelativeOffset(BlockPos startPos, BlockPos offsetPos, EntityPlayer player, EnumFacing onFace, OffsetPosition offset)
    {
        Vec3d lookVec = player.getLookVec();
        int xFactor = lookVec.x > 0 ? 1 : -1;
        int zFactor = lookVec.z > 0 ? 1 : -1;
        
        if(onFace != null)
        {
            switch(onFace.getAxis())
            {
            case X:
                xFactor = onFace.getAxisDirection().getOffset();
                break;
            case Z:
                zFactor = onFace.getAxisDirection().getOffset();
                break;
            case Y:
            }
        }

        if(player.getHorizontalFacing().getAxis() == EnumFacing.Axis.X)
        {
            return startPos.add((offsetPos.getX() - 1) * xFactor * offset.depthFactor, (offsetPos.getY() - 1) * offset.heightFactor, (offsetPos.getZ() - 1) * zFactor * offset.widthFactor);
        }
        else
        {
            return startPos.add((offsetPos.getZ() - 1) * xFactor * offset.widthFactor, (offsetPos.getY() - 1) * offset.heightFactor, (offsetPos.getX() - 1) * zFactor * offset.depthFactor);
        }
    }

    /**
     * Returns an AABB aligned along block boundaries that includes both positions given.
     */
    public static AxisAlignedBB blockBoundaryAABB(BlockPos startPos, BlockPos endPos)
    {
        AxisAlignedBB aabbStart = Block.FULL_BLOCK_AABB.offset(startPos.getX(), startPos.getY(), startPos.getZ());
        AxisAlignedBB aabbEnd = Block.FULL_BLOCK_AABB.offset(endPos.getX(), endPos.getY(), endPos.getZ());
        return aabbStart.union(aabbEnd);
    }

    /**
     * Determines outcome when player right clicks on the face of a block.
     *  if no hit block is known or if floating selection is known to be enabled, pass onPos, onFace, and hitVec = null instead.
     * DOES NOT UPDATE STATE.
     */
    public static PlacementResult doRightClickBlock(EntityPlayer player, @Nullable BlockPos onPos, @Nullable EnumFacing onFace, @Nullable Vec3d hitVec, ItemStack stack)
    {
        /**
         * position user is activating, either by clicking against a block or with floating selection.
         */
        BlockPos startPos = PlacementItem.isFloatingSelectionEnabled(stack)
                ? PlacementItem.getFloatingSelectionBlockPos(stack, player) 
                : onPos == null || onFace == null ? null : onPos.offset(onFace);
        
        if(!(stack.getItem() instanceof PlacementItem) || !((PlacementItem)stack.getItem()).getSuperBlock().canUseControls(player))
        {
            // player not allowed to use non-vanilla features of this block
            // so emulate vanilla right-click behavior
            
            // nothing to do if no position
            if(startPos == null) return PlacementResult.EMPTY_RESULT_CONTINUE;
            
            return new PlacementResult(
                    blockBoundaryAABB(startPos, startPos),
                    ImmutableList.of(Pair.of(startPos, stack)), 
                    null, 
                    null, 
                    startPos,
                    PlacementEvent.PLACE);
        }
                
        switch(PlacementItem.operationInProgress(stack))
        {
            case DELETING:
            {
                
                // any right click while selecting cancels the operation;
                return new PlacementResult(
                        null, 
                        null, 
                        null, 
                        null, 
                        null,
                        PlacementEvent.CANCEL_EXCAVATION_REGION);
            }
            
            case SELECTING:
            {
                BlockPos endPos = PlacementItem.operationPosition(stack);
                
                // need both positions to do anything
                if(startPos == null || endPos == null) return PlacementResult.EMPTY_RESULT_CONTINUE;
                
                if(ModPlayerCaps.isModifierKeyPressed(player, ModifierKey.CTRL_KEY))
                {
                    // Ctrl right-click while selecting - set region without placing
                    return new PlacementResult(
                            blockBoundaryAABB(startPos, endPos), 
                            null, 
                            null, 
                            null, 
                            startPos,
                            PlacementEvent.SET_PLACEMENT_REGION);
                }
                else
                {
                    // right-click while selecting - set region and place blocks 
                    // Note that we don't pass end pos - will be re-evaluated by placement logic
                    // to allow for obstacle handling.
                    return doPlacement(stack, player, onPos, onFace, hitVec, startPos, true);
                }
            }
            
            case NONE:
            default:
            {
                // nothing to do if no position
                if(startPos == null) return PlacementResult.EMPTY_RESULT_CONTINUE;
                
                if(ModPlayerCaps.isModifierKeyPressed(player, ModifierKey.CTRL_KEY))
                {
                    // Ctrl + right click: start new placement region
                    return new PlacementResult(
                            blockBoundaryAABB(startPos, startPos),
                            null, 
                            null, 
                            null, 
                            startPos,
                            PlacementEvent.START_PLACEMENT_REGION);
                }
                else
                {
                    // normal right click on block 
                    return doPlacement(stack, player, onPos, onFace, hitVec, startPos, false);
                }
            }
        }
    }
    
    public static PlacementResult doPlacement(ItemStack stack, EntityPlayer player, BlockPos onPos, EnumFacing onFace, Vec3d hitVec, BlockPos startPos, boolean setRegion)
    {
        boolean isSelecting = PlacementItem.operationInProgress(stack) == PlacementOperation.SELECTING;
        
        // check for a multi-block placement region
        BlockPos placementPos = PlacementItem.getMode(stack) == PlacementMode.FILL_REGION ? PlacementItem.placementRegionPosition(stack, true) : null;
        BlockPos endPos;
        if(isSelecting)
        {
            endPos = PlacementItem.operationPosition(stack);
        }
        else
        {
            endPos = placementPos == null ? startPos : getPlayerRelativeOffset(startPos, placementPos, player, onFace, OffsetPosition.FLIP_NONE);
        }
        
        Set<BlockPos> region = positionsInRegion(startPos, endPos);
        Set<BlockPos> exclusions = obstaclesInRegion(player, onPos, onFace, hitVec, stack, region);
        
        ObstacleHandling handling = PlacementItem.getObstacleHandling(stack);
        
        /** true if no obstacles */
        boolean isClear = exclusions.isEmpty() 
                && player.world.checkNoEntityCollision(blockBoundaryAABB(startPos, endPos));
        
        // adjust selection to avoid exclusions if requested and necessary
        // no point if is only a single position
        // also can't do this if user is currently selecting a region
        if(!isClear && !isSelecting && handling.adjust && placementPos != null && !startPos.equals(endPos))
        {
            for(OffsetPosition offset : OffsetPosition.ALTERNATES)
            {
                // first try pivoting the selection box around the position being targeted
                BlockPos endPos2 = getPlayerRelativeOffset(startPos, placementPos, player, onFace, offset);
                Set<BlockPos> region2 = positionsInRegion(startPos, endPos2);
                Set<BlockPos> exclusions2 = obstaclesInRegion(player, onPos, onFace, hitVec, stack, region2);
    
                if(exclusions2.isEmpty() && player.world.checkNoEntityCollision(blockBoundaryAABB(startPos, endPos2)))
                {
                    endPos = endPos2;
                    region = region2;
                    exclusions = exclusions2;
                    isClear = true;
                    break;
                }
            }
            
            if(!isClear)
            {
                // that didn't work, so try nudging the region a block in each direction
                EnumFacing[] checkOrder = faceCheckOrder(player, onFace);
                
                for(EnumFacing face : checkOrder)
                {
                    BlockPos startPos2 = startPos.offset(face);
                    BlockPos endPos2 = endPos.offset(face);
                    Set<BlockPos>region2 = positionsInRegion(startPos2, endPos2);
                    Set<BlockPos> exclusions2 = obstaclesInRegion(player, onPos, onFace, hitVec, stack, region2);
                    if(exclusions2.isEmpty() && player.world.checkNoEntityCollision(blockBoundaryAABB(startPos2, endPos2)))
                    {
                        endPos = endPos2;
                        region = region2;
                        exclusions = exclusions2;
                        isClear = true;
                        break;
                    }
                }
            }
        }
        
        List<Pair<BlockPos, ItemStack>> placements = (handling.skip || isClear)
                ? placeRegion(player, onPos, onFace, hitVec, stack, region, exclusions)
                : Collections.emptyList();
        
        return new PlacementResult(
                blockBoundaryAABB(startPos, endPos), 
                placements, 
                null, 
                exclusions, 
                startPos,
                setRegion ? PlacementEvent.PLACE_AND_SET_REGION : PlacementEvent.PLACE);
    }

    /**
     * Order of preference for selection adjustment based on player facing.
     */
    public static EnumFacing[] faceCheckOrder(EntityPlayer player, EnumFacing onFace)
    {
        if(onFace == null)
        {
            HorizontalFace playerFacing = HorizontalFace.find(player.getHorizontalFacing());
            EnumFacing[] result = new EnumFacing[6];
            result[0] = playerFacing.getLeft().face;
            result[1] = playerFacing.getRight().face;
            result[2] = playerFacing.face.getOpposite();
            result[3] = playerFacing.face;
            result[4] = EnumFacing.UP;
            result[5] = EnumFacing.DOWN;
            return result;
        }
       
        
        switch(onFace)
        {
            case DOWN:
                final EnumFacing[] DOWN_RESULT = {EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.UP, };
                return DOWN_RESULT;
                
            case UP:
                final EnumFacing[] UP_RESULT = {EnumFacing.UP, EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.DOWN};
                return UP_RESULT;

            case EAST:
                final EnumFacing[]  EAST_RESULT = {EnumFacing.EAST, EnumFacing.NORTH,  EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.UP, EnumFacing.DOWN};
                return EAST_RESULT;

            case NORTH:
                final EnumFacing[] NORTH_RESULT = {EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.UP, EnumFacing.DOWN};
                return NORTH_RESULT;

            case SOUTH:
                final EnumFacing[] SOUTH_RESULT = {EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.UP, EnumFacing.DOWN};
                return SOUTH_RESULT;

            case WEST:
            default:
                final EnumFacing[] WEST_RESULT = {EnumFacing.WEST, EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.UP, EnumFacing.DOWN};
                return WEST_RESULT;
       
        }
    }
    public static Set<BlockPos> obstaclesInRegion(EntityPlayer player, BlockPos onPos, EnumFacing onFace, Vec3d hitVec, ItemStack stack, Collection<BlockPos> region)
    {
        World world = player.world;
        
       ImmutableSet.Builder<BlockPos> builder = ImmutableSet.builder();
        
        for(BlockPos pos : region)
        {
            if(!world.getBlockState(pos).getMaterial().isReplaceable())
            {
                builder.add(pos);
            }
        }
        return builder.build();
    }
    
    /**
     * Generates positions and item stacks that should be placed in the given region 
     * according to current stack settings.
     * The 
     */
    public static List<Pair<BlockPos, ItemStack>> placeRegion(EntityPlayer player, BlockPos onPos, EnumFacing onFace, Vec3d hitVec, ItemStack stack, 
            Collection<BlockPos> region, Set<BlockPos> exclusions)
    {
        
        ImmutableList.Builder<Pair<BlockPos, ItemStack>> builder = ImmutableList.builder();
        
        for(BlockPos pos : region)
        {
            if(!exclusions.contains(pos))
            {
                builder.add(Pair.of(pos, stack));
            }
        }
        return builder.build();
       
    }
    
    /**
     * Returns list of stacks to be placed.
     * Responsible for confirming that all positions placed are air or replaceable.
     * Has to be checked here because some placement methods may legitimately replace
     * existing non-air blocks. (Stackable plates, for example.) 
     * Checking that normally happens before this in ItemBlock is skipped for SuperBlocks.<br><br>
     * 
     * Caller expected to confirm that player has edit rights 
     * and to skip any positions occupied by entities. <br><br>
     * 
     * Stacks that are returned should be copies of the input stack.
     * (Do not modify the input stack!) <br><br>
     * 
     * Output stacks should have correct metadata and other properties for the blocks to be placed
     * This also include modelState and any other TE properties that must be transferred to the world. <br><br>
     * 
     * List should be empty if nothing can be placed.
     */
    default List<Pair<BlockPos, ItemStack>> getPlacementResults(EntityPlayer playerIn, World worldIn, BlockPos posOn, EnumHand hand, EnumFacing facing, float hitX,
            float hitY, float hitZ, ItemStack stack)
    {
        if(!PlacementItem.isPlacementItem(stack)) return Collections.emptyList();

        PlacementItem item = (PlacementItem)stack.getItem();
        PlacementMode placementMode = PlacementItem.getMode(stack);

        boolean isFloating = PlacementItem.isFloatingSelectionEnabled(stack);

        SuperBlock stackBlock = item.getSuperBlock();

        ModelState stackModelState = PlacementItem.getStackModelState(stack);

        switch(placementMode)
        {
        case FILL_REGION:
            return isFloating 
                    ? floatingMultiBlockResults()
                            : multiBlockResults();

        case ADD_TO_EXISTING:
            additiveResults();

        case SINGLE_BLOCK:
        default:
            return isFloating 
                    ? floatingSingleBlockResult()
                            : singleBlockResult(worldIn, posOn, hand, facing, hitZ, hitZ, hitZ, stack);
        }
    }

    /**
     * Conventional block placement - placed on a given face, so that face can be used for context
     */
    default List<Pair<BlockPos, ItemStack>> singleBlockResult(World worldIn, BlockPos posOn, EnumHand hand, EnumFacing facing, float hitX,
            float hitY, float hitZ, ItemStack stack)
    {
        return Collections.emptyList();
    }


    /**
     * Single block placed at an arbitrary location, so there is no "placed on" face that can be used for context
     */
    default List<Pair<BlockPos, ItemStack>> floatingSingleBlockResult()
    {
        return Collections.emptyList();
    }

    /**
     * Multiple blocks but still placed on a given face, so that face can still be used for context
     */
    default List<Pair<BlockPos, ItemStack>> multiBlockResults()
    {
        return Collections.emptyList();
    }

    /**
     * Multiple blocks placed at an arbitrary location, so there is no "placed on" face that can be used for context
     */
    default List<Pair<BlockPos, ItemStack>> floatingMultiBlockResults()
    {
        return Collections.emptyList();
    }

    /**
     * "Builders wand" style of placement.
     */
    default List<Pair<BlockPos, ItemStack>> additiveResults()
    {
        return Collections.emptyList();
    }



}
