package grondag.adversity.niceblock.newmodel.joinstate;

import net.minecraft.util.EnumFacing;

public enum FaceSide
{
    TOP(EnumFacing.NORTH, EnumFacing.NORTH, EnumFacing.UP, EnumFacing.UP, EnumFacing.UP, EnumFacing.UP),
    BOTTOM(EnumFacing.SOUTH, EnumFacing.SOUTH, EnumFacing.DOWN, EnumFacing.DOWN, EnumFacing.DOWN, EnumFacing.DOWN),
    LEFT(EnumFacing.WEST, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.WEST),
    RIGHT(EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST);
    
    // for a given face, which face is at the position identified by this enum? 
    private final EnumFacing RELATIVE_LOOKUP[] = new EnumFacing[EnumFacing.values().length];
    
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
    
    public EnumFacing getRelativeFace(EnumFacing face)
    {
        return RELATIVE_LOOKUP[face.ordinal()];
    }  
}
