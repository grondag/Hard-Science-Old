package grondag.hard_science.superblock.model.shape;

import java.util.List;

import javax.annotation.Nonnull;
import com.google.common.collect.ImmutableList;

import grondag.exotic_matter.model.BlockOrientationType;
import grondag.exotic_matter.model.StateFormat;
import grondag.exotic_matter.render.RawQuad;
import grondag.exotic_matter.render.SideShape;
import grondag.exotic_matter.render.Surface;
import grondag.exotic_matter.render.SurfaceType;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.collision.ICollisionHandler;
import grondag.hard_science.superblock.model.state.ModelState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public abstract class ShapeMeshGenerator
{
    /** used by ModelState to know why type of state representation is needed by this shape */
    public final StateFormat stateFormat;
    
    /** bits flags used by ModelState to know which optional state elements are needed by this shape */
    private final int stateFlags;
    
    /** Surfaces that compose the model. */
    private final List<Surface> surfaces;
    
    /**
     * When shape is changed on ModelState, the per-shape bits will be
     * set to this value.  Only need to change if shape needs some preset state.
     */
    public final long defaultShapeStateBits;
    
    /**
     * True if it is possible for this generator to output a lamp surface.
     */
    protected final boolean hasAnyLampSurface;
    
    protected ShapeMeshGenerator(StateFormat stateFormat, int stateFlags, Surface... surfaces)
    {
        this(stateFormat, stateFlags, 0L, surfaces);
    }
    
    protected ShapeMeshGenerator(StateFormat stateFormat, int stateFlags, long defaultShapeStateBits, Surface... surfaces)
    {
        this.stateFormat = stateFormat;
        this.stateFlags = stateFlags;
        this.surfaces = new ImmutableList.Builder<Surface>()
                .add(surfaces).build();
        this.defaultShapeStateBits = defaultShapeStateBits;
        
        boolean hasLamp = false;
        for(Surface s : surfaces)
        {
            if(s.surfaceType == SurfaceType.LAMP)
            {
                hasLamp = true;
                break;
            }
        }
        this.hasAnyLampSurface = hasLamp;
    }
    
    /**
     * Override if shape has any kind of orientation to it that can be selected during placement.
     */
    public BlockOrientationType orientationType(ModelState modelState) { return BlockOrientationType.NONE; } 
    
    
    @Nonnull
    public abstract ICollisionHandler collisionHandler();
    
    /** 
     * How much of the sky is occluded by the shape of this block?
     * Based on geometry alone, not transparency.
     * Returns 0 if no occlusion (unlikely result).
     * 1-15 if some occlusion.
     * 255 if fully occludes sky.
     */
    public abstract int geometricSkyOcclusion(ModelState modelState);

    @Nonnull
    public abstract List<RawQuad> getShapeQuads(ModelState modelState);

    /** Returns true if geometry is a full 1x1x1 cube. */
    public abstract boolean isCube(ModelState modelState);
    
     /**
     * If this shape uses metadata to affect geometry, retrieves the block/item metadata 
     * value that should correspond to this modelstate
     */
    public int getMetaData(ModelState modelState)
    {
        return 0;
    }
    
    /**
     * If this shape uses metadata to affect geometry, will be called during block
     * placement and during refreshFromWorld. Can be ignored if the
     * shaped has another mechanism for synchronizing with block meta.
     * (TerrainBlocks get it via TerrainState, for example)
     */
    public void setMetaData(ModelState modelState, int meta)
    {
        // Default is to do nothing
    }
    
    /**
     * If true, shape can be placed on itself to become bigger.
     */
    public boolean isAdditive() { return false; }
    
    /**
     * True if shape mesh generator will output lamp surface quads
     * with the given model state. Default implementation
     * simply looks for presence of any lamp surface.
     */
    public boolean hasLampSurface(ModelState modelState)
    {
        return this.hasAnyLampSurface;
    }
    
    /**
     * Override to true for blocks like stairs and wedges.
     * CubicPlacementHandler will know they need to be placed 
     * in a corner instead of a face.
     */
    public boolean isAxisOrthogonalToPlacementFace() { return false; }
    
    public abstract boolean rotateBlock(IBlockState blockState, World world, BlockPos pos, EnumFacing axis, SuperBlock block, ModelState modelState);

    public abstract SideShape sideShape(ModelState modelState, EnumFacing side);
    
    public int getStateFlags(ModelState modelState)
    {
        return stateFlags;
    }

    public List<Surface> getSurfaces(ModelState modelState)
    {
        return surfaces;
    }
}
