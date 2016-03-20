package grondag.adversity.niceblock.newmodel.joinstate;

import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.niceblock.newmodel.ModelReference.SimpleJoin;
import net.minecraft.util.EnumFacing;

public class BlockJoinSelector
{
    // STATIC MEMBERS START
    public static final int BLOCK_JOIN_STATE_COUNT = 20115;
    private static final BlockJoinState BLOCK_JOIN_STATES[] = new BlockJoinState[BLOCK_JOIN_STATE_COUNT];
    private static final BlockJoinSelector BLOCK_JOIN_SELECTOR[] = new BlockJoinSelector[64];
    
    static
    {
        int firstIndex = 0;
        
        for(int i = 0; i < 64; i++)
        {
            SimpleJoin baseJoin = new SimpleJoin(i);
            BLOCK_JOIN_SELECTOR[i] = new BlockJoinSelector(baseJoin, firstIndex);
            
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
    
    public static BlockJoinState getJoinState(int index)
    {
        return BLOCK_JOIN_STATES[index];
    }
    
    // STATIC MEMBERS END
    
    private final int firstIndex;
    
    private FaceJoinSelector faceSelector[] = new FaceJoinSelector[EnumFacing.values().length];
    
    private BlockJoinSelector(SimpleJoin baseJoinState, int firstIndex)
    {
        this.firstIndex = firstIndex;
        for(EnumFacing face : EnumFacing.values())
        {
            faceSelector[face.ordinal()] = new FaceJoinSelector(face, baseJoinState);
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
    
    private BlockJoinState getJoinFromIndex(int index)
    {
        int shift = 1;
        int localIndex = index - firstIndex;
        
        BlockJoinState retVal = new BlockJoinState();
        
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
    
    public static class BlockJoinState
    {
        private byte faceJoinIndex[] = new byte[EnumFacing.values().length];
        
        private void setFaceJoinState(EnumFacing face, FaceJoinState state)
        {
            faceJoinIndex[face.ordinal()]=(byte)state.ordinal();
        }
        
        public FaceJoinState getFaceJoinState(EnumFacing face)
        {
            return FaceJoinState.values()[faceJoinIndex[face.ordinal()]];
        }
    }
}
