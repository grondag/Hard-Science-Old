package grondag.hard_science.superblock.model.painter;

import grondag.exotic_matter.render.RawQuad;
import grondag.exotic_matter.render.Surface;
import grondag.exotic_matter.world.Rotation;
import grondag.hard_science.Log;
import grondag.hard_science.superblock.model.state.PaintLayer;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.texture.TextureRotationType;
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
        if(Log.DEBUG_MODE && quad.lockUV) Log.warn("Tiled surface quad painter received quad with lockUV semantics.  Not expected");
        
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
        quad.textureName = this.texture.getTextureName(textureVersion);
                
        //FIXME - doubt that the rest of this still works at all after
        //lockUV and texture rotation were refactored.

        int rotationOrdinal = this.texture.rotation.rotation.ordinal();
        if(this.texture.rotation.rotationType() == TextureRotationType.RANDOM)
        {
            rotationOrdinal = (rotationOrdinal + hash) & 3;
        }
        
        quad.rotation = Rotation.values()[rotationOrdinal];
        
        if(rotationOrdinal > 0)
        {
            for(int i = 0; i < rotationOrdinal; i++)
            {
                float oldMinU = quad.minU;
                float oldMaxU = quad.maxU;
                quad.minU = quad.minV;
                quad.maxU = quad.maxV;
                quad.minV = 16 - oldMaxU;
                quad.maxV = 16 - oldMinU;

            }
        }
        return quad;
    }
}
