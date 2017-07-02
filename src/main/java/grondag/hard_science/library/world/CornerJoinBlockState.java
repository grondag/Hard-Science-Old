package grondag.hard_science.library.world;

import net.minecraft.util.EnumFacing;

public class CornerJoinBlockState
{
    private final int index;
    
    /** join state considering only direct neighbors */
    public final SimpleJoin simpleJoin;
    
    private byte faceJoinIndex[] = new byte[EnumFacing.values().length];
    
    CornerJoinBlockState(int index, SimpleJoin simpleJoin)
    {
        this.index = index;
        this.simpleJoin = simpleJoin;
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