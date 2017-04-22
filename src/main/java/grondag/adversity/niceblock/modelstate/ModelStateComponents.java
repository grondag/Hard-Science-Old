package grondag.adversity.niceblock.modelstate;

import grondag.adversity.niceblock.base.ModelAppearanceLists;
import grondag.adversity.niceblock.color.BlockColorMapProvider;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.color.FixedColorMapProvider;
import grondag.adversity.niceblock.color.HueSet.Chroma;
import grondag.adversity.niceblock.color.HueSet.Luminance;
import grondag.adversity.niceblock.color.NoColorMapProvider;
import grondag.adversity.niceblock.color.NiceHues.Hue;
import grondag.adversity.niceblock.modelstate.ModelStateComponent.WorldRefreshType;
import grondag.adversity.niceblock.support.BlockTests;

/**
 * axis
 * alternate texture index - could be different for diff layers
 * corner join state
 * big texture model index
 * simple alternate index (hot basalt, for example)
 * base color
 * glow color
 * border color
 * highlight color
 * simple join state

 * flow join state
 * height state

 * primitive offset

 * underlying primitive (probably determined by the block)
 * 
 * 
 * views of model state
 * controller - will use a subset, but can have access to whole state
 * dispatcher - needs a cache key based on visual appearance - get from state or controller?
 * niceblock/plus - needs to persist the parts of the state that should be persisted
 *         - can vary based on type of block with same controller (flowing vs. static lava)
 *         - persistence options are meta, world-derived, NBT, cached)
 * multipart/CSG - will have need to persist full state to NBT even if originally not
 * StateProvider - obtains state instance from a key or from appropriate persistence locale
 * 
 * column - axis (meta), color, altTex, cornerJoin
 * bigTex - color, metaVariant (optional), bigTex index
 * border - color, altTex*2, cornerJoin, (meta used to derive cornerjoin but not part of state)
 * color - color, altTex
 * flow - altTex, flowState (meta is used/implied by flowState), color?
 * height - altTex, height (meta), color?
 * masonry - color, altTex*2, simpleJoin
 * cylinder - color, offset, radius, length, cornerOrCenter
 * 
 * some blocks could have different alternate textures per controller
 */


public class ModelStateComponents
{
    // used by registration
    private static int counter = 0;
    private static final ModelStateComponent<?,?>[] MODEL_STATE_COMPONENTS = new ModelStateComponent<?,?>[45];

    public static final ModelAxisComponent AXIS = register(new ModelAxisComponent(counter++));
    

    public static final ModelRotationComponent ROTATION = register(new ModelRotationComponent(counter++));
    public static final ModelRotationComponent ROTATION_NONE = register(new ModelRotationComponent(counter++, true));
    
    public static final ModelTextureVersionComponent TEXTURE_1 = register(new ModelTextureVersionComponent(counter++, 1));
    public static final ModelTextureVersionComponent TEXTURE_2 = register(new ModelTextureVersionComponent(counter++, 2));
    public static final ModelTextureVersionComponent TEXTURE_4 = register(new ModelTextureVersionComponent(counter++, 4));
    
    public static final ModelBigTexComponent BIG_TEX_META_VARIED = register(new ModelBigTexComponent(counter++, true));
    public static final ModelBigTexComponent BIG_TEX_IGNORE_META = register(new ModelBigTexComponent(counter++, false));

    public static final ModelFlowTexComponent FLOW_TEX = register(new ModelFlowTexComponent(counter++));
    
    public static final ModelSpeciesComponent SPECIES_4 = register(new ModelSpeciesComponent(counter++, 4));
    public static final ModelSpeciesComponent SPECIES_16 = register(new ModelSpeciesComponent(counter++, 16));
    
    public static final ModelCornerJoinComponent CORNER_JOIN = register(new ModelCornerJoinComponent(counter++, BlockTests.BIG_BLOCK_MATCH));
    public static final ModelSimpleJoinComponent MASONRY_JOIN = register(new ModelSimpleJoinComponent(counter++, BlockTests.MASONRY_MATCH));
    public static final ModelFlowJoinComponent FLOW_JOIN = register(new ModelFlowJoinComponent(counter++, WorldRefreshType.CACHED));


    public static final ModelColorMapComponent COLORS_BLOCK = register(new ModelColorMapComponent(counter++, WorldRefreshType.NEVER, BlockColorMapProvider.INSTANCE));
    public static final ModelColorMapComponent COLORS_WHITE = register(new ModelColorMapComponent(counter++, WorldRefreshType.NEVER,
            NoColorMapProvider.INSTANCE));
    public static final ModelColorMapComponent COLORS_BASALT = register(new ModelColorMapComponent(counter++, WorldRefreshType.NEVER,
            new FixedColorMapProvider(ColorMap.makeColorMap(Hue.COBALT, Chroma.NEUTRAL, Luminance.MEDIUM_DARK, 0))));
    
    public static final ModelAppearanceComponent APPERANCE_BLOCK_SOLID = new ModelAppearanceComponent(counter++, WorldRefreshType.NEVER, ModelAppearanceLists.BLOCK_SOLID);
    
    public static int getCount()
    {
        return counter;
    }
    
    public static ModelStateComponent<?,?> get(int index)
    {
        return MODEL_STATE_COMPONENTS[index];
    }
    
    private static <T extends ModelStateComponent<?,?>> T register(T c)
    {
        MODEL_STATE_COMPONENTS[c.getOrdinal()] = c;
        return c;
    }
}
