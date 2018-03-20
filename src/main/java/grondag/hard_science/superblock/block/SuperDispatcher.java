package grondag.hard_science.superblock.block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;

import grondag.exotic_matter.cache.ObjectSimpleCacheLoader;
import grondag.exotic_matter.cache.ObjectSimpleLoadingCache;
import grondag.exotic_matter.model.BlockRenderMode;
import grondag.exotic_matter.model.ISuperBlock;
import grondag.exotic_matter.model.ISuperModelState;
import grondag.exotic_matter.model.PaintLayer;
import grondag.exotic_matter.model.RenderLayout;
import grondag.exotic_matter.model.ShapeMeshGenerator;
import grondag.exotic_matter.model.painter.QuadPainter;
import grondag.exotic_matter.model.painter.QuadPainterFactory;
import grondag.exotic_matter.model.varia.CraftingItem;
import grondag.exotic_matter.render.QuadBakery;
import grondag.exotic_matter.render.QuadContainer;
import grondag.exotic_matter.render.QuadHelper;
import grondag.exotic_matter.render.RawQuad;
import grondag.exotic_matter.render.SimpleItemBlockModel;
import grondag.exotic_matter.render.SparseLayerMapBuilder;
import grondag.exotic_matter.render.SparseLayerMapBuilder.SparseLayerMap;
import grondag.exotic_matter.render.Surface;
import grondag.exotic_matter.render.SurfaceTopology;
import grondag.hard_science.HardScience;
import grondag.hard_science.Log;
import grondag.hard_science.superblock.blockmovetest.PlacementItem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SuperDispatcher
{
    public static final SuperDispatcher INSTANCE = new SuperDispatcher();
    public static final String RESOURCE_BASE_NAME = "super_dispatcher";

    private final SparseLayerMapBuilder[] layerMapBuilders;
    
    public final DispatchDelegate[] delegates;
    
    //custom loading cache is at least 2X faster than guava LoadingCache for our use case
    private final ObjectSimpleLoadingCache<ISuperModelState, SparseLayerMap> modelCache = new ObjectSimpleLoadingCache<ISuperModelState, SparseLayerMap>(new BlockCacheLoader(),  0xFFFF);
    private final ObjectSimpleLoadingCache<ISuperModelState, SimpleItemBlockModel> itemCache = new ObjectSimpleLoadingCache<ISuperModelState, SimpleItemBlockModel>(new ItemCacheLoader(), 0xFFF);
    /** contains quads for use by block damage rendering based on shape only and with appropriate UV mapping*/
    private final ObjectSimpleLoadingCache<ISuperModelState, QuadContainer> damageCache = new ObjectSimpleLoadingCache<ISuperModelState, QuadContainer>(new DamageCacheLoader(), 0x4FF);
    
    private class BlockCacheLoader implements ObjectSimpleCacheLoader<ISuperModelState, SparseLayerMap>
    {
		@Override
		public SparseLayerMap load(ISuperModelState key) {
			
		    Collection<RawQuad> paintedQuads = getFormattedQuads(key, false);
		    
		    @SuppressWarnings("unchecked")
            ArrayList<RawQuad>[] containers = new ArrayList[BlockRenderLayer.values().length];
		    for(int i = 0; i < containers.length; i++)
		    {
		        containers[i] = new ArrayList<RawQuad>();
		    }
		    
			for(RawQuad quad : paintedQuads)
			{
			    containers[quad.renderPass.blockRenderLayer.ordinal()].add(quad);
			}

			RenderLayout renderLayout = key.getRenderPassSet().renderLayout;
			
			SparseLayerMap result = layerMapBuilders[renderLayout.blockLayerFlags].makeNewMap();

			for(BlockRenderLayer layer : BlockRenderLayer.values())
			{
			    if(renderLayout.containsBlockRenderLayer(layer))
			    {
//			        if(Output.DEBUG_MODE && containers[layer.ordinal()].isEmpty())
//			            Output.warn("SuperDispatcher BlockCacheLoader: Empty quads on enabled render layer.");
			        result.set(layer, QuadContainer.fromRawQuads(containers[layer.ordinal()]));
			    }
			}
			return result;
		}
    }
    
    private class ItemCacheLoader implements ObjectSimpleCacheLoader<ISuperModelState, SimpleItemBlockModel>
    {
		@Override
		public SimpleItemBlockModel load(ISuperModelState key) 
		{
	    	ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();
	    	for(RawQuad quad : getFormattedQuads(key, true))
	    	{
	    	    switch(quad.surfaceInstance.surfaceType())
	    	    {
                case CUT:
                    break;
                case LAMP:
                    break;
                case MAIN:
                    break;
                default:
                    break;
	    	    
	    	    
	    	    }
	    	    
	    	    builder.add(QuadBakery.createBakedQuad(quad, true));
	    	}
			return new SimpleItemBlockModel(builder.build(), true);
		}       
    }
    
    private class DamageCacheLoader implements ObjectSimpleCacheLoader<ISuperModelState, QuadContainer>
    {
        @Override
        public QuadContainer load(ISuperModelState key) 
        {
            List<RawQuad> quads = key.getShape().meshFactory().getShapeQuads(key);
            if(quads.isEmpty()) return QuadContainer.EMPTY_CONTAINER;
            for(RawQuad q : quads)
            {
                // arbitrary choice - just needs to be a simple non-null texture
                q.textureName = grondag.exotic_matter.init.ModTextures.BLOCK_COBBLE.getSampleTextureName();
             
                // Need to scale UV on non-cubic surfaces to be within a 1 block boundary.
                // This causes breaking textures to be scaled to normal size.
                // If we didn't do this, bigtex block break textures would appear abnormal.
                if(q.surfaceInstance.topology() == SurfaceTopology.TILED)
                {
                    // This is simple for tiled surface because UV scale is always 1.0
                    q.minU = 0;
                    q.maxU = 16;
                    q.minV = 0;
                    q.maxV = 16;
                }
            }
            return QuadContainer.fromRawQuads(quads);
        }       
    }
    
    private SuperDispatcher()
    {
        this.layerMapBuilders = new SparseLayerMapBuilder[RenderLayout.BENUMSET_BLOCK_RENDER_LAYER.combinationCount()];

        for(int i = 0; i < RenderLayout.BENUMSET_BLOCK_RENDER_LAYER.combinationCount(); i++)
        {
            this.layerMapBuilders[i] = new SparseLayerMapBuilder(RenderLayout.BENUMSET_BLOCK_RENDER_LAYER.getValuesForSetFlags(i));
        }
        
        this.delegates = new DispatchDelegate[BlockRenderMode.values().length];
        for(BlockRenderMode mode : BlockRenderMode.values())
        {
            DispatchDelegate newDelegate = new DispatchDelegate(mode);
            this.delegates[mode.ordinal()] = newDelegate;
        }
    }
    
    public void clear()
    {
            modelCache.clear();
            itemCache.clear();
    }

    public int getOcclusionKey(ISuperModelState modelState, EnumFacing face)
    {
        if(!modelState.getRenderPassSet().renderLayout.containsBlockRenderLayer(BlockRenderLayer.SOLID)) return 0;

        SparseLayerMap map = modelCache.get(modelState);
        if(map == null)
        {
            Log.warn("Missing layer map for occlusion key.");
            return 0;
        }

        QuadContainer container = map.get(BlockRenderLayer.SOLID);
        if(container == null) 
        {
            Log.warn("Missing model for occlusion key.");
            return 0;
        }
        return container.getOcclusionHash(face);
    }
    
    private Collection<RawQuad> getFormattedQuads(ISuperModelState modelState, boolean isItem)
    {
        ArrayList<RawQuad> result = new ArrayList<RawQuad>();
         
        ShapeMeshGenerator mesher = modelState.getShape().meshFactory();
                
        ArrayList<QuadPainter> painters = new ArrayList<QuadPainter>();
        for(Surface surface : mesher.getSurfaces(modelState))
        {
            switch(surface.surfaceType)
            {
            case CUT:
                painters.add(QuadPainterFactory.getPainterForSurface(modelState, surface, PaintLayer.CUT));
                if(modelState.isMiddleLayerEnabled())
                {
                    painters.add(QuadPainterFactory.getPainterForSurface(modelState, surface, PaintLayer.MIDDLE));
                }
                break;
            
            case LAMP:
                painters.add(QuadPainterFactory.getPainterForSurface(modelState, surface, PaintLayer.LAMP));
                break;

            case MAIN:
                painters.add(QuadPainterFactory.getPainterForSurface(modelState, surface, PaintLayer.BASE));
                if(modelState.isMiddleLayerEnabled())
                {
                    painters.add(QuadPainterFactory.getPainterForSurface(modelState, surface, PaintLayer.MIDDLE));
                }
                if(modelState.isOuterLayerEnabled())
                {
                    painters.add(QuadPainterFactory.getPainterForSurface(modelState, surface, PaintLayer.OUTER));
                }
                break;

            default:
                break;
            }
        }
        
        for(RawQuad shapeQuad : modelState.getShape().meshFactory().getShapeQuads(modelState))
        {
            Surface qSurface = shapeQuad.surfaceInstance.surface();
            for(QuadPainter p : painters)
            {
                if(qSurface == p.surface) p.addPaintedQuadToList(shapeQuad, result, isItem);
            }
        }
        return result;
    }
    
    public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity)
    {
        ISuperModelState key = stack.getItem() instanceof CraftingItem
                ? ((CraftingItem)stack.getItem()).modelState
                : PlacementItem.getStackModelState(stack);
        return itemCache.get(key);
    }
  
    public DispatchDelegate getDelegate(SuperBlock block)
    {
        return this.delegates[block.blockRenderMode.ordinal()];
    }
    
    /**
     * Ugly but only used during load. Retrieves delegates for our custom model loader.
     */
    public DispatchDelegate getDelegate(String resourceString)
    {
        int start = resourceString.lastIndexOf(SuperDispatcher.RESOURCE_BASE_NAME) + SuperDispatcher.RESOURCE_BASE_NAME.length();
        int index;
        if(resourceString.contains("item"))
        {
            int end = resourceString.lastIndexOf(".");
            index = Integer.parseInt(resourceString.substring(start, end));
        }
        else
        {
            index = Integer.parseInt(resourceString.substring(start));
        }
        return this.delegates[index];
    }
    
    /**
     * Delegate to use for generic crafting item rendering
     */
    public DispatchDelegate getItemDelegate()
    {
        return this.delegates[BlockRenderMode.TRANSLUCENT_SHADED.ordinal()];
    }
    
    public class DispatchDelegate implements IBakedModel, IModel
    {
        private final BlockRenderMode blockRenderMode;
        private final String modelResourceString;
        
        private DispatchDelegate(BlockRenderMode blockRenderMode)
        {
            this.blockRenderMode = blockRenderMode;
            this.modelResourceString = HardScience.prefixResource(SuperDispatcher.RESOURCE_BASE_NAME  + blockRenderMode.ordinal());
        }

        /** only used for block layer version */
        public String getModelResourceString()
        {
            return this.modelResourceString;
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
            if(state == null) return QuadHelper.EMPTY_QUAD_LIST;
    
            ISuperModelState modelState = ((IExtendedBlockState)state).getValue(ISuperBlock.MODEL_STATE);
            
            BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
            
            // If no renderIntent set then probably getting request from block breaking
            if(layer == null)
            {
                QuadContainer qc = damageCache.get(modelState.geometricState());
                if(qc == null) 
                    return QuadHelper.EMPTY_QUAD_LIST;
                return qc.getQuads(side);
            }
            else
            {
                SparseLayerMap map = modelCache.get(modelState);
                if(map == null) 
                    return QuadHelper.EMPTY_QUAD_LIST;
                QuadContainer container = map.get(layer);
                if(container == null)
                        return QuadHelper.EMPTY_QUAD_LIST;
                return container.getQuads(side);
            }
        }
     
        @Override
        public boolean isAmbientOcclusion()
        {
         
            BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
            if(layer == null) return true;
            
            switch(layer)
            {
            case SOLID:
                return !this.blockRenderMode.isSolidLayerFlatLighting;
                
            case TRANSLUCENT:
                return !this.blockRenderMode.isTranlucentLayerFlatLighting;
                
            default:
                return true;
            }
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

        @Override
        public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
        {
            return this;
        }
    }
}