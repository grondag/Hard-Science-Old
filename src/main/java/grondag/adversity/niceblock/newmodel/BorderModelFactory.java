package grondag.adversity.niceblock.newmodel;

import grondag.adversity.Adversity;
import grondag.adversity.niceblock.newmodel.QuadFactory.CubeInputs;
import grondag.adversity.niceblock.newmodel.color.ColorVector;
import grondag.adversity.niceblock.newmodel.color.IColorProvider;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraftforge.client.event.ModelBakeEvent;

public class BorderModelFactory extends BakedModelFactory
{

    /**
     * Dimensions are color index, alternate texture index, face ID, and texture variant.
     * Everything is instantiated lazily.
     */
    protected final List<BakedQuad>[][][][] faceQuads;
    
    protected final static FaceQuadInputs[][] FACE_INPUTS = new FaceQuadInputs[6][48];
    protected final static FacadeFaceSelector[] FACADE_FACE_SELECTORS = new FacadeFaceSelector[386];

    public BorderModelFactory(BorderController controller)
    {
        super(controller);
        faceQuads = new List[ModelState.MAX_COLOR_INDEX][][][];
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
                faceQuads[colorIndex] = new List[controller.alternateTextureCount][6][48];
            }
        }
        
        ColorVector colorVector = colorProvider.getColor(colorIndex);
        BorderController controller = (BorderController)this.controller;
        int facadeIndex = controller.getFacadeIndexFromModelState(modelState);
        int alternateTextureIndex = controller.getAlternateTextureIndexFromModelState(modelState);

        // ensure all needed faces are baked
        for(EnumFacing face : EnumFacing.values())
        {
            int faceIndex = FACADE_FACE_SELECTORS[facadeIndex].selectors[face.ordinal()];
            if(faceQuads[colorIndex][alternateTextureIndex][face.ordinal()][faceIndex] == null)
            {
                List<BakedQuad> newQuads = makeBorderFace(colorVector.border, alternateTextureIndex, faceIndex, face);
                synchronized(faceQuads)
                {
                    faceQuads[colorIndex][alternateTextureIndex][face.ordinal()][faceIndex] = newQuads;
                }
            }
        }
 
        return new BorderFacade(colorIndex, alternateTextureIndex, facadeIndex);
    }

    @Override
    public List<BakedQuad> getItemQuads(ModelState modelState, IColorProvider colorProvider)
    {
        CubeInputs cubeInputs = new CubeInputs();
        cubeInputs.u0 = 0;
        cubeInputs.v0 = 0;
        cubeInputs.u1 = 16;
        cubeInputs.v1 = 16;
        cubeInputs.isItem = true;
        cubeInputs.isOverlay = controller.renderLayer != EnumWorldBlockLayer.SOLID;
        cubeInputs.color = colorProvider.getColor(modelState.getColorIndex()).border;
        // offset 4 is all borders
        cubeInputs.textureSprite = 
                Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(controller.getTextureName(4));

        ImmutableList.Builder<BakedQuad> itemBuilder = new ImmutableList.Builder<BakedQuad>();

        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.UP));
        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.DOWN));
        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.EAST));
        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.WEST));
        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.NORTH));
        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.SOUTH));
        return itemBuilder.build(); 
    }

    private List<BakedQuad> makeBorderFace(int color, int alternateTextureIndex, int faceIndex, EnumFacing face){
        
        FaceQuadInputs inputs = FACE_INPUTS[face.ordinal()][faceIndex];
        
        if(inputs == null)
        {
            return Collections.emptyList();
        }
        
        CubeInputs cubeInputs = new CubeInputs();
        cubeInputs.color = color;
        cubeInputs.textureRotation = inputs.rotation;
        cubeInputs.rotateBottom = true;
        cubeInputs.u0 = inputs.flipU ? 16 : 0;
        cubeInputs.v0 = inputs.flipV ? 16 : 0;
        cubeInputs.u1 = inputs.flipU ? 0 : 16;
        cubeInputs.v1 = inputs.flipV ? 0 : 16;
        cubeInputs.textureSprite = 
                Minecraft.getMinecraft().getTextureMapBlocks()
                    .getAtlasSprite(controller.getTextureName(alternateTextureIndex * 16 + inputs.textureOffset));
        
        return cubeInputs.makeFace(face);
    }

    private class BorderFacade implements IBakedModel 
    {
        private final int colorIndex; 
        private final int alternateTextureIndex;
        private final int facadeIndex;
        
        public BorderFacade(int colorIndex, int alternateTextureIndex, int facadeIndex) {
            this.colorIndex = colorIndex;
            this.alternateTextureIndex = alternateTextureIndex;
            this.facadeIndex = facadeIndex;
        }

        @Override
        public List<BakedQuad> getFaceQuads(EnumFacing face) {
            return faceQuads[colorIndex][alternateTextureIndex][face.ordinal()][FACADE_FACE_SELECTORS[facadeIndex].selectors[face.ordinal()]];
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
    
    private static class FaceQuadInputs
    {
        protected final int textureOffset;
        protected final Rotation rotation;
        protected final boolean flipU;
        protected final boolean flipV;
        
        protected FaceQuadInputs(int textureOffset, Rotation rotation, boolean flipU, boolean flipV)
        {
            this.textureOffset = textureOffset;
            this.rotation = rotation;
            this.flipU = flipU;
            this.flipV = flipV;
        }
    }
    
    private static class FacadeFaceSelector
    {
        protected final byte[] selectors = new byte[EnumFacing.values().length];
        
        protected FacadeFaceSelector(int upFace, int downFace, int eastFace, int westFace, int northFace, int southFace) {
            selectors[EnumFacing.UP.ordinal()] = (byte) upFace;
            selectors[EnumFacing.DOWN.ordinal()] = (byte) downFace;
            selectors[EnumFacing.EAST.ordinal()] = (byte) eastFace;
            selectors[EnumFacing.WEST.ordinal()] = (byte) westFace;
            selectors[EnumFacing.NORTH.ordinal()] = (byte) northFace;
            selectors[EnumFacing.SOUTH.ordinal()] = (byte) southFace;
        }
    }
    
    static
    {
        for(EnumFacing face: EnumFacing.values()){
            FACE_INPUTS[face.ordinal()][0] = new FaceQuadInputs( 4, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][1] = new FaceQuadInputs( 3, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][2] = new FaceQuadInputs( 3, Rotation.ROTATE_270, false, false);
            FACE_INPUTS[face.ordinal()][3] = new FaceQuadInputs( 1, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][4] = new FaceQuadInputs( 3, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][5] = new FaceQuadInputs( 2, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][6] = new FaceQuadInputs( 1, Rotation.ROTATE_270, false, false);
            FACE_INPUTS[face.ordinal()][7] = new FaceQuadInputs( 0, Rotation.ROTATE_270, false, false);
            FACE_INPUTS[face.ordinal()][8] = new FaceQuadInputs( 3, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][9] = new FaceQuadInputs( 1, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][10] = new FaceQuadInputs( 2, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][11] = new FaceQuadInputs( 0, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][12] = new FaceQuadInputs( 1, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][13] = new FaceQuadInputs( 0, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][14] = new FaceQuadInputs( 0, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][15] = null; //new ImmutableList.Builder<BakedQuad>().build(); // NO BORDER
            FACE_INPUTS[face.ordinal()][16] = new FaceQuadInputs( 7, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][17] = new FaceQuadInputs( 7, Rotation.ROTATE_270, false, false);
            FACE_INPUTS[face.ordinal()][18] = new FaceQuadInputs( 7, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][19] = new FaceQuadInputs( 7, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][20] = new FaceQuadInputs( 5, Rotation.ROTATE_270, false, false);
            FACE_INPUTS[face.ordinal()][21] = new FaceQuadInputs( 5, Rotation.ROTATE_270, true, false);
            FACE_INPUTS[face.ordinal()][22] = new FaceQuadInputs( 6, Rotation.ROTATE_270, false, false);
            FACE_INPUTS[face.ordinal()][23] = new FaceQuadInputs( 5, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][24] = new FaceQuadInputs( 5, Rotation.ROTATE_180, true, false);
            FACE_INPUTS[face.ordinal()][25] = new FaceQuadInputs( 6, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][26] = new FaceQuadInputs( 5, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][27] = new FaceQuadInputs( 5, Rotation.ROTATE_90, true, false);
            FACE_INPUTS[face.ordinal()][28] = new FaceQuadInputs( 6, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][29] = new FaceQuadInputs( 5, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][30] = new FaceQuadInputs( 5, Rotation.ROTATE_NONE, true, false);
            FACE_INPUTS[face.ordinal()][31] = new FaceQuadInputs( 6, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][32] = null; //new ImmutableList.Builder<BakedQuad>().build(); // NULL FACE
            FACE_INPUTS[face.ordinal()][33] = new FaceQuadInputs( 8, Rotation.ROTATE_270, false, false);
            FACE_INPUTS[face.ordinal()][34] = new FaceQuadInputs( 8, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][35] = new FaceQuadInputs( 9, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][36] = new FaceQuadInputs( 8, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][37] = new FaceQuadInputs( 10, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][38] = new FaceQuadInputs( 9, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][39] = new FaceQuadInputs( 11, Rotation.ROTATE_270, false, false);
            FACE_INPUTS[face.ordinal()][40] = new FaceQuadInputs( 8, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][41] = new FaceQuadInputs( 9, Rotation.ROTATE_270, false, false);
            FACE_INPUTS[face.ordinal()][42] = new FaceQuadInputs( 10, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][43] = new FaceQuadInputs( 11, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][44] = new FaceQuadInputs( 9, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][45] = new FaceQuadInputs( 11, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][46] = new FaceQuadInputs( 11, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][47] = new FaceQuadInputs( 12, Rotation.ROTATE_NONE, false, false);
        }
        
        FACADE_FACE_SELECTORS[0] = new FacadeFaceSelector(0, 0, 0, 0, 0, 0);
        FACADE_FACE_SELECTORS[1] = new FacadeFaceSelector(32, 0, 1, 1, 1, 1);
        FACADE_FACE_SELECTORS[2] = new FacadeFaceSelector(0, 32, 4, 4, 4, 4);
        FACADE_FACE_SELECTORS[3] = new FacadeFaceSelector(2, 2, 32, 0, 8, 2);
        FACADE_FACE_SELECTORS[4] = new FacadeFaceSelector(8, 8, 0, 32, 2, 8);
        FACADE_FACE_SELECTORS[5] = new FacadeFaceSelector(1, 4, 2, 8, 32, 0);
        FACADE_FACE_SELECTORS[6] = new FacadeFaceSelector(4, 1, 8, 2, 0, 32);
        FACADE_FACE_SELECTORS[7] = new FacadeFaceSelector(32, 32, 5, 5, 5, 5);
        FACADE_FACE_SELECTORS[8] = new FacadeFaceSelector(10, 10, 32, 32, 10, 10);
        FACADE_FACE_SELECTORS[9] = new FacadeFaceSelector(5, 5, 10, 10, 32, 32);
        FACADE_FACE_SELECTORS[10] = new FacadeFaceSelector(32, 2, 32, 1, 9, 3);
        FACADE_FACE_SELECTORS[11] = new FacadeFaceSelector(32, 2, 32, 1, 19, 16);
        FACADE_FACE_SELECTORS[12] = new FacadeFaceSelector(32, 8, 1, 32, 3, 9);
        FACADE_FACE_SELECTORS[13] = new FacadeFaceSelector(32, 8, 1, 32, 16, 19);
        FACADE_FACE_SELECTORS[14] = new FacadeFaceSelector(32, 4, 3, 9, 32, 1);
        FACADE_FACE_SELECTORS[15] = new FacadeFaceSelector(32, 4, 16, 19, 32, 1);
        FACADE_FACE_SELECTORS[16] = new FacadeFaceSelector(32, 1, 9, 3, 1, 32);
        FACADE_FACE_SELECTORS[17] = new FacadeFaceSelector(32, 1, 19, 16, 1, 32);
        FACADE_FACE_SELECTORS[18] = new FacadeFaceSelector(2, 32, 32, 4, 12, 6);
        FACADE_FACE_SELECTORS[19] = new FacadeFaceSelector(2, 32, 32, 4, 18, 17);
        FACADE_FACE_SELECTORS[20] = new FacadeFaceSelector(8, 32, 4, 32, 6, 12);
        FACADE_FACE_SELECTORS[21] = new FacadeFaceSelector(8, 32, 4, 32, 17, 18);
        FACADE_FACE_SELECTORS[22] = new FacadeFaceSelector(1, 32, 6, 12, 32, 4);
        FACADE_FACE_SELECTORS[23] = new FacadeFaceSelector(1, 32, 17, 18, 32, 4);
        FACADE_FACE_SELECTORS[24] = new FacadeFaceSelector(4, 32, 12, 6, 4, 32);
        FACADE_FACE_SELECTORS[25] = new FacadeFaceSelector(4, 32, 18, 17, 4, 32);
        FACADE_FACE_SELECTORS[26] = new FacadeFaceSelector(3, 6, 32, 8, 32, 2);
        FACADE_FACE_SELECTORS[27] = new FacadeFaceSelector(16, 17, 32, 8, 32, 2);
        FACADE_FACE_SELECTORS[28] = new FacadeFaceSelector(6, 3, 32, 2, 8, 32);
        FACADE_FACE_SELECTORS[29] = new FacadeFaceSelector(17, 16, 32, 2, 8, 32);
        FACADE_FACE_SELECTORS[30] = new FacadeFaceSelector(9, 12, 2, 32, 32, 8);
        FACADE_FACE_SELECTORS[31] = new FacadeFaceSelector(19, 18, 2, 32, 32, 8);
        FACADE_FACE_SELECTORS[32] = new FacadeFaceSelector(12, 9, 8, 32, 2, 32);
        FACADE_FACE_SELECTORS[33] = new FacadeFaceSelector(18, 19, 8, 32, 2, 32);
        FACADE_FACE_SELECTORS[34] = new FacadeFaceSelector(32, 10, 32, 32, 11, 11);
        FACADE_FACE_SELECTORS[35] = new FacadeFaceSelector(32, 10, 32, 32, 23, 24);
        FACADE_FACE_SELECTORS[36] = new FacadeFaceSelector(32, 10, 32, 32, 24, 23);
        FACADE_FACE_SELECTORS[37] = new FacadeFaceSelector(32, 10, 32, 32, 25, 25);
        FACADE_FACE_SELECTORS[38] = new FacadeFaceSelector(32, 5, 11, 11, 32, 32);
        FACADE_FACE_SELECTORS[39] = new FacadeFaceSelector(32, 5, 24, 23, 32, 32);
        FACADE_FACE_SELECTORS[40] = new FacadeFaceSelector(32, 5, 23, 24, 32, 32);
        FACADE_FACE_SELECTORS[41] = new FacadeFaceSelector(32, 5, 25, 25, 32, 32);
        FACADE_FACE_SELECTORS[42] = new FacadeFaceSelector(10, 32, 32, 32, 14, 14);
        FACADE_FACE_SELECTORS[43] = new FacadeFaceSelector(10, 32, 32, 32, 30, 29);
        FACADE_FACE_SELECTORS[44] = new FacadeFaceSelector(10, 32, 32, 32, 29, 30);
        FACADE_FACE_SELECTORS[45] = new FacadeFaceSelector(10, 32, 32, 32, 31, 31);
        FACADE_FACE_SELECTORS[46] = new FacadeFaceSelector(5, 32, 14, 14, 32, 32);
        FACADE_FACE_SELECTORS[47] = new FacadeFaceSelector(5, 32, 29, 30, 32, 32);
        FACADE_FACE_SELECTORS[48] = new FacadeFaceSelector(5, 32, 30, 29, 32, 32);
        FACADE_FACE_SELECTORS[49] = new FacadeFaceSelector(5, 32, 31, 31, 32, 32);
        FACADE_FACE_SELECTORS[50] = new FacadeFaceSelector(11, 14, 32, 32, 32, 10);
        FACADE_FACE_SELECTORS[51] = new FacadeFaceSelector(24, 29, 32, 32, 32, 10);
        FACADE_FACE_SELECTORS[52] = new FacadeFaceSelector(23, 30, 32, 32, 32, 10);
        FACADE_FACE_SELECTORS[53] = new FacadeFaceSelector(25, 31, 32, 32, 32, 10);
        FACADE_FACE_SELECTORS[54] = new FacadeFaceSelector(32, 32, 7, 13, 32, 5);
        FACADE_FACE_SELECTORS[55] = new FacadeFaceSelector(32, 32, 20, 27, 32, 5);
        FACADE_FACE_SELECTORS[56] = new FacadeFaceSelector(32, 32, 21, 26, 32, 5);
        FACADE_FACE_SELECTORS[57] = new FacadeFaceSelector(32, 32, 22, 28, 32, 5);
        FACADE_FACE_SELECTORS[58] = new FacadeFaceSelector(14, 11, 32, 32, 10, 32);
        FACADE_FACE_SELECTORS[59] = new FacadeFaceSelector(29, 24, 32, 32, 10, 32);
        FACADE_FACE_SELECTORS[60] = new FacadeFaceSelector(30, 23, 32, 32, 10, 32);
        FACADE_FACE_SELECTORS[61] = new FacadeFaceSelector(31, 25, 32, 32, 10, 32);
        FACADE_FACE_SELECTORS[62] = new FacadeFaceSelector(32, 32, 13, 7, 5, 32);
        FACADE_FACE_SELECTORS[63] = new FacadeFaceSelector(32, 32, 27, 20, 5, 32);
        FACADE_FACE_SELECTORS[64] = new FacadeFaceSelector(32, 32, 26, 21, 5, 32);
        FACADE_FACE_SELECTORS[65] = new FacadeFaceSelector(32, 32, 28, 22, 5, 32);
        FACADE_FACE_SELECTORS[66] = new FacadeFaceSelector(32, 32, 32, 5, 13, 7);
        FACADE_FACE_SELECTORS[67] = new FacadeFaceSelector(32, 32, 32, 5, 27, 20);
        FACADE_FACE_SELECTORS[68] = new FacadeFaceSelector(32, 32, 32, 5, 26, 21);
        FACADE_FACE_SELECTORS[69] = new FacadeFaceSelector(32, 32, 32, 5, 28, 22);
        FACADE_FACE_SELECTORS[70] = new FacadeFaceSelector(7, 7, 32, 10, 32, 32);
        FACADE_FACE_SELECTORS[71] = new FacadeFaceSelector(20, 21, 32, 10, 32, 32);
        FACADE_FACE_SELECTORS[72] = new FacadeFaceSelector(21, 20, 32, 10, 32, 32);
        FACADE_FACE_SELECTORS[73] = new FacadeFaceSelector(22, 22, 32, 10, 32, 32);
        FACADE_FACE_SELECTORS[74] = new FacadeFaceSelector(32, 32, 5, 32, 7, 13);
        FACADE_FACE_SELECTORS[75] = new FacadeFaceSelector(32, 32, 5, 32, 20, 27);
        FACADE_FACE_SELECTORS[76] = new FacadeFaceSelector(32, 32, 5, 32, 21, 26);
        FACADE_FACE_SELECTORS[77] = new FacadeFaceSelector(32, 32, 5, 32, 22, 28);
        FACADE_FACE_SELECTORS[78] = new FacadeFaceSelector(13, 13, 10, 32, 32, 32);
        FACADE_FACE_SELECTORS[79] = new FacadeFaceSelector(26, 27, 10, 32, 32, 32);
        FACADE_FACE_SELECTORS[80] = new FacadeFaceSelector(27, 26, 10, 32, 32, 32);
        FACADE_FACE_SELECTORS[81] = new FacadeFaceSelector(28, 28, 10, 32, 32, 32);
        FACADE_FACE_SELECTORS[82] = new FacadeFaceSelector(32, 6, 32, 9, 32, 3);
        FACADE_FACE_SELECTORS[83] = new FacadeFaceSelector(32, 6, 32, 19, 32, 3);
        FACADE_FACE_SELECTORS[84] = new FacadeFaceSelector(32, 6, 32, 9, 32, 16);
        FACADE_FACE_SELECTORS[85] = new FacadeFaceSelector(32, 6, 32, 19, 32, 16);
        FACADE_FACE_SELECTORS[86] = new FacadeFaceSelector(32, 17, 32, 9, 32, 3);
        FACADE_FACE_SELECTORS[87] = new FacadeFaceSelector(32, 17, 32, 19, 32, 3);
        FACADE_FACE_SELECTORS[88] = new FacadeFaceSelector(32, 17, 32, 9, 32, 16);
        FACADE_FACE_SELECTORS[89] = new FacadeFaceSelector(32, 17, 32, 19, 32, 16);
        FACADE_FACE_SELECTORS[90] = new FacadeFaceSelector(32, 3, 32, 3, 9, 32);
        FACADE_FACE_SELECTORS[91] = new FacadeFaceSelector(32, 3, 32, 3, 19, 32);
        FACADE_FACE_SELECTORS[92] = new FacadeFaceSelector(32, 3, 32, 16, 9, 32);
        FACADE_FACE_SELECTORS[93] = new FacadeFaceSelector(32, 3, 32, 16, 19, 32);
        FACADE_FACE_SELECTORS[94] = new FacadeFaceSelector(32, 16, 32, 3, 9, 32);
        FACADE_FACE_SELECTORS[95] = new FacadeFaceSelector(32, 16, 32, 3, 19, 32);
        FACADE_FACE_SELECTORS[96] = new FacadeFaceSelector(32, 16, 32, 16, 9, 32);
        FACADE_FACE_SELECTORS[97] = new FacadeFaceSelector(32, 16, 32, 16, 19, 32);
        FACADE_FACE_SELECTORS[98] = new FacadeFaceSelector(32, 12, 3, 32, 32, 9);
        FACADE_FACE_SELECTORS[99] = new FacadeFaceSelector(32, 12, 16, 32, 32, 9);
        FACADE_FACE_SELECTORS[100] = new FacadeFaceSelector(32, 12, 3, 32, 32, 19);
        FACADE_FACE_SELECTORS[101] = new FacadeFaceSelector(32, 12, 16, 32, 32, 19);
        FACADE_FACE_SELECTORS[102] = new FacadeFaceSelector(32, 18, 3, 32, 32, 9);
        FACADE_FACE_SELECTORS[103] = new FacadeFaceSelector(32, 18, 16, 32, 32, 9);
        FACADE_FACE_SELECTORS[104] = new FacadeFaceSelector(32, 18, 3, 32, 32, 19);
        FACADE_FACE_SELECTORS[105] = new FacadeFaceSelector(32, 18, 16, 32, 32, 19);
        FACADE_FACE_SELECTORS[106] = new FacadeFaceSelector(32, 9, 9, 32, 3, 32);
        FACADE_FACE_SELECTORS[107] = new FacadeFaceSelector(32, 9, 19, 32, 3, 32);
        FACADE_FACE_SELECTORS[108] = new FacadeFaceSelector(32, 9, 9, 32, 16, 32);
        FACADE_FACE_SELECTORS[109] = new FacadeFaceSelector(32, 9, 19, 32, 16, 32);
        FACADE_FACE_SELECTORS[110] = new FacadeFaceSelector(32, 19, 9, 32, 3, 32);
        FACADE_FACE_SELECTORS[111] = new FacadeFaceSelector(32, 19, 19, 32, 3, 32);
        FACADE_FACE_SELECTORS[112] = new FacadeFaceSelector(32, 19, 9, 32, 16, 32);
        FACADE_FACE_SELECTORS[113] = new FacadeFaceSelector(32, 19, 19, 32, 16, 32);
        FACADE_FACE_SELECTORS[114] = new FacadeFaceSelector(3, 32, 32, 12, 32, 6);
        FACADE_FACE_SELECTORS[115] = new FacadeFaceSelector(3, 32, 32, 18, 32, 6);
        FACADE_FACE_SELECTORS[116] = new FacadeFaceSelector(3, 32, 32, 12, 32, 17);
        FACADE_FACE_SELECTORS[117] = new FacadeFaceSelector(3, 32, 32, 18, 32, 17);
        FACADE_FACE_SELECTORS[118] = new FacadeFaceSelector(16, 32, 32, 12, 32, 6);
        FACADE_FACE_SELECTORS[119] = new FacadeFaceSelector(16, 32, 32, 18, 32, 6);
        FACADE_FACE_SELECTORS[120] = new FacadeFaceSelector(16, 32, 32, 12, 32, 17);
        FACADE_FACE_SELECTORS[121] = new FacadeFaceSelector(16, 32, 32, 18, 32, 17);
        FACADE_FACE_SELECTORS[122] = new FacadeFaceSelector(6, 32, 32, 6, 12, 32);
        FACADE_FACE_SELECTORS[123] = new FacadeFaceSelector(6, 32, 32, 6, 18, 32);
        FACADE_FACE_SELECTORS[124] = new FacadeFaceSelector(6, 32, 32, 17, 12, 32);
        FACADE_FACE_SELECTORS[125] = new FacadeFaceSelector(6, 32, 32, 17, 18, 32);
        FACADE_FACE_SELECTORS[126] = new FacadeFaceSelector(17, 32, 32, 6, 12, 32);
        FACADE_FACE_SELECTORS[127] = new FacadeFaceSelector(17, 32, 32, 6, 18, 32);
        FACADE_FACE_SELECTORS[128] = new FacadeFaceSelector(17, 32, 32, 17, 12, 32);
        FACADE_FACE_SELECTORS[129] = new FacadeFaceSelector(17, 32, 32, 17, 18, 32);
        FACADE_FACE_SELECTORS[130] = new FacadeFaceSelector(9, 32, 6, 32, 32, 12);
        FACADE_FACE_SELECTORS[131] = new FacadeFaceSelector(9, 32, 17, 32, 32, 12);
        FACADE_FACE_SELECTORS[132] = new FacadeFaceSelector(9, 32, 6, 32, 32, 18);
        FACADE_FACE_SELECTORS[133] = new FacadeFaceSelector(9, 32, 17, 32, 32, 18);
        FACADE_FACE_SELECTORS[134] = new FacadeFaceSelector(19, 32, 6, 32, 32, 12);
        FACADE_FACE_SELECTORS[135] = new FacadeFaceSelector(19, 32, 17, 32, 32, 12);
        FACADE_FACE_SELECTORS[136] = new FacadeFaceSelector(19, 32, 6, 32, 32, 18);
        FACADE_FACE_SELECTORS[137] = new FacadeFaceSelector(19, 32, 17, 32, 32, 18);
        FACADE_FACE_SELECTORS[138] = new FacadeFaceSelector(12, 32, 12, 32, 6, 32);
        FACADE_FACE_SELECTORS[139] = new FacadeFaceSelector(12, 32, 18, 32, 6, 32);
        FACADE_FACE_SELECTORS[140] = new FacadeFaceSelector(12, 32, 12, 32, 17, 32);
        FACADE_FACE_SELECTORS[141] = new FacadeFaceSelector(12, 32, 18, 32, 17, 32);
        FACADE_FACE_SELECTORS[142] = new FacadeFaceSelector(18, 32, 12, 32, 6, 32);
        FACADE_FACE_SELECTORS[143] = new FacadeFaceSelector(18, 32, 18, 32, 6, 32);
        FACADE_FACE_SELECTORS[144] = new FacadeFaceSelector(18, 32, 12, 32, 17, 32);
        FACADE_FACE_SELECTORS[145] = new FacadeFaceSelector(18, 32, 18, 32, 17, 32);
        FACADE_FACE_SELECTORS[146] = new FacadeFaceSelector(15, 15, 32, 32, 32, 32);
        FACADE_FACE_SELECTORS[147] = new FacadeFaceSelector(34, 36, 32, 32, 32, 32);
        FACADE_FACE_SELECTORS[148] = new FacadeFaceSelector(36, 34, 32, 32, 32, 32);
        FACADE_FACE_SELECTORS[149] = new FacadeFaceSelector(38, 38, 32, 32, 32, 32);
        FACADE_FACE_SELECTORS[150] = new FacadeFaceSelector(40, 33, 32, 32, 32, 32);
        FACADE_FACE_SELECTORS[151] = new FacadeFaceSelector(42, 37, 32, 32, 32, 32);
        FACADE_FACE_SELECTORS[152] = new FacadeFaceSelector(44, 35, 32, 32, 32, 32);
        FACADE_FACE_SELECTORS[153] = new FacadeFaceSelector(46, 39, 32, 32, 32, 32);
        FACADE_FACE_SELECTORS[154] = new FacadeFaceSelector(33, 40, 32, 32, 32, 32);
        FACADE_FACE_SELECTORS[155] = new FacadeFaceSelector(35, 44, 32, 32, 32, 32);
        FACADE_FACE_SELECTORS[156] = new FacadeFaceSelector(37, 42, 32, 32, 32, 32);
        FACADE_FACE_SELECTORS[157] = new FacadeFaceSelector(39, 46, 32, 32, 32, 32);
        FACADE_FACE_SELECTORS[158] = new FacadeFaceSelector(41, 41, 32, 32, 32, 32);
        FACADE_FACE_SELECTORS[159] = new FacadeFaceSelector(43, 45, 32, 32, 32, 32);
        FACADE_FACE_SELECTORS[160] = new FacadeFaceSelector(45, 43, 32, 32, 32, 32);
        FACADE_FACE_SELECTORS[161] = new FacadeFaceSelector(47, 47, 32, 32, 32, 32);
        FACADE_FACE_SELECTORS[162] = new FacadeFaceSelector(32, 32, 32, 32, 15, 15);
        FACADE_FACE_SELECTORS[163] = new FacadeFaceSelector(32, 32, 32, 32, 33, 34);
        FACADE_FACE_SELECTORS[164] = new FacadeFaceSelector(32, 32, 32, 32, 34, 33);
        FACADE_FACE_SELECTORS[165] = new FacadeFaceSelector(32, 32, 32, 32, 35, 35);
        FACADE_FACE_SELECTORS[166] = new FacadeFaceSelector(32, 32, 32, 32, 40, 36);
        FACADE_FACE_SELECTORS[167] = new FacadeFaceSelector(32, 32, 32, 32, 41, 38);
        FACADE_FACE_SELECTORS[168] = new FacadeFaceSelector(32, 32, 32, 32, 42, 37);
        FACADE_FACE_SELECTORS[169] = new FacadeFaceSelector(32, 32, 32, 32, 43, 39);
        FACADE_FACE_SELECTORS[170] = new FacadeFaceSelector(32, 32, 32, 32, 36, 40);
        FACADE_FACE_SELECTORS[171] = new FacadeFaceSelector(32, 32, 32, 32, 37, 42);
        FACADE_FACE_SELECTORS[172] = new FacadeFaceSelector(32, 32, 32, 32, 38, 41);
        FACADE_FACE_SELECTORS[173] = new FacadeFaceSelector(32, 32, 32, 32, 39, 43);
        FACADE_FACE_SELECTORS[174] = new FacadeFaceSelector(32, 32, 32, 32, 44, 44);
        FACADE_FACE_SELECTORS[175] = new FacadeFaceSelector(32, 32, 32, 32, 45, 46);
        FACADE_FACE_SELECTORS[176] = new FacadeFaceSelector(32, 32, 32, 32, 46, 45);
        FACADE_FACE_SELECTORS[177] = new FacadeFaceSelector(32, 32, 32, 32, 47, 47);
        FACADE_FACE_SELECTORS[178] = new FacadeFaceSelector(32, 32, 15, 15, 32, 32);
        FACADE_FACE_SELECTORS[179] = new FacadeFaceSelector(32, 32, 34, 33, 32, 32);
        FACADE_FACE_SELECTORS[180] = new FacadeFaceSelector(32, 32, 33, 34, 32, 32);
        FACADE_FACE_SELECTORS[181] = new FacadeFaceSelector(32, 32, 35, 35, 32, 32);
        FACADE_FACE_SELECTORS[182] = new FacadeFaceSelector(32, 32, 36, 40, 32, 32);
        FACADE_FACE_SELECTORS[183] = new FacadeFaceSelector(32, 32, 38, 41, 32, 32);
        FACADE_FACE_SELECTORS[184] = new FacadeFaceSelector(32, 32, 37, 42, 32, 32);
        FACADE_FACE_SELECTORS[185] = new FacadeFaceSelector(32, 32, 39, 43, 32, 32);
        FACADE_FACE_SELECTORS[186] = new FacadeFaceSelector(32, 32, 40, 36, 32, 32);
        FACADE_FACE_SELECTORS[187] = new FacadeFaceSelector(32, 32, 42, 37, 32, 32);
        FACADE_FACE_SELECTORS[188] = new FacadeFaceSelector(32, 32, 41, 38, 32, 32);
        FACADE_FACE_SELECTORS[189] = new FacadeFaceSelector(32, 32, 43, 39, 32, 32);
        FACADE_FACE_SELECTORS[190] = new FacadeFaceSelector(32, 32, 44, 44, 32, 32);
        FACADE_FACE_SELECTORS[191] = new FacadeFaceSelector(32, 32, 46, 45, 32, 32);
        FACADE_FACE_SELECTORS[192] = new FacadeFaceSelector(32, 32, 45, 46, 32, 32);
        FACADE_FACE_SELECTORS[193] = new FacadeFaceSelector(32, 32, 47, 47, 32, 32);
        FACADE_FACE_SELECTORS[194] = new FacadeFaceSelector(32, 13, 11, 32, 32, 32);
        FACADE_FACE_SELECTORS[195] = new FacadeFaceSelector(32, 13, 24, 32, 32, 32);
        FACADE_FACE_SELECTORS[196] = new FacadeFaceSelector(32, 13, 23, 32, 32, 32);
        FACADE_FACE_SELECTORS[197] = new FacadeFaceSelector(32, 13, 25, 32, 32, 32);
        FACADE_FACE_SELECTORS[198] = new FacadeFaceSelector(32, 27, 11, 32, 32, 32);
        FACADE_FACE_SELECTORS[199] = new FacadeFaceSelector(32, 27, 24, 32, 32, 32);
        FACADE_FACE_SELECTORS[200] = new FacadeFaceSelector(32, 27, 23, 32, 32, 32);
        FACADE_FACE_SELECTORS[201] = new FacadeFaceSelector(32, 27, 25, 32, 32, 32);
        FACADE_FACE_SELECTORS[202] = new FacadeFaceSelector(32, 26, 11, 32, 32, 32);
        FACADE_FACE_SELECTORS[203] = new FacadeFaceSelector(32, 26, 24, 32, 32, 32);
        FACADE_FACE_SELECTORS[204] = new FacadeFaceSelector(32, 26, 23, 32, 32, 32);
        FACADE_FACE_SELECTORS[205] = new FacadeFaceSelector(32, 26, 25, 32, 32, 32);
        FACADE_FACE_SELECTORS[206] = new FacadeFaceSelector(32, 28, 11, 32, 32, 32);
        FACADE_FACE_SELECTORS[207] = new FacadeFaceSelector(32, 28, 24, 32, 32, 32);
        FACADE_FACE_SELECTORS[208] = new FacadeFaceSelector(32, 28, 23, 32, 32, 32);
        FACADE_FACE_SELECTORS[209] = new FacadeFaceSelector(32, 28, 25, 32, 32, 32);
        FACADE_FACE_SELECTORS[210] = new FacadeFaceSelector(32, 7, 32, 11, 32, 32);
        FACADE_FACE_SELECTORS[211] = new FacadeFaceSelector(32, 7, 32, 23, 32, 32);
        FACADE_FACE_SELECTORS[212] = new FacadeFaceSelector(32, 7, 32, 24, 32, 32);
        FACADE_FACE_SELECTORS[213] = new FacadeFaceSelector(32, 7, 32, 25, 32, 32);
        FACADE_FACE_SELECTORS[214] = new FacadeFaceSelector(32, 21, 32, 11, 32, 32);
        FACADE_FACE_SELECTORS[215] = new FacadeFaceSelector(32, 21, 32, 23, 32, 32);
        FACADE_FACE_SELECTORS[216] = new FacadeFaceSelector(32, 21, 32, 24, 32, 32);
        FACADE_FACE_SELECTORS[217] = new FacadeFaceSelector(32, 21, 32, 25, 32, 32);
        FACADE_FACE_SELECTORS[218] = new FacadeFaceSelector(32, 20, 32, 11, 32, 32);
        FACADE_FACE_SELECTORS[219] = new FacadeFaceSelector(32, 20, 32, 23, 32, 32);
        FACADE_FACE_SELECTORS[220] = new FacadeFaceSelector(32, 20, 32, 24, 32, 32);
        FACADE_FACE_SELECTORS[221] = new FacadeFaceSelector(32, 20, 32, 25, 32, 32);
        FACADE_FACE_SELECTORS[222] = new FacadeFaceSelector(32, 22, 32, 11, 32, 32);
        FACADE_FACE_SELECTORS[223] = new FacadeFaceSelector(32, 22, 32, 23, 32, 32);
        FACADE_FACE_SELECTORS[224] = new FacadeFaceSelector(32, 22, 32, 24, 32, 32);
        FACADE_FACE_SELECTORS[225] = new FacadeFaceSelector(32, 22, 32, 25, 32, 32);
        FACADE_FACE_SELECTORS[226] = new FacadeFaceSelector(32, 11, 32, 32, 11, 32);
        FACADE_FACE_SELECTORS[227] = new FacadeFaceSelector(32, 11, 32, 32, 23, 32);
        FACADE_FACE_SELECTORS[228] = new FacadeFaceSelector(32, 11, 32, 32, 24, 32);
        FACADE_FACE_SELECTORS[229] = new FacadeFaceSelector(32, 11, 32, 32, 25, 32);
        FACADE_FACE_SELECTORS[230] = new FacadeFaceSelector(32, 24, 32, 32, 11, 32);
        FACADE_FACE_SELECTORS[231] = new FacadeFaceSelector(32, 24, 32, 32, 23, 32);
        FACADE_FACE_SELECTORS[232] = new FacadeFaceSelector(32, 24, 32, 32, 24, 32);
        FACADE_FACE_SELECTORS[233] = new FacadeFaceSelector(32, 24, 32, 32, 25, 32);
        FACADE_FACE_SELECTORS[234] = new FacadeFaceSelector(32, 23, 32, 32, 11, 32);
        FACADE_FACE_SELECTORS[235] = new FacadeFaceSelector(32, 23, 32, 32, 23, 32);
        FACADE_FACE_SELECTORS[236] = new FacadeFaceSelector(32, 23, 32, 32, 24, 32);
        FACADE_FACE_SELECTORS[237] = new FacadeFaceSelector(32, 23, 32, 32, 25, 32);
        FACADE_FACE_SELECTORS[238] = new FacadeFaceSelector(32, 25, 32, 32, 11, 32);
        FACADE_FACE_SELECTORS[239] = new FacadeFaceSelector(32, 25, 32, 32, 23, 32);
        FACADE_FACE_SELECTORS[240] = new FacadeFaceSelector(32, 25, 32, 32, 24, 32);
        FACADE_FACE_SELECTORS[241] = new FacadeFaceSelector(32, 25, 32, 32, 25, 32);
        FACADE_FACE_SELECTORS[242] = new FacadeFaceSelector(32, 14, 32, 32, 32, 11);
        FACADE_FACE_SELECTORS[243] = new FacadeFaceSelector(32, 14, 32, 32, 32, 24);
        FACADE_FACE_SELECTORS[244] = new FacadeFaceSelector(32, 14, 32, 32, 32, 23);
        FACADE_FACE_SELECTORS[245] = new FacadeFaceSelector(32, 14, 32, 32, 32, 25);
        FACADE_FACE_SELECTORS[246] = new FacadeFaceSelector(32, 29, 32, 32, 32, 11);
        FACADE_FACE_SELECTORS[247] = new FacadeFaceSelector(32, 29, 32, 32, 32, 24);
        FACADE_FACE_SELECTORS[248] = new FacadeFaceSelector(32, 29, 32, 32, 32, 23);
        FACADE_FACE_SELECTORS[249] = new FacadeFaceSelector(32, 29, 32, 32, 32, 25);
        FACADE_FACE_SELECTORS[250] = new FacadeFaceSelector(32, 30, 32, 32, 32, 11);
        FACADE_FACE_SELECTORS[251] = new FacadeFaceSelector(32, 30, 32, 32, 32, 24);
        FACADE_FACE_SELECTORS[252] = new FacadeFaceSelector(32, 30, 32, 32, 32, 23);
        FACADE_FACE_SELECTORS[253] = new FacadeFaceSelector(32, 30, 32, 32, 32, 25);
        FACADE_FACE_SELECTORS[254] = new FacadeFaceSelector(32, 31, 32, 32, 32, 11);
        FACADE_FACE_SELECTORS[255] = new FacadeFaceSelector(32, 31, 32, 32, 32, 24);
        FACADE_FACE_SELECTORS[256] = new FacadeFaceSelector(32, 31, 32, 32, 32, 23);
        FACADE_FACE_SELECTORS[257] = new FacadeFaceSelector(32, 31, 32, 32, 32, 25);
        FACADE_FACE_SELECTORS[258] = new FacadeFaceSelector(13, 32, 14, 32, 32, 32);
        FACADE_FACE_SELECTORS[259] = new FacadeFaceSelector(13, 32, 29, 32, 32, 32);
        FACADE_FACE_SELECTORS[260] = new FacadeFaceSelector(13, 32, 30, 32, 32, 32);
        FACADE_FACE_SELECTORS[261] = new FacadeFaceSelector(13, 32, 31, 32, 32, 32);
        FACADE_FACE_SELECTORS[262] = new FacadeFaceSelector(26, 32, 14, 32, 32, 32);
        FACADE_FACE_SELECTORS[263] = new FacadeFaceSelector(26, 32, 29, 32, 32, 32);
        FACADE_FACE_SELECTORS[264] = new FacadeFaceSelector(26, 32, 30, 32, 32, 32);
        FACADE_FACE_SELECTORS[265] = new FacadeFaceSelector(26, 32, 31, 32, 32, 32);
        FACADE_FACE_SELECTORS[266] = new FacadeFaceSelector(27, 32, 14, 32, 32, 32);
        FACADE_FACE_SELECTORS[267] = new FacadeFaceSelector(27, 32, 29, 32, 32, 32);
        FACADE_FACE_SELECTORS[268] = new FacadeFaceSelector(27, 32, 30, 32, 32, 32);
        FACADE_FACE_SELECTORS[269] = new FacadeFaceSelector(27, 32, 31, 32, 32, 32);
        FACADE_FACE_SELECTORS[270] = new FacadeFaceSelector(28, 32, 14, 32, 32, 32);
        FACADE_FACE_SELECTORS[271] = new FacadeFaceSelector(28, 32, 29, 32, 32, 32);
        FACADE_FACE_SELECTORS[272] = new FacadeFaceSelector(28, 32, 30, 32, 32, 32);
        FACADE_FACE_SELECTORS[273] = new FacadeFaceSelector(28, 32, 31, 32, 32, 32);
        FACADE_FACE_SELECTORS[274] = new FacadeFaceSelector(7, 32, 32, 14, 32, 32);
        FACADE_FACE_SELECTORS[275] = new FacadeFaceSelector(7, 32, 32, 30, 32, 32);
        FACADE_FACE_SELECTORS[276] = new FacadeFaceSelector(7, 32, 32, 29, 32, 32);
        FACADE_FACE_SELECTORS[277] = new FacadeFaceSelector(7, 32, 32, 31, 32, 32);
        FACADE_FACE_SELECTORS[278] = new FacadeFaceSelector(20, 32, 32, 14, 32, 32);
        FACADE_FACE_SELECTORS[279] = new FacadeFaceSelector(20, 32, 32, 30, 32, 32);
        FACADE_FACE_SELECTORS[280] = new FacadeFaceSelector(20, 32, 32, 29, 32, 32);
        FACADE_FACE_SELECTORS[281] = new FacadeFaceSelector(20, 32, 32, 31, 32, 32);
        FACADE_FACE_SELECTORS[282] = new FacadeFaceSelector(21, 32, 32, 14, 32, 32);
        FACADE_FACE_SELECTORS[283] = new FacadeFaceSelector(21, 32, 32, 30, 32, 32);
        FACADE_FACE_SELECTORS[284] = new FacadeFaceSelector(21, 32, 32, 29, 32, 32);
        FACADE_FACE_SELECTORS[285] = new FacadeFaceSelector(21, 32, 32, 31, 32, 32);
        FACADE_FACE_SELECTORS[286] = new FacadeFaceSelector(22, 32, 32, 14, 32, 32);
        FACADE_FACE_SELECTORS[287] = new FacadeFaceSelector(22, 32, 32, 30, 32, 32);
        FACADE_FACE_SELECTORS[288] = new FacadeFaceSelector(22, 32, 32, 29, 32, 32);
        FACADE_FACE_SELECTORS[289] = new FacadeFaceSelector(22, 32, 32, 31, 32, 32);
        FACADE_FACE_SELECTORS[290] = new FacadeFaceSelector(14, 32, 32, 32, 14, 32);
        FACADE_FACE_SELECTORS[291] = new FacadeFaceSelector(14, 32, 32, 32, 30, 32);
        FACADE_FACE_SELECTORS[292] = new FacadeFaceSelector(14, 32, 32, 32, 29, 32);
        FACADE_FACE_SELECTORS[293] = new FacadeFaceSelector(14, 32, 32, 32, 31, 32);
        FACADE_FACE_SELECTORS[294] = new FacadeFaceSelector(29, 32, 32, 32, 14, 32);
        FACADE_FACE_SELECTORS[295] = new FacadeFaceSelector(29, 32, 32, 32, 30, 32);
        FACADE_FACE_SELECTORS[296] = new FacadeFaceSelector(29, 32, 32, 32, 29, 32);
        FACADE_FACE_SELECTORS[297] = new FacadeFaceSelector(29, 32, 32, 32, 31, 32);
        FACADE_FACE_SELECTORS[298] = new FacadeFaceSelector(30, 32, 32, 32, 14, 32);
        FACADE_FACE_SELECTORS[299] = new FacadeFaceSelector(30, 32, 32, 32, 30, 32);
        FACADE_FACE_SELECTORS[300] = new FacadeFaceSelector(30, 32, 32, 32, 29, 32);
        FACADE_FACE_SELECTORS[301] = new FacadeFaceSelector(30, 32, 32, 32, 31, 32);
        FACADE_FACE_SELECTORS[302] = new FacadeFaceSelector(31, 32, 32, 32, 14, 32);
        FACADE_FACE_SELECTORS[303] = new FacadeFaceSelector(31, 32, 32, 32, 30, 32);
        FACADE_FACE_SELECTORS[304] = new FacadeFaceSelector(31, 32, 32, 32, 29, 32);
        FACADE_FACE_SELECTORS[305] = new FacadeFaceSelector(31, 32, 32, 32, 31, 32);
        FACADE_FACE_SELECTORS[306] = new FacadeFaceSelector(11, 32, 32, 32, 32, 14);
        FACADE_FACE_SELECTORS[307] = new FacadeFaceSelector(11, 32, 32, 32, 32, 29);
        FACADE_FACE_SELECTORS[308] = new FacadeFaceSelector(11, 32, 32, 32, 32, 30);
        FACADE_FACE_SELECTORS[309] = new FacadeFaceSelector(11, 32, 32, 32, 32, 31);
        FACADE_FACE_SELECTORS[310] = new FacadeFaceSelector(24, 32, 32, 32, 32, 14);
        FACADE_FACE_SELECTORS[311] = new FacadeFaceSelector(24, 32, 32, 32, 32, 29);
        FACADE_FACE_SELECTORS[312] = new FacadeFaceSelector(24, 32, 32, 32, 32, 30);
        FACADE_FACE_SELECTORS[313] = new FacadeFaceSelector(24, 32, 32, 32, 32, 31);
        FACADE_FACE_SELECTORS[314] = new FacadeFaceSelector(23, 32, 32, 32, 32, 14);
        FACADE_FACE_SELECTORS[315] = new FacadeFaceSelector(23, 32, 32, 32, 32, 29);
        FACADE_FACE_SELECTORS[316] = new FacadeFaceSelector(23, 32, 32, 32, 32, 30);
        FACADE_FACE_SELECTORS[317] = new FacadeFaceSelector(23, 32, 32, 32, 32, 31);
        FACADE_FACE_SELECTORS[318] = new FacadeFaceSelector(25, 32, 32, 32, 32, 14);
        FACADE_FACE_SELECTORS[319] = new FacadeFaceSelector(25, 32, 32, 32, 32, 29);
        FACADE_FACE_SELECTORS[320] = new FacadeFaceSelector(25, 32, 32, 32, 32, 30);
        FACADE_FACE_SELECTORS[321] = new FacadeFaceSelector(25, 32, 32, 32, 32, 31);
        FACADE_FACE_SELECTORS[322] = new FacadeFaceSelector(32, 32, 32, 13, 32, 7);
        FACADE_FACE_SELECTORS[323] = new FacadeFaceSelector(32, 32, 32, 27, 32, 7);
        FACADE_FACE_SELECTORS[324] = new FacadeFaceSelector(32, 32, 32, 13, 32, 20);
        FACADE_FACE_SELECTORS[325] = new FacadeFaceSelector(32, 32, 32, 27, 32, 20);
        FACADE_FACE_SELECTORS[326] = new FacadeFaceSelector(32, 32, 32, 26, 32, 7);
        FACADE_FACE_SELECTORS[327] = new FacadeFaceSelector(32, 32, 32, 28, 32, 7);
        FACADE_FACE_SELECTORS[328] = new FacadeFaceSelector(32, 32, 32, 26, 32, 20);
        FACADE_FACE_SELECTORS[329] = new FacadeFaceSelector(32, 32, 32, 28, 32, 20);
        FACADE_FACE_SELECTORS[330] = new FacadeFaceSelector(32, 32, 32, 13, 32, 21);
        FACADE_FACE_SELECTORS[331] = new FacadeFaceSelector(32, 32, 32, 27, 32, 21);
        FACADE_FACE_SELECTORS[332] = new FacadeFaceSelector(32, 32, 32, 13, 32, 22);
        FACADE_FACE_SELECTORS[333] = new FacadeFaceSelector(32, 32, 32, 27, 32, 22);
        FACADE_FACE_SELECTORS[334] = new FacadeFaceSelector(32, 32, 32, 26, 32, 21);
        FACADE_FACE_SELECTORS[335] = new FacadeFaceSelector(32, 32, 32, 28, 32, 21);
        FACADE_FACE_SELECTORS[336] = new FacadeFaceSelector(32, 32, 32, 26, 32, 22);
        FACADE_FACE_SELECTORS[337] = new FacadeFaceSelector(32, 32, 32, 28, 32, 22);
        FACADE_FACE_SELECTORS[338] = new FacadeFaceSelector(32, 32, 32, 7, 13, 32);
        FACADE_FACE_SELECTORS[339] = new FacadeFaceSelector(32, 32, 32, 7, 27, 32);
        FACADE_FACE_SELECTORS[340] = new FacadeFaceSelector(32, 32, 32, 20, 13, 32);
        FACADE_FACE_SELECTORS[341] = new FacadeFaceSelector(32, 32, 32, 20, 27, 32);
        FACADE_FACE_SELECTORS[342] = new FacadeFaceSelector(32, 32, 32, 7, 26, 32);
        FACADE_FACE_SELECTORS[343] = new FacadeFaceSelector(32, 32, 32, 7, 28, 32);
        FACADE_FACE_SELECTORS[344] = new FacadeFaceSelector(32, 32, 32, 20, 26, 32);
        FACADE_FACE_SELECTORS[345] = new FacadeFaceSelector(32, 32, 32, 20, 28, 32);
        FACADE_FACE_SELECTORS[346] = new FacadeFaceSelector(32, 32, 32, 21, 13, 32);
        FACADE_FACE_SELECTORS[347] = new FacadeFaceSelector(32, 32, 32, 21, 27, 32);
        FACADE_FACE_SELECTORS[348] = new FacadeFaceSelector(32, 32, 32, 22, 13, 32);
        FACADE_FACE_SELECTORS[349] = new FacadeFaceSelector(32, 32, 32, 22, 27, 32);
        FACADE_FACE_SELECTORS[350] = new FacadeFaceSelector(32, 32, 32, 21, 26, 32);
        FACADE_FACE_SELECTORS[351] = new FacadeFaceSelector(32, 32, 32, 21, 28, 32);
        FACADE_FACE_SELECTORS[352] = new FacadeFaceSelector(32, 32, 32, 22, 26, 32);
        FACADE_FACE_SELECTORS[353] = new FacadeFaceSelector(32, 32, 32, 22, 28, 32);
        FACADE_FACE_SELECTORS[354] = new FacadeFaceSelector(32, 32, 7, 32, 32, 13);
        FACADE_FACE_SELECTORS[355] = new FacadeFaceSelector(32, 32, 20, 32, 32, 13);
        FACADE_FACE_SELECTORS[356] = new FacadeFaceSelector(32, 32, 7, 32, 32, 27);
        FACADE_FACE_SELECTORS[357] = new FacadeFaceSelector(32, 32, 20, 32, 32, 27);
        FACADE_FACE_SELECTORS[358] = new FacadeFaceSelector(32, 32, 21, 32, 32, 13);
        FACADE_FACE_SELECTORS[359] = new FacadeFaceSelector(32, 32, 22, 32, 32, 13);
        FACADE_FACE_SELECTORS[360] = new FacadeFaceSelector(32, 32, 21, 32, 32, 27);
        FACADE_FACE_SELECTORS[361] = new FacadeFaceSelector(32, 32, 22, 32, 32, 27);
        FACADE_FACE_SELECTORS[362] = new FacadeFaceSelector(32, 32, 7, 32, 32, 26);
        FACADE_FACE_SELECTORS[363] = new FacadeFaceSelector(32, 32, 20, 32, 32, 26);
        FACADE_FACE_SELECTORS[364] = new FacadeFaceSelector(32, 32, 7, 32, 32, 28);
        FACADE_FACE_SELECTORS[365] = new FacadeFaceSelector(32, 32, 20, 32, 32, 28);
        FACADE_FACE_SELECTORS[366] = new FacadeFaceSelector(32, 32, 21, 32, 32, 26);
        FACADE_FACE_SELECTORS[367] = new FacadeFaceSelector(32, 32, 22, 32, 32, 26);
        FACADE_FACE_SELECTORS[368] = new FacadeFaceSelector(32, 32, 21, 32, 32, 28);
        FACADE_FACE_SELECTORS[369] = new FacadeFaceSelector(32, 32, 22, 32, 32, 28);
        FACADE_FACE_SELECTORS[370] = new FacadeFaceSelector(32, 32, 13, 32, 7, 32);
        FACADE_FACE_SELECTORS[371] = new FacadeFaceSelector(32, 32, 27, 32, 7, 32);
        FACADE_FACE_SELECTORS[372] = new FacadeFaceSelector(32, 32, 13, 32, 20, 32);
        FACADE_FACE_SELECTORS[373] = new FacadeFaceSelector(32, 32, 27, 32, 20, 32);
        FACADE_FACE_SELECTORS[374] = new FacadeFaceSelector(32, 32, 26, 32, 7, 32);
        FACADE_FACE_SELECTORS[375] = new FacadeFaceSelector(32, 32, 28, 32, 7, 32);
        FACADE_FACE_SELECTORS[376] = new FacadeFaceSelector(32, 32, 26, 32, 20, 32);
        FACADE_FACE_SELECTORS[377] = new FacadeFaceSelector(32, 32, 28, 32, 20, 32);
        FACADE_FACE_SELECTORS[378] = new FacadeFaceSelector(32, 32, 13, 32, 21, 32);
        FACADE_FACE_SELECTORS[379] = new FacadeFaceSelector(32, 32, 27, 32, 21, 32);
        FACADE_FACE_SELECTORS[380] = new FacadeFaceSelector(32, 32, 13, 32, 22, 32);
        FACADE_FACE_SELECTORS[381] = new FacadeFaceSelector(32, 32, 27, 32, 22, 32);
        FACADE_FACE_SELECTORS[382] = new FacadeFaceSelector(32, 32, 26, 32, 21, 32);
        FACADE_FACE_SELECTORS[383] = new FacadeFaceSelector(32, 32, 28, 32, 21, 32);
        FACADE_FACE_SELECTORS[384] = new FacadeFaceSelector(32, 32, 26, 32, 22, 32);
        FACADE_FACE_SELECTORS[385] = new FacadeFaceSelector(32, 32, 28, 32, 22, 32);
    }
}
