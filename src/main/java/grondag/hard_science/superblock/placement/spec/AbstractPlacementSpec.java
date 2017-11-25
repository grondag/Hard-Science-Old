package grondag.hard_science.superblock.placement.spec;

import static grondag.hard_science.superblock.placement.PlacementPreviewRenderMode.OBSTRUCTED;
import static grondag.hard_science.superblock.placement.PlacementPreviewRenderMode.PLACE;
import static grondag.hard_science.superblock.placement.PlacementPreviewRenderMode.SELECT;

import java.util.ArrayDeque;
import java.util.HashSet;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.ClientProxy;
import grondag.hard_science.library.render.RenderUtil;
import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.world.BlockRegion;
import grondag.hard_science.library.world.Location;
import grondag.hard_science.library.world.Location.ILocated;
import grondag.hard_science.library.world.WorldHelper;
import grondag.hard_science.simulator.base.DomainManager;
import grondag.hard_science.simulator.base.DomainManager.Domain;
import grondag.hard_science.simulator.base.IIdentified;
import grondag.hard_science.simulator.base.jobs.AbstractTask;
import grondag.hard_science.simulator.base.jobs.IWorldTask;
import grondag.hard_science.simulator.base.jobs.Job;
import grondag.hard_science.simulator.base.jobs.RequestPriority;
import grondag.hard_science.simulator.base.jobs.tasks.ExcavationTask;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.placement.FilterMode;
import grondag.hard_science.superblock.placement.FixedRegionBounds;
import grondag.hard_science.superblock.placement.IPlacementSpecBuilder;
import grondag.hard_science.superblock.placement.OffsetPosition;
import grondag.hard_science.superblock.placement.PlacementHandler;
import grondag.hard_science.superblock.placement.PlacementItem;
import grondag.hard_science.superblock.placement.PlacementPosition;
import grondag.hard_science.superblock.placement.PlacementPreviewRenderMode;
import grondag.hard_science.superblock.placement.PlacementSpecType;
import grondag.hard_science.superblock.placement.RegionOrientation;
import grondag.hard_science.superblock.placement.TargetMode;
import jline.internal.Log;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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

public abstract class AbstractPlacementSpec implements ILocated, IReadWriteNBT
{
    /**
     * @see {@link #getLocation()}
     */
    protected Location location;

    protected String playerName;
    
    /**
     * Make true if setting all positions to air.
     */
    protected boolean isExcavation;
    
    /**
     * True if only places/removes virtual blocks. 
     * Derived from the placement item that created this spec.
     */
    protected boolean isVirtual;
    
    /**
     * Will be adjusted to a value that makes sense if we are excavating.
     */
    protected FilterMode filterMode;
    
    protected AbstractPlacementSpec() {};
            
    protected AbstractPlacementSpec(PlacementSpecBuilder builder)
    {
        this.isExcavation = builder.isExcavation;
        this.isVirtual = builder.isVirtual;
        this.filterMode = builder.effectiveFilterMode;
    }
    
    /**
     * Used for serialization, factory methods
     */
    public abstract PlacementSpecType specType();
    
    /**
     * List of excavations and block placements in this spec.
     * Should only be called after {@link #worldTask(EntityPlayer)} has run.
     * Entries are in order they should be executed.
     * Excavations and placements for same position must be 
     * different entries.
     */
    public abstract ImmutableList<PlacementSpecEntry> entries();
    
    /**
     * Position where the user clicked to activate the placement.
     * Also identifies the dimension in which placement occurs.
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
        this.isVirtual = tag.getBoolean(ModNBTTag.PLACEMENT_IS_VIRTUAL);
        this.filterMode = FilterMode.FILL_REPLACEABLE.deserializeNBT(tag);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        this.serializeLocation(tag);
        tag.setString(ModNBTTag.PLACEMENT_PLAYER_NAME, this.playerName);
        tag.setBoolean(ModNBTTag.PLACEMENT_DELETE_ENABLED, this.isExcavation);
        tag.setBoolean(ModNBTTag.PLACEMENT_IS_VIRTUAL, this.isVirtual);
        this.filterMode.serializeNBT(tag);
    }

    /**
     * Holds individual blocks to be placed.
     */
    public abstract static class PlacementSpecEntry
    {
        private int index;
        private BlockPos pos;

