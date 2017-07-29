package grondag.hard_science.library.render;


import grondag.hard_science.HardScience;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.obj.OBJLoader;

public class HSObjModelLoader implements ICustomModelLoader
{

    public static final HSObjModelLoader INSTANCE = new HSObjModelLoader();
    
    @Override
    public void onResourceManagerReload(IResourceManager resourceManager)
    {
       // relies on OBJLoader for everything so nothing to do
    }

    @Override
    public boolean accepts(ResourceLocation modelLocation)
    {
        return modelLocation.getResourceDomain().equals(HardScience.MODID) 
                && modelLocation.getResourcePath().endsWith(".obj");
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws Exception
    {
        return new HSOBJModelWrapper(OBJLoader.INSTANCE.loadModel(modelLocation));
    }
    
}
