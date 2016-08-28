package grondag.adversity.library.joinstate;

import grondag.adversity.library.NeighborBlocks;
import net.minecraft.util.EnumFacing;

public class SimpleJoin
{
    
    private final byte joins;
    
    public static final int STATE_COUNT = 64; // 2^6
    
    public SimpleJoin(NeighborBlocks.NeighborTestResults testResults)
    {
        this.joins = getIndex(testResults);
    }
    
    public static byte getIndex(NeighborBlocks.NeighborTestResults testResults)
    {
        byte j = 0;
        for(EnumFacing face : EnumFacing.values())
        {
            if(testResults.result(face))
            {
                j |= NeighborBlocks.FACE_FLAGS[face.ordinal()];
            }
        }
        return j;
    }
    
    public SimpleJoin(boolean up, boolean down, boolean east, boolean west, boolean north, boolean south)
    {
    	byte j = 0;
    	if(up) j |= NeighborBlocks.FACE_FLAGS[EnumFacing.UP.ordinal()];
    	if(down) j |= NeighborBlocks.FACE_FLAGS[EnumFacing.DOWN.ordinal()];
    	if(east) j |= NeighborBlocks.FACE_FLAGS[EnumFacing.EAST.ordinal()];
    	if(west) j |= NeighborBlocks.FACE_FLAGS[EnumFacing.WEST.ordinal()];
    	if(north) j |= NeighborBlocks.FACE_FLAGS[EnumFacing.NORTH.ordinal()];
    	if(south) j |= NeighborBlocks.FACE_FLAGS[EnumFacing.SOUTH.ordinal()];
        this.joins = j;
    }
    
    public SimpleJoin(int index)
    {
        this.joins = (byte)index;
    }
    
    public boolean isJoined(EnumFacing face)
    {
        return (joins & NeighborBlocks.FACE_FLAGS[face.ordinal()]) == NeighborBlocks.FACE_FLAGS[face.ordinal()];
    }
    
    public int getIndex()
    {
        return (int) joins;
    }
}