package grondag.hard_science.superblock.model.painter;

import grondag.hard_science.Log;
import grondag.hard_science.library.render.RawQuad;
import grondag.hard_science.superblock.model.state.PaintLayer;
import grondag.hard_science.superblock.model.state.Surface;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;

public class SurfaceQuadPainterTorus extends SurfaceQuadPainter
{
    
    public SurfaceQuadPainterTorus(ModelState modelState, Surface surface, PaintLayer paintLayer)
    {
        super(modelState, surface, paintLayer);
    }

    @Override
    public RawQuad paintQuad(RawQuad quad)
    {
        if(Log.DEBUG_MODE && quad.lockUV) Log.warn("Toroidal surface quad painter received quad with lockUV semantics.  Not expected");
        
        return null;
    }
}
