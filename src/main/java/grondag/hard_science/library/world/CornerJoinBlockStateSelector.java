package grondag.hard_science.library.world;

import grondag.hard_science.library.world.NeighborBlocks.NeighborTestResults;
import net.minecraft.util.EnumFacing;

public class CornerJoinBlockStateSelector
{
    // STATIC MEMBERS START
    public static final int BLOCK_JOIN_STATE_COUNT = 20115;
    private static final CornerJoinBlockState BLOCK_JOIN_STATES[] = new CornerJoinBlockState[BLOCK_JOIN_STATE_COUNT];
    private static final CornerJoinBlockStateSelector BLOCK_JOIN_SELECTOR[] = new CornerJoinBlockStateSelector[64];
    
    
    static
    {
        int firstIndex = 0;
        
        for(int i = 0; i < 64; i++)
        {
            SimpleJoin baseJoin = new SimpleJoin(i);
            BLOCK_JOIN_SELECTOR[i] = new CornerJoinBlockStateSelector(baseJoin, firstIndex);
            
            for(int j = 0; j < BLOCK_JOIN_SELECTOR[i].getStateCount(); j++)
            {
                BLOCK_JOIN_STATES[firstIndex + j] = BLOCK_JOIN_SELECTOR[i].getJoinFromIndex(firstIndex + j);
            }

            firstIndex += BLOCK_JOIN_SELECTOR[i].getStateCount();
        }
    }
    
    public static int findIndex(NeighborTestResults tests)
    {
        SimpleJoin baseJoin = new SimpleJoin(tests);
        return BLOCK_JOIN_SELECTOR[baseJoin.getIndex()].getIndexFromNeighbors(tests);
    }
    
    public static CornerJoinBlockState getJoinState(int index)
    {
        return BLOCK_JOIN_STATES[index];
    }
    
    // STATIC MEMBERS END
    
    private final int firstIndex;
    private final SimpleJoin simpleJoin;
    
    private CornerJoinFaceSelector faceSelector[] = new CornerJoinFaceSelector[EnumFacing.values().length];
    
    private CornerJoinBlockStateSelector(SimpleJoin baseJoinState, int firstIndex)
    {
        this.firstIndex = firstIndex;
        this.simpleJoin = baseJoinState;
        for(EnumFacing face : EnumFacing.values())
        {
            faceSelector[face.ordinal()] = new CornerJoinFaceSelector(face, baseJoinState);
        }
    }

    private int getStateCount()
    {
        int count = 1;
        for(EnumFacing face : EnumFacing.values())
        {
            count *= faceSelector[face.ordinal()].faceCount;
        }
        return count;
    }
    
    private int getIndexFromNeighbors(NeighborTestResults tests)
    {
        int index = 0;
        int shift = 1;
        for(EnumFacing face : EnumFacing.values())
        {
            if(faceSelector[face.ordinal()].faceCount > 1)
            {
                index += shift * faceSelector[face.ordinal()].getIndexFromNeighbors(tests);
                shift *= faceSelector[face.ordinal()].faceCount;
            }
        }
        return index + firstIndex;
    }
    
    private CornerJoinBlockState getJoinFromIndex(int index)
    {
        int shift = 1;
        int localIndex = index - firstIndex;
        
        CornerJoinBlockState retVal = new CornerJoinBlockState(index, simpleJoin);
        
        for(EnumFacing face : EnumFacing.values())
        {
            if(faceSelector[face.ordinal()].faceCount == 1)
            {
                retVal.setFaceJoinState(face, faceSelector[face.ordinal()].getFaceJoinFromIndex(0));
            }
            else
            {
                int faceIndex = (localIndex / shift) % faceSelector[face.ordinal()].faceCount;
                retVal.setFaceJoinState(face, faceSelector[face.ordinal()].getFaceJoinFromIndex(faceIndex));
                shift *= faceSelector[face.ordinal()].faceCount;
            }
        }       

        return retVal;
    }
}
