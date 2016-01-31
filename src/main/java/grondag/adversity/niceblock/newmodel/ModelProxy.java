package grondag.adversity.niceblock.newmodel;

import grondag.adversity.Adversity;

import java.util.Collections;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.client.model.ISmartItemModel;

public class ModelProxy implements ISmartItemModel
{

    protected final BlockModelHelper blockModelHelper;

    public ModelProxy(BlockModelHelper blockModelHelper)
    {
        this.blockModelHelper = blockModelHelper;
    }

    @Override
    public IBakedModel handleItemState(ItemStack stack)
    {
        return blockModelHelper.dispatcher.getItemModelForModelState(blockModelHelper.getModelStateForItem(stack).getModelIndex());
    }

    // /// REMAINING METHODS SHOULD NEVER BE CALLED

    @Override
    public List<BakedQuad> getFaceQuads(EnumFacing p_177551_1_)
    {
        Adversity.log.warn("Unsupported method call: NiceModelDispatcher.getFaceQuads()");
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<BakedQuad> getGeneralQuads()
    {
        Adversity.log.warn("Unsupported method call: NiceModelDispatcher.getGeneralQuads()");
        return Collections.EMPTY_LIST;
    }

    @Override
    public boolean isAmbientOcclusion()
    {
        Adversity.log.warn("Unsupported method call: NiceModelDispatcher.isAmbientOcclusion()");
        return false;
    }

    @Override
    public boolean isGui3d()
    {
        Adversity.log.warn("Unsupported method call: NiceModelDispatcher.isGui3d()");
        return false;
    }

    @Override
    public boolean isBuiltInRenderer()
    {
        Adversity.log.warn("Unsupported method call: NiceModelDispatcher.isBuiltInRenderer()");
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture()
    {
        Adversity.log.warn("Unsupported method call: NiceModelDispatcher.getParticleTexture()");
        return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel().getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms()
    {
        Adversity.log.warn("Unsupported method call: NiceModelDispatcher.getItemCameraTransforms()");
        return ItemCameraTransforms.DEFAULT;
    }

}
