package grondag.adversity.feature.volcano.lava;

import java.util.List;

import grondag.adversity.library.render.QuadFactory;
import grondag.adversity.library.render.RawQuad;
import grondag.adversity.library.render.Vertex;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@SideOnly(Side.CLIENT)
public class RenderLavaBlob extends Render<EntityLavaBlob>
{

    /** The GL display list index - 0 indicates not yet created b/c GL returns non-zero indices */
    private static int displayList = 0;

    public RenderLavaBlob(RenderManager renderManagerIn)
    {
        super(renderManagerIn);
    }

    private static final ResourceLocation TEXTURE = new ResourceLocation("adversity:textures/entity/lava.png");

    private static final List<RawQuad> quads = QuadFactory.makeIcosahedron(new Vec3d(0,0,0), 0.5, new RawQuad());

    public void doRender(EntityLavaBlob entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        this.bindEntityTexture(entity);
        GlStateManager.translate((float)x, (float)y, (float)z);
        GlStateManager.enableRescaleNormal();
        float scale = entity.getScale();
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((float)(this.renderManager.options.thirdPersonView == 2 ? -1 : 1) * -this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

        if (this.renderOutlines)
        {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(entity));
        }

        if(displayList == 0) compileDisplayList();

        GlStateManager.callList(displayList);

        if (this.renderOutlines)
        {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }

        GlStateManager.disableRescaleNormal();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(EntityLavaBlob entity)
    {
        return TEXTURE;
    }

    public static IRenderFactory<EntityLavaBlob> factory() {
        return manager -> new RenderLavaBlob(manager);
    }

    @SideOnly(Side.CLIENT)
    private static void compileDisplayList()
    {
        displayList = GLAllocation.generateDisplayLists(1);
        GlStateManager.glNewList(displayList, 4864);
        VertexBuffer vertexbuffer = Tessellator.getInstance().getBuffer();

        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);

        for(RawQuad q : quads)
        {
            for(int i = 0; i < 4; i++)
            {
                Vertex v = q.getVertex(i);
                Vec3d n = v.getNormal();
                vertexbuffer.pos(v.xCoord, v.yCoord, v.zCoord).tex(v.u, v.v).normal((float)n.xCoord, (float)n.yCoord, (float)n.zCoord).endVertex();
            }
        }

        Tessellator.getInstance().draw();

        GlStateManager.glEndList();



    }

}
