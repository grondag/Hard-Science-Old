package grondag.adversity.library;

//import java.util.BitSet;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

import gnu.trove.set.hash.TIntHashSet;

public class ShapeValidatorCubic {
	
	//TIntHashSet visited;
	TIntHashSet visited;
	

	//dimension of valid shape
	// members sorted such that x >= y >= z;
	private BlockPos validShape;
	
	// magnitude of longest dimension
	// redundant of validShape.getX()
	// but use for brevity and clarity
	private int maxOffset;
	
	private BlockPos origin;
	private BlockPos hashOrigin;

	private BlockPos maxPos;
	private BlockPos minPos;
	private BlockPos length;

	
	public ShapeValidatorCubic( int i, int j, int k){
		
		validShape = Useful.sortedBlockPos(new BlockPos(Math.min(i, 255), Math.min(j, 255), Math.min(j, 255)));
		
		// we don't know which dimension will have the max value
		maxOffset = validShape.getX();
		
		visited = new TIntHashSet((i+2) * (j+2) * (k+2));
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
		
		// doing this ensures positive numbers for PosHash function
		hashOrigin = origin.subtract(new BlockPos(256,256, 256));
		
		visited.clear();
		
		setVisited(origin);
		
		updateMeasurements(origin);
		
		visit(worldIn, origin.up(),test);
		visit(worldIn, origin.down(),test);
		visit(worldIn, origin.east(),test);
		visit(worldIn, origin.west(),test);
		visit(worldIn, origin.north(),test);
		visit(worldIn, origin.south(),test);
		
		return isValid();
	}
	
	private void visit(IBlockAccess worldIn, BlockPos pos, IBlockTest test){
		
		if(!isReachable(pos) || !setVisited(pos)) {
			return;
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
	
	
	private boolean isReachable(BlockPos pos){
		BlockPos dist = pos.subtract(origin);
		return !(Math.abs(dist.getX()) > maxOffset || Math.abs(dist.getY()) > maxOffset || Math.abs(dist.getZ()) > maxOffset);
	}
	
	private boolean isValid(){
		
		BlockPos clearances = validShape.subtract(Useful.sortedBlockPos(maxPos.subtract(minPos).add(1, 1, 1)));
		return clearances.getX() >= 0 && clearances.getY() >= 0 && clearances.getZ() >=0;
	}
	
	// returns true if key was not in the set
	private boolean setVisited(BlockPos pos){
		
		return visited.add(getPosHash(pos.subtract(hashOrigin)));

	}
	
	private boolean getVisited(BlockPos pos){
		return visited.contains(getPosHash(pos.subtract(hashOrigin)));	
	}
	
	private int getPosHash(BlockPos pos){	
		// Add max offset to handle negative values.  Bitset width is double max offset for this purpose.
		return (pos.getX() & 0x3FF) | ((pos.getY() & 0x3FF) << 10) | ((pos.getZ() & 0x3FF) << 20); 
	}
}
