package grondag.adversity.superblock.model.painter;

import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;

public abstract class CubicQuadPainter extends QuadPainter
{

    protected CubicQuadPainter(ModelState modelState, int painterIndex)
    {
        super(modelState, painterIndex);
    }
}
