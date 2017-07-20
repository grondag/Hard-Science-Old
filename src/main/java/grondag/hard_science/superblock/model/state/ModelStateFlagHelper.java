package grondag.hard_science.superblock.model.state;

import static grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState.*;

import grondag.hard_science.superblock.model.shape.ShapeMeshGenerator;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.texture.TexturePalletteRegistry.TexturePallette;
import grondag.hard_science.superblock.texture.Textures;

/**
 * Populates state flags for a given model state.
 * 
 * Most important function is to encapsulates rules 
 * for shading mode and render layer selection
 * for each paint layer based on limitations of MC rendering engine.
 * 
 * Overlay paint layers always render on translucent layer when present.
 * 
 * If any paint layer rendering in translucency is full brightness,
 * then all paint layers rendering in translucency must be full brightness, 
 * because all will use the flat lighter.
 * 
 * For Base/Cut and Lamp layers:
 * 
 *  If one is translucent, the other renders on solid.
 *  If none are translucent and none are glowing, both render on solid
 *  If none are translucent and one is glowing, try to render one on cutout and other on solid
 *      Except: if neither can be rendered cutout render both solid - both will glow
 *      
 * Results are returns as STATE_FLAG_XXXX values from ModelState
 * for easy persistence and usage within that class.     
 */
public class ModelStateFlagHelper
{
    // each of these flags represents an input to the selection logic
    // added together they find the appropriate result flags in a lookup
    // table that is constructed in static{}.
    
    /** True if base paint layer is translucent */
    private static final int IS_BASE_TRANSLUCENT = 1;
    
    /** True if base paint layer is full brightness */
    private static final int IS_BASE_LIT = IS_BASE_TRANSLUCENT << 1;
    
    /** True if base paint layer texture support cutout layer for rendering */
    private static final int IS_BASE_CUTOUT_OK = IS_BASE_LIT << 1;
    
    /** True if lamp paint layer surface is present in the model */
    private static final int IS_LAMP_PRESENT = IS_BASE_CUTOUT_OK << 1;
    
    /** True if lamp paint layer surface is translucent */
    private static final int IS_LAMP_TRANSLUCENT = IS_LAMP_PRESENT << 1;

    /** True if lamp paint layer surface is full brightness */
    private static final int IS_LAMP_LIT = IS_LAMP_TRANSLUCENT << 1;
    
    /** True if lamp paint layer texture support cutout layer for rendering */
    private static final int IS_LAMP_CUTOUT_OK = IS_LAMP_LIT << 1;
    
    /** True if middle paint layer is enabled */
    private static final int IS_MIDDLE_PRESENT = IS_LAMP_CUTOUT_OK << 1;
    
    /** True if middle paint layer is full brightness */
    private static final int IS_MIDDLE_LIT = IS_MIDDLE_PRESENT << 1;
    
    /** True if outer paint layer is enabled */
    private static final int IS_OUTER_PRESENT = IS_MIDDLE_LIT << 1;
    
    /** True if outer paint layer is full brightness */
    private static final int IS_OUTER_LIT = IS_OUTER_PRESENT << 1;
    
    private static final int COMBINATION_COUNT = IS_OUTER_LIT << 1;
    
    private static int[] RESULTS = new int[COMBINATION_COUNT];
    
