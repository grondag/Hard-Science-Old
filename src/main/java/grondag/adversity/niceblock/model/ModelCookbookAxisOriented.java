package grondag.adversity.niceblock.model;

import java.util.ArrayList;
import java.util.List;

import grondag.adversity.Adversity;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.niceblock.NiceBlock;
import grondag.adversity.niceblock.NiceBlock.TestForStyle;
import grondag.adversity.niceblock.support.ICollisionHandler;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;

import org.lwjgl.util.vector.Vector3f;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;

public abstract class ModelCookbookAxisOriented extends ModelCookbook implements ICollisionHandler{
	
	protected String[] modelNames = new String[AxisAlignedModel.values().length];
	public final ImmutableList<AxisAlignedBB>[] MODEL_BOUNDS = new ImmutableList[64];
	public final AxisAlignedBB[] COMBINED_BOUNDS = new AxisAlignedBB[64];
	
	protected final  Integer[][][][][][] RECIPE_LOOKUP = new Integer[2][2][2][2][2][2];
	protected final TRSRTransformation[] ROTATION_LOOKUP;
	protected final Vec3[] ROTATION_LOOKUP_Y = {
			new Vec3(0.0, 90.0, 0.0 ), new Vec3(0.0, 270.0, 0.0 ), new Vec3(0.0, 0.0, 0.0 ), new Vec3(0.0, 180.0, 0.0 ), 
			new Vec3(0.0, 0.0, 0.0 ), new Vec3(0.0, 270.0, 0.0 ), new Vec3(0.0, 90.0, 0.0 ), new Vec3(0.0, 180.0, 0.0 ), 
			new Vec3(0.0,0.0, 0.0 ), new Vec3(0.0, 90.0, 0.0 ), new Vec3(0.0, 0.0, 0.0 ), new Vec3(0.0, 180.0, 0.0 ), 
			new Vec3(0.0, 90.0, 0.0 ), new Vec3(0.0, 270.0, 0.0 ), new Vec3(0.0, 0.0, 0.0 ), new Vec3(0.0, 0.0, 0.0 ), 
			new Vec3(0.0, 90.0, 0.0 ), new Vec3(0.0, 270.0, 0.0 ), new Vec3(0.0, 0.0, 0.0 ), new Vec3(0.0, 180.0, 0.0 ), 
			new Vec3(0.0, 0.0, 0.0 ), new Vec3(0.0, 270.0, 0.0), new Vec3(0.0, 90.0, 0.0 ), new Vec3(0.0, 180.0, 0.0 ), 
			new Vec3(0.0, 0.0, 0.0 ), new Vec3(0.0, 90.0, 0.0 ), new Vec3(0.0, 0.0, 0.0 ), new Vec3(0.0, 180.0, 0.0 ), 
			new Vec3(0.0, 90.0, 0.0 ), new Vec3(0.0, 270.0, 0.0 ), new Vec3(0.0,0.0, 0.0 ), new Vec3(0.0, 0.0, 0.0 ), 
			new Vec3(180.0, 270.0, 0.0 ), new Vec3(180.0, 90.0, 0.0 ), new Vec3(180.0, 180.0,0.0 ), new Vec3(180.0, 0.0, 0.0 ), 
			new Vec3(180.0, 270.0, 0.0 ), new Vec3(180.0, 180.0, 0.0 ), new Vec3(180.0, 0.0, 0.0), new Vec3(180.0, 90.0, 0.0 ), 
			new Vec3(180.0, 0.0, 0.0 ), new Vec3(180.0, 90.0, 0.0 ), new Vec3(180.0, 180.0, 0.0 ),new Vec3(180.0, 0.0, 0.0 ), 
			new Vec3(180.0, 270.0, 0.0 ), new Vec3(180.0, 90.0, 0.0 ), new Vec3(180.0, 0.0, 0.0 ), new Vec3(180.0, 0.0, 0.0 ), 
			new Vec3(0.0, 90.0, 0.0 ), new Vec3(0.0, 270.0, 0.0 ), new Vec3(0.0, 0.0, 0.0 ), new Vec3(0.0,180.0, 0.0 ), 
			new Vec3(0.0, 0.0, 0.0 ), new Vec3(0.0, 270.0, 0.0 ), new Vec3(0.0, 90.0, 0.0 ), new Vec3(0.0, 180.0, 0.0), 
			new Vec3(0.0, 0.0, 0.0 ), new Vec3(0.0, 90.0, 0.0 ), new Vec3(0.0, 0.0, 0.0 ), new Vec3(0.0, 180.0, 0.0 ), 
			new Vec3(0.0, 90.0, 0.0 ), new Vec3(0.0, 270.0, 0.0 ), new Vec3(0.0, 0.0, 0.0 ), new Vec3(0.0, 0.0, 0.0 )
	};
	
