package grondag.adversity.superblock.model.shape;

import static grondag.adversity.superblock.model.state.ModelStateFactory.ModelState.STATE_FLAG_NONE;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.library.Rotation;
import grondag.adversity.library.model.quadfactory.CubeInputs;
import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.superblock.block.SuperBlock;
import grondag.adversity.superblock.model.painter.surface.Surface;
import grondag.adversity.superblock.model.painter.surface.SurfaceTopology;
import grondag.adversity.superblock.model.painter.surface.SurfaceType;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.model.state.ModelStateFactory.StateFormat;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CubeMeshFactory extends ShapeMeshGenerator
{
    private static ShapeMeshGenerator instance;
    
    /** all cubes have this same shape */
    private final ModelState GEOMETRIC_STATE;

    public static ShapeMeshGenerator getShapeMeshFactory()
    {
        if(instance == null) instance = new CubeMeshFactory();
        return instance; 
    };
    
    /** never changes so may as well save it */
    private final List<RawQuad> cachedQuads;
    
    protected CubeMeshFactory()
    {
        super(StateFormat.BLOCK, STATE_FLAG_NONE, new Surface(SurfaceType.MAIN, SurfaceTopology.CUBIC));
        this.cachedQuads = getCubeQuads();
        this.GEOMETRIC_STATE = new ModelState();
        this.GEOMETRIC_STATE.setShape(ModelShape.CUBE);
    }

    @Override
    public List<RawQuad> getShapeQuads(ModelState modelState)
    {
        return cachedQuads;
    }
    
    private List<RawQuad> getCubeQuads()
    {
        CubeInputs result = new CubeInputs();
        result.color = 0xFFFFFFFF;
        result.textureRotation = Rotation.ROTATE_NONE;
        result.lightingMode = LightingMode.SHADED;
        result.u0 = 0;
        result.v0 = 0;
        result.u1 = 16;
        result.v1 = 16;
        result.isOverlay = false;
        result.surfaceInstance = this.surfaces.get(0).unitInstance;
        
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
    public boolean isCube(ModelState modelState)
    {
        return true;
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
    public ICollisionHandler collisionHandler()
    {
        return CubeCollisionHandler.INSTANCE;
    }

    @Override
    public SideShape sideShape(ModelState modelState, EnumFacing side)
    {
        return SideShape.SOLID;
    }

    @Override
    public ModelState geometricModelState(ModelState modelState)
    {
        return GEOMETRIC_STATE.clone();
    }
}
