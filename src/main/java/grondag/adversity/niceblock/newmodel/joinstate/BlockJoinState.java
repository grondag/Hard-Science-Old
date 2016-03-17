package grondag.adversity.niceblock.newmodel.joinstate;

import java.util.ArrayList;

import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import grondag.adversity.Adversity;
import grondag.adversity.library.NeighborBlocks.BlockCorner;
import grondag.adversity.library.NeighborBlocks.FarCorner;
import grondag.adversity.niceblock.newmodel.ModelReference.CornerJoin;
import net.minecraft.util.EnumFacing;

public class BlockJoinState
{

    private final static BlockJoinState[] DEFINED = new BlockJoinState[60134];
    private final static TLongObjectHashMap<BlockJoinState> LOOKUP = new TLongObjectHashMap<BlockJoinState>(60134);
    private static int nextAvailableIndex = 0;
    //600134
    
    private final long stateBits;
    private final int stateIndex;

    static
    {
        for(int i = 0; i < FarCorner.DOWN_SOUTH_WEST.bitFlag * 2; i++)
        {
            CornerJoin.Far modelJoin = new CornerJoin.Far(i);
            
            FaceJoinState[] joinStates = new FaceJoinState[EnumFacing.values().length];
            
            for(EnumFacing face: EnumFacing.values())
            {
                int faceFlags = 0;
                int cornerFlags = 0;
                
                FaceJoinState fjs;
                
//                if(face == EnumFacing.WEST
//                        && modelJoin.isJoined(EnumFacing.DOWN)
//                        && modelJoin.isJoined(EnumFacing.UP)
//                        && modelJoin.isJoined(EnumFacing.NORTH)
//                        && modelJoin.isJoined(EnumFacing.SOUTH)
//                        && modelJoin.isJoined(EnumFacing.EAST)
//                        )
//                {
//                    Adversity.log.info("found one!");
//                }
                    
                
                if(modelJoin.isJoined(face))
                {
                    fjs = FaceJoinState.NO_FACE;
                }
                else
                {                   
                    for(FaceSide fside : FaceSide.values())
                    {
                        EnumFacing joinFace = fside.getRelativeFace(face);
                        BlockCorner joinCover = BlockCorner.find(face, joinFace);
                        if(modelJoin.isJoined(joinFace) &&
                                !modelJoin.isCornerPresent(joinCover))
                        {
                            faceFlags |= fside.bitFlag();
                        }
                    }
                
                    fjs = FaceJoinState.find(faceFlags, cornerFlags);
                
                    if(fjs.hasCornerTests())
                    {
                        for(FaceCorner corner : fjs.getCornerTests())
                        {
                            if(!modelJoin.isCornerPresent(corner.side1.getRelativeFace(face), corner.side2.getRelativeFace(face))
                                    || modelJoin.isCornerPresent(corner.side1.getRelativeFace(face), corner.side2.getRelativeFace(face), face))
                            {
                                cornerFlags |= corner.bitFlag();
                            }
                        }
                        
                        fjs = FaceJoinState.find(faceFlags, cornerFlags);
                    }
                }               
                joinStates[face.ordinal()] = fjs;
            }

            BlockJoinState bjs = BlockJoinState.find(joinStates);
        }
        Adversity.log.info("Array Count " + nextAvailableIndex);
        Adversity.log.info("Hash Count " + LOOKUP.size());
    }
    
    private BlockJoinState(long stateBits)
    {
        this.stateBits = stateBits;
        this.stateIndex = nextAvailableIndex++;
        DEFINED[stateIndex] = this;
        LOOKUP.put(stateBits, this);
    }
    
    public static BlockJoinState get(int index)
    {
        return DEFINED[index];
    }
    
    /** Values in array MUST be in same ordinal order as EnumFacing */
    public static BlockJoinState find(FaceJoinState[] fjs)
    {
        long bits = 0;
        
        for(EnumFacing face : EnumFacing.values())
        {
            bits |= (fjs[face.ordinal()].ordinal() << (6 * face.ordinal()));
        }
        
        BlockJoinState searchResult = LOOKUP.get(bits);
        if(searchResult == null)
        {
            searchResult = new BlockJoinState(bits);
            Adversity.log.info(fjs[0].name() + ", " + fjs[1].name() + ", " + fjs[2].name() + ", "
                    + fjs[3].name() + ", " + fjs[4].name() + ", " + fjs[5].name());
//            Adversity.log.info(fjs[0].ordinal() + ", " + fjs[1].ordinal() + ", " + fjs[2].ordinal() + ", "
//                    + fjs[3].ordinal() + ", " + fjs[4].ordinal() + ", " + fjs[5].ordinal());
        }
        
        return searchResult;
    }
    
    public FaceJoinState getFaceState(EnumFacing face)
    {
        return FaceJoinState.values()[(int)(stateBits >> (6 * face.ordinal())) & 63];
    }
    
}
