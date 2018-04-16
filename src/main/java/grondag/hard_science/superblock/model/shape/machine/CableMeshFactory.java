package grondag.hard_science.superblock.model.shape.machine;

import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import grondag.exotic_matter.model.ICollisionHandler;
import grondag.exotic_matter.model.ISuperBlock;
import grondag.exotic_matter.model.ISuperModelState;
import grondag.exotic_matter.model.ModelStateData;
import grondag.exotic_matter.render.CSGMesh;
import grondag.exotic_matter.render.IMutablePolygon;
import grondag.exotic_matter.render.IPolygon;
import grondag.exotic_matter.render.Poly;
import grondag.exotic_matter.render.QuadHelper;
import grondag.exotic_matter.render.SideShape;
import grondag.exotic_matter.world.Rotation;
import grondag.exotic_matter.world.SimpleJoin;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Square machines use lamp surface as the control face
 * and main surface as the casing sides.
 */
public class CableMeshFactory extends AbstractMachineMeshGenerator implements ICollisionHandler
{
    private final double CABLE_RADIUS;
    private final double CABLE_Y_CENTER;
    private final double yLow;
    private final double yHigh;
    private final double xzMin;
    private final double xzMax;
    
    public CableMeshFactory(double cableRadius, boolean onGround)
    {
        super(ModelStateData.STATE_FLAG_NEEDS_SIMPLE_JOIN | ModelStateData.STATE_FLAG_NEEDS_SPECIES, 
                MachineMeshFactory.SURFACE_MAIN); 
        
        this.CABLE_RADIUS = cableRadius;
        this.CABLE_Y_CENTER = onGround ? cableRadius : 0.5;
        this.yLow = CABLE_Y_CENTER - CABLE_RADIUS;
        this.yHigh = CABLE_Y_CENTER + CABLE_RADIUS;
        this.xzMin = 0.5 - CABLE_RADIUS;
        this.xzMax = 0.5 + CABLE_RADIUS;
    }
    
    /**
     * Top surface is main surface (so can support borders if wanted).
     * Sides and bottom are lamp surface. 
     */
    @Override
    public List<IPolygon> getShapeQuads(ISuperModelState modelState)
    {
        ImmutableList.Builder<IPolygon> builder = ImmutableList.builder();
        
        IMutablePolygon template = Poly.mutable(4);
        template.setColor(0xFFFFFFFF);
        template.setRotation(Rotation.ROTATE_NONE);
        template.setFullBrightness(false);
        template.setLockUV(true);
        template.setSurfaceInstance(MachineMeshFactory.INSTANCE_MAIN);
        
        SimpleJoin join = modelState.getSimpleJoin();
        
        CSGMesh shape = makeAxis(join, EnumFacing.NORTH, template);
        
        CSGMesh box = makeAxis(join, EnumFacing.EAST, template);
        
        if(box != null)
        {
            shape = shape == null ? box : shape.union(box);
        }
        
        box = makeAxis(join, EnumFacing.UP, template);
        
        if(box != null)
        {
            shape = shape == null ? box : shape.union(box);
        }
        
        if(shape == null)
        {

            builder.addAll(QuadHelper.makeBox(new AxisAlignedBB(xzMin, yLow, xzMin, xzMax, yHigh, xzMax), template));
        }
        else
        {
            //shape.recolor();
            builder.addAll(shape);
        }
        
        return builder.build();
    }
   
    private CSGMesh makeAxis(SimpleJoin join, @Nonnull EnumFacing face, @Nonnull IPolygon template)
    {
        if(join.isJoined(face))
        {
            return new CSGMesh(makeBox(face, template, join.isJoined(face.getOpposite())));
        }
        else if(join.isJoined(face.getOpposite()))
        {
            return new CSGMesh(makeBox(face.getOpposite(), template, false));
        }
        else
        {
            return null;
        }
    }
    
    private List<IPolygon> makeBox(@Nonnull EnumFacing face, @Nonnull IPolygon template, boolean isBothEnds)
    {
        AxisAlignedBB aabb;
        
        switch(face.getAxis())
        {
        case X:
            aabb = isBothEnds 
                ? new AxisAlignedBB(0.0, yLow, xzMin, 1.0, yHigh, xzMax)
                : face.getAxisDirection() == AxisDirection.POSITIVE
                    ? new AxisAlignedBB(xzMin, yLow, xzMin, 1.0, yHigh, xzMax)
                    : new AxisAlignedBB(0.0, yLow, xzMin, xzMax, yHigh, xzMax);  
            break;
            
        case Y:
            aabb = isBothEnds 
                ? new AxisAlignedBB(xzMin, 0.0, xzMin, xzMax, 1.0, xzMax)
                : face.getAxisDirection() == AxisDirection.POSITIVE
                    ? new AxisAlignedBB(xzMin, yLow, xzMin, xzMax, 1.0, xzMax)
                    : new AxisAlignedBB(xzMin, 0.0, xzMin, xzMax, yHigh, xzMax);  
            break;
            
        case Z:
        default:
            aabb = isBothEnds 
                ? new AxisAlignedBB(xzMin, yLow, 0.0, xzMax, yHigh, 1.0)
                : face.getAxisDirection() == AxisDirection.POSITIVE
                    ? new AxisAlignedBB(xzMin, yLow, xzMin, xzMax, yHigh, 1.0)
                    : new AxisAlignedBB(xzMin, yLow, 0.0, xzMax, yHigh, xzMax);  
            break;
        
        }
        return QuadHelper.makeBox(aabb, template);
    }
   
    @Override
    public boolean isCube(ISuperModelState modelState)
    {
        return false;
    }

    @Override
    public boolean rotateBlock(IBlockState blockState, World world, BlockPos pos, EnumFacing axis, ISuperBlock block, ISuperModelState modelState)
    {
        return false;
    }

    @Override
    public int geometricSkyOcclusion(ISuperModelState modelState)
    {
        return 0;
    }

    @Override
    public SideShape sideShape(ISuperModelState modelState, EnumFacing side)
    {
        return SideShape.MISSING;
    }
    
    @Override
    public ICollisionHandler collisionHandler()
    {
        return this;
    }
}
