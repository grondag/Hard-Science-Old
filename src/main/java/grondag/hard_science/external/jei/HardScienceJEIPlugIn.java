package grondag.hard_science.external.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;

@JEIPlugin
public class HardScienceJEIPlugIn implements IModPlugin
{

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry)
    {
        IModPlugin.super.registerCategories(registry);
    }

}
