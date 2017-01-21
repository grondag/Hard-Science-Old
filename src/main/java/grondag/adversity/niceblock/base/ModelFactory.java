package grondag.adversity.niceblock.base;

import java.util.Collections;
import java.util.List;

import grondag.adversity.library.model.QuadContainer;
import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.niceblock.modelstate.ModelBigTexComponent;
import grondag.adversity.niceblock.modelstate.ModelColorMapComponent;
import grondag.adversity.niceblock.modelstate.ModelFlowTexComponent;
import grondag.adversity.niceblock.modelstate.ModelRotationComponent;
import grondag.adversity.niceblock.modelstate.ModelSpeciesComponent;
import grondag.adversity.niceblock.modelstate.ModelStateComponent;
import grondag.adversity.niceblock.modelstate.ModelStateGroup;
import grondag.adversity.niceblock.modelstate.ModelStateSet.ModelStateSetValue;
import grondag.adversity.niceblock.modelstate.ModelTextureComponent;
import grondag.adversity.niceblock.support.AbstractCollisionHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
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
public abstract class ModelFactory<V extends ModelFactory.ModelInputs>
{
    protected final ModelStateGroup stateGroup;
    protected final V modelInputs;
    protected final ModelColorMapComponent colorComponent;
    protected final ModelTextureComponent textureComponent;
    protected final ModelRotationComponent rotationComponent;
    protected final ModelBigTexComponent bigTexComponent;
    protected final ModelSpeciesComponent speciesComponent;
    protected final ModelFlowTexComponent flowTexComponent;
    
    public ModelFactory(V modelInputs, ModelStateComponent<?,?>... components)
    {
        this.stateGroup = ModelStateGroup.find(components);
        this.modelInputs = modelInputs;

        ModelColorMapComponent colorComponent = null;
        ModelTextureComponent textureComponent = null;
        ModelRotationComponent rotationComponent = null;
        ModelBigTexComponent bigTexComponent = null;
        ModelSpeciesComponent speciesComponent = null;
        ModelFlowTexComponent flowTexComponent = null;
        
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
            else if(c instanceof ModelFlowTexComponent)
                flowTexComponent = (ModelFlowTexComponent) c;
        }
        
        this.colorComponent = colorComponent;
        this.textureComponent = textureComponent;
        this.rotationComponent = rotationComponent;
        this.bigTexComponent = bigTexComponent;
        this.speciesComponent = speciesComponent;
        this.flowTexComponent = flowTexComponent;
    }
    
    public boolean canRenderInLayer(BlockRenderLayer renderLayer) 
    { 
        return this.modelInputs.renderLayer == renderLayer;
    }

    public ModelStateGroup getStateGroup() { return stateGroup; }
    
    public abstract QuadContainer getFaceQuads(ModelStateSetValue state, BlockRenderLayer renderLayer);
    public abstract List<BakedQuad> getItemQuads(ModelStateSetValue state);    
    
    /** 
     * Provide fast, simple quads for generating collision boxes. 
     * Won't be used unless a collision handler is provided by overriding getCollisionHandler.
     */
    public List<RawQuad> getCollisionQuads(ModelStateSetValue state)
    {
        return Collections.emptyList();
    }
    
    /**
     * Override if special collision handling is needed due to non-cubic shape.
     */
    public AbstractCollisionHandler getCollisionHandler(ModelDispatcher dispatcher)
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
    
//    public boolean isShaded() { return modelInputs.isShaded; }
    
    public static class ModelInputs
    {
        public final String textureName;
        public final LightingMode lightingMode;
        public final BlockRenderLayer renderLayer;
        
        public ModelInputs(String textureName, LightingMode lightingMode, BlockRenderLayer renderLayer)
        {
            this.textureName = textureName;
            this.lightingMode = lightingMode;
            this.renderLayer = renderLayer;
        }
    }
    
    public  AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        return Block.FULL_BLOCK_AABB;
    }
}
