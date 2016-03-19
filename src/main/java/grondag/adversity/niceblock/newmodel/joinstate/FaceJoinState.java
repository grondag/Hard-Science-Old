package grondag.adversity.niceblock.newmodel.joinstate;

import java.util.ArrayList;

import grondag.adversity.Adversity;
import grondag.adversity.library.NeighborBlocks.BlockCorner;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.niceblock.newmodel.ModelReference.SimpleJoin;
import net.minecraft.util.EnumFacing;

public enum FaceJoinState
{
    NO_FACE(0, 0),
    NONE(0, 0), //must be after NO_FACE, overwrites NO_FACE in lookup table, should never be found by lookup
    TOP(FaceSide.TOP.bitFlag, 0),
    BOTTOM(FaceSide.BOTTOM.bitFlag, 0),
    LEFT(FaceSide.LEFT.bitFlag, 0),
    RIGHT(FaceSide.RIGHT.bitFlag, 0),
    TOP_BOTTOM(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag, 0),
    LEFT_RIGHT(FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, 0),
    
    TOP_BOTTOM_RIGHT_00(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.RIGHT.bitFlag, 0, FaceCorner.TOP_RIGHT, FaceCorner.BOTTOM_RIGHT),
    TOP_BOTTOM_RIGHT_01(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.TOP_RIGHT.bitFlag),
    TOP_BOTTOM_RIGHT_10(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_RIGHT.bitFlag),
    TOP_BOTTOM_RIGHT_11(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.TOP_RIGHT.bitFlag | FaceCorner.BOTTOM_RIGHT.bitFlag),
    
    TOP_BOTTOM_LEFT_00(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag, 0, FaceCorner.TOP_LEFT, FaceCorner.BOTTOM_LEFT),
    TOP_BOTTOM_LEFT_01(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag, FaceCorner.TOP_LEFT.bitFlag),
    TOP_BOTTOM_LEFT_10(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag, FaceCorner.BOTTOM_LEFT.bitFlag),
    TOP_BOTTOM_LEFT_11(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag, FaceCorner.TOP_LEFT.bitFlag | FaceCorner.BOTTOM_LEFT.bitFlag),

    TOP_LEFT_RIGHT_00(FaceSide.TOP.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, 0, FaceCorner.TOP_LEFT, FaceCorner.TOP_RIGHT),
    TOP_LEFT_RIGHT_01(FaceSide.TOP.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.TOP_LEFT.bitFlag),
    TOP_LEFT_RIGHT_10(FaceSide.TOP.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.TOP_RIGHT.bitFlag),
    TOP_LEFT_RIGHT_11(FaceSide.TOP.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.TOP_LEFT.bitFlag | FaceCorner.TOP_RIGHT.bitFlag),
    
    BOTTOM_LEFT_RIGHT_00(FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, 0, FaceCorner.BOTTOM_LEFT, FaceCorner.BOTTOM_RIGHT),
    BOTTOM_LEFT_RIGHT_01(FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_LEFT.bitFlag),
    BOTTOM_LEFT_RIGHT_10(FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_RIGHT.bitFlag),
    BOTTOM_LEFT_RIGHT_11(FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_LEFT.bitFlag | FaceCorner.BOTTOM_RIGHT.bitFlag),

    TOP_LEFT_0(FaceSide.TOP.bitFlag | FaceSide.LEFT.bitFlag, 0, FaceCorner.TOP_LEFT),
    TOP_LEFT_1(FaceSide.TOP.bitFlag | FaceSide.LEFT.bitFlag, FaceCorner.TOP_LEFT.bitFlag),
    
    TOP_RIGHT_0(FaceSide.TOP.bitFlag | FaceSide.RIGHT.bitFlag, 0, FaceCorner.TOP_RIGHT),
    TOP_RIGHT_1(FaceSide.TOP.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.TOP_RIGHT.bitFlag),
    
    BOTTOM_LEFT_0(FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag, 0, FaceCorner.BOTTOM_LEFT),
    BOTTOM_LEFT_1(FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag, FaceCorner.BOTTOM_LEFT.bitFlag),
    
    BOTTOM_RIGHT_0(FaceSide.BOTTOM.bitFlag | FaceSide.RIGHT.bitFlag, 0, FaceCorner.BOTTOM_RIGHT),
    BOTTOM_RIGHT_1(FaceSide.BOTTOM.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_RIGHT.bitFlag),
    
    ALL_0000(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, 0, FaceCorner.TOP_LEFT, FaceCorner.TOP_RIGHT, FaceCorner.BOTTOM_LEFT, FaceCorner.BOTTOM_RIGHT),
    ALL_0001(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.TOP_LEFT.bitFlag),
    ALL_0010(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.TOP_RIGHT.bitFlag),
    ALL_0011(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.TOP_RIGHT.bitFlag | FaceCorner.TOP_LEFT.bitFlag),
    ALL_0100(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_LEFT.bitFlag),
    ALL_0101(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_LEFT.bitFlag | FaceCorner.TOP_LEFT.bitFlag),
    ALL_0110(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_LEFT.bitFlag | FaceCorner.TOP_RIGHT.bitFlag),
    ALL_0111(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_LEFT.bitFlag | FaceCorner.TOP_RIGHT.bitFlag | FaceCorner.TOP_LEFT.bitFlag),
    ALL_1000(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_RIGHT.bitFlag),
    ALL_1001(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_RIGHT.bitFlag | FaceCorner.TOP_LEFT.bitFlag),
    ALL_1010(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_RIGHT.bitFlag | FaceCorner.TOP_RIGHT.bitFlag),
    ALL_1011(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_RIGHT.bitFlag | FaceCorner.TOP_RIGHT.bitFlag | FaceCorner.TOP_LEFT.bitFlag),
    ALL_1100(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_RIGHT.bitFlag | FaceCorner.BOTTOM_LEFT.bitFlag),
    ALL_1101(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_RIGHT.bitFlag | FaceCorner.BOTTOM_LEFT.bitFlag | FaceCorner.TOP_LEFT.bitFlag),
    ALL_1110(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_RIGHT.bitFlag | FaceCorner.BOTTOM_LEFT.bitFlag | FaceCorner.TOP_RIGHT.bitFlag),
    ALL_1111(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_RIGHT.bitFlag | FaceCorner.BOTTOM_LEFT.bitFlag | FaceCorner.TOP_RIGHT.bitFlag | FaceCorner.TOP_LEFT.bitFlag);
    
