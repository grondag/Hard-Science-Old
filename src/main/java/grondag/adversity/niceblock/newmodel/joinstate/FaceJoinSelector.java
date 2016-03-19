package grondag.adversity.niceblock.newmodel.joinstate;

import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.niceblock.newmodel.ModelReference.SimpleJoin;
import net.minecraft.util.EnumFacing;

public class FaceJoinSelector
{
    private final EnumFacing myFace;
    
    public final int faceCount;
    public final FaceJoinState[] faceJoins;
    public final int[] joinIndex = new int[48];
    
    public FaceJoinSelector(EnumFacing face, SimpleJoin baseJoinState)
    {
        myFace = face;
        faceJoins = FaceJoinState.find(face, baseJoinState).getSubStates();
        this.faceCount = faceJoins.length;
        
        for(int i = 0; i < faceCount; i++)
        {
            joinIndex[faceJoins[i].ordinal()] = i;
        }
    }

    public int getIndexFromNeighbors(NeighborTestResults tests)
    {
        return joinIndex[FaceJoinState.find(myFace, tests).ordinal()];
    }
    
    public FaceJoinState getFaceJoinFromIndex(int index)
    {
        return faceJoins[index];
    }
}
