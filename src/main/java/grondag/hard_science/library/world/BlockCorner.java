package grondag.hard_science.library.world;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;

public enum BlockCorner
{
    UP_EAST(EnumFacing.UP, EnumFacing.EAST, Rotation.ROTATE_NONE),          // Z AXIS
    UP_WEST(EnumFacing.UP, EnumFacing.WEST, Rotation.ROTATE_270),           // Z AXIS
    UP_NORTH(EnumFacing.UP, EnumFacing.NORTH, Rotation.ROTATE_180),         // X AXIS
    UP_SOUTH(EnumFacing.UP, EnumFacing.SOUTH, Rotation.ROTATE_90),          // X AXIS
    NORTH_EAST(EnumFacing.NORTH, EnumFacing.EAST, Rotation.ROTATE_NONE),    // Y AXIS
    NORTH_WEST(EnumFacing.NORTH, EnumFacing.WEST, Rotation.ROTATE_270),     // Y AXIS
    SOUTH_EAST(EnumFacing.SOUTH, EnumFacing.EAST, Rotation.ROTATE_90),      // Y AXIS
    SOUTH_WEST(EnumFacing.SOUTH, EnumFacing.WEST, Rotation.ROTATE_180),     // Y AXIS
    DOWN_EAST(EnumFacing.DOWN, EnumFacing.EAST, Rotation.ROTATE_90),        // Z AXIS
    DOWN_WEST(EnumFacing.DOWN, EnumFacing.WEST, Rotation.ROTATE_180),       // Z AXIS
    DOWN_NORTH(EnumFacing.DOWN, EnumFacing.NORTH, Rotation.ROTATE_270),     // X AXIS
    DOWN_SOUTH(EnumFacing.DOWN, EnumFacing.SOUTH, Rotation.ROTATE_NONE);    // X AXIS

    public final EnumFacing face1;
    public final EnumFacing face2;
    
    /**
     * Axis that is orthogonal to both faces.
     */
    public final EnumFacing.Axis orthogonalAxis;
    
    
    /**
     * Used to position models like stairs/wedges.
     * Representation rotation around the orthogonal axis such 
     * that face1 and face2 are most occluded.
     * Based on "default" model having Y axis and occluding north and east faces.
     */
    public final Rotation modelRotation;
    
    public final int bitFlag;
    public final Vec3i directionVector;
    /** 
     * Ordinal sequence that includes all faces, corner and far corners.
     * Use to index them in a mixed array.
     */
    public final int superOrdinal;
    
    private static final BlockCorner[][] CORNER_LOOKUP = new BlockCorner[6][6];
    
    /** used to look up by axis and rotation */
    private static final BlockCorner[][] MODEL_LOOKUP = new BlockCorner[3][4];
    
    static
    {
        for(BlockCorner corner : BlockCorner.values())
        {
            CORNER_LOOKUP[corner.face1.ordinal()][corner.face2.ordinal()] = corner;
            CORNER_LOOKUP[corner.face2.ordinal()][corner.face1.ordinal()] = corner;
            MODEL_LOOKUP[corner.orthogonalAxis.ordinal()][corner.modelRotation.ordinal()] = corner;
        }
    }

    private BlockCorner(EnumFacing face1, EnumFacing face2, Rotation modelRotation)
    {
        this.face1 = face1;
        this.face2 = face2;
        this.modelRotation = modelRotation;
        this.bitFlag = 1 << (NeighborBlocks.FACE_FLAGS.length + this.ordinal());
        this.superOrdinal = EnumFacing.values().length + this.ordinal();
        boolean hasX = (face1.getAxis() == EnumFacing.Axis.X || face2.getAxis() == EnumFacing.Axis.X);
        boolean hasY = (face1.getAxis() == EnumFacing.Axis.Y || face2.getAxis() == EnumFacing.Axis.Y);
        this.orthogonalAxis = hasX && hasY ? EnumFacing.Axis.Z : hasX ? EnumFacing.Axis.Y : EnumFacing.Axis.X;

        Vec3i v1 = face1.getDirectionVec();
        Vec3i v2 = face2.getDirectionVec();
        this.directionVector = new Vec3i(v1.getX() + v2.getX(), v1.getY() + v2.getY(), v1.getZ() + v2.getZ());

    }

    public static BlockCorner find(EnumFacing face1, EnumFacing face2)
    {
        return BlockCorner.CORNER_LOOKUP[face1.ordinal()][face2.ordinal()];
    }
    
    public static BlockCorner find(EnumFacing.Axis axis, Rotation modelRotation)
    {
        return BlockCorner.MODEL_LOOKUP[axis.ordinal()][modelRotation.ordinal()];
    }
}