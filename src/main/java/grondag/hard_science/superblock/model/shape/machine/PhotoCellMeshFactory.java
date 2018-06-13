package grondag.hard_science.superblock.model.shape.machine;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.exotic_matter.block.ISuperBlock;
import grondag.exotic_matter.model.primitives.IMutablePolygon;
import grondag.exotic_matter.model.primitives.IPolygon;
import grondag.exotic_matter.model.primitives.Poly;
import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.exotic_matter.model.state.ModelStateData;
import grondag.exotic_matter.model.varia.ICollisionHandler;
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
    public List<IPolygon> getShapeQuads(ISuperModelState modelState)
    {
        IMutablePolygon template = Poly.mutable(4);
        template.setColor(0xFFFFFFFF);
        template.setRotation(Rotation.ROTATE_NONE);
        template.setLockUV(true);

        ImmutableList.Builder<IPolygon> builder = ImmutableList.builder();
        
        IMutablePolygon quad = Poly.mutableCopyOf(template);
        quad.setSurfaceInstance(MachineMeshFactory.SURFACE_MAIN);
        quad.setNominalFace(EnumFacing.UP);
        quad.setupFaceQuad(0, 0, 1, 1, 1 - height, EnumFacing.NORTH);
        builder.add(quad);
      
        for(EnumFacing face : EnumFacing.Plane.HORIZONTAL.facings())
        {
            quad = Poly.mutableCopyOf(template);
            quad.setSurfaceInstance(MachineMeshFactory.SURFACE_LAMP);
            quad.setNominalFace(face);
            quad.setupFaceQuad( 0, 0, 1, height, 0, EnumFacing.UP);
            builder.add(quad);
        }
        
        quad = Poly.mutableCopyOf(template);
        quad.setSurfaceInstance(MachineMeshFactory.SURFACE_LAMP);
        quad.setNominalFace(EnumFacing.DOWN);
        quad.setupFaceQuad(0, 0, 1, 1, 0, EnumFacing.NORTH);
        builder.add(quad);
        
        return builder.build();
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
