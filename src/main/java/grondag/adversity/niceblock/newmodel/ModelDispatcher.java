package grondag.adversity.niceblock.newmodel;

import grondag.adversity.Adversity;

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
import net.minecraft.util.EnumFacing;
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
public class ModelDispatcher implements ISmartBlockModel
{

    /** cache for baked block models */
    private final IBakedModel[] bakedBlockModels;

    /** cache for baked item models */
    private final IBakedModel[] bakedItemModels;
    
    public final ModelControllerNew controller;

    public ModelDispatcher(ModelControllerNew controller)
    {
        this.controller = controller;
        NiceBlockRegistrar.allDispatchers.add(this);
        
        if(FMLCommonHandler.instance().getSide() == Side.CLIENT) 
        {
            bakedBlockModels = new IBakedModel[controller.getBakedBlockModelCount()];
            bakedItemModels = new IBakedModel[controller.getBakedItemModelCount()];
        }
        else
        {
            bakedBlockModels = null;
            bakedItemModels = null;
        }
    }
    
    public String getModelResourceString()
    {
        return Adversity.MODID + ":" + controller.styleName;
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

            retVal = bakedBlockModels[modelState.getModelIndex()];

            if (retVal == null)
            {
                retVal = controller.getBakedModelFactory().getBlockModel(modelState);

                synchronized (bakedBlockModels)
                {
                    bakedBlockModels[modelState.getModelIndex()] = retVal;
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

    @SideOnly(Side.CLIENT)
    public IBakedModel getItemModelForModelState(ModelState modelState)
    {
        
        if(bakedItemModels[modelState.getModelIndex()] == null){
            
            /**
             * Enable perspective handling.
             */
                
            TRSRTransformation thirdperson = TRSRTransformation.blockCenterToCorner(new TRSRTransformation(
                    new Vector3f(0, 1.5f / 16, -2.75f / 16),
                    TRSRTransformation.quatFromYXZDegrees(new Vector3f(10, -45, 170)),
                    new Vector3f(0.375f, 0.375f, 0.375f),
                    null));

            IModelState state = new SimpleModelState(ImmutableMap.of(TransformType.THIRD_PERSON, thirdperson), Optional.of(TRSRTransformation.identity()));
            
            bakedItemModels[modelState.getModelIndex()] = 
                new IPerspectiveAwareModel.MapWrapper(
                    new SimpleItemModel(
                            controller.getBakedModelFactory().getItemQuads(modelState), 
                            controller.isShaded), 
                state);
        }
        
        return bakedItemModels[modelState.getModelIndex()];
    }

    // REMAINING METHODS SHOULD NEVER BE CALLED

    @Override
    public List<BakedQuad> getFaceQuads(EnumFacing p_177551_1_)
    {
        Adversity.log.warn("Unsupported method call: ModelDispatcher.getFaceQuads()");
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<BakedQuad> getGeneralQuads()
    {
        Adversity.log.warn("Unsupported method call: ModelDispatcher.getGeneralQuads()");
        return Collections.EMPTY_LIST;
    }

    @Override
    public boolean isAmbientOcclusion()
    {
        Adversity.log.warn("Unsupported method call: ModelDispatcher.isAmbientOcclusion()");
        return false;
    }

    @Override
    public boolean isGui3d()
    {
        Adversity.log.warn("Unsupported method call: ModelDispatcher.isGui3d()");
        return false;
    }

    @Override
    public boolean isBuiltInRenderer()
    {
        Adversity.log.warn("Unsupported method call: ModelDispatcher.isBuiltInRenderer()");
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture()
    {
        Adversity.log.warn("Unsupported method call: ModelDispatcher.getParticleTexture()");
        return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel().getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms()
    {
        Adversity.log.warn("Unsupported method call: ModelDispatcher.getItemCameraTransforms()");
        return ItemCameraTransforms.DEFAULT;
    }

}
