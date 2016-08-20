package grondag.adversity.niceblock.modelstate;

public enum ModelStateGroup
{
    OUTER_CJ_SHAREDCOLOR_TEX2(ModelStateComponents.TEXTURE_INNER_2, ModelStateComponents.CORNER_JOIN, ModelStateComponents.ROTATION_OUTER_NO),
    INNER_SHAREDCOLOR_TEX4_ROTATE(ModelStateComponents.TEXTURE_INNER_4, ModelStateComponents.ROTATION_OUTER_YES);
    
    private ModelStateComponent<?,?>[] components;
    
    private ModelStateGroup(ModelStateComponent<?, ?>... components)
    {
        this.components = components;
    }
    
    public ModelStateComponent<?,?>[] getComponents()
    {
        return components;
    }
}
