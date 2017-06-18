package grondag.adversity.superblock.model.painter;

import grondag.adversity.library.world.Rotation;
import grondag.adversity.superblock.model.state.PaintLayer;
import grondag.adversity.superblock.model.state.Surface;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;

public abstract class CubicQuadPainter extends QuadPainter
{

    protected final Rotation rotation;
    protected final int blockVersion;

    protected CubicQuadPainter(ModelState modelState, Surface surface, PaintLayer paintLayer)
    {
        super(modelState, surface, paintLayer);
        this.rotation = modelState.hasTextureRotation() ? modelState.getTextureRotation(this.texture.textureScale) : Rotation.ROTATE_NONE;
        this.blockVersion = modelState.hasBlockVersions() ? modelState.getBlockVersion(this.texture.textureScale) : 0;
    }
}
