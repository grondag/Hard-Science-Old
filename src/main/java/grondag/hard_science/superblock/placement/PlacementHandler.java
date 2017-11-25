package grondag.hard_science.superblock.placement;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.Configurator;
import grondag.hard_science.Log;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.library.world.CubicBlockRegion;
import grondag.hard_science.library.world.HorizontalFace;
import grondag.hard_science.library.world.IBlockRegion;
import grondag.hard_science.library.world.IntegerAABB;
import grondag.hard_science.player.ModPlayerCaps;
import grondag.hard_science.player.ModPlayerCaps.ModifierKey;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.model.state.MetaUsage;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.placement.spec.IPlacementSpecBuilder;
import grondag.hard_science.superblock.placement.spec.SingleStackBuilder;
import grondag.hard_science.superblock.varia.SuperBlockHelper;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * TODO: 
 * Add render for excavations
 * Temporary drone service machine
 * Make virtual blocks truly virtual (no world state)
 * Selection mode - veinmine
 * Selection mode - builders wand
 * Selection mode - complete region
 * Block filter - add way to add specific blocks to filter
 * Block filter - implement whitelist / blacklist modes
 * Add fixed selection mode (in addition to floating, normal)
 * Follow mode
 * Fixed block orientation
 * Dynamic block orientation
 * Match closest block orientation
 * Block History
 * Region History
 * mode indicator on Item icon
 * Undo
 * performance tuning
 * Optimization: don't generate a placement if target is already equal to placed block / air if delete
 * Cache predicted results - previous attempt wasn't workable, see predicePlacementResults
 * Add move selection controls (AWSD Sneak/Jump with Ctrl, relative to look vector)
 */


public abstract class PlacementHandler
{

    //    /**
    //     * This and similarly-named variables cache the last result of {@link #predictPlacementResults(EntityPlayerSP, ItemStack, PlacementItem)}.
    //     */
    //    @SideOnly(Side.CLIENT)
    //    private static PlacementResult lastPredictionResult = null;
    //    @SideOnly(Side.CLIENT)
    //    private static ItemStack lastPredicionStack = ItemStack.EMPTY;
    //    @SideOnly(Side.CLIENT)
    //    private static BlockPos lastPredictionPos = null;
    //    @SideOnly(Side.CLIENT)
    //    private static EnumFacing lastPredictionFace = null;
    //    @SideOnly(Side.CLIENT)
    //    private static Vec3d lastPredictionHitVec = null;

