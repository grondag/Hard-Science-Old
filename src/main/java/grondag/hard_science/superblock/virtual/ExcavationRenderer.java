package grondag.hard_science.superblock.virtual;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.exotic_matter.render.RenderUtil;
import grondag.hard_science.Configurator;
import grondag.hard_science.Log;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ExcavationRenderer
{
    public final int id;
    
    /**
     * If true, is replacement instead of straight excavation.
     */
    public final boolean isExchange;
    
    private AxisAlignedBB aabb;
    
    private AxisAlignedBB visibilityBounds;
    
    private boolean didDrawBoundsLastTime = false;
    
    private Vec3d lastEyePosition;
    
    /**
     * If non-null, then we should render individual positions instead of AABB.
     */
    private BlockPos[] positions;
    
    public ExcavationRenderer(int id, @Nonnull AxisAlignedBB aabb, boolean isExchange, @Nullable BlockPos[] positions)
    {
        this.id = id;
        this.isExchange = isExchange;
        this.setBounds(aabb, positions);
    }
    
    public void setBounds(@Nonnull AxisAlignedBB bounds, @Nullable BlockPos[] positions)
    {
        this.aabb = bounds;
        this.visibilityBounds = bounds.grow(192);
        this.positions = positions;
        
        if(Configurator.logExcavationRenderTracking) Log.info("id %d Renderer setBounds position count = %d", id, positions == null ? 0 : positions.length);
    }
    
    public AxisAlignedBB bounds()
    {
        return this.aabb;
    }
    
    public AxisAlignedBB visibilityBounds()
    {
        return this.visibilityBounds;
    }
    
    /** return true if something was drawn */
    @SideOnly(Side.CLIENT)
    public boolean drawBounds(BufferBuilder bufferbuilder, Entity viewEntity, double d0, double d1, double d2, float partialTicks)
    {
        this.lastEyePosition = viewEntity.getPositionEyes(partialTicks);
        if(this.visibilityBounds.contains(this.lastEyePosition))
        {
            if(this.positions == null)
            {
                AxisAlignedBB box = this.aabb;
                RenderGlobal.drawBoundingBox(bufferbuilder, box.minX - d0, box.minY - d1, box.minZ - d2, box.maxX - d0, box.maxY - d1, box.maxZ - d2, 1f, 0.3f, 0.3f, 1f);
            }
            else
            {
                for(BlockPos pos : this.positions)
                {
                    double x = pos.getX() - d0;
                    double y = pos.getY() - d1;
                    double z = pos.getZ() - d2;
                    RenderGlobal.drawBoundingBox(bufferbuilder, x, y, z, x + 1, y + 1, z + 1, 1f, 0.3f, 0.3f, 1f);
                }
            }
            this.didDrawBoundsLastTime = true;
            return true;
        }
        else
        {
            this.didDrawBoundsLastTime = false;
            return false;
        }
    }
    
    @SideOnly(Side.CLIENT)
    public void drawGrid(BufferBuilder buffer, double d0, double d1, double d2)
    {
        if(this.didDrawBoundsLastTime && this.positions == null)
        {
            RenderUtil.drawGrid(buffer, this.aabb, this.lastEyePosition, d0, d1, d2, 1f, 0.3f, 0.3f, 0.5F);
        }
    }
    
    @SideOnly(Side.CLIENT)
    public void drawBox(BufferBuilder bufferbuilder, double d0, double d1, double d2)
    {
        if(this.didDrawBoundsLastTime)
        {
            if(this.positions == null)
            {
                AxisAlignedBB box = this.aabb;
                RenderGlobal.addChainedFilledBoxVertices(bufferbuilder, box.minX - d0, box.minY - d1, box.minZ - d2, box.maxX - d0, box.maxY - d1, box.maxZ - d2, 1f, 0.3f, 0.3f, 0.3f);
            }
            else
            {
                for(BlockPos pos : this.positions)
                {
                    double x = pos.getX() - d0;
                    double y = pos.getY() - d1;
                    double z = pos.getZ() - d2;
                    RenderGlobal.addChainedFilledBoxVertices(bufferbuilder, x, y, z, x + 1, y + 1, z + 1, 1f, 0.3f, 0.3f, 0.3f);
                }
            }
        }
    }
}