        /**
         * ID of associated excavation task.
         * Will be unassigned if no task created.
         */
        public int excavationTaskID = IIdentified.UNASSIGNED_ID;
        
        /**
         * ID of associated placement task.
         * Will be unassigned if no task created.
         */
        public int placementTaskID = IIdentified.UNASSIGNED_ID;
        
        /**
         * ID of associated procurement task - set by build planning task.
         * Fabrication task (if applies) can be obtained from the procurement task.
         * Will be unassigned if no task created.
         */
        public int procurementTaskID = IIdentified.UNASSIGNED_ID;
        
        protected PlacementSpecEntry(int index, BlockPos pos)
        {
            this.index = index;
            this.pos = pos;
        }
      
        /** 0-based position within this spec - must never change because used externally to identify */
        public int index()
        {
            return this.index;
        }

        public BlockPos pos()
        {
            return this.pos;
        }
        
        /** Will be air if is excavation */
        public abstract ItemStack placement();
        
        public abstract PlacementItem placementItem();
        
        /** Same result as checking placement() stack is air */
        public abstract boolean isExcavation();
        
        /** 
         * Block state that should be placed.
         */
        public IBlockState blockState()
        {
            return placementItem().getPlacementBlockStateFromStack(this.placement());
        }

    }
    
    protected static abstract class PlacementSpecBuilder implements IPlacementSpecBuilder
    {
        private final ItemStack placedStack;
        protected final PlacementItem placementItem;
        protected final EntityPlayer player;
        protected final PlacementPosition pPos;
        protected Boolean isValid = null;
        protected final TargetMode selectionMode;
        protected final boolean isExcavation;
        protected final boolean isVirtual;
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
            this.placementItem = (PlacementItem)placedStack.getItem();
            this.isSelectionInProgress = this.placementItem.isFixedRegionSelectionInProgress(placedStack);
            this.selectionMode = this.placementItem.getTargetMode(placedStack);
            this.isExcavation = this.placementItem.isExcavator(placedStack);
            this.isVirtual = this.placementItem.isVirtual(placedStack);
            
            FilterMode filterMode =  this.placementItem.getFilterMode(placedStack);

            // if excavating, adjust filter mode if needed so that it does something
            if(isExcavation && filterMode == FilterMode.FILL_REPLACEABLE) filterMode = FilterMode.REPLACE_SOLID;
            this.effectiveFilterMode = filterMode;
        }
        
        /**
         * Type-specific logic for {@link #validate()}.
         * Populate obstacles if applicable.
         * 
         * @return Same semantics as {@link #validate()}
         */
        protected abstract boolean doValidate();

