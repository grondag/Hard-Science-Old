package grondag.adversity.niceblock;

import java.util.List;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import grondag.adversity.library.model.QuadContainer;
import grondag.adversity.library.model.QuadFactory;
import grondag.adversity.library.model.SimpleItemBlockModel;
import grondag.adversity.niceblock.base.BlockModelHelper;
import grondag.adversity.niceblock.base.ModelController;
import grondag.adversity.niceblock.base.ModelDispatcher;
import grondag.adversity.niceblock.base.ModelState;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.base.NiceItemBlock;
import grondag.adversity.niceblock.color.IColorProvider;
import grondag.adversity.niceblock.support.ICollisionHandler;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent.Pre;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Manages Lookup, instantiation and caching of all models for a given controller instance.
 *
 * Is also ISmartBlockModel proxy for handleBlockState
 */
public class ModelDispatcherBasic extends ModelDispatcher
{
    private LoadingCache<Long, QuadContainer> modelCache = CacheBuilder.newBuilder().maximumSize(0xFFFF).build(new CacheLoader<Long, QuadContainer>()
    {
        public QuadContainer load(Long key) throws Exception
        {
            return new QuadContainer();
        }
    });
    
    private ThreadLocal<IBlockState> lastState = new ThreadLocal<IBlockState>();
    private ThreadLocal<QuadContainer> lastContainer = new ThreadLocal<QuadContainer>();
    
    private SimpleItemBlockModel[] itemModels;

    private final ModelController controller;

    public ModelDispatcherBasic(IColorProvider colorProvider, String particleTextureName, ModelController controller)
    {
        super(colorProvider, particleTextureName);
        this.controller = controller;
        this.itemModels = new SimpleItemBlockModel[colorProvider.getColorCount()];
        NiceBlockRegistrar.allDispatchers.add(this);
    }
    
    @Override
    public void handleTexturePreStitch(Pre event)
    {
        super.handleTexturePreStitch(event);
        for (String tex : controller.getAllTextureNames())
        {
            event.getMap().registerSprite(new ResourceLocation(tex));
        }
    }

	@Override
    public void handleBakeEvent(ModelBakeEvent event)
    {
        // need to clear cache to force rebaking of cached models
        if(FMLCommonHandler.instance().getSide() == Side.CLIENT) 
        {
            modelCache.invalidateAll();
        }
        controller.getBakedModelFactory().handleBakeEvent(event);
    }   

	@SideOnly(Side.CLIENT)
	@Override
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) 
    {
		if(state == null) return QuadFactory.EMPTY_QUAD_LIST;
		
		if(this.lastState.get() != state)
		{
		    this.lastState.set(state);
	        ModelState modelState = ((IExtendedBlockState)state).getValue(NiceBlock.MODEL_STATE);
		    this.lastContainer.set(modelCache.getUnchecked(controller.getCacheKeyFromModelState(modelState)));
		}

        List<BakedQuad> retVal = lastContainer.get().getQuads(side);
        if(retVal == null)
        {
            ModelState modelState = ((IExtendedBlockState)state).getValue(NiceBlock.MODEL_STATE);
        	retVal = controller.getBakedModelFactory().getFaceQuads(modelState, colorProvider, side);
        	QuadContainer container = lastContainer.get();
        	synchronized (container)
            {
        	    container.setQuads(side, retVal);
            }
        }
        return retVal;
  
    }

     @Override
    public boolean refreshClientShapeIndex(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos, ModelState modelState, boolean isCachedStateDirty)
    {
        if(isCachedStateDirty || !controller.useCachedClientState)
        {
            long oldShapeIndex = modelState.getClientShapeIndex(0);
            modelState.setClientShapeIndex(controller.getClientShapeIndex(block, state, world, pos), 0);
            return modelState.getClientShapeIndex(0) != oldShapeIndex;
        }
        else
        {
            return false;
        }
    }
     
     @Override
     public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity)
     {
    	 if(itemModels[stack.getMetadata()] == null)
    	 {

	    	 BlockModelHelper helper = ((NiceBlock)((NiceItemBlock)stack.getItem()).block).blockModelHelper;
	    	 ModelState modelState = helper.getModelStateForItemModel(stack.getMetadata());

	    	 synchronized(itemModels)
	    	 {
	    		 itemModels[stack.getMetadata()] = new SimpleItemBlockModel(controller.getBakedModelFactory().getItemQuads(modelState, colorProvider), this.isAmbientOcclusion());
	    	 }
    	 }
    	 return itemModels[stack.getMetadata()];
	 } 

    @Override
    public ICollisionHandler getCollisionHandler()
    {
        return controller.getCollisionHandler();
    }

    @Override
    public boolean canRenderInLayer(BlockRenderLayer layer)
    {
        return controller.canRenderInLayer(layer);
    }

	@Override
	public boolean isAmbientOcclusion() {
		return controller.isShaded;
	}
}