	/** Maps which base model (out of 18) to use with each recipe index (out of 64).
	 *  Also used to map bounding boxes for each recipe because these generally correspond with model.
	 */
	protected static final AxisAlignedModel[] MODEL_FOR_RECIPE = {
		AxisAlignedModel.ONE_OPEN, AxisAlignedModel.ONE_OPEN, AxisAlignedModel.ONE_OPEN, AxisAlignedModel.ONE_OPEN, AxisAlignedModel.TWO_ADJACENT_OPEN, AxisAlignedModel.TWO_ADJACENT_OPEN, AxisAlignedModel.TWO_ADJACENT_OPEN, AxisAlignedModel.TWO_ADJACENT_OPEN,
		AxisAlignedModel.TWO_OPPOSITE_OPEN, AxisAlignedModel.TWO_OPPOSITE_OPEN, AxisAlignedModel.THREE_OPEN, AxisAlignedModel.THREE_OPEN, AxisAlignedModel.THREE_OPEN, AxisAlignedModel.THREE_OPEN, AxisAlignedModel.FOUR_OPEN, AxisAlignedModel.NONE_OPEN,
		AxisAlignedModel.ONE_TOP_CLOSED, AxisAlignedModel.ONE_TOP_CLOSED, AxisAlignedModel.ONE_TOP_CLOSED, AxisAlignedModel.ONE_TOP_CLOSED, AxisAlignedModel.TWO_ADJACENT_TOP_CLOSED, AxisAlignedModel.TWO_ADJACENT_TOP_CLOSED, AxisAlignedModel.TWO_ADJACENT_TOP_CLOSED, AxisAlignedModel.TWO_ADJACENT_TOP_CLOSED,
		AxisAlignedModel.TWO_OPPOSITE_TOP_CLOSED, AxisAlignedModel.TWO_OPPOSITE_TOP_CLOSED, AxisAlignedModel.THREE_TOP_CLOSED, AxisAlignedModel.THREE_TOP_CLOSED, AxisAlignedModel.THREE_TOP_CLOSED, AxisAlignedModel.THREE_TOP_CLOSED, AxisAlignedModel.FOUR_TOP_CLOSED, AxisAlignedModel.NONE_TOP_CLOSED,
		AxisAlignedModel.ONE_TOP_CLOSED, AxisAlignedModel.ONE_TOP_CLOSED, AxisAlignedModel.ONE_TOP_CLOSED, AxisAlignedModel.ONE_TOP_CLOSED, AxisAlignedModel.TWO_ADJACENT_TOP_CLOSED, AxisAlignedModel.TWO_ADJACENT_TOP_CLOSED, AxisAlignedModel.TWO_ADJACENT_TOP_CLOSED, AxisAlignedModel.TWO_ADJACENT_TOP_CLOSED,
		AxisAlignedModel.TWO_OPPOSITE_TOP_CLOSED, AxisAlignedModel.TWO_OPPOSITE_TOP_CLOSED, AxisAlignedModel.THREE_TOP_CLOSED, AxisAlignedModel.THREE_TOP_CLOSED, AxisAlignedModel.THREE_TOP_CLOSED, AxisAlignedModel.THREE_TOP_CLOSED, AxisAlignedModel.FOUR_TOP_CLOSED, AxisAlignedModel.NONE_TOP_CLOSED,
		AxisAlignedModel.ONE_CLOSED, AxisAlignedModel.ONE_CLOSED, AxisAlignedModel.ONE_CLOSED, AxisAlignedModel.ONE_CLOSED, AxisAlignedModel.TWO_ADJACENT_CLOSED, AxisAlignedModel.TWO_ADJACENT_CLOSED, AxisAlignedModel.TWO_ADJACENT_CLOSED, AxisAlignedModel.TWO_ADJACENT_CLOSED,
		AxisAlignedModel.TWO_OPPOSITE_CLOSED, AxisAlignedModel.TWO_OPPOSITE_CLOSED, AxisAlignedModel.THREE_CLOSED, AxisAlignedModel.THREE_CLOSED, AxisAlignedModel.THREE_CLOSED, AxisAlignedModel.THREE_CLOSED, AxisAlignedModel.FOUR_CLOSED, AxisAlignedModel.NONE_CLOSED
	};
	
	
	protected abstract void populateModelNames();
	
	/** not important unless getCollisionHandler is overriden */
	protected ImmutableList<AxisAlignedBB> getModelBounds(AxisAlignedModel model, Matrix4f rotation){
		ImmutableList defaultList = new ImmutableList.Builder<AxisAlignedBB>().add(new AxisAlignedBB(0, 0, 0, 1, 1, 1)).build();
		return defaultList;
	}
	
	/** won't be called unless getCollisionHandler is overriden */
	@Override
	public MovingObjectPosition collisionRayTrace(World worldIn, BlockPos pos, Vec3 start, Vec3 end) {
        
        ArrayList<AxisAlignedBB> bounds = new ArrayList();
        
        this.addCollisionBoxesToList(worldIn, pos, worldIn.getBlockState(pos), 
        		new AxisAlignedBB(start.xCoord, start.yCoord, start.zCoord, end.xCoord, end.yCoord, end.zCoord), 
        		bounds, null);
        
        MovingObjectPosition retval = null;
        double distance = 1;
        
        for(AxisAlignedBB aabb : bounds){
        	MovingObjectPosition candidate = aabb.calculateIntercept(start, end);
        	if (candidate != null) {
		    	double checkDist = candidate.hitVec.squareDistanceTo(start);
		    	if(retval == null || checkDist < distance) {
		    		retval = candidate;
		    		distance = checkDist;
		    	}
        	}
        }
        
        return retval == null ? null : new MovingObjectPosition(retval.hitVec.addVector((double)pos.getX(), (double)pos.getY(), (double)pos.getZ()), retval.sideHit, pos);
 		
	}
	
	/** Won't be called unless getCollisionHandler is overriden. 
	 *  Uses the bounds in modelBounds array to add appropriate collision bounds.
	 *  Necessary for non-cube blocks.
	 */
	@Override
	public void addCollisionBoxesToList(World worldIn, BlockPos pos, IBlockState state, AxisAlignedBB mask,
			List<AxisAlignedBB> list, Entity collidingEntity) {
	
		AxisAlignedBB localMask = mask.offset(-pos.getX(), -pos.getY(), -pos.getZ());
		NeighborTestResults tests = new NeighborBlocks(worldIn, pos).getNeighborTestResults(new TestForStyle(state));
		
		int recipe =  RECIPE_LOOKUP[tests.up?1:0][tests.down?1:0][tests.east?1:0][tests.west?1:0][tests.north?1:0][tests.south?1:0];
		
		for(AxisAlignedBB aabb: MODEL_BOUNDS[recipe]){
			if(localMask.intersectsWith(aabb)){
				list.add(aabb.offset(pos.getX(), pos.getY(), pos.getZ()));
			}
		}
	}
	
	/** won't  be called unless getCollisionHandler is overriden */
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockAccess worldIn, BlockPos pos, IBlockState state) {
		NeighborTestResults tests = new NeighborBlocks(worldIn, pos).getNeighborTestResults(new TestForStyle(state));
		
		int recipe =  RECIPE_LOOKUP[tests.up?1:0][tests.down?1:0][tests.east?1:0][tests.west?1:0][tests.north?1:0][tests.south?1:0];

		return COMBINED_BOUNDS[recipe].offset(pos.getX(), pos.getY(), pos.getZ());
	}
	
	/** won't be called unless getCollisionHandler is overriden */
	@Override
	public List<AxisAlignedBB> getSelectionBoundingBoxes(World worldIn, BlockPos pos, IBlockState state) {
		
		NeighborTestResults tests = new NeighborBlocks(worldIn, pos).getNeighborTestResults(new TestForStyle(state));
		
		int recipe =  RECIPE_LOOKUP[tests.up?1:0][tests.down?1:0][tests.east?1:0][tests.west?1:0][tests.north?1:0][tests.south?1:0];
		
		ImmutableList.Builder builder = new ImmutableList.Builder<AxisAlignedBB>();
		
		for(AxisAlignedBB aabb: MODEL_BOUNDS[recipe]){
			builder.add(aabb.offset(pos.getX(), pos.getY(), pos.getZ()));
		}
		return builder.build();
	}
	
	protected void setModelBoundsForRecipe(int recipe){
		MODEL_BOUNDS[recipe] = this.getModelBounds(MODEL_FOR_RECIPE[recipe], ROTATION_LOOKUP[recipe].getMatrix());
		AxisAlignedBB compositeBounds = null;
		for(AxisAlignedBB aabb: MODEL_BOUNDS[recipe]){
			if(compositeBounds == null){
				compositeBounds = aabb;
			} else {
				compositeBounds = compositeBounds.union(aabb);
			}
		}
		COMBINED_BOUNDS[recipe] = compositeBounds;
	}
	

	public ModelCookbookAxisOriented (EnumFacing.Axis axis){
		super();
		populateModelNames();

		ROTATION_LOOKUP = new TRSRTransformation[64];
		
		switch (axis){
		case X:
			for(int i=0; i < 64; i++){
				Quat4f rotation = new Quat4f(0, 0, 0, 1);
				rotation.mul(rotationForAxis(Axis.X, ROTATION_LOOKUP_Y[i].yCoord));
				rotation.mul(rotationForAxis(Axis.Y, ROTATION_LOOKUP_Y[i].xCoord));
				rotation.mul(rotationForAxis(Axis.Z, 90.0));
				ROTATION_LOOKUP[i] = TRSRTransformation.blockCenterToCorner(new TRSRTransformation(null, rotation, null, null));
				setModelBoundsForRecipe(i);
			}
			
			RECIPE_LOOKUP[0][1][1][1][1][1]=0;
			RECIPE_LOOKUP[1][0][1][1][1][1]=1;
			RECIPE_LOOKUP[1][1][1][1][0][1]=2;
			RECIPE_LOOKUP[1][1][1][1][1][0]=3;
			RECIPE_LOOKUP[0][1][1][1][0][1]=4;
			RECIPE_LOOKUP[1][0][1][1][0][1]=5;
			RECIPE_LOOKUP[0][1][1][1][1][0]=6;
			RECIPE_LOOKUP[1][0][1][1][1][0]=7;
			RECIPE_LOOKUP[0][0][1][1][1][1]=8;
			RECIPE_LOOKUP[1][1][1][1][0][0]=9;
			RECIPE_LOOKUP[0][0][1][1][0][1]=10;
			RECIPE_LOOKUP[0][0][1][1][1][0]=11;
			RECIPE_LOOKUP[0][1][1][1][0][0]=12;
			RECIPE_LOOKUP[1][0][1][1][0][0]=13;
			RECIPE_LOOKUP[0][0][1][1][0][0]=14;
			RECIPE_LOOKUP[1][1][1][1][1][1]=15;
			RECIPE_LOOKUP[0][1][1][0][1][1]=16;
			RECIPE_LOOKUP[1][0][1][0][1][1]=17;
			RECIPE_LOOKUP[1][1][1][0][0][1]=18;
			RECIPE_LOOKUP[1][1][1][0][1][0]=19;
			RECIPE_LOOKUP[0][1][1][0][0][1]=20;
			RECIPE_LOOKUP[1][0][1][0][0][1]=21;
			RECIPE_LOOKUP[0][1][1][0][1][0]=22;
			RECIPE_LOOKUP[1][0][1][0][1][0]=23;
			RECIPE_LOOKUP[0][0][1][0][1][1]=24;
			RECIPE_LOOKUP[1][1][1][0][0][0]=25;
			RECIPE_LOOKUP[0][0][1][0][0][1]=26;
			RECIPE_LOOKUP[0][0][1][0][1][0]=27;
			RECIPE_LOOKUP[0][1][1][0][0][0]=28;
			RECIPE_LOOKUP[1][0][1][0][0][0]=29;
			RECIPE_LOOKUP[0][0][1][0][0][0]=30;
			RECIPE_LOOKUP[1][1][1][0][1][1]=31;
			RECIPE_LOOKUP[0][1][0][1][1][1]=32;
			RECIPE_LOOKUP[1][0][0][1][1][1]=33;
			RECIPE_LOOKUP[1][1][0][1][0][1]=34;
			RECIPE_LOOKUP[1][1][0][1][1][0]=35;
			RECIPE_LOOKUP[0][1][0][1][0][1]=36;
			RECIPE_LOOKUP[1][0][0][1][0][1]=37;
			RECIPE_LOOKUP[0][1][0][1][1][0]=38;
			RECIPE_LOOKUP[1][0][0][1][1][0]=39;
			RECIPE_LOOKUP[0][0][0][1][1][1]=40;
			RECIPE_LOOKUP[1][1][0][1][0][0]=41;
			RECIPE_LOOKUP[0][0][0][1][0][1]=42;
			RECIPE_LOOKUP[0][0][0][1][1][0]=43;
			RECIPE_LOOKUP[0][1][0][1][0][0]=44;
			RECIPE_LOOKUP[1][0][0][1][0][0]=45;
			RECIPE_LOOKUP[0][0][0][1][0][0]=46;
			RECIPE_LOOKUP[1][1][0][1][1][1]=47;
			RECIPE_LOOKUP[0][1][0][0][1][1]=48;
			RECIPE_LOOKUP[1][0][0][0][1][1]=49;
			RECIPE_LOOKUP[1][1][0][0][0][1]=50;
			RECIPE_LOOKUP[1][1][0][0][1][0]=51;
			RECIPE_LOOKUP[0][1][0][0][0][1]=52;
			RECIPE_LOOKUP[1][0][0][0][0][1]=53;
			RECIPE_LOOKUP[0][1][0][0][1][0]=54;
			RECIPE_LOOKUP[1][0][0][0][1][0]=55;
			RECIPE_LOOKUP[0][0][0][0][1][1]=56;
			RECIPE_LOOKUP[1][1][0][0][0][0]=57;
			RECIPE_LOOKUP[0][0][0][0][0][1]=58;
			RECIPE_LOOKUP[0][0][0][0][1][0]=59;
			RECIPE_LOOKUP[0][1][0][0][0][0]=60;
			RECIPE_LOOKUP[1][0][0][0][0][0]=61;
			RECIPE_LOOKUP[0][0][0][0][0][0]=62;
			RECIPE_LOOKUP[1][1][0][0][1][1]=63;
			break;
			
		case Y:
			for(int i=0; i < 64; i++){
				Quat4f rotation = new Quat4f(0, 0, 0, 1);
				rotation.mul(rotationForAxis(Axis.Y, -ROTATION_LOOKUP_Y[i].yCoord));
				rotation.mul(rotationForAxis(Axis.X, ROTATION_LOOKUP_Y[i].xCoord));
				ROTATION_LOOKUP[i] = TRSRTransformation.blockCenterToCorner(new TRSRTransformation(null, rotation, null, null));
				setModelBoundsForRecipe(i);
			}
			
			RECIPE_LOOKUP[1][1][0][1][1][1]=0;
			RECIPE_LOOKUP[1][1][1][0][1][1]=1;
			RECIPE_LOOKUP[1][1][1][1][0][1]=2;
			RECIPE_LOOKUP[1][1][1][1][1][0]=3;
			RECIPE_LOOKUP[1][1][0][1][0][1]=4;
			RECIPE_LOOKUP[1][1][1][0][0][1]=5;
			RECIPE_LOOKUP[1][1][0][1][1][0]=6;
			RECIPE_LOOKUP[1][1][1][0][1][0]=7;
			RECIPE_LOOKUP[1][1][0][0][1][1]=8;
			RECIPE_LOOKUP[1][1][1][1][0][0]=9;
			RECIPE_LOOKUP[1][1][0][0][0][1]=10;
			RECIPE_LOOKUP[1][1][0][0][1][0]=11;
			RECIPE_LOOKUP[1][1][0][1][0][0]=12;
			RECIPE_LOOKUP[1][1][1][0][0][0]=13;
			RECIPE_LOOKUP[1][1][0][0][0][0]=14;
			RECIPE_LOOKUP[1][1][1][1][1][1]=15;
			RECIPE_LOOKUP[0][1][0][1][1][1]=16;
			RECIPE_LOOKUP[0][1][1][0][1][1]=17;
			RECIPE_LOOKUP[0][1][1][1][0][1]=18;
			RECIPE_LOOKUP[0][1][1][1][1][0]=19;
			RECIPE_LOOKUP[0][1][0][1][0][1]=20;
			RECIPE_LOOKUP[0][1][1][0][0][1]=21;
			RECIPE_LOOKUP[0][1][0][1][1][0]=22;
			RECIPE_LOOKUP[0][1][1][0][1][0]=23;
			RECIPE_LOOKUP[0][1][0][0][1][1]=24;
			RECIPE_LOOKUP[0][1][1][1][0][0]=25;
			RECIPE_LOOKUP[0][1][0][0][0][1]=26;
			RECIPE_LOOKUP[0][1][0][0][1][0]=27;
			RECIPE_LOOKUP[0][1][0][1][0][0]=28;
			RECIPE_LOOKUP[0][1][1][0][0][0]=29;
			RECIPE_LOOKUP[0][1][0][0][0][0]=30;
			RECIPE_LOOKUP[0][1][1][1][1][1]=31;
			RECIPE_LOOKUP[1][0][0][1][1][1]=32;
			RECIPE_LOOKUP[1][0][1][0][1][1]=33;
			RECIPE_LOOKUP[1][0][1][1][0][1]=34;
			RECIPE_LOOKUP[1][0][1][1][1][0]=35;
			RECIPE_LOOKUP[1][0][0][1][0][1]=36;
			RECIPE_LOOKUP[1][0][1][0][0][1]=37;
			RECIPE_LOOKUP[1][0][0][1][1][0]=38;
			RECIPE_LOOKUP[1][0][1][0][1][0]=39;
			RECIPE_LOOKUP[1][0][0][0][1][1]=40;
			RECIPE_LOOKUP[1][0][1][1][0][0]=41;
			RECIPE_LOOKUP[1][0][0][0][0][1]=42;
			RECIPE_LOOKUP[1][0][0][0][1][0]=43;
			RECIPE_LOOKUP[1][0][0][1][0][0]=44;
			RECIPE_LOOKUP[1][0][1][0][0][0]=45;
			RECIPE_LOOKUP[1][0][0][0][0][0]=46;
			RECIPE_LOOKUP[1][0][1][1][1][1]=47;
			RECIPE_LOOKUP[0][0][0][1][1][1]=48;
			RECIPE_LOOKUP[0][0][1][0][1][1]=49;
			RECIPE_LOOKUP[0][0][1][1][0][1]=50;
			RECIPE_LOOKUP[0][0][1][1][1][0]=51;
			RECIPE_LOOKUP[0][0][0][1][0][1]=52;
			RECIPE_LOOKUP[0][0][1][0][0][1]=53;
			RECIPE_LOOKUP[0][0][0][1][1][0]=54;
			RECIPE_LOOKUP[0][0][1][0][1][0]=55;
			RECIPE_LOOKUP[0][0][0][0][1][1]=56;
			RECIPE_LOOKUP[0][0][1][1][0][0]=57;
			RECIPE_LOOKUP[0][0][0][0][0][1]=58;
			RECIPE_LOOKUP[0][0][0][0][1][0]=59;
			RECIPE_LOOKUP[0][0][0][1][0][0]=60;
			RECIPE_LOOKUP[0][0][1][0][0][0]=61;
			RECIPE_LOOKUP[0][0][0][0][0][0]=62;
			RECIPE_LOOKUP[0][0][1][1][1][1]=63;

			break;
			
		case Z:
			for(int i=0; i < 64; i++){
				Quat4f rotation = new Quat4f(0, 0, 0, 1);
				rotation.mul(rotationForAxis(Axis.Z, -ROTATION_LOOKUP_Y[i].yCoord));
				rotation.mul(rotationForAxis(Axis.X, ROTATION_LOOKUP_Y[i].xCoord + 90));
				rotation.mul(rotationForAxis(Axis.Y, 180.0));
				ROTATION_LOOKUP[i] = TRSRTransformation.blockCenterToCorner(new TRSRTransformation(null, rotation, null, null));
				setModelBoundsForRecipe(i);
			}
			
			RECIPE_LOOKUP[1][1][1][0][1][1]=0;
			RECIPE_LOOKUP[1][1][0][1][1][1]=1;
			RECIPE_LOOKUP[1][0][1][1][1][1]=2;
			RECIPE_LOOKUP[0][1][1][1][1][1]=3;
			RECIPE_LOOKUP[1][0][1][0][1][1]=4;
			RECIPE_LOOKUP[1][0][0][1][1][1]=5;
			RECIPE_LOOKUP[0][1][1][0][1][1]=6;
			RECIPE_LOOKUP[0][1][0][1][1][1]=7;
			RECIPE_LOOKUP[1][1][0][0][1][1]=8;
			RECIPE_LOOKUP[0][0][1][1][1][1]=9;
			RECIPE_LOOKUP[1][0][0][0][1][1]=10;
			RECIPE_LOOKUP[0][1][0][0][1][1]=11;
			RECIPE_LOOKUP[0][0][1][0][1][1]=12;
			RECIPE_LOOKUP[0][0][0][1][1][1]=13;
			RECIPE_LOOKUP[0][0][0][0][1][1]=14;
			RECIPE_LOOKUP[1][1][1][1][1][1]=15;
			RECIPE_LOOKUP[1][1][1][0][1][0]=16;
			RECIPE_LOOKUP[1][1][0][1][1][0]=17;
			RECIPE_LOOKUP[1][0][1][1][1][0]=18;
			RECIPE_LOOKUP[0][1][1][1][1][0]=19;
			RECIPE_LOOKUP[1][0][1][0][1][0]=20;
			RECIPE_LOOKUP[1][0][0][1][1][0]=21;
			RECIPE_LOOKUP[0][1][1][0][1][0]=22;
			RECIPE_LOOKUP[0][1][0][1][1][0]=23;
			RECIPE_LOOKUP[1][1][0][0][1][0]=24;
			RECIPE_LOOKUP[0][0][1][1][1][0]=25;
			RECIPE_LOOKUP[1][0][0][0][1][0]=26;
			RECIPE_LOOKUP[0][1][0][0][1][0]=27;
			RECIPE_LOOKUP[0][0][1][0][1][0]=28;
			RECIPE_LOOKUP[0][0][0][1][1][0]=29;
			RECIPE_LOOKUP[0][0][0][0][1][0]=30;
			RECIPE_LOOKUP[1][1][1][1][1][0]=31;
			RECIPE_LOOKUP[1][1][1][0][0][1]=32;
			RECIPE_LOOKUP[1][1][0][1][0][1]=33;
			RECIPE_LOOKUP[1][0][1][1][0][1]=34;
			RECIPE_LOOKUP[0][1][1][1][0][1]=35;
			RECIPE_LOOKUP[1][0][1][0][0][1]=36;
			RECIPE_LOOKUP[1][0][0][1][0][1]=37;
			RECIPE_LOOKUP[0][1][1][0][0][1]=38;
			RECIPE_LOOKUP[0][1][0][1][0][1]=39;
			RECIPE_LOOKUP[1][1][0][0][0][1]=40;
			RECIPE_LOOKUP[0][0][1][1][0][1]=41;
			RECIPE_LOOKUP[1][0][0][0][0][1]=42;
			RECIPE_LOOKUP[0][1][0][0][0][1]=43;
			RECIPE_LOOKUP[0][0][1][0][0][1]=44;
			RECIPE_LOOKUP[0][0][0][1][0][1]=45;
			RECIPE_LOOKUP[0][0][0][0][0][1]=46;
			RECIPE_LOOKUP[1][1][1][1][0][1]=47;
			RECIPE_LOOKUP[1][1][1][0][0][0]=48;
			RECIPE_LOOKUP[1][1][0][1][0][0]=49;
			RECIPE_LOOKUP[1][0][1][1][0][0]=50;
			RECIPE_LOOKUP[0][1][1][1][0][0]=51;
			RECIPE_LOOKUP[1][0][1][0][0][0]=52;
			RECIPE_LOOKUP[1][0][0][1][0][0]=53;
			RECIPE_LOOKUP[0][1][1][0][0][0]=54;
			RECIPE_LOOKUP[0][1][0][1][0][0]=55;
			RECIPE_LOOKUP[1][1][0][0][0][0]=56;
			RECIPE_LOOKUP[0][0][1][1][0][0]=57;
			RECIPE_LOOKUP[1][0][0][0][0][0]=58;
			RECIPE_LOOKUP[0][1][0][0][0][0]=59;
			RECIPE_LOOKUP[0][0][1][0][0][0]=60;
			RECIPE_LOOKUP[0][0][0][1][0][0]=61;
			RECIPE_LOOKUP[0][0][0][0][0][0]=62;
			RECIPE_LOOKUP[1][1][1][1][0][0]=63;
			break;
		}
	}

	@Override
	public int getRecipeIndex(IExtendedBlockState state, IBlockAccess worldIn, BlockPos pos) {
		
		NeighborTestResults tests = new NeighborBlocks(worldIn, pos).getNeighborTestResults(new TestForStyle(state));
		
		return  RECIPE_LOOKUP[tests.up?1:0][tests.down?1:0][tests.east?1:0][tests.west?1:0][tests.north?1:0][tests.south?1:0];

	}
	
	@Override
	public int getRecipeCount() {
		return 64;
	}

	@Override
	public int getItemModelIndex() {
		return 62;
	}
	
	public enum AxisAlignedModel{
		FOUR_CLOSED(0),
		FOUR_TOP_CLOSED(1),
		FOUR_OPEN(2),
		THREE_CLOSED(3),
		THREE_TOP_CLOSED(4),
		THREE_OPEN(5),
		TWO_ADJACENT_CLOSED(6),
		TWO_ADJACENT_TOP_CLOSED(7),
		TWO_ADJACENT_OPEN(8),
		TWO_OPPOSITE_CLOSED(9),
		TWO_OPPOSITE_TOP_CLOSED(10),
		TWO_OPPOSITE_OPEN(11),
		ONE_CLOSED(12),
		ONE_TOP_CLOSED(13),
		ONE_OPEN(14),
		NONE_CLOSED(15),
		NONE_TOP_CLOSED(16),
		NONE_OPEN(17);
		
		public final int index;
		
		private AxisAlignedModel(int index){
			this.index = index;
		}
		
	}


	
	
}