    static
    {
        
        for(int i = 0; i < COMBINATION_COUNT; i++)
        {
            int result = 0;
            
            boolean isBaseTranslucent = (i & IS_BASE_TRANSLUCENT) != 0;
            boolean isBaseLit = (i & IS_BASE_LIT) != 0;
            boolean isBaseCutoutOK = (i & IS_BASE_CUTOUT_OK) != 0;
            boolean isLampPresent = (i & IS_LAMP_PRESENT) != 0;
            boolean isLampTranslucent = (i & IS_LAMP_TRANSLUCENT) != 0;
            boolean isLampLit = (i & IS_LAMP_LIT) != 0;
            boolean isLampCutoutOK = (i & IS_LAMP_CUTOUT_OK) != 0;
            boolean isMiddlePresent = (i & IS_MIDDLE_PRESENT) != 0;
            boolean isMiddleLit = (i & IS_MIDDLE_LIT) != 0;
            boolean isOuterPresent = (i & IS_OUTER_PRESENT) != 0;
            boolean isOuterLit = (i & IS_OUTER_LIT) != 0;
            
            
            boolean isLampCutout = isLampPresent && !isBaseTranslucent && !isLampTranslucent && isLampCutoutOK && (isBaseLit != isLampLit);
            if(isLampCutout) result |= STATE_FLAG_IS_LAMP_CUTOUT;
                    
            boolean isBaseCutout = !isLampCutout && !isBaseTranslucent && !isLampTranslucent && isBaseCutoutOK && (isBaseLit != isLampLit);
            if(isBaseCutout) result |= STATE_FLAG_IS_LAMP_CUTOUT;
            
            boolean isLampSolid = isLampPresent && !isLampTranslucent && !isLampCutout;
            boolean isBaseSolid = !isBaseTranslucent && !isBaseCutout;

            boolean isSolidPresent = isLampSolid || isBaseSolid;
            if(isSolidPresent) result |= STATE_FLAG_HAS_RENDER_LAYER_SOLID;
            
            boolean isSolidLit = (isLampSolid && isLampLit) || (isBaseSolid && isBaseLit);
            if(isSolidLit) result |= STATE_FLAG_FULLBRIGHT_RENDER_LAYER_SOLID;
            
            boolean isCutoutPresent = isLampCutout || isBaseCutout;
            if(isCutoutPresent) result |= STATE_FLAG_HAS_RENDER_LAYER_CUTOUT_MIPPED;
            
            boolean isCutoutLit = (isLampCutout && isLampLit) || (isBaseCutout && isBaseLit);
            if(isCutoutLit) result |= STATE_FLAG_FULLBRIGHT_RENDER_LAYER_CUTOUT_MIPPED;
           
            boolean hasTranslucent = isBaseTranslucent || (isLampPresent && isLampTranslucent) || isMiddlePresent || isOuterPresent;
            if(hasTranslucent) result |= STATE_FLAG_HAS_RENDER_LAYER_TRANSLUCENT;
            
            boolean isTranslucentLit = (isMiddlePresent && isMiddleLit) || (isOuterPresent && isOuterLit) || (isBaseTranslucent && isBaseLit)
                    || (isLampPresent && isLampTranslucent && isLampLit);
            if(isTranslucentLit) result |= STATE_FLAG_FULLBRIGHT_RENDER_LAYER_TRANSLUCENT;
            
            if(isBaseTranslucent || (isLampPresent && isLampTranslucent))
                result |= STATE_FLAG_HAS_TRANSLUCENT_GEOMETRY;
 
            RESULTS[i] = result;
        }
    }
    
    public static int getFlags(ModelState state)
    {
        int index = 0;
        
        ShapeMeshGenerator mesh = state.getShape().meshFactory();
        
        int flags = ModelState.STATE_FLAG_IS_POPULATED | mesh.stateFlags;
        
        TexturePallette texBase = state.getTexture(PaintLayer.BASE);
        flags |= texBase.stateFlags;
        
        flags |= state.getTexture(PaintLayer.CUT).stateFlags;
        
        if(state.isTranslucent(PaintLayer.BASE)) index |= IS_BASE_TRANSLUCENT;
        
        if(state.isFullBrightness(PaintLayer.BASE)) index |= IS_BASE_LIT;
        
        if(mesh.hasLampSurface(state)) 
        {
            index |= IS_LAMP_PRESENT;
            
            // don't need to consider base cutout if no lamp surface
            if(texBase.renderIntent.canRenderAsBaseInCutoutLayer) index |= IS_BASE_CUTOUT_OK;

            TexturePallette texLamp = state.getTexture(PaintLayer.LAMP);
            flags |= texLamp.stateFlags;
            
            if(state.isTranslucent(PaintLayer.LAMP)) index |= IS_LAMP_TRANSLUCENT;
            if(state.isFullBrightness(PaintLayer.LAMP)) index |= IS_LAMP_LIT;
            if(texLamp.renderIntent.canRenderAsBaseInCutoutLayer) index |= IS_LAMP_CUTOUT_OK;
        }
        
        TexturePallette texOverlay = state.getTexture(PaintLayer.MIDDLE);
        if(texOverlay != Textures.NONE)
        {
            flags |= texOverlay.stateFlags;
            index |= IS_MIDDLE_PRESENT;
            if(state.isFullBrightness(PaintLayer.MIDDLE)) index |= IS_MIDDLE_LIT;
        }

        texOverlay = state.getTexture(PaintLayer.OUTER);
        if(texOverlay != Textures.NONE)
        {
            flags |= texOverlay.stateFlags;
            index |= IS_OUTER_PRESENT;
            if(state.isFullBrightness(PaintLayer.OUTER)) index |= IS_OUTER_LIT;
        }
        
        flags |= RESULTS[index];
        
        // turn off this.stateFlags that don't apply to non-block formats if we aren't one
        if(mesh.stateFormat != StateFormat.BLOCK)
        {
            flags &= ModelState.STATE_FLAG_DISABLE_BLOCK_ONLY;
        }
        
        return flags;
    }
}
