package grondag.adversity.niceblock.base;

import grondag.adversity.Adversity;
import grondag.adversity.library.cache.ILoadingCache;
import grondag.adversity.library.cache.ManagedLoadingCache;
import grondag.adversity.library.cache.SimpleCacheLoader;
import grondag.adversity.library.cache.SimpleLoadingCache;
import grondag.adversity.library.model.ItemModelDelegate;
import grondag.adversity.library.model.QuadContainer;
import grondag.adversity.library.model.SimpleItemBlockModel;
import grondag.adversity.library.model.SparseLayerMapBuilder;
import grondag.adversity.library.model.SparseLayerMapBuilder.SparseLayerMap;
import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.library.model.quadfactory.QuadFactory;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.modelstate.ModelStateGroup;
import grondag.adversity.niceblock.modelstate.ModelStateSet;
import grondag.adversity.niceblock.modelstate.ModelStateSet.ModelStateSetValue;
import grondag.adversity.niceblock.support.ICollisionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent.Pre;
import net.minecraftforge.common.property.IExtendedBlockState;

public class ModelDispatcher implements IBakedModel
{
    private final String resourceName = UUID.randomUUID().toString();
    private final String particleTextureName;
    private final ModelFactory<?>[] models;
    private final ModelStateSet stateSet;
    private final boolean renderLayerFlags[] = new boolean[BlockRenderLayer.values().length];
    private final boolean shadedFlags[] = new boolean[BlockRenderLayer.values().length];
    private final SparseLayerMapBuilder layerMapBuilder;
    private final ICollisionHandler collisionHandler;
    
    private TextureAtlasSprite particleTexture;


    private final ILoadingCache<SparseLayerMap> modelCache = new ManagedLoadingCache<SparseLayerMap>(new BlockCacheLoader(), 1024, 0xFFFF);
    private final ILoadingCache<SimpleItemBlockModel> itemCache = new ManagedLoadingCache<SimpleItemBlockModel>( new ItemCacheLoader(), 256, 0xFFF);
    
    
    private class BlockCacheLoader implements SimpleCacheLoader<SparseLayerMap>
    {
		@Override
		public SparseLayerMap load(long key) {
			
			ModelStateSetValue state = stateSet.getSetValueFromBits(key);
			
			SparseLayerMap result = layerMapBuilder.makeNewMap();
			for(BlockRenderLayer layer : layerMapBuilder.layerList)
			{
				ArrayList<QuadContainer> containers = new ArrayList<QuadContainer>();
				for(ModelFactory<?> model : models)
				{
					containers.add(model.getFaceQuads(state, layer));
				}
				result.set(layer, QuadContainer.merge(containers));
			}
			return result;
		}       
    }
    
    private class ItemCacheLoader implements SimpleCacheLoader<SimpleItemBlockModel>
    {
		@Override
		public SimpleItemBlockModel load(long key) 
		{
			ModelStateSetValue state = stateSet.getSetValueFromBits(key);
	    	ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();
			
			for(ModelFactory<?> model : models)
			{
				builder.addAll(model.getItemQuads(state));
			}
			return new SimpleItemBlockModel(builder.build(), shadedFlags[0]);
		}       
    }
  
    public ModelDispatcher(ModelFactory<?>... models)
    {
    	this(models[0].getDefaultParticleTexture(), models);
    }
    
    public ModelDispatcher(String particleTextureName, ModelFactory<?>... models)
    {
        this.particleTextureName = particleTextureName;
        this.models = models;
        
        ArrayList<BlockRenderLayer> layerList = new ArrayList<BlockRenderLayer>();
        ModelStateGroup groups[] = new ModelStateGroup[models.length];
        for(int i = 0 ; i < models.length; i++)
        {
            groups[i] = models[i].getStateGroup();
        }
        this.stateSet = ModelStateSet.find(groups);

        ArrayList<ICollisionHandler> collisionHandlers = new ArrayList<>();

        for(int i = 0; i < models.length; i++)
        {
            groups[i] = models[i].getStateGroup();
            if(models[i].getCollisionHandler() != null)
            {
                collisionHandlers.add(models[i].getCollisionHandler());
            }
            
            for(BlockRenderLayer layer : BlockRenderLayer.values())
            {
                if(models[i].canRenderInLayer(layer))
                {
                    renderLayerFlags[layer.ordinal()] = true;
                    layerList.add(layer);
                    shadedFlags[layer.ordinal()] = shadedFlags[layer.ordinal()] || models[i].modelInputs.lightingMode == LightingMode.SHADED;
                }
            }
        }
       
        if(collisionHandlers.isEmpty())
        {
            this.collisionHandler = null;
        }
        else
        {
            this.collisionHandler = new CompositeCollisionHandler(ImmutableList.copyOf(collisionHandlers));
        }
        
        layerMapBuilder = new SparseLayerMapBuilder(layerList);
   
        NiceBlockRegistrar.allDispatchers.add(this);
    }
        
    /**
     * Register all textures that will be needed for associated models. 
     * Happens before model bake.
     */
    public void handleTexturePreStitch(Pre event)
    {
        event.getMap().registerSprite(new ResourceLocation(particleTextureName));
        
        for(ModelFactory<?> model : models)
        {
            for (String tex : model.getAllTextureNames())
            {
                event.getMap().registerSprite(new ResourceLocation(tex));
            }
        }
    }

    public void handleBakeEvent(ModelBakeEvent event)
    {
        //clear caches to force rebaking of cached models
        modelCache.clear();
        itemCache.clear();
        
        for(ModelFactory<?> model : models)
        {
            model.handleBakeEvent(event);
        }
    }

    /**
     * Override if special collision handling is needed due to non-cubic shape.
     */
    public ICollisionHandler getCollisionHandler()
    {
        return this.collisionHandler;
    }

    /**
     * Used by NiceBlock to control rendering.
     */
    public boolean canRenderInLayer(BlockRenderLayer layer)
    {
        return this.renderLayerFlags[layer.ordinal()];
    }
    
    public String getModelResourceString()
    {
        return Adversity.MODID + ":" + resourceName;
    }

    public ModelStateSet getStateSet() 
    {
    	return this.stateSet;
    }
    
    @Override
    public TextureAtlasSprite getParticleTexture()
    {
        if(particleTexture == null)
        {
            particleTexture = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(particleTextureName);
        }
        return particleTexture;
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand)
    {
        if(state == null) return QuadFactory.EMPTY_QUAD_LIST;

        long key = ((IExtendedBlockState)state).getValue(NiceBlock.MODEL_KEY);
        
        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
        
        // If no layer can be determined than probably getting request from block breaking
        // In that case, return quads from all layers
        if(layer == null)
        {
            ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();
            
            for(QuadContainer qc : modelCache.get(key).getAll())
            {
                builder.addAll(qc.getQuads(side));
            }
            
            return builder.build();
            
        }
        else
        {
            return modelCache.get(key).get(layer).getQuads(side);
        }
    }
    
//    public boolean isEmpty(long modelStateKey)
//    {
//        return modelCache.get(modelStateKey).isEmpty();
//    } 
    
    public int getOcclusionKey(long modelStateKey, EnumFacing face)
    {
        if(this.renderLayerFlags[BlockRenderLayer.SOLID.ordinal()] == false) return 0;
        QuadContainer container = modelCache.get(modelStateKey).get(BlockRenderLayer.SOLID);
        if(container == null) 
        {
            //TODO: remove
            Adversity.log.warn("Missing model for occlusion key.");
            return 0;
        }
        return container.getOcclusionHash(face);
    }
    
    public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity)
    {
        long key = NiceItemBlock.getModelStateKey(stack);
        return itemCache.get(key);
    }

    @Override
    public boolean isAmbientOcclusion()
    {
        // Render layer will be null for block damage rendering.
        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
        return layer == null ? true : shadedFlags[MinecraftForgeClient.getRenderLayer().ordinal()];
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
		return new ItemModelDelegate(this) ;
	}
	
	@Override
    public ItemCameraTransforms getItemCameraTransforms()
    {
        return ItemCameraTransforms.DEFAULT;
    }


}