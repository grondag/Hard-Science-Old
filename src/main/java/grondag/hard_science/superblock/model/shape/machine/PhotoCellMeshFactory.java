package grondag.hard_science.superblock.model.shape.machine;

import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;

import grondag.exotic_matter.block.ISuperBlock;
import grondag.exotic_matter.model.collision.ICollisionHandler;
import grondag.exotic_matter.model.primitives.better.IMutablePolygon;
import grondag.exotic_matter.model.primitives.better.IPolygon;
import grondag.exotic_matter.model.primitives.better.PolyFactory;
import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.exotic_matter.model.state.ModelStateData;
import grondag.exotic_matter.model.varia.SideShape;
import grondag.exotic_matter.world.Rotation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Square machines use lamp surface as the control face
 * and main surface as the casing sides.
 */
public class PhotoCellMeshFactory extends AbstractMachineMeshGenerator implements ICollisionHandler
{
    private final float height;
    private final AxisAlignedBB AABB;
    
    public PhotoCellMeshFactory(float height)
    {
        super(ModelStateData.STATE_FLAG_NONE); 
        this.height = height;
        this.AABB = new AxisAlignedBB(0, 0, 0, 1, height, 1);
    }
    
    /**
     * Top surface is main surface (so can support borders if wanted).
     * Sides and bottom are lamp surface. 
     */
    @Override
    public void produceShapeQuads(ISuperModelState modelState, Consumer<IPolygon> target)
    {
        IMutablePolygon quad = PolyFactory.newPaintable(4);
        quad.setRotation(0, Rotation.ROTATE_NONE);
        quad.setLockUV(0, true);

        quad.setSurface(MachineMeshFactory.SURFACE_MAIN);
        quad.setNominalFace(EnumFacing.UP);
        quad.setupFaceQuad(0, 0, 1, 1, 1 - height, EnumFacing.NORTH);
        target.accept(quad.toPainted());
      
        for(EnumFacing face : EnumFacing.Plane.HORIZONTAL.facings())
        {
            quad.setSurface(MachineMeshFactory.SURFACE_LAMP);
            quad.setNominalFace(face);
            quad.setupFaceQuad( 0, 0, 1, height, 0, EnumFacing.UP);
            target.accept(quad.toPainted());
        }
        
        quad.setSurface(MachineMeshFactory.SURFACE_LAMP);
        quad.setNominalFace(EnumFacing.DOWN);
        quad.setupFaceQuad(0, 0, 1, 1, 0, EnumFacing.NORTH);
        target.accept(quad.toPainted());
        
        quad.release();
    }
   
    @Override
    public boolean isCube(ISuperModelState modelState)
    {
        return false;
    }

    @Override
    public boolean rotateBlock(IBlockState blockState, World world, BlockPos pos, EnumFacing axis, ISuperBlock block, ISuperModelState modelState)
    {
        return false;
    }

    @Override
    public int geometricSkyOcclusion(ISuperModelState modelState)
    {
        return 255;
    }

    @Override
    public SideShape sideShape(ISuperModelState modelState, EnumFacing side)
    {
        return side == EnumFacing.DOWN ? SideShape.SOLID : SideShape.MISSING;
    }
    
    @Override
    public ICollisionHandler collisionHandler()
    {
        return this;
    }

    @Override
    public List<AxisAlignedBB> getCollisionBoxes(ISuperModelState modelState)
    {
        return ImmutableList.of(getCollisionBoundingBox(modelState));
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(ISuperModelState modelState)
    {
        return AABB;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox(ISuperModelState modelState)
    {
        return getCollisionBoundingBox(modelState);
    }

    @Override
    public boolean hasLampSurface(ISuperModelState modelState)
    {
        return true;
    }
}
