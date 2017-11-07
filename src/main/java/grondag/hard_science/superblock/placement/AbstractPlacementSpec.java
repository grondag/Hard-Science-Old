package grondag.hard_science.superblock.placement;

import java.util.HashSet;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.ClientProxy;
import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.world.BlockRegion;
import grondag.hard_science.library.world.Location;
import grondag.hard_science.library.world.Location.ILocated;
import grondag.hard_science.library.world.WorldHelper;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.placement.PlacementItem.FixedRegionBounds;
import jline.internal.Log;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static grondag.hard_science.superblock.placement.PlacementPreviewRenderMode.*;

public abstract class AbstractPlacementSpec implements ILocated, IReadWriteNBT
{
    protected Location location;

    protected String playerName;
    
    /**
     * Make true if setting all positions to air.
     */
    protected boolean isExcavation;
    
    /**
     * Will be adjusted to a value that makes sense if we are excavating.
     */
    protected FilterMode filterMode;
    
    /**
     * Used for serialization, factory methods
     */
    public abstract PlacementSpecType specType();
    
    public abstract ImmutableList<PlacementSpecEntry> entries();
    
    /**
     * Defines approximate center of the affected blocks and 
     * identifies the dimension in which placement occurs.
     */
    @Override
    public Location getLocation()
    {
        return this.location;
    }

    @Override
    public void setLocation(Location loc)
    {
        this.location = loc;
    }
    
    public String playerName()
    {
        return this.playerName;
    }
    
    public boolean isExcavation()
    {
        return this.isExcavation;
    }
    
    public FilterMode filterMode()
    {
        return this.filterMode;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        this.deserializeLocation(tag);
        this.playerName = tag.getString(ModNBTTag.PLACEMENT_PLAYER_NAME);
        this.isExcavation = tag.getBoolean(ModNBTTag.PLACEMENT_DELETE_ENABLED);
        this.filterMode = FilterMode.FILL_REPLACEABLE.deserializeNBT(tag);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        this.serializeLocation(tag);
        tag.setString(ModNBTTag.PLACEMENT_PLAYER_NAME, this.playerName);
        tag.setBoolean(ModNBTTag.PLACEMENT_DELETE_ENABLED, this.isExcavation);
        this.filterMode.serializeNBT(tag);
    }

    /**
     * Holds individual blocks to be placed.
     */
    public abstract static class PlacementSpecEntry
    {
        /** 0-based position within this spec - must never change because used externally to identify */
        public abstract int index();
        public abstract BlockPos pos();
        
        /** Will be air if is simple excavation */
        public abstract ItemStack placement();
        
        /** Same result as checking placement() stack is air */
        public abstract boolean isExcavation();
    }
    
    protected static abstract class PlacementSpecBuilder implements IPlacementSpecBuilder
    {
        protected final ItemStack placedStack;
        protected final EntityPlayer player;
        protected final PlacementPosition pPos;
        protected Boolean isValid = null;
        protected final TargetMode selectionMode;
        protected final boolean isExcavating;
        protected final boolean isSelectionInProgress;

        /**
         * From stack but adjusted to a value that makes sense if we are excavating.
         */
        protected final FilterMode effectiveFilterMode;

        protected PlacementSpecBuilder(ItemStack placedStack, EntityPlayer player, PlacementPosition pPos)
        {
            this.placedStack = placedStack;
            this.player = player;
            this.pPos = pPos;
            this.isSelectionInProgress = PlacementItem.isFixedRegionSelectionInProgress(placedStack);
            this.selectionMode = PlacementItem.getTargetMode(placedStack);
            this.isExcavating = PlacementItem.isDeleteModeEnabled(placedStack);

            FilterMode filterMode =  PlacementItem.getFilterMode(placedStack);

            // if excavating, adjust filter mode if needed so that it does something
            if(isExcavating && filterMode == FilterMode.FILL_REPLACEABLE) filterMode = FilterMode.REPLACE_SOLID;
            this.effectiveFilterMode = filterMode;
        }

        /**
         * Type-specific logic for {@link #validate(boolean)}.
         * Populate obstacles if applicable.
         * @param isPreview TODO
         * 
         * @return Same semantics as {@link #validate(boolean)}
         */
        protected abstract boolean doValidate(boolean isPreview);

        @Override
        public final boolean validate(boolean isPreview)
        {
            if(isValid == null)
            {
                isValid = doValidate(isPreview);
            }
            return isValid;
        }

        @SideOnly(Side.CLIENT)
        protected abstract void drawSelection(Tessellator tessellator, BufferBuilder bufferBuilder);

        @SideOnly(Side.CLIENT)
        protected abstract void drawPlacement(Tessellator tessellator, BufferBuilder bufferBuilder, PlacementPreviewRenderMode previewMode);

        /**
         * Location used for {@link #drawPlacementPreview(Tessellator, BufferBuilder)}.
         * Override if the placement region does not include target position in {@link #pPos}.
         */
        @SideOnly(Side.CLIENT)
        protected BlockPos previewPos()
        {
            return this.pPos.inPos;
        }
        
        /** draw single-block sample to show shape/orientation of block to be be placed */
        @SideOnly(Side.CLIENT)
        protected void drawPlacementPreview(Tessellator tessellator, BufferBuilder bufferBuilder)
        {
            if(this.previewPos() == null) return;
            
            GlStateManager.disableDepth();
            
            ModelState placementModelState = PlacementItem.getStackModelState(this.placedStack);
            if(placementModelState == null)
            {
                // No model state, draw generic box
                BlockPos pos = this.previewPos();
                bufferBuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
                RenderGlobal.drawBoundingBox(bufferBuilder, pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, OBSTRUCTED.red, OBSTRUCTED.green, OBSTRUCTED.blue, 1f);
                tessellator.draw();
            }
            else
            {
                // Draw collision boxes
                GlStateManager.glLineWidth(1.0F);
                for (AxisAlignedBB blockAABB : placementModelState.collisionBoxes(this.previewPos()))
                {
                    bufferBuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
                    RenderGlobal.drawBoundingBox(bufferBuilder, blockAABB.minX, blockAABB.minY, blockAABB.minZ, blockAABB.maxX, blockAABB.maxY, blockAABB.maxZ, 1f, 1f, 1f, 1f);
                    tessellator.draw();
                }
            }
        }
        
        @SideOnly(Side.CLIENT)
        @Override
        public final void renderPreview(RenderWorldLastEvent event, EntityPlayerSP player)
        {
            this.validate(true);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();

            double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.getPartialTicks();
            double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.getPartialTicks();
            double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.getPartialTicks();

            bufferBuilder.setTranslation(-d0, -d1, -d2);

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            
            // prevent z-fighting
            GlStateManager.enablePolygonOffset();
            GlStateManager.doPolygonOffset(-1, -1);
            
            if(this.isSelectionInProgress)
            {
                this.drawSelection(tessellator, bufferBuilder);
            }
            else if(this.isExcavating)
            {
                this.drawPlacement(tessellator, bufferBuilder, PlacementPreviewRenderMode.EXCAVATE);
            }
            else if(this.isValid)
            {
                this.drawPlacement(tessellator, bufferBuilder, PlacementPreviewRenderMode.PLACE);
            }
            else
            {
                this.drawPlacement(tessellator, bufferBuilder, PlacementPreviewRenderMode.OBSTRUCTED);
            }

            bufferBuilder.setTranslation(0, 0, 0);
            
            GlStateManager.disablePolygonOffset();
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
        }


        /**
         * Builds the type-specific spec, only called if validate is successful.
         */
        protected abstract AbstractPlacementSpec buildSpec();

        @Override
        public final AbstractPlacementSpec build()
        {
            if(!this.validate(false)) return null;

            AbstractPlacementSpec spec = this.buildSpec();

            //TODO: if block is not virtual, should be a single
            // block placement and need to place directly in world
            // without creating job, virtual blocks, etc.
            // perhaps create spec applicator classes to can employ
            // different methods for placing blocks in world (single blocks, creative mode, survival mode)

            return spec;
        }

    }

    /**
     * Parent of placements that place copies of the same item stack
     * at every block position within the placement. Separated
     * from root to allow for future composite placements (blueprints.)
     */
    private static abstract class SingleStackPlacementSpec extends AbstractPlacementSpec
    {
        /**
         * Stack of the block that is to be placed.
         * The stack provided to constructor should ALREADY have correct
         * block rotation and species and other properties that are dependent on 
         * in-world placement context.
         * 
         * Stack does NOT need to be AIR for excavation-only placements.
         * Some placements (CSG) need a model state to define the placement geometry.
         * Excavation-only placements that do not need this will ignore the source stack.
         */
        protected ItemStack sourceStack;
        
        protected ImmutableList<SingleStackEntry> entries;
        
