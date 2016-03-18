package grondag.adversity.niceblock.newmodel.joinstate;

import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.niceblock.newmodel.ModelReference.SimpleJoin;
import net.minecraft.util.EnumFacing;

public class FaceJoinSelector
{
    private final EnumFacing myFace;
    private final int firstIndex;
    
    public final int faceCount;
    public final FaceJoinState[] faceJoins;
    
    public FaceJoinSelector(EnumFacing face, SimpleJoin baseJoinState, int firstIndex)
    {
        myFace = face;
        this.firstIndex = firstIndex;
        this.faceCount = 1;
        this.faceJoins = new FaceJoinState[faceCount];
        
        //identify join tests & join cover tests
        
        //identify corner tests & corner cover tests
        
        //iterate through the combinations and identify all possible faces
    }

    public int getIndexFromNeighbors(NeighborTestResults tests)
    {
        return 0;
    }
    
    public FaceJoinState getFaceJoinFromIndex(int index)
    {
        return faceJoins[index];
    }
}
