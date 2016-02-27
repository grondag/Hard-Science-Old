package grondag.adversity.niceblock.newmodel;

import net.minecraft.util.EnumFacing;

public class FacadeFaceSelector
{
    protected final byte[] selectors = new byte[EnumFacing.values().length];
    
    protected FacadeFaceSelector(int upFace, int downFace, int eastFace, int westFace, int northFace, int southFace) {
        selectors[EnumFacing.UP.ordinal()] = (byte) upFace;
        selectors[EnumFacing.DOWN.ordinal()] = (byte) downFace;
        selectors[EnumFacing.EAST.ordinal()] = (byte) eastFace;
        selectors[EnumFacing.WEST.ordinal()] = (byte) westFace;
        selectors[EnumFacing.NORTH.ordinal()] = (byte) northFace;
        selectors[EnumFacing.SOUTH.ordinal()] = (byte) southFace;
    }
}