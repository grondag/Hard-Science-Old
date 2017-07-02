package grondag.hard_science.library.render;

import net.minecraft.util.EnumFacing;

public class SimpleQuadBounds
{
    public EnumFacing face;
    public double x0;
    public double y0;
    public double x1;
    public double y1;
    public double depth;
    public EnumFacing topFace;

    public SimpleQuadBounds(EnumFacing face, double x0, double y0, double x1, double y1, double depth, EnumFacing topFace)
    {
        this.face = face;
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.depth = depth;
        this.topFace = topFace;
    }
}