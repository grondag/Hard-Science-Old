package grondag.hard_science.superblock.model.shape.machine;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.exotic_matter.block.ISuperBlock;
import grondag.exotic_matter.model.primitives.CubeInputs;
import grondag.exotic_matter.model.primitives.IMutablePolygon;
import grondag.exotic_matter.model.primitives.IPolygon;
import grondag.exotic_matter.model.primitives.Poly;
import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.exotic_matter.model.state.ModelStateData;
import grondag.exotic_matter.model.varia.CubeCollisionHandler;
import grondag.exotic_matter.model.varia.ICollisionHandler;
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
    private final List<IPolygon> cubeQuads0;
    private final List<IPolygon> cubeQuads90;
    private final List<IPolygon> cubeQuads180;
    private final List<IPolygon> cubeQuads270;
    
    /**
     * @param hasFront If true, model will have an orientation and front display face
     */
    public MachineCubeMeshFactory(boolean hasFront)
    {
        super(hasFront ? ModelStateData.STATE_FLAG_HAS_AXIS_ROTATION : ModelStateData.STATE_FLAG_NONE); 
        
        this.cubeQuads0 = getCubeQuads(Rotation.ROTATE_NONE, hasFront);
        if(hasFront)
        {
            this.cubeQuads90 = getCubeQuads(Rotation.ROTATE_90, hasFront);
            this.cubeQuads180 = getCubeQuads(Rotation.ROTATE_180, hasFront);
            this.cubeQuads270 = getCubeQuads(Rotation.ROTATE_270, hasFront);
        }
        else
        {
            this.cubeQuads90 = this.cubeQuads0;
            this.cubeQuads180 = this.cubeQuads0;
            this.cubeQuads270 = this.cubeQuads0;
        }
    }
    
    /**
     * Top surface is main surface (so can support borders if wanted).
     * Sides and bottom are lamp surface. 
     */
    @Override
    public List<IPolygon> getShapeQuads(ISuperModelState modelState)
    {
        switch(modelState.getAxisRotation())
        {
        case ROTATE_NONE:
        default:
            return this.cubeQuads0;
        case ROTATE_90:
            return this.cubeQuads90;
        case ROTATE_180:
            return this.cubeQuads180;
        case ROTATE_270:
            return this.cubeQuads270;
        }
    }
   
    private List<IPolygon> getCubeQuads(Rotation rotation, boolean hasFront)
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
        
        IMutablePolygon template = Poly.mutable(4);
        template.setColor(0xFFFFFFFF);
        template.setRotation(Rotation.ROTATE_NONE);
        template.setMinU(0);
        template.setMinV(0);
        template.setMaxU(16);
        template.setMaxV(16);
        
        ImmutableList.Builder<IPolygon> builder = ImmutableList.builder();
       
        for(EnumFacing face : EnumFacing.VALUES)
        {
            
            result.surfaceInstance = hasFront && face == rotation.horizontalFace  
                    ? MachineMeshFactory.INSTANCE_LAMP 
                    : MachineMeshFactory.INSTANCE_MAIN;
            
            builder.add(result.makeRawFace(face));
        }
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
