package grondag.hard_science.superblock.model.state;

import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.varia.BlockSubstance;

/**
 * Less granular representation of block translucency than Translucency.
 * Necessary to limit proliferation of static block states and consumption of block IDs.
 * Used during world lighting to know if this block should block sky light and be part of height map.
 *
 * Basically only three values
 */
public enum WorldLightOpacity
{
    /** blocks no light, not part of height map */
    TRANSPARENT(0),
    
    /** blocks half light, part of height map */
    SHADED(8),
    
    /** blocks all light , part of height map*/
    SOLID(255);
    
    /**
     * 0 means fully transparent
     * values 1-15 are various degrees of opacity
     * 255 means fully opaque
     * values 16-254 have no meaning
     */
    public final int opacity;
    
    private WorldLightOpacity(int opacity)
    {
        this.opacity = opacity;
    }
    
    public static WorldLightOpacity getClosest(BlockSubstance substance, ModelState modelState)
    {
        int substanceOpacity = substance.isTranslucent ? modelState.getTranslucency().blockLightOpacity : 255;
        int blockOpacity = modelState.geometricSkyOcclusion();
        int minOpacity = Math.min(substanceOpacity, blockOpacity);
        
        if(minOpacity == 0) 
            return TRANSPARENT;
        if(minOpacity < 15)
            return SHADED;
        else
            return SOLID;
    }
}
