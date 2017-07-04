package grondag.hard_science.superblock.model.shape;

import java.util.List;

import javax.annotation.Nonnull;
import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.library.render.RawQuad;
import grondag.hard_science.library.world.Rotation;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.collision.ICollisionHandler;
import grondag.hard_science.superblock.collision.SideShape;
import grondag.hard_science.superblock.model.state.StateFormat;
import grondag.hard_science.superblock.model.state.Surface;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
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
     * Override to true for blocks like stairs and wedges.
     * CubicPlacementHandler will know they need to be placed 
     * in a corner instead of a face.
     */
    public boolean isAxisOrthogonalToPlacementFace() { return false; }
    
    public abstract boolean rotateBlock(IBlockState blockState, World world, BlockPos pos, EnumFacing axis, SuperBlock block, ModelState modelState);

    public abstract SideShape sideShape(ModelState modelState, EnumFacing side);
    
    /**
     * Find appropriate transformation assuming base model is oriented to Y orthogonalAxis, positive.
     * This is different than the Minecraft/Forge default because I brain that way.<br><br>
     * 
     * @see #getMatrixForAxisAndRotation(net.minecraft.util.EnumFacing.Axis, boolean, Rotation) for
     * more explanation.
     */
    protected static Matrix4f getMatrix4f(ModelState modelState)
    {
        if(modelState.hasAxis())
        {
            if(modelState.hasModelRotation())
            {
                return getMatrixForAxisAndRotation(modelState.getAxis(), modelState.isAxisInverted(), modelState.getModelRotation());
            }
            else
            {
                return getMatrixForAxis(modelState.getAxis(), modelState.isAxisInverted());
            }
        }
        else if(modelState.hasModelRotation())
        {
            return getMatrixForRotation(modelState.getModelRotation());
        }
        else
        {
            return ForgeHooksClient.getMatrix(ModelRotation.X0_Y0);
        }
    }
    
    /** for compatibility with double-valued raw quad vertices */
    protected static Matrix4d getMatrix4d(ModelState modelState)
    {
        return new Matrix4d(getMatrix4f(modelState));
    }
    
    /**
     * See {@link #getMatrixForAxisAndRotation(net.minecraft.util.EnumFacing.Axis, boolean, Rotation)}
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
    
    /**
     * See {@link #getMatrixForAxisAndRotation(net.minecraft.util.EnumFacing.Axis, boolean, Rotation)}
     */
    protected static Matrix4f getMatrixForRotation(Rotation rotation)
    {
        switch(rotation)
        {
        default:
        case ROTATE_NONE:
            return ForgeHooksClient.getMatrix(ModelRotation.X0_Y0);
    
        case ROTATE_90:
            return ForgeHooksClient.getMatrix(ModelRotation.X0_Y90);
    
        case ROTATE_180:
            return ForgeHooksClient.getMatrix(ModelRotation.X0_Y180);
            
        case ROTATE_270:
            return ForgeHooksClient.getMatrix(ModelRotation.X0_Y270);
        }
    }
    
    /**
     * Models in default state should have orthogonalAxis = Y with positive
     * orientation (if orientation applies) and whatever rotation
     * that represents "none".  Generally, in this mod, north is
     * considered "top" of the reference frame when looking down
     * the Y-orthogonalAxis. <br><br>
     * 
     * Models that are oriented to an edge, like stairs and wedges
     * should have a default geometry such that the North and East
     * faces are "full" or "behind" the sloped part of the geometry.<br><br>
     *
     * 
     * With a model in the default state, the rotation occurs
     * first - around the Y-orthogonalAxis, followed by the transformation
     * from the Y orthogonalAxis/orientation to whatever new orthogonalAxis/orientation is 
     * given. <br><br>
     * 
     * Because 4d rotational matrices are the brain-child of
     * malevolent walrus creatures from hyperspace, this means
     * the order of multiplication is the opposite of what I just described.
     */
    protected static Matrix4f getMatrixForAxisAndRotation(EnumFacing.Axis axis, boolean isAxisInverted, Rotation rotation)
    {
        Matrix4f result = getMatrixForAxis(axis, isAxisInverted);
        result.mul(getMatrixForRotation(rotation));
        return result;
    }
}
