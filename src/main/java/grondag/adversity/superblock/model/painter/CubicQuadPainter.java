package grondag.adversity.superblock.model.painter;

import grondag.adversity.library.Rotation;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;

public abstract class CubicQuadPainter extends QuadPainter
{

    protected final Rotation rotation;
    protected final int blockVersion;

    protected CubicQuadPainter(ModelState modelState, int painterIndex)
    {
        super(modelState, painterIndex);
        this.rotation = modelState.getRotation();
        this.blockVersion = modelState.getBlockVersion();
    }
}
