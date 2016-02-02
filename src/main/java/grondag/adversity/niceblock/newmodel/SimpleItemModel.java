package grondag.adversity.niceblock.newmodel;

import grondag.adversity.Adversity;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IFlexibleBakedModel;

public class SimpleItemModel implements IFlexibleBakedModel
{
    private final List<BakedQuad> quads;
    private final boolean isShaded;

    protected SimpleItemModel(List<BakedQuad> quads, boolean isShaded)
    {
        this.quads = quads;
        this.isShaded = isShaded;
    }

    @Override
    public List<BakedQuad> getFaceQuads(EnumFacing face)
    {
        return Collections.emptyList();
    }

    @Override
    public List<BakedQuad> getGeneralQuads()
    {
        return quads;
    }

    @Override
    public boolean isAmbientOcclusion()
    {
        return isShaded;
    }

    @Override
    public boolean isGui3d()
    {
        return true;
    }

    @Override
    public boolean isBuiltInRenderer()
    {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture()
    {
        Adversity.log.warn("Unsupported method call: SimpleItemModel.getParticleTexture()");
        return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel().getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms()
    {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public VertexFormat getFormat()
    {
        return DefaultVertexFormats.ITEM;
    }
}
