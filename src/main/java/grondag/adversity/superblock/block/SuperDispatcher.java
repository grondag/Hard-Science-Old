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
import grondag.adversity.superblock.items.SuperItemBlock;
import grondag.adversity.superblock.items.SuperModelItemOverrideList;
import grondag.adversity.superblock.model.painter.QuadPainter;
import grondag.adversity.superblock.model.painter.QuadPainterFactory;
import grondag.adversity.superblock.model.painter.surface.Surface;
import grondag.adversity.superblock.model.painter.surface.SurfaceType;
import grondag.adversity.superblock.model.shape.ShapeMeshGenerator;
import grondag.adversity.superblock.model.state.PaintLayer;
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

@SuppressWarnings("unused")
public class SuperDispatcher
{
    private final String resourceName;
    private final SparseLayerMapBuilder[] layerMapBuilders;
    
    public final DispatchDelegate[] delegates;
    
    //custom loading cache is at least 2X faster than guava LoadingCache for our use case
    private final ObjectSimpleLoadingCache<ModelState, SparseLayerMap> modelCache = new ObjectSimpleLoadingCache<ModelState, SparseLayerMap>(new BlockCacheLoader(),  0xFFFF);
    private final ObjectSimpleLoadingCache<ModelState, SimpleItemBlockModel> itemCache = new ObjectSimpleLoadingCache<ModelState, SimpleItemBlockModel>(new ItemCacheLoader(), 0xFFF);
    
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

			SparseLayerMap result = layerMapBuilders[key.getCanRenderInLayerFlags()].makeNewMap();

			for(BlockRenderLayer layer : BlockRenderLayer.values())
			{
			    if(key.canRenderInLayer(layer))
			    {
//			        if(Output.DEBUG_MODE && containers[layer.ordinal()].isEmpty())
//			            Output.warn("SuperDispatcher BlockCacheLoader: Empty quads on enabled render layer.");
			        result.set(layer, QuadContainer.fromRawQuads(containers[layer.ordinal()]));
			    }
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

        this.layerMapBuilders = new SparseLayerMapBuilder[ModelState.BENUMSET_RENDER_LAYER.combinationCount()];
        this.delegates  = new DispatchDelegate[ModelState.BENUMSET_RENDER_LAYER.combinationCount()];

        for(int i = 0; i < ModelState.BENUMSET_RENDER_LAYER.combinationCount(); i++)
        {
            this.layerMapBuilders[i] = new SparseLayerMapBuilder(ModelState.BENUMSET_RENDER_LAYER.getValuesForSetFlags(i));
            this.delegates[i] = new DispatchDelegate(i);
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
            Output.warn("Missing layer map for occlusion key.");
            return 0;
        }
        
        QuadContainer container = map.get(BlockRenderLayer.SOLID);
        if(container == null) 
        {
            Output.warn("Missing model for occlusion key.");
            return 0;
        }
        return container.getOcclusionHash(face);
    }
    
    private Collection<RawQuad> getFormattedQuads(ModelState modelState)
    {
        ArrayList<RawQuad> result = new ArrayList<RawQuad>();
         
        ShapeMeshGenerator mesher = modelState.getShape().meshFactory();
                
        ArrayList<QuadPainter> painters = new ArrayList<QuadPainter>();
        for(Surface surface : mesher.surfaces)
        {
            switch(surface.surfaceType)
            {
            case CUT:
                painters.add(QuadPainterFactory.getPainterForSurface(modelState, surface, PaintLayer.CUT));
                if(modelState.isDetailLayerEnabled())
                {
                    // don't render painters that interpret species as multi-block boundaries
                    // if species is used for block height instead
                    if(!mesher.isSpeciesUsedForHeight() 
                            || (modelState.getTexture(PaintLayer.DETAIL).textureLayout.modelStateFlag & ModelState.STATE_FLAG_NEEDS_SPECIES) == 0)
                    {
                        painters.add(QuadPainterFactory.getPainterForSurface(modelState, surface, PaintLayer.DETAIL));
                    }
                }
                break;
            
            case LAMP:
                painters.add(QuadPainterFactory.getPainterForSurface(modelState, surface, PaintLayer.LAMP));
                break;

            case MAIN:
                painters.add(QuadPainterFactory.getPainterForSurface(modelState, surface, PaintLayer.BASE));
                if(modelState.isDetailLayerEnabled())
                {
                    // don't render painters that interpret species as multi-block boundaries
                    // if species is used for block height instead
                    if(!mesher.isSpeciesUsedForHeight() 
                            || (modelState.getTexture(PaintLayer.DETAIL).textureLayout.modelStateFlag & ModelState.STATE_FLAG_NEEDS_SPECIES) == 0)
                    {
                        painters.add(QuadPainterFactory.getPainterForSurface(modelState, surface, PaintLayer.DETAIL));
                    }
                }
                if(modelState.isOverlayLayerEnabled())
                {
                    // don't render painters that interpret species as multi-block boundaries
                    // if species is used for block height instead
                    if(!mesher.isSpeciesUsedForHeight() 
                            || (modelState.getTexture(PaintLayer.OVERLAY).textureLayout.modelStateFlag & ModelState.STATE_FLAG_NEEDS_SPECIES) == 0)
                    {
                        painters.add(QuadPainterFactory.getPainterForSurface(modelState, surface, PaintLayer.OVERLAY));
                    }
                }
                break;

            default:
                break;
            }
        }
        
        for(RawQuad shapeQuad : modelState.getShape().meshFactory().getShapeQuads(modelState))
        {
            Surface qSurface = shapeQuad.surface;
            for(QuadPainter p : painters)
            {
                if(qSurface == p.surface) p.addPaintedQuadToList(shapeQuad, result);
            }
        }
        return result;
    }
    
    public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity)
    {
        ModelState key = SuperItemBlock.getModelState(stack);
        return itemCache.get(key);
    }
  
    
    public DispatchDelegate getDelegate(SuperBlock block)
    {
        return this.delegates[block.renderLayerShadedFlags()];
    }
        
    public class DispatchDelegate implements IBakedModel
    {
        private final int renderLayerShadedFlags;
        
        public String getModelResourceString()
        {
            return SuperDispatcher.this.resourceName  + this.renderLayerShadedFlags;
        }

        private DispatchDelegate(int renderLayerShadedFlags)
        {
            this.renderLayerShadedFlags = renderLayerShadedFlags;
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
            // In that case, return quads from all layers with UV coordinates scaled to be within a 1 block boundary.
            // This causes breaking textures to be scaled to normal size.
            // If we didn't do this, bigtex block break textures would appear abnormal.
            if(layer == null)
            {
    
                // TODO: Actually rescale the quad UVs per above note.   Look at BakedQuadRetextured for ideas
                
                ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();
                
                for(QuadContainer qc : modelCache.get(modelState).getAll())
                {
                    builder.addAll(qc.getQuads(side));
                }
                
                return builder.build();
                
            }
            else
            {
                SparseLayerMap map = modelCache.get(modelState);
                if(map == null) 
                    return QuadFactory.EMPTY_QUAD_LIST;
                QuadContainer container = map.get(layer);
                if(container == null)
                        return QuadFactory.EMPTY_QUAD_LIST;
                return container.getQuads(side);
            }
        }
     
        @Override
        public boolean isAmbientOcclusion()
        {
            return ModelState.BENUMSET_RENDER_LAYER.isFlagSetForValue(MinecraftForgeClient.getRenderLayer(), this.renderLayerShadedFlags);
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