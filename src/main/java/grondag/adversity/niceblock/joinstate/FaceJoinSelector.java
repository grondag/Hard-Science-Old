package grondag.adversity.niceblock.joinstate;

import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.niceblock.support.ModelReference.SimpleJoin;
import net.minecraft.util.EnumFacing;

public class FaceJoinSelector
{
    public final EnumFacing face;
    
    public final int faceCount;
    public final FaceJoinState[] faceJoins;
    public final int[] joinIndex = new int[48];
    
    public FaceJoinSelector(EnumFacing face, SimpleJoin baseJoinState)
    {
        this.face = face;
        faceJoins = FaceJoinState.find(face, baseJoinState).getSubStates();
        this.faceCount = faceJoins.length;
        
        for(int i = 0; i < faceCount; i++)
        {
            joinIndex[faceJoins[i].ordinal()] = i;
        }
    }

    public int getIndexFromNeighbors(NeighborTestResults tests)
    {
        return joinIndex[FaceJoinState.find(face, tests).ordinal()];
    }
    
    public FaceJoinState getFaceJoinFromIndex(int index)
    {
        return faceJoins[index];
    }
}
