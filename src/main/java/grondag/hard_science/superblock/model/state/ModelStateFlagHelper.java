package grondag.hard_science.superblock.model.state;

import static grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState.*;

import grondag.exotic_matter.render.RenderPass;
import grondag.hard_science.superblock.model.shape.ShapeMeshGenerator;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.texture.TexturePalletteRegistry.TexturePallette;
import grondag.hard_science.superblock.texture.Textures;

/**
 * Populates state flags for a given model state.
 * 
 * Most important function is to encapsulates rules 
 * for render mode selection for each paint layer.
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
    
    /** True if lamp paint layer surface is present in the model */
    private static final int IS_LAMP_PRESENT = IS_BASE_LIT << 1;
    
    /** True if lamp paint layer surface is translucent */
    private static final int IS_LAMP_TRANSLUCENT = IS_LAMP_PRESENT << 1;

    /** True if lamp paint layer surface is full brightness */
    private static final int IS_LAMP_LIT = IS_LAMP_TRANSLUCENT << 1;
    
    /** True if middle paint layer is enabled */
    private static final int IS_MIDDLE_PRESENT = IS_LAMP_LIT << 1;
    
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
            boolean isLampPresent = (i & IS_LAMP_PRESENT) != 0;
            boolean isLampTranslucent = (i & IS_LAMP_TRANSLUCENT) != 0;
            boolean isLampLit = (i & IS_LAMP_LIT) != 0;
            boolean isMiddlePresent = (i & IS_MIDDLE_PRESENT) != 0;
            boolean isMiddleLit = (i & IS_MIDDLE_LIT) != 0;
            boolean isOuterPresent = (i & IS_OUTER_PRESENT) != 0;
            boolean isOuterLit = (i & IS_OUTER_LIT) != 0;
            
            
            boolean isLampSolid = isLampPresent && !isLampTranslucent;
            boolean isBaseSolid = !isBaseTranslucent;
            
            int renderPassFlags = 0;
            
            if((isBaseSolid && !isBaseLit) || (isLampSolid && !isLampLit))
            {
                renderPassFlags = RenderLayout.BENUMSET_RENDER_PASS.setFlagForValue(RenderPass.SOLID_SHADED, renderPassFlags, true); 
            }
           
            if((isBaseSolid && isBaseLit) || (isLampSolid && isLampLit))
            {
                renderPassFlags = RenderLayout.BENUMSET_RENDER_PASS.setFlagForValue(RenderPass.SOLID_FLAT, renderPassFlags, true); 
            }
            
            if((!isBaseSolid && !isBaseLit) || (isLampPresent && isLampTranslucent && !isLampLit) || (isMiddlePresent &&  !isMiddleLit) || (isOuterPresent && !isOuterLit))
            {
                renderPassFlags = RenderLayout.BENUMSET_RENDER_PASS.setFlagForValue(RenderPass.TRANSLUCENT_SHADED, renderPassFlags, true); 
            }
            
            if((!isBaseSolid && isBaseLit) || (isLampPresent && isLampTranslucent && isLampLit) || (isMiddlePresent &&  isMiddleLit) || (isOuterPresent && isOuterLit))
            {
                renderPassFlags = RenderLayout.BENUMSET_RENDER_PASS.setFlagForValue(RenderPass.TRANSLUCENT_FLAT, renderPassFlags, true); 
            }
            
            RenderPassSet rps = RenderPassSet.findByFlags(renderPassFlags);
            
            result = (int) ModelStateFactory.ModelState.STATE_ENUM_RENDER_PASS_SET.setValue(rps, result);
            
            if(isBaseTranslucent || (isLampPresent && isLampTranslucent))
                result |= STATE_FLAG_HAS_TRANSLUCENT_GEOMETRY;
 
            RESULTS[i] = result;
        }
    }
    
    public static int getFlags(ModelState state)
    {
        int index = 0;
        
        ShapeMeshGenerator mesh = state.getShape().meshFactory();
        
        int flags = ModelState.STATE_FLAG_IS_POPULATED | mesh.getStateFlags(state);
        
        TexturePallette texBase = state.getTexture(PaintLayer.BASE);
        flags |= texBase.stateFlags;
        
        flags |= state.getTexture(PaintLayer.CUT).stateFlags;
        
        if(state.isTranslucent(PaintLayer.BASE)) index |= IS_BASE_TRANSLUCENT;
        
        if(state.isFullBrightness(PaintLayer.BASE)) index |= IS_BASE_LIT;
        
        if(mesh.hasLampSurface(state)) 
        {
            index |= IS_LAMP_PRESENT;
      
            TexturePallette texLamp = state.getTexture(PaintLayer.LAMP);
            flags |= texLamp.stateFlags;
            
            if(state.isTranslucent(PaintLayer.LAMP)) index |= IS_LAMP_TRANSLUCENT;
            if(state.isFullBrightness(PaintLayer.LAMP)) index |= IS_LAMP_LIT;

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
