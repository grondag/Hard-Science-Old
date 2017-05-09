package grondag.adversity.superblock.model.painter;

import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;

public abstract class SurfaceQuadPainter extends QuadPainter
{

    protected SurfaceQuadPainter(ModelState modelState, int painterIndex)
    {
        super(modelState, painterIndex);
    }
}
