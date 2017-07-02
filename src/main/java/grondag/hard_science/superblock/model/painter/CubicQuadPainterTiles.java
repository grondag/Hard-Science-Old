package grondag.hard_science.superblock.model.painter;

import grondag.hard_science.Log;
import grondag.hard_science.library.render.RawQuad;
import grondag.hard_science.superblock.model.state.PaintLayer;
import grondag.hard_science.superblock.model.state.Surface;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;

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
        quad.rotation = this.textureRotationForFace(quad.getNominalFace());
        quad.textureName = this.texture.getTextureName(this.textureVersionForFace(quad.getNominalFace()));
        return quad;
    }
}
