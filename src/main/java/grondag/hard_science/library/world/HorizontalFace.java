package grondag.hard_science.library.world;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;

public enum HorizontalFace
{
    NORTH(EnumFacing.NORTH),
    EAST(EnumFacing.EAST),
    SOUTH(EnumFacing.SOUTH),
    WEST(EnumFacing.WEST);

    public final EnumFacing face;

    public final Vec3i directionVector;

    private static final HorizontalFace HORIZONTAL_FACE_LOOKUP[] = new HorizontalFace[6];
    
    static
    {
        for(HorizontalFace hFace : HorizontalFace.values())
        {
            HORIZONTAL_FACE_LOOKUP[hFace.face.ordinal()] = hFace;
        }
    }

    private HorizontalFace(EnumFacing face)
    {
        this.face = face;
        this.directionVector = face.getDirectionVec();
    }
    
    public static HorizontalFace find(EnumFacing face)
    {
        return HorizontalFace.HORIZONTAL_FACE_LOOKUP[face.ordinal()];
    }
    
    public HorizontalFace getLeft()
    {
        if(this.ordinal() == 0)
        {
            return HorizontalFace.values()[3];
        }
        else
        {
            return HorizontalFace.values()[this.ordinal()-1];
        }
    }
    
    public HorizontalFace getRight()
    {
        if(this.ordinal() == 3)
        {
            return HorizontalFace.values()[0];
        }
        else
        {
            return HorizontalFace.values()[this.ordinal()+1];
        }
    }

}