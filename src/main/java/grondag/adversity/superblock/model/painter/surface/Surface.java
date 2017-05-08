package grondag.adversity.superblock.model.painter.surface;

public class Surface
{
    public final int ordinal;
    public final SurfaceType paintType;
    public final SurfaceTopology topology;
    
    public Surface(int ordinal, SurfaceType paintType, SurfaceTopology topology)
    {
        this.ordinal = ordinal;
        this.paintType = paintType;
        this.topology = topology;
    }
}