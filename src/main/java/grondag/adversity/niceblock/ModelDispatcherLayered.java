package grondag.adversity.niceblock;

import grondag.adversity.Adversity;
import grondag.adversity.library.model.QuadContainer;
import grondag.adversity.library.model.QuadFactory;
import grondag.adversity.library.model.SimpleItemBlockModel;
import grondag.adversity.niceblock.base.BlockModelHelper;
import grondag.adversity.niceblock.base.ModelController;
import grondag.adversity.niceblock.base.ModelDispatcherBase;
import grondag.adversity.niceblock.base.ModelState;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.base.NiceItemBlock;
import grondag.adversity.niceblock.color.IColorProvider;
import grondag.adversity.niceblock.support.ICollisionHandler;

import java.util.List;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
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
public class ModelDispatcherLayered extends ModelDispatcherBase
{

    /** 
     * Cache for baked block models 
     * Dimensions are render layer, shape, color.
     * Conserves memory to put shape first because most shapes in large-count shape models will never
     * be instantiated.
     */
    private QuadContainer[][][] bakedQuads = new QuadContainer[BlockRenderLayer.values().length][][];
    
    private SimpleItemBlockModel[] itemModels;

    private final ModelController controllers[] = new ModelController[BlockRenderLayer.values().length];
    
    private final ModelController controllerPrimary;
    
    private final boolean isColorCountBiggerThanShapeCount;

    public ModelDispatcherLayered(IColorProvider colorProvider, String particleTextureName, ModelController ... controllersIn)
    {
        super(colorProvider, particleTextureName);
        this.controllerPrimary = controllersIn[0];
        this.itemModels = new SimpleItemBlockModel[colorProvider.getColorCount()];
        boolean testColorCountBiggerThanShapeCount = true;
        for(ModelController cont : controllersIn)
        {
            if(this.controllers[cont.getRenderLayer().ordinal()] != null)
            {
                Adversity.log.warn("Duplicate render layer in controllers passed to ModelDispatherLayered.");
            }
            this.controllers[cont.getRenderLayer().ordinal()] = cont;
            if(cont.getShapeCount() > colorProvider.getColorCount())
            {
                testColorCountBiggerThanShapeCount = false;
            }
        }
        this.isColorCountBiggerThanShapeCount = testColorCountBiggerThanShapeCount;
        NiceBlockRegistrar.allDispatchers.add(this);
    }
    
    @Override
    public void handleTexturePreStitch(Pre event)
    {
        super.handleTexturePreStitch(event);
        for(ModelController cont : controllers)
        {
            if(cont != null)
            {
                for (String tex : cont.getAllTextureNames())
                {
                    event.map.registerSprite(new ResourceLocation(tex));
                }
            }
        }
    }
    
    @Override
    public void handleBakeEvent(ModelBakeEvent event)
    {
        // need to clear arrays to force rebaking of cached models
        if(FMLCommonHandler.instance().getSide() == Side.CLIENT) 
        {
            for(int i = 0; i < controllers.length; i++)
            {
                if(controllers[i] != null)
                {
                    if(isColorCountBiggerThanShapeCount)
                    {
                        bakedQuads[i] = new QuadContainer[colorProvider.getColorCount()][];
                    }
                    else
                    {
                        bakedQuads[i] = new QuadContainer[controllers[i].getShapeCount()][];
                    }
                    controllers[i].getBakedModelFactory().handleBakeEvent(event);
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) 
    {
		if(state == null) return QuadFactory.EMPTY_QUAD_LIST;

        int layer = MinecraftForgeClient.getRenderLayer().ordinal();

        if (controllers[layer] == null) return QuadFactory.EMPTY_QUAD_LIST;
        
        ModelState modelState = ((IExtendedBlockState)state).getValue(NiceBlock.MODEL_STATE);
    	int firstIndex;
    	int secondIndex;
    	if(isColorCountBiggerThanShapeCount)
		{
    		firstIndex = modelState.getColorIndex();
    		secondIndex = modelState.getClientShapeIndex(layer);
		}
    	else
    	{
    		firstIndex = modelState.getClientShapeIndex(layer);
    		secondIndex = modelState.getColorIndex();
    	}
    	
        if(bakedQuads[layer][firstIndex] == null)
        {
            synchronized (bakedQuads)
            {
            	// first check was not synchronized, so confirm
	            if(bakedQuads[layer][firstIndex] == null)
	            {
	            	bakedQuads[layer][firstIndex] = new QuadContainer[isColorCountBiggerThanShapeCount ? controllers[layer].getShapeCount() : this.colorProvider.getColorCount()];
	            }
            }
        }
        
        if(bakedQuads[layer][firstIndex][secondIndex] == null)
        {
            synchronized (bakedQuads)
            {
            	// first check was not synchronized, so confirm
	            if(bakedQuads[layer][firstIndex][secondIndex] == null)
	            {
	            	bakedQuads[layer][firstIndex][secondIndex] = new QuadContainer();
	            }
            }
        }
        
        List<BakedQuad> retVal = bakedQuads[layer][firstIndex][secondIndex].getQuads(side);
        if(retVal == null)
        {
        	retVal = controllers[layer].getBakedModelFactory().getFaceQuads(modelState, colorProvider, side);
        	synchronized (bakedQuads)
            {
            	bakedQuads[layer][firstIndex][secondIndex].setQuads(side, retVal);
            }
        }
        return retVal;
    }

     @Override
    public boolean refreshClientShapeIndex(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos, ModelState modelState, boolean isCachedStateDirty)
    {
        boolean updated = false;
        for(ModelController cont : controllers)
        {
            if(cont != null)
            {
                if(isCachedStateDirty || !cont.useCachedClientState)
                {
                    int oldShapeIndex = modelState.getClientShapeIndex(cont.getRenderLayer().ordinal());
                    modelState.setClientShapeIndex(cont.getClientShapeIndex(block, state, world, pos), cont.getRenderLayer().ordinal());
                    updated = updated || modelState.getClientShapeIndex(0) != oldShapeIndex;
                }
             }
        }
        return updated;
    }

     @Override
     public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity)
     {
    	 if(itemModels[stack.getMetadata()] == null)
    	 {
	    	 BlockModelHelper helper = ((NiceBlock)((NiceItemBlock)stack.getItem()).block).blockModelHelper;
	    	 ModelState modelState = helper.getModelStateForItemModel(stack.getMetadata());
	    	 ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();
	
	    	 for(ModelController cont : controllers)
	    	 {
	    		 if(cont != null)
	    		 {
	    			 builder.addAll(cont.getBakedModelFactory().getItemQuads(modelState, colorProvider));
	    		 }
	    	 }
	
	    	 synchronized(itemModels)
	    	 {
	    		 itemModels[stack.getMetadata()] = new SimpleItemBlockModel(builder.build(), this.isAmbientOcclusion());
	    	 }
    	 }
    	 return itemModels[stack.getMetadata()];
     } 
     
    @Override
    public ICollisionHandler getCollisionHandler()
    {
        return controllerPrimary.getCollisionHandler();
    }

    @Override
    public boolean canRenderInLayer(BlockRenderLayer layer)
    {
        return controllers[layer.ordinal()] != null;
    }

	@Override
	public boolean isAmbientOcclusion() {
		BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
        if (layer == null || controllers[layer.ordinal()] == null) return true;
		return controllers[layer.ordinal()].isShaded;
	}

}
