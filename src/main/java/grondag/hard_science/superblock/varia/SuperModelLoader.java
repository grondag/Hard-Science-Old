package grondag.hard_science.superblock.varia;

import grondag.hard_science.HardScience;
import grondag.hard_science.init.ModModels;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

public class SuperModelLoader implements ICustomModelLoader
{

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        //NOOP
    }

    @Override
    public boolean accepts(ResourceLocation modelLocation)
    {
        return modelLocation.getResourceDomain().equals(HardScience.MODID) && modelLocation.getResourcePath().contains(SuperDispatcher.RESOURCE_BASE_NAME);
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws Exception
    {
        return ModModels.MODEL_DISPATCH.getDelegate(modelLocation.getResourcePath());
    }

}
