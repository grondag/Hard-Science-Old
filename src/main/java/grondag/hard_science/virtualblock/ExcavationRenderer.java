package grondag.hard_science.virtualblock;

import grondag.hard_science.library.render.RenderUtil;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
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
    
    public ExcavationRenderer(int id, AxisAlignedBB aabb, boolean isExchange)
    {
        this.id = id;
        this.isExchange = isExchange;
        this.setBounds(aabb);
    }
    
    public void setBounds(AxisAlignedBB bounds)
    {
        this.aabb = bounds;
        this.visibilityBounds = bounds.grow(192);
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
            AxisAlignedBB box = this.aabb;
            RenderGlobal.drawBoundingBox(bufferbuilder, box.minX - d0, box.minY - d1, box.minZ - d2, box.maxX - d0, box.maxY - d1, box.maxZ - d2, 1f, 0.3f, 0.3f, 1f);
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
        if(this.didDrawBoundsLastTime)
        {
            RenderUtil.drawGrid(buffer, this.aabb, this.lastEyePosition, d0, d1, d2, 1f, 0.3f, 0.3f, 0.5F);
        }
    }
    
    @SideOnly(Side.CLIENT)
    public void drawBox(BufferBuilder bufferbuilder, double d0, double d1, double d2)
    {
        if(this.didDrawBoundsLastTime)
        {
            AxisAlignedBB box = this.aabb;
            RenderGlobal.addChainedFilledBoxVertices(bufferbuilder, box.minX - d0, box.minY - d1, box.minZ - d2, box.maxX - d0, box.maxY - d1, box.maxZ - d2, 1f, 0.3f, 0.3f, 0.3f);
            this.didDrawBoundsLastTime = true;
        }
    }
}
