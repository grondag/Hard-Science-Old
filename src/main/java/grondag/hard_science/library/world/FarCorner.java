package grondag.hard_science.library.world;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;

public enum FarCorner
{
    UP_NORTH_EAST(EnumFacing.UP, EnumFacing.EAST, EnumFacing.NORTH),
    UP_NORTH_WEST(EnumFacing.UP, EnumFacing.WEST, EnumFacing.NORTH),
    UP_SOUTH_EAST(EnumFacing.UP, EnumFacing.EAST, EnumFacing.SOUTH),
    UP_SOUTH_WEST(EnumFacing.UP, EnumFacing.WEST, EnumFacing.SOUTH),
    DOWN_NORTH_EAST(EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.NORTH),
    DOWN_NORTH_WEST(EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.NORTH),
    DOWN_SOUTH_EAST(EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.SOUTH),
    DOWN_SOUTH_WEST(EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.SOUTH);


    public final EnumFacing face1;
    public final EnumFacing face2;
    public final EnumFacing face3;
    public final int bitFlag;
    public final Vec3i directionVector;
    /** 
     * Ordinal sequence that includes all faces, corner and far corners.
     * Use to index them in a mixed array.
     */
    public final int superOrdinal;
    private static final FarCorner[][][] FAR_CORNER_LOOKUP = new FarCorner[6][6][6];
    
    static
    {
        for(FarCorner corner : FarCorner.values())
        {
            FAR_CORNER_LOOKUP[corner.face1.ordinal()][corner.face2.ordinal()][corner.face3.ordinal()] = corner;
            FAR_CORNER_LOOKUP[corner.face1.ordinal()][corner.face3.ordinal()][corner.face2.ordinal()] = corner;
            FAR_CORNER_LOOKUP[corner.face2.ordinal()][corner.face1.ordinal()][corner.face3.ordinal()] = corner;
            FAR_CORNER_LOOKUP[corner.face2.ordinal()][corner.face3.ordinal()][corner.face1.ordinal()] = corner;
            FAR_CORNER_LOOKUP[corner.face3.ordinal()][corner.face2.ordinal()][corner.face1.ordinal()] = corner;
            FAR_CORNER_LOOKUP[corner.face3.ordinal()][corner.face1.ordinal()][corner.face2.ordinal()] = corner;
        }
    }

    private FarCorner(EnumFacing face1, EnumFacing face2, EnumFacing face3)
    {
        this.face1 = face1;
        this.face2 = face2;
        this.face3 = face3;
        this.bitFlag = 1 << (NeighborBlocks.FACE_FLAGS.length + BlockCorner.values().length + this.ordinal());
        this.superOrdinal = this.ordinal() + EnumFacing.values().length + BlockCorner.values().length;

          Vec3i v1 = face1.getDirectionVec();
        Vec3i v2 = face2.getDirectionVec();
        Vec3i v3 = face3.getDirectionVec();
        this.directionVector = new Vec3i(v1.getX() + v2.getX() + v3.getX(), v1.getY() + v2.getY() + v3.getY(), v1.getZ() + v2.getZ() + v3.getZ());

    }

    public static FarCorner find(EnumFacing face1, EnumFacing face2, EnumFacing face3)
    {
        return FarCorner.FAR_CORNER_LOOKUP[face1.ordinal()][face2.ordinal()][face3.ordinal()];
    }
}