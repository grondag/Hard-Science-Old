package grondag.adversity.niceblock.newmodel;

import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraftforge.client.event.ModelBakeEvent;

/**

Generates and BakedModel instances
for a given controller/state instance.

May cache some pre-baked elements for performance optimization.
getBlockModelForVariant()
getItemQuadsForVariant()
handleBake()

 */
public abstract class BakedModelFactory
{
    protected final ModelControllerNew controller;
    
    public BakedModelFactory(ModelControllerNew controller)
    {
        this.controller = controller;
    }
    
    public abstract IBakedModel getBlockModel(ModelState modelState);
    public abstract List<BakedQuad> getItemQuads(ModelState modelState);
    public abstract void handleBakeEvent(ModelBakeEvent event);
    
}
