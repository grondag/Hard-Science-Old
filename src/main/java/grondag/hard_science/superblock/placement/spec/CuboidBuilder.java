package grondag.hard_science.superblock.placement.spec;

import static grondag.exotic_matter.placement.PlacementPreviewRenderMode.OBSTRUCTED;
import static grondag.exotic_matter.placement.PlacementPreviewRenderMode.PLACE;
import static grondag.exotic_matter.placement.PlacementPreviewRenderMode.SELECT;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import grondag.exotic_matter.placement.OffsetPosition;
import grondag.exotic_matter.placement.PlacementPosition;
import grondag.exotic_matter.placement.PlacementPreviewRenderMode;
import grondag.exotic_matter.render.RenderUtil;
import grondag.exotic_matter.simulator.IWorldTask;
import grondag.exotic_matter.simulator.domain.DomainManager;
import grondag.exotic_matter.simulator.domain.IDomain;
import grondag.exotic_matter.simulator.job.RequestPriority;
import grondag.exotic_matter.varia.FixedRegionBounds;
import grondag.exotic_matter.world.CubicBlockRegion;
import grondag.exotic_matter.world.IBlockRegion;
import grondag.exotic_matter.world.WorldHelper;
import grondag.exotic_matter.ClientProxy;
import grondag.hard_science.simulator.jobs.AbstractTask;
import grondag.hard_science.simulator.jobs.Job;
import grondag.hard_science.simulator.jobs.JobManager;
import grondag.hard_science.simulator.jobs.tasks.ExcavationTask;
import grondag.hard_science.superblock.placement.Build;
import grondag.hard_science.superblock.placement.BuildManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CuboidBuilder extends VolumetricBuilder
{

    protected CubicBlockRegion region;

    /**
     * Where should block shape preview sample be drawn?
     */
    protected BlockPos previewPos;

    public CuboidBuilder(ItemStack placedStack, EntityPlayer player, PlacementPosition pPos)
    {
        super(placedStack, player, pPos);
        this.previewPos = pPos.inPos;
    }

//    protected CuboidPlacementSpec buildSpec()
//    {
//        CuboidPlacementSpec result = new CuboidPlacementSpec(this);
//        result.isHollow = this.isHollow;
//        result.region = this.region;
//        return result;
//    }

    @Override
    protected boolean doValidate()
    {
        if(this.isSelectionInProgress) 
        {
            this.region = new CubicBlockRegion(pPos.inPos, this.placementItem.fixedRegionSelectionPos(this.placedStack()).getLeft(), false);
            return true;
        }

        if(this.isFixedRegion)
        {
            FixedRegionBounds bounds = this.placementItem.getFixedRegion(this.placedStack());
            this.region = new CubicBlockRegion(bounds.fromPos, bounds.toPos, this.isHollow);
            this.excludeObstaclesInRegion(this.region);

            this.outputStack = PlacementHandler.cubicPlacementStack(this);
            return this.canPlaceRegion(region);
            
        }

        else if(this.isExcavation)
        {
            // excavation regions do not take adjustment and are always
            // relative to the "inPos" block.
            BlockPos startPos = pPos.inPos;
            BlockPos endPos = pPos.inPos;

            if(this.offsetPos.getZ() > 1)
            {
                // depth
                endPos = endPos.offset(pPos.onFace.getOpposite(), this.offsetPos.getZ() - 1);
            }

            Pair<EnumFacing, EnumFacing> nearestCorner 
            = WorldHelper.closestAdjacentFaces(
                    this.pPos.onFace, 
                    (float)this.pPos.hitX, 
                    (float)this.pPos.hitY, 
                    (float)this.pPos.hitZ);

            // height
            final int h = this.offsetPos.getY();
            if(h > 1)
            {
                EnumFacing relativeUp = WorldHelper.relativeUp(this.player, this.pPos.onFace);
                final int half_h = h / 2;

                final boolean isUpclosest = nearestCorner.getLeft() == relativeUp
                        || nearestCorner.getRight() == relativeUp;

                final boolean fullUp = (h & 1) == 1 || isUpclosest;
                final boolean fullDown = (h & 1) == 1 || !isUpclosest;

                startPos = startPos.offset(relativeUp, fullUp ? half_h : half_h - 1);
                endPos = endPos.offset(relativeUp.getOpposite(), fullDown ? half_h : half_h - 1);
            }

            // width
            final int w = this.offsetPos.getX();
            if(w > 1)
            {
                EnumFacing relativeLeft = WorldHelper.relativeLeft(this.player, this.pPos.onFace);
                final int half_w = w / 2;

                final boolean isLeftclosest = nearestCorner.getLeft() == relativeLeft
                        || nearestCorner.getRight() == relativeLeft;

                final boolean fullLeft = (w & 1) == 1 || isLeftclosest;
                final boolean fullRight = (w & 1) == 1 || !isLeftclosest;

                startPos = startPos.offset(relativeLeft, fullLeft ? half_w : half_w - 1);
                endPos = endPos.offset(relativeLeft.getOpposite(), fullRight ? half_w : half_w - 1);
            }

            this.region = new CubicBlockRegion(startPos, endPos, false);
            return true;
        }

        else
        {
            // pass null face into relative offset when using floating selection
            // to avoid re-orientation based on hit face
            EnumFacing offsetFace = pPos.isFloating ? null : pPos.onFace;

            BlockPos endPos = PlacementHandler.getPlayerRelativeOffset(this.pPos.inPos, this.offsetPos, this.player, offsetFace, OffsetPosition.FLIP_NONE);
            CubicBlockRegion region = new CubicBlockRegion(pPos.inPos, endPos, this.isHollow);

            this.excludeObstaclesInRegion(region);
            boolean isClear = this.canPlaceRegion(region);

            if(this.isAdjustmentEnabled && !isClear)
            {
                //try to adjust

                for(OffsetPosition offset : OffsetPosition.ALTERNATES)
                {
                    // start try pivoting the selection box around the position being targeted
                    BlockPos endPos2 = PlacementHandler.getPlayerRelativeOffset(this.pPos.inPos, this.offsetPos, this.player, offsetFace, offset);
                    CubicBlockRegion region2 = new CubicBlockRegion(pPos.inPos, endPos2, this.isHollow);
                    this.excludeObstaclesInRegion(region2);

                    if(this.canPlaceRegion(region2))
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
                    EnumFacing[] checkOrder = PlacementHandler.faceCheckOrder(player, offsetFace);

                    for(EnumFacing face : checkOrder)
                    {
                        BlockPos startPos2 = pPos.inPos.offset(face);
                        BlockPos endPos2 = endPos.offset(face);
                        CubicBlockRegion region2 = new CubicBlockRegion(startPos2, endPos2, isHollow);
                        this.excludeObstaclesInRegion(region2);
                        if(this.canPlaceRegion(region2))
                        {
                            endPos = endPos2;
                            region = region2;
                            this.previewPos = startPos2;
                            isClear = true;
                            break;
                        }
                    }
                }
            }

            this.region = region;

            this.outputStack = PlacementHandler.cubicPlacementStack(this);

            return isClear;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected BlockPos previewPos()
    {
        return this.previewPos;
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected void drawSelection(Tessellator tessellator, BufferBuilder bufferBuilder)
    {
        if(this.region == null) return;

        AxisAlignedBB box = this.region.toAABB();
        // draw edge without depth to show extent of region
        GlStateManager.disableDepth();
        GlStateManager.glLineWidth(2.0F);
        bufferBuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        RenderGlobal.drawBoundingBox(bufferBuilder, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, SELECT.red, SELECT.green, SELECT.blue, 1f);
        tessellator.draw();

        // draw sides with depth to better show what parts are unobstructed
        GlStateManager.enableDepth();
        bufferBuilder.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        RenderGlobal.addChainedFilledBoxVertices(bufferBuilder, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, SELECT.red, SELECT.green, SELECT.blue, 0.4f);
        tessellator.draw();
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected void drawPlacement(Tessellator tessellator, BufferBuilder bufferBuilder, PlacementPreviewRenderMode previewMode)
    {
        if(this.region == null) return;

        AxisAlignedBB box = this.region.toAABB();

        // fixed regions could be outside of view
        if(ClientProxy.camera() == null || !ClientProxy.camera().isBoundingBoxInFrustum(box)) return;

        // draw edges without depth to show extent of region
        GlStateManager.disableDepth();
        GlStateManager.glLineWidth(2.0F);
        bufferBuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        RenderGlobal.drawBoundingBox(bufferBuilder, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, previewMode.red, previewMode.green, previewMode.blue, 1f);
        tessellator.draw();

        Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
        if(entity != null)
        {
            GlStateManager.glLineWidth(1.0F);
            bufferBuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            RenderUtil.drawGrid(
                    bufferBuilder, 
                    box, 
                    entity.getPositionEyes(Minecraft.getMinecraft().getRenderPartialTicks()),
                    0, 0, 0, previewMode.red, previewMode.green, previewMode.blue, 0.5f);

            tessellator.draw();
        }

        if(previewMode == OBSTRUCTED)
        {
            // try to show where obstructions are
            for(BlockPos pos : this.region.exclusions())
            {
                bufferBuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
                RenderGlobal.drawBoundingBox(bufferBuilder, pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, OBSTRUCTED.red, OBSTRUCTED.green, OBSTRUCTED.blue, 1f);
                tessellator.draw();
            }
        }
        else if(previewMode == PLACE)
        {
            // show shape/orientation of blocks to be be placed via a sample 
            this.drawPlacementPreview(tessellator, bufferBuilder);
        }

        // draw sides with depth to better show what parts are unobstructed
        GlStateManager.enableDepth();
        bufferBuilder.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        RenderGlobal.addChainedFilledBoxVertices(bufferBuilder, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, previewMode.red, previewMode.green, previewMode.blue, 0.4f);
        tessellator.draw();
    }

    @Override
    public IWorldTask worldTask(EntityPlayerMP player)
    {
        if(this.isExcavation)
        {
            // excavation world task sequences entries using
            // a flood fill starting with the block last clicked
            // by the player.
            return new IWorldTask()
            {
//                private CuboidPlacementSpec spec = (CuboidPlacementSpec) buildSpec();
                private Job job = new Job(RequestPriority.MEDIUM, player);
                IDomain domain = DomainManager.instance().getActiveDomain(player);

                /**
                 * Block positions to be checked. Will initially contain
                 * only the starting (user-clicked) position. Entry is
                 * antecedent, or null if no dependency.
                 */
                ArrayDeque<Pair<BlockPos, ExcavationTask>> queue = new ArrayDeque<Pair<BlockPos, ExcavationTask>>();

                /**
                 * Block positions that have been tested.
                 */
                HashSet<BlockPos> checked = new HashSet<BlockPos>();

                World world = player.world;

                {
                    scheduleVisitIfNotAlreadyVisited(pPos.inPos, null);
                }

                @Override
                public int runInServerTick(int maxOperations)
                {
                    if(queue.isEmpty()) return 0;

                    int opCount = 0;
                    while(opCount < maxOperations)
                    {
                        Pair<BlockPos, ExcavationTask> visit = queue.poll();
                        if(visit == null) break;

                        BlockPos pos = visit.getLeft();

                        opCount++;

                        // is the position inside our region?
                        if(!region.contains(pos)) continue;

                        // is the position inside the world?
                        if(world.isOutsideBuildHeight(pos)) continue;

                        boolean canPassThrough = false;

                        IBlockState blockState = world.getBlockState(pos);

                        // will be antecedent for any branches from here
                        // if this is empty space, then will be antecedent for this visit
                        ExcavationTask branchAntecedent = visit.getRight();

                        // is the block at the position affected
                        // by this excavation?
                        if(effectiveFilterMode.shouldAffectBlock(
                                blockState, 
                                world, 
                                pos, 
                                outputStack,
                                isVirtual))
                        {
                            branchAntecedent = new ExcavationTask(pos);
                            job.addTask(branchAntecedent);
                            if(visit.getRight() != null)
                            {
                                AbstractTask.link(visit.getRight(), branchAntecedent);
                            }
                            canPassThrough = true;
                        }

                        // even if we can't excavate the block, 
                        // can we move through it to check others?
                        canPassThrough = canPassThrough || !blockState.getMaterial().blocksMovement();

                        // check adjacent blocks if are or will
                        // be accessible and haven't already been
                        // checked.
                        if(canPassThrough)
                        {
                            opCount++;
                            scheduleVisitIfNotAlreadyVisited(pos.up(), branchAntecedent);
                            scheduleVisitIfNotAlreadyVisited(pos.down(), branchAntecedent);
                            scheduleVisitIfNotAlreadyVisited(pos.east(), branchAntecedent);
                            scheduleVisitIfNotAlreadyVisited(pos.west(), branchAntecedent);
                            scheduleVisitIfNotAlreadyVisited(pos.north(), branchAntecedent);
                            scheduleVisitIfNotAlreadyVisited(pos.south(), branchAntecedent);
                        }

                    }

                    if(queue.isEmpty())
                    {
                        // when done, finalize entries list and submit job
                        this.checked.clear();
                        if(domain != null) domain.getCapability(JobManager.class).addJob(job);
                    }

                    return opCount;
                }

                private void scheduleVisitIfNotAlreadyVisited(BlockPos pos, ExcavationTask task)
                {
                    if(this.checked.contains(pos)) return;
                    this.checked.add(pos);
                    this.queue.addLast(Pair.of(pos, task));
                }

                @Override
                public boolean isDone()
                {
                    // done if no more positions to check
                    return queue.isEmpty();
                }};
        }
        else
        {
            // Placement world task places virtual blocks in the currently active build
            return new IWorldTask()
            {
                /**
                 * Block positions to be checked. 
                 */
                private Iterator<MutableBlockPos> positionIterator = CuboidBuilder.this.region.includedPositions().iterator();

                private World world = player.world;
                
                private Build build = BuildManager.getActiveBuildForPlayer(player);

                {
                    if(build == null)
                    {
                        String chatMessage = I18n.translateToLocal("placement.message.no_build");
                        player.sendMessage(new TextComponentString(chatMessage));
                    }
                }
                
                @Override
                public int runInServerTick(int maxOperations)
                {
                    if(build == null) return 0;
                    
                    int opCount = 0;
                    while(opCount < maxOperations && positionIterator.hasNext() && build.isOpen())
                    {
                        BlockPos pos = positionIterator.next().toImmutable();
                        opCount++;

                        // is the position inside the world?
                        if(world.isOutsideBuildHeight(pos)) continue;

                        IBlockState blockState = world.getBlockState(pos);

                        // is the block at the position affected
                        // by this excavation?
                        if(CuboidBuilder.this.effectiveFilterMode.shouldAffectBlock(
                                blockState, 
                                world, 
                                pos, 
                                CuboidBuilder.this.placedStack(),
                                CuboidBuilder.this.isVirtual))
                        {
                            PlacementHandler.placeVirtualBlock(world, CuboidBuilder.this.outputStack, player, pos, build);
                            opCount += 5;
                        }

                    }
                    return opCount;
                }

                @Override
                public boolean isDone()
                {
                    // done if no active build or more positions to check
                    return build == null || !build.isOpen() || !this.positionIterator.hasNext();
                }};
        }
    }

    @Override
    public IBlockRegion region()
    {
        return this.region;
    }
}