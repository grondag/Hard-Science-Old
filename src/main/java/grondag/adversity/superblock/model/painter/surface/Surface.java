package grondag.adversity.superblock.model.painter.surface;

public class Surface
{
    public final SurfaceType surfaceType;
    public final SurfaceTopology topology;
    
    /** 
     * If true, generator will assign colors to vertexes to indicate proximity to lamp surface.
     * Vertices next to lamp have color WHITE and those away have color BLACK.
     * If the lighting mode for the surface is shaded, then quad bake should color
     * vertices to form a gradient.  
     * If the surface is full-brightness, need to re-color all vertices to white.
     */
    public final boolean isLampGradient;
    
    public Surface(SurfaceType paintType, SurfaceTopology topology)
    {
        this(paintType, topology, false);
    }
    
    public Surface(SurfaceType paintType, SurfaceTopology topology, boolean isPreShaded)
    {
        this.surfaceType = paintType;
        this.topology = topology;
        this.isLampGradient = isPreShaded;
    }
}