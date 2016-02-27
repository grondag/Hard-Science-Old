package grondag.adversity.niceblock.newmodel;

import grondag.adversity.Adversity;
import grondag.adversity.niceblock.newmodel.QuadFactory.CubeInputs;
import grondag.adversity.niceblock.newmodel.color.ColorMap;
import grondag.adversity.niceblock.newmodel.color.IColorProvider;
import grondag.adversity.niceblock.newmodel.color.ColorMap.EnumColorMap;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraftforge.client.model.IFlexibleBakedModel;

public class BigTexModelFactory extends BakedModelFactory
{

    /**
     * Dimensions are color index, face ID, and texture variant.
     * Everything is instantiated lazily.
     */
    protected final List<BakedQuad>[][][] faceQuads;

    protected final static FacadeFaceSelector[] FACADE_FACE_SELECTORS = new FacadeFaceSelector[4096];
    
    public BigTexModelFactory(ModelControllerNew controller)
    {
        super(controller);
        faceQuads = new List[ModelState.MAX_COLOR_INDEX][][];
    }

    @Override
    public IBakedModel getBlockModel(ModelState modelState, IColorProvider colorProvider)
    {
       int colorIndex = modelState.getColorIndex();
        
        // allocate face quads for this color if not already done
        if(faceQuads[colorIndex] == null)
        {
            synchronized(faceQuads)
            {
                faceQuads[colorIndex] = new List[6][256];
            }
        }
        
        ColorMap colorMap = colorProvider.getColor(colorIndex);
        BigTexController controller = (BigTexController)this.controller;
        int facadeIndex = modelState.getClientShapeIndex(controller.renderLayer.ordinal());

        // ensure all needed faces are baked
        for(EnumFacing face : EnumFacing.values())
        {
            int faceIndex = FACADE_FACE_SELECTORS[facadeIndex].selectors[face.ordinal()];
            if(faceQuads[colorIndex][face.ordinal()][faceIndex] == null)
            {
                List<BakedQuad> newQuads = makeBigTexFace(colorMap.getColorMap(EnumColorMap.BASE), faceIndex, face);
                synchronized(faceQuads)
                {
                    faceQuads[colorIndex][face.ordinal()][faceIndex] = newQuads;
                }
            }
        }
        return new BigTexFacade(colorIndex, facadeIndex);
    }

    @Override
    public List<BakedQuad> getItemQuads(ModelState modelState, IColorProvider colorProvider)
    {
        CubeInputs cubeInputs = new CubeInputs();
        cubeInputs.u0 = 0;
        cubeInputs.v0 = 0;
        cubeInputs.u1 = 1;
        cubeInputs.v1 = 1;
        cubeInputs.isItem = true;
        cubeInputs.isOverlay = controller.renderLayer != EnumWorldBlockLayer.SOLID;
        cubeInputs.color = colorProvider.getColor(modelState.getColorIndex()).getColorMap(EnumColorMap.BASE);
        cubeInputs.textureSprite = 
                Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(controller.getTextureName(0));

        ImmutableList.Builder<BakedQuad> itemBuilder = new ImmutableList.Builder<BakedQuad>();

        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.UP));
        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.DOWN));
        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.EAST));
        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.WEST));
        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.NORTH));
        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.SOUTH));
        return itemBuilder.build();     
    }
    
    private List<BakedQuad> makeBigTexFace(int color, int faceIndex, EnumFacing face){

        int i = (faceIndex >> 4) & 15;
        int j = faceIndex & 15;
        
        CubeInputs cubeInputs = new CubeInputs();
        cubeInputs.color = color;
        cubeInputs.u0 = i;
        cubeInputs.v0 = j;
        cubeInputs.u1 = i + 1;
        cubeInputs.v1 = j + 1;
        cubeInputs.textureSprite = 
                Minecraft.getMinecraft().getTextureMapBlocks()
                    .getAtlasSprite(controller.getTextureName(0));
        
        return cubeInputs.makeFace(face);
    }


    private class BigTexFacade implements IBakedModel
    {
        private final int colorIndex; 
        private final int facadeIndex;

        protected BigTexFacade(int colorIndex, int facadeIndex)
        {
            this.colorIndex = colorIndex;
            this.facadeIndex = facadeIndex;
        }
   
        @Override
        public List<BakedQuad> getFaceQuads(EnumFacing face) {
            return faceQuads[colorIndex][face.ordinal()][FACADE_FACE_SELECTORS[facadeIndex].selectors[face.ordinal()]];
        }
        
        @Override
        public List<BakedQuad> getGeneralQuads() {
            return Collections.emptyList();
        }

        @Override
        public boolean isAmbientOcclusion() {
            return controller.isShaded;
        }

        @Override
        public boolean isGui3d() {
            return true;
        }

        @Override
        public boolean isBuiltInRenderer() {
            return false;
        }

        @Override
        public TextureAtlasSprite getParticleTexture() {
            Adversity.log.warn("Unsupported method call: SimpleItemModel.getParticleTexture()");
            return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel().getParticleTexture();
        }

        @Override
        public ItemCameraTransforms getItemCameraTransforms() {
            return ItemCameraTransforms.DEFAULT;
        }
    }

    static
    {
        int xOff = 0;
        for (int x = 0; x < 16; x++)
        {
            int yOff = 0;
            for (int y = 0; y < 16; y++)
            {
                int zOff = 0;
                for (int z = 0; z < 16; z++)
                {
                    FACADE_FACE_SELECTORS[x << 8 | y << 4 | z] = 
                            new FacadeFaceSelector((x + yOff & 0xF) << 4 | z + yOff & 0xF, (~(x + yOff) & 0xF) << 4 | z + yOff & 0xF, ~(y + xOff) & 0xF
                                | (~(z + xOff) & 0xF) << 4, ~(y + xOff) & 0xF | (z + xOff & 0xF) << 4, (~(x + zOff) & 0xF) << 4 | ~(y + zOff) & 0xF,
                                (x + zOff & 0xF) << 4 | ~(y + zOff) & 0xF);
 
                    zOff += 7;
                }
                yOff += 7;
            }
            xOff += 7;
        }
    }
}
