package grondag.hard_science.library.render;

import grondag.hard_science.Configurator;
import grondag.hard_science.library.world.Rotation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class QuadBakery
{
    /**
     * Creates a baked quad - does not (permanently) mutate the given instance.
     */
    public static BakedQuad createBakedQuad(RawQuad raw)
    {
        // this is an egregious hack, but ensure we don't mutate the instance if UVs need to be rotated
        if(!raw.lockUV || raw.rotation == Rotation.ROTATE_NONE)
        {
            return createBakedQuadInner(raw);
        }
        else
        {
            RawQuad workQuad = raw.clone();
            for (int r = 0; r < raw.rotation.ordinal(); r++)
            {
                workQuad.rotateQuadUV();
            }
            return createBakedQuadInner(workQuad);
        }
    }
    
    /**
     * Does not do rotation because it mutates this instance.
     */
    private static BakedQuad createBakedQuadInner(RawQuad raw)
    {    
        float spanU = raw.maxU - raw.minU;
        float spanV = raw.maxV - raw.minV;
        
        TextureAtlasSprite textureSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(raw.textureName);
        
        //Used to manipulate UV without breaking immutability.
        //Dimensions are vertex 0-4 and u/v 0-1.
        float[][] uvData = new float[4][2];
        for(int v = 0; v < 4; v++)
        {
            uvData[v][0] = raw.minU + spanU * ((float)raw.getVertex(v).u) / 16F;
            uvData[v][1] = raw.minV + spanV * ((float)raw.getVertex(v).v) / 16F;
        }

        if(raw.shouldContractUVs)
        {
            contractUVs(raw, textureSprite, uvData);
        }

        int[] vertexData = new int[28];

        // see LightingMode for more info on how this enables full brightness for block models.
//        VertexFormat format = raw.isItem ? DefaultVertexFormats.ITEM : lightingMode.vertexFormat;
        VertexFormat format = raw.lightingMode.getVertexFormat();

        float[] faceNormal = raw.getFaceNormalArray();          

        // The item renderer doesn't recognize pre-baked lightmaps, so we have to get creative.
        // REMOVED: this was really a hack - doesn't work well if item is at angle to Y axis
        // There does not yet appear to be a supported method for full-bright items.
//        if(raw.isItem && raw.lightingMode == LightingMode.FULLBRIGHT)
//        {
//            faceNormal[0] = 0;
//            faceNormal[1] = 1;
//            faceNormal[2] = 0;
//        }
        
        for(int v = 0; v < 4; v++)
        {

            for(int e = 0; e < format.getElementCount(); e++)
            {
                switch(format.getElement(e).getUsage())
                {
                case POSITION:
                    LightUtil.pack(raw.getVertex(v).xyzToFloatArray(), vertexData, format, v, e);
                    break;

                case NORMAL: 
                    LightUtil.pack(raw.getVertex(v).hasNormal() ? raw.getVertex(v).normalToFloatArray() : faceNormal, vertexData, format, v, e);
                    break;

                case COLOR:
                    float shade;
                    if(raw.lightingMode == LightingMode.SHADED && Configurator.RENDER.enableCustomShading && !raw.surfaceInstance.isLampGradient())
                    {
                        Vec3d surfaceNormal = raw.getVertex(v).hasNormal() ? raw.getVertex(v).getNormal() : raw.getFaceNormal();
                        shade = Configurator.RENDER.minAmbientLight + 
                                (float) ((surfaceNormal.dotProduct(Configurator.Render.lightingNormal) + 1) * Configurator.Render.normalLightFactor);
                    }
                    else
                    {
                        shade = 1.0F;
                    }
                    float[] colorRGBA = new float[4];
                    colorRGBA[0] = ((float) (raw.getVertex(v).color >> 16 & 0xFF)) * shade / 255f;
                    colorRGBA[1] = ((float) (raw.getVertex(v).color >> 8 & 0xFF)) * shade / 255f;
                    colorRGBA[2] = ((float) (raw.getVertex(v).color  & 0xFF)) * shade / 255f;
                    colorRGBA[3] = ((float) (raw.getVertex(v).color >> 24 & 0xFF)) / 255f;
                    LightUtil.pack(colorRGBA, vertexData, format, v, e);
                    break;

                case UV: 
                    if(format.getElement(e).getIndex() == 1)
                    {
                        // This block happens when we are using a BLOCK vertex format
                        // that accepts pre-baked lightmaps.  Assuming here that the 
                        // intention is for full brightness. (Don't have a way to pass something dimmer.)
                        float[] fullBright = new float[2];

                        //Don't really understand how brightness format works, but this does the job.
                        //It mimics the lightmap that would be returned from a block in full brightness.
                        fullBright[0] = (float)(15 * 0x20) / 0xFFFF;
                        fullBright[1] = (float)(15 * 0x20) / 0xFFFF;

                        LightUtil.pack(fullBright, vertexData, format, v, e);
                    }
                    else
                    {
                        // This block handles the normal case: texture UV coordinates
                        float[] interpolatedUV = new float[2];
                        interpolatedUV[0] = textureSprite.getInterpolatedU(uvData[v][0]);
                        interpolatedUV[1] = textureSprite.getInterpolatedV(uvData[v][1]);
                        LightUtil.pack(interpolatedUV, vertexData, format, v, e);
                    }
                    break;

                default:
                    // NOOP, padding or weirdness
                }
            }
        }

        boolean applyDiffuseLighting = raw.lightingMode == LightingMode.SHADED
                && !raw.surfaceInstance.isLampGradient()  
                && !Configurator.RENDER.enableCustomShading;
        
        return QuadCache.INSTANCE.getCachedQuad(new CachedBakedQuad(vertexData, raw.color, raw.face, textureSprite, 
                applyDiffuseLighting, format));
        
    }
    
    /**
     * Prevents visible seams along quad boundaries due to slight overlap
     * with neighboring textures or empty texture buffer.
     * Borrowed from Forge as implemented by Fry in UnpackedBakedQuad.build().
     * Array dimensions are vertex 0-3, u/v 0-1
     */
    private static void contractUVs(RawQuad raw, TextureAtlasSprite textureSprite, float[][] uvData)
    {
        if(!raw.shouldContractUVs) return;

        final float eps = 1f / 0x100;

        float tX = textureSprite.getOriginX() / textureSprite.getMinU();
        float tY = textureSprite.getOriginY() / textureSprite.getMinV();
        float tS = tX > tY ? tX : tY;
        float ep = 1f / (tS * 0x100);

        //uve refers to the uv element number in the format
        //we will always have uv data directly
        float center[] = new float[2];

        for(int v = 0; v < 4; v++)
        {
            center[0] += uvData[v][0] / 4;
            center[1] += uvData[v][1] / 4;
        }

        for(int v = 0; v < 4; v++)
        {
            for (int i = 0; i < 2; i++)
            {
                float uo = uvData[v][i];
                float un = uo * (1 - eps) + center[i] * eps;
                float ud = uo - un;
                float aud = ud;
                if(aud < 0) aud = -aud;
                if(aud < ep) // not moving a fraction of a pixel
                {
                    float udc = uo - center[i];
                    if(udc < 0) udc = -udc;
                    if(udc < 2 * ep) // center is closer than 2 fractions of a pixel, don't move too close
                    {
                        un = (uo + center[i]) / 2;
                    }
                    else // move at least by a fraction
                    {
                        un = uo + (ud < 0 ? ep : -ep);
                    }
                }
                uvData[v][i] = un;
            }
        }
    }
}
