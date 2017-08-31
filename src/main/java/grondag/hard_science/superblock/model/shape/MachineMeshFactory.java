package grondag.hard_science.superblock.model.shape;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.library.render.CubeInputs;
import grondag.hard_science.library.render.RawQuad;
import grondag.hard_science.library.world.Rotation;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.collision.CubeCollisionHandler;
import grondag.hard_science.superblock.collision.ICollisionHandler;
import grondag.hard_science.superblock.collision.SideShape;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.model.state.StateFormat;
import grondag.hard_science.superblock.model.state.Surface;
import grondag.hard_science.superblock.model.state.Surface.SurfaceInstance;
import grondag.hard_science.superblock.model.state.SurfaceTopology;
import grondag.hard_science.superblock.model.state.SurfaceType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Square machines use lamp surface as the control face
 * and main surface as the casing sides.
 */
public class MachineMeshFactory extends ShapeMeshGenerator
{
    private static ShapeMeshGenerator instance;
    

    public static ShapeMeshGenerator getShapeMeshFactory()
    {
        if(instance == null) instance = new MachineMeshFactory();
        return instance; 
    };
    
    /** never changes so may as well save it */
    private final List<RawQuad> quads0;
    private final List<RawQuad> quads90;
    private final List<RawQuad> quads180;
    private final List<RawQuad> quads270;
    
    private static final Surface mainSurface = new Surface(SurfaceType.MAIN, SurfaceTopology.CUBIC);
    private static final Surface symbolSurface = new Surface(SurfaceType.MAIN, SurfaceTopology.CUBIC);
    private static final Surface lampSurface = new Surface(SurfaceType.LAMP, SurfaceTopology.CUBIC);
    
    private static final SurfaceInstance mainInstance = mainSurface.unitInstance;
    private static final SurfaceInstance lampInstance = lampSurface.unitInstance.withAllowBorders(false);
    
    
    protected MachineMeshFactory()
    {
        super(StateFormat.BLOCK, ModelState.STATE_FLAG_HAS_AXIS_ROTATION, 
                mainSurface, symbolSurface, lampSurface); 
        
        this.quads0 = getCubeQuads(Rotation.ROTATE_NONE);
        this.quads90 = getCubeQuads(Rotation.ROTATE_90);
        this.quads180 = getCubeQuads(Rotation.ROTATE_180);
        this.quads270 = getCubeQuads(Rotation.ROTATE_270);
    }

    @Override
    public List<RawQuad> getShapeQuads(ModelState modelState)
    {
        switch(modelState.getAxisRotation())
        {
        case ROTATE_NONE:
            return this.quads0;
        case ROTATE_90:
            return this.quads90;
        case ROTATE_180:
            return this.quads180;
        case ROTATE_270:
            return this.quads270;
        default:
            return Collections.emptyList();
        }
    }
    
    private List<RawQuad> getCubeQuads(Rotation rotation)
    {
        CubeInputs result = new CubeInputs();
        result.color = 0xFFFFFFFF;
        result.textureRotation = Rotation.ROTATE_NONE;
        result.isFullBrightness = false;
        result.u0 = 0;
        result.v0 = 0;
        result.u1 = 16;
        result.v1 = 16;
        result.isOverlay = false;
        
        RawQuad template = new RawQuad();
        template.color = 0xFFFFFFFF;
        template.rotation = Rotation.ROTATE_NONE;
        template.isFullBrightness = false;
        template.minU = 0;
        template.minV = 0;
        template.maxU = 16;
        template.maxV = 16;
        
        ImmutableList.Builder<RawQuad> builder = new ImmutableList.Builder<RawQuad>();
       
        for(EnumFacing face : EnumFacing.VALUES)
        {
            result.surfaceInstance = face == rotation.horizontalFace  ? lampInstance : mainInstance;
            builder.add(result.makeRawFace(face));
        }
        return builder.build();
    }


    @Override
    public boolean isCube(ModelState modelState)
    {
        return true;
    }

    @Override
    public boolean rotateBlock(IBlockState blockState, World world, BlockPos pos, EnumFacing axis, SuperBlock block, ModelState modelState)
    {
        return false;
        
        // does not work, see comments in SuperBlock parent method
//        modelState.setAxisRotation(modelState.getAxisRotation().clockwise());
//        return true;
    }

    @Override
    public int geometricSkyOcclusion(ModelState modelState)
    {
        return 255;
    }

    @Override
    public ICollisionHandler collisionHandler()
    {
        return CubeCollisionHandler.INSTANCE;
    }

    @Override
    public SideShape sideShape(ModelState modelState, EnumFacing side)
    {
        return SideShape.SOLID;
    }
}
