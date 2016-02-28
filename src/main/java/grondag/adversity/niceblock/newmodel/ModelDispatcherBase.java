package grondag.adversity.niceblock.newmodel;

import grondag.adversity.Adversity;
import grondag.adversity.niceblock.newmodel.color.IColorProvider;
import grondag.adversity.niceblock.support.ICollisionHandler;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent.Pre;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class ModelDispatcherBase implements ISmartBlockModel
{

    protected final String resourceName = UUID.randomUUID().toString();
    protected final IColorProvider colorProvider;
    protected final String particleTextureName;
    protected TextureAtlasSprite particleTexture;

    public ModelDispatcherBase(IColorProvider colorProvider, String particleTextureName)
    {
        this.colorProvider = colorProvider;
        this.particleTextureName = "adversity:blocks/" + particleTextureName;
    }
    
    public abstract void handleBakeEvent(ModelBakeEvent event);

    public abstract IBakedModel getItemModelForModelState(ModelState modelState);

    public abstract IBakedModel handleBlockState(IBlockState state);

    /** Calls controllers to update client state index(es)
     * in passed modelState.  Returns true if the client state changed.
     */
    public abstract boolean refreshClientShapeIndex(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos, ModelState modelState, boolean isCachedStateDirty);

    /**
     * Override if special collision handling is needed due to non-cubic shape.
     */
    public abstract ICollisionHandler getCollisionHandler();

    /**
     * Used by NiceBlock to control rendering.
     */
    public abstract boolean canRenderInLayer(EnumWorldBlockLayer layer);
    
    public String getModelResourceString()
    {
        return Adversity.MODID + ":" + resourceName;
    }

    public IColorProvider getColorProvider()
    {
        return colorProvider;
    }

    public TextureAtlasSprite getParticleTexture()
    {
        if(particleTexture == null)
        {
            particleTexture = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(particleTextureName);
        }
        return particleTexture;
    }
    
    /**
     * Override to registers all textures that will be needed for associated controllers. 
     * Be sure to call super so that parent implementation handles particle texture.
     * Happens before model bake.
     */
    public void handleTexturePreStitch(Pre event)
    {
        event.map.registerSprite(new ResourceLocation(particleTextureName));
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