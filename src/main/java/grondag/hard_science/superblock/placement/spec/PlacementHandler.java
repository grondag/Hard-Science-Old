package grondag.hard_science.superblock.placement.spec;

import javax.annotation.Nonnull;

import grondag.exotic_matter.ConfigXM;
import grondag.exotic_matter.block.SuperBlockStackHelper;
import grondag.exotic_matter.model.ISuperBlock;
import grondag.exotic_matter.model.ISuperModelState;
import grondag.exotic_matter.model.MetaUsage;
import grondag.exotic_matter.model.varia.SuperBlockHelper;
import grondag.exotic_matter.placement.OffsetPosition;
import grondag.exotic_matter.placement.PlacementEvent;
import grondag.exotic_matter.placement.PlacementPosition;
import grondag.exotic_matter.placement.SpeciesMode;
import grondag.exotic_matter.player.ModifierKeys;
import grondag.exotic_matter.player.ModifierKeys.ModifierKey;
import grondag.exotic_matter.varia.Useful;
import grondag.exotic_matter.world.HorizontalFace;
import grondag.exotic_matter.world.IBlockRegion;
import grondag.hard_science.Log;
import grondag.hard_science.superblock.placement.Build;
import grondag.hard_science.superblock.virtual.VirtualTileEntity;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
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
                        && !ISuperBlock.isVirtuallySolidBlock(onPos, player))
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
        return PlacementItem.doRightClickBlock(player, onPos, onFace, hitVec, stack, item);
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
        else if(ModifierKeys.isModifierKeyPressed(player, ModifierKey.ALT_KEY))
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
    
    public static void placeVirtualBlock(World world, ItemStack stack, EntityPlayer player, BlockPos pos, Build build)
    {
        if(!player.capabilities.allowEdit || build == null || !build.isOpen()) return;

        SoundType soundtype = SuperBlockStackHelper.getStackSubstance(stack).soundType;

        PlacementItem item = PlacementItem.getPlacementItem(stack);
        if(item == null) return;

        IBlockState placedState = item.getPlacementBlockStateFromStack(stack);
        
        if (placeBlockAt(stack, player, world, pos, null, 0, 0, 0, placedState))
        {
            build.addPosition(pos);

            world.playSound(null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
        
            TileEntity blockTE = world.getTileEntity(pos);
            if (blockTE != null && blockTE instanceof VirtualTileEntity) 
            {
                ((VirtualTileEntity)blockTE).setBuild(build);
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


    /**
     * Returns modified copy of stack adjusted for context-dependent state.
     * Right now this is just species.
     * Intended for single and cubic region placements of non-CSG virtual blocks.<p>
     * 
     * Assumes block rotation was already set in stack by calling 
     * {@link BlockOrientationHandler#configureStackForPlacement(ItemStack, EntityPlayer, PlacementPosition)}
     * when spec was constructed.
     */
    public static ItemStack cubicPlacementStack(SingleStackBuilder specBuilder)
    {

        ItemStack stack = specBuilder.placedStack().copy();
        PlacementPosition pPos = specBuilder.placementPosition();
        ISuperModelState modelState = SuperBlockStackHelper.getStackModelState(stack);
        if(modelState != null && modelState.hasSpecies())
        {
            int species = speciesForPlacement(specBuilder.player(), pPos.onPos, pPos.onFace, stack, specBuilder.region());
            if(species >= 0) 
            {
                modelState.setSpecies(species);
                SuperBlockStackHelper.setStackModelState(stack, modelState);
                if(modelState.metaUsage() == MetaUsage.SPECIES)
                {
                    stack.setItemDamage(species);
                }
            }
        }
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
        if(ModifierKeys.isModifierKeyPressed(player, ModifierKey.ALT_KEY)) mode = mode.alternate();

        boolean shouldBreak = mode != SpeciesMode.MATCH_MOST;

        ISuperModelState  withModelState = SuperBlockStackHelper.getStackModelState(stack);
        if(withModelState == null || !withModelState.hasSpecies()) return 0;

        if(player.world == null) return 0;
        
        World world = player.world;

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
            if(checkCount++ >= ConfigXM.BLOCKS.maxPlacementCheckCount) break;
        }
        
        for(BlockPos pos : region.surfacePositions())
        {
            int interiorSpecies = SuperBlockHelper.getJoinableSpecies(world, pos, withBlockState, withModelState);
            if(interiorSpecies >= 0 && interiorSpecies <= 15) surfaceCount[interiorSpecies]++;
            if(checkCount++ >= ConfigXM.BLOCKS.maxPlacementCheckCount) break;
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




}
