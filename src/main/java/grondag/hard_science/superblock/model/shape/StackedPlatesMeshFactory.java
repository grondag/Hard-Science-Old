package grondag.hard_science.superblock.model.shape;

import static grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState.*;

import java.util.List;

import javax.vecmath.Matrix4d;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.library.render.RawQuad;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.library.world.Rotation;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.collision.ICollisionHandler;
import grondag.hard_science.superblock.collision.SideShape;
import grondag.hard_science.superblock.model.state.StateFormat;
import grondag.hard_science.superblock.model.state.Surface;
import grondag.hard_science.superblock.model.state.SurfaceTopology;
import grondag.hard_science.superblock.model.state.SurfaceType;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StackedPlatesMeshFactory extends ShapeMeshGenerator implements ICollisionHandler
{
    private static ShapeMeshGenerator instance;
    
    private static Surface TOP_AND_BOTTOM = new Surface(SurfaceType.MAIN, SurfaceTopology.CUBIC);
    private static Surface SIDES = new Surface(SurfaceType.CUT, SurfaceTopology.CUBIC);
    
    public static ShapeMeshGenerator getShapeMeshFactory()
    {
        if(instance == null) instance = new StackedPlatesMeshFactory();
        return instance; 
    }
    
    private StackedPlatesMeshFactory()
    {
        super(StateFormat.BLOCK, 
                STATE_FLAG_NEEDS_SPECIES | STATE_FLAG_HAS_AXIS | STATE_FLAG_HAS_AXIS_ORIENTATION,
                TOP_AND_BOTTOM, SIDES);
    }
 
    private List<RawQuad> makeQuads(int meta, Matrix4d matrix)
    {
        double height = (meta + 1) / 16.0;
        
        RawQuad template = new RawQuad();
        template.color = 0xFFFFFFFF;
        template.rotation = Rotation.ROTATE_NONE;
        template.isFullBrightness = false;
        template.lockUV = true;

        ImmutableList.Builder<RawQuad> builder = new ImmutableList.Builder<RawQuad>();
        
        RawQuad quad = template.clone();
        quad.surfaceInstance = TOP_AND_BOTTOM.unitInstance;
        quad.setFace(EnumFacing.UP);
        quad.setupFaceQuad(0.0, 0.0, 1.0, 1.0, 1-height, EnumFacing.NORTH);
        builder.add(quad.transform(matrix));
      
        for(EnumFacing face : EnumFacing.Plane.HORIZONTAL.facings())
        {
            quad = template.clone();
            quad.surfaceInstance = SIDES.unitInstance;
            quad.setFace(face);
            quad.setupFaceQuad( 0.0, 0.0, 1.0, height, 0.0, EnumFacing.UP);
            builder.add(quad.transform(matrix));
        }
        
        quad = template.clone();
        quad.surfaceInstance = TOP_AND_BOTTOM.unitInstance;
        quad.setFace(EnumFacing.DOWN);
        quad.setupFaceQuad(0.0, 0.0, 1.0, 1.0, 0.0, EnumFacing.NORTH);
        builder.add(quad.transform(matrix));
        
        return builder.build();
    }
    
    @Override
    public boolean isAdditive()
    {
        return true;
    }

    @Override
    public List<RawQuad> getShapeQuads(ModelState modelState)
    {
        return this.makeQuads(modelState.getMetaData(), getMatrix4d(modelState));
    }

    @Override
    public boolean isCube(ModelState modelState)
    {
        return modelState.getMetaData() == 15;
    }

    @Override
    public boolean rotateBlock(IBlockState blockState, World world, BlockPos pos, EnumFacing axis, SuperBlock block, ModelState modelState)
    {
        return false;
    }

    @Override
    public int geometricSkyOcclusion(ModelState modelState)
    {
        return modelState.getAxis() == EnumFacing.Axis.Y ? 255 : modelState.getMetaData();
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

    @Override
    public AxisAlignedBB getCollisionBoundingBox(ModelState modelState)
    {
        return Useful.makeRotatedAABB(0, 0, 0, 1, (modelState.getMetaData() + 1) / 16f, 1, getMatrix4f(modelState));
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox(ModelState modelState)
    {
        return getCollisionBoundingBox(modelState);
    }

    @Override
    public SideShape sideShape(ModelState modelState, EnumFacing side)
    {
        if(modelState.getMetaData() ==15) return SideShape.SOLID;
        
        if(side.getAxis() == modelState.getAxis())
        {
            return (side.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE) == modelState.isAxisInverted()
                    ? SideShape.SOLID : SideShape.MISSING;
        }
        else
        {
            return modelState.getMetaData() > 8 ? SideShape.PARTIAL : SideShape.MISSING;
        }
    }
    
    @Override
    public int getMetaData(ModelState modelState)
    {
        return (int) (modelState.getStaticShapeBits() & 0xF);
    }

    @Override
    public void setMetaData(ModelState modelState, int meta)
    {
        modelState.setStaticShapeBits(meta);
    }
    
}