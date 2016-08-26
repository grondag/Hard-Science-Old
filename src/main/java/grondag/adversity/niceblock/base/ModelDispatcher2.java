package grondag.adversity.niceblock.base;

import grondag.adversity.Adversity;

import grondag.adversity.library.model.ItemModelDelegate2;
import grondag.adversity.library.model.QuadContainer;
import grondag.adversity.library.model.SimpleItemBlockModel;
import grondag.adversity.library.model.SparseLayerMapBuilder;
import grondag.adversity.library.model.SparseLayerMapBuilder.SparseLayerMap;
import grondag.adversity.library.model.quadfactory.QuadFactory;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.modelstate.ModelState;
import grondag.adversity.niceblock.modelstate.ModelStateGroup;
import grondag.adversity.niceblock.modelstate.ModelStateSet;
import grondag.adversity.niceblock.support.ICollisionHandler;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent.Pre;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class ModelDispatcher2 implements IBakedModel
{
    private final String resourceName = UUID.randomUUID().toString();
    private final String particleTextureName;
    private final ModelFactory2[] models;
    private final ModelStateSet stateSet;
    private final boolean renderLayerFlags[] = new boolean[BlockRenderLayer.values().length];
    private final boolean shadedFlag;
    private final SparseLayerMapBuilder layerMapBuilder;
    private final ICollisionHandler collisionHandler;
    
    private TextureAtlasSprite particleTexture;

    private final SimpleCacheLoader<SparseLayerMap> blockLoader = new BlockCacheLoader();
    private final SimpleLoadingCache<SparseLayerMap> modelCache = new SimpleLoadingCache<SparseLayerMap>(blockLoader, 1024);
    private final SimpleCacheLoader<SimpleItemBlockModel> itemLoader = new ItemCacheLoader();
    private final SimpleLoadingCache<SimpleItemBlockModel> itemCache = new SimpleLoadingCache<SimpleItemBlockModel>(itemLoader, 256);
    
//    private ThreadLocal<Long> lastStateKey = new ThreadLocal<Long>();
//    private ThreadLocal<SparseLayerMap> lastLayerMap = new ThreadLocal<SparseLayerMap>();
    
    public ModelDispatcher2(String particleTextureName, ModelFactory2... models)
    {
        this.particleTextureName = "adversity:blocks/" + particleTextureName;
        this.models = models;
        
        ArrayList<BlockRenderLayer> layerList = new ArrayList<BlockRenderLayer>();
        ModelStateGroup groups[] = new ModelStateGroup[models.length];
        this.stateSet = ModelStateSet.find(groups);

        ArrayList<ICollisionHandler> collisionHandlers = new ArrayList<>();

        boolean isShaded = false;
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
                }
            }
            isShaded = isShaded || models[i].modelInputs.isShaded;
        }
        this.shadedFlag = isShaded;
       
        if(collisionHandlers.isEmpty())
        {
            this.collisionHandler = null;
        }
        else
        {
            this.collisionHandler = new CompositeCollisionHandler(ImmutableList.copyOf(collisionHandlers));
        }
        
        layerMapBuilder = new SparseLayerMapBuilder(layerList);
   
        NiceBlockRegistrar.allDispatchers2.add(this);
    }
        
    /** 
     * Updated model state key from world.  
     */
    public long getRefreshedKeyFromWorld(long oldKey, NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        //TODO: make this side-aware, states only need to be refreshed on server if matter for collision detection
        //TODO: handle block test somehow
        return stateSet.getRefreshedKeyFromWorld(oldKey, block, null, state, world, pos);
    }

    /**
     * Register all textures that will be needed for associated models. 
     * Happens before model bake.
     */
    public void handleTexturePreStitch(Pre event)
    {
        event.getMap().registerSprite(new ResourceLocation(particleTextureName));
        
        for(ModelFactory2 model : models)
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
        
        for(ModelFactory2 model : models)
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
        
//        if(this.lastStateKey.get() != key)
//        {
//            this.lastStateKey.set(key);
//            this.lastLayerMap.set(modelCache.get(key));
//        }
        
        return modelCache.get(key).get(MinecraftForgeClient.getRenderLayer()).getQuads(side);
    }
    
    private class BlockCacheLoader extends CacheLoader<ModelState, SparseLayerMap>
    {

        @Override
        public SparseLayerMap load(ModelState key) throws Exception
        {
            // TODO Auto-generated method stub
            return null;
        }       
    }
    
    public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity)
    {
        
    }

    @Override
    public boolean isAmbientOcclusion()
    {
        return this.shadedFlag;
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
		return new ItemModelDelegate2(this) ;
	}
	
	@Override
    public ItemCameraTransforms getItemCameraTransforms()
    {
        return ItemCameraTransforms.DEFAULT;
    }


}