package grondag.hard_science.library.render;


/**
 * Used to express quads where u,v map directly to x, y on the given face
 */
public class FaceVertex
{
    public double x;
    public double y;
    public double depth;

    public FaceVertex(double x, double y, double depth)
    {
        this.x = x;
        this.y = y;
        this.depth = depth;
    }

    public FaceVertex clone()
    {
        return new FaceVertex(x, y, depth);
    }

    public int getColor(int defaultColor)
    {
        return defaultColor;
    }

    public static class Colored extends FaceVertex
    {
        private int color = 0xFFFFFFFF;

        public Colored(double x, double y, double depth, int color)
        {
            super(x, y, depth);
            this.color = color;
        }

        public Colored(double x, double y, double depth, double u, double v, int color)
        {
            super(x, y, depth);
            this.color = color;
        }

        @Override
        public FaceVertex clone()
        {
            return new FaceVertex.Colored(x, y, depth, color);
        }

        @Override
        public int getColor(int defaultColor)
        {
            return color;
        }
    }
}