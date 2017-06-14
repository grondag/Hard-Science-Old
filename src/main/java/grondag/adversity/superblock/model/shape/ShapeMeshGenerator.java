package grondag.adversity.superblock.model.shape;

import java.util.List;

import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;

import com.google.common.collect.ImmutableList;

import grondag.adversity.library.render.RawQuad;
import grondag.adversity.superblock.block.SuperBlock;
import grondag.adversity.superblock.model.state.Surface;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.model.state.ModelStateFactory.StateFormat;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;


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

    @Nonnull
    public abstract List<RawQuad> getShapeQuads(ModelState modelState);

    /** Returns true if geometry is a full 1x1x1 cube. */
    public abstract boolean isCube(ModelState modelState);
    
    /** 
     * Returns a copy of the given model state with only the attributes that affect geometry.
     * Used as a lookup key for block breaking models.
     */
    public ModelState geometricModelState(ModelState modelState)
    {
        ModelState result = new ModelState();
        result.setShape(modelState.getShape());
        result.setStaticShapeBits(modelState.getStaticShapeBits());
        return result;
    }

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
    
    public abstract boolean rotateBlock(IBlockState blockState, World world, BlockPos pos, EnumFacing axis, SuperBlock block, ModelState modelState);

    public abstract SideShape sideShape(ModelState modelState, EnumFacing side);
    
    /**
     * Find appropriate transformation assuming base model is oriented to Y axis, positive.
     * This is different than the Minecraft/Forge default because I brain that way.
     */
    protected static Matrix4f getMatrixForAxis(ModelState modelState)
    {
        return getMatrixForAxis(modelState.getAxis(), modelState.isAxisInverted());
    }

    /**
     * See {@link #getMatrixForAxis(ModelState)}
     */
    protected static Matrix4f getMatrixForAxis(EnumFacing.Axis axis, boolean isAxisInverted)
    {
        switch(axis)
        {
        case X:
            return ForgeHooksClient.getMatrix(isAxisInverted ? ModelRotation.X90_Y270 : ModelRotation.X90_Y90);
    
        case Y:
            return ForgeHooksClient.getMatrix(isAxisInverted ? ModelRotation.X180_Y0 : ModelRotation.X0_Y0);
    
        case Z:
            return ForgeHooksClient.getMatrix(isAxisInverted ? ModelRotation.X90_Y0 : ModelRotation.X270_Y0);
            
        default:
            return ForgeHooksClient.getMatrix(ModelRotation.X0_Y0);
        
        }
    }
    
}
