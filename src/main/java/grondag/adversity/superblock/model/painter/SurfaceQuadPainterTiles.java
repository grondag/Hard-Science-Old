package grondag.adversity.superblock.model.painter;

import grondag.adversity.Output;
import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.superblock.model.layout.PaintLayer;
import grondag.adversity.superblock.model.painter.surface.Surface;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.texture.TextureScale;

/**
 * Paints unconstrained, non-wrapping 2d surface.
 * Expects that UV coordinates on incoming quads have the following properties:
 * A UV distance of 16 represents one block in world.
 * UV values range from 0 through 4096 (16 * 256) and then repeat.
 * This means surfaces repeat every 256 blocks in both directions of the plane.
 * 
 * Texture rotation and alternation is driven by position of UV coordinates
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
        quad.textureSprite = this.texture.getTextureSprite(0);
        int sliceCount = this.texture.textureScale.sliceCount;
        
        // bring unit uv coordinates within the range of our texture size
        float maxU = Math.max(quad.maxU, quad.minU);
        int shiftU = ((Math.round(maxU) - 1) / sliceCount) * sliceCount;
        if(shiftU > 0)
        {
            quad.maxU -= shiftU;
            quad.minU -= shiftU;
        }
        
        float maxV = Math.max(quad.maxV, quad.minV);
        int shiftV= ((Math.round(maxV) - 1) / sliceCount) * sliceCount;
        if(shiftV > 0)
        {
            quad.maxV -= shiftV;
            quad.minV -= shiftV;
        }
        
        // uv coordinates should now be in range 0 to sliceCount
        // so just need to scale so that max values are 16.0
        
        double uvScale = 16.0 / this.texture.textureScale.sliceCount;
        quad.scaleQuadUV(uvScale, uvScale);
       
        return quad;
    }
}
