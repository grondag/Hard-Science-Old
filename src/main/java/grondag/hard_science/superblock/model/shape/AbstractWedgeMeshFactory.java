package grondag.hard_science.superblock.model.shape;

import static grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState.STATE_FLAG_HAS_AXIS;
import static grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState.STATE_FLAG_HAS_AXIS_ROTATION;
import static grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState.STATE_FLAG_NEEDS_SPECIES;

import java.util.List;

import grondag.hard_science.library.world.BlockCorner;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.collision.CollisionBoxDispatcher;
import grondag.hard_science.superblock.collision.ICollisionHandler;
import grondag.hard_science.superblock.collision.SideShape;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.model.state.StateFormat;
import grondag.hard_science.superblock.model.state.Surface;
import grondag.hard_science.superblock.model.state.Surface.SurfaceInstance;
import grondag.hard_science.superblock.model.state.SurfaceTopology;
import grondag.hard_science.superblock.model.state.SurfaceType;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class AbstractWedgeMeshFactory extends ShapeMeshGenerator implements ICollisionHandler
{

    private static Surface BACK_AND_BOTTOM = new Surface(SurfaceType.MAIN, SurfaceTopology.CUBIC);
    private static Surface SIDES = new Surface(SurfaceType.MAIN, SurfaceTopology.CUBIC);
    private static Surface TOP = new Surface(SurfaceType.MAIN, SurfaceTopology.CUBIC);
    
    protected static SurfaceInstance BACK_AND_BOTTOM_INSTANCE = BACK_AND_BOTTOM.unitInstance;
    protected static SurfaceInstance SIDE_INSTANCE = SIDES.unitInstance.withAllowBorders(false);
    
    // salt is for stairs, so cuts appear different from top/front face
    // wedges can't connect textures with adjacent flat blocks consistently anyway, so doesn't hurt them
    protected static SurfaceInstance TOP_INSTANCE = TOP.unitInstance.withIgnoreDepthForRandomization(true).withAllowBorders(false).withTextureSalt(1);
    

    public AbstractWedgeMeshFactory()
    {
        super(StateFormat.BLOCK, 
                STATE_FLAG_NEEDS_SPECIES | STATE_FLAG_HAS_AXIS | STATE_FLAG_HAS_AXIS_ROTATION,
                BACK_AND_BOTTOM, SIDES, TOP);
    }

    @Override
    public boolean isCube(ModelState modelState)
    {
        return false;
    }

    @Override
    public boolean rotateBlock(IBlockState blockState, World world, BlockPos pos, EnumFacing axis, SuperBlock block, ModelState modelState)
    {
        // not currently implemented - ambivalent about it
        return false;
    }

    @Override
    public int geometricSkyOcclusion(ModelState modelState)
    {
        return modelState.getAxis() == EnumFacing.Axis.Y ? 7 : 255;
    }

    @Override
    public ICollisionHandler collisionHandler()
    {
        return this;
    }

    @Override
    public SideShape sideShape(ModelState modelState, EnumFacing side)
    {
        BlockCorner corner = BlockCorner.find(modelState.getAxis(), modelState.getAxisRotation());
        return side == corner.face1 || side == corner.face2 ? SideShape.SOLID : SideShape.MISSING;
    }

    @Override
    public List<AxisAlignedBB> getCollisionBoxes(ModelState modelState)
    {
        return CollisionBoxDispatcher.INSTANCE.getCollisionBoxes(modelState);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(ModelState modelState)
    {
        return Block.FULL_BLOCK_AABB;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox(ModelState modelState)
    {
        return Block.FULL_BLOCK_AABB;
    }

    @Override
    public boolean isAxisOrthogonalToPlacementFace()
    { return true; }

}