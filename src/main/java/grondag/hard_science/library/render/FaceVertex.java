package grondag.hard_science.library.render;


/**
 * Used to express quads on a face (2D).  By default u,v map directly to x, y on the given face
 */
public class FaceVertex
{
    public final double x;
    public final double y;
    public final double depth;

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

    public FaceVertex withXY(double x, double y)
    {
        return new FaceVertex(x, y, this.depth);
    }
    
    public FaceVertex withDepth(double depth)
    {
        return new FaceVertex(this.x, this.y, depth);
    }
    
    public FaceVertex withColor(int color)
    {
        return new FaceVertex.Colored(this.x, this.y, depth, color);
    }
    
    public FaceVertex withUV(double u, double v)
    {
        return new FaceVertex.UV(this.x, this.y, this.depth, u, v);
    }

    public int color(int defaultColor)
    {
        return defaultColor;
    }
    
    /**
     * This value is logical 0-1 within the texture for this face. NOT 0-16.  And NOT interpolated for the sprite. <br><br>
     * 
     * Note that the V orientation is flipped from the Y axis used for vertices.
     * Origin is at the top left for textures, vs. bottom left for vertex coordinates. 
     * This means the default values for u, v will be x, 1-y.  <br><br>
     * 
     * The bottom face is handled differently and RawQuad will flip it automatically.. 
     */
    public double u()
    {
        return x;
    }
    
    /**
     * See {@link #u()}
     */
    public double v()
    {
        return 1.0-y;
    }

    public static class Colored extends FaceVertex
    {
        private final int color;

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
        public int color(int defaultColor)
        {
            return color;
        }
        
        public FaceVertex withXY(double x, double y)
        {
            return new FaceVertex.Colored(x, y, this.depth, this.color);
        }
        
        public FaceVertex withDepth(double depth)
        {
            return new FaceVertex.Colored(this.x, this.y, depth, this.color);
        }
        
        public FaceVertex withColor(int color)
        {
            return new FaceVertex.Colored(this.x, this.y, depth, color);
        }
        
        public FaceVertex withUV(double u, double v)
        {
            return new FaceVertex.UVColored(this.x, this.y, this.depth, u, v, this.color);
        }
    }
    
    public static class UV extends FaceVertex
    {
        private final double u;
        private final double v;
        
        public UV(double x, double y, double depth, double u, double v)
        {
            super(x, y, depth);
            this.u = u;
            this.v = v;
        }

        @Override
        public double u()
        {
            return this.u;
        }
        
        @Override
        public double v()
        {
            return this.v;
        }
        
        @Override
        public FaceVertex clone()
        {
            return new FaceVertex.UV(x, y, depth, u, v);
        }
        
        public FaceVertex withXY(double x, double y)
        {
            return new FaceVertex.UV(x, y, this.depth, this.u, this.v);
        }
        
        public FaceVertex withDepth(double depth)
        {
            return new FaceVertex.UV(this.x, this.y, depth, this.u, this.v);
        }
        
        public FaceVertex withColor(int color)
        {
            return new FaceVertex.UVColored(this.x, this.y, depth, this.u, this.v, color);
        }
        
        public FaceVertex withUV(double u, double v)
        {
            return new FaceVertex.UV(this.x, this.y, this.depth, u, v);
        }
    }
    
    public static class UVColored extends FaceVertex
    {
        private final double u;
        private final double v;
        private final int color;
        
        public UVColored(double x, double y, double depth, double u, double v, int color)
        {
            super(x, y, depth);
            this.u = u;
            this.v = v;
            this.color = color;
        }
        
        @Override
        public int color(int defaultColor)
        {
            return color;
        }

        @Override
        public double u()
        {
            return this.u;
        }
        
        @Override
        public double v()
        {
            return this.v;
        }
        
        @Override
        public FaceVertex clone()
        {
            return new FaceVertex.UVColored(x, y, depth, u, v, color);
        }
        
        public FaceVertex withXY(double x, double y)
        {
            return new FaceVertex.UVColored(x, y, this.depth, this.u, this.v, this.color);
        }
        
        public FaceVertex withDepth(double depth)
        {
            return new FaceVertex.UVColored(this.x, this.y, depth, this.u, this.v, this.color);
        }
        
        public FaceVertex withColor(int color)
        {
            return new FaceVertex.UVColored(this.x, this.y, this.depth, this.u, this.v, color);
        }
        
        public FaceVertex withUV(double u, double v)
        {
            return new FaceVertex.UVColored(this.x, this.y, this.depth, u, v, this.color);
        }
    }
}