package grondag.adversity.niceblock.modelstate;

import java.util.ArrayList;

public class ModelStateComponents
{
    // used by registration
    private static int counter = 0;
    private static ArrayList<ModelStateComponent<?,?>> list = new ArrayList<ModelStateComponent<?,?>>(20);

    public static final ModelAxisComponent AXIS = register(new ModelAxisComponent(counter++));
    public static final ModelCornerJoinStateComponent CORNER_JOIN = register(new ModelCornerJoinStateComponent(counter++));

    public static final ModelAlternateComponent ROTATION_INNER_YES = register(new ModelAlternateComponent(counter++, 4));
    public static final ModelAlternateComponent ROTATION_INNER_NO = register(new ModelAlternateComponent(counter++, 1));
    public static final ModelAlternateComponent ROTATION_OUTER_YES = register(new ModelAlternateComponent(counter++, 4));
    public static final ModelAlternateComponent ROTATION_OUTER_NO = register(new ModelAlternateComponent(counter++, 1));
    
    public static final ModelAlternateComponent TEXTURE_INNER_1 = register(new ModelAlternateComponent(counter++, 1));
    public static final ModelAlternateComponent TEXTURE_INNER_2 = register(new ModelAlternateComponent(counter++, 2));
    public static final ModelAlternateComponent TEXTURE_INNER_4 = register(new ModelAlternateComponent(counter++, 4));


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
