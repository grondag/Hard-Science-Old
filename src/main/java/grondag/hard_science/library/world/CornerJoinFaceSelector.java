package grondag.hard_science.library.world;

import grondag.exotic_matter.world.NeighborBlocks;
import net.minecraft.util.EnumFacing;

public class CornerJoinFaceSelector
{
    public final EnumFacing face;
    
    public final int faceCount;
    public final CornerJoinFaceState[] faceJoins;
    public final int[] joinIndex = new int[48];
    
    public CornerJoinFaceSelector(EnumFacing face, SimpleJoin baseJoinState)
    {
        this.face = face;
        faceJoins = CornerJoinFaceState.find(face, baseJoinState).getSubStates();
        this.faceCount = faceJoins.length;
        
        for(int i = 0; i < faceCount; i++)
        {
            joinIndex[faceJoins[i].ordinal()] = i;
        }
    }

    public int getIndexFromNeighbors(NeighborBlocks<?>.NeighborTestResults tests)
    {
        return joinIndex[CornerJoinFaceState.find(face, tests).ordinal()];
    }
    
    public CornerJoinFaceState getFaceJoinFromIndex(int index)
    {
        return faceJoins[index];
    }
}
