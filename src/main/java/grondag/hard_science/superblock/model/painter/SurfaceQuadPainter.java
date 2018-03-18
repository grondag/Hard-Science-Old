package grondag.hard_science.superblock.model.painter;

import grondag.exotic_matter.model.PaintLayer;
import grondag.exotic_matter.render.Surface;
import grondag.hard_science.superblock.model.state.ModelState;

public abstract class SurfaceQuadPainter extends QuadPainter
{

    protected SurfaceQuadPainter(ModelState modelState, Surface surface, PaintLayer paintLayer)
    {
        super(modelState, surface, paintLayer);
    }
}
