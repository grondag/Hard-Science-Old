package grondag.adversity.library;

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

	private IBlockState blockStates[] = new IBlockState[EnumFacing.values().length];
    private IBlockState cornerStates[] = new IBlockState[BlockCorner.values().length];
    private IBlockState farCornerStates[] = new IBlockState[FarCorner.values().length];
	
	private final IBlockAccess world;
	private final BlockPos pos;

	/**
	 * Gathers blockstates for adjacent positions as needed.
	 */
	public NeighborBlocks(IBlockAccess worldIn, BlockPos pos) {
		this.world = worldIn;
		this.pos = pos;
	}

	public IBlockState getBlockState(EnumFacing face)
	{
	    if(blockStates[face.ordinal()] == null)
	    {
	        blockStates[face.ordinal()] = world.getBlockState(pos.add(face.getDirectionVec()));
	    }
	    return blockStates[face.ordinal()];
 	}
	
	public IBlockState getBlockState(EnumFacing face1, EnumFacing face2)
	{
	    BlockCorner corner = BlockCorner.find(face1, face2);
	    return getBlockState(corner);
	}

   public IBlockState getBlockState(BlockCorner corner)
    {
        if(cornerStates[corner.ordinal()] == null)
        {
            cornerStates[corner.ordinal()] = world.getBlockState(pos.add(corner.getDirectionVec()));
        }
        return cornerStates[corner.ordinal()];
    }
   
   public IBlockState getBlockState(EnumFacing face1, EnumFacing face2, EnumFacing face3)
   {
       FarCorner corner = FarCorner.find(face1, face2, face3);
       return getBlockState(corner);
   }

  public IBlockState getBlockState(FarCorner corner)
   {
       if(farCornerStates[corner.ordinal()] == null)
       {
           farCornerStates[corner.ordinal()] = world.getBlockState(pos.add(corner.getDirectionVec()));
       }
       return farCornerStates[corner.ordinal()];
   }
  
	/**
	 * Apply given test to neighboring block states.
	 */
	public NeighborTestResults getNeighborTestResults(IBlockTest test) {
		return new NeighborTestResults(test);
	}

	/**
	 * Convenient data structure for returning test results.
	 */
	public class NeighborTestResults {

		private int completionFlags = 0;
		private int resultFlags = 0;
		private final IBlockTest test;

		protected NeighborTestResults(IBlockTest test) {
			this.test = test;
		}
		
		public boolean result(EnumFacing face)
		{
		    int bitFlag = FACE_FLAGS[face.ordinal()];
		    if((completionFlags & bitFlag) != bitFlag) {
		        if(test.testBlock(world, NeighborBlocks.this.getBlockState(face), pos.add(face.getDirectionVec())))
		        {
		            resultFlags |= bitFlag;
		        }
		        completionFlags |= bitFlag;
		    }
		    return (resultFlags & bitFlag) == bitFlag;
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
        
        public boolean result(BlockCorner corner)
        {
            if((completionFlags & corner.bitFlag) != corner.bitFlag) {
                if(test.testBlock(world, NeighborBlocks.this.getBlockState(corner), pos.add(corner.getDirectionVec())))
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
                if(test.testBlock(world, NeighborBlocks.this.getBlockState(corner), pos.add(corner.getDirectionVec())))
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
        
        private BlockCorner(EnumFacing face1, EnumFacing face2)
        {
            this.face1 = face1;
            this.face2 = face2;
            this.bitFlag = 1 << (FACE_FLAGS.length + this.ordinal());
            boolean hasX = (face1.getAxis() == EnumFacing.Axis.X || face2.getAxis() == EnumFacing.Axis.X);
            boolean hasY = (face1.getAxis() == EnumFacing.Axis.Y || face2.getAxis() == EnumFacing.Axis.Y);
            this.axis = hasX && hasY ? EnumFacing.Axis.Z : hasX ? EnumFacing.Axis.Y : EnumFacing.Axis.X;
            
            CORNER_LOOKUP[face1.ordinal()][face2.ordinal()] = this;
            CORNER_LOOKUP[face2.ordinal()][face1.ordinal()] = this;
        }
        
        public Vec3i getDirectionVec()
        {
            Vec3i v1 = face1.getDirectionVec();
            Vec3i v2 = face2.getDirectionVec();
            return new Vec3i(v1.getX() + v2.getX(), v1.getY() + v2.getY(), v1.getZ() + v2.getZ());
        }
        
        public static BlockCorner find(EnumFacing face1, EnumFacing face2)
        {
            return CORNER_LOOKUP[face1.ordinal()][face2.ordinal()];
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
        
        private FarCorner(EnumFacing face1, EnumFacing face2, EnumFacing face3)
        {
            this.face1 = face1;
            this.face2 = face2;
            this.face3 = face3;
            this.bitFlag = 1 << (FACE_FLAGS.length + BlockCorner.values().length + this.ordinal());
            
            FAR_CORNER_LOOKUP[face1.ordinal()][face2.ordinal()][face3.ordinal()] = this;
            FAR_CORNER_LOOKUP[face1.ordinal()][face3.ordinal()][face2.ordinal()] = this;
            FAR_CORNER_LOOKUP[face2.ordinal()][face1.ordinal()][face3.ordinal()] = this;
            FAR_CORNER_LOOKUP[face2.ordinal()][face3.ordinal()][face1.ordinal()] = this;
            FAR_CORNER_LOOKUP[face3.ordinal()][face2.ordinal()][face1.ordinal()] = this;
            FAR_CORNER_LOOKUP[face3.ordinal()][face1.ordinal()][face2.ordinal()] = this;
        }
        
        public Vec3i getDirectionVec()
        {
            Vec3i v1 = face1.getDirectionVec();
            Vec3i v2 = face2.getDirectionVec();
            Vec3i v3 = face3.getDirectionVec();
            return new Vec3i(v1.getX() + v2.getX() + v3.getX(), v1.getY() + v2.getY() + v3.getY(), v1.getZ() + v2.getZ() + v3.getZ());
        }
        
        public static FarCorner find(EnumFacing face1, EnumFacing face2, EnumFacing face3)
        {
            return FAR_CORNER_LOOKUP[face1.ordinal()][face2.ordinal()][face3.ordinal()];
        }
    }
}
