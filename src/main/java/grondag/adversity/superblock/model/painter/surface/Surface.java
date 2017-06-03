package grondag.adversity.superblock.model.painter.surface;

public class Surface
{
    public final SurfaceType surfaceType;
    public final SurfaceTopology topology;
    
    /** if true, generator will pre-shade the quads on this surface.
     * Baked quads will be emitted with applyDiffuseLighting = false;
     */
    public final boolean isPreShaded;
    
    public Surface(SurfaceType paintType, SurfaceTopology topology)
    {
        this(paintType, topology, false);
    }
    
    public Surface(SurfaceType paintType, SurfaceTopology topology, boolean isPreShaded)
    {
        this.surfaceType = paintType;
        this.topology = topology;
        this.isPreShaded = isPreShaded;
    }
}