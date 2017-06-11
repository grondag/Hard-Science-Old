package grondag.adversity.superblock.model.painter;

import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.superblock.model.painter.surface.Surface;
import grondag.adversity.superblock.model.state.PaintLayer;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.util.math.MathHelper;

/**
 * Paints unconstrained, non-wrapping 2d surface.
 * Expects that UV coordinates on incoming quads have the following properties:
 * A UV distance of 1 represents one block in world.
 * (UV scale on the surface instance will be 1.0)
 * UV values range from 0 through 256 and then repeat.
 * This means applied textures repeat every 256 blocks in both directions of the plane.
 * 
 * All textures will be smaller than 256x256 blocks, so attempts to 
 * alternate and rotate (if supported) the texture within that area to break up appearance.
 * 
 * Texture rotation and alternation is driven by relative position of UV coordinates
 * within the 256 x 256 block plane.
 * 
 * @author grondag
 *
 */
public class SurfaceQuadPainterTiles extends SurfaceQuadPainter
{
    public SurfaceQuadPainterTiles(ModelState modelState, Surface surface, PaintLayer paintLayer)
    {
        super(modelState, surface, paintLayer);
    }

    @Override
    public RawQuad paintQuad(RawQuad quad)
    {
        int sliceCount = this.texture.textureScale.sliceCount;
        
        float maxU = Math.max(quad.maxU, quad.minU);
        int uOrdinal = ((Math.round(maxU) - 1) / sliceCount);
        
        float maxV = Math.max(quad.maxV, quad.minV);
        int vOrdinal = ((Math.round(maxV) - 1) / sliceCount);
        
        // bring unit uv coordinates within the range of our texture size
        int shiftU = uOrdinal * sliceCount;
        if(shiftU > 0)
        {
            quad.maxU -= shiftU;
            quad.minU -= shiftU;
        }
        
        int shiftV= vOrdinal * sliceCount;
        if(shiftV > 0)
        {
            quad.maxV -= shiftV;
            quad.minV -= shiftV;
        }
        
        // uv coordinates should now be in range 0 to sliceCount
        // so just need to scale so that max values are 16.0
        
        double uvScale = 16.0 / this.texture.textureScale.sliceCount;
        quad.scaleQuadUV(uvScale, uvScale);
       
       int hash = MathHelper.hash(uOrdinal | (vOrdinal << 8));
        
        int textureVersion = this.texture.textureVersionMask & (hash >> 4);
        quad.textureSprite = this.texture.getTextureSprite(textureVersion);
                
        int rotation = hash & 0x3;
        if(this.texture.allowRotation & rotation > 0)
        {
            for(int i = 0; i <= rotation; i++)
            {
                float oldMinU = quad.minU;
                float oldMaxU = quad.maxU;
                quad.minU = quad.minV;
                quad.maxU = quad.maxV;
                quad.minV = 16 - oldMaxU;
                quad.maxV = 16 - oldMinU;

                quad.rotateQuadUVMoveTexture();
            }
        }
        return quad;
    }
}
