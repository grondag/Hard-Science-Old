package grondag.hard_science.superblock.model.painter;

import grondag.exotic_matter.render.RawQuad;
import grondag.exotic_matter.render.Surface;
import grondag.hard_science.Log;
import grondag.hard_science.superblock.model.state.ModelState;
import grondag.hard_science.superblock.model.state.PaintLayer;

public class SurfaceQuadPainterCylinder extends SurfaceQuadPainter
{

    public SurfaceQuadPainterCylinder(ModelState modelState, Surface surface, PaintLayer paintLayer)
    {
        super(modelState, surface, paintLayer);
    }

    @Override
    public RawQuad paintQuad(RawQuad quad)
    {
        if(Log.DEBUG_MODE && quad.lockUV) Log.warn("Cylindrical surface quad painter received quad with lockUV semantics.  Not expected");
        return null;
    }
}
