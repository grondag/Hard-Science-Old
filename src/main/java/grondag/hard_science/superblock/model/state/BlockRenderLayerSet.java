package grondag.hard_science.superblock.model.state;

import net.minecraft.util.BlockRenderLayer;

import static net.minecraft.util.BlockRenderLayer.*;

import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;

/** 
 * RenderLayer combinations that are expected to be used most frequently.
 * Used in SuperModelBlock to control which layers are rendered.
 * @author grondag
 *
 */
public enum BlockRenderLayerSet
{
    SOLID_ONLY(SOLID),
    TRANSLUCENT_ONLY(TRANSLUCENT),
    CUTOUT_ONLY(CUTOUT),
    SOLID_AND_TRANSLUCENT(SOLID, TRANSLUCENT),
    SOLID_AND_CUTOUT(SOLID, CUTOUT),
    TRANSLUCENT_AND_CUTOUT(TRANSLUCENT, CUTOUT),
    SOLID_TRANSLUCENT_AND_CUTOUT(SOLID, TRANSLUCENT, CUTOUT),
    ALL(SOLID, TRANSLUCENT, CUTOUT, CUTOUT_MIPPED);
    
    /** maps render flags to the smallest includes enum */
    private final static BlockRenderLayerSet[] setMap = new BlockRenderLayerSet[16];
    
    public final int blockRenderLayerFlags;
    
    static 
    {
        // handle strange case of no flags
        setMap[0] = SOLID_ONLY;

        setMap[ModelStateFactory.ModelState.BENUMSET_RENDER_LAYER.getFlagsForIncludedValues(SOLID)] = SOLID_ONLY;
        setMap[ModelStateFactory.ModelState.BENUMSET_RENDER_LAYER.getFlagsForIncludedValues(CUTOUT)] = CUTOUT_ONLY;
        setMap[ModelStateFactory.ModelState.BENUMSET_RENDER_LAYER.getFlagsForIncludedValues(TRANSLUCENT)] = TRANSLUCENT_ONLY;
        setMap[ModelStateFactory.ModelState.BENUMSET_RENDER_LAYER.getFlagsForIncludedValues(SOLID, TRANSLUCENT)] = SOLID_AND_TRANSLUCENT;
        setMap[ModelStateFactory.ModelState.BENUMSET_RENDER_LAYER.getFlagsForIncludedValues(SOLID, CUTOUT)] = SOLID_AND_CUTOUT;
        setMap[ModelStateFactory.ModelState.BENUMSET_RENDER_LAYER.getFlagsForIncludedValues(TRANSLUCENT, CUTOUT)] = TRANSLUCENT_AND_CUTOUT;
        setMap[ModelStateFactory.ModelState.BENUMSET_RENDER_LAYER.getFlagsForIncludedValues(SOLID, CUTOUT, TRANSLUCENT)] = SOLID_TRANSLUCENT_AND_CUTOUT;
        
        // anything with cutout mipped has to be handled as all
        // try to avoid textures that need this render type for that reason
        setMap[ModelStateFactory.ModelState.BENUMSET_RENDER_LAYER.getFlagsForIncludedValues(CUTOUT_MIPPED)] = ALL;
        setMap[ModelStateFactory.ModelState.BENUMSET_RENDER_LAYER.getFlagsForIncludedValues(CUTOUT_MIPPED, SOLID)] = ALL;
        setMap[ModelStateFactory.ModelState.BENUMSET_RENDER_LAYER.getFlagsForIncludedValues(CUTOUT_MIPPED, CUTOUT)] = ALL;
        setMap[ModelStateFactory.ModelState.BENUMSET_RENDER_LAYER.getFlagsForIncludedValues(CUTOUT_MIPPED, TRANSLUCENT)] = ALL;
        setMap[ModelStateFactory.ModelState.BENUMSET_RENDER_LAYER.getFlagsForIncludedValues(CUTOUT_MIPPED, SOLID, TRANSLUCENT)] = ALL;
        setMap[ModelStateFactory.ModelState.BENUMSET_RENDER_LAYER.getFlagsForIncludedValues(CUTOUT_MIPPED, SOLID, CUTOUT)] = ALL;
        setMap[ModelStateFactory.ModelState.BENUMSET_RENDER_LAYER.getFlagsForIncludedValues(CUTOUT_MIPPED, TRANSLUCENT, CUTOUT)] = ALL;
        setMap[ModelStateFactory.ModelState.BENUMSET_RENDER_LAYER.getFlagsForIncludedValues(CUTOUT_MIPPED, SOLID, CUTOUT, TRANSLUCENT)] = ALL;
    }
    
    private BlockRenderLayerSet(BlockRenderLayer... layers)
    {
        this.blockRenderLayerFlags = ModelStateFactory.ModelState.BENUMSET_RENDER_LAYER.getFlagsForIncludedValues(layers);
    }
    
    /** use this to determine which SuperModelBlock instance is needed to render the given model appropriately */
    public static BlockRenderLayerSet findSmallestInclusiveSet(ModelState modelState)
    {
        return setMap[modelState.getCanRenderInLayerFlags()];
    }
}
