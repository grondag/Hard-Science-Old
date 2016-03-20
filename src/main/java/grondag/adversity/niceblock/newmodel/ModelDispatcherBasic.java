package grondag.adversity.niceblock.newmodel;

import java.util.List;

import grondag.adversity.niceblock.newmodel.color.IColorProvider;
import grondag.adversity.niceblock.support.ICollisionHandler;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
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
public class ModelDispatcherBasic extends ModelDispatcherBase
{

    /** 
     * Cache for baked block models 
     * Dimensions are color, shape.
     * Conserves memory to put color first because most colors will never
     * be instantiated, but many shapes within a color probably will.
     */
    private QuadContainer[][] bakedQuads;

    private final ModelControllerNew controller;

    private final boolean isColorCountBiggerThanShapeCount;

    public ModelDispatcherBasic(IColorProvider colorProvider, String particleTextureName, ModelControllerNew controller)
    {
        super(colorProvider, particleTextureName);
        this.controller = controller;
        NiceBlockRegistrar.allDispatchers.add(this);
        this.isColorCountBiggerThanShapeCount = controller.getShapeCount() > colorProvider.getColorCount();
    }
    
    @Override
    public void handleTexturePreStitch(Pre event)
    {
        super.handleTexturePreStitch(event);
        for (String tex : controller.getAllTextureNames())
        {
            event.map.registerSprite(new ResourceLocation(tex));
        }
    }

	@Override
    public void handleBakeEvent(ModelBakeEvent event)
    {
        // need to clear arrays to force rebaking of cached models
        if(FMLCommonHandler.instance().getSide() == Side.CLIENT) 
        {
            if(isColorCountBiggerThanShapeCount)
            {
                bakedQuads = new QuadContainer[colorProvider.getColorCount()][];
            }
            else
            {
                bakedQuads = new QuadContainer[controller.getShapeCount()][];
            }
        }
        else
        {
        	bakedQuads = null;
        }
        controller.getBakedModelFactory().handleBakeEvent(event);
    }
    

	@SideOnly(Side.CLIENT)
	@Override
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) 
    {
		if(state == null) return QuadFactory.EMPTY_QUAD_LIST;
		
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
    	
        if(bakedQuads[firstIndex] == null)
        {
            synchronized (bakedQuads)
            {
            	// first check was not synchronized, so confirm
	            if(bakedQuads[firstIndex] == null)
	            {
	            	bakedQuads[firstIndex] = new QuadContainer[isColorCountBiggerThanShapeCount ? this.colorProvider.getColorCount() : controller.getShapeCount()];
	            }
            }
        }
        
        if(bakedQuads[firstIndex][secondIndex] == null)
        {
            synchronized (bakedQuads)
            {
            	// first check was not synchronized, so confirm
	            if(bakedQuads[firstIndex][secondIndex] == null)
	            {
	            	bakedQuads[firstIndex][secondIndex] = new QuadContainer();
	            }
            }
        }
        List<BakedQuad> retVal = bakedQuads[firstIndex][secondIndex].getQuads(side);
        if(retVal == null)
        {
        	retVal = controller.getBakedModelFactory().getFaceQuads(modelState, colorProvider, side);
        	synchronized (bakedQuads)
            {
            	bakedQuads[firstIndex][secondIndex].setQuads(side, retVal);
            }
        }
        return retVal;
  
    }

     @Override
    public boolean refreshClientShapeIndex(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos, ModelState modelState, boolean isCachedStateDirty)
    {
        if(isCachedStateDirty || !controller.useCachedClientState)
        {
            int oldShapeIndex = modelState.getClientShapeIndex(0);
            modelState.setClientShapeIndex(controller.getClientShapeIndex(block, state, world, pos), 0);
            return modelState.getClientShapeIndex(0) != oldShapeIndex;
        }
        else
        {
            return false;
        }
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
