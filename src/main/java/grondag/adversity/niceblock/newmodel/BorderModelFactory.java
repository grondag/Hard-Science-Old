package grondag.adversity.niceblock.newmodel;

import grondag.adversity.Adversity;
import grondag.adversity.niceblock.newmodel.QuadFactory.CubeInputs;
import grondag.adversity.niceblock.newmodel.color.ColorMap;
import grondag.adversity.niceblock.newmodel.color.ColorMap.EnumColorMap;
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
        
        ColorMap colorMap = colorProvider.getColor(colorIndex);
        BorderController controller = (BorderController)this.controller;
        int facadeIndex = modelState.getClientShapeIndex(controller.renderLayer.ordinal()) / controller.alternateTextureCount;
        int alternateTextureIndex = controller.getAlternateTextureIndexFromModelState(modelState);

        // ensure all needed faces are baked
        for(EnumFacing face : EnumFacing.values())
        {
            int faceIndex = ModelReference.BORDER_FACADE_FACE_SELECTORS[facadeIndex].selectors[face.ordinal()];
            if(faceQuads[colorIndex][alternateTextureIndex][face.ordinal()][faceIndex] == null)
            {
                List<BakedQuad> newQuads = makeBorderFace(colorMap.getColorMap(EnumColorMap.BORDER), alternateTextureIndex, faceIndex, face);
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
        cubeInputs.color = colorProvider.getColor(modelState.getColorIndex()).getColorMap(EnumColorMap.BORDER);
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
            return faceQuads[colorIndex][alternateTextureIndex][face.ordinal()][ModelReference.BORDER_FACADE_FACE_SELECTORS[facadeIndex].selectors[face.ordinal()]];
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
    }
}