        @Override
        public ImmutableList<PlacementSpecEntry> entries()
        {
            return this.entries();
        }
        
        @Override
        public void deserializeNBT(NBTTagCompound tag)
        {
            super.deserializeNBT(tag);
            this.sourceStack = new ItemStack(tag);
            ImmutableList.Builder<SingleStackEntry> builder = ImmutableList.builder();
            if(tag.hasKey(ModNBTTag.PLACEMENT_ENTRY_DATA))
            {
                int[] entryData = tag.getIntArray(ModNBTTag.PLACEMENT_ENTRY_DATA);
                if(entryData.length % 3 != 0)
                {
                    Log.warn("Detected corrupt data on NBT read of construction specification. Some data may be lost.");
                }
                else
                {
                    int i = 0;
                    int entryIndex = 0;
                    while(i < entryData.length)
                    {
                        BlockPos pos = new BlockPos(entryData[i++], entryData[i++], entryData[i++]);
                        builder.add(new SingleStackEntry(entryIndex++, pos));
                    }
                }
            }
            this.entries = builder.build();
        }

        @Override
        public void serializeNBT(NBTTagCompound tag)
        {
            super.serializeNBT(tag);
            if(this.sourceStack != null) this.sourceStack.writeToNBT(tag);
            if(this.entries != null && !this.entries.isEmpty())
            {
                int i = 0;
                int[] entryData = new int[this.entries.size() * 3];
                for(SingleStackEntry entry : this.entries)
                {
                    entryData[i++] = entry.pos.getX();
                    entryData[i++] = entry.pos.getY();
                    entryData[i++] = entry.pos.getZ();
                }
                tag.setIntArray(ModNBTTag.PLACEMENT_ENTRY_DATA, entryData);
            }
        }

        protected class SingleStackEntry extends PlacementSpecEntry
        {
            protected final int index;
            protected final BlockPos pos;

            protected SingleStackEntry(int index, BlockPos pos)
            {
                this.index = index;
                this.pos = pos;
            }
            
            @Override
            public int index()
            {
                return this.index;
            }

            @Override
            public BlockPos pos()
            {
                return this.pos;
            }

            @Override
            public ItemStack placement()
            {
                return isExcavation ? Items.AIR.getDefaultInstance() : sourceStack;
            }

            @Override
            public boolean isExcavation()
            {
                return isExcavation;
            }
        }
        
        protected static abstract class SingleStackBuilder extends PlacementSpecBuilder
        {
            protected SingleStackBuilder(ItemStack placedStack, EntityPlayer player, PlacementPosition pPos)
            {
                super(placedStack, player, pPos);
            }
        }
    }

    /**
     * Parent of placement types that have a regular geometric shape
     * and can be hollow, have a surface, interior, etc.
     */
    private static abstract class VolumetricPlacementSpec extends SingleStackPlacementSpec
    {
        protected boolean isHollow;

        @Override
        public void deserializeNBT(NBTTagCompound tag)
        {
            super.deserializeNBT(tag);
            this.isHollow = tag.getBoolean(ModNBTTag.PLACMENT_IS_HOLLOW);
        }

        @Override
        public void serializeNBT(NBTTagCompound tag)
        {
            super.serializeNBT(tag);
            tag.setBoolean(ModNBTTag.PLACMENT_IS_HOLLOW, this.isHollow);
        }

        protected abstract static class VolumetricBuilder extends SingleStackBuilder
        {
            protected final boolean isHollow;
            protected final BlockPos offsetPos;
            protected final boolean isFixedRegion;
            protected final boolean isAdjustmentEnabled;

            protected VolumetricBuilder(ItemStack placedStack, EntityPlayer player, PlacementPosition pPos)
            {
                super(placedStack, player, pPos);
                this.isHollow = this.selectionMode == TargetMode.HOLLOW_REGION;
                this.offsetPos = PlacementItem.getRegionSize(placedStack, true);
                this.isFixedRegion = PlacementItem.isFixedRegionEnabled(placedStack);
                this.isAdjustmentEnabled = !this.isFixedRegion 
                        && !this.isExcavating 
                        && !this.isSelectionInProgress
                        && PlacementItem.getRegionOrientation(placedStack) == RegionOrientation.AUTOMATIC;
            }

