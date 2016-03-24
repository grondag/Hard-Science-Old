package grondag.adversity.library.joinstate;

import net.minecraft.util.EnumFacing;

public class FacadeFaceSelector
{
    public final short[] selectors = new short[EnumFacing.values().length];
    
    public FacadeFaceSelector(int upFace, int downFace, int eastFace, int westFace, int northFace, int southFace, int faceOffset) {
        selectors[EnumFacing.UP.ordinal()] = (short) (upFace + faceOffset);
        selectors[EnumFacing.DOWN.ordinal()] = (short) (downFace + faceOffset);
        selectors[EnumFacing.EAST.ordinal()] = (short) (eastFace + faceOffset);
        selectors[EnumFacing.WEST.ordinal()] = (short) (westFace + faceOffset);
        selectors[EnumFacing.NORTH.ordinal()] = (short) (northFace + faceOffset);
        selectors[EnumFacing.SOUTH.ordinal()] = (short) (southFace + faceOffset);
    }
    
    public FacadeFaceSelector(int upFace, int downFace, int eastFace, int westFace, int northFace, int southFace) {
        this(upFace, downFace, eastFace, westFace, northFace, southFace, 0);
    }

}