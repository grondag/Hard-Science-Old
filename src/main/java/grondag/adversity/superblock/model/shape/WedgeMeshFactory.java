package grondag.adversity.superblock.model.shape;

import static grondag.adversity.superblock.model.state.ModelStateFactory.ModelState.*;

import java.util.List;

import javax.vecmath.Matrix4d;

import com.google.common.collect.ImmutableList;

import grondag.adversity.library.render.FaceVertex;
import grondag.adversity.library.render.LightingMode;
import grondag.adversity.library.render.RawQuad;
import grondag.adversity.library.world.Rotation;
import grondag.adversity.superblock.block.SuperBlock;
import grondag.adversity.superblock.collision.CollisionBoxDispatcher;
import grondag.adversity.superblock.collision.ICollisionHandler;
import grondag.adversity.superblock.collision.SideShape;
import grondag.adversity.superblock.model.state.Surface;
import grondag.adversity.superblock.model.state.SurfaceTopology;
import grondag.adversity.superblock.model.state.SurfaceType;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.model.state.StateFormat;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WedgeMeshFactory extends ShapeMeshGenerator implements ICollisionHandler
{
    private static ShapeMeshGenerator instance;
    
    private static Surface BACK_AND_BOTTOM = new Surface(SurfaceType.MAIN, SurfaceTopology.CUBIC);
    private static Surface SIDES = new Surface(SurfaceType.CUT, SurfaceTopology.CUBIC);
    private static Surface TOP = new Surface(SurfaceType.CUT, SurfaceTopology.TILED);
    
    public static ShapeMeshGenerator getShapeMeshFactory()
    {
        if(instance == null) instance = new WedgeMeshFactory();
        return instance; 
    }
    
    private WedgeMeshFactory()
    {
        super(StateFormat.BLOCK, 
                STATE_FLAG_NEEDS_SPECIES | STATE_FLAG_HAS_AXIS | STATE_FLAG_HAS_AXIS_ORIENTATION | STATE_FLAG_HAS_MODEL_ROTATION,
                BACK_AND_BOTTOM, SIDES, TOP);
    }
 
    private List<RawQuad> makeQuads(Matrix4d matrix)
    {
        RawQuad template = new RawQuad();
        template.color = 0xFFFFFFFF;
        template.rotation = Rotation.ROTATE_NONE;
        template.lightingMode = LightingMode.SHADED;
        template.lockUV = true;

        ImmutableList.Builder<RawQuad> builder = new ImmutableList.Builder<RawQuad>();
        
        RawQuad quad = template.clone();
        quad.surfaceInstance = BACK_AND_BOTTOM.unitInstance;
        quad.setFace(EnumFacing.NORTH);
        quad.setupFaceQuad(0.0, 0.0, 1.0, 1.0, 0, EnumFacing.UP);
        builder.add(quad.transform(matrix));
      
        quad = template.clone();
        quad.surfaceInstance = BACK_AND_BOTTOM.unitInstance;
        quad.setFace(EnumFacing.DOWN);
        quad.setupFaceQuad(0.0, 0.0, 1.0, 1.0, 0.0, EnumFacing.NORTH);
        builder.add(quad.transform(matrix));
        
        quad = template.clone();
        quad.surfaceInstance = SIDES.unitInstance;
        quad.setFace(EnumFacing.EAST);
        quad.setupFaceQuad(EnumFacing.EAST,
                new FaceVertex(0, 0, 0),
                new FaceVertex(1, 0, 0),
                new FaceVertex(1, 1, 0), 
                EnumFacing.UP);
        builder.add(quad.transform(matrix));
        
        quad = template.clone();
        quad.surfaceInstance = SIDES.unitInstance;
        quad.setFace(EnumFacing.WEST);
        quad.setupFaceQuad(EnumFacing.WEST,
                new FaceVertex(0, 0, 0),
                new FaceVertex(1, 0, 0),
                new FaceVertex(0, 1, 0), 
                EnumFacing.UP);
        builder.add(quad.transform(matrix));
        
        
        
        return builder.build();
    }
    
    @Override
    public List<RawQuad> getShapeQuads(ModelState modelState)
    {
        return this.makeQuads(getMatrix4d(modelState));
    }

    @Override
    public boolean isCube(ModelState modelState)
    {
        return false;
    }

    @Override
    public boolean rotateBlock(IBlockState blockState, World world, BlockPos pos, EnumFacing axis, SuperBlock block, ModelState modelState)
    {
        //FIXME
        return false;
    }

    @Override
    public int geometricSkyOcclusion(ModelState modelState)
    {
        return modelState.getAxis() == EnumFacing.Axis.Y ? 255 : 7;
    }

    @Override
    public ICollisionHandler collisionHandler()
    {
        return this;
    }

    @Override
    public SideShape sideShape(ModelState modelState, EnumFacing side)
    {
        //FIXME
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
}