            /**
             * Clears the exclusion list in the given block region and
             * adds obstacles found within the region to the exclusion list. 
             * 
             * @param region
             * @param isPreview if true, will only check up to 512 block positions 
             * and stops after finding 16 obstacles.  Use this option to limit
             * compute cost when previewing placement. If true, checks are only 
             * performed if the selection mode is <code>COMPLETE_REGION</code>
             * because otherwise the placement cannot be prevented by obstructions.
             */
            protected void excludeObstaclesInRegion(BlockRegion region, boolean isPreview)
            {
                region.clearExclusions();

                if(isPreview && this.selectionMode != TargetMode.COMPLETE_REGION) return;

                HashSet<BlockPos> set = new HashSet<BlockPos>();

                World world = this.player.world;

                int checkCount = 0, foundCount = 0;

                if(this.isExcavating)
                {
                    for(BlockPos.MutableBlockPos pos : region.positions())
                    {
                        if(world.isOutsideBuildHeight(pos))
                        {
                            set.add(pos.toImmutable());
                            if(isPreview && foundCount++ == 16) break;
                        }

                        IBlockState blockState = world.getBlockState(pos);
                        if(blockState.getBlock().isAir(blockState, world, pos) 
                                || !this.effectiveFilterMode.shouldAffectBlock(blockState, world, pos, this.placedStack, this.player))
                        {
                            set.add(pos.toImmutable());
                            if(isPreview && foundCount++ == 16) break;
                        }
                        if(isPreview && checkCount++ == 512) break;
                    }
                }
                else
                {
                    for(BlockPos.MutableBlockPos pos : region.includedPositions())
                    {
                        if(world.isOutsideBuildHeight(pos))
                        {
                            set.add(pos.toImmutable());
                            if(isPreview && foundCount++ == 16) break;
                        }

                        IBlockState blockState = world.getBlockState(pos);
                        if(!this.effectiveFilterMode.shouldAffectBlock(blockState, world, pos, this.placedStack, this.player))
                        {
                            set.add(pos.toImmutable());
                            if(isPreview && foundCount++ == 16) break;
                        }
                        if(isPreview && checkCount++ == 512) break;
                    }
                }
                region.exclude(set);
            }

            /**
             * Returns true if the region has no obstacles or
             * obstacles do not matter. Must call AFTER {@link #excludeObstaclesInRegion(BlockRegion, boolean)}
             * or result will be meaningless. 
             */
            protected boolean canPlaceRegion(BlockRegion region)
            {
                return region.exclusions().isEmpty() || this.selectionMode != TargetMode.COMPLETE_REGION;
            }
        }
    }

    /**
     * Places a single block.
     */
    public static class SinglePlacementSpec extends SingleStackPlacementSpec
    {
        protected static class SingleBuilder extends SingleStackBuilder
        {

            protected SingleBuilder(ItemStack placedStack, EntityPlayer player, PlacementPosition pPos)
            {
                super(placedStack, player, pPos);
            }

            @Override
            protected AbstractPlacementSpec buildSpec()
            {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            protected boolean doValidate(boolean isPreview)
            {
                if(this.player.world.isOutsideBuildHeight(this.pPos.inPos)) return false;

                if(this.isExcavating)
                {
                    return !this.player.world.isAirBlock(this.pPos.inPos);
                }
                else
                {
                    return WorldHelper.isBlockReplaceable(this.player.world, this.pPos.inPos, false);
                }
            }

            @SideOnly(Side.CLIENT)
            @Override
            protected void drawSelection(Tessellator tessellator, BufferBuilder bufferBuilder)
            {
                // NOOP - selection mode not meaningful for a single-block region
            }
            
            @SideOnly(Side.CLIENT)
            @Override
            protected void drawPlacement(Tessellator tessellator, BufferBuilder bufferBuilder, PlacementPreviewRenderMode previewMode)
            {
                this.drawPlacementPreview(tessellator, bufferBuilder);
            }
        }

        public static IPlacementSpecBuilder builder(ItemStack placedStack, EntityPlayer player, PlacementPosition pPos)
        {
            return new SingleBuilder(placedStack, player, pPos);
        }

        @Override
        public PlacementSpecType specType()
        {
            return PlacementSpecType.SINGLE;
        }
    }

    /**
     * Multi-block placement of the same block in a cuboid region
     */
    public static class CuboidPlacementSpec extends VolumetricPlacementSpec
    {
        protected static class CuboidBuilder extends VolumetricBuilder
        {

            protected BlockRegion region;
            
            /**
             * Where should block shape preview sample be drawn?
             */
            protected BlockPos previewPos;

