package grondag.hard_science.superblock.block;

import grondag.exotic_matter.ExoticMatter;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

public class SuperModelLoader implements ICustomModelLoader
{

    public final static SuperModelLoader INSTANCE = new SuperModelLoader();
    
    private SuperModelLoader() {};
    
    @Override
    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        SuperDispatcher.INSTANCE.clear();
    }

    /**
     * Note that SuperStateMapper maps all ISuperBlock states to models with the library mod resource domain. The mod of the
     * block is not used be or known to the dispatcher.  Thus, the only mod we need to check for is the library mod.
     */
    @Override
    public boolean accepts(ResourceLocation modelLocation)
    {
        return modelLocation.getResourceDomain().equals(ExoticMatter.MODID) && modelLocation.getResourcePath().contains(SuperDispatcher.RESOURCE_BASE_NAME);
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws Exception
    {
        return SuperDispatcher.INSTANCE.getDelegate(modelLocation.getResourcePath());
    }

}
