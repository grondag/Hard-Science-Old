package grondag.adversity.niceblock.base;

import java.util.List;

import grondag.adversity.niceblock.base.ModelFactory2.ModelInputs;
import grondag.adversity.niceblock.color.IColorMapProvider;
import grondag.adversity.niceblock.modelstate.ModelColorMapComponent;
import grondag.adversity.niceblock.modelstate.ModelRotationComponent;
import grondag.adversity.niceblock.modelstate.ModelState;
import grondag.adversity.niceblock.modelstate.ModelStateComponent;
import grondag.adversity.niceblock.modelstate.ModelStateGroup;
import grondag.adversity.niceblock.modelstate.ModelTextureComponent;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.event.ModelBakeEvent;

/**

Generates and BakedModel instances
for a given controller/state instance.

May cache some pre-baked elements for performance optimization.
getBlockModelForVariant()
getItemQuadsForVariant()
handleBake()


TODO: remove super inheritance and references
 */
public abstract class ModelFactory2
{
    protected final ModelStateGroup stateGroup;
    protected final ModelColorMapComponent colorComponent;
    protected final ModelTextureComponent textureComponent;
    protected final ModelInputs modelInputs;
    protected final ModelRotationComponent rotationComponent;

    
    public ModelFactory2(ModelInputs modelInputs, ModelStateComponent<?,?>... components)
    {
        ModelStateGroup.find(colorComponent, textureComponent, rotationComponent)
        
        this.stateGroup = stateGroup;
        this.colorComponent = colorComponent;
        this.textureComponent = textureComponent;
        this.rotationComponent = rotationComponent;
        this.modelInputs = modelInputs;
    }
    
    public ModelStateGroup getStateGroup() { return stateGroup; }
    
    public abstract List<BakedQuad> getFaceQuads(ModelState modelState, EnumFacing face);
    public abstract List<BakedQuad> getItemQuads(ModelState modelState);
    
    public void handleBakeEvent(ModelBakeEvent event)
    {
        // NOOP: default implementation assumes lazy baking
    }
    
    /**
     * identifies all textures needed for texture stitch
     */
    public String[] getAllTextureNames()
    {
        final String retVal[] = new String[getAlternateTextureCount() * textureCount];

        for (int i = 0; i < getAlternateTextureCount() * textureCount; i++)
        {
            retVal[i] = getTextureName(i);
        }
        return retVal;
    }
    
    public static String buildTextureName(String baseName, int offset)
    {
        return "adversity:blocks/" + baseName + "_" + (offset >> 3) + "_" + (offset & 7);
    }
    
    public static class ModelInputs
    {
        public final String textureName;
        public final boolean isShaded;
        public final BlockRenderLayer renderLayer;
        
        public ModelInputs(String textureName, boolean isShaded, BlockRenderLayer renderLayer)
        {
            this.textureName = textureName;
            this.isShaded = isShaded;
            this.renderLayer = renderLayer;
        }
    }
}
