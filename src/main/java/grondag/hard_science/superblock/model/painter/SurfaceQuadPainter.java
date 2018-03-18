package grondag.hard_science.superblock.model.painter;

import grondag.exotic_matter.model.ISuperModelState;
import grondag.exotic_matter.model.PaintLayer;
import grondag.exotic_matter.render.Surface;

public abstract class SurfaceQuadPainter extends QuadPainter
{

    protected SurfaceQuadPainter(ISuperModelState modelState, Surface surface, PaintLayer paintLayer)
    {
        super(modelState, surface, paintLayer);
    }
}
