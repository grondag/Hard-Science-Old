package grondag.adversity.niceblock.newmodel;

import grondag.adversity.Adversity;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class ModelDispatcherBase implements ISmartBlockModel
{

    protected final String resourceName = UUID.randomUUID().toString();

    public abstract void handleBakeEvent(ModelBakeEvent event);

    public abstract IBakedModel getItemModelForModelState(ModelState modelState);

    @Override
    public abstract IBakedModel handleBlockState(IBlockState state);

    @Override
    public abstract TextureAtlasSprite getParticleTexture();
    
    public String getModelResourceString()
    {
        return Adversity.MODID + ":" + resourceName;
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
    public ItemCameraTransforms getItemCameraTransforms()
    {
        Adversity.log.warn("Unsupported method call: ModelDispatcher.getItemCameraTransforms()");
        return ItemCameraTransforms.DEFAULT;
    }


}