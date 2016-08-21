package grondag.adversity.library.joinstate;

import net.minecraft.util.EnumFacing;

public class CornerJoinBlockState
{
    private final int index;
    
    private byte faceJoinIndex[] = new byte[EnumFacing.values().length];
    
    CornerJoinBlockState(int index)
    {
        this.index = index;
    }
    
    public int getIndex()
    {
        return index;
    }
    
    void setFaceJoinState(EnumFacing face, CornerJoinFaceState state)
    {
        faceJoinIndex[face.ordinal()]=(byte)state.ordinal();
    }
    
    public CornerJoinFaceState getFaceJoinState(EnumFacing face)
    {
        return CornerJoinFaceState.values()[faceJoinIndex[face.ordinal()]];
    }
}