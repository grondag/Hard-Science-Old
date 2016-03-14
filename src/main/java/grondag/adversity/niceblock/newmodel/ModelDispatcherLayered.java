package grondag.adversity.niceblock.newmodel;

import grondag.adversity.Adversity;
import grondag.adversity.niceblock.newmodel.color.ColorMap;
import grondag.adversity.niceblock.newmodel.color.IColorProvider;
import grondag.adversity.niceblock.support.ICollisionHandler;

import java.util.Collections;
import java.util.List;

import javax.vecmath.Vector3f;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent.Pre;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModelState;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.TRSRTransformation;
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
    private IBakedModel[][][] bakedBlockModels = new IBakedModel[EnumWorldBlockLayer.values().length][][];

    private final ModelControllerNew controllers[] = new ModelControllerNew[EnumWorldBlockLayer.values().length];
    
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
                        bakedBlockModels[i] = new IBakedModel[colorProvider.getColorCount()][];
                    }
                    else
                    {
                        bakedBlockModels[i] = new IBakedModel[controllers[i].getShapeCount()][];
                    }
                    controllers[i].getBakedModelFactory().handleBakeEvent(event);
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IBakedModel handleBlockState(IBlockState state)
    {
        IBakedModel retVal = null;
        EnumWorldBlockLayer layer = MinecraftForgeClient.getRenderLayer();

        // Really should ALWAYS be a NiceBlock instance but if someone goes
        // mucking about with the model registry crazy stuff could happen.
        if (state instanceof IExtendedBlockState && state.getBlock() instanceof NiceBlock && controllers[layer.ordinal()] != null)
        {
            IExtendedBlockState exState = (IExtendedBlockState) state;
            ModelState modelState = exState.getValue(NiceBlock.MODEL_STATE);
            
            if(isColorCountBiggerThanShapeCount)
            {
                if(bakedBlockModels[layer.ordinal()][modelState.getColorIndex()] != null)
                {                    
                    retVal = bakedBlockModels[layer.ordinal()][modelState.getColorIndex()][modelState.getClientShapeIndex(layer.ordinal())];
                }
            }
            else if(bakedBlockModels[layer.ordinal()][modelState.getClientShapeIndex(layer.ordinal())] != null)
            {
                retVal = bakedBlockModels[layer.ordinal()][modelState.getClientShapeIndex(layer.ordinal())][modelState.getColorIndex()];
            }
        
            if (retVal == null)
            {
                retVal = controllers[layer.ordinal()].getBakedModelFactory().getBlockModel(modelState, colorProvider);
                
                synchronized (bakedBlockModels)
                {
                    if(isColorCountBiggerThanShapeCount)
                    {
                        if(bakedBlockModels[layer.ordinal()][modelState.getColorIndex()] == null)
                        {
                            bakedBlockModels[layer.ordinal()][modelState.getColorIndex()] = new IBakedModel[controllers[layer.ordinal()].getShapeCount()];
                        }
                        bakedBlockModels[layer.ordinal()][modelState.getColorIndex()][modelState.getClientShapeIndex(layer.ordinal())] = retVal;
                    }
                    else
                    {
                        if(bakedBlockModels[layer.ordinal()][modelState.getClientShapeIndex(layer.ordinal())] == null)
                        {
                            bakedBlockModels[layer.ordinal()][modelState.getClientShapeIndex(layer.ordinal())] = new IBakedModel[this.colorProvider.getColorCount()];
                        }
                        bakedBlockModels[layer.ordinal()][modelState.getClientShapeIndex(layer.ordinal())][modelState.getColorIndex()] = retVal;
                    }
                }
            }
        }

        // Provide a default to contain the damage if we have somehow still derped it up.
        if (retVal == null)
        {
            retVal = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel();
        }

        return retVal;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IBakedModel getItemModelForModelState(ModelState modelState)
    {
        // Enable perspective handling.
        TRSRTransformation thirdperson = TRSRTransformation.blockCenterToCorner(new TRSRTransformation(
                new Vector3f(0, 1.5f / 16, -2.75f / 16),
                TRSRTransformation.quatFromYXZDegrees(new Vector3f(10, -45, 170)),
                new Vector3f(0.375f, 0.375f, 0.375f),
                null));

        IModelState state = new SimpleModelState(ImmutableMap.of(TransformType.THIRD_PERSON, thirdperson), Optional.of(TRSRTransformation.identity()));
        
        ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder();
        
        for(ModelControllerNew cont : controllers)
        {
            if(cont != null)
            {
                builder.addAll(cont.getBakedModelFactory().getItemQuads(modelState, colorProvider));
            }
        }
        
        return new IPerspectiveAwareModel.MapWrapper(
                new SimpleItemModel(builder.build(),controllerPrimary.isShaded), state);
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
    public ICollisionHandler getCollisionHandler()
    {
        return controllerPrimary.getCollisionHandler();
    }

    @Override
    public boolean canRenderInLayer(EnumWorldBlockLayer layer)
    {
        return controllers[layer.ordinal()] != null;
    }

}
