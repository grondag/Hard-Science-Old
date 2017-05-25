package grondag.adversity.library;

import grondag.adversity.superblock.block.SuperBlock;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;

/**
 * Convenient way to gather and test block states
 * for blocks adjacent to a given position.
 * Position is immutable, blockstates are looked up lazily 
 * and values are cached for reuse.
 */
public class NeighborBlocks {

    public final static int[] FACE_FLAGS = new int[]{1, 2, 4, 8, 16, 32};

    // EnumFacing.values().length + BlockCorner.values().length + FarCorner.values().length = 6 + 
    private final static int STATE_COUNT = 6 + 12 + 8;
    
    private IBlockState blockStates[] = new IBlockState[STATE_COUNT];
    private ModelState modelStates[] = new ModelState[STATE_COUNT];


    private final IBlockAccess world;
    private final BlockPos pos;
    private final boolean refreshModelStateFromWorld;

    /**
     * Gathers blockstates for adjacent positions as needed.
     */
    public NeighborBlocks(IBlockAccess worldIn, BlockPos pos) 
    {
        this(worldIn, pos, false);
    }

    public NeighborBlocks(IBlockAccess worldIn, BlockPos pos, boolean refreshModelStateFromWorld) 
    {
        this.world = worldIn;
        this.pos = pos;
        this.refreshModelStateFromWorld = refreshModelStateFromWorld;
    }
    
    //////////////////////////////
    // BLOCK STATE
    //////////////////////////////
    public IBlockState getBlockState(EnumFacing face)
    {
        if(blockStates[face.ordinal()] == null)
        {
            blockStates[face.ordinal()] = world.getBlockState(pos.add(face.getDirectionVec()));
        }
        return blockStates[face.ordinal()];
    }

    public IBlockState getBlockState(HorizontalFace face)
    {
        return getBlockState(face.face);
    }

    public IBlockState getBlockStateUp(HorizontalFace face)
    {
        return getBlockState(face.face, EnumFacing.UP);
    }

    public IBlockState getBlockStateDown(HorizontalFace face)
    {
        return getBlockState(face.face, EnumFacing.DOWN);
    }
    public IBlockState getBlockState(EnumFacing face1, EnumFacing face2)
    {
        BlockCorner corner = BlockCorner.find(face1, face2);
        return getBlockState(corner);
    }

    public IBlockState getBlockState(HorizontalCorner corner)
    {
        return getBlockState(corner.face1.face, corner.face2.face);
    }

    public IBlockState getBlockStateUp(HorizontalCorner corner)
    {
        return getBlockState(corner.face1.face, corner.face2.face, EnumFacing.UP);
    }

    public IBlockState getBlockStateDown(HorizontalCorner corner)
    {
        return getBlockState(corner.face1.face, corner.face2.face, EnumFacing.DOWN);
    }
    public IBlockState getBlockState(BlockCorner corner)
    {
        if(blockStates[corner.superOrdinal] == null)
        {
            blockStates[corner.superOrdinal] = world.getBlockState(pos.add(corner.directionVector));
        }
        return blockStates[corner.superOrdinal];
    }

    public IBlockState getBlockState(EnumFacing face1, EnumFacing face2, EnumFacing face3)
    {
        FarCorner corner = FarCorner.find(face1, face2, face3);
        return getBlockState(corner);
    }

    public IBlockState getBlockState(FarCorner corner)
    {
        if(blockStates[corner.superOrdinal] == null)
        {
            blockStates[corner.superOrdinal] = world.getBlockState(pos.add(corner.directionVector));
        }
        return blockStates[corner.superOrdinal];
    }
    
    //////////////////////////////
    // MODEL STATE
    //////////////////////////////
    public ModelState getModelState(EnumFacing face)
    {
        if(modelStates[face.ordinal()] == null)
        {
            IBlockState state = this.getBlockState(face);
            Block block = state.getBlock();
            if(block instanceof SuperBlock)
            {
                modelStates[face.ordinal()] = ((SuperBlock)block).getModelState(state, this.world, pos.add(face.getDirectionVec()), this.refreshModelStateFromWorld);
            }
        }
        return modelStates[face.ordinal()];
    }

    public ModelState getModelState(HorizontalFace face)
    {
        return getModelState(face.face);
    }

    public ModelState getModelStateUp(HorizontalFace face)
    {
        return getModelState(face.face, EnumFacing.UP);
    }

    public ModelState getModelStateDown(HorizontalFace face)
    {
        return getModelState(face.face, EnumFacing.DOWN);
    }
    public ModelState getModelState(EnumFacing face1, EnumFacing face2)
    {
        BlockCorner corner = BlockCorner.find(face1, face2);
        return getModelState(corner);
    }

    public ModelState getModelState(HorizontalCorner corner)
    {
        return getModelState(corner.face1.face, corner.face2.face);
    }

    public ModelState getModelStateUp(HorizontalCorner corner)
    {
        return getModelState(corner.face1.face, corner.face2.face, EnumFacing.UP);
    }

    public ModelState getModelStateDown(HorizontalCorner corner)
    {
        return getModelState(corner.face1.face, corner.face2.face, EnumFacing.DOWN);
    }
    public ModelState getModelState(BlockCorner corner)
    {
        if(modelStates[corner.superOrdinal] == null)
        {
            IBlockState state = this.getBlockState(corner);
            Block block = state.getBlock();
            if(block instanceof SuperBlock)
            {
                modelStates[corner.superOrdinal] = ((SuperBlock)block).getModelState(state, this.world, pos.add(corner.directionVector), this.refreshModelStateFromWorld);
            }
        }
        return modelStates[corner.superOrdinal];
    }

    public ModelState getModelState(EnumFacing face1, EnumFacing face2, EnumFacing face3)
    {
        FarCorner corner = FarCorner.find(face1, face2, face3);
        return getModelState(corner);
    }

    public ModelState getModelState(FarCorner corner)
    {
        if(modelStates[corner.superOrdinal] == null)
        {
            IBlockState state = this.getBlockState(corner);
            Block block = state.getBlock();
            if(block instanceof SuperBlock)
            {
                modelStates[corner.superOrdinal] = ((SuperBlock)block).getModelState(state, this.world, pos.add(corner.directionVector), this.refreshModelStateFromWorld);
            }
        }
        return modelStates[corner.superOrdinal];
    }

    //////////////////////////////
    // TESTS AND OTHER STUFF
    //////////////////////////////
    
    /**
     * Apply given test to neighboring block states.
     */
    public NeighborTestResults getNeighborTestResults(IBlockTest test) {
        return new NeighborTestResults(test);
    }

    /**
     * For testing
     */
    public NeighborTestResults getFakeNeighborTestResults(int faceFlags) {
        return new NeighborTestResults(faceFlags);
    }
    
    /**
     * Convenient data structure for returning test results.
     */
    public class NeighborTestResults {

        private int completionFlags = 0;
        private int resultFlags = 0;
        private final IBlockTest test;

        private NeighborTestResults(IBlockTest test) {
            this.test = test;
        }

        // for testing
        private NeighborTestResults(int faceFlags)
        {
            this.test = null;
            this.resultFlags = faceFlags;
            this.completionFlags = Useful.intBitMask(26);
        }
        
        private boolean doTest(EnumFacing face)
        {
            if(test.wantsModelState())
            {
                return test.testBlock(world, getBlockState(face), pos.add(face.getDirectionVec()), getModelState(face));
            }
            else
            {
                return test.testBlock(world, getBlockState(face), pos.add(face.getDirectionVec()));
            }
        }
        
        private boolean doTest(BlockCorner corner)
        {
            if(test.wantsModelState())
            {
                return test.testBlock(world, getBlockState(corner), pos.add(corner.directionVector), getModelState(corner));
            }
            else
            {
                return test.testBlock(world, getBlockState(corner), pos.add(corner.directionVector));
            }
        }
        
        private boolean doTest(FarCorner corner)
        {
            if(test.wantsModelState())
            {
                return test.testBlock(world, getBlockState(corner), pos.add(corner.directionVector), getModelState(corner));
            }
            else
            {
                return test.testBlock(world, getBlockState(corner), pos.add(corner.directionVector));
            }
        }
        
        public boolean result(EnumFacing face)
        {
            int bitFlag = FACE_FLAGS[face.ordinal()];
            if((completionFlags & bitFlag) != bitFlag) {
                if(doTest(face))
                {
                    resultFlags |= bitFlag;
                }
                completionFlags |= bitFlag;
            }
            return (resultFlags & bitFlag) == bitFlag;
        }
        
        /** use this to override world results */
        public void override(EnumFacing face, boolean override)
        {
            int bitFlag = FACE_FLAGS[face.ordinal()];
            completionFlags |= bitFlag;
            if(override)
            {
                resultFlags |= bitFlag;
            }
            else
            {
                resultFlags &= ~bitFlag;
            }
        }

        public boolean result(HorizontalFace face)
        {
            return result(face.face);
        }

        public boolean resultUp(HorizontalFace face)
        {
            return result(face.face, EnumFacing.UP);
        }

        public boolean resultDown(HorizontalFace face)
        {
            return result(face.face, EnumFacing.DOWN);
        }

        /** convenience method */
        public int resultBit(EnumFacing face){
            return  result(face) ? 1 : 0;
        }

        public boolean result(EnumFacing face1, EnumFacing face2)
        {
            BlockCorner corner = BlockCorner.find(face1, face2);
            return result(corner);
        }

        public boolean result(HorizontalCorner corner)
        {
            return result(corner.face1.face, corner.face2.face);
        }

        public boolean resultUp(HorizontalCorner corner)
        {
            return result(corner.face1.face, corner.face2.face, EnumFacing.UP);
        }

        public boolean resultDown(HorizontalCorner corner)
        {
            return result(corner.face1.face, corner.face2.face, EnumFacing.DOWN);
        }

        public boolean result(BlockCorner corner)
        {
            if((completionFlags & corner.bitFlag) != corner.bitFlag) {
                if(doTest(corner))
                {
                    resultFlags |= corner.bitFlag;
                }
                completionFlags |= corner.bitFlag;
            }
            return (resultFlags & corner.bitFlag) == corner.bitFlag;
        }

        public int resultBit(EnumFacing face1, EnumFacing face2)
        {
            return  result(face1, face2) ? 1 : 0;
        }

        public int resultBit(BlockCorner corner)
        {
            return  result(corner) ? 1 : 0;
        }

        public boolean result(EnumFacing face1, EnumFacing face2, EnumFacing face3)
        {
            FarCorner corner = FarCorner.find(face1, face2, face3);
            return result(corner);
        }

        public boolean result(FarCorner corner)
        {
            if((completionFlags & corner.bitFlag) != corner.bitFlag) {
                if(doTest(corner))
                {
                    resultFlags |= corner.bitFlag;
                }
                completionFlags |= corner.bitFlag;
            }
            return (resultFlags & corner.bitFlag) == corner.bitFlag;
        }

        public int resultBit(EnumFacing face1, EnumFacing face2, EnumFacing face3)
        {
            return  result(face1, face2, face3) ? 1 : 0;
        }

        public int resultBit(FarCorner corner)
        {
            return  result(corner) ? 1 : 0;
        }
        
        public String toString()
        {
            String retval = "";
            
            for(EnumFacing face : EnumFacing.values())
            {
                retval += face.toString() + "=" + this.result(face) + " ";
            }

            for(BlockCorner corner : BlockCorner.values())
            {
                retval += corner.toString() + "=" + this.result(corner) + " ";
            }
            
            for(FarCorner corner : FarCorner.values())
            {
                retval += corner.toString() + "=" + this.result(corner) + " ";
            }
            return retval;
        }
    }

    private static BlockCorner[][] CORNER_LOOKUP = new BlockCorner[6][6];

