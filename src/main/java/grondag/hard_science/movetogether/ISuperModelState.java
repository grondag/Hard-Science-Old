package grondag.hard_science.movetogether;

import java.util.List;

import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;

import grondag.exotic_matter.model.BlockOrientationType;
import grondag.exotic_matter.model.MetaUsage;
import grondag.exotic_matter.model.PaintLayer;
import grondag.exotic_matter.model.RenderPassSet;
import grondag.exotic_matter.model.Translucency;
import grondag.exotic_matter.render.RenderPass;
import grondag.exotic_matter.render.SideShape;
import grondag.exotic_matter.world.CornerJoinBlockState;
import grondag.exotic_matter.world.Rotation;
import grondag.exotic_matter.world.SimpleJoin;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public interface ISuperModelState
{

    int[] serializeToInts();

    long getBits0();

    long getBits1();

    long getBits2();

    long getBits3();

    /** 
     * Persisted but not part of hash nor included in equals comparison.
     * If true, refreshFromWorldState does nothing.
     */
    boolean isStatic();

    void setStatic(boolean isStatic);

    boolean equalsIncludeStatic(Object obj);

    int hashCode();

    /** returns self as convenience method */
    ISuperModelState refreshFromWorld(IBlockState state, IBlockAccess world, BlockPos pos);

    ModelShape<?> getShape();

    /**
     * Also resets shape-specific bits to default for the given shape.
     * Does nothing if shape is the same as existing.
     */
    void setShape(ModelShape<?> shape);

    ColorMap getColorMap(PaintLayer layer);

    void setColorMap(PaintLayer layer, ColorMap map);

    /**
     * Used by placement logic to know if shape has any kind of orientation to it that can be selected during placement.
     */
    BlockOrientationType orientationType();

    EnumFacing.Axis getAxis();

    void setAxis(EnumFacing.Axis axis);

    boolean isAxisInverted();

    void setAxisInverted(boolean isInverted);

    /**
     * For base/lamp paint layers, true means should be rendered in translucent render layer.
     * (Overlay textures always render in translucent layer.)
     * For all paint layers, true also means {@link #getTranslucency()} applies.
     */
    boolean isTranslucent(PaintLayer layer);

    /**
     * See {@link #isTranslucent(PaintLayer)}
     */
    void setTranslucent(PaintLayer layer, boolean isTranslucent);

    boolean isMiddleLayerEnabled();

    void setMiddleLayerEnabled(boolean isEnabled);

    boolean isOuterLayerEnabled();

    void setOuterLayerEnabled(boolean isEnabled);

    /**
     * Should only be applied to rendering if {@link #isTranslucent(PaintLayer)} is true
     * for the paint layer being rendered. 
     */
    Translucency getTranslucency();

    /**
     * Will be applied to rendering if {@link #isTranslucent(PaintLayer)} is true
     * for the paint layer being rendered.
     */
    void setTranslucency(Translucency translucency);

    ITexturePalette getTexture(PaintLayer layer);

    void setTexture(PaintLayer layer, ITexturePalette tex);

    /** 
     * Don't use this directly for rendering - user choice may not be workable.
     * Use {@link #getRenderMode(PaintLayer)} instead.
     */
    boolean isFullBrightness(PaintLayer layer);

    void setFullBrightness(PaintLayer layer, boolean isFullBrightness);

    int getPosX();

    void setPosX(int index);

    int getPosY();

    void setPosY(int index);

    int getPosZ();

    void setPosZ(int index);

    /** Usage is determined by shape. Limited to 44 bits and does not update from world. */
    long getStaticShapeBits();

    /** usage is determined by shape */
    void setStaticShapeBits(long bits);

    /**
     * Will return 0 if model state does not include species.
     * This is more convenient than checking each place species is used.
     * @return
     */
    int getSpecies();

    void setSpecies(int species);

    CornerJoinBlockState getCornerJoin();

    void setCornerJoin(CornerJoinBlockState join);

    SimpleJoin getSimpleJoin();

    void setSimpleJoin(SimpleJoin join);

    SimpleJoin getMasonryJoin();

    void setMasonryJoin(SimpleJoin join);

    /**
     * For machines and other blocks with a privileged horizontal face, North is considered the zero rotation.
     */
    Rotation getAxisRotation();

    /**
     * For machines and other blocks with a privileged horizontal face, North is considered the zero rotation.
     */
    void setAxisRotation(Rotation rotation);

    /** Multiblock shapes also get a full 64 bits of information - does not update from world */
    long getMultiBlockBits();

    /** Multiblock shapes also get a full 64 bits of information - does not update from world */
    void setMultiBlockBits(long bits);

    TerrainState getTerrainState();

    void setTerrainState(TerrainState flowState);

    /**
     * Determines what rendering path should apply for the given paint layer
     * based on user choices and the constraints imposed by MC rendering.  
     */
    RenderPass getRenderPass(PaintLayer layer);

    RenderPassSet getRenderPassSet();

    boolean hasAxis();

    boolean hasAxisOrientation();

    /**
     * True if shape mesh generator can output lamp surface quads.
     */
    boolean hasLampSurface();

    /**
     * True if base paint layer is translucent or lamp paint layer is present and translucent.
     */
    boolean hasTranslucentGeometry();

    boolean hasAxisRotation();

    boolean hasMasonryJoin();

    boolean hasTextureRotation();

    /**
     * Means that one or more elements (like a texture) uses species.
     * Does not mean that the shape or block actually capture or generate species other than 0.
     */
    boolean hasSpecies();

    /** Convenience method. Same as shape attribute. */
    MetaUsage metaUsage();

    /** Convenience method. Same as shape attribute. */
    boolean isAxisOrthogonalToPlacementFace();

    /**
     * Retrieves block/item metadata that should apply to this modelState.
     */
    int getMetaData();

    void setMetaData(int meta);

    /** True if shape can be placed on itself to grow */
    boolean isAdditive();

    SideShape sideShape(EnumFacing side);

    /** returns true if geometry is a full 1x1x1 cube. */
    boolean isCube();

    /** 
     * Rotate this block around the given orthogonalAxis if possible, making necessary changes to world state.
     * Return true if successful. 
     * @param blockState 
     */
    boolean rotateBlock(IBlockState blockState, World world, BlockPos pos, EnumFacing axis, ISuperBlock block);

    /** 
     * How much of the sky is occluded by the shape of this block?
     * Based on geometry alone, not transparency.
     * Returns 0 if no occlusion (unlikely result).
     * 1-15 if some occlusion.
     * 255 if fully occludes sky.
     */
    int geometricSkyOcclusion();

    /** 
     * Returns true if visual elements and geometry match.
     * Does not consider species in matching.
     */
    boolean doShapeAndAppearanceMatch(ISuperModelState other);

    /** 
     * Returns true if visual elements match.
     * Does not consider species or geometry in matching.
     */
    boolean doesAppearanceMatch(ISuperModelState other);

    /** 
     * Returns a copy of this model state with only the bits that matter for geometry.
     * Used as lookup key for block damage models.
     */
    ISuperModelState geometricState();

    /**
     * Returns a list of collision boxes offset to the given world position 
     */
    List<AxisAlignedBB> collisionBoxes(BlockPos offset);

    /**
     * See {@link Transform#rotateFace(ModelState, EnumFacing)}
     */
    EnumFacing rotateFace(EnumFacing face);

    /**
     * Find appropriate transformation assuming base model is oriented to Y orthogonalAxis, positive.
     * This is different than the Minecraft/Forge default because I brain that way.<br><br>
     * See {@link Transform#getMatrix4f(ModelState)}
     */
    Matrix4f getMatrix4f();

    /** for compatibility with double-valued raw quad vertices */
    Matrix4d getMatrix4d();

    void deserializeNBT(NBTTagCompound tag);

    /**
     * Use when you need to avoid tag name conflicts / have more than one.
     */
    void deserializeNBT(NBTTagCompound tag, String tagName);

    void serializeNBT(NBTTagCompound tag);

    /**
     * Use when you need to avoid tag name conflicts / have more than one.
     */
    void serializeNBT(NBTTagCompound tag, String tagName);

    void fromBytes(PacketBuffer pBuff);

    void toBytes(PacketBuffer pBuff);
    
    ISuperModelState clone();

}