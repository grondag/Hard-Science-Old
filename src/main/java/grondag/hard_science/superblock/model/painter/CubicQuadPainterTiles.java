package grondag.hard_science.superblock.model.painter;

import grondag.exotic_matter.render.RawQuad;
import grondag.exotic_matter.render.Surface;
import grondag.exotic_matter.varia.Useful;
import grondag.exotic_matter.world.Rotation;
import grondag.hard_science.Log;
import grondag.hard_science.superblock.model.state.ModelState;
import grondag.hard_science.superblock.model.state.PaintLayer;
import net.minecraft.util.math.MathHelper;

public class CubicQuadPainterTiles extends CubicQuadPainter
{
    public CubicQuadPainterTiles(ModelState modelState, Surface surface, PaintLayer paintLayer)
    {
        super(modelState, surface, paintLayer);
    }

    @Override
    public RawQuad paintQuad(RawQuad quad)
    {
        if(Log.DEBUG_MODE && !quad.lockUV) Log.warn("Tiled cubic quad painter received quad without lockUV semantics.  Not expected");
        
        Rotation rotation = this.textureRotationForFace(quad.getNominalFace());
        int textureVersion = this.textureVersionForFace(quad.getNominalFace());
        
        if(quad.surfaceInstance.textureSalt != 0)
        {
            int saltHash = MathHelper.hash(quad.surfaceInstance.textureSalt);
            rotation = Useful.offsetEnumValue(rotation, saltHash & 3);
            textureVersion = (textureVersion + (saltHash >> 2)) & this.texture.textureVersionMask;
        }
        
        quad.rotation = rotation;
        quad.textureName = this.texture.getTextureName(textureVersion);
        return quad;
    }
}