    public static enum BlockCorner
    {
        UP_EAST(EnumFacing.UP, EnumFacing.EAST),
        UP_WEST(EnumFacing.UP, EnumFacing.WEST),
        UP_NORTH(EnumFacing.UP, EnumFacing.NORTH),
        UP_SOUTH(EnumFacing.UP, EnumFacing.SOUTH),
        NORTH_EAST(EnumFacing.NORTH, EnumFacing.EAST),
        NORTH_WEST(EnumFacing.NORTH, EnumFacing.WEST),
        SOUTH_EAST(EnumFacing.SOUTH, EnumFacing.EAST),
        SOUTH_WEST(EnumFacing.SOUTH, EnumFacing.WEST),
        DOWN_EAST(EnumFacing.DOWN, EnumFacing.EAST),
        DOWN_WEST(EnumFacing.DOWN, EnumFacing.WEST),
        DOWN_NORTH(EnumFacing.DOWN, EnumFacing.NORTH),
        DOWN_SOUTH(EnumFacing.DOWN, EnumFacing.SOUTH);

        public final EnumFacing face1;
        public final EnumFacing face2;
        public final EnumFacing.Axis axis;
        public final int bitFlag;
        public final Vec3i directionVector;
        /** 
         * Ordinal sequence that includes all faces, corner and far corners.
         * Use to index them in a mixed array.
         */
        public final int superOrdinal;

        private BlockCorner(EnumFacing face1, EnumFacing face2)
        {
            this.face1 = face1;
            this.face2 = face2;
            this.bitFlag = 1 << (FACE_FLAGS.length + this.ordinal());
            this.superOrdinal = EnumFacing.values().length + this.ordinal();
            boolean hasX = (face1.getAxis() == EnumFacing.Axis.X || face2.getAxis() == EnumFacing.Axis.X);
            boolean hasY = (face1.getAxis() == EnumFacing.Axis.Y || face2.getAxis() == EnumFacing.Axis.Y);
            this.axis = hasX && hasY ? EnumFacing.Axis.Z : hasX ? EnumFacing.Axis.Y : EnumFacing.Axis.X;

            CORNER_LOOKUP[face1.ordinal()][face2.ordinal()] = this;
            CORNER_LOOKUP[face2.ordinal()][face1.ordinal()] = this;

            Vec3i v1 = face1.getDirectionVec();
            Vec3i v2 = face2.getDirectionVec();
            this.directionVector = new Vec3i(v1.getX() + v2.getX(), v1.getY() + v2.getY(), v1.getZ() + v2.getZ());

        }

        public static BlockCorner find(EnumFacing face1, EnumFacing face2)
        {
            return CORNER_LOOKUP[face1.ordinal()][face2.ordinal()];
        }
    }

    private static HorizontalCorner[][] HORIZONTAL_CORNER_LOOKUP = new HorizontalCorner[4][4];

    public static enum HorizontalCorner
    {
        NORTH_EAST(HorizontalFace.NORTH, HorizontalFace.EAST),
        NORTH_WEST(HorizontalFace.NORTH, HorizontalFace.WEST),
        SOUTH_EAST(HorizontalFace.SOUTH, HorizontalFace.EAST),
        SOUTH_WEST(HorizontalFace.SOUTH, HorizontalFace.WEST);

        public final HorizontalFace face1;
        public final HorizontalFace face2;

        public final Vec3i directionVector;

        private HorizontalCorner(HorizontalFace face1, HorizontalFace face2)
        {
            this.face1 = face1;
            this.face2 = face2;
            this.directionVector = new Vec3i(face1.face.getDirectionVec().getX() + face2.face.getDirectionVec().getX(), 0, face1.face.getDirectionVec().getZ() + face2.face.getDirectionVec().getZ());
            HORIZONTAL_CORNER_LOOKUP[face1.ordinal()][face2.ordinal()] = this;
            HORIZONTAL_CORNER_LOOKUP[face2.ordinal()][face1.ordinal()] = this;
        }

        public static HorizontalCorner find(HorizontalFace face1, HorizontalFace face2)
        {
            return HORIZONTAL_CORNER_LOOKUP[face1.ordinal()][face2.ordinal()];
        }

    }

    private static HorizontalFace HORIZONTAL_FACE_LOOKUP[] = new HorizontalFace[6];
    