    /**
     * Called client-side by overlay renderer to know
     * what should be rendered for player. If no operation is in progress,
     * assumes player will click the right mouse button.
     * If an operation is in progress, assumes user will click the button
     * that completes the operation.
     */
    @SideOnly(Side.CLIENT)
    @Nonnull
    public static PlacementResult predictPlacementResults(EntityPlayerSP player, @Nonnull ItemStack stack, @Nonnull PlacementItem item)
    {

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

        if(!item.isFloatingSelectionEnabled(stack))
        {
            // if floating selection enabled, there is no "placed on" position
            // No floating selection - so look for block placed on.

            Minecraft mc = Minecraft.getMinecraft();

            RayTraceResult target = mc.objectMouseOver;

            if(target.typeOfHit == RayTraceResult.Type.BLOCK)
            {
                onPos = target.getBlockPos();

                // if block out of range there will be no "placed on" position
                if(onPos.distanceSq(onPos) > Useful.squared(mc.playerController.getBlockReachDistance() + 1)
                        || player.world.getBlockState(onPos).getMaterial().isReplaceable())
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

        // assume user will click the right mouse button
        // Pass stack copy so that predicted action doesn't affect real stack
        return doRightClickBlock(player, onPos, onFace, hitVec, stack, item);
    }

    /**
     * Call when item is left clicked on a block.
     * Returns true if the click was handled and input processing should stop.
     */
    public boolean handleLeftClickBlock(EntityPlayer player, BlockPos onPos, EnumFacing face, Vec3d hitVec)
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
        if(!PlacementItem.isPlacementItem(stack)) return PlacementResult.EMPTY_RESULT_CONTINUE;
        PlacementItem item = (PlacementItem)stack.getItem();

        if(item.isFixedRegionSelectionInProgress(stack))
        {

            // any left click while selecting cancels the operation;
            return new PlacementResult(
                    null, 
                    PlacementEvent.CANCEL_PLACEMENT_REGION,
                    null);
        }
        else if(ModPlayerCaps.isModifierKeyPressed(player, ModifierKey.ALT_KEY))
        {
            // Alt+left click: undo block placement

            //TODO
            Log.info("UNDO PLACEMENT LOGIC HAPPENS NOW");
            return new PlacementResult(
                    onPos, 
                    PlacementEvent.UNDO_PLACEMENT,
                    null);
        }
        else
        {
            // normal left click on block - let normal MC behavior occur
            return PlacementResult.EMPTY_RESULT_CONTINUE;
        }
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
    public static IntegerAABB blockBoundaryAABB(BlockPos startPos, BlockPos endPos)
    {
        IntegerAABB aabbStart = new IntegerAABB(startPos);
        IntegerAABB aabbEnd = new IntegerAABB(endPos);
        return aabbStart.union(aabbEnd);
    }

    /**
     * Determines outcome when player right clicks on the face of a block.
     *  if no hit block is known or if floating selection is known to be enabled, pass onPos, onFace, and hitVec = null instead.
     * DOES NOT UPDATE STATE.
     */
    public static PlacementResult doRightClickBlock(EntityPlayer player, @Nullable BlockPos onPos, @Nullable EnumFacing onFace, @Nullable Vec3d hitVec, @Nonnull ItemStack stack, @Nonnull PlacementItem item)
    {
        PlacementPosition pPos = new PlacementPosition(player, onPos, onFace, hitVec, stack);

        // if not position, then either need to be using floating selection 
        // or a fixed region (for preview only - see logic below) if not enabled
        if(onPos == null && !item.isFloatingSelectionEnabled(stack)) 
        {
            // don't force player to be in placement range to see big region selections
            // but note this doesn't work for selection in progress
            if(item.isFixedRegionEnabled(stack) && !item.isFixedRegionSelectionInProgress(stack))
            {
                return new PlacementResult(
                        pPos.inPos, 
                        PlacementEvent.NO_OPERATION_CONTINUE,
                        PlacementSpecHelper.placementBuilder(player, pPos, stack));
            }
            else return PlacementResult.EMPTY_RESULT_CONTINUE;
        }


        // nothing to do if no position
        if(pPos.inPos == null) return PlacementResult.EMPTY_RESULT_CONTINUE;


        // only virtual blocks support advanced placement behavior
        // so emulate vanilla right-click behavior if we have non-virtual block
        if(item.getSuperBlock() != null && !item.getSuperBlock().isVirtual())
        {
            ItemStack tweakedStack = stack.copy();
            item.setTargetMode(tweakedStack, TargetMode.ON_CLICKED_FACE);

            return new PlacementResult(
                    pPos.inPos, 
                    PlacementEvent.PLACE,
                    PlacementSpecHelper.placementBuilder(player, pPos, tweakedStack));
        }

        // Ctrl + right click: start new placement region
        if(ModPlayerCaps.isModifierKeyPressed(player, ModifierKey.CTRL_KEY))
        {
            ItemStack tweakedStack = stack.copy();
            item.fixedRegionStart(tweakedStack, pPos.inPos, false);

            return new PlacementResult(
                    pPos.inPos, 
                    PlacementEvent.START_PLACEMENT_REGION,
                    PlacementSpecHelper.placementBuilder(player, pPos, tweakedStack));
        }

        if(item.isFixedRegionSelectionInProgress(stack))
        {
            // finish placement region
            ItemStack tweakedStack = stack.copy();
            item.fixedRegionFinish(tweakedStack, player, pPos.inPos, false);

            return new PlacementResult(
                    pPos.inPos, 
                    PlacementEvent.SET_PLACEMENT_REGION,
                    PlacementSpecHelper.placementBuilder(player, pPos, tweakedStack));
        }
        else
        {
            // normal right click on block 
            IPlacementSpecBuilder builder = PlacementSpecHelper.placementBuilder(player, pPos, stack);
            if(builder.validate())
            {
                return new PlacementResult(
                        pPos.inPos, 
                        builder.isExcavation() ? PlacementEvent.EXCAVATE : PlacementEvent.PLACE,
                        builder);
            }
            else
            {
                return PlacementResult.EMPTY_RESULT_CONTINUE;
            }
        }
    }

    //FIXME: remove
    public static PlacementResult doPlacement(ItemStack stack, EntityPlayer player, PlacementPosition pPos)
    {
        if(!PlacementItem.isPlacementItem(stack)) return PlacementResult.EMPTY_RESULT_CONTINUE;
        PlacementItem item = (PlacementItem)stack.getItem();

        TargetMode selectionMode = item.getTargetMode(stack);
        boolean isHollow = selectionMode == TargetMode.HOLLOW_REGION;
        boolean isExcavating = false; //item.isDeleteModeEnabled(stack);

        // check for a multi-block placement region
        BlockPos placementPos = selectionMode.usesSelectionRegion ? item.getRegionSize(stack, true) : null;
        BlockPos endPos = placementPos == null ? pPos.inPos : getPlayerRelativeOffset(pPos.inPos, placementPos, player, pPos.onFace, OffsetPosition.FLIP_NONE);

        CubicBlockRegion region = new CubicBlockRegion(pPos.inPos, endPos, isHollow);
        if(selectionMode.usesSelectionRegion) excludeObstaclesInRegion(player, pPos, stack, region);

        boolean adjustmentEnabled = item.getRegionOrientation(stack) == RegionOrientation.AUTOMATIC && !isExcavating;

        /** true if no obstacles */
        boolean isClear = region.exclusions().isEmpty() 
                && player.world.checkNoEntityCollision(region.toAABB());

        // Adjust selection to avoid exclusions if requested and necessary.
        // No point if is only a single position or a selection mode that doesn't use the selection region.
        // Also can't do this if user is currently selecting a region
        if(     adjustmentEnabled 
                && selectionMode.usesSelectionRegion 
                && !isClear 
                && placementPos != null 
                && !pPos.inPos.equals(endPos))
        {
            for(OffsetPosition offset : OffsetPosition.ALTERNATES)
            {
                // first try pivoting the selection box around the position being targeted
                BlockPos endPos2 = getPlayerRelativeOffset(pPos.inPos, placementPos, player, pPos.onFace, offset);
                CubicBlockRegion region2 = new CubicBlockRegion(pPos.inPos, endPos2, isHollow);
                excludeObstaclesInRegion(player, pPos, stack, region2);

                if(region2.exclusions().isEmpty() && player.world.checkNoEntityCollision(region2.toAABB()))
                {
                    endPos = endPos2;
                    region = region2;
                    isClear = true;
                    break;
                }
            }

            if(!isClear)
            {
                // that didn't work, so try nudging the region a block in each direction
                EnumFacing[] checkOrder = faceCheckOrder(player, pPos.onFace);

                for(EnumFacing face : checkOrder)
                {
                    BlockPos startPos2 = pPos.inPos.offset(face);
                    BlockPos endPos2 = endPos.offset(face);
                    CubicBlockRegion region2 = new CubicBlockRegion(startPos2, endPos2, isHollow);
                    excludeObstaclesInRegion(player, pPos, stack, region2);
                    if(region2.exclusions().isEmpty() && player.world.checkNoEntityCollision(region2.toAABB()))
                    {
                        endPos = endPos2;
                        region = region2;
                        isClear = true;
                        break;
                    }
                }
            }
        }

        List<Pair<BlockPos, ItemStack>> placements = (selectionMode != TargetMode.COMPLETE_REGION || isClear)
                ? placeRegion(player, pPos.onPos, pPos.onFace, new Vec3d(pPos.hitX, pPos.hitY, pPos.hitZ), stack, region)
                        : Collections.emptyList();

                return new PlacementResult(
                        pPos.inPos, 
                        isExcavating ? PlacementEvent.EXCAVATE : PlacementEvent.PLACE,
                                PlacementSpecHelper.placementBuilder(player, pPos, stack));
    }

    private static boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState)
    { 
        // world.setBlockState returns false if the state was already the requested state
        // this is OK normally, but if we need to update the TileEntity it is the opposite of OK
        boolean wasUpdated = world.setBlockState(pos, newState, 3)
                || world.getBlockState(pos) == newState;
            
        if(!wasUpdated) 
            return false;
        
        newState.getBlock().onBlockPlacedBy(world, pos, newState, player, stack);
        return true;
    }
    
    public static void placeVirtualBlock(World world, ItemStack stack, EntityPlayer player, BlockPos pos)
    {
        if(!player.capabilities.allowEdit) return;

        SoundType soundtype = PlacementItem.getStackSubstance(stack).soundType;

        ModelState placedModelState = PlacementItem.getStackModelState(stack);

        AxisAlignedBB axisalignedbb = placedModelState == null ? Block.FULL_BLOCK_AABB : placedModelState.getShape().meshFactory().collisionHandler().getCollisionBoundingBox(placedModelState);

        if(world.checkNoEntityCollision(axisalignedbb.offset(pos)))
        {
            PlacementItem item = PlacementItem.getPlacementItem(stack);
            if(item == null) return;

            IBlockState placedState = item.getPlacementBlockStateFromStack(stack);
            //targetBlock.getStateFromMeta(placedStack.getMetadata());
            
            if (placeBlockAt(stack, player, world, pos, null, 0, 0, 0, placedState))
            {
                world.playSound(null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            }
        }
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

    @Deprecated
    public static void excludeObstaclesInRegion(EntityPlayer player, PlacementPosition pPos, ItemStack stack, CubicBlockRegion region)
    {
        if(!PlacementItem.isPlacementItem(stack)) return;
        PlacementItem item = (PlacementItem)stack.getItem();
        boolean isVirtual = item.isVirtual(stack);

        FilterMode filterMode =  item.getFilterMode(stack);
        boolean isExcavating = false; //item.isDeleteModeEnabled(stack);

        // if excavating, adjust filter mode if needed so that it does something
        if(isExcavating && filterMode == FilterMode.FILL_REPLACEABLE) filterMode = FilterMode.REPLACE_SOLID;

        World world = player.world;

        HashSet<BlockPos> set = new HashSet<BlockPos>();

        if(isExcavating)
        {
            for(BlockPos.MutableBlockPos pos : region.positions())
            {
                IBlockState blockState = world.getBlockState(pos);
                if(blockState.getBlock().isAir(blockState, world, pos) || !filterMode.shouldAffectBlock(blockState, world, pos, stack, isVirtual))
                {
                    set.add(pos.toImmutable());
                }
            }
        }
        else
        {
            for(BlockPos.MutableBlockPos pos : region.includedPositions())
            {
                IBlockState blockState = world.getBlockState(pos);
                if(!filterMode.shouldAffectBlock(blockState, world, pos, stack, isVirtual))
                {
                    set.add(pos.toImmutable());
                }
            }
        }
        region.exclude(set);
    }

    /**
     * Generates positions and item stacks that should be placed in the given region 
     * according to current stack settings.
     * The 
     */
    @Deprecated
    public static List<Pair<BlockPos, ItemStack>> placeRegion(EntityPlayer player, BlockPos onPos, EnumFacing onFace, Vec3d hitVec, ItemStack stackIn, 
            CubicBlockRegion region)
    {

        ItemStack stack;

        //        if(PlacementItem.isDeleteModeEnabled(stackIn))
        //        {
        //            stack = Items.AIR.getDefaultInstance();
        //        }
        //        else
        //        {
        stack = stackIn.copy();

        ModelState modelState = PlacementItem.getStackModelState(stack);
        if(modelState.hasSpecies())
        {
            int species = speciesForPlacement(player, onPos, onFace, stack, region);
            if(species >= 0) 
            {
                modelState.setSpecies(species);
                PlacementItem.setStackModelState(stack, modelState);
                if(modelState.metaUsage() == MetaUsage.SPECIES)
                {
                    stack.setItemDamage(species);
                }
            }
        }
        //        }

        ImmutableList.Builder<Pair<BlockPos, ItemStack>> builder = ImmutableList.builder();

        for(BlockPos.MutableBlockPos pos : region.includedPositions())
        {
            builder.add(Pair.of(pos.toImmutable(), stack));
        }
        return builder.build();
    }

    /**
     * Returns modified copy of stack adjusted for context-dependent state.
     * Intended for single and cubic region placements of non-CSG virtual blocks
     */
    public static ItemStack cubicPlacementStack(SingleStackBuilder specBuilder)
    {

        ItemStack stack = specBuilder.placedStack().copy();
        PlacementPosition pPos = specBuilder.placementPosition();
        ModelState modelState = PlacementItem.getStackModelState(stack);
        if(modelState.hasSpecies())
        {
            int species = speciesForPlacement(specBuilder.player(), pPos.onPos, pPos.onFace, stack, specBuilder.region());
            if(species >= 0) 
            {
                modelState.setSpecies(species);
                PlacementItem.setStackModelState(stack, modelState);
                if(modelState.metaUsage() == MetaUsage.SPECIES)
                {
                    stack.setItemDamage(species);
                }
            }
        }
        
        // TODO: block rotation, etc.
        
        return stack;
    }
    
    /**
     * Determines species that should be used for placing a region
     * according to current stack settings.
     */
    public static int speciesForPlacement(EntityPlayer player, BlockPos onPos, EnumFacing onFace, ItemStack stack, IBlockRegion region)
    {
        // ways this can happen:
        // have a species we want to match because we clicked on a face
        // break with everything - need to know adjacent species
        // match with most - need to know adjacent species

        if(!PlacementItem.isPlacementItem(stack)) return 0;
        PlacementItem item = (PlacementItem)stack.getItem();

        SpeciesMode mode = item.getSpeciesMode(stack);
        if(ModPlayerCaps.isModifierKeyPressed(player, ModifierKey.ALT_KEY)) mode = mode.alternate();

        boolean shouldBreak = mode != SpeciesMode.MATCH_MOST;

        ModelState  withModelState = PlacementItem.getStackModelState(stack);
        if(withModelState == null || !withModelState.hasSpecies()) return 0;

        World world = player.world;
        if(world == null) return 0;

        IBlockState withBlockState = item.getPlacementBlockStateFromStack(stack);

        // if no region provided or species mode used clicked block then 
        // result is based on the clicked face
        if(region == null 
                || ((mode == SpeciesMode.MATCH_CLICKED || mode == SpeciesMode.MATCH_MOST)
                        && onPos != null && onFace != null))
        {
            int clickedSpecies = SuperBlockHelper.getJoinableSpecies(world, onPos, withBlockState, withModelState);
            
            // if no region, then return something different than what is clicked,
            // unless didn't get a species - will return 0 in that case.
            if(region == null) return shouldBreak || clickedSpecies < 0 ? clickedSpecies + 1 : clickedSpecies;
                
            if(clickedSpecies >= 0) return clickedSpecies;
        }
        
        int[] adjacentCount = new int[16];
        int[] surfaceCount = new int[16];

        /** limit block positions checked for very large regions */
        int checkCount = 0;
        
        for(BlockPos pos : region.adjacentPositions())
        {
            int adjacentSpecies = SuperBlockHelper.getJoinableSpecies(world, pos, withBlockState, withModelState);
            if(adjacentSpecies >= 0 && adjacentSpecies <= 15) adjacentCount[adjacentSpecies]++;
            if(checkCount++ >= Configurator.BLOCKS.maxPlacementCheckCount) break;
        }
        
        for(BlockPos pos : region.surfacePositions())
        {
            int interiorSpecies = SuperBlockHelper.getJoinableSpecies(world, pos, withBlockState, withModelState);
            if(interiorSpecies >= 0 && interiorSpecies <= 15) surfaceCount[interiorSpecies]++;
            if(checkCount++ >= Configurator.BLOCKS.maxPlacementCheckCount) break;
        }

        if(shouldBreak)
        {
            // find a species that matches as few things as possible
            int bestSpecies = 0;
            int bestCount = adjacentCount[0] + surfaceCount[0];

            for(int i = 1; i < 16; i++)
            {
                int tryCount = adjacentCount[i] + surfaceCount[i];
                if(tryCount < bestCount) 
                {
                    bestCount = tryCount;
                    bestSpecies = i;
                }
            }
            return bestSpecies;
        }
        else
        {
            // find the most common species and match with that
            // give preference to species that are included in the region surface if any
            int bestSpecies = 0;
            int bestCount = surfaceCount[0];

            for(int i = 1; i < 16; i++)
            {
                if(surfaceCount[i] > bestCount)
                {
                    bestCount = surfaceCount[i];
                    bestSpecies = i;
                }
            }

            if(bestCount == 0)
            {
                for(int i = 1; i < 16; i++)
                {
                    if(adjacentCount[i] > bestCount)
                    {
                        bestCount = adjacentCount[i];
                        bestSpecies = i;
                    }
                }
            }
            return bestSpecies;
        }
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
    @Deprecated
    public List<Pair<BlockPos, ItemStack>> getPlacementResults(EntityPlayer playerIn, World worldIn, BlockPos posOn, EnumHand hand, EnumFacing facing, float hitX,
            float hitY, float hitZ, ItemStack stack)
    {
        if(!PlacementItem.isPlacementItem(stack)) return Collections.emptyList();
        PlacementItem item = (PlacementItem)stack.getItem();

        TargetMode placementMode = item.getTargetMode(stack);

        boolean isFloating = item.isFloatingSelectionEnabled(stack);

        SuperBlock stackBlock = item.getSuperBlock();

        ModelState stackModelState = PlacementItem.getStackModelState(stack);

        switch(placementMode)
        {
        case FILL_REGION:
            return isFloating 
                    ? floatingMultiBlockResults()
                            : multiBlockResults();

        case ON_CLICKED_SURFACE:
            additiveResults();

        case ON_CLICKED_FACE:
        default:
            return isFloating 
                    ? floatingSingleBlockResult()
                            : singleBlockResult(worldIn, posOn, hand, facing, hitZ, hitZ, hitZ, stack);
        }
    }

    /**
     * Conventional block placement - placed on a given face, so that face can be used for context
     */
    public List<Pair<BlockPos, ItemStack>> singleBlockResult(World worldIn, BlockPos posOn, EnumHand hand, EnumFacing facing, float hitX,
            float hitY, float hitZ, ItemStack stack)
    {
        return Collections.emptyList();
    }


    /**
     * Single block placed at an arbitrary location, so there is no "placed on" face that can be used for context
     */
    public List<Pair<BlockPos, ItemStack>> floatingSingleBlockResult()
    {
        return Collections.emptyList();
    }

    /**
     * Multiple blocks but still placed on a given face, so that face can still be used for context
     */
    public List<Pair<BlockPos, ItemStack>> multiBlockResults()
    {
        return Collections.emptyList();
    }

    /**
     * Multiple blocks placed at an arbitrary location, so there is no "placed on" face that can be used for context
     */
    public List<Pair<BlockPos, ItemStack>> floatingMultiBlockResults()
    {
        return Collections.emptyList();
    }

    /**
     * "Builders wand" style of placement.
     */
    public List<Pair<BlockPos, ItemStack>> additiveResults()
    {
        return Collections.emptyList();
    }



}
