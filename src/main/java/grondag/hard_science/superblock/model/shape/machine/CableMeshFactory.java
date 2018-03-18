package grondag.hard_science.superblock.model.shape.machine;

import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import grondag.exotic_matter.render.CSGShape;
import grondag.exotic_matter.render.QuadHelper;
import grondag.exotic_matter.render.RawQuad;
import grondag.exotic_matter.render.SideShape;
import grondag.exotic_matter.world.Rotation;
import grondag.exotic_matter.world.SimpleJoin;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.collision.ICollisionHandler;
import grondag.hard_science.superblock.model.shape.MachineMeshFactory;
import grondag.hard_science.superblock.model.state.ModelState;
import grondag.hard_science.superblock.model.state.ModelStateData;
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
    public List<RawQuad> getShapeQuads(ModelState modelState)
    {

        RawQuad template = new RawQuad();
        template.color = 0xFFFFFFFF;
        template.rotation = Rotation.ROTATE_NONE;
        template.isFullBrightness = false;
        template.lockUV = true;
        template.surfaceInstance = MachineMeshFactory.INSTANCE_MAIN;

        ImmutableList.Builder<RawQuad> builder = new ImmutableList.Builder<RawQuad>();
        
        SimpleJoin join = modelState.getSimpleJoin();
        
        CSGShape shape = makeAxis(join, EnumFacing.NORTH, template);
        
        CSGShape box = makeAxis(join, EnumFacing.EAST, template);
        
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
   
    private CSGShape makeAxis(SimpleJoin join, @Nonnull EnumFacing face, @Nonnull RawQuad template)
    {
        if(join.isJoined(face))
        {
            return new CSGShape(makeBox(face, template, join.isJoined(face.getOpposite())));
        }
        else if(join.isJoined(face.getOpposite()))
        {
            return new CSGShape(makeBox(face.getOpposite(), template, false));
        }
        else
        {
            return null;
        }
    }
    
    private List<RawQuad> makeBox(@Nonnull EnumFacing face, @Nonnull RawQuad template, boolean isBothEnds)
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
    public boolean isCube(ModelState modelState)
    {
        return false;
    }

    @Override
    public boolean rotateBlock(IBlockState blockState, World world, BlockPos pos, EnumFacing axis, SuperBlock block, ModelState modelState)
    {
        return false;
    }

    @Override
    public int geometricSkyOcclusion(ModelState modelState)
    {
        return 0;
    }

    @Override
    public SideShape sideShape(ModelState modelState, EnumFacing side)
    {
        return SideShape.MISSING;
    }
    
    @Override
    public ICollisionHandler collisionHandler()
    {
        return this;
    }
}