    public static enum HorizontalFace
    {
        NORTH(EnumFacing.NORTH),
        EAST(EnumFacing.EAST),
        SOUTH(EnumFacing.SOUTH),
        WEST(EnumFacing.WEST);

        public final EnumFacing face;

        public final Vec3i directionVector;

        private HorizontalFace(EnumFacing face)
        {
            this.face = face;
            HORIZONTAL_FACE_LOOKUP[face.ordinal()] = this;

            this.directionVector = face.getDirectionVec();
        }
        
        public static HorizontalFace find(EnumFacing face)
        {
            return HORIZONTAL_FACE_LOOKUP[face.ordinal()];
        }
        
        public HorizontalFace getLeft()
        {
            if(this.ordinal() == 0)
            {
                return HorizontalFace.values()[3];
            }
            else
            {
                return HorizontalFace.values()[this.ordinal()-1];
            }
        }
        
        public HorizontalFace getRight()
        {
            if(this.ordinal() == 3)
            {
                return HorizontalFace.values()[0];
            }
            else
            {
                return HorizontalFace.values()[this.ordinal()+1];
            }
        }

    }
    private static FarCorner[][][] FAR_CORNER_LOOKUP = new FarCorner[6][6][6];

    public static enum FarCorner
    {
        UP_NORTH_EAST(EnumFacing.UP, EnumFacing.EAST, EnumFacing.NORTH),
        UP_NORTH_WEST(EnumFacing.UP, EnumFacing.WEST, EnumFacing.NORTH),
        UP_SOUTH_EAST(EnumFacing.UP, EnumFacing.EAST, EnumFacing.SOUTH),
        UP_SOUTH_WEST(EnumFacing.UP, EnumFacing.WEST, EnumFacing.SOUTH),
        DOWN_NORTH_EAST(EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.NORTH),
        DOWN_NORTH_WEST(EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.NORTH),
        DOWN_SOUTH_EAST(EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.SOUTH),
        DOWN_SOUTH_WEST(EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.SOUTH);


        public final EnumFacing face1;
        public final EnumFacing face2;
        public final EnumFacing face3;
        public final int bitFlag;
        public final Vec3i directionVector;
        /** 
         * Ordinal sequence that includes all faces, corner and far corners.
         * Use to index them in a mixed array.
         */
        public final int superOrdinal;

        private FarCorner(EnumFacing face1, EnumFacing face2, EnumFacing face3)
        {
            this.face1 = face1;
            this.face2 = face2;
            this.face3 = face3;
            this.bitFlag = 1 << (FACE_FLAGS.length + BlockCorner.values().length + this.ordinal());
            this.superOrdinal = this.ordinal() + EnumFacing.values().length + BlockCorner.values().length;

            FAR_CORNER_LOOKUP[face1.ordinal()][face2.ordinal()][face3.ordinal()] = this;
            FAR_CORNER_LOOKUP[face1.ordinal()][face3.ordinal()][face2.ordinal()] = this;
            FAR_CORNER_LOOKUP[face2.ordinal()][face1.ordinal()][face3.ordinal()] = this;
            FAR_CORNER_LOOKUP[face2.ordinal()][face3.ordinal()][face1.ordinal()] = this;
            FAR_CORNER_LOOKUP[face3.ordinal()][face2.ordinal()][face1.ordinal()] = this;
            FAR_CORNER_LOOKUP[face3.ordinal()][face1.ordinal()][face2.ordinal()] = this;

            Vec3i v1 = face1.getDirectionVec();
            Vec3i v2 = face2.getDirectionVec();
            Vec3i v3 = face3.getDirectionVec();
            this.directionVector = new Vec3i(v1.getX() + v2.getX() + v3.getX(), v1.getY() + v2.getY() + v3.getY(), v1.getZ() + v2.getZ() + v3.getZ());

        }

        public static FarCorner find(EnumFacing face1, EnumFacing face2, EnumFacing face3)
        {
            return FAR_CORNER_LOOKUP[face1.ordinal()][face2.ordinal()][face3.ordinal()];
        }
    }
}
