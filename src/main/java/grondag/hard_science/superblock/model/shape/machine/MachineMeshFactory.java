package grondag.hard_science.superblock.model.shape.machine;

import java.util.List;
import java.util.function.Consumer;

import grondag.exotic_matter.block.ISuperBlock;
import grondag.exotic_matter.model.collision.ICollisionHandler;
import grondag.exotic_matter.model.mesh.ShapeMeshGenerator;
import grondag.exotic_matter.model.painting.PaintLayer;
import grondag.exotic_matter.model.painting.Surface;
import grondag.exotic_matter.model.painting.SurfaceTopology;
import grondag.exotic_matter.model.primitives.polygon.IMutablePolygon;
import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.exotic_matter.model.state.ModelStateData;
import grondag.exotic_matter.model.state.StateFormat;
import grondag.exotic_matter.model.varia.SideShape;
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
    
    protected static final Surface SURFACE_MAIN = Surface.builder(SurfaceTopology.CUBIC)
            .withDisabledLayers(PaintLayer.CUT, PaintLayer.LAMP)
            .build();
    
    protected static final Surface SURFACE_LAMP = Surface.builder(SurfaceTopology.CUBIC)
            .withAllowBorders(false)
            .withEnabledLayers(PaintLayer.LAMP)
            .build();
    
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
        super(StateFormat.BLOCK, ModelStateData.STATE_FLAG_NONE); 
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
    public void produceShapeQuads(ISuperModelState modelState, Consumer<IMutablePolygon> target)
    {
        HANDLERS[getMachineShape(modelState).ordinal()].produceShapeQuads(modelState, target);
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
    public boolean hasLampSurface(ISuperModelState modelState)
    {
        return true;
    }

}
