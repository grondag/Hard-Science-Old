package grondag.adversity.niceblock.model;

import grondag.adversity.niceblock.NiceColor;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent.Post;
import net.minecraftforge.client.event.TextureStitchEvent.Pre;
import net.minecraftforge.client.model.IFlexibleBakedModel;

import com.google.common.collect.ImmutableList;

public class NiceModelBigTex extends NiceModel
{
    protected final ModelControllerBigTex controller;

    /**
     * Holds the baked models that will be returned for rendering based on extended state. Array is populated during the handleBake event.
     */
    protected final List<BakedQuad>[][] faceQuads;

    protected final BigTexFacade[] facadeModels;
    
    protected BigTexFacade itemModel;

    protected TextureAtlasSprite textureSprite;

    protected final NiceColor color;

    /**
     * Registers all textures that will be needed for this style/substance. Happens before model bake.
     */
    @Override
    public void handleTexturePreStitch(Pre event)
    {
        event.map.registerSprite(new ResourceLocation(getController().getFirstTextureName(meta)));
    }

    @Override
    public void handleTexturePostStitch(Post event)
    {
        textureSprite = event.map.getAtlasSprite(getController().getFirstTextureName(meta));
        particleTexture = textureSprite;
    }

    protected NiceModelBigTex(ModelControllerBigTex controller, int meta, NiceColor color)
    {
        super(meta);
        this.color = color;
        this.controller = controller;
        faceQuads = new List[6][256];
        facadeModels = new BigTexFacade[4096];
    }

    @Override
    public IModelController getController()
    {
        return controller;
    }

    @Override
    public void handleBakeEvent(ModelBakeEvent event)
    {
        textureSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(getController().getFirstTextureName(meta));

        for (int i = 0; i < 16; i++)
        {
            for (int j = 0; j < 16; j++)
            {
                int index = i << 4 | j;
                float u0 = controller.flipU ? 16 - i : i;
                float v0 = controller.flipV ? 16 - j : j;
                float u1 = u0 + (controller.flipU ? -1 : 1);
                float v1 = v0 + (controller.flipV ? -1 : 1);

                faceQuads[EnumFacing.UP.ordinal()][index] = new ImmutableList.Builder<BakedQuad>().add(
                        createQuad(new Vertex(0, 1, 0, u0, v0), new Vertex(0, 1, 1, u0, v1), new Vertex(1, 1, 1, u1, v1), new Vertex(1, 1, 0, u1, v0),
                                EnumFacing.UP, textureSprite, controller.textureRotation, color.base)).build();

                faceQuads[EnumFacing.DOWN.ordinal()][index] = new ImmutableList.Builder<BakedQuad>().add(
                        createQuad(new Vertex(1, 0, 1, u0, v1), new Vertex(0, 0, 1, u1, v1), new Vertex(0, 0, 0, u1, v0), new Vertex(1, 0, 0, u0, v0),
                                EnumFacing.DOWN, textureSprite, controller.textureRotation, color.base)).build();

                faceQuads[EnumFacing.WEST.ordinal()][index] = new ImmutableList.Builder<BakedQuad>().add(
                        createQuad(new Vertex(0, 0, 0, u0, v1), new Vertex(0, 0, 1, u1, v1), new Vertex(0, 1, 1, u1, v0), new Vertex(0, 1, 0, u0, v0),
                                EnumFacing.WEST, textureSprite, controller.textureRotation, color.base)).build();

                faceQuads[EnumFacing.EAST.ordinal()][index] = new ImmutableList.Builder<BakedQuad>().add(
                        createQuad(new Vertex(1, 0, 0, u1, v1), new Vertex(1, 1, 0, u1, v0), new Vertex(1, 1, 1, u0, v0), new Vertex(1, 0, 1, u0, v1),
                                EnumFacing.EAST, textureSprite, controller.textureRotation, color.base)).build();

                faceQuads[EnumFacing.NORTH.ordinal()][index] = new ImmutableList.Builder<BakedQuad>().add(
                        createQuad(new Vertex(0, 0, 0, u1, v1), new Vertex(0, 1, 0, u1, v0), new Vertex(1, 1, 0, u0, v0), new Vertex(1, 0, 0, u0, v1),
                                EnumFacing.NORTH, textureSprite, controller.textureRotation, color.base)).build();

                faceQuads[EnumFacing.SOUTH.ordinal()][index] = new ImmutableList.Builder<BakedQuad>().add(
                        createQuad(new Vertex(0, 0, 1, u0, v1), new Vertex(1, 0, 1, u1, v1), new Vertex(1, 1, 1, u1, v0), new Vertex(0, 1, 1, u0, v0),
                                EnumFacing.SOUTH, textureSprite, controller.textureRotation, color.base)).build();
            }
        }

        int xOff = 0;
        for (int x = 0; x < 16; x++)
        {
            int yOff = 0;
            for (int y = 0; y < 16; y++)
            {
                int zOff = 0;
                for (int z = 0; z < 16; z++)
                {

                    // Really can't be null because all cases handled.
                    // Initializing to silence eclipse warning.
                    BigTexFacade facade = null;

                    // clockwise rotations
                    switch (controller.textureRotation)
                    {
                    case ROTATE_NONE:
                        facade = makeFacadeFromInts((x + yOff & 0xF) << 4 | z + yOff & 0xF, (~(x + yOff) & 0xF) << 4 | z + yOff & 0xF, ~(y + xOff) & 0xF
                                | (~(z + xOff) & 0xF) << 4, ~(y + xOff) & 0xF | (z + xOff & 0xF) << 4, (~(x + zOff) & 0xF) << 4 | ~(y + zOff) & 0xF,
                                (x + zOff & 0xF) << 4 | ~(y + zOff) & 0xF);
                        break;
                    case ROTATE_90:
                        facade = makeFacadeFromInts((z + yOff & 0xF) << 4 | ~(x + yOff) & 0xF, (z + yOff & 0xF) << 4 | x + yOff & 0xF, z + xOff & 0xF
                                | (~(y + xOff) & 0xF) << 4, ~(z + xOff) & 0xF | (~(y + xOff) & 0xF) << 4, (~(y + zOff) & 0xF) << 4 | x + zOff & 0xF,
                                (~(y + zOff) & 0xF) << 4 | ~(x + zOff) & 0xF);
                        break;
                    case ROTATE_180:
                        facade = makeFacadeFromInts((~(x + yOff) & 0xF) << 4 | ~(z + yOff) & 0xF, (x + yOff & 0xF) << 4 | ~(z + yOff) & 0xF, y + xOff & 0xF
                                | (z + xOff & 0xF) << 4, y + xOff & 0xF | (~(z + xOff) & 0xF) << 4, (x + zOff & 0xF) << 4 | y + zOff & 0xF,
                                (~(x + zOff) & 0xF) << 4 | y + zOff & 0xF);
                        break;
                    case ROTATE_270:
                        facade = makeFacadeFromInts((~(z + yOff) & 0xF) << 4 | x + yOff & 0xF, (~(z + yOff) & 0xF) << 4 | ~(x + yOff) & 0xF, ~(z + xOff) & 0xF
                                | (y + xOff & 0xF) << 4, z + xOff & 0xF | (y + xOff & 0xF) << 4, (y + zOff & 0xF) << 4 | ~(x + zOff) & 0xF,
                                (y + zOff & 0xF) << 4 | x + zOff & 0xF);
                        break;
                    }

                    facadeModels[x << 8 | y << 4 | z] = facade;

                    zOff += 7;
                }
                yOff += 7;
            }
            xOff += 7;
        }
        
        itemModel = facadeModels[0];
//        itemModel = new BigTexFacade(
//                new ImmutableList.Builder<BakedQuad>().add(
//                        NiceModel.tintedBakedQuad(faceQuads[EnumFacing.UP.ordinal()][0].get(0), color.base)).build(),
//                new ImmutableList.Builder<BakedQuad>().add(
//                        NiceModel.tintedBakedQuad(faceQuads[EnumFacing.DOWN.ordinal()][0].get(0), color.base)).build(),
//                new ImmutableList.Builder<BakedQuad>().add(
//                        NiceModel.tintedBakedQuad(faceQuads[EnumFacing.EAST.ordinal()][0].get(0), color.base)).build(),
//                new ImmutableList.Builder<BakedQuad>().add(
//                        NiceModel.tintedBakedQuad(faceQuads[EnumFacing.WEST.ordinal()][0].get(0), color.base)).build(),                      
//                new ImmutableList.Builder<BakedQuad>().add(
//                        NiceModel.tintedBakedQuad(faceQuads[EnumFacing.NORTH.ordinal()][0].get(0), color.base)).build(),
//                new ImmutableList.Builder<BakedQuad>().add(
//                        NiceModel.tintedBakedQuad(faceQuads[EnumFacing.SOUTH.ordinal()][0].get(0), color.base)).build()
//        );
    }

    @Override
    public IBakedModel getModelVariant(int variantID)
    {
        return facadeModels[variantID];
    }

    @Override
    public TextureAtlasSprite getParticleTexture()
    {
        return textureSprite;
    }

    
    
    @Override
    public IBakedModel handleItemState(ItemStack stack)
    {
        return itemModel;
    }

    private BigTexFacade makeFacadeFromInts(int upFace, int downFace, int eastFace, int westFace, int northFace, int southFace){
        return new BigTexFacade(
                faceQuads[EnumFacing.UP.ordinal()][upFace],
                faceQuads[EnumFacing.DOWN.ordinal()][downFace], 
                faceQuads[EnumFacing.EAST.ordinal()][eastFace], 
                faceQuads[EnumFacing.WEST.ordinal()][westFace], 
                faceQuads[EnumFacing.NORTH.ordinal()][northFace], 
                faceQuads[EnumFacing.SOUTH.ordinal()][southFace]
            );
    }

    private class BigTexFacade implements IFlexibleBakedModel
    {
        private final List<BakedQuad>[] faces = new List[6];

        protected BigTexFacade(List<BakedQuad> upFace, List<BakedQuad> downFace, List<BakedQuad> eastFace, List<BakedQuad> westFace, List<BakedQuad> northFace, List<BakedQuad> southFace)
        {
            faces[EnumFacing.UP.ordinal()] = upFace;
            faces[EnumFacing.DOWN.ordinal()] = downFace;
            faces[EnumFacing.EAST.ordinal()] = eastFace;
            faces[EnumFacing.WEST.ordinal()] = westFace;
            faces[EnumFacing.NORTH.ordinal()] = northFace;
            faces[EnumFacing.SOUTH.ordinal()] = southFace;
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
            return controller.isShaded;
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
            return textureSprite;
        }

        @Override
        public ItemCameraTransforms getItemCameraTransforms()
        {
            return ItemCameraTransforms.DEFAULT;
        }

        @Override
        public VertexFormat getFormat()
        {
            return DefaultVertexFormats.BLOCK;
        }

    }

}
