package grondag.adversity.library.joinstate;

import net.minecraft.util.EnumFacing;

public class CornerJoinBlockState
{
    private byte faceJoinIndex[] = new byte[EnumFacing.values().length];
    
    void setFaceJoinState(EnumFacing face, CornerJoinFaceState state)
    {
        faceJoinIndex[face.ordinal()]=(byte)state.ordinal();
    }
    
    public CornerJoinFaceState getFaceJoinState(EnumFacing face)
    {
        return CornerJoinFaceState.values()[faceJoinIndex[face.ordinal()]];
    }
}