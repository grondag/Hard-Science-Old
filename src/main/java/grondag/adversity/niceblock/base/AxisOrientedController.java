package grondag.adversity.niceblock.base;

import java.util.ArrayList;
import java.util.List;

import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.niceblock.joinstate.BlockJoinSelector;
import grondag.adversity.niceblock.support.BlockTests;
import grondag.adversity.niceblock.support.ICollisionHandler;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class AxisOrientedController extends ModelController implements ICollisionHandler
{

    /**
     * Locate models for each scenario.  The models for this type of block
     * are rotated as needed so noisy textures are best avoided.
     */
   // protected String[] modelNames = new String[AxisAlignedModel.values().length];
    
    protected final IAlternator alternator;

    /**
     * Cache collision box lists.
     * Override getCollisionHandler and getModelBounds if need something 
     * other than standard cubes.  Has to be here and not in parent because
     * the number of models is specific to cookbook.
     */
    @SuppressWarnings("unchecked")
	public final ImmutableList<AxisAlignedBB>[][] MODEL_BOUNDS = new ImmutableList[3][64];
    
    /**
     * Cached union of model bounds.  
     */
    public final AxisAlignedBB[][] COMBINED_BOUNDS = new AxisAlignedBB[3][64];

    public final ModelType modelType;
    
    protected AxisOrientedController(String textureName, int alternateTextureCount, ModelType modelType, boolean isShaded)
    {
        super(textureName, alternateTextureCount, modelType == ModelType.LAMP_OVERLAY ? BlockRenderLayer.CUTOUT_MIPPED : BlockRenderLayer.SOLID, isShaded, false);
        this.alternator = Alternator.getAlternator((byte)(alternateTextureCount));
        this.modelType = modelType;
        for (int i = 0; i < 64; i++) {
            this.setModelBoundsForShape(EnumFacing.Axis.X, i);
            this.setModelBoundsForShape(EnumFacing.Axis.Y, i);
            this.setModelBoundsForShape(EnumFacing.Axis.Z, i);
        }
    }
    
    /**
     * Creates combined bounds for methods that don't do ray tracing or that
     * test for an hit before doing the ray trace.
     * Generally should not need to override this for non-standard bounds.
     */
    private void setModelBoundsForShape(EnumFacing.Axis axis, int shapeIndex) {
        MODEL_BOUNDS[axis.ordinal()][shapeIndex] = getModelBounds(axis, shapeIndex);
        AxisAlignedBB compositeBounds = null;
        for (AxisAlignedBB aabb : MODEL_BOUNDS[axis.ordinal()][shapeIndex]) {
            if (compositeBounds == null) {
                compositeBounds = aabb;
            } else {
                compositeBounds = compositeBounds.union(aabb);
            }
        }
        COMBINED_BOUNDS[axis.ordinal()][shapeIndex] = compositeBounds;
    }


    /** Override this and getCollisionHandler to implement non-standard collision bounds*/
    protected ImmutableList<AxisAlignedBB> getModelBounds(EnumFacing.Axis axis, int shapeIndex) {
        ImmutableList<AxisAlignedBB> defaultList = new ImmutableList.Builder<AxisAlignedBB>().add(new AxisAlignedBB(0, 0, 0, 1, 1, 1)).build();
        return defaultList;
    }
    
    /** won't be called unless getCollisionHandler is overriden */
    @Override
    public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {

        ArrayList<AxisAlignedBB> bounds = new ArrayList<AxisAlignedBB>();

        addCollisionBoxesToList(worldIn, pos, worldIn.getBlockState(pos),
                new AxisAlignedBB(start.xCoord, start.yCoord, start.zCoord, end.xCoord, end.yCoord, end.zCoord),
                bounds, null);

        RayTraceResult retval = null;
        double distance = 1;

        for (AxisAlignedBB aabb : bounds) {
        	RayTraceResult candidate = aabb.calculateIntercept(start, end);
            if (candidate != null) {
                double checkDist = candidate.hitVec.squareDistanceTo(start);
                if (retval == null || checkDist < distance) {
                    retval = candidate;
                    distance = checkDist;
                }
            }
        }
        return retval == null ? null : new RayTraceResult(retval.hitVec.addVector(pos.getX(), pos.getY(), pos.getZ()), retval.sideHit, pos);
    }

    /**
     * Won't be called unless getCollisionHandler is overriden. Uses the bounds
     * in modelBounds array to add appropriate collision bounds. Necessary for
     * non-cube blocks.
     */
    @Override
    public void addCollisionBoxesToList(World worldIn, BlockPos pos, IBlockState state, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {

        int clientShapeIndex = ((NiceBlock)state.getBlock()).blockModelHelper.getModelStateForBlock(state, worldIn, pos, true)
                .getClientShapeIndex(this.getRenderLayer().ordinal());
            
        AxisAlignedBB localMask = mask.offset(-pos.getX(), -pos.getY(), -pos.getZ());

        for (AxisAlignedBB aabb : MODEL_BOUNDS[getAxisFromModelIndex(clientShapeIndex)][getShapeFromModelIndex(clientShapeIndex)]) {
            if (localMask.intersectsWith(aabb)) {
                list.add(aabb.offset(pos.getX(), pos.getY(), pos.getZ()));
            }
        }
    }

    /** won't be called unless getCollisionHandler is overriden */
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockAccess worldIn, BlockPos pos, IBlockState state) {
        int clientShapeIndex = ((NiceBlock)state.getBlock()).blockModelHelper.getModelStateForBlock(state, worldIn, pos, true)
                .getClientShapeIndex(this.getRenderLayer().ordinal());
            
        return COMBINED_BOUNDS[getAxisFromModelIndex(clientShapeIndex)][getShapeFromModelIndex(clientShapeIndex)].offset(pos.getX(), pos.getY(), pos.getZ());
    }

    /** won't be called unless getCollisionHandler is overriden */
    @Override
    public List<AxisAlignedBB> getSelectionBoundingBoxes(World worldIn, BlockPos pos, IBlockState state) {

        int clientShapeIndex = ((NiceBlock)state.getBlock()).blockModelHelper.getModelStateForBlock(state, worldIn, pos, true)
                .getClientShapeIndex(this.getRenderLayer().ordinal());

        ImmutableList.Builder<AxisAlignedBB> builder = new ImmutableList.Builder<AxisAlignedBB>();

        for (AxisAlignedBB aabb : MODEL_BOUNDS[getAxisFromModelIndex(clientShapeIndex)][getShapeFromModelIndex(clientShapeIndex)]) {
            builder.add(aabb.offset(pos.getX(), pos.getY(), pos.getZ()));
        }
        return builder.build();
    }
    
    @Override
    public int getClientShapeIndex(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        int axis = Math.max(0, Math.min(2, state.getValue(NiceBlock.META)));
        NeighborTestResults tests = new NeighborBlocks(world, pos).getNeighborTestResults(new BlockTests.TestForBlockMetaMatch(state));
        
        int shapeIndex = BlockJoinSelector.findIndex(tests);
        
        int textureAlternate = this.alternator.getAlternate(pos);

        return (textureAlternate * 3 + axis) * BlockJoinSelector.BLOCK_JOIN_STATE_COUNT + shapeIndex;
    }

    @Override
    public int getShapeCount()
    {
        return BlockJoinSelector.BLOCK_JOIN_STATE_COUNT * 3 * getAlternateTextureCount();
    }

    public int getAxisFromModelIndex(int clientShapeIndex)
    {
        return (clientShapeIndex / BlockJoinSelector.BLOCK_JOIN_STATE_COUNT) % 3;
    }

    public int getTextureFromModelIndex(int clientShapeIndex)
    {
        return (clientShapeIndex / BlockJoinSelector.BLOCK_JOIN_STATE_COUNT) / 3;
    }
    
    public int getShapeFromModelIndex(int clientShapeIndex)
    {
        return (clientShapeIndex % BlockJoinSelector.BLOCK_JOIN_STATE_COUNT);
    }

    public static enum ModelType
    {
        NORMAL,
        LAMP_BASE,
        LAMP_OVERLAY;
    }
}
