package grondag.adversity.superblock.model.painter.surface;

public class Surface
{
    public final int ordinal;
    public final SurfaceType surfaceType;
    public final SurfaceTopology topology;
    
    public Surface(int ordinal, SurfaceType paintType, SurfaceTopology topology)
    {
        this.ordinal = ordinal;
        this.surfaceType = paintType;
        this.topology = topology;
    }
}