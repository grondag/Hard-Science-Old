package grondag.hard_science.superblock.model.shape.machine;

import java.util.function.Consumer;

import grondag.exotic_matter.block.ISuperBlock;
import grondag.exotic_matter.model.collision.CubeCollisionHandler;
import grondag.exotic_matter.model.collision.ICollisionHandler;
import grondag.exotic_matter.model.primitives.CubeInputs;
import grondag.exotic_matter.model.primitives.polygon.IMutablePolygon;
import grondag.exotic_matter.model.primitives.polygon.IPolygon;
import grondag.exotic_matter.model.primitives.stream.IPolyStream;
import grondag.exotic_matter.model.primitives.stream.IWritablePolyStream;
import grondag.exotic_matter.model.primitives.stream.PolyStreams;
import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.exotic_matter.model.state.ModelStateData;
import grondag.exotic_matter.model.varia.SideShape;
import grondag.exotic_matter.world.Rotation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Square machines use lamp surface as the control face
 * and main surface as the casing sides.
 */
public class MachineCubeMeshFactory extends AbstractMachineMeshGenerator
{
    /** never changes so may as well save it */
    private final IPolyStream[] cubeQuads = new IPolyStream[4];
    
    /**
     * @param hasFront If true, model will have an orientation and front display face
     */
    public MachineCubeMeshFactory(boolean hasFront)
    {
        super(hasFront ? ModelStateData.STATE_FLAG_HAS_AXIS_ROTATION : ModelStateData.STATE_FLAG_NONE); 
        
        cubeQuads[Rotation.ROTATE_NONE.ordinal()] = getCubeQuads(Rotation.ROTATE_NONE, hasFront);
        if(hasFront)
        {
            cubeQuads[Rotation.ROTATE_90.ordinal()] = getCubeQuads(Rotation.ROTATE_90, true);
            cubeQuads[Rotation.ROTATE_180.ordinal()] = getCubeQuads(Rotation.ROTATE_180, true);
            cubeQuads[Rotation.ROTATE_270.ordinal()] = getCubeQuads(Rotation.ROTATE_270, true);
        }
        else
        {
            cubeQuads[Rotation.ROTATE_90.ordinal()] = cubeQuads[Rotation.ROTATE_NONE.ordinal()];
            cubeQuads[Rotation.ROTATE_180.ordinal()] = cubeQuads[Rotation.ROTATE_NONE.ordinal()];
            cubeQuads[Rotation.ROTATE_270.ordinal()] = cubeQuads[Rotation.ROTATE_NONE.ordinal()];
        }
    }
    
    /**
     * Top surface is main surface (so can support borders if wanted).
     * Sides and bottom are lamp surface. 
     */
    @Override
    public void produceShapeQuads(ISuperModelState modelState, Consumer<IPolygon> target)
    {
        IPolyStream cachedQuads = cubeQuads[modelState.getAxisRotation().ordinal()];
        
        cachedQuads.origin();
        IPolygon reader = cachedQuads.reader();
        
        do
            target.accept(reader);
        while(cachedQuads.next());
    }
   
    private IPolyStream getCubeQuads(Rotation rotation, boolean hasFront)
    {
        CubeInputs cube = new CubeInputs();
        cube.color = 0xFFFFFFFF;
        cube.textureRotation = Rotation.ROTATE_NONE;
        cube.isFullBrightness = false;
        cube.u0 = 0;
        cube.v0 = 0;
        cube.u1 = 16;
        cube.v1 = 16;
        cube.isOverlay = false;
        
        IWritablePolyStream stream = PolyStreams.claimWritable();
        IMutablePolygon writer = stream.writer();
        writer.setRotation(0, Rotation.ROTATE_NONE);
        writer.setMinU(0, 0);
        writer.setMinV(0, 0);
        writer.setMaxU(0, 16);
        writer.setMaxV(0, 16);
        stream.saveDefaults();
        
        for(EnumFacing face : EnumFacing.VALUES)
        {
            cube.surfaceInstance = hasFront && face == rotation.horizontalFace  
                    ? MachineMeshFactory.SURFACE_LAMP 
                    : MachineMeshFactory.SURFACE_MAIN;
            
            cube.appendFace(stream, face);
        }
        
        return stream.releaseAndConvertToReader();
    }
    

    @Override
    public boolean isCube(ISuperModelState modelState)
    {
        return true;
    }

    @Override
    public boolean rotateBlock(IBlockState blockState, World world, BlockPos pos, EnumFacing axis, ISuperBlock block, ISuperModelState modelState)
    {
        return false;
        
        // does not work, see comments in SuperBlock parent method
//        modelState.setAxisRotation(modelState.getAxisRotation().clockwise());
//        return true;
    }

    @Override
    public int geometricSkyOcclusion(ISuperModelState modelState)
    {
        return 255;
    }

    @Override
    public ICollisionHandler collisionHandler()
    {
        return CubeCollisionHandler.INSTANCE;
    }

    @Override
    public SideShape sideShape(ISuperModelState modelState, EnumFacing side)
    {
        return SideShape.SOLID;
    }

    @Override
    public boolean hasLampSurface(ISuperModelState modelState)
    {
        return true;
    }
}
