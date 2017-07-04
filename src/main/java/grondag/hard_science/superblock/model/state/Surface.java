package grondag.hard_science.superblock.model.state;

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
     * Instance with unit scale uScale and vScale = 1.0, uses depth for bigtex
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
        this.unitInstance = new SurfaceInstance(1.0, 1.0, false, true, 0);
    }
    
    public SurfaceInstance newInstance(double uScale, double vScale)
    {
        return new SurfaceInstance(uScale, vScale, false, true, 0);
    }
    
    public SurfaceInstance newInstance(double uScale, double vScale, boolean ignoreDepthForRandomization)
    {
        return new SurfaceInstance(uScale, vScale, ignoreDepthForRandomization, true, 0);
    }
    
    public SurfaceInstance newInstance(double uScale, double vScale, boolean ignoreDepthForRandomization, boolean allowBorders)
    {
        return new SurfaceInstance(uScale, vScale, ignoreDepthForRandomization, allowBorders, 0);
    }
    
    public SurfaceInstance newInstance(double uScale, double vScale, boolean ignoreDepthForRandomization, boolean allowBorders, int textureSalt)
    {
        return new SurfaceInstance(uScale, vScale, ignoreDepthForRandomization, allowBorders, textureSalt);
    }
    
    public SurfaceInstance newInstance(boolean ignoreDepthForRandomization)
    {
        return new SurfaceInstance(1, 1, ignoreDepthForRandomization, true, 0);
    }
    
    public SurfaceInstance newInstance(boolean ignoreDepthForRandomization, boolean allowBorders)
    {
        return new SurfaceInstance(1, 1, ignoreDepthForRandomization, allowBorders, 0);
    }
    
    public SurfaceInstance newInstance(boolean ignoreDepthForRandomization, boolean allowBorders, int textureSalt)
    {
        return new SurfaceInstance(1, 1, ignoreDepthForRandomization, allowBorders, textureSalt);
    }
    
    public class SurfaceInstance
    {
        /** 
         * The approximate in-world scale of the U texture orthogonalAxis.
         * At scale = 1.0, 0 to 16 is one block.
         * At scale = 1/16, 0 to 1 is one block, etc.
         * Generally only comes into play for non-cubic surface painters.
         */
        public final double uScale;
        
        /** 
         * The approximate in-world scale of the V texture orthogonalAxis.
         * At scale = 1.0, 0 to 16 is one block.
         * At scale = 1/16, 0 to 1 is one block, etc.
         * Generally only comes into play for non-cubic surface painters.
         */
        public final double vScale;
        
        /**
         * If true, texture painting should not vary by orthogonalAxis
         * orthogonal to the surface.  Ignored if {@link #textureSalt} is non-zero.
         */
        public final boolean ignoreDepthForRandomization;
        
        /**
         * If false, border and masonry painters will not render on this surface.
         * Set false for topologies that don't play well with borders.
         */
        public final boolean allowBorders;
        
        /**
         * If non-zero, signals painter to randomize texture on this surface
         * to be different from and not join with adjacent textures.
         * Use to make cuts into the surface visually distance from adjacent surfaces. 
         */
        public final int textureSalt;
        
        private SurfaceInstance(double uScale, double vScale, boolean ignoreDepthForRandomization, boolean allowBorders, int textureSalt)
        {
            this.uScale = uScale;
            this.vScale = vScale;
            this.ignoreDepthForRandomization = ignoreDepthForRandomization;
            this.allowBorders = allowBorders;
            this.textureSalt = textureSalt;
        }
        
        public SurfaceInstance withScale(double uScale, double vScale)
        {
            return new SurfaceInstance(uScale, vScale, this.ignoreDepthForRandomization, this.allowBorders, this.textureSalt);
        }
        
        public SurfaceInstance withIgnoreDepthForRandomization(boolean ignoreDepthForRandomization)
        {
            return new SurfaceInstance(this.uScale, this.vScale, ignoreDepthForRandomization, this.allowBorders, this.textureSalt);
        }
        
        public SurfaceInstance withAllowBorders(boolean allowBorders)
        {
            return new SurfaceInstance(this.uScale, this.vScale, this.ignoreDepthForRandomization, allowBorders, this.textureSalt);
        }
        
        public SurfaceInstance withTextureSalt(int textureSalt)
        {
            return new SurfaceInstance(this.uScale, this.vScale, this.ignoreDepthForRandomization, this.allowBorders, textureSalt);
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