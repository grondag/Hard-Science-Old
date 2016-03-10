package grondag.adversity.niceblock.newmodel;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;

import grondag.adversity.Adversity;
import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.niceblock.newmodel.AxisOrientedController.ModelType;
import grondag.adversity.niceblock.support.ICollisionHandler;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;

public abstract class AxisOrientedController extends ModelControllerNew implements ICollisionHandler
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
    public final ImmutableList<AxisAlignedBB>[][] MODEL_BOUNDS = new ImmutableList[3][64];
    
    /**
     * Cached union of model bounds.  
     */
    public final AxisAlignedBB[][] COMBINED_BOUNDS = new AxisAlignedBB[3][64];

//    /**
//     * Shape lookups for axis-aligned models.
//     * First dimension is axis.
//     */
//    protected final static Integer[][][][][][][] SHAPE_INDEX_LOOKUPS = new Integer[3][2][2][2][2][2][2];
//    
//    /**
//     * Rotations needed to reorient models properly for a given recipe.
//     * Could generate this programmatically but did these by hand before deciding to 
//     * make it generic and it weren't broke, so... 
//     * First dimenions is axis.
//     */
//    protected final static TRSRTransformation[][] ROTATION_LOOKUPS;
//    
//    protected final static Vec3[] ROTATION_LOOKUP_Y = {
//            new Vec3(0.0, 90.0, 0.0), new Vec3(0.0, 270.0, 0.0), new Vec3(0.0, 0.0, 0.0), new Vec3(0.0, 180.0, 0.0),
//            new Vec3(0.0, 0.0, 0.0), new Vec3(0.0, 270.0, 0.0), new Vec3(0.0, 90.0, 0.0), new Vec3(0.0, 180.0, 0.0),
//            new Vec3(0.0, 0.0, 0.0), new Vec3(0.0, 90.0, 0.0), new Vec3(0.0, 0.0, 0.0), new Vec3(0.0, 180.0, 0.0),
//            new Vec3(0.0, 90.0, 0.0), new Vec3(0.0, 270.0, 0.0), new Vec3(0.0, 0.0, 0.0), new Vec3(0.0, 0.0, 0.0),
//            new Vec3(0.0, 90.0, 0.0), new Vec3(0.0, 270.0, 0.0), new Vec3(0.0, 0.0, 0.0), new Vec3(0.0, 180.0, 0.0),
//            new Vec3(0.0, 0.0, 0.0), new Vec3(0.0, 270.0, 0.0), new Vec3(0.0, 90.0, 0.0), new Vec3(0.0, 180.0, 0.0),
//            new Vec3(0.0, 0.0, 0.0), new Vec3(0.0, 90.0, 0.0), new Vec3(0.0, 0.0, 0.0), new Vec3(0.0, 180.0, 0.0),
//            new Vec3(0.0, 90.0, 0.0), new Vec3(0.0, 270.0, 0.0), new Vec3(0.0, 0.0, 0.0), new Vec3(0.0, 0.0, 0.0),
//            new Vec3(180.0, 270.0, 0.0), new Vec3(180.0, 90.0, 0.0), new Vec3(180.0, 180.0, 0.0), new Vec3(180.0, 0.0, 0.0),
//            new Vec3(180.0, 270.0, 0.0), new Vec3(180.0, 180.0, 0.0), new Vec3(180.0, 0.0, 0.0), new Vec3(180.0, 90.0, 0.0),
//            new Vec3(180.0, 0.0, 0.0), new Vec3(180.0, 90.0, 0.0), new Vec3(180.0, 180.0, 0.0), new Vec3(180.0, 0.0, 0.0),
//            new Vec3(180.0, 270.0, 0.0), new Vec3(180.0, 90.0, 0.0), new Vec3(180.0, 0.0, 0.0), new Vec3(180.0, 0.0, 0.0),
//            new Vec3(0.0, 90.0, 0.0), new Vec3(0.0, 270.0, 0.0), new Vec3(0.0, 0.0, 0.0), new Vec3(0.0, 180.0, 0.0),
//            new Vec3(0.0, 0.0, 0.0), new Vec3(0.0, 270.0, 0.0), new Vec3(0.0, 90.0, 0.0), new Vec3(0.0, 180.0, 0.0),
//            new Vec3(0.0, 0.0, 0.0), new Vec3(0.0, 90.0, 0.0), new Vec3(0.0, 0.0, 0.0), new Vec3(0.0, 180.0, 0.0),
//            new Vec3(0.0, 90.0, 0.0), new Vec3(0.0, 270.0, 0.0), new Vec3(0.0, 0.0, 0.0), new Vec3(0.0, 0.0, 0.0)
//    };

    /**
     * Maps which base model (out of 18) to use with each shape index (out of
     * 64). Also used to map bounding boxes for each shape because these
     * generally correspond with model.
     */
//    protected static final AxisAlignedModel[] MODEL_FOR_SHAPE_INDEX = {
//            AxisAlignedModel.ONE_OPEN, AxisAlignedModel.ONE_OPEN, AxisAlignedModel.ONE_OPEN, AxisAlignedModel.ONE_OPEN, AxisAlignedModel.TWO_ADJACENT_OPEN, AxisAlignedModel.TWO_ADJACENT_OPEN, AxisAlignedModel.TWO_ADJACENT_OPEN, AxisAlignedModel.TWO_ADJACENT_OPEN,
//            AxisAlignedModel.TWO_OPPOSITE_OPEN, AxisAlignedModel.TWO_OPPOSITE_OPEN, AxisAlignedModel.THREE_OPEN, AxisAlignedModel.THREE_OPEN, AxisAlignedModel.THREE_OPEN, AxisAlignedModel.THREE_OPEN, AxisAlignedModel.FOUR_OPEN, AxisAlignedModel.NONE_OPEN,
//            AxisAlignedModel.ONE_TOP_CLOSED, AxisAlignedModel.ONE_TOP_CLOSED, AxisAlignedModel.ONE_TOP_CLOSED, AxisAlignedModel.ONE_TOP_CLOSED, AxisAlignedModel.TWO_ADJACENT_TOP_CLOSED, AxisAlignedModel.TWO_ADJACENT_TOP_CLOSED, AxisAlignedModel.TWO_ADJACENT_TOP_CLOSED, AxisAlignedModel.TWO_ADJACENT_TOP_CLOSED,
//            AxisAlignedModel.TWO_OPPOSITE_TOP_CLOSED, AxisAlignedModel.TWO_OPPOSITE_TOP_CLOSED, AxisAlignedModel.THREE_TOP_CLOSED, AxisAlignedModel.THREE_TOP_CLOSED, AxisAlignedModel.THREE_TOP_CLOSED, AxisAlignedModel.THREE_TOP_CLOSED, AxisAlignedModel.FOUR_TOP_CLOSED, AxisAlignedModel.NONE_TOP_CLOSED,
//            AxisAlignedModel.ONE_TOP_CLOSED, AxisAlignedModel.ONE_TOP_CLOSED, AxisAlignedModel.ONE_TOP_CLOSED, AxisAlignedModel.ONE_TOP_CLOSED, AxisAlignedModel.TWO_ADJACENT_TOP_CLOSED, AxisAlignedModel.TWO_ADJACENT_TOP_CLOSED, AxisAlignedModel.TWO_ADJACENT_TOP_CLOSED, AxisAlignedModel.TWO_ADJACENT_TOP_CLOSED,
//            AxisAlignedModel.TWO_OPPOSITE_TOP_CLOSED, AxisAlignedModel.TWO_OPPOSITE_TOP_CLOSED, AxisAlignedModel.THREE_TOP_CLOSED, AxisAlignedModel.THREE_TOP_CLOSED, AxisAlignedModel.THREE_TOP_CLOSED, AxisAlignedModel.THREE_TOP_CLOSED, AxisAlignedModel.FOUR_TOP_CLOSED, AxisAlignedModel.NONE_TOP_CLOSED,
//            AxisAlignedModel.ONE_CLOSED, AxisAlignedModel.ONE_CLOSED, AxisAlignedModel.ONE_CLOSED, AxisAlignedModel.ONE_CLOSED, AxisAlignedModel.TWO_ADJACENT_CLOSED, AxisAlignedModel.TWO_ADJACENT_CLOSED, AxisAlignedModel.TWO_ADJACENT_CLOSED, AxisAlignedModel.TWO_ADJACENT_CLOSED,
//            AxisAlignedModel.TWO_OPPOSITE_CLOSED, AxisAlignedModel.TWO_OPPOSITE_CLOSED, AxisAlignedModel.THREE_CLOSED, AxisAlignedModel.THREE_CLOSED, AxisAlignedModel.THREE_CLOSED, AxisAlignedModel.THREE_CLOSED, AxisAlignedModel.FOUR_CLOSED, AxisAlignedModel.NONE_CLOSED
//    };

    public final ModelType modelType;
    
  
        
    protected AxisOrientedController(String textureName, int alternateTextureCount, ModelType modelType, boolean isShaded)
    {
        super(textureName, alternateTextureCount, modelType == ModelType.LAMP_OVERLAY ? EnumWorldBlockLayer.CUTOUT_MIPPED : EnumWorldBlockLayer.SOLID, isShaded, false);
        this.alternator = Alternator.getAlternator((byte)(alternateTextureCount));
  //      populateModelNames();
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
    
//    /**
//     * Implementation-specific, to allow for different model types/layouts.
//     */
//    protected abstract void populateModelNames();

    /** Override this and getCollisionHandler to implement non-standard collision bounds*/
    protected ImmutableList<AxisAlignedBB> getModelBounds(EnumFacing.Axis axis, int shapeIndex) {
        ImmutableList defaultList = new ImmutableList.Builder<AxisAlignedBB>().add(new AxisAlignedBB(0, 0, 0, 1, 1, 1)).build();
        return defaultList;
    }
    
    /** won't be called unless getCollisionHandler is overriden */
    @Override
    public MovingObjectPosition collisionRayTrace(World worldIn, BlockPos pos, Vec3 start, Vec3 end) {

        ArrayList<AxisAlignedBB> bounds = new ArrayList();

        addCollisionBoxesToList(worldIn, pos, worldIn.getBlockState(pos),
                new AxisAlignedBB(start.xCoord, start.yCoord, start.zCoord, end.xCoord, end.yCoord, end.zCoord),
                bounds, null);

        MovingObjectPosition retval = null;
        double distance = 1;

        for (AxisAlignedBB aabb : bounds) {
            MovingObjectPosition candidate = aabb.calculateIntercept(start, end);
            if (candidate != null) {
                double checkDist = candidate.hitVec.squareDistanceTo(start);
                if (retval == null || checkDist < distance) {
                    retval = candidate;
                    distance = checkDist;
                }
            }
        }
        return retval == null ? null : new MovingObjectPosition(retval.hitVec.addVector(pos.getX(), pos.getY(), pos.getZ()), retval.sideHit, pos);
    }

    /**
     * Won't be called unless getCollisionHandler is overriden. Uses the bounds
     * in modelBounds array to add appropriate collision bounds. Necessary for
     * non-cube blocks.
     */
    @Override
    public void addCollisionBoxesToList(World worldIn, BlockPos pos, IBlockState state, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {

        int clientShapeIndex = ((NiceBlock)state.getBlock()).blockModelHelper.getModelStateForBlock(state, worldIn, pos, true)
                .getClientShapeIndex(this.renderLayer.ordinal());
            
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
                .getClientShapeIndex(this.renderLayer.ordinal());
            
        return COMBINED_BOUNDS[getAxisFromModelIndex(clientShapeIndex)][getShapeFromModelIndex(clientShapeIndex)].offset(pos.getX(), pos.getY(), pos.getZ());
    }

    /** won't be called unless getCollisionHandler is overriden */
    @Override
    public List<AxisAlignedBB> getSelectionBoundingBoxes(World worldIn, BlockPos pos, IBlockState state) {

        int clientShapeIndex = ((NiceBlock)state.getBlock()).blockModelHelper.getModelStateForBlock(state, worldIn, pos, true)
                .getClientShapeIndex(this.renderLayer.ordinal());

        ImmutableList.Builder builder = new ImmutableList.Builder<AxisAlignedBB>();

        for (AxisAlignedBB aabb : MODEL_BOUNDS[getAxisFromModelIndex(clientShapeIndex)][getShapeFromModelIndex(clientShapeIndex)]) {
            builder.add(aabb.offset(pos.getX(), pos.getY(), pos.getZ()));
        }
        return builder.build();
    }

//    public enum AxisAlignedModel {
//        FOUR_CLOSED(0),
//        FOUR_TOP_CLOSED(1),
//        FOUR_OPEN(2),
//        THREE_CLOSED(3),
//        THREE_TOP_CLOSED(4),
//        THREE_OPEN(5),
//        TWO_ADJACENT_CLOSED(6),
//        TWO_ADJACENT_TOP_CLOSED(7),
//        TWO_ADJACENT_OPEN(8),
//        TWO_OPPOSITE_CLOSED(9),
//        TWO_OPPOSITE_TOP_CLOSED(10),
//        TWO_OPPOSITE_OPEN(11),
//        ONE_CLOSED(12),
//        ONE_TOP_CLOSED(13),
//        ONE_OPEN(14),
//        NONE_CLOSED(15),
//        NONE_TOP_CLOSED(16),
//        NONE_OPEN(17);
//
//        public final int index;
//
//        private AxisAlignedModel(int index) {
//            this.index = index;
//        }
//
//    }
    
    @Override
    public int getClientShapeIndex(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        int axis = Math.max(0, Math.min(2, state.getValue(NiceBlock.META)));
        NeighborTestResults tests = new NeighborBlocks(world, pos).getNeighborTestResults(new BlockTests.TestForBlockMetaMatch(state));
        
        //TODO should probably just use a regular simple join
        int shapeIndex = new ModelReference.AxisJoin(tests, EnumFacing.Axis.values()[axis]).getIndex();
        
        // int shapeIndex = SHAPE_INDEX_LOOKUPS[axis][tests.upBit()][tests.downBit()][tests.eastBit()][tests.westBit()][tests.northBit()][tests.southBit()];
        int textureAlternate = this.alternator.getAlternate(pos);

        return (textureAlternate * 3 + axis) << 6 | shapeIndex;
        //return (textureAlternate * 3 + axis) << 386 | shapeIndex;
    }

    @Override
    public int getShapeCount()
    {
        return 64 * 3 * alternateTextureCount;
        //return 386 * 3 * alternateTextureCount;
    }

    protected static int getAxisFromModelIndex(int clientShapeIndex)
    {
        return (clientShapeIndex >> 6) % 3;
        //return (clientShapeIndex / 386) % 3;
    }

    protected static int getTextureFromModelIndex(int clientShapeIndex)
    {
        return (clientShapeIndex >> 6) / 3;
        //return (clientShapeIndex / 386) / 3;
    }
    
    protected static int getShapeFromModelIndex(int clientShapeIndex)
    {
        return (clientShapeIndex & 63);
        //return (clientShapeIndex % 386);
    }

//    static
//    {
//        ROTATION_LOOKUPS = new TRSRTransformation[3][64];
//
//        // X AXIS SETUP
//        for (int i = 0; i < 64; i++) {
//            Quat4f rotation = new Quat4f(0, 0, 0, 1);
//            rotation.mul(ModelReference.rotationForAxis(Axis.X, ROTATION_LOOKUP_Y[i].yCoord));
//            rotation.mul(ModelReference.rotationForAxis(Axis.Y, ROTATION_LOOKUP_Y[i].xCoord));
//            rotation.mul(ModelReference.rotationForAxis(Axis.Z, 90.0));
//            ROTATION_LOOKUPS[EnumFacing.Axis.X.ordinal()][i] = TRSRTransformation.blockCenterToCorner(new TRSRTransformation(null, rotation, null, null));
//        }
//
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][1][1][1][1][1] = 0;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][0][1][1][1][1] = 1;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][1][1][1][0][1] = 2;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][1][1][1][1][0] = 3;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][1][1][1][0][1] = 4;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][0][1][1][0][1] = 5;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][1][1][1][1][0] = 6;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][0][1][1][1][0] = 7;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][0][1][1][1][1] = 8;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][1][1][1][0][0] = 9;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][0][1][1][0][1] = 10;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][0][1][1][1][0] = 11;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][1][1][1][0][0] = 12;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][0][1][1][0][0] = 13;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][0][1][1][0][0] = 14;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][1][1][1][1][1] = 15;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][1][1][0][1][1] = 16;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][0][1][0][1][1] = 17;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][1][1][0][0][1] = 18;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][1][1][0][1][0] = 19;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][1][1][0][0][1] = 20;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][0][1][0][0][1] = 21;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][1][1][0][1][0] = 22;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][0][1][0][1][0] = 23;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][0][1][0][1][1] = 24;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][1][1][0][0][0] = 25;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][0][1][0][0][1] = 26;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][0][1][0][1][0] = 27;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][1][1][0][0][0] = 28;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][0][1][0][0][0] = 29;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][0][1][0][0][0] = 30;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][1][1][0][1][1] = 31;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][1][0][1][1][1] = 32;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][0][0][1][1][1] = 33;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][1][0][1][0][1] = 34;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][1][0][1][1][0] = 35;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][1][0][1][0][1] = 36;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][0][0][1][0][1] = 37;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][1][0][1][1][0] = 38;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][0][0][1][1][0] = 39;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][0][0][1][1][1] = 40;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][1][0][1][0][0] = 41;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][0][0][1][0][1] = 42;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][0][0][1][1][0] = 43;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][1][0][1][0][0] = 44;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][0][0][1][0][0] = 45;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][0][0][1][0][0] = 46;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][1][0][1][1][1] = 47;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][1][0][0][1][1] = 48;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][0][0][0][1][1] = 49;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][1][0][0][0][1] = 50;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][1][0][0][1][0] = 51;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][1][0][0][0][1] = 52;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][0][0][0][0][1] = 53;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][1][0][0][1][0] = 54;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][0][0][0][1][0] = 55;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][0][0][0][1][1] = 56;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][1][0][0][0][0] = 57;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][0][0][0][0][1] = 58;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][0][0][0][1][0] = 59;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][1][0][0][0][0] = 60;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][0][0][0][0][0] = 61;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][0][0][0][0][0][0] = 62;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.X.ordinal()][1][1][0][0][1][1] = 63;
//
//        // Y AXIS SETUP
//        for (int i = 0; i < 64; i++) {
//            Quat4f rotation = new Quat4f(0, 0, 0, 1);
//            rotation.mul(ModelReference.rotationForAxis(Axis.Y, -ROTATION_LOOKUP_Y[i].yCoord));
//            rotation.mul(ModelReference.rotationForAxis(Axis.X, ROTATION_LOOKUP_Y[i].xCoord));
//            ROTATION_LOOKUPS[EnumFacing.Axis.Y.ordinal()][i] = TRSRTransformation.blockCenterToCorner(new TRSRTransformation(null, rotation, null, null));
//        }
//
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][1][0][1][1][1] = 0;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][1][1][0][1][1] = 1;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][1][1][1][0][1] = 2;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][1][1][1][1][0] = 3;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][1][0][1][0][1] = 4;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][1][1][0][0][1] = 5;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][1][0][1][1][0] = 6;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][1][1][0][1][0] = 7;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][1][0][0][1][1] = 8;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][1][1][1][0][0] = 9;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][1][0][0][0][1] = 10;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][1][0][0][1][0] = 11;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][1][0][1][0][0] = 12;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][1][1][0][0][0] = 13;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][1][0][0][0][0] = 14;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][1][1][1][1][1] = 15;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][1][0][1][1][1] = 16;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][1][1][0][1][1] = 17;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][1][1][1][0][1] = 18;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][1][1][1][1][0] = 19;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][1][0][1][0][1] = 20;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][1][1][0][0][1] = 21;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][1][0][1][1][0] = 22;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][1][1][0][1][0] = 23;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][1][0][0][1][1] = 24;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][1][1][1][0][0] = 25;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][1][0][0][0][1] = 26;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][1][0][0][1][0] = 27;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][1][0][1][0][0] = 28;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][1][1][0][0][0] = 29;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][1][0][0][0][0] = 30;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][1][1][1][1][1] = 31;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][0][0][1][1][1] = 32;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][0][1][0][1][1] = 33;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][0][1][1][0][1] = 34;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][0][1][1][1][0] = 35;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][0][0][1][0][1] = 36;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][0][1][0][0][1] = 37;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][0][0][1][1][0] = 38;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][0][1][0][1][0] = 39;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][0][0][0][1][1] = 40;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][0][1][1][0][0] = 41;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][0][0][0][0][1] = 42;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][0][0][0][1][0] = 43;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][0][0][1][0][0] = 44;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][0][1][0][0][0] = 45;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][0][0][0][0][0] = 46;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][1][0][1][1][1][1] = 47;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][0][0][1][1][1] = 48;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][0][1][0][1][1] = 49;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][0][1][1][0][1] = 50;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][0][1][1][1][0] = 51;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][0][0][1][0][1] = 52;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][0][1][0][0][1] = 53;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][0][0][1][1][0] = 54;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][0][1][0][1][0] = 55;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][0][0][0][1][1] = 56;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][0][1][1][0][0] = 57;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][0][0][0][0][1] = 58;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][0][0][0][1][0] = 59;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][0][0][1][0][0] = 60;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][0][1][0][0][0] = 61;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][0][0][0][0][0] = 62;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Y.ordinal()][0][0][1][1][1][1] = 63;
//
//        // Z AXIS SETUP
//        for (int i = 0; i < 64; i++) {
//            Quat4f rotation = new Quat4f(0, 0, 0, 1);
//            rotation.mul(ModelReference.rotationForAxis(Axis.Z, -ROTATION_LOOKUP_Y[i].yCoord));
//            rotation.mul(ModelReference.rotationForAxis(Axis.X, ROTATION_LOOKUP_Y[i].xCoord + 90));
//            rotation.mul(ModelReference.rotationForAxis(Axis.Y, 180.0));
//            ROTATION_LOOKUPS[EnumFacing.Axis.Z.ordinal()][i] = TRSRTransformation.blockCenterToCorner(new TRSRTransformation(null, rotation, null, null));
//        }
//
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][1][1][0][1][1] = 0;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][1][0][1][1][1] = 1;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][0][1][1][1][1] = 2;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][1][1][1][1][1] = 3;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][0][1][0][1][1] = 4;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][0][0][1][1][1] = 5;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][1][1][0][1][1] = 6;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][1][0][1][1][1] = 7;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][1][0][0][1][1] = 8;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][0][1][1][1][1] = 9;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][0][0][0][1][1] = 10;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][1][0][0][1][1] = 11;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][0][1][0][1][1] = 12;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][0][0][1][1][1] = 13;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][0][0][0][1][1] = 14;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][1][1][1][1][1] = 15;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][1][1][0][1][0] = 16;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][1][0][1][1][0] = 17;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][0][1][1][1][0] = 18;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][1][1][1][1][0] = 19;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][0][1][0][1][0] = 20;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][0][0][1][1][0] = 21;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][1][1][0][1][0] = 22;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][1][0][1][1][0] = 23;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][1][0][0][1][0] = 24;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][0][1][1][1][0] = 25;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][0][0][0][1][0] = 26;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][1][0][0][1][0] = 27;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][0][1][0][1][0] = 28;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][0][0][1][1][0] = 29;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][0][0][0][1][0] = 30;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][1][1][1][1][0] = 31;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][1][1][0][0][1] = 32;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][1][0][1][0][1] = 33;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][0][1][1][0][1] = 34;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][1][1][1][0][1] = 35;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][0][1][0][0][1] = 36;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][0][0][1][0][1] = 37;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][1][1][0][0][1] = 38;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][1][0][1][0][1] = 39;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][1][0][0][0][1] = 40;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][0][1][1][0][1] = 41;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][0][0][0][0][1] = 42;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][1][0][0][0][1] = 43;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][0][1][0][0][1] = 44;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][0][0][1][0][1] = 45;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][0][0][0][0][1] = 46;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][1][1][1][0][1] = 47;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][1][1][0][0][0] = 48;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][1][0][1][0][0] = 49;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][0][1][1][0][0] = 50;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][1][1][1][0][0] = 51;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][0][1][0][0][0] = 52;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][0][0][1][0][0] = 53;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][1][1][0][0][0] = 54;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][1][0][1][0][0] = 55;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][1][0][0][0][0] = 56;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][0][1][1][0][0] = 57;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][0][0][0][0][0] = 58;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][1][0][0][0][0] = 59;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][0][1][0][0][0] = 60;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][0][0][1][0][0] = 61;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][0][0][0][0][0][0] = 62;
//        SHAPE_INDEX_LOOKUPS[EnumFacing.Axis.Z.ordinal()][1][1][1][1][0][0] = 63;
//    }
    
    public static enum ModelType
    {
        NORMAL,
        LAMP_BASE,
        LAMP_OVERLAY;
    }
}
