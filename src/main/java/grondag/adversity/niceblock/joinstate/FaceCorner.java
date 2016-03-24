package grondag.adversity.niceblock.joinstate;

public enum FaceCorner
{
    TOP_LEFT(FaceSide.TOP, FaceSide.LEFT),
    TOP_RIGHT(FaceSide.TOP, FaceSide.RIGHT),
    BOTTOM_LEFT(FaceSide.BOTTOM, FaceSide.LEFT),
    BOTTOM_RIGHT(FaceSide.BOTTOM, FaceSide.RIGHT);
    
    private static FaceCorner[][]LOOKUP = new FaceCorner[4][4];

    public final FaceSide side1;
    public final FaceSide side2;
    public final int bitFlag;
    
    static
    {
        for(FaceCorner corner : FaceCorner.values())
        {
            LOOKUP[corner.side1.ordinal()][corner.side2.ordinal()]=corner;
            LOOKUP[corner.side2.ordinal()][corner.side1.ordinal()]=corner;
        }
    }
    
    private FaceCorner(FaceSide side1, FaceSide side2)
    {
        this.side1 = side1;
        this.side2 = side2;
        this.bitFlag = 1 << this.ordinal();
    }
    
    public static FaceCorner find(FaceSide side1, FaceSide side2)
    {
        return LOOKUP[side1.ordinal()][side2.ordinal()];
    }
}
