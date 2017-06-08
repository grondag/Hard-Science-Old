package grondag.adversity.superblock.model.painter;

import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.superblock.model.painter.surface.Surface;
import grondag.adversity.superblock.model.state.PaintLayer;
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
        if(this.texture.allowRotation)
        {
            quad.rotation = this.rotation;
        }
        quad.textureSprite = this.texture.getTextureSprite(this.blockVersion);
        return quad;
    }
}
