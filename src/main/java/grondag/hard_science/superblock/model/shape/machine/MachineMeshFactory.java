package grondag.hard_science.superblock.model.shape.machine;

import java.util.List;

import grondag.exotic_matter.model.ICollisionHandler;
import grondag.exotic_matter.model.ISuperBlock;
import grondag.exotic_matter.model.ISuperModelState;
import grondag.exotic_matter.model.ModelStateData;
import grondag.exotic_matter.model.ShapeMeshGenerator;
import grondag.exotic_matter.model.StateFormat;
import grondag.exotic_matter.render.RawQuad;
import grondag.exotic_matter.render.SideShape;
import grondag.exotic_matter.render.Surface;
import grondag.exotic_matter.render.SurfaceTopology;
import grondag.exotic_matter.render.SurfaceType;
import grondag.exotic_matter.render.Surface.SurfaceInstance;
import grondag.exotic_matter.varia.Useful;
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
        PHOTOCHEM_CELL,
        PHOTOCHEM_CABLE,
        BOTTOM_BUS,
        MIDDLE_BUS,
        PHOTOELECTRIC_CELL
    }
    
    private static final ShapeMeshGenerator[] HANDLERS;
    
    public static final Surface SURFACE_MAIN = new Surface(SurfaceType.MAIN, SurfaceTopology.CUBIC);
    public static final Surface SURFACE_LAMP = new Surface(SurfaceType.LAMP, SurfaceTopology.CUBIC);
    
    public static final SurfaceInstance INSTANCE_MAIN = SURFACE_MAIN.unitInstance;
    public static final SurfaceInstance INSTANCE_LAMP = SURFACE_LAMP.unitInstance.withAllowBorders(false);
    
    static
    {
        HANDLERS = new ShapeMeshGenerator[MachineShape.values().length];
        
        HANDLERS[MachineShape.BASIC_BOX.ordinal()] = new MachineCubeMeshFactory(true);
        HANDLERS[MachineShape.PHOTOCHEM_CELL.ordinal()] = new PhotoCellMeshFactory(0.5f);
        HANDLERS[MachineShape.PHOTOCHEM_CABLE.ordinal()] = new CableMeshFactory(1.0/16.0, true);
        HANDLERS[MachineShape.BOTTOM_BUS.ordinal()] = new CableMeshFactory(1.0 / 5.0, false);
        HANDLERS[MachineShape.MIDDLE_BUS.ordinal()] = new MachineCubeMeshFactory(false);
        HANDLERS[MachineShape.PHOTOELECTRIC_CELL.ordinal()] = new PhotoCellMeshFactory(0.15f);
    }
    
    public MachineMeshFactory()
    {
        super(StateFormat.BLOCK, ModelStateData.STATE_FLAG_NONE, 
                SURFACE_MAIN, SURFACE_LAMP); 
    }

    public static MachineShape getMachineShape(ISuperModelState modelState)
    {
        return Useful.safeEnumFromOrdinal((int)modelState.getStaticShapeBits(), MachineShape.BASIC_BOX);
    }

    public static void setMachineShape(MachineShape shape, ISuperModelState modelState)
    {
        modelState.setStaticShapeBits(shape.ordinal());
    }
    
    @Override
    public List<RawQuad> getShapeQuads(ISuperModelState modelState)
    {
        return HANDLERS[getMachineShape(modelState).ordinal()].getShapeQuads(modelState);
    }
    
 
    @Override
    public boolean isCube(ISuperModelState modelState)
    {
        return HANDLERS[getMachineShape(modelState).ordinal()].isCube(modelState);
    }

    @Override
    public boolean rotateBlock(IBlockState blockState, World world, BlockPos pos, EnumFacing axis, ISuperBlock block, ISuperModelState modelState)
    {
        return HANDLERS[getMachineShape(modelState).ordinal()].rotateBlock(blockState, world, pos, axis, block, modelState);
    }

    @Override
    public int geometricSkyOcclusion(ISuperModelState modelState)
    {
        return HANDLERS[getMachineShape(modelState).ordinal()].geometricSkyOcclusion(modelState);
    }

    @Override
    public ICollisionHandler collisionHandler()
    {
        return this;
    }

    @Override
    public SideShape sideShape(ISuperModelState modelState, EnumFacing side)
    {
        return HANDLERS[getMachineShape(modelState).ordinal()].sideShape(modelState, side);
    }

    @Override
    public List<AxisAlignedBB> getCollisionBoxes(ISuperModelState modelState)
    {
        return HANDLERS[getMachineShape(modelState).ordinal()].collisionHandler().getCollisionBoxes(modelState);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(ISuperModelState modelState)
    {
        return HANDLERS[getMachineShape(modelState).ordinal()].collisionHandler().getCollisionBoundingBox(modelState);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox(ISuperModelState modelState)
    {
        return HANDLERS[getMachineShape(modelState).ordinal()].collisionHandler().getRenderBoundingBox(modelState);
    }

    @Override
    public int getStateFlags(ISuperModelState modelState)
    {
        return HANDLERS[getMachineShape(modelState).ordinal()].getStateFlags(modelState);
    }
    
    @Override
    public List<Surface> getSurfaces(ISuperModelState modelState)
    {
        return HANDLERS[getMachineShape(modelState).ordinal()].getSurfaces(modelState);
    }

}