package grondag.adversity.niceblock.newmodel.joinstate;

public enum FaceJoinState
{
    NO_FACE(0, 0),
    NONE(0, 0), //must be after NO_FACE, overwrites NO_FACE in lookup table, should never be found by lookup
    TOP(FaceSide.TOP.bitFlag(), 0),
    BOTTOM(FaceSide.BOTTOM.bitFlag(), 0),
    LEFT(FaceSide.LEFT.bitFlag(), 0),
    RIGHT(FaceSide.RIGHT.bitFlag(), 0),
    TOP_BOTTOM(FaceSide.TOP.bitFlag() | FaceSide.BOTTOM.bitFlag(), 0),
    LEFT_RIGHT(FaceSide.LEFT.bitFlag() | FaceSide.RIGHT.bitFlag(), 0),
    
    TOP_BOTTOM_RIGHT_00(FaceSide.TOP.bitFlag() | FaceSide.BOTTOM.bitFlag() | FaceSide.RIGHT.bitFlag(), 0, FaceCorner.TOP_RIGHT, FaceCorner.BOTTOM_RIGHT),
    TOP_BOTTOM_RIGHT_01(FaceSide.TOP.bitFlag() | FaceSide.BOTTOM.bitFlag() | FaceSide.RIGHT.bitFlag(), FaceCorner.TOP_RIGHT.bitFlag()),
    TOP_BOTTOM_RIGHT_10(FaceSide.TOP.bitFlag() | FaceSide.BOTTOM.bitFlag() | FaceSide.RIGHT.bitFlag(), FaceCorner.BOTTOM_RIGHT.bitFlag()),
    TOP_BOTTOM_RIGHT_11(FaceSide.TOP.bitFlag() | FaceSide.BOTTOM.bitFlag() | FaceSide.RIGHT.bitFlag(), FaceCorner.TOP_RIGHT.bitFlag() | FaceCorner.BOTTOM_RIGHT.bitFlag()),
    
    TOP_BOTTOM_LEFT_00(FaceSide.TOP.bitFlag() | FaceSide.BOTTOM.bitFlag() | FaceSide.LEFT.bitFlag(), 0, FaceCorner.TOP_LEFT, FaceCorner.BOTTOM_LEFT),
    TOP_BOTTOM_LEFT_01(FaceSide.TOP.bitFlag() | FaceSide.BOTTOM.bitFlag() | FaceSide.LEFT.bitFlag(), FaceCorner.TOP_LEFT.bitFlag()),
    TOP_BOTTOM_LEFT_10(FaceSide.TOP.bitFlag() | FaceSide.BOTTOM.bitFlag() | FaceSide.LEFT.bitFlag(), FaceCorner.BOTTOM_LEFT.bitFlag()),
    TOP_BOTTOM_LEFT_11(FaceSide.TOP.bitFlag() | FaceSide.BOTTOM.bitFlag() | FaceSide.LEFT.bitFlag(), FaceCorner.TOP_LEFT.bitFlag() | FaceCorner.BOTTOM_LEFT.bitFlag()),

    TOP_LEFT_RIGHT_00(FaceSide.TOP.bitFlag() | FaceSide.LEFT.bitFlag() | FaceSide.RIGHT.bitFlag(), 0, FaceCorner.TOP_LEFT, FaceCorner.TOP_RIGHT),
    TOP_LEFT_RIGHT_01(FaceSide.TOP.bitFlag() | FaceSide.LEFT.bitFlag() | FaceSide.RIGHT.bitFlag(), FaceCorner.TOP_LEFT.bitFlag()),
    TOP_LEFT_RIGHT_10(FaceSide.TOP.bitFlag() | FaceSide.LEFT.bitFlag() | FaceSide.RIGHT.bitFlag(), FaceCorner.TOP_RIGHT.bitFlag()),
    TOP_LEFT_RIGHT_11(FaceSide.TOP.bitFlag() | FaceSide.LEFT.bitFlag() | FaceSide.RIGHT.bitFlag(), FaceCorner.TOP_LEFT.bitFlag() | FaceCorner.TOP_RIGHT.bitFlag()),
    
    BOTTOM_LEFT_RIGHT_00(FaceSide.BOTTOM.bitFlag() | FaceSide.LEFT.bitFlag() | FaceSide.RIGHT.bitFlag(), 0, FaceCorner.BOTTOM_LEFT, FaceCorner.BOTTOM_RIGHT),
    BOTTOM_LEFT_RIGHT_01(FaceSide.BOTTOM.bitFlag() | FaceSide.LEFT.bitFlag() | FaceSide.RIGHT.bitFlag(), FaceCorner.BOTTOM_LEFT.bitFlag()),
    BOTTOM_LEFT_RIGHT_10(FaceSide.BOTTOM.bitFlag() | FaceSide.LEFT.bitFlag() | FaceSide.RIGHT.bitFlag(), FaceCorner.BOTTOM_RIGHT.bitFlag()),
    BOTTOM_LEFT_RIGHT_11(FaceSide.BOTTOM.bitFlag() | FaceSide.LEFT.bitFlag() | FaceSide.RIGHT.bitFlag(), FaceCorner.BOTTOM_LEFT.bitFlag() | FaceCorner.BOTTOM_RIGHT.bitFlag()),

    TOP_LEFT_0(FaceSide.TOP.bitFlag() | FaceSide.LEFT.bitFlag(), 0, FaceCorner.TOP_LEFT),
    TOP_LEFT_1(FaceSide.TOP.bitFlag() | FaceSide.LEFT.bitFlag(), FaceCorner.TOP_LEFT.bitFlag()),
    
    TOP_RIGHT_0(FaceSide.TOP.bitFlag() | FaceSide.RIGHT.bitFlag(), 0, FaceCorner.TOP_RIGHT),
    TOP_RIGHT_1(FaceSide.TOP.bitFlag() | FaceSide.RIGHT.bitFlag(), FaceCorner.TOP_RIGHT.bitFlag()),
    
    BOTTOM_LEFT_0(FaceSide.BOTTOM.bitFlag() | FaceSide.LEFT.bitFlag(), 0, FaceCorner.BOTTOM_LEFT),
    BOTTOM_LEFT_1(FaceSide.BOTTOM.bitFlag() | FaceSide.LEFT.bitFlag(), FaceCorner.BOTTOM_LEFT.bitFlag()),
    
    BOTTOM_RIGHT_0(FaceSide.BOTTOM.bitFlag() | FaceSide.RIGHT.bitFlag(), 0, FaceCorner.BOTTOM_RIGHT),
    BOTTOM_RIGHT_1(FaceSide.BOTTOM.bitFlag() | FaceSide.RIGHT.bitFlag(), FaceCorner.BOTTOM_RIGHT.bitFlag()),
    
    ALL_0000(FaceSide.TOP.bitFlag() | FaceSide.BOTTOM.bitFlag() | FaceSide.LEFT.bitFlag() | FaceSide.RIGHT.bitFlag(), 0, FaceCorner.TOP_LEFT, FaceCorner.TOP_RIGHT, FaceCorner.BOTTOM_LEFT, FaceCorner.BOTTOM_RIGHT),
    ALL_0001(FaceSide.TOP.bitFlag() | FaceSide.BOTTOM.bitFlag() | FaceSide.LEFT.bitFlag() | FaceSide.RIGHT.bitFlag(), FaceCorner.TOP_LEFT.bitFlag()),
    ALL_0010(FaceSide.TOP.bitFlag() | FaceSide.BOTTOM.bitFlag() | FaceSide.LEFT.bitFlag() | FaceSide.RIGHT.bitFlag(), FaceCorner.TOP_RIGHT.bitFlag()),
    ALL_0011(FaceSide.TOP.bitFlag() | FaceSide.BOTTOM.bitFlag() | FaceSide.LEFT.bitFlag() | FaceSide.RIGHT.bitFlag(), FaceCorner.TOP_RIGHT.bitFlag() | FaceCorner.TOP_LEFT.bitFlag()),
    ALL_0100(FaceSide.TOP.bitFlag() | FaceSide.BOTTOM.bitFlag() | FaceSide.LEFT.bitFlag() | FaceSide.RIGHT.bitFlag(), FaceCorner.BOTTOM_LEFT.bitFlag()),
    ALL_0101(FaceSide.TOP.bitFlag() | FaceSide.BOTTOM.bitFlag() | FaceSide.LEFT.bitFlag() | FaceSide.RIGHT.bitFlag(), FaceCorner.BOTTOM_LEFT.bitFlag() | FaceCorner.TOP_LEFT.bitFlag()),
    ALL_0110(FaceSide.TOP.bitFlag() | FaceSide.BOTTOM.bitFlag() | FaceSide.LEFT.bitFlag() | FaceSide.RIGHT.bitFlag(), FaceCorner.BOTTOM_LEFT.bitFlag() | FaceCorner.TOP_RIGHT.bitFlag()),
    ALL_0111(FaceSide.TOP.bitFlag() | FaceSide.BOTTOM.bitFlag() | FaceSide.LEFT.bitFlag() | FaceSide.RIGHT.bitFlag(), FaceCorner.BOTTOM_LEFT.bitFlag() | FaceCorner.TOP_RIGHT.bitFlag() | FaceCorner.TOP_LEFT.bitFlag()),
    ALL_1000(FaceSide.TOP.bitFlag() | FaceSide.BOTTOM.bitFlag() | FaceSide.LEFT.bitFlag() | FaceSide.RIGHT.bitFlag(), FaceCorner.BOTTOM_RIGHT.bitFlag()),
    ALL_1001(FaceSide.TOP.bitFlag() | FaceSide.BOTTOM.bitFlag() | FaceSide.LEFT.bitFlag() | FaceSide.RIGHT.bitFlag(), FaceCorner.BOTTOM_RIGHT.bitFlag() | FaceCorner.TOP_LEFT.bitFlag()),
    ALL_1010(FaceSide.TOP.bitFlag() | FaceSide.BOTTOM.bitFlag() | FaceSide.LEFT.bitFlag() | FaceSide.RIGHT.bitFlag(), FaceCorner.BOTTOM_RIGHT.bitFlag() | FaceCorner.TOP_RIGHT.bitFlag()),
    ALL_1011(FaceSide.TOP.bitFlag() | FaceSide.BOTTOM.bitFlag() | FaceSide.LEFT.bitFlag() | FaceSide.RIGHT.bitFlag(), FaceCorner.BOTTOM_RIGHT.bitFlag() | FaceCorner.TOP_RIGHT.bitFlag() | FaceCorner.TOP_LEFT.bitFlag()),
    ALL_1100(FaceSide.TOP.bitFlag() | FaceSide.BOTTOM.bitFlag() | FaceSide.LEFT.bitFlag() | FaceSide.RIGHT.bitFlag(), FaceCorner.BOTTOM_RIGHT.bitFlag() | FaceCorner.BOTTOM_LEFT.bitFlag()),
    ALL_1101(FaceSide.TOP.bitFlag() | FaceSide.BOTTOM.bitFlag() | FaceSide.LEFT.bitFlag() | FaceSide.RIGHT.bitFlag(), FaceCorner.BOTTOM_RIGHT.bitFlag() | FaceCorner.BOTTOM_LEFT.bitFlag() | FaceCorner.TOP_LEFT.bitFlag()),
    ALL_1110(FaceSide.TOP.bitFlag() | FaceSide.BOTTOM.bitFlag() | FaceSide.LEFT.bitFlag() | FaceSide.RIGHT.bitFlag(), FaceCorner.BOTTOM_RIGHT.bitFlag() | FaceCorner.BOTTOM_LEFT.bitFlag() | FaceCorner.TOP_RIGHT.bitFlag()),
    ALL_1111(FaceSide.TOP.bitFlag() | FaceSide.BOTTOM.bitFlag() | FaceSide.LEFT.bitFlag() | FaceSide.RIGHT.bitFlag(), FaceCorner.BOTTOM_RIGHT.bitFlag() | FaceCorner.BOTTOM_LEFT.bitFlag() | FaceCorner.TOP_RIGHT.bitFlag() | FaceCorner.TOP_LEFT.bitFlag());
    
    private static final FaceJoinState[] LOOKUP = new FaceJoinState[256];

    private final int bitFlags;
    private final FaceCorner[] cornerTests;

    static
    {
        for(FaceJoinState state : FaceJoinState.values())
        {
            LOOKUP[state.bitFlags] = state;
        }
    }
    
    private FaceJoinState(int faceBits, int cornerBits, FaceCorner... cornerTests)
    {
        this.bitFlags = faceBits | (cornerBits << 4);
        this.cornerTests = cornerTests;
    }
    
    public static FaceJoinState find(int faceBits, int cornerBits)
    {
        return LOOKUP[(faceBits & 15) | ((cornerBits & 15) << 4)];
    }
    
    public boolean hasCornerTests()
    {
        return (cornerTests != null && cornerTests.length > 0);
    }
    
    public FaceCorner[] getCornerTests()
    {
        return cornerTests;
    }
}
