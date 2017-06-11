package grondag.adversity.superblock.model.painter;

import grondag.adversity.library.render.FaceQuadInputs;
import grondag.adversity.library.render.RawQuad;
import grondag.adversity.library.world.CornerJoinBlockState;
import grondag.adversity.library.world.CornerJoinFaceState;
import grondag.adversity.library.world.Rotation;
import grondag.adversity.superblock.model.state.PaintLayer;
import grondag.adversity.superblock.model.state.Surface;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.util.EnumFacing;

public class CubicQuadPainterBorders extends CubicQuadPainter
{
    protected final static FaceQuadInputs[][] FACE_INPUTS = new FaceQuadInputs[EnumFacing.values().length][CornerJoinFaceState.values().length];

    /** Texture offsets */
    private final static int TEXTURE_BOTTOM_LEFT_RIGHT = 0;
    private final static int TEXTURE_BOTTOM_LEFT = 1;
    private final static int TEXTURE_LEFT_RIGHT = 2;
    private final static int TEXTURE_BOTTOM = 3;
    private final static int TEXTURE_JOIN_NONE = 4;
    private final static int TEXTURE_BOTTOM_LEFT_RIGHT_BR = 5;
    private final static int TEXTURE_BOTTOM_LEFT_RIGHT_BL_BR = 6;    
    private final static int TEXTURE_BOTTOM_LEFT_BL = 7;
    private final static int TEXTURE_JOIN_ALL_TR = 8;
    private final static int TEXTURE_JOIN_ALL_TL_TR = 9;
    private final static int TEXTURE_JOIN_ALL_TR_BL = 10;
    private final static int TEXTURE_JOIN_ALL_TR_BL_BR = 11;
    private final static int TEXTURE_JOIN_ALL_ALL_CORNERS = 12;
    
    public final static int TEXTURE_COUNT = 13;
    public final static int TEXTURE_BLOCK_SIZE = 16;
    
    protected final CornerJoinBlockState bjs;
    
    public CubicQuadPainterBorders(ModelState modelState, Surface surface, PaintLayer paintLayer)
    {
        super(modelState, surface, paintLayer);
        this.bjs = modelState.getCornerJoin();
    }

    @Override
    public RawQuad paintQuad(RawQuad quad)
    {
        EnumFacing face = quad.getNominalFace();
        if(face == null) return null;

        FaceQuadInputs inputs = FACE_INPUTS[face.ordinal()][ bjs.getFaceJoinState(face).ordinal()];

        if(inputs == null)
            return null;
        
        quad.rotation = inputs.rotation;
//        cubeInputs.rotateBottom = false;
        quad.minU = inputs.flipU ? 16 : 0;
        quad.minV = inputs.flipV ? 16 : 0;
        quad.maxU = inputs.flipU ? 0 : 16;
        quad.maxV = inputs.flipV ? 0 : 16;
        quad.textureSprite = this.texture.getTextureSprite(this.blockVersion, inputs.textureOffset);
        
        return quad;
    }
    
    static
    {
        for(EnumFacing face: EnumFacing.values()){
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_NO_CORNERS.ordinal()] = null; // NO BORDER
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.NO_FACE.ordinal()] = null; // NULL FACE
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.NONE.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_NONE, Rotation.ROTATE_NONE, false, false);
            
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.BOTTOM.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.LEFT.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.RIGHT.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM, Rotation.ROTATE_270, false, false);
            
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.BOTTOM_LEFT_NO_CORNER.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_LEFT_NO_CORNER.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_RIGHT_NO_CORNER.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.BOTTOM_RIGHT_NO_CORNER.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT, Rotation.ROTATE_270, false, false);
            
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.BOTTOM_LEFT_RIGHT_NO_CORNERS.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_BOTTOM_LEFT_NO_CORNERS.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_LEFT_RIGHT_NO_CORNERS.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_BOTTOM_RIGHT_NO_CORNERS.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT, Rotation.ROTATE_270, false, false);

            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.LEFT_RIGHT.ordinal()] = new FaceQuadInputs( TEXTURE_LEFT_RIGHT, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_BOTTOM.ordinal()] = new FaceQuadInputs( TEXTURE_LEFT_RIGHT, Rotation.ROTATE_90, false, false);
            
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.BOTTOM_LEFT_RIGHT_BR.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT_BR, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.BOTTOM_LEFT_RIGHT_BL.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT_BR, Rotation.ROTATE_NONE, true, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_BOTTOM_LEFT_BL.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT_BR, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_BOTTOM_LEFT_TL.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT_BR, Rotation.ROTATE_90, true, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_LEFT_RIGHT_TL.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT_BR, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_LEFT_RIGHT_TR.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT_BR, Rotation.ROTATE_180, true, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_BOTTOM_RIGHT_TR.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT_BR, Rotation.ROTATE_270, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_BOTTOM_RIGHT_BR.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT_BR, Rotation.ROTATE_270, true, false);
            
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.BOTTOM_LEFT_RIGHT_BL_BR.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT_BL_BR, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_BOTTOM_LEFT_TL_BL.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT_BL_BR, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_LEFT_RIGHT_TL_TR.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT_BL_BR, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_BOTTOM_RIGHT_TR_BR.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT_BL_BR, Rotation.ROTATE_270, false, false);
            
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.BOTTOM_LEFT_BL.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_BL, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_LEFT_TL.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_BL, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_RIGHT_TR.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_BL, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.BOTTOM_RIGHT_BR.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_BL, Rotation.ROTATE_270, false, false);

            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TR.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_ALL_TR, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_BR.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_ALL_TR, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_BL.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_ALL_TR, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TL.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_ALL_TR, Rotation.ROTATE_270, false, false);
            
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TL_TR.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_ALL_TL_TR, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TR_BR.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_ALL_TL_TR, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_BL_BR.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_ALL_TL_TR, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TL_BL.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_ALL_TL_TR, Rotation.ROTATE_270, false, false);

            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TR_BL.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_ALL_TR_BL, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TL_BR.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_ALL_TR_BL, Rotation.ROTATE_90, false, false);
            
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TR_BL_BR.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_ALL_TR_BL_BR, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TL_BL_BR.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_ALL_TR_BL_BR, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TL_TR_BL.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_ALL_TR_BL_BR, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TL_TR_BR.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_ALL_TR_BL_BR, Rotation.ROTATE_270, false, false);
            
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TL_TR_BL_BR.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_ALL_ALL_CORNERS, Rotation.ROTATE_NONE, false, false);
        }
    }
}
