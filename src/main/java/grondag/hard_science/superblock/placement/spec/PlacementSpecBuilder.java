package grondag.hard_science.superblock.placement.spec;

import static grondag.hard_science.superblock.placement.PlacementPreviewRenderMode.OBSTRUCTED;

import org.lwjgl.opengl.GL11;

import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.placement.FilterMode;
import grondag.hard_science.superblock.placement.IPlacementSpecBuilder;
import grondag.hard_science.superblock.placement.PlacementItem;
import grondag.hard_science.superblock.placement.PlacementPosition;
import grondag.hard_science.superblock.placement.PlacementPreviewRenderMode;
import grondag.hard_science.superblock.placement.TargetMode;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

abstract class PlacementSpecBuilder implements IPlacementSpecBuilder
{
    /**
     * Stack player is holding to do the placement.
     */
    private final ItemStack heldStack;
    
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

    protected PlacementSpecBuilder(ItemStack heldStack, EntityPlayer player, PlacementPosition pPos)
    {
        this.heldStack = heldStack;
        this.player = player;
        this.pPos = pPos;
        this.placementItem = (PlacementItem)heldStack.getItem();
        this.isSelectionInProgress = this.placementItem.isFixedRegionSelectionInProgress(heldStack);
        this.selectionMode = this.placementItem.getTargetMode(heldStack);
        this.isExcavation = this.placementItem.isExcavator(heldStack);
        this.isVirtual = this.placementItem.isVirtual(heldStack);
        
        FilterMode filterMode =  this.placementItem.getFilterMode(heldStack);

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
    
    public boolean isExcavation()
    {
        return this.isExcavation;
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
    
    /**
     * The model state (if applies) that should be used to 
     * render placement preview. Override with context-dependent
     * version if available.
     */
    protected ModelState previewModelState()
    {
        return PlacementItem.getStackModelState(this.heldStack);
    }
    
    public ItemStack placedStack()
    {
        return heldStack;
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
        
        ModelState placementModelState = this.previewModelState();
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
}