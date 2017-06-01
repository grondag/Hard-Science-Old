package grondag.adversity.superblock.model.shape;

import static grondag.adversity.superblock.model.state.ModelStateFactory.ModelState.*;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.Output;
import grondag.adversity.library.Rotation;
import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.color.NoColorMapProvider;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import grondag.adversity.superblock.block.SuperBlock;
import grondag.adversity.superblock.model.painter.surface.Surface;
import grondag.adversity.superblock.model.painter.surface.SurfaceTopology;
import grondag.adversity.superblock.model.painter.surface.SurfaceType;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.model.state.ModelStateFactory.StateFormat;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StackedPlatesMeshFactory extends ShapeMeshGenerator implements ICollisionHandler
{
    private static ShapeMeshGenerator instance;
    
    private static final AxisAlignedBB[] COLLISION_BOUNDS =
    {
        new AxisAlignedBB(0, 0, 0, 1, 1F/16F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 2F/16F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 3F/16F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 4F/16F, 1),

        new AxisAlignedBB(0, 0, 0, 1, 5F/16F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 6F/16F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 7F/16F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 8F/16F, 1),
    
        new AxisAlignedBB(0, 0, 0, 1, 9F/16F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 10F/16F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 11F/16F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 12F,16F),
        
        new AxisAlignedBB(0, 0, 0, 1, 13F/16F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 14F/16F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 15F/16F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 1, 1)
            
    };
    
    /** never change so may as well save em */
    @SuppressWarnings("unchecked")
    private final Collection<RawQuad>[] cachedQuads = new Collection[16];
    
    public static ShapeMeshGenerator getShapeMeshFactory()
    {
        if(instance == null) instance = new StackedPlatesMeshFactory();
        return instance; 
    }
    
    private StackedPlatesMeshFactory()
    {
        super(StateFormat.BLOCK, STATE_FLAG_NEEDS_SPECIES, new Surface(0, SurfaceType.MAIN, SurfaceTopology.CUBIC));
        this.populateQuads();
    }

    private void populateQuads()
    {
        for(int i = 0; i < 16; i++)
        {
            this.cachedQuads[i] = makeQuads(i);
        }
    }
 
    private Collection<RawQuad> makeQuads(int species)
    {
        double height = (species + 1) / 16.0;
        
        RawQuad template = new RawQuad();
        ColorMap colorMap = NoColorMapProvider.INSTANCE.getColorMap(0);
        template.color = colorMap.getColor(EnumColorMap.BASE);
        template.rotation = Rotation.ROTATE_NONE;
        template.lightingMode = LightingMode.SHADED;
        template.surface = this.surfaces.get(0);
        template.lockUV = true;

        ImmutableList.Builder<RawQuad> builder = new ImmutableList.Builder<RawQuad>();
        
        RawQuad quad = template.clone();
        quad.setFace(EnumFacing.UP);
        quad.setupFaceQuad(0.0, 0.0, 1.0, 1.0, 1-height, EnumFacing.NORTH);
        builder.add(quad);
      
        for(EnumFacing face : EnumFacing.Plane.HORIZONTAL.facings())
        {
            quad = template.clone();
            quad.setFace(face);
            quad.setupFaceQuad( 0.0, 0.0, 1.0, height, 0.0, EnumFacing.UP);
            builder.add(quad);
        }
        
        quad = template.clone();
        quad.setFace(EnumFacing.DOWN);
        quad.setupFaceQuad(0.0, 0.0, 1.0, 1.0, 0.0, EnumFacing.NORTH);
        builder.add(quad);
        
        return builder.build();
    }
    
    
    
    @Override
    public boolean isSpeciesUsedForHeight()
    {
        return true;
    }

    @Override
    public Collection<RawQuad> getShapeQuads(ModelState modelState)
    {
        return this.cachedQuads[modelState.getSpecies()];
    }

    @Override
    public boolean canPlaceTorchOnTop(ModelState modelState)
    {
        return modelState.getSpecies() == 15;
    }

    @Override
    public boolean isSideSolid(ModelState modelState, EnumFacing side)
    {
        return side == EnumFacing.DOWN || modelState.getSpecies() == 15;
    }

    @Override
    public boolean isCube(ModelState modelState)
    {
        return modelState.getSpecies() == 15;
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
        return this;
    }

    @Override
    public List<AxisAlignedBB> getCollisionBoxes(ModelState modelState)
    {
        return ImmutableList.of(getCollisionBoundingBox(modelState));
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(ModelState modelState)
    {
        try
        {
            return COLLISION_BOUNDS[modelState.getSpecies()];
        }
        catch (Exception ex)
        {
            Output.getLog().info("HeightModelFactory recevied Collision Bounding Box check for a foreign block.");
            return Block.FULL_BLOCK_AABB;
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox(ModelState modelState)
    {
        return getCollisionBoundingBox(modelState);
    };
}
