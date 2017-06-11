package grondag.adversity.superblock.model.painter;

import grondag.adversity.superblock.model.state.PaintLayer;
import grondag.adversity.superblock.model.state.Surface;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;

public abstract class SurfaceQuadPainter extends QuadPainter
{

    protected SurfaceQuadPainter(ModelState modelState, Surface surface, PaintLayer paintLayer)
    {
        super(modelState, surface, paintLayer);
    }
}
