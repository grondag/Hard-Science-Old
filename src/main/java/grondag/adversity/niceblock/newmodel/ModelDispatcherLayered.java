package grondag.adversity.niceblock.newmodel;

import grondag.adversity.Adversity;
import grondag.adversity.niceblock.newmodel.color.IColorProvider;
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

    private final ModelControllerNew controllers[] = new ModelControllerNew[BlockRenderLayer.values().length];
    
    private final ModelControllerNew controllerPrimary;
    
    private final boolean isColorCountBiggerThanShapeCount;

    public ModelDispatcherLayered(IColorProvider colorProvider, String particleTextureName, ModelControllerNew ... controllersIn)
    {
        super(colorProvider, particleTextureName);
        this.controllerPrimary = controllersIn[0];
        boolean testColorCountBiggerThanShapeCount = true;
        for(ModelControllerNew cont : controllersIn)
        {
            if(this.controllers[cont.renderLayer.ordinal()] != null)
            {
                Adversity.log.warn("Duplicate render layer in controllers passed to ModelDispatherLayered.");
            }
            this.controllers[cont.renderLayer.ordinal()] = cont;
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
        for(ModelControllerNew cont : controllers)
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

        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();

        if (controllers[layer.ordinal()] == null) return QuadFactory.EMPTY_QUAD_LIST;
        
        ModelState modelState = ((IExtendedBlockState)state).getValue(NiceBlock.MODEL_STATE);
    	int firstIndex;
    	int secondIndex;
    	if(isColorCountBiggerThanShapeCount)
		{
    		firstIndex = modelState.getColorIndex();
    		secondIndex = modelState.getClientShapeIndex(0);
		}
    	else
    	{
    		firstIndex = modelState.getClientShapeIndex(0);
    		secondIndex = modelState.getColorIndex();
    	}
    	
        if(bakedQuads[layer.ordinal()][firstIndex] == null)
        {
            synchronized (bakedQuads)
            {
            	// first check was not synchronized, so confirm
	            if(bakedQuads[layer.ordinal()][firstIndex] == null)
	            {
	            	bakedQuads[layer.ordinal()][firstIndex] = new QuadContainer[isColorCountBiggerThanShapeCount ? this.colorProvider.getColorCount() : controllers[layer.ordinal()].getShapeCount()];
	            }
            }
        }
        
        if(bakedQuads[layer.ordinal()][firstIndex][secondIndex] == null)
        {
            synchronized (bakedQuads)
            {
            	// first check was not synchronized, so confirm
	            if(bakedQuads[layer.ordinal()][firstIndex][secondIndex] == null)
	            {
	            	bakedQuads[layer.ordinal()][firstIndex][secondIndex] = new QuadContainer();
	            }
            }
        }
        
        List<BakedQuad> retVal = bakedQuads[layer.ordinal()][firstIndex][secondIndex].getQuads(side);
        if(retVal == null)
        {
        	retVal = controllers[layer.ordinal()].getBakedModelFactory().getFaceQuads(modelState, colorProvider, side);
        	synchronized (bakedQuads)
            {
            	bakedQuads[layer.ordinal()][firstIndex][secondIndex].setQuads(side, retVal);
            }
        }
        return retVal;
    }

     @Override
    public boolean refreshClientShapeIndex(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos, ModelState modelState, boolean isCachedStateDirty)
    {
        boolean updated = false;
        for(ModelControllerNew cont : controllers)
        {
            if(cont != null)
            {
                if(isCachedStateDirty || !cont.useCachedClientState)
                {
                    int oldShapeIndex = modelState.getClientShapeIndex(cont.renderLayer.ordinal());
                    modelState.setClientShapeIndex(cont.getClientShapeIndex(block, state, world, pos), cont.renderLayer.ordinal());
                    updated = updated || modelState.getClientShapeIndex(0) != oldShapeIndex;
                }
             }
        }
        return updated;
    }

     @Override
     public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity)
     {
    	 BlockModelHelper helper = ((NiceBlock)((NiceItemBlock)stack.getItem()).block).blockModelHelper;
    	 ModelState modelState = helper.getModelStateForItemModel(stack.getMetadata());
    	 ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();

    	 for(ModelControllerNew cont : controllers)
    	 {
    		 if(cont != null)
    		 {
    			 builder.addAll(cont.getBakedModelFactory().getItemQuads(modelState, colorProvider));
    		 }
    	 }

    	 return new SimpleItemModel(builder.build(), this.isAmbientOcclusion());
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
