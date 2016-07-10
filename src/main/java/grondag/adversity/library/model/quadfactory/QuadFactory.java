package grondag.adversity.library.model.quadfactory;

import java.util.LinkedList;
import java.util.List;

import grondag.adversity.Adversity;

import javax.print.attribute.standard.MediaSize.Other;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraftforge.client.model.pipeline.LightUtil;
import com.google.common.collect.ImmutableList;

public class QuadFactory
{
    static final double EPSILON = 0.0000005;


    public static final List<BakedQuad> EMPTY_QUAD_LIST = new ImmutableList.Builder<BakedQuad>().build();
    /**
     * Takes a normal vanilla quad and recolors it to return a normal quad.
     */
    public static BakedQuad recolorVanillaQuad(BakedQuad quadIn, int color)
    {
        float shade = LightUtil.diffuseLight(quadIn.getFace());
        int colorOut = shadeColor(color, shade, true);

        int[] aint = quadIn.getVertexData();
        aint[3] = colorOut;
        aint[3 + 7] = colorOut;
        aint[3 + 14] = colorOut;
        aint[3 + 21] = colorOut;

        return new BakedQuad(aint, colorOut, quadIn.getFace(), quadIn.getSprite(), quadIn.shouldApplyDiffuseLighting(), quadIn.getFormat());
    }

    public static int shadeColor(int color, float shade, boolean glOrder)
    {
        int red = (int) (shade * 255f * ((color >> 16 & 0xFF) / 255f));
        int green = (int) (shade * 255f * ((color >> 8 & 0xFF) / 255f));
        int blue = (int) (shade * 255f * ((color & 0xFF) / 255f));
        int alpha = color >> 24 & 0xFF;

        return glOrder ? red  | green << 8 | blue << 16 | alpha << 24 : red << 16 | green << 8 | blue | alpha << 24;
    }

    /**
     * Rotates face texture 90deg clockwise
     */
    static void rotateQuadUV(Vertex v1, Vertex v2, Vertex v3, Vertex v4)
    {
        double swapU = v1.u;
        double swapV = v1.v;
        v1.u = v2.u;
        v1.v = v2.v;
        v2.u = v3.u;
        v2.v = v3.v;
        v3.u = v4.u;
        v3.v = v4.v;
        v4.u = swapU;
        v4.v = swapV;
    }



    //    private static int[] vertexToInts(double x, double y, double z, double u, double v, int color, TextureAtlasSprite sprite)
    //    {
    //
    //        return new int[] { Float.floatToRawIntBits((float) x), Float.floatToRawIntBits((float) y), Float.floatToRawIntBits((float) z), color,
    //                Float.floatToRawIntBits(sprite.getInterpolatedU(u)), Float.floatToRawIntBits(sprite.getInterpolatedV(v)), 0 };
    //    }


}