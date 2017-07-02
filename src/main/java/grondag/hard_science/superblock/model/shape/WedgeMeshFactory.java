package grondag.hard_science.superblock.model.shape;

import static grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState.*;

import java.util.List;

import javax.vecmath.Matrix4d;
import com.google.common.collect.ImmutableList;

import grondag.hard_science.library.render.FaceVertex;
import grondag.hard_science.library.render.LightingMode;
import grondag.hard_science.library.render.RawQuad;
import grondag.hard_science.library.world.Rotation;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.collision.CollisionBoxDispatcher;
import grondag.hard_science.superblock.collision.ICollisionHandler;
import grondag.hard_science.superblock.collision.SideShape;
import grondag.hard_science.superblock.model.state.StateFormat;
import grondag.hard_science.superblock.model.state.Surface;
import grondag.hard_science.superblock.model.state.SurfaceTopology;
import grondag.hard_science.superblock.model.state.SurfaceType;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
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
    private static Surface SIDES = new Surface(SurfaceType.MAIN, SurfaceTopology.CUBIC);
    private static Surface TOP = new Surface(SurfaceType.MAIN, SurfaceTopology.CUBIC);
    
    public static ShapeMeshGenerator getShapeMeshFactory()
    {
        if(instance == null) instance = new WedgeMeshFactory();
        return instance; 
    }
    
    private WedgeMeshFactory()
    {
        super(StateFormat.BLOCK, 
                STATE_FLAG_NEEDS_SPECIES | STATE_FLAG_HAS_AXIS | STATE_FLAG_HAS_MODEL_ROTATION,
                BACK_AND_BOTTOM, SIDES, TOP);
    }
    
    @Override
    public List<RawQuad> getShapeQuads(ModelState modelState)
    {
        
        // Axis for this shape is along the face of the sloping surface
        // Four rotations x 3 axes gives 12 orientations - one for each edge of a cube.
        // Default geometry is Y axis with full sides against north/east faces.
        
        Matrix4d matrix = getMatrix4d(modelState);
        
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
        quad.setFace(EnumFacing.EAST);
        quad.setupFaceQuad(0.0, 0.0, 1.0, 1.0, 0.0, EnumFacing.UP);
        builder.add(quad.transform(matrix));
        
        quad = template.clone();
        quad.surfaceInstance = SIDES.unitInstance;
        quad.setFace(EnumFacing.UP);
        quad.setupFaceQuad(EnumFacing.UP,
                new FaceVertex(0, 1, 0),
                new FaceVertex(1, 0, 0),
                new FaceVertex(1, 1, 0), 
                EnumFacing.NORTH);
        builder.add(quad.transform(matrix));
        
        quad = template.clone();
        quad.surfaceInstance = SIDES.unitInstance;
        quad.setFace(EnumFacing.DOWN);
        quad.setupFaceQuad(EnumFacing.DOWN,
                new FaceVertex(0, 0, 0),
                new FaceVertex(1, 1, 0),
                new FaceVertex(0, 1, 0), 
                EnumFacing.NORTH);
        builder.add(quad.transform(matrix));
        
        // get position within surface for bigtex according to axis 

        // in default model, texture flows with u,v mapped to x, -y
//        Vector4d uVec = new Vector4d(1, 0, 0, 1);
//        Vector4d vVec = new Vector4d(0, -1, 0, 1);
//        
//        matrix.transform(uVec);
//        matrix.transform(vVec);
        
        
        
        
        // note that x, y, z are already limited to 0-31 for block-type model state
//        int uPos = (int) Math.round(uVec.x * modelState.getPosX() + uVec.y * modelState.getPosY() + uVec.z * modelState.getPosZ()); 
//        int vPos = (int) Math.round(vVec.x * modelState.getPosX() + vVec.y * modelState.getPosY() + vVec.z * modelState.getPosZ()); 
//        int uStep = (int)Math.round(uVec.x + uVec.y + uVec.z);
//        int vStep = (int)Math.round(vVec.x + vVec.y + vVec.z);
        
        quad = template.clone();
        quad.surfaceInstance = TOP.newInstance(true);
        quad.setFace(EnumFacing.SOUTH);
        quad.setupFaceQuad(EnumFacing.SOUTH,
                new FaceVertex(0, 0, 1),
                new FaceVertex(1, 0, 0),
                new FaceVertex(1, 1, 0), 
                new FaceVertex(0, 1, 1), 
                EnumFacing.UP);
//        quad.minU = uStep < 0 ? 32 - uPos : uPos;
//        quad.maxU = quad.minU + uStep;
//        quad.minV = vStep < 0 ? 32 - vPos : vPos;
//        quad.maxV = quad.minV + vStep;
        builder.add(quad.transform(matrix));
        
        
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
        //FIXME
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
        return SideShape.PARTIAL;
        //FIXME
//        if(modelState.getMetaData() ==15) return SideShape.SOLID;
//        
//        if(side.getAxis() == modelState.getAxis())
//        {
//            return (side.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE) == modelState.isAxisInverted()
//                    ? SideShape.SOLID : SideShape.MISSING;
//        }
//        else
//        {
//            return modelState.getMetaData() > 8 ? SideShape.PARTIAL : SideShape.MISSING;
//        }
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
