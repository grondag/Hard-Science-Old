package grondag.hard_science.library.world;

import net.minecraft.util.math.Vec3i;

public enum HorizontalCorner
{
    NORTH_EAST(HorizontalFace.NORTH, HorizontalFace.EAST),
    NORTH_WEST(HorizontalFace.NORTH, HorizontalFace.WEST),
    SOUTH_EAST(HorizontalFace.SOUTH, HorizontalFace.EAST),
    SOUTH_WEST(HorizontalFace.SOUTH, HorizontalFace.WEST);

    public final HorizontalFace face1;
    public final HorizontalFace face2;

    public final Vec3i directionVector;
    private static final HorizontalCorner[][] HORIZONTAL_CORNER_LOOKUP = new HorizontalCorner[4][4];
    
    static
    {
        for(HorizontalCorner corner : HorizontalCorner.values())
        {
            HORIZONTAL_CORNER_LOOKUP[corner.face1.ordinal()][corner.face2.ordinal()] = corner;
            HORIZONTAL_CORNER_LOOKUP[corner.face2.ordinal()][corner.face1.ordinal()] = corner;
        }
    }

    private HorizontalCorner(HorizontalFace face1, HorizontalFace face2)
    {
        this.face1 = face1;
        this.face2 = face2;
        this.directionVector = new Vec3i(face1.face.getDirectionVec().getX() + face2.face.getDirectionVec().getX(), 0, face1.face.getDirectionVec().getZ() + face2.face.getDirectionVec().getZ());    }

    public static HorizontalCorner find(HorizontalFace face1, HorizontalFace face2)
    {
        return HorizontalCorner.HORIZONTAL_CORNER_LOOKUP[face1.ordinal()][face2.ordinal()];
    }

}