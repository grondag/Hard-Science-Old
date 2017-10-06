package grondag.hard_science.superblock.model.shape.machine;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.library.render.RawQuad;
import grondag.hard_science.library.world.Rotation;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.collision.ICollisionHandler;
import grondag.hard_science.superblock.collision.SideShape;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.model.shape.MachineMeshFactory;
import grondag.hard_science.superblock.model.shape.ShapeMeshGenerator;
import grondag.hard_science.superblock.model.state.StateFormat;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Square machines use lamp surface as the control face
 * and main surface as the casing sides.
 */
public class SolarCellMeshFactory extends ShapeMeshGenerator implements ICollisionHandler
{
    
    public SolarCellMeshFactory()
    {
        super(StateFormat.BLOCK, ModelState.STATE_FLAG_HAS_AXIS_ROTATION, 
                MachineMeshFactory.SURFACE_MAIN, MachineMeshFactory.SURFACE_LAMP); 
    }
    
    /**
     * Top surface is main surface (so can support borders if wanted).
     * Sides and bottom are lamp surface. 
     */
    @Override
    public List<RawQuad> getShapeQuads(ModelState modelState)
    {
        RawQuad template = new RawQuad();
        template.color = 0xFFFFFFFF;
        template.rotation = Rotation.ROTATE_NONE;
        template.isFullBrightness = false;
        template.lockUV = true;

        ImmutableList.Builder<RawQuad> builder = new ImmutableList.Builder<RawQuad>();
        
        RawQuad quad = template.clone();
        quad.surfaceInstance = MachineMeshFactory.INSTANCE_MAIN;
        quad.setFace(EnumFacing.UP);
        quad.setupFaceQuad(0.0, 0.0, 1.0, 1.0, 0.5, EnumFacing.NORTH);
        builder.add(quad);
      
        for(EnumFacing face : EnumFacing.Plane.HORIZONTAL.facings())
        {
            quad = template.clone();
            quad.surfaceInstance = MachineMeshFactory.INSTANCE_LAMP;
            quad.setFace(face);
            quad.setupFaceQuad( 0.0, 0.0, 1.0, 0.5, 0.0, EnumFacing.UP);
            builder.add(quad);
        }
        
        quad = template.clone();
        quad.surfaceInstance = MachineMeshFactory.INSTANCE_LAMP;
        quad.setFace(EnumFacing.DOWN);
        quad.setupFaceQuad(0.0, 0.0, 1.0, 1.0, 0.0, EnumFacing.NORTH);
        builder.add(quad);
        
        return builder.build();
    }
   
    @Override
    public boolean isCube(ModelState modelState)
    {
        return false;
    }

    @Override
    public boolean rotateBlock(IBlockState blockState, World world, BlockPos pos, EnumFacing axis, SuperBlock block, ModelState modelState)
    {
        return false;
    }

    @Override
    public int geometricSkyOcclusion(ModelState modelState)
    {
        return 255;
    }

    @Override
    public SideShape sideShape(ModelState modelState, EnumFacing side)
    {
        return side == EnumFacing.DOWN ? SideShape.SOLID : SideShape.MISSING;
    }
    
    @Override
    public ICollisionHandler collisionHandler()
    {
        return this;
    }

    @Override
    public List<AxisAlignedBB> getCollisionBoxes(ModelState modelState)
    {
        return ImmutableList.of(getCollisionBoundingBox(modelState));
    }

    private static final AxisAlignedBB AABB = new AxisAlignedBB(0, 0, 0, 1, 0.5f, 1);
    @Override
    public AxisAlignedBB getCollisionBoundingBox(ModelState modelState)
    {
        return AABB;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox(ModelState modelState)
    {
        return getCollisionBoundingBox(modelState);
    }
}
