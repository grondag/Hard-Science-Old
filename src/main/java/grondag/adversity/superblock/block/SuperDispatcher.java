package grondag.adversity.superblock.block;

import grondag.adversity.Output;
import grondag.adversity.library.cache.objectKey.ObjectSimpleCacheLoader;
import grondag.adversity.library.cache.objectKey.ObjectSimpleLoadingCache;
import grondag.adversity.library.model.QuadContainer;
import grondag.adversity.library.model.SimpleItemBlockModel;
import grondag.adversity.library.model.SparseLayerMapBuilder;
import grondag.adversity.library.model.SparseLayerMapBuilder.SparseLayerMap;
import grondag.adversity.library.model.quadfactory.QuadFactory;
import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.superblock.model.state.ModelStateFactory;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.google.common.collect.ImmutableList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;

public class SuperDispatcher
{
    private final String resourceName;
    private final SparseLayerMapBuilder layerMapBuilder;
    
    //custom loading cache is at least 2X faster than guava LoadingCache for our use case
    private final ObjectSimpleLoadingCache<ModelState, SparseLayerMap> modelCache = new ObjectSimpleLoadingCache<ModelState, SparseLayerMap>(new BlockCacheLoader(),  0xFFFF);
    private final ObjectSimpleLoadingCache<ModelState, SimpleItemBlockModel> itemCache = new ObjectSimpleLoadingCache<ModelState, SimpleItemBlockModel>(new ItemCacheLoader(), 0xFFF);
    
    private final DispatcherDelegate[] delegates;
    
    private class BlockCacheLoader implements ObjectSimpleCacheLoader<ModelState, SparseLayerMap>
    {
		@Override
		public SparseLayerMap load(ModelState key) {
			
		    Collection<RawQuad> paintedQuads = getFormattedQuads(key);
		    
		    @SuppressWarnings("unchecked")
            ArrayList<RawQuad>[] containers = new ArrayList[BlockRenderLayer.values().length];
		    for(int i = 0; i < containers.length; i++)
		    {
		        containers[i] = new ArrayList<RawQuad>();
		    }
		    
			for(RawQuad quad : paintedQuads)
			{
			    containers[quad.renderLayer.ordinal()].add(quad);
			}

			SparseLayerMap result = layerMapBuilder.makeNewMap();
			for(BlockRenderLayer layer : layerMapBuilder.layerList)
			{

				result.set(layer, QuadContainer.fromRawQuads(containers[layer.ordinal()]));
			}
			return result;
		}
    }
    
    private class ItemCacheLoader implements ObjectSimpleCacheLoader<ModelState, SimpleItemBlockModel>
    {
		@Override
		public SimpleItemBlockModel load(ModelState key) 
		{
	    	ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();
	    	for(RawQuad quad : getFormattedQuads(key))
	    	{
	    	    builder.add(quad.createBakedQuad());
	    	}
			return new SimpleItemBlockModel(builder.build(), key.getRenderLayerShadedFlags() != 0);
		}       
    }
    
    public SuperDispatcher(String resourceName)
    {
        this.resourceName = resourceName;
        ArrayList<BlockRenderLayer> layerList = new ArrayList<BlockRenderLayer>();
        layerMapBuilder = new SparseLayerMapBuilder(layerList);
        this.delegates = new DispatcherDelegate[ModelState.BENUMSET_RENDER_LAYER.combinationCount()];
        for(int i = 0; i < this.delegates.length; i++)
        {
            this.delegates[i] = new DispatcherDelegate(i);
        }
    }
        
    public void clear()
    {
            modelCache.clear();
            itemCache.clear();
    }

    public int getOcclusionKey(ModelState modelState, EnumFacing face)
    {
        if(modelState.canRenderInLayer(BlockRenderLayer.SOLID)) return 0;

        SparseLayerMap map = modelCache.get(modelState);
        if(map == null)
        {
            Output.getLog().warn("Missing layer map for occlusion key.");
            return 0;
        }
        
        QuadContainer container = map.get(BlockRenderLayer.SOLID);
        if(container == null) 
        {
            Output.getLog().warn("Missing model for occlusion key.");
            return 0;
        }
        return container.getOcclusionHash(face);
    }
    
    private Collection<RawQuad> getFormattedQuads(ModelState modelState)
    {
        Collection<RawQuad> shapeQuads = modelState.getShape().meshFactory.getShapeQuads(modelState);
        ArrayList<RawQuad> result = new ArrayList<RawQuad>();
        for(int i = 0; i < ModelStateFactory.MAX_PAINTERS; i++) 
        {
            modelState.getSurfacePainter(0).addPaintedQuadsToList(shapeQuads, result);
        }
        return result;
    }
    
    public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity)
    {
        ModelState key = SuperItemBlock.getModelState(stack);
        return itemCache.get(key);
    }
    
    public DispatcherDelegate getDelegateForShadedFlags(int shadedFlags)
    {
        return this.delegates[shadedFlags];
    }
    
    /**
     * Delegates are needed to provide accurate results for isAmbientOcclusion().
     * This determines which rendering path the model takes for the current render layer - AO vs flat.
     * Wrong values may cause strange lighting artifacts when rending full-bright layers.
     * 
     * We get no state information in that method, so we instead provide a delegate 
     * that is determined during getActualState based on the needs of current model state.
     * 
     */
    public class DispatcherDelegate implements IBakedModel
    {
        /** 
         * Identifies which layers are shaded. See {@link ModelState#getRenderLayerShadedFlags()}
         */
        private final int layerShadedFlags;

        
        private DispatcherDelegate(int layerShadedFlags)
        {
            this.layerShadedFlags = layerShadedFlags;
        }
        
        public String getModelResourceString()
        {
            return SuperDispatcher.this.resourceName + layerShadedFlags;
        }
        
        @Override
        public TextureAtlasSprite getParticleTexture()
        {
            // should not ever be used
            return Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
        }
    
        @Override
        public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand)
        {
            if(state == null) return QuadFactory.EMPTY_QUAD_LIST;
    
            ModelState modelState = ((IExtendedBlockState)state).getValue(SuperBlock.MODEL_STATE);
            
            BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
            
            // If no layer can be determined than probably getting request from block breaking
            // In that case, return quads from all layers
            if(layer == null)
            {
                ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();
                
                for(QuadContainer qc : modelCache.get(modelState).getAll())
                {
                    builder.addAll(qc.getQuads(side));
                }
                
                return builder.build();
                
            }
            else
            {
                return modelCache.get(modelState).get(layer).getQuads(side);
            }
        }
    
        @Override
        public boolean isAmbientOcclusion()
        {
            BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
            // Render layer will be null for block damage rendering.
            return layer == null 
                ? true 
                : ModelState.BENUMSET_RENDER_LAYER.isFlagSetForValue(MinecraftForgeClient.getRenderLayer(), this.layerShadedFlags);
        }
    
        @Override
        public boolean isBuiltInRenderer()
        {
            return false;
        }
        
    	@Override
    	public boolean isGui3d()
    	{
    		return true;
    	}
	
    	@Override
    	public ItemOverrideList getOverrides()
    	{
    		return new SuperModelItemOverrideList(SuperDispatcher.this) ;
    	}
	
    	@Override
        public ItemCameraTransforms getItemCameraTransforms()
        {
            return ItemCameraTransforms.DEFAULT;
        }

    }
}