package grondag.adversity.niceblock.base;

import java.util.Collections;
import java.util.List;

import grondag.adversity.library.model.QuadContainer;
import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.niceblock.modelstate.ModelBigTexComponent;
import grondag.adversity.niceblock.modelstate.ModelColorMapComponent;
import grondag.adversity.niceblock.modelstate.ModelFlowTexComponent;
import grondag.adversity.niceblock.modelstate.ModelRotationComponent;
import grondag.adversity.niceblock.modelstate.ModelShape;
import grondag.adversity.niceblock.modelstate.ModelSpeciesComponent;
import grondag.adversity.niceblock.modelstate.ModelStateComponent;
import grondag.adversity.niceblock.modelstate.ModelStateSet;
import grondag.adversity.niceblock.modelstate.ModelStateSet.ModelStateSetValue;
import grondag.adversity.niceblock.modelstate.ModelTextureVersionComponent;
import grondag.adversity.niceblock.texture.TextureProvider.Texture.TextureState;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public abstract class ModelFactory
{
    protected final ModelStateSet stateSet;
    protected final ModelShape shape;
    protected final ModelColorMapComponent colorComponent;
    protected final ModelTextureVersionComponent textureComponent;
    protected final ModelRotationComponent rotationComponent;
    protected final ModelBigTexComponent bigTexComponent;
    protected final ModelSpeciesComponent speciesComponent;
    protected final ModelFlowTexComponent flowTexComponent;
    
    public ModelFactory(ModelShape shape, ModelStateComponent<?,?>... components)
    {
        this.stateSet = ModelStateSet.find(shape, components);
        this.shape = shape;

        ModelColorMapComponent colorComponent = null;
        ModelTextureVersionComponent textureComponent = null;
        ModelRotationComponent rotationComponent = null;
        ModelBigTexComponent bigTexComponent = null;
        ModelSpeciesComponent speciesComponent = null;
        ModelFlowTexComponent flowTexComponent = null;
        
        for(ModelStateComponent<?,?> c : components)
        {
            if(c instanceof ModelColorMapComponent)
                colorComponent = (ModelColorMapComponent) c;
            else if(c instanceof ModelTextureVersionComponent)
                textureComponent = (ModelTextureVersionComponent) c;
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
    
    public ModelStateSet getStateSet() { return stateSet; }
    
    public abstract QuadContainer getFaceQuads(TextureState texState, ModelStateSetValue state, BlockRenderLayer renderLayer);
    public abstract List<BakedQuad> getItemQuads(TextureState texState, ModelStateSetValue state);    
    
    /** 
     * Provide fast, simple quads for generating collision boxes. 
     * Won't be used unless a collision handler is provided by overriding getCollisionHandler.
     */
    public List<RawQuad> getCollisionQuads(ModelStateSetValue state)
    {
        return Collections.emptyList();
    }
       
    public  AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        return Block.FULL_BLOCK_AABB;
    }
}
