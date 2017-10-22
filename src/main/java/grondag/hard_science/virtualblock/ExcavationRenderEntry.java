package grondag.hard_science.virtualblock;

import grondag.hard_science.superblock.placement.PlacementResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Class exists on server but render methods do not.
 * Server instantiates (and generates IDs) and transmits to clients.
 */
@SideOnly(Side.CLIENT)
public class ExcavationRenderEntry
{
    private static int nextID = 0;
    
    public final int id;
    
    public final AxisAlignedBB aabb;
    
    public final AxisAlignedBB visibilityBounds;
    
    private boolean didDrawBoundsLastTime = false;
    
    /**
     * For server side
     */
    public ExcavationRenderEntry(EntityPlayer player, PlacementResult result)
    {
        this.id = nextID++;
        this.aabb = result.placementAABB();
        this.visibilityBounds = this.aabb.grow(192);
    }
    
    /**
     * For client side
     */
    @SideOnly(Side.CLIENT)
    public ExcavationRenderEntry(int id, AxisAlignedBB bounds)
    {
        this.id = nextID++;
        this.aabb = bounds;
        this.visibilityBounds = this.aabb.grow(Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16);
    }

    /** return true if something was drawn */
    public boolean drawBounds(BufferBuilder bufferbuilder, Entity viewEntity, double d0, double d1, double d2, float partialTicks)
    {
        if(this.visibilityBounds.contains(viewEntity.getPositionEyes(partialTicks)))
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
    
    public void drawBox(BufferBuilder bufferbuilder, Entity viewEntity, double d0, double d1, double d2, float partialTicks)
    {
        if(this.didDrawBoundsLastTime)
        {
            AxisAlignedBB box = this.aabb;
            RenderGlobal.addChainedFilledBoxVertices(bufferbuilder, box.minX - d0, box.minY - d1, box.minZ - d2, box.maxX - d0, box.maxY - d1, box.maxZ - d2, 1f, 0.3f, 0.3f, 0.3f);
            this.didDrawBoundsLastTime = true;
        }
    }
}
