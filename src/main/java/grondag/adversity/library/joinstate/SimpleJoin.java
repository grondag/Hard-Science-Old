package grondag.adversity.library.joinstate;

import grondag.adversity.library.NeighborBlocks;
import net.minecraft.util.EnumFacing;

public class SimpleJoin
{
    
    private final byte joins;
    
    public SimpleJoin(NeighborBlocks.NeighborTestResults testResults)
    {
        byte j = 0;
        for(EnumFacing face : EnumFacing.values())
        {
            if(testResults.result(face))
            {
                j |= NeighborBlocks.FACE_FLAGS[face.ordinal()];
            }
        }
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