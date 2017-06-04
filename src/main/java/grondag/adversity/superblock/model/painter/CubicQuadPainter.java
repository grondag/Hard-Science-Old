package grondag.adversity.superblock.model.painter;

import grondag.adversity.library.Rotation;
import grondag.adversity.superblock.model.layout.PaintLayer;
import grondag.adversity.superblock.model.painter.surface.Surface;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;

public abstract class CubicQuadPainter extends QuadPainter
{

    protected final Rotation rotation;
    protected final int blockVersion;

    protected CubicQuadPainter(ModelState modelState, Surface surface, PaintLayer paintLayer)
    {
        super(modelState, surface, paintLayer);
        this.rotation = modelState.hasRotation() ? modelState.getRotation(this.texture.textureScale) : Rotation.ROTATE_NONE;
        this.blockVersion = modelState.hasBlockVersions() ? modelState.getBlockVersion(this.texture.textureScale) : 0;
    }
}
