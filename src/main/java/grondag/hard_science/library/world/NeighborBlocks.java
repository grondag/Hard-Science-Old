package grondag.hard_science.library.world;

import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
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
                modelStates[face.ordinal()] = ((SuperBlock)block).getModelStateAssumeStateIsCurrent(state, this.world, pos.add(face.getDirectionVec()), this.refreshModelStateFromWorld);
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
                modelStates[corner.superOrdinal] = ((SuperBlock)block).getModelStateAssumeStateIsCurrent(state, this.world, pos.add(corner.directionVector), this.refreshModelStateFromWorld);
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
                modelStates[corner.superOrdinal] = ((SuperBlock)block).getModelStateAssumeStateIsCurrent(state, this.world, pos.add(corner.directionVector), this.refreshModelStateFromWorld);
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
                return test.testBlock(face, world, getBlockState(face), pos.add(face.getDirectionVec()), getModelState(face));
            }
            else
            {
                return test.testBlock(face, world, getBlockState(face), pos.add(face.getDirectionVec()));
            }
        }
        
        private boolean doTest(BlockCorner corner)
        {
            if(test.wantsModelState())
            {
                return test.testBlock(corner, world, getBlockState(corner), pos.add(corner.directionVector), getModelState(corner));
            }
            else
            {
                return test.testBlock(corner, world, getBlockState(corner), pos.add(corner.directionVector));
            }
        }
        
        private boolean doTest(FarCorner corner)
        {
            if(test.wantsModelState())
            {
                return test.testBlock(corner, world, getBlockState(corner), pos.add(corner.directionVector), getModelState(corner));
            }
            else
            {
                return test.testBlock(corner, world, getBlockState(corner), pos.add(corner.directionVector));
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
}
