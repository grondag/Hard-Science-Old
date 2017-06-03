package grondag.adversity.superblock.model.shape;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.superblock.block.SuperBlock;
import grondag.adversity.superblock.model.painter.surface.Surface;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.model.state.ModelStateFactory.StateFormat;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public abstract class ShapeMeshGenerator
{
    /** used by ModelState to know why type of state representation is needed by this shape */
    public final StateFormat stateFormat;
    
    /** bits flags used by ModelState to know which optional state elements are needed by this shape */
    public int stateFlags;
    
    /** Surfaces that compose the model. */
    public List<Surface> surfaces;
    
    /**
     * When shape is changed on ModelState, the per-shape bits will be
     * set to this value.  Only need to change if shape needs some preset state.
     */
    public final long defaultShapeStateBits;
    
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
    }
    
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

    public abstract Collection<RawQuad> getShapeQuads(ModelState modelState);

    /** Returns true if geometry is a full 1x1x1 cube. */
    public abstract boolean isCube(ModelState modelState);

    /** 
     * If true, will disable species selection on block placement. 
     * Will also prevent rendering of textures with texture layouts that require species
     * because those will expect species to demarcate multiblock boundaries.
     */
    public boolean isSpeciesUsedForHeight() { return false; }
    
    public abstract boolean rotateBlock(IBlockState blockState, World world, BlockPos pos, EnumFacing axis, SuperBlock block, ModelState modelState);

    public abstract SideShape sideShape(ModelState modelState, EnumFacing side);
    
}
