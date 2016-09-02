package grondag.adversity.niceblock.base;

import java.util.List;

import grondag.adversity.library.model.QuadContainer2;
import grondag.adversity.niceblock.modelstate.ModelBigTexComponent;
import grondag.adversity.niceblock.modelstate.ModelColorMapComponent;
import grondag.adversity.niceblock.modelstate.ModelFlowJoinComponent;
import grondag.adversity.niceblock.modelstate.ModelRotationComponent;
import grondag.adversity.niceblock.modelstate.ModelSpeciesComponent;
import grondag.adversity.niceblock.modelstate.ModelStateComponent;
import grondag.adversity.niceblock.modelstate.ModelStateGroup;
import grondag.adversity.niceblock.modelstate.ModelStateSet.ModelStateSetValue;
import grondag.adversity.niceblock.modelstate.ModelTextureComponent;
import grondag.adversity.niceblock.support.ICollisionHandler;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.BlockRenderLayer;
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
public abstract class ModelFactory2<V extends ModelFactory2.ModelInputs>
{
    protected final ModelStateGroup stateGroup;
    protected final V modelInputs;
    protected final ModelColorMapComponent colorComponent;
    protected final ModelTextureComponent textureComponent;
    protected final ModelRotationComponent rotationComponent;
    protected final ModelBigTexComponent bigTexComponent;
    protected final ModelSpeciesComponent speciesComponent;
    protected final ModelFlowJoinComponent flowJoinComponent;
    
    public ModelFactory2(V modelInputs, ModelStateComponent<?,?>... components)
    {
        this.stateGroup = ModelStateGroup.find(components);
        this.modelInputs = modelInputs;

        ModelColorMapComponent colorComponent = null;
        ModelTextureComponent textureComponent = null;
        ModelRotationComponent rotationComponent = null;
        ModelBigTexComponent bigTexComponent = null;
        ModelSpeciesComponent speciesComponent = null;
        ModelFlowJoinComponent flowJoinComponent = null;
        
        for(ModelStateComponent<?,?> c : components)
        {
            if(c instanceof ModelColorMapComponent)
                colorComponent = (ModelColorMapComponent) c;
            else if(c instanceof ModelTextureComponent)
                textureComponent = (ModelTextureComponent) c;
            else if(c instanceof ModelRotationComponent)
                rotationComponent = (ModelRotationComponent) c;
            else if(c instanceof ModelBigTexComponent)
                bigTexComponent = (ModelBigTexComponent) c;
            else if(c instanceof ModelSpeciesComponent)
                speciesComponent = (ModelSpeciesComponent) c;
            else if(c instanceof ModelFlowJoinComponent)
                flowJoinComponent = (ModelFlowJoinComponent) c;
        }
        
        this.colorComponent = colorComponent;
        this.textureComponent = textureComponent;
        this.rotationComponent = rotationComponent;
        this.bigTexComponent = bigTexComponent;
        this.speciesComponent = speciesComponent;
        this.flowJoinComponent = flowJoinComponent;
    }
    
    public boolean canRenderInLayer(BlockRenderLayer renderLayer) 
    { 
        return this.modelInputs.renderLayer == renderLayer;
    }

    public ModelStateGroup getStateGroup() { return stateGroup; }
    
    public abstract QuadContainer2 getFaceQuads(ModelStateSetValue state, BlockRenderLayer renderLayer);
    public abstract List<BakedQuad> getItemQuads(ModelStateSetValue state);
    
    /**
     * Override if special collision handling is needed due to non-cubic shape.
     */
    public ICollisionHandler getCollisionHandler()
    {
        return null;
    }
    
    /** override if need to do some setup that must wait until bake event */
    public void handleBakeEvent(ModelBakeEvent event)
    {
        // NOOP: default implementation assumes lazy baking
    }
    
    /**
     * Identifies all textures needed for texture stitch.
     * Assumes a single texture per model.
     * Override if have something more complicated.
     */
    public String[] getAllTextureNames()
    {
        if(this.modelInputs.textureName == null) return new String[0];
        
        final String retVal[] = new String[(int) this.textureComponent.getValueCount()];

        for (int i = 0; i < retVal.length; i++)
        {
            retVal[i] = buildTextureName(this.modelInputs.textureName, i);
        }
        return retVal;
    }
    
    /** used by dispatched as default particle texture */
    public String getDefaultParticleTexture() 
    { 
    	return buildTextureName(modelInputs.textureName, 0);
    }
    
    protected String buildTextureName(String baseName, int offset)
    {
        return "adversity:blocks/" + baseName + "_" + (offset >> 3) + "_" + (offset & 7);
    }
    
    public boolean isShaded() { return modelInputs.isShaded; }
    
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
