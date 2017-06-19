package grondag.adversity.superblock.model.painter;

import grondag.adversity.library.render.RawQuad;
import grondag.adversity.superblock.model.state.PaintLayer;
import grondag.adversity.superblock.model.state.Surface;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;

public class CubicQuadPainterTiles extends CubicQuadPainter
{
    public CubicQuadPainterTiles(ModelState modelState, Surface surface, PaintLayer paintLayer)
    {
        super(modelState, surface, paintLayer);
    }

    @Override
    public RawQuad paintQuad(RawQuad quad)
    {
        quad.rotation = this.rotation;
        quad.textureName = this.texture.getTextureName(this.blockVersion);
        return quad;
    }
}
