package grondag.hard_science.movetogether;

import static grondag.hard_science.movetogether.ModelStateData.STATE_FLAG_NONE;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.exotic_matter.model.StateFormat;
import grondag.exotic_matter.render.RawQuad;
import grondag.exotic_matter.render.SideShape;
import grondag.exotic_matter.render.Surface;
import grondag.exotic_matter.render.SurfaceTopology;
import grondag.exotic_matter.render.SurfaceType;
import grondag.exotic_matter.world.Rotation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CubeMeshFactory extends ShapeMeshGenerator
{
    private static final Surface SURFACE_MAIN = new Surface(SurfaceType.MAIN, SurfaceTopology.CUBIC);
    
    /** never changes so may as well save it */
    private final List<RawQuad> cachedQuads;
    
    public CubeMeshFactory()
    {
        super(StateFormat.BLOCK, STATE_FLAG_NONE, SURFACE_MAIN);
        this.cachedQuads = getCubeQuads();
    }

    @Override
    public List<RawQuad> getShapeQuads(ISuperModelState modelState)
    {
        return cachedQuads;
    }
    
    private List<RawQuad> getCubeQuads()
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
        result.surfaceInstance = SURFACE_MAIN.unitInstance;
        
        ImmutableList.Builder<RawQuad> builder = new ImmutableList.Builder<RawQuad>();
       
        builder.add(result.makeRawFace(EnumFacing.DOWN));
        builder.add(result.makeRawFace(EnumFacing.UP));
        builder.add(result.makeRawFace(EnumFacing.EAST));
        builder.add(result.makeRawFace(EnumFacing.WEST));
        builder.add(result.makeRawFace(EnumFacing.SOUTH));
        builder.add(result.makeRawFace(EnumFacing.NORTH));
       
        return builder.build();
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
}
