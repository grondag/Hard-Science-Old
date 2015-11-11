package grondag.adversity.library;

import java.util.BitSet;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

// import gnu.trove.set.hash.TIntHashSet;

public class ShapeValidatorCubic {
	
	//TIntHashSet visited;
	BitSet visited;
	

	//dimension of valid shape
	// members sorted such that x >= y >= z;
	private BlockPos validShape;
	
	// magnitude of longest dimension
	// redundant of validShape.getX()
	// but use for brevity and clarity
	private int maxOffset;
	
	private BlockPos origin;

	private BlockPos maxPos;
	private BlockPos minPos;
	private BlockPos length;

	
	public ShapeValidatorCubic( int i, int j, int k){
		
		validShape = Useful.sortedBlockPos(new BlockPos(i, j, k));
		
		// we don't know which dimension will have the max value
		maxOffset = validShape.getX();
	}
	
	public boolean isValidShape(IBlockAccess worldIn, BlockPos origin, IBlockTest test, boolean assumeStartValid){

		if(!assumeStartValid){
			if(!test.testBlock(worldIn.getBlockState(origin))){
				return false;
			}
		}

		maxPos = origin;
		minPos = origin;
		this.origin = origin;
		
		// have origin block at this point
		length = new BlockPos(1, 1, 1);
		
		if(visited == null) {
			// bitset has to support max range in any dimension, thus multiply by 2^3
			visited = new BitSet( maxOffset * maxOffset * maxOffset * 8);
		} else {
			visited.clear();
		}
		
		setVisited(origin);
		
		visit(worldIn, origin.up(),test);
		visit(worldIn, origin.down(),test);
		visit(worldIn, origin.east(),test);
		visit(worldIn, origin.west(),test);
		visit(worldIn, origin.north(),test);
		visit(worldIn, origin.south(),test);
		
		return isValid();
	}
	
	private void visit(IBlockAccess worldIn, BlockPos pos, IBlockTest test){
		
		if(getVisited(pos)) {
			return;
		} else if(Math.abs(pos.getX()) > maxOffset || Math.abs(pos.getY()) > maxOffset || Math.abs(pos.getZ()) > maxOffset){
			return;
		} else {
			setVisited(pos);
		}
		
		if(test.testBlock(worldIn.getBlockState(pos))) {
			updateMeasurements(pos);
			
			visit(worldIn, pos.up(),test);
			visit(worldIn, pos.down(),test);
			visit(worldIn, pos.east(),test);
			visit(worldIn, pos.west(),test);
			visit(worldIn, pos.north(),test);
			visit(worldIn, pos.south(),test);
		}
		
	}
	
	private void updateMeasurements(BlockPos pos){
		
		minPos = new BlockPos(Math.min(minPos.getX(), pos.getX()), Math.min(minPos.getY(), pos.getY()), Math.min(minPos.getZ(), pos.getZ()));
		maxPos = new BlockPos(Math.max(maxPos.getX(), pos.getX()), Math.max(maxPos.getY(), pos.getY()), Math.max(maxPos.getZ(), pos.getZ()));		
		length = maxPos.subtract(minPos).add(1, 1, 1);

	}
	
	private boolean isValid(){
		
		BlockPos clearances = validShape.subtract(Useful.sortedBlockPos(maxPos.subtract(minPos).add(1, 1, 1)));
		return clearances.getX() >= 0 && clearances.getY() >= 0 && clearances.getZ() >=0;
	}
	
	private void setVisited(BlockPos pos){
		
		visited.set(getBitIndex(origin.subtract(pos)));

	}
	
	private boolean getVisited(BlockPos pos){
		return visited.get(getBitIndex(origin.subtract(pos)));	
	}
	
	private int getBitIndex(BlockPos pos){	
		// Add max offset to handle negative values.  Bitset width is double max offset for this purpose.
		return (pos.getX() + maxOffset) * maxOffset * maxOffset + (pos.getY()+ maxOffset) * maxOffset + pos.getZ() + + maxOffset;
	}
}
