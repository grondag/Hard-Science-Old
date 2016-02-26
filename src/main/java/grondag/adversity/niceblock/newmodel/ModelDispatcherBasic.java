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
public class ModelDispatcherBasic extends ModelDispatcherBase
{

    /** 
     * Cache for baked block models 
     * Dimensions are color, shape.
     * Conserves memory to put color first because most colors will never
     * be instantiated, but many shapes within a color probably will.
     */
    private IBakedModel[][] bakedBlockModels;

    private final ModelControllerNew controller;

    public ModelDispatcherBasic(IColorProvider colorProvider, String particleTextureName, ModelControllerNew controller)
    {
        super(colorProvider, particleTextureName);
        this.controller = controller;
        NiceBlockRegistrar.allDispatchers.add(this);
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
            bakedBlockModels = new IBakedModel[colorProvider.getColorCount()][];
        }
        else
        {
            bakedBlockModels = null;
        }
        controller.getBakedModelFactory().handleBakeEvent(event);
    }
    

    @Override
    @SideOnly(Side.CLIENT)
    public IBakedModel handleBlockState(IBlockState state)
    {
        IBakedModel retVal = null;

        // Really should ALWAYS be a NiceBlock instance but if someone goes
        // mucking about with the model registry crazy stuff could happen.
        if (state instanceof IExtendedBlockState && state.getBlock() instanceof NiceBlock)
        {
            IExtendedBlockState exState = (IExtendedBlockState) state;
            ModelState modelState = exState.getValue(NiceBlock.MODEL_STATE);

            if(bakedBlockModels[modelState.getColorIndex()] != null)
            {
                retVal = bakedBlockModels[modelState.getColorIndex()][modelState.getClientShapeIndex(0)];
            }
            
            if (retVal == null)
            {
                retVal = controller.getBakedModelFactory().getBlockModel(modelState, colorProvider);

                synchronized (bakedBlockModels)
                {
                    if(bakedBlockModels[modelState.getColorIndex()] == null)
                    {
                        bakedBlockModels[modelState.getColorIndex()] = new IBakedModel[controller.getShapeCount()];
                    }
                    bakedBlockModels[modelState.getColorIndex()][modelState.getClientShapeIndex(0)] = retVal;
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
        
        return new IPerspectiveAwareModel.MapWrapper(
                new SimpleItemModel(
                        controller.getBakedModelFactory().getItemQuads(modelState, colorProvider), 
                        controller.isShaded), 
            state);
    }

    @Override
    public boolean refreshClientShapeIndex(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos, ModelState modelState)
    {
        int oldShapeIndex = modelState.getClientShapeIndex(0);
        modelState.setClientShapeIndex(controller.getClientShapeIndex(block, state, world, pos), 0);
        return modelState.getClientShapeIndex(0) != oldShapeIndex;
    }

    @Override
    public ICollisionHandler getCollisionHandler()
    {
        return controller.getCollisionHandler();
    }

    @Override
    public boolean canRenderInLayer(EnumWorldBlockLayer layer)
    {
        return controller.canRenderInLayer(layer);
    }

}
