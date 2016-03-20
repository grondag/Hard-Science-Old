package grondag.adversity.niceblock.newmodel;

import grondag.adversity.niceblock.newmodel.color.IColorProvider;

import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;
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
    
    public abstract List<BakedQuad> getFaceQuads(ModelState modelState, IColorProvider colorProvider, EnumFacing face);
    public abstract List<BakedQuad> getItemQuads(ModelState modelState, IColorProvider colorProvider);
    
    public void handleBakeEvent(ModelBakeEvent event)
    {
        // NOOP: default implementation assumes lazy baking
    }
    
}