    private static final FaceJoinState[] LOOKUP = new FaceJoinState[256];
    private static final FaceJoinState[][] SUBFACES = new FaceJoinState[FaceJoinState.values().length][];

    private final int bitFlags;
    private final FaceCorner[] cornerTests;
    private FaceJoinState[] subStates;

    static
    {
        for(FaceJoinState state : FaceJoinState.values())
        {
            LOOKUP[state.bitFlags] = state;
            
            ArrayList<FaceJoinState> subStateList = new ArrayList<FaceJoinState>();
            
            if(state == NO_FACE)
            {
                subStateList.add(NO_FACE);
            }
            else
            {
                for(FaceJoinState subState : FaceJoinState.values())
                {
                    if(subState != NO_FACE && (subState.bitFlags & state.bitFlags & 15) == (subState.bitFlags & 15))
                    {
                        subStateList.add(subState);
                    }
                }
            }
            state.subStates = subStateList.toArray(new FaceJoinState[subStateList.size()]);
        }
    }
    
    private FaceJoinState(int faceBits, int cornerBits, FaceCorner... cornerTests)
    {
        this.bitFlags = faceBits | (cornerBits << 4);
        this.cornerTests = cornerTests;
    }
    
    private static FaceJoinState find(int faceBits, int cornerBits)
    {
        return LOOKUP[(faceBits & 15) | ((cornerBits & 15) << 4)];
    }
    
    public static FaceJoinState find(EnumFacing face, SimpleJoin join)
    {
        int faceFlags = 0;
        
        FaceJoinState fjs;
        
        if(join.isJoined(face))
        {
            fjs = FaceJoinState.NO_FACE;
        }
        else
        {                   
            for(FaceSide fside : FaceSide.values())
            {
                if(join.isJoined(fside.getRelativeFace(face)))
                {
                    faceFlags |= fside.bitFlag;
                }
            }
        
            fjs = FaceJoinState.find(faceFlags, 0);
        }
        return fjs;
    }
    
    public static FaceJoinState find(EnumFacing face, NeighborTestResults tests)
    {
        int faceFlags = 0;
        int cornerFlags = 0;
        
        FaceJoinState fjs;
        
        if(tests.result(face))
        {
            fjs = FaceJoinState.NO_FACE;
        }
        else
        {                   
            for(FaceSide fside : FaceSide.values())
            {
                EnumFacing joinFace = fside.getRelativeFace(face);
                if(tests.result(joinFace) && !tests.result(BlockCorner.find(face, joinFace)))
                {
                    faceFlags |= fside.bitFlag;
                }
            }
        
            fjs = FaceJoinState.find(faceFlags, cornerFlags);

            if(fjs.hasCornerTests())
            {
                for(FaceCorner corner : fjs.getCornerTests())
                {
                    if(!tests.result(corner.side1.getRelativeFace(face), corner.side2.getRelativeFace(face))
                            || tests.result(corner.side1.getRelativeFace(face), corner.side2.getRelativeFace(face), face))
                    {
                        cornerFlags |= corner.bitFlag;
                    }
                }
                
                fjs = FaceJoinState.find(faceFlags, cornerFlags);
            }
        }
        return fjs;
    }
    
    private boolean hasCornerTests()
    {
        return (cornerTests != null && cornerTests.length > 0);
    }
    
    private FaceCorner[] getCornerTests()
    {
        return cornerTests;
    }
    
    public FaceJoinState[] getSubStates()
    {
        return subStates;
    }
    
    public boolean isJoined(FaceSide side)
    {
        return (this.bitFlags & side.bitFlag) == side.bitFlag;
    }
    
    public boolean isJoined(EnumFacing toFace, EnumFacing onFace)
    {
        FaceSide side = FaceSide.lookup(toFace, onFace);
        return side == null ? false : this.isJoined(side);
    }
    
    /**
     * True if connected-texture/shape blocks need to render corner due
     * to missing/covered block in adjacent corner.
     */
    public boolean needsCorner(FaceCorner corner)
    {
        return ((this.bitFlags >> 4) & corner.bitFlag) == corner.bitFlag;
    }
    
    public boolean needsCorner(EnumFacing face1, EnumFacing face2, EnumFacing onFace)
    {
        FaceSide side1 = FaceSide.lookup(face1, onFace);
        FaceSide side2 = FaceSide.lookup(face2, onFace);
        return side1 == null || side2 == null ? false : this.needsCorner(FaceCorner.find(side1, side2));
    }
}
