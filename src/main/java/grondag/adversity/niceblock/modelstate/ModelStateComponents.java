package grondag.adversity.niceblock.modelstate;

import java.util.ArrayList;

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
    private static ArrayList<ModelStateComponent<?,?>> list = new ArrayList<ModelStateComponent<?,?>>(20);

    public static final ModelAxisComponent AXIS_DYNAMIC = register(new ModelAxisComponent(counter++, true));
    public static final ModelAxisComponent AXIS_STATIC = register(new ModelAxisComponent(counter++, false));
    
    public static final ModelCornerJoinComponent CORNER_JOIN_DYNAMIC = register(new ModelCornerJoinComponent(counter++, true));
    public static final ModelCornerJoinComponent CORNER_JOIN_STATIC = register(new ModelCornerJoinComponent(counter++, false));

    public static final ModelAlternateComponent ROTATION_INNER_YES = register(new ModelAlternateComponent(counter++, 4));
    public static final ModelAlternateComponent ROTATION_INNER_NO = register(new ModelAlternateComponent(counter++, 1));
    public static final ModelAlternateComponent ROTATION_OUTER_YES = register(new ModelAlternateComponent(counter++, 4));
    public static final ModelAlternateComponent ROTATION_OUTER_NO = register(new ModelAlternateComponent(counter++, 1));
    
    public static final ModelAlternateComponent TEXTURE_INNER_1 = register(new ModelAlternateComponent(counter++, 1));
    public static final ModelAlternateComponent TEXTURE_INNER_2 = register(new ModelAlternateComponent(counter++, 2));
    public static final ModelAlternateComponent TEXTURE_INNER_4 = register(new ModelAlternateComponent(counter++, 4));
    
    public static final ModelBigTexComponent BIG_TEX = register(new ModelBigTexComponent(counter++));

    public static final ModelStateComponent<?,?>[] MODEL_STATE_COMPONENTS;

    
    static
    {
        MODEL_STATE_COMPONENTS = list.toArray(null);
        
        // prevent unintentional use
        list = null;
    }
    
    private static <T extends ModelStateComponent<?,?>> T register(T c)
    {
        list.add(c);
        return c;
    }
}
