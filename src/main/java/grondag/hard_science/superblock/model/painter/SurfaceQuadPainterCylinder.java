package grondag.hard_science.superblock.model.painter;

import grondag.hard_science.library.render.RawQuad;
import grondag.hard_science.superblock.model.state.PaintLayer;
import grondag.hard_science.superblock.model.state.Surface;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;

public class SurfaceQuadPainterCylinder extends SurfaceQuadPainter
{

    public SurfaceQuadPainterCylinder(ModelState modelState, Surface surface, PaintLayer paintLayer)
    {
        super(modelState, surface, paintLayer);
    }

    @Override
    public RawQuad paintQuad(RawQuad quad)
    {
        return null;
    }
}
