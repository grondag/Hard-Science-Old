package grondag.hard_science.superblock.model.painter;

import grondag.exotic_matter.render.Surface;
import grondag.hard_science.superblock.model.state.ModelState;
import grondag.hard_science.superblock.model.state.PaintLayer;

public abstract class SurfaceQuadPainter extends QuadPainter
{

    protected SurfaceQuadPainter(ModelState modelState, Surface surface, PaintLayer paintLayer)
    {
        super(modelState, surface, paintLayer);
    }
}
