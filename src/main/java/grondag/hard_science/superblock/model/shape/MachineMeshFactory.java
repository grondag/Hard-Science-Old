package grondag.hard_science.superblock.model.shape;

import java.util.List;

import grondag.hard_science.library.render.RawQuad;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.collision.ICollisionHandler;
import grondag.hard_science.superblock.collision.SideShape;
import grondag.hard_science.superblock.model.shape.machine.MachineCubeMeshFactory;
import grondag.hard_science.superblock.model.shape.machine.CableMeshFactory;
import grondag.hard_science.superblock.model.shape.machine.SolarCellMeshFactory;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.model.state.Surface.SurfaceInstance;
import grondag.hard_science.superblock.model.state.StateFormat;
import grondag.hard_science.superblock.model.state.Surface;
import grondag.hard_science.superblock.model.state.SurfaceTopology;
import grondag.hard_science.superblock.model.state.SurfaceType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Square machines use lamp surface as the control face
 * and main surface as the casing sides.
 */
public class MachineMeshFactory extends ShapeMeshGenerator implements ICollisionHandler
{
    public static enum MachineShape
    {
        BASIC_BOX,
        SOLAR_CELL,
        SOLAR_CABLE,
        EXTENSION_BUS,
        INTERMEDIATE_BUS
    }
    
    private static final ShapeMeshGenerator[] HANDLERS;
    
    public static final Surface SURFACE_MAIN = new Surface(SurfaceType.MAIN, SurfaceTopology.CUBIC);
    public static final Surface SURFACE_LAMP = new Surface(SurfaceType.LAMP, SurfaceTopology.CUBIC);
    
    public static final SurfaceInstance INSTANCE_MAIN = SURFACE_MAIN.unitInstance;
    public static final SurfaceInstance INSTANCE_LAMP = SURFACE_LAMP.unitInstance.withAllowBorders(false);
    
    private static ShapeMeshGenerator instance;

    static
    {
        instance = new MachineMeshFactory();
        
        HANDLERS = new ShapeMeshGenerator[MachineShape.values().length];
        
        HANDLERS[MachineShape.BASIC_BOX.ordinal()] = new MachineCubeMeshFactory();
        HANDLERS[MachineShape.SOLAR_CELL.ordinal()] = new SolarCellMeshFactory();
        HANDLERS[MachineShape.SOLAR_CABLE.ordinal()] = new CableMeshFactory(1.0/16.0);
        HANDLERS[MachineShape.EXTENSION_BUS.ordinal()] = new CableMeshFactory(1.0/4.0);
        HANDLERS[MachineShape.INTERMEDIATE_BUS.ordinal()] = new CableMeshFactory(14.0/16.0);
    }
    
    public static ShapeMeshGenerator getShapeMeshFactory()
    {
        return instance; 
    };
    
    protected MachineMeshFactory()
    {
        super(StateFormat.BLOCK, ModelState.STATE_FLAG_NONE, 
                SURFACE_MAIN, SURFACE_LAMP); 
    }

    public static MachineShape getMachineShape(ModelState modelState)
    {
        return Useful.safeEnumFromOrdinal((int)modelState.getStaticShapeBits(), MachineShape.BASIC_BOX);
    }

    public static void setMachineShape(MachineShape shape, ModelState modelState)
    {
        modelState.setStaticShapeBits(shape.ordinal());
    }
    
    @Override
    public List<RawQuad> getShapeQuads(ModelState modelState)
    {
        return HANDLERS[getMachineShape(modelState).ordinal()].getShapeQuads(modelState);
    }
    
 
    @Override
    public boolean isCube(ModelState modelState)
    {
        return HANDLERS[getMachineShape(modelState).ordinal()].isCube(modelState);
    }

    @Override
    public boolean rotateBlock(IBlockState blockState, World world, BlockPos pos, EnumFacing axis, SuperBlock block, ModelState modelState)
    {
        return HANDLERS[getMachineShape(modelState).ordinal()].rotateBlock(blockState, world, pos, axis, block, modelState);
    }

    @Override
    public int geometricSkyOcclusion(ModelState modelState)
    {
        return HANDLERS[getMachineShape(modelState).ordinal()].geometricSkyOcclusion(modelState);
    }

    @Override
    public ICollisionHandler collisionHandler()
    {
        return this;
    }

    @Override
    public SideShape sideShape(ModelState modelState, EnumFacing side)
    {
        return HANDLERS[getMachineShape(modelState).ordinal()].sideShape(modelState, side);
    }

    @Override
    public List<AxisAlignedBB> getCollisionBoxes(ModelState modelState)
    {
        return HANDLERS[getMachineShape(modelState).ordinal()].collisionHandler().getCollisionBoxes(modelState);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(ModelState modelState)
    {
        return HANDLERS[getMachineShape(modelState).ordinal()].collisionHandler().getCollisionBoundingBox(modelState);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox(ModelState modelState)
    {
        return HANDLERS[getMachineShape(modelState).ordinal()].collisionHandler().getRenderBoundingBox(modelState);
    }

    @Override
    public int getStateFlags(ModelState modelState)
    {
        return HANDLERS[getMachineShape(modelState).ordinal()].getStateFlags(modelState);
    }
    
    @Override
    public List<Surface> getSurfaces(ModelState modelState)
    {
        return HANDLERS[getMachineShape(modelState).ordinal()].getSurfaces(modelState);
    }

}
