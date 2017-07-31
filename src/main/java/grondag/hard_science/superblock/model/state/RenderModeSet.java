package grondag.hard_science.superblock.model.state;

import static grondag.hard_science.superblock.model.state.RenderMode.*;

import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.util.BlockRenderLayer;

/** 
 * RenderLayer combinations that are expected to be used most frequently.
 * Used in SuperModelBlock to control which layers are rendered.
 */
public enum RenderModeSet
{
    SOLID_ONLY(SOLID_SHADED),
    TRANSLUCENT_ONLY(TRANSLUCENT_SHADED),
    SOLID_AND_TRANSLUCENT(SOLID_SHADED, TRANSLUCENT_SHADED),
    SOLID_AND_TRANSLUCENT_TESR(SOLID_SHADED, TRANSLUCENT_TESR),
    SOLID_TESR_AND_TRANSLUCENT(SOLID_TESR, TRANSLUCENT_SHADED),
    TRANSLUCENT_AND_TRANSLUCENT_TESR(TRANSLUCENT_SHADED, TRANSLUCENT_TESR),
    TESR_ONLY(SOLID_TESR, TRANSLUCENT_TESR),
    ALL(SOLID_SHADED, TRANSLUCENT_SHADED, SOLID_TESR, TRANSLUCENT_TESR);
    
    /** maps render flags to the smallest inclusive enum */
    private final static RenderModeSet[] setMap = new RenderModeSet[16];
    
    public final int renderModeFlags;
    
    public final boolean hasTESR;
    
    private final boolean[] canRenderInLayer = new boolean[BlockRenderLayer.values().length];
    
    private final boolean [] includesMode = new boolean[RenderMode.values().length]; 
    
    
    static 
    {
        // handle strange case of no flags
        setMap[0] = SOLID_ONLY;

        setMap[ModelStateFactory.ModelState.BENUMSET_RENDER_MODE.getFlagsForIncludedValues(SOLID_SHADED)] = SOLID_ONLY;
        setMap[ModelStateFactory.ModelState.BENUMSET_RENDER_MODE.getFlagsForIncludedValues(TRANSLUCENT_SHADED)] = TRANSLUCENT_ONLY;
        setMap[ModelStateFactory.ModelState.BENUMSET_RENDER_MODE.getFlagsForIncludedValues(SOLID_SHADED, TRANSLUCENT_SHADED)] = SOLID_AND_TRANSLUCENT;
        setMap[ModelStateFactory.ModelState.BENUMSET_RENDER_MODE.getFlagsForIncludedValues(SOLID_SHADED, TRANSLUCENT_TESR)] = SOLID_AND_TRANSLUCENT_TESR;
        setMap[ModelStateFactory.ModelState.BENUMSET_RENDER_MODE.getFlagsForIncludedValues(SOLID_TESR, TRANSLUCENT_SHADED)] = SOLID_TESR_AND_TRANSLUCENT;
        setMap[ModelStateFactory.ModelState.BENUMSET_RENDER_MODE.getFlagsForIncludedValues(TRANSLUCENT_SHADED, TRANSLUCENT_TESR)] = TRANSLUCENT_AND_TRANSLUCENT_TESR;
        
        setMap[ModelStateFactory.ModelState.BENUMSET_RENDER_MODE.getFlagsForIncludedValues(SOLID_TESR, TRANSLUCENT_TESR)] = TESR_ONLY;
        setMap[ModelStateFactory.ModelState.BENUMSET_RENDER_MODE.getFlagsForIncludedValues(SOLID_TESR)] = TESR_ONLY;
        setMap[ModelStateFactory.ModelState.BENUMSET_RENDER_MODE.getFlagsForIncludedValues(TRANSLUCENT_TESR)] = TESR_ONLY;
        
        setMap[ModelStateFactory.ModelState.BENUMSET_RENDER_MODE.getFlagsForIncludedValues(SOLID_SHADED, SOLID_TESR)] = ALL;
        setMap[ModelStateFactory.ModelState.BENUMSET_RENDER_MODE.getFlagsForIncludedValues(SOLID_SHADED, TRANSLUCENT_SHADED, SOLID_TESR)] = ALL;
        setMap[ModelStateFactory.ModelState.BENUMSET_RENDER_MODE.getFlagsForIncludedValues(SOLID_SHADED, TRANSLUCENT_SHADED, TRANSLUCENT_TESR)] = ALL;
        setMap[ModelStateFactory.ModelState.BENUMSET_RENDER_MODE.getFlagsForIncludedValues(SOLID_SHADED, SOLID_TESR, TRANSLUCENT_TESR)] = ALL;
        setMap[ModelStateFactory.ModelState.BENUMSET_RENDER_MODE.getFlagsForIncludedValues(TRANSLUCENT_SHADED, SOLID_TESR, TRANSLUCENT_TESR)] = ALL;
        setMap[ModelStateFactory.ModelState.BENUMSET_RENDER_MODE.getFlagsForIncludedValues(SOLID_SHADED, TRANSLUCENT_SHADED, SOLID_TESR, TRANSLUCENT_TESR)] = ALL;
    }
    
    private RenderModeSet(RenderMode... modes)
    {
        this.renderModeFlags = ModelStateFactory.ModelState.BENUMSET_RENDER_MODE.getFlagsForIncludedValues(modes);
        boolean needsTESR = false;
        for(RenderMode mode : modes)
        {
            this.includesMode[mode.ordinal()] = true;
            this.canRenderInLayer[mode.renderLayer.ordinal()] = true;
            if(mode.needsTESR) needsTESR = true;
        }
        this.hasTESR = needsTESR;
     }
    
    /** use this to determine which SuperModelBlock instance is needed to render the given model appropriately */
    public static RenderModeSet findSmallestInclusiveSet(ModelState modelState)
    {
        return setMap[modelState.getRenderModeFlags()];
    }
    
    public boolean canRenderInLayer(BlockRenderLayer layer)
    {
        return this.canRenderInLayer[layer.ordinal()];
    }

    public boolean includes(RenderMode mode)
    {
        return this.includesMode[mode.ordinal()];
    }
}
