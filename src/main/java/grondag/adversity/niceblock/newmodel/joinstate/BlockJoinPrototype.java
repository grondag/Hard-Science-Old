//package grondag.adversity.niceblock.newmodel.joinstate;
//
//import java.util.ArrayList;
//
//import gnu.trove.map.hash.TLongIntHashMap;
//import gnu.trove.map.hash.TLongObjectHashMap;
//import grondag.adversity.Adversity;
//import grondag.adversity.library.NeighborBlocks.BlockCorner;
//import grondag.adversity.library.NeighborBlocks.FarCorner;
//import grondag.adversity.niceblock.newmodel.ModelReference.CornerJoin;
//import net.minecraft.util.EnumFacing;
//
//public class BlockJoinPrototype
//{
//
//    private final static BlockJoinPrototype[] DEFINED = new BlockJoinPrototype[60134];
//    private final static TLongObjectHashMap<BlockJoinPrototype> LOOKUP = new TLongObjectHashMap<BlockJoinPrototype>(60134);
//    private static int nextAvailableIndex = 0;
//    //600134
//    
//    private final long stateBits;
//    private final int stateIndex;
//
//    static
//    {
//        for(int i = 0; i < FarCorner.DOWN_SOUTH_WEST.bitFlag * 2; i++)
//        {
//            CornerJoin.Far modelJoin = new CornerJoin.Far(i);
//            boolean isDebug = false;
//            
//            FaceJoinState[] joinStates = new FaceJoinState[EnumFacing.values().length];
//            
//            for(EnumFacing face: EnumFacing.values())
//            {
//                int faceFlags = 0;
//                int cornerFlags = 0;
//                
//                FaceJoinState fjs;
//                
////                if(face == EnumFacing.EAST
////                        && modelJoin.isJoined(EnumFacing.DOWN)
////                        && modelJoin.isJoined(EnumFacing.UP)
////                        && modelJoin.isJoined(EnumFacing.NORTH)
////                        && modelJoin.isJoined(EnumFacing.WEST)
////                        && !modelJoin.isJoined(EnumFacing.EAST)
////                        && modelJoin.isJoined(EnumFacing.SOUTH)
////                        )
////                {
////                    Adversity.log.info("found one!");
////                    isDebug = true;
////                }
//                
//                     
//                
//                if(modelJoin.isJoined(face))
//                {
//                    fjs = FaceJoinState.NO_FACE;
//                }
//                else
//                {                   
//                    for(FaceSide fside : FaceSide.values())
//                    {
//                        EnumFacing joinFace = fside.getRelativeFace(face);
//                        BlockCorner joinCover = BlockCorner.find(face, joinFace);
//                        boolean sideJoin = modelJoin.isJoined(joinFace);
//                        boolean sideCover = modelJoin.isCornerPresent(joinCover);
//                        
//                        if(isDebug)
//                        {
//                            Adversity.log.info("sideJoin="+joinFace+": " + sideJoin +", sideCover=" + sideCover +":"+sideCover);
//                        }
//                        
//                        if(modelJoin.isJoined(joinFace) &&
//                                !modelJoin.isCornerPresent(joinCover))
//                        {
//                            faceFlags |= fside.bitFlag;
//                        }
//                    }
//                    
//                    if(isDebug)
//                    {
//                        Adversity.log.info("faceFlags=" + faceFlags);
//                    }
//                
//                    fjs = FaceJoinState.find(faceFlags, cornerFlags);
//                
//                    if(isDebug)
//                    {
//                        Adversity.log.info("first fjs="+fjs);
//                    }
//                    
//                    if(fjs.hasCornerTests())
//                    {
//                        if(isDebug)
//                        {
//                            Adversity.log.info("Starting corner tests");
//                        }
//                        
//                        for(FaceCorner corner : fjs.getCornerTests())
//                        {
//                            EnumFacing cornerFacing1 = corner.side1.getRelativeFace(face);
//                            EnumFacing cornerFacing2 = corner.side2.getRelativeFace(face);
//                            boolean cornerPresent = modelJoin.isCornerPresent(corner.side1.getRelativeFace(face), corner.side2.getRelativeFace(face));
//                            boolean cornerCover = modelJoin.isCornerPresent(corner.side1.getRelativeFace(face), corner.side2.getRelativeFace(face), face);
//                            
//                            if(isDebug)
//                            {
//                                Adversity.log.info("cornerFace1="+cornerFacing1+", cornerFace2=" + cornerFacing2);
//                                Adversity.log.info("cornerPresent="+cornerPresent+", cornerCovered=" + cornerCover);
//                            }
//                            
//                            if(!modelJoin.isCornerPresent(corner.side1.getRelativeFace(face), corner.side2.getRelativeFace(face))
//                                    || modelJoin.isCornerPresent(corner.side1.getRelativeFace(face), corner.side2.getRelativeFace(face), face))
//                            {
//                                cornerFlags |= corner.bitFlag;
//                            }
//                        }
//                        
//                        if(isDebug)
//                        {
//                            Adversity.log.info("conerFlags="+cornerFlags);
//                        }
//                        
//                        fjs = FaceJoinState.find(faceFlags, cornerFlags);
//                        
//                        
//                        if(isDebug)
//                        {
//                            Adversity.log.info("revised fjs="+fjs);
//                        }
//                    }
//                }               
//                joinStates[face.ordinal()] = fjs;
//            }
//
//            BlockJoinPrototype bjs = BlockJoinPrototype.find(joinStates);
//            if(isDebug)
//            {
//                Adversity.log.info("Ending block join state =" + bjs);
//            }
//        }
//        Adversity.log.info("Array Count " + nextAvailableIndex);
//        Adversity.log.info("Hash Count " + LOOKUP.size());
//    }
//    
//    private BlockJoinPrototype(long stateBits)
//    {
//        this.stateBits = stateBits;
//        this.stateIndex = nextAvailableIndex++;
//        DEFINED[stateIndex] = this;
//        LOOKUP.put(stateBits, this);
//    }
//    
//    public static BlockJoinPrototype get(int index)
//    {
//        return DEFINED[index];
//    }
//    
//    /** Values in array MUST be in same ordinal order as EnumFacing */
//    public static BlockJoinPrototype find(FaceJoinState[] fjs)
//    {
//        long bits = 0;
//        
//        for(EnumFacing face : EnumFacing.values())
//        {
//            bits |= ((long)fjs[face.ordinal()].ordinal() << (6 * face.ordinal()));
//        }
//        
//        BlockJoinPrototype searchResult = LOOKUP.get(bits);
//        if(searchResult == null)
//        {
//            searchResult = new BlockJoinPrototype(bits);
//            Adversity.log.info(fjs[0].name() + ", " + fjs[1].name() + ", " + fjs[2].name() + ", "
//                    + fjs[3].name() + ", " + fjs[4].name() + ", " + fjs[5].name());
////            Adversity.log.info(fjs[0].ordinal() + ", " + fjs[1].ordinal() + ", " + fjs[2].ordinal() + ", "
////                    + fjs[3].ordinal() + ", " + fjs[4].ordinal() + ", " + fjs[5].ordinal());
//        }
//        
//        return searchResult;
//    }
//    
//    public FaceJoinState getFaceState(EnumFacing face)
//    {
//        return FaceJoinState.values()[(int)(stateBits >> (6 * face.ordinal())) & 63];
//    }
//    
//}
