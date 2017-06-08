package grondag.adversity.library.joinstate;

import net.minecraft.util.EnumFacing;

public enum FaceSide
{
    TOP(EnumFacing.NORTH, EnumFacing.NORTH, EnumFacing.UP, EnumFacing.UP, EnumFacing.UP, EnumFacing.UP),
    BOTTOM(EnumFacing.SOUTH, EnumFacing.SOUTH, EnumFacing.DOWN, EnumFacing.DOWN, EnumFacing.DOWN, EnumFacing.DOWN),
    LEFT(EnumFacing.WEST, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.WEST),
    RIGHT(EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST);
    
    // for a given face, which face is at the position identified by this enum? 
    private final EnumFacing RELATIVE_LOOKUP[] = new EnumFacing[EnumFacing.values().length];
    
    // find the side for a given face orthogonal to a face
    private final static FaceSide FACE_LOOKUP[][] = new FaceSide[EnumFacing.values().length][EnumFacing.values().length];
    
    static
    {
        for(EnumFacing onFace : EnumFacing.values())
        {
            for(EnumFacing sideFace : EnumFacing.values())
            {
                FaceSide match = null;
                
                for(FaceSide side : FaceSide.values())
                {
                    if(side.getRelativeFace(onFace) == sideFace)
                    {
                        match = side;
                    }
                }
                
                FACE_LOOKUP[onFace.ordinal()][sideFace.ordinal()] = match;
            }
        }
    }
    
    /**
     * Determines if the given sideFace is TOP, BOTTOM, LEFT or RIGHT
     * of onFace.  If none (sideFace on same axis as onFace), return null;
     */
    public static FaceSide lookup(EnumFacing sideFace, EnumFacing onFace)
    {
        return FACE_LOOKUP[onFace.ordinal()][sideFace.ordinal()];
    }
    
    private FaceSide(EnumFacing up, EnumFacing down, EnumFacing east, EnumFacing west, EnumFacing north, EnumFacing south)
    {
        RELATIVE_LOOKUP[EnumFacing.UP.ordinal()] = up;
        RELATIVE_LOOKUP[EnumFacing.DOWN.ordinal()] = down;
        RELATIVE_LOOKUP[EnumFacing.EAST.ordinal()] = east;
        RELATIVE_LOOKUP[EnumFacing.WEST.ordinal()] = west;
        RELATIVE_LOOKUP[EnumFacing.NORTH.ordinal()] = north;
        RELATIVE_LOOKUP[EnumFacing.SOUTH.ordinal()] = south;
        
        this.bitFlag = 1 << this.ordinal();
    }
    
    public final int bitFlag;
    
    public FaceSide getClockwise()
    {
        switch(this)
        {
        case BOTTOM:
            return LEFT;
        case LEFT:
            return TOP;
        case RIGHT:
            return BOTTOM;
        case TOP:
            return RIGHT;
        default:
            return null;
        }
    }
    
    public FaceSide getCounterClockwise()
    {
        switch(this)
        {
        case BOTTOM:
            return RIGHT;
        case LEFT:
            return BOTTOM;
        case RIGHT:
            return TOP;
        case TOP:
            return LEFT;
        default:
            return null;
        }
    }
    
    /**
     * Returns the face that is at the side identified by this enum 
     * on the given face.
     */
    public EnumFacing getRelativeFace(EnumFacing face)
    {
        return RELATIVE_LOOKUP[face.ordinal()];
    }  
}
