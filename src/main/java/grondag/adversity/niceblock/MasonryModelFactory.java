package grondag.adversity.niceblock;

import grondag.adversity.library.Rotation;
import grondag.adversity.library.joinstate.ModelReference;
import grondag.adversity.library.model.FaceQuadInputs;
import grondag.adversity.library.model.QuadFactory;
import grondag.adversity.library.model.QuadFactory.CubeInputs;
import grondag.adversity.niceblock.base.ModelFactory;
import grondag.adversity.niceblock.base.ModelController;
import grondag.adversity.niceblock.base.ModelState;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.color.IColorProvider;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockRenderLayer;

public class MasonryModelFactory extends ModelFactory
{
    /**
     * Dimensions are color index, alternate texture index, face ID, and texture variant.
     * Everything is instantiated lazily.
     */
    protected final List<BakedQuad>[][][][] faceQuads;
    
    protected final static FaceQuadInputs[][] FACE_INPUTS = new FaceQuadInputs[6][16];
    
    /** typed convenience reference */
    private final MasonryController myController;
    
    @SuppressWarnings("unchecked")
	public MasonryModelFactory(ModelController controller)
    {
        super(controller);
        faceQuads = (List<BakedQuad>[][][][]) new List[ModelState.MAX_COLOR_INDEX][][][];
        this.myController = (MasonryController)controller;
    }

    @SuppressWarnings("unchecked")
	@Override
    public List<BakedQuad> getFaceQuads(ModelState modelState, IColorProvider colorProvider, EnumFacing face) 
    {
    	if (face == null) return QuadFactory.EMPTY_QUAD_LIST;
    	
        int colorIndex = modelState.getColorIndex();
        
        // allocate face quads for this color if not already done
        if(faceQuads[colorIndex] == null)
        {
            synchronized(faceQuads)
            {
                faceQuads[colorIndex] = (List<BakedQuad>[][][]) new List[controller.getAlternateTextureCount()][6][16];
            }
        }
        
        int facadeIndex = modelState.getClientShapeIndex(myController.getRenderLayer().ordinal()) / controller.getAlternateTextureCount();
        int alternateTextureIndex = myController.getAlternateTextureIndexFromModelState(modelState);
        int faceIndex = ModelReference.MASONRY_FACADE_FACE_SELECTORS[facadeIndex].selectors[face.ordinal()];

        // ensure all needed faces are baked
        List<BakedQuad> retVal = faceQuads[colorIndex][alternateTextureIndex][face.ordinal()][faceIndex];
        if(retVal == null)
        {
            ColorMap colorMap = colorProvider.getColor(colorIndex);
            retVal = makeMasonryFace(colorMap.getColorMap(EnumColorMap.BORDER), alternateTextureIndex, faceIndex, face);
            synchronized(faceQuads)
            {
                faceQuads[colorIndex][alternateTextureIndex][face.ordinal()][faceIndex] = retVal;
            }
        }

        return retVal;
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
        cubeInputs.isOverlay = controller.getRenderLayer() != BlockRenderLayer.SOLID;
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

    private List<BakedQuad> makeMasonryFace(int color, int alternateTextureIndex, int faceIndex, EnumFacing face){
        
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
        }
    }
}