            protected CuboidBuilder(ItemStack placedStack, EntityPlayer player, PlacementPosition pPos)
            {
                super(placedStack, player, pPos);
                this.previewPos = pPos.inPos;
            }

            @Override
            protected AbstractPlacementSpec buildSpec()
            {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            protected boolean doValidate(boolean isPreview)
            {
                if(this.isSelectionInProgress) 
                {
                    this.region = new BlockRegion(pPos.inPos, PlacementItem.fixedRegionSelectionPos(this.placedStack).first(), false);
                    return true;
                }
                
                // pass null face into relative offset when using floating selection
                // to avoid re-orientation based on hit face
                EnumFacing offsetFace = pPos.isFloating ? null : pPos.onFace;
                
                if(this.isFixedRegion)
                {
                    FixedRegionBounds bounds = PlacementItem.getFixedRegion(placedStack);
                    this.region = new BlockRegion(bounds.fromPos, bounds.toPos, this.isHollow);
                    this.excludeObstaclesInRegion(this.region, isPreview);
                    
                    // to place a fixed region, player must be targeting a space within it
                    if(this.region.contains(pPos.inPos))
                    {
                        return this.canPlaceRegion(region);
                    }
                    else
                    {
                        // prevent block preview in nonsensical location
                        this.previewPos = null;
                        return false;
                    }
                }
                else
                {
                    BlockPos endPos = PlacementHandler.getPlayerRelativeOffset(this.pPos.inPos, this.offsetPos, this.player, offsetFace, OffsetPosition.FLIP_NONE);
                    BlockRegion region = new BlockRegion(pPos.inPos, endPos, this.isHollow);
                
                    this.excludeObstaclesInRegion(region, isPreview);
                    boolean isClear = this.canPlaceRegion(region);
                    
                    // section will not happen for fixed regions
                    // because adjustement will be disabled
                    if(this.isAdjustmentEnabled && !isClear)
                    {
                        //try to adjust
    
                        for(OffsetPosition offset : OffsetPosition.ALTERNATES)
                        {
                            // first try pivoting the selection box around the position being targeted
                            BlockPos endPos2 = PlacementHandler.getPlayerRelativeOffset(this.pPos.inPos, this.offsetPos, this.player, offsetFace, offset);
                            BlockRegion region2 = new BlockRegion(pPos.inPos, endPos2, this.isHollow);
                            this.excludeObstaclesInRegion(region2, isPreview);
    
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
                                BlockRegion region2 = new BlockRegion(startPos2, endPos2, isHollow);
                                this.excludeObstaclesInRegion(region2, isPreview);
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
                
                // draw edge without depth to show extent of region
                GlStateManager.disableDepth();
                GlStateManager.glLineWidth(2.0F);
                bufferBuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
                RenderGlobal.drawBoundingBox(bufferBuilder, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, previewMode.red, previewMode.green, previewMode.blue, 1f);
                tessellator.draw();

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
        }
        
        public static IPlacementSpecBuilder builder(ItemStack placedStack, EntityPlayer player, PlacementPosition pPos)
        {
            return new CuboidBuilder(placedStack, player, pPos);
        }

        @Override
        public PlacementSpecType specType()
        {
            return PlacementSpecType.CUBOID;
        }
    }

    /**
     * Builder's wand type of placement
     */
    public static class SurfacePlacementSpec extends SingleStackPlacementSpec
    {
        protected static class SurfaceBuilder extends SingleStackBuilder
        {

            protected SurfaceBuilder(ItemStack placedStack, EntityPlayer player, PlacementPosition pPos)
            {
                super(placedStack, player, pPos);
            }

            @Override
            protected AbstractPlacementSpec buildSpec()
            {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            protected boolean doValidate(boolean isPreview)
            {
                // excavation doesn't make sense with this mode
                if(this.isExcavating) return false;

                if(this.player.world.isOutsideBuildHeight(this.pPos.inPos)) return false;

                return WorldHelper.isBlockReplaceable(this.player.world, this.pPos.inPos, false);
            }

            @SideOnly(Side.CLIENT)
            @Override
            protected void drawSelection(Tessellator tessellator, BufferBuilder bufferBuilder)
            {
                // TODO Auto-generated method stub

            }

            @SideOnly(Side.CLIENT)
            @Override
            protected void drawPlacement(Tessellator tessellator, BufferBuilder bufferBuilder, PlacementPreviewRenderMode previewMode)
            {
                // TODO Auto-generated method stub

            }

        }

        public static IPlacementSpecBuilder builder(ItemStack placedStack, EntityPlayer player, PlacementPosition pPos)
        {
            return new SurfaceBuilder(placedStack, player, pPos);
        }

        @Override
        public PlacementSpecType specType()
        {
            return PlacementSpecType.SURFACE;
        }
    }

    /**
     * Placement that defines target region based on blocks that match a given predicate.
     */
    public static class PredicatePlacementSpec extends SingleStackPlacementSpec
    {
        protected static class PredicateBuilder extends SingleStackBuilder
        {

            protected PredicateBuilder(ItemStack placedStack, EntityPlayer player, PlacementPosition pPos)
            {
                super(placedStack, player, pPos);
            }

            @Override
            protected AbstractPlacementSpec buildSpec()
            {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            protected boolean doValidate(boolean isPreview)
            {
                // can't replace air, water, weeds, etc.
                return !WorldHelper.isBlockReplaceable(this.player.world, this.pPos.onPos, false);
            }

            @SideOnly(Side.CLIENT)
            @Override
            protected void drawSelection(Tessellator tessellator, BufferBuilder bufferBuilder)
            {
                // TODO Auto-generated method stub

            }

            @SideOnly(Side.CLIENT)
            @Override
            protected void drawPlacement(Tessellator tessellator, BufferBuilder bufferBuilder, PlacementPreviewRenderMode previewMode)
            {
                // TODO Auto-generated method stub

            }

        }

        public static IPlacementSpecBuilder builder(ItemStack placedStack, EntityPlayer player, PlacementPosition pPos)
        {
            return new PredicateBuilder(placedStack, player, pPos);
        }

        @Override
        public PlacementSpecType specType()
        {
            return PlacementSpecType.PREDICATE;
        }
    }

    /**
     * Surface, but adds to height of existing block surfaces 
     * that may not be aligned with block boundaries.
     */
    public static class AdditivePlacementSpec extends SurfacePlacementSpec
    {
        @Override
        public PlacementSpecType specType()
        {
            return PlacementSpecType.ADDITIVE;
        }
        
        protected static class AdditiveBuilder extends SingleStackBuilder
        {
            protected AdditiveBuilder(ItemStack placedStack, EntityPlayer player, PlacementPosition pPos)
            {
                super(placedStack, player, pPos);
            }

            @Override
            protected AbstractPlacementSpec buildSpec()
            {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            protected boolean doValidate(boolean isPreview)
            {
                // TODO Auto-generated method stub
                return false;
            }

            @SideOnly(Side.CLIENT)
            @Override
            protected void drawSelection(Tessellator tessellator, BufferBuilder bufferBuilder)
            {
                // TODO Auto-generated method stub

            }

            @SideOnly(Side.CLIENT)
            @Override
            protected void drawPlacement(Tessellator tessellator, BufferBuilder bufferBuilder, PlacementPreviewRenderMode previewMode)
            {
                // TODO Auto-generated method stub

            }

        }

        public static IPlacementSpecBuilder builder(ItemStack placedStack, EntityPlayer player, PlacementPosition pPos)
        {
            return new AdditiveBuilder(placedStack, player, pPos);
        }
    }

    /** placeholder class for CSG multiblock placements */
    public static class CSGPlacementSpec extends VolumetricPlacementSpec
    {
        protected static class CSGBuilder extends VolumetricBuilder
        {
            protected CSGBuilder(ItemStack placedStack, EntityPlayer player, PlacementPosition pPos)
            {
                super(placedStack, player, pPos);
            }

            @Override
            protected AbstractPlacementSpec buildSpec()
            {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            protected boolean doValidate(boolean isPreview)
            {
                // TODO: Logic will be similar to VolumetricBuilder
                return false;
            }

            @SideOnly(Side.CLIENT)
            @Override
            protected void drawSelection(Tessellator tessellator, BufferBuilder bufferBuilder)
            {
                // TODO Auto-generated method stub

            }

            @SideOnly(Side.CLIENT)
            @Override
            protected void drawPlacement(Tessellator tessellator, BufferBuilder bufferBuilder, PlacementPreviewRenderMode previewMode)
            {
                // TODO Auto-generated method stub

            }
        }
        public static IPlacementSpecBuilder builder(ItemStack placedStack, EntityPlayer player, PlacementPosition pPos)
        {
            return new CSGBuilder(placedStack, player, pPos);
        }
        @Override
        public PlacementSpecType specType()
        {
            return PlacementSpecType.CSG;
        }
    }


}
