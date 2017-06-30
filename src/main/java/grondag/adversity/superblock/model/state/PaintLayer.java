package grondag.adversity.superblock.model.state;

import net.minecraft.util.text.translation.I18n;

public enum PaintLayer
{
    
    /** 
     * Textures the MAIN surface.  
     * Must always be present.
     */
    BASE(0, SurfaceType.MAIN),
    
    /**
     * Textures the MAIN and CUT surfaces.  
     * Provides dirt or other character to a base layer.
     * Optional.
     * Middle z-position when other layers are present.
     * Will generally be partial quads, translucent or clipped.
     * Individual quads can be solid or translucent, shaded or lit.
     * Has separate color.
     */
    MIDDLE(1, SurfaceType.MAIN),
    
    /**
      * Textures the MAIN surface only.  
     * Provides dirt or other character to a base layer.
     * Optional.
     * Outer z-position when other layers are present.
     * Will generally be partial quads, translucent or clipped.
     * Individual quads can be solid or translucent, shaded or lit.
     * Has separate color.
     */
    OUTER(2, SurfaceType.MAIN),
    
    /**
     * Textures the LAMP surface.  
     * Optional.
     * Middle z-position.
     * Has separate color.
     */
    LAMP(3, SurfaceType.LAMP),
    
    /**
     * Textures the CUT surface. 
     * Same texture and color as base layer but without any overlay or detail applied.
     * Must always be present.
     */
    CUT(0, SurfaceType.CUT);
    
    /** see {@link #dynamicIndex} */
    public static final int DYNAMIC_SIZE = 4;

    /** Does not include the CUT layer */
    public static final PaintLayer DYNAMIC_VALUES[];
//    public 
    
    /** slightly more convenient than values().length, also more clear - includes CUT in addition to dynamic values*/
    public static final int STATIC_SIZE;
    
    /** Does include the CUT layer. Sane as values(), but more clear. */
    public static final PaintLayer STATIC_VALUES[];
    static
    {
        STATIC_SIZE = values().length;
        
        DYNAMIC_VALUES = new PaintLayer[DYNAMIC_SIZE];
        DYNAMIC_VALUES[BASE.ordinal()] = BASE;
        DYNAMIC_VALUES[MIDDLE.ordinal()] = MIDDLE;
        DYNAMIC_VALUES[OUTER.ordinal()] = OUTER;
        DYNAMIC_VALUES[LAMP.ordinal()] = LAMP;
        
        STATIC_VALUES = values();
    }
    
    /** 
     * There are five layers, but BASE and CUT share the same color, lighting etc.
     * So when enumerating those attributes we only have four.
     * Use this to index which of those four apply to this value.
     */
    public final int dynamicIndex;
    
    public final SurfaceType surfaceType;
    
    private PaintLayer(int colorIndex, SurfaceType surfaceType)
    {
        this.dynamicIndex = colorIndex;
        this.surfaceType = surfaceType;
    }
    
    @SuppressWarnings("deprecation")
    public String localizedName()
    {
        return I18n.translateToLocal("paintlayer." + this.name().toLowerCase());
    }
    
}
