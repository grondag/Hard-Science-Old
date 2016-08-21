package grondag.adversity.niceblock.base;

import java.util.List;

import grondag.adversity.niceblock.color.IColorMapProvider;
import grondag.adversity.niceblock.modelstate.ModelState;
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
public abstract class ModelFactory
{
    protected final ModelController controller;
    
    public ModelFactory(ModelController controller)
    {
        this.controller = controller;
    }
    
    public abstract List<BakedQuad> getFaceQuads(ModelState modelState, IColorMapProvider colorProvider, EnumFacing face);
    public abstract List<BakedQuad> getItemQuads(ModelState modelState, IColorMapProvider colorProvider);
    
    public void handleBakeEvent(ModelBakeEvent event)
    {
        // NOOP: default implementation assumes lazy baking
    }
    
}
