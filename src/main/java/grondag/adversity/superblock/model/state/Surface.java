package grondag.adversity.superblock.model.state;

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
    
    /**
     * Instance with unit scale uScale and vScale = 1.0
     */
    public final SurfaceInstance unitInstance;
    
    public Surface(SurfaceType paintType, SurfaceTopology topology)
    {
        this(paintType, topology, false);
    }
    
    public Surface(SurfaceType paintType, SurfaceTopology topology, boolean isLampGradient)
    {
        this.surfaceType = paintType;
        this.topology = topology;
        this.isLampGradient = isLampGradient;
        this.unitInstance = new SurfaceInstance(1.0, 1.0);
    }
    
    public SurfaceInstance newInstance(double uScale, double vScale)
    {
        return new SurfaceInstance(uScale, vScale);
    }
    
    public class SurfaceInstance
    {
        /** 
         * The approximate in-world scale of the U texture axis.
         * At scale = 1.0, 0 to 16 is one block.
         * At scale = 1/16, 0 to 1 is one block, etc.
         * Generally only comes into play for non-cubic surface painters.
         */
        public final double uScale;
        
        /** 
         * The approximate in-world scale of the V texture axis.
         * At scale = 1.0, 0 to 16 is one block.
         * At scale = 1/16, 0 to 1 is one block, etc.
         * Generally only comes into play for non-cubic surface painters.
         */
        public final double vScale;
        
        private SurfaceInstance(double uScale, double vScale)
        {
            this.uScale = uScale;
            this.vScale = vScale;
        }

        public Surface surface()
        {
            return Surface.this;
        }
        
        public SurfaceType surfaceType() { return surfaceType; }
        public SurfaceTopology topology() { return topology; }
        public boolean isLampGradient() { return isLampGradient; }
    }
}