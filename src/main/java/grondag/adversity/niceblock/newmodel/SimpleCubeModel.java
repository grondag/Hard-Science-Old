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

public class SimpleCubeModel implements IFlexibleBakedModel
{
    private final List<BakedQuad>[] faces;
    private final boolean isShaded;

    protected SimpleCubeModel(List<BakedQuad>[] faces, boolean isShaded)
    {
        this.faces = faces;
        this.isShaded = isShaded;
    }

    @Override
    public List<BakedQuad> getFaceQuads(EnumFacing face)
    {
        return faces[face.ordinal()];
    }

    @Override
    public List<BakedQuad> getGeneralQuads()
    {
        return Collections.emptyList();
    }

    @Override
    public boolean isAmbientOcclusion()
    {
        return isShaded;
    }

    @Override
    public boolean isGui3d()
    {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer()
    {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture()
    {
        Adversity.log.warn("Unsupported method call: SimpleCubeModel.getParticleTexture()");
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