        @Override
        public final boolean validate()
        {
            if(isValid == null)
            {
                isValid = doValidate();
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
         * Will generally not be used for excavations.
         */
        @SideOnly(Side.CLIENT)
        protected BlockPos previewPos()
        {
            return this.pPos.inPos;
        }
        
        public ItemStack placedStack()
        {
            return placedStack;
        }
        
        public PlacementPosition placementPosition()
        {
            return this.pPos;
        }
        
        public EntityPlayer player()
        {
            return this.player;
        }
        
        /** 
         * Draw single-block sample to show shape/orientation of block to be be placed.
         * Does not render for excavations.
         */
        @SideOnly(Side.CLIENT)
        protected void drawPlacementPreview(Tessellator tessellator, BufferBuilder bufferBuilder)
        {
            if(this.previewPos() == null || this.isExcavation) return;
            
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
            this.validate();

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
            else if(this.isExcavation)
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
            if(!this.validate()) return null;

            AbstractPlacementSpec spec = this.buildSpec();
            spec.location = new Location(this.pPos.inPos, this.player.world);
            return spec;
        }
    }

    /**
     * Parent of placements that place copies of the same item stack
     * at every block position within the placement. Separated
     * from root to allow for future composite placements (blueprints.)
     */
    public static abstract class SingleStackPlacementSpec extends AbstractPlacementSpec
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
        private ItemStack sourceStack;
        
        private PlacementItem placementItem;
        
        protected ImmutableList<PlacementSpecEntry> entries;
        
        protected SingleStackPlacementSpec() {};

        /** 
         * Source stack should be already be modified for in-world placement context.
         */
        protected SingleStackPlacementSpec(PlacementSpecBuilder builder, ItemStack sourceStack)
        {
            super(builder);
            this.sourceStack = sourceStack;
            this.placementItem = builder.placementItem;
        }
        
        @Override
        public ImmutableList<PlacementSpecEntry> entries()
        {
            return this.entries;
        }
        
        /**
         * Stack is modified for placement context.
         */
        public ItemStack sourceStack()
        {
            return this.sourceStack;
        }
        
        @Override
        public void deserializeNBT(NBTTagCompound tag)
        {
            super.deserializeNBT(tag);
            this.sourceStack = new ItemStack(tag);
            this.placementItem = (PlacementItem)sourceStack.getItem();
            
            ImmutableList.Builder<PlacementSpecEntry> builder = ImmutableList.builder();
            if(tag.hasKey(ModNBTTag.PLACEMENT_ENTRY_DATA))
            {
                int[] entryData = tag.getIntArray(ModNBTTag.PLACEMENT_ENTRY_DATA);
                if(entryData.length % 6 != 0)
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
                        SingleStackEntry entry = new SingleStackEntry(entryIndex++, pos);
                        entry.excavationTaskID = entryData[i++];
                        entry.placementTaskID = entryData[i++];
                        entry.procurementTaskID = entryData[i++];
                        builder.add(entry);
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
                int[] entryData = new int[this.entries.size() * 6];
                for(PlacementSpecEntry entry : this.entries)
                {
                    entryData[i++] = entry.pos().getX();
                    entryData[i++] = entry.pos().getY();
                    entryData[i++] = entry.pos().getZ();
                    entryData[i++] = entry.excavationTaskID;
                    entryData[i++] = entry.placementTaskID;
                    entryData[i++] = entry.procurementTaskID;
                }
                tag.setIntArray(ModNBTTag.PLACEMENT_ENTRY_DATA, entryData);
            }
        }

        public class SingleStackEntry extends PlacementSpecEntry
        {
            protected SingleStackEntry(int index, BlockPos pos)
            {
                super(index, pos);
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

            @Override
            public PlacementItem placementItem()
            {
                // TODO Auto-generated method stub
                return null;
            }
        }
        
        public static abstract class SingleStackBuilder extends PlacementSpecBuilder
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

        protected VolumetricPlacementSpec() {};
        
        protected VolumetricPlacementSpec(PlacementSpecBuilder builder, ItemStack sourceStack)
        {
            super(builder, sourceStack);
        }
        
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
                this.offsetPos = this.placementItem.getRegionSize(placedStack, true);
                this.isFixedRegion = this.placementItem.isFixedRegionEnabled(placedStack);
                this.isAdjustmentEnabled = !this.isFixedRegion 
                        && !this.isExcavation 
                        && !this.isSelectionInProgress
                        && this.placementItem.getRegionOrientation(placedStack) == RegionOrientation.AUTOMATIC;
            }

            /**
             * Clears the exclusion list in the given block region and
             * adds obstacles checked within the region to the exclusion list. 
             * Does not fully validate region - is intended for preview use only.
             * <p>
             * Will only check up to 512 block positions and stops after finding 16 obstacles.  
             * Checks are only  performed if the selection mode is <code>COMPLETE_REGION</code>
             * because otherwise the placement cannot be prevented by obstructions.
             * 
             * @param region
             */
            protected void excludeObstaclesInRegion(BlockRegion region)
            {
                region.clearExclusions();

                if(this.selectionMode != TargetMode.COMPLETE_REGION) return;

                HashSet<BlockPos> set = new HashSet<BlockPos>();

                World world = this.player.world;

                int checkCount = 0, foundCount = 0;

                if(this.isExcavation)
                {
                    for(BlockPos.MutableBlockPos pos : region.positions())
                    {
                        if(world.isOutsideBuildHeight(pos))
                        {
                            set.add(pos.toImmutable());
                            if(foundCount++ == 16) break;
                        }

                        IBlockState blockState = world.getBlockState(pos);
                        if(blockState.getBlock().isAir(blockState, world, pos) 
                                || !this.effectiveFilterMode.shouldAffectBlock(blockState, world, pos, this.placedStack(), this.isVirtual))
                        {
                            set.add(pos.toImmutable());
                            if(foundCount++ == 16) break;
                        }
                        if(checkCount++ == 512) break;
                    }
                }
                else
                {
                    for(BlockPos.MutableBlockPos pos : region.includedPositions())
                    {
                        if(world.isOutsideBuildHeight(pos))
                        {
                            set.add(pos.toImmutable());
                            if(foundCount++ == 16) break;
                        }

                        IBlockState blockState = world.getBlockState(pos);
                        if(!this.effectiveFilterMode.shouldAffectBlock(blockState, world, pos, this.placedStack(), this.isVirtual))
                        {
                            set.add(pos.toImmutable());
                            if(foundCount++ == 16) break;
                        }
                        if(checkCount++ == 512) break;
                    }
                }
                region.exclude(set);
            }

            /**
             * Returns true if the region has no obstacles or
             * obstacles do not matter. Must call AFTER {@link #excludeObstaclesInRegion(BlockRegion)}
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
        public SinglePlacementSpec() {};
        
        protected SinglePlacementSpec(PlacementSpecBuilder builder, ItemStack sourceStack)
        {
            super(builder, sourceStack);
        }
        
        protected static class SingleBuilder extends SingleStackBuilder
        {

            protected SingleBuilder(ItemStack placedStack, EntityPlayer player, PlacementPosition pPos)
            {
                super(placedStack, player, pPos);
            }

            @Override
            protected AbstractPlacementSpec buildSpec()
            {
                SinglePlacementSpec result = new SinglePlacementSpec(this, PlacementHandler.cubicPlacementStack(this));
                result.playerName = this.player.getName();
                
                SingleStackEntry entry 
                    = result.new SingleStackEntry(0, this.pPos.inPos);
                result.entries = ImmutableList.of(entry);
                return result;
            }

            @Override
            protected boolean doValidate()
            {
                if(this.player.world.isOutsideBuildHeight(this.pPos.inPos)) return false;

                if(this.isExcavation)
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

            public IWorldTask worldTask(EntityPlayerMP player)
            {
                if(this.isExcavation)
                {
                    return new IWorldTask()
                    {
                        private boolean isDone = false;
                        
                        @Override
                        public int runInServerTick(int maxOperations)
                        {
                            SinglePlacementSpec spec = (SinglePlacementSpec) buildSpec();
                            this.isDone = true;
                            
                            World world = spec.location.world();
                            
                            if(spec.entries.isEmpty()) return 1;
                            
                            BlockPos pos = spec.entries.get(0).pos();
                            if(pos == null) return 1;
                                
                            // is the position inside the world?
                            if(world.isOutsideBuildHeight(pos)) return 1;
                                
                            IBlockState blockState = world.getBlockState(pos);
                                
                            // is the block at the position affected
                            // by this excavation?
                            if(spec.filterMode().shouldAffectBlock(
                                    blockState, 
                                    world, 
                                    pos, 
                                    spec.sourceStack(),
                                    spec.isVirtual))
                            {
                            
                                Job job = new Job(RequestPriority.MEDIUM, player, spec);
                                job.addTask(new ExcavationTask(spec.entries.get(0)));
                                Domain domain = DomainManager.INSTANCE.getActiveDomain(player);
                                if(domain != null)
                                {
                                    domain.JOB_MANAGER.addJob(job);
                                }
                            }
                            return 2;
                        }

                        @Override
                        public boolean isDone()
                        {
                            return this.isDone;
                        }
                    };
                }
                else
                {
                    // Placement world task places virtual blocks in the currently active build
                    return new IWorldTask()
                    {
                        private boolean isDone = false;
                        
                        @Override
                        public int runInServerTick(int maxOperations)
                        {
                            SinglePlacementSpec spec = (SinglePlacementSpec) buildSpec();
                            
                            this.isDone = true;
                            
                            World world = spec.location.world();
                            
                            if(spec.entries.isEmpty()) return 1;
                            
                            BlockPos pos = spec.entries.get(0).pos();
                            if(pos == null) return 1;
                                
                            // is the position inside the world?
                            if(world.isOutsideBuildHeight(pos)) return 1;
                                
                            IBlockState blockState = world.getBlockState(pos);
                                
                            // is the block at the position affected
                            // by this excavation?
                            if(spec.filterMode().shouldAffectBlock(
                                    blockState, 
                                    world, 
                                    pos, 
                                    spec.sourceStack(),
                                    spec.isVirtual))
                            {
                                //TODO: set virtual block build/domain
//                                Domain domain = DomainManager.INSTANCE.getActiveDomain(player);
                                PlacementHandler.placeVirtualBlock(world, spec.sourceStack(), player, pos);
                                return 5;
                            }
                            return 3;
                        }

                        @Override
                        public boolean isDone()
                        {
                            return this.isDone;
                        }
                    };
                }
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
        private BlockRegion region;
        
        public CuboidPlacementSpec() {};
        
        protected CuboidPlacementSpec(PlacementSpecBuilder builder, ItemStack sourceStack)
        {
            super(builder, sourceStack);
        }
        
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
                CuboidPlacementSpec result = new CuboidPlacementSpec(this, PlacementHandler.cubicPlacementStack(this));
                result.playerName = this.player.getName();
                result.isHollow = this.isHollow;
                result.region = this.region;
                return result;
            }

            @Override
            protected boolean doValidate()
            {
                if(this.isSelectionInProgress) 
                {
                    this.region = new BlockRegion(pPos.inPos, this.placementItem.fixedRegionSelectionPos(this.placedStack()).first(), false);
                    return true;
                }
                
                if(this.isFixedRegion)
                {
                    FixedRegionBounds bounds = this.placementItem.getFixedRegion(this.placedStack());
                    this.region = new BlockRegion(bounds.fromPos, bounds.toPos, this.isHollow);
                    this.excludeObstaclesInRegion(this.region);
                    
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
                    
                    this.region = new BlockRegion(startPos, endPos, false);
                    return true;
                    
                }
                
                else
                {
                    // pass null face into relative offset when using floating selection
                    // to avoid re-orientation based on hit face
                    EnumFacing offsetFace = pPos.isFloating ? null : pPos.onFace;
                    
                    BlockPos endPos = PlacementHandler.getPlayerRelativeOffset(this.pPos.inPos, this.offsetPos, this.player, offsetFace, OffsetPosition.FLIP_NONE);
                    BlockRegion region = new BlockRegion(pPos.inPos, endPos, this.isHollow);
                
                    this.excludeObstaclesInRegion(region);
                    boolean isClear = this.canPlaceRegion(region);
                    
                    if(this.isAdjustmentEnabled && !isClear)
                    {
                        //try to adjust
    
                        for(OffsetPosition offset : OffsetPosition.ALTERNATES)
                        {
                            // first try pivoting the selection box around the position being targeted
                            BlockPos endPos2 = PlacementHandler.getPlayerRelativeOffset(this.pPos.inPos, this.offsetPos, this.player, offsetFace, offset);
                            BlockRegion region2 = new BlockRegion(pPos.inPos, endPos2, this.isHollow);
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
                                BlockRegion region2 = new BlockRegion(startPos2, endPos2, isHollow);
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
                        private CuboidPlacementSpec spec = (CuboidPlacementSpec) buildSpec();
                        private Job job = new Job(RequestPriority.MEDIUM, player, spec);
                        Domain domain = DomainManager.INSTANCE.getActiveDomain(player);
                        
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
                        
                        /**
                         * Block positions that should be included in placement.
                         */
                        ImmutableList.Builder<PlacementSpecEntry> builder = ImmutableList.builder();
                        
                        World world = spec.location.world();
                        
                        int index = 0;
                        
                        {
                            scheduleVisitIfNotAlreadyVisited(spec.location, null);
                            
//                            Log.info("Starting excavation from " + location.toString());
                        }
                        
                        @Override
                        public int runInServerTick(int maxOperations)
                        {
//                            Log.info("Starting run, checked contains %d and queue contains %d", checked.size(), queue.size());
                            
                            if(queue.isEmpty()) return 0;
                            
                            int opCount = 0;
                            while(opCount < maxOperations)
                            {
                                Pair<BlockPos, ExcavationTask> visit = queue.poll();
                                if(visit == null) break;
                                
                                BlockPos pos = visit.getLeft();
                                
//                                Log.info("Checking position " + pos.toString());
                                
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
                                if(spec.filterMode().shouldAffectBlock(
                                        blockState, 
                                        world, 
                                        pos, 
                                        spec.sourceStack(),
                                        isVirtual))
                                {
                                    SingleStackEntry entry = spec.new SingleStackEntry(index++, pos);
                                    builder.add(entry);
                                    branchAntecedent = new ExcavationTask(entry);
                                    job.addTask(branchAntecedent);
                                    if(visit.getRight() != null)
                                    {
                                        AbstractTask.link(visit.getRight(), branchAntecedent);
                                    }
                                    canPassThrough = true;
                                    
//                                    Log.info("Added position " + pos.toString());
                                }
                                
                                // even if we can't excavate the block, 
                                // can we move through it to check others?
                                canPassThrough = canPassThrough || !blockState.getMaterial().blocksMovement();
                                
                                // check adjacent blocks if are or will
                                // be accessible and haven't already been
                                // checked.
                                if(canPassThrough)
                                {
//                                    Log.info("Branching from position " + pos.toString());
                                    
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
                                spec.entries = builder.build();
                                this.checked.clear();
                                
//                                Log.info("Finalizing job");
                                
                                if(domain != null) domain.JOB_MANAGER.addJob(job);
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
                    
                    
                    //TODO
                    return null;
                }
            }
        }
        
        @Override
        public void deserializeNBT(NBTTagCompound tag)
        {
            super.deserializeNBT(tag);
            this.isHollow= tag.getBoolean(ModNBTTag.PLACMENT_IS_HOLLOW);
            BlockPos minPos = BlockPos.fromLong(tag.getLong(ModNBTTag.PLACEMENT_FIXED_REGION_START_POS));
            BlockPos maxPos = BlockPos.fromLong(tag.getLong(ModNBTTag.PLACEMENT_FIXED_REGION_END_POS));
            this.region = new BlockRegion(minPos, maxPos, this.isHollow);
        }

        @Override
        public void serializeNBT(NBTTagCompound tag)
        {
            super.serializeNBT(tag);
            tag.setBoolean(ModNBTTag.PLACMENT_IS_HOLLOW, this.isHollow);
            tag.setLong(ModNBTTag.PLACEMENT_FIXED_REGION_START_POS, this.region.minPos().toLong());
            tag.setLong(ModNBTTag.PLACEMENT_FIXED_REGION_END_POS, this.region.maxPos().toLong());
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
        public SurfacePlacementSpec() {};
        
        protected SurfacePlacementSpec(PlacementSpecBuilder builder, ItemStack sourceStack)
        {
            super(builder, sourceStack);
        }
        
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
            protected boolean doValidate()
            {
                // excavation doesn't make sense with this mode
                if(this.isExcavation) return false;

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

            @Override
            public IWorldTask worldTask(EntityPlayerMP player)
            {
                // TODO Auto-generated method stub
                return null;
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
        public PredicatePlacementSpec() {};
        
        protected PredicatePlacementSpec(PlacementSpecBuilder builder, ItemStack sourceStack)
        {
            super(builder, sourceStack);
        }
        
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
            protected boolean doValidate()
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
            
            @Override
            public IWorldTask worldTask(EntityPlayerMP player)
            {
                // TODO Auto-generated method stub
                return null;
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
        public AdditivePlacementSpec() {};
        
        protected AdditivePlacementSpec(PlacementSpecBuilder builder, ItemStack sourceStack)
        {
            super(builder, sourceStack);
        }
        
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
            protected boolean doValidate()
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

            @Override
            public IWorldTask worldTask(EntityPlayerMP player)
            {
                // TODO Auto-generated method stub
                return null;
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
        public CSGPlacementSpec() {};
        
        protected CSGPlacementSpec(PlacementSpecBuilder builder, ItemStack sourceStack)
        {
            super(builder, sourceStack);
        }
        
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
            protected boolean doValidate()
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
            
            @Override
            public IWorldTask worldTask(EntityPlayerMP player)
            {
                // TODO Auto-generated method stub
                return null;
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
