package grondag.hard_science.superblock.model.state;

import static grondag.hard_science.superblock.model.state.ModelStateData.STATE_ENUM_RENDER_PASS_SET;
import static grondag.hard_science.superblock.model.state.ModelStateData.STATE_FLAG_HAS_AXIS;
import static grondag.hard_science.superblock.model.state.ModelStateData.STATE_FLAG_HAS_AXIS_ORIENTATION;
import static grondag.hard_science.superblock.model.state.ModelStateData.STATE_FLAG_HAS_AXIS_ROTATION;
import static grondag.hard_science.superblock.model.state.ModelStateData.STATE_FLAG_HAS_TRANSLUCENT_GEOMETRY;
import static grondag.hard_science.superblock.model.state.ModelStateData.STATE_FLAG_NEEDS_CORNER_JOIN;
import static grondag.hard_science.superblock.model.state.ModelStateData.STATE_FLAG_NEEDS_MASONRY_JOIN;
import static grondag.hard_science.superblock.model.state.ModelStateData.STATE_FLAG_NEEDS_POS;
import static grondag.hard_science.superblock.model.state.ModelStateData.STATE_FLAG_NEEDS_SIMPLE_JOIN;
import static grondag.hard_science.superblock.model.state.ModelStateData.STATE_FLAG_NEEDS_SPECIES;
import static grondag.hard_science.superblock.model.state.ModelStateData.STATE_FLAG_NEEDS_TEXTURE_ROTATION;
import static grondag.hard_science.superblock.model.state.ModelStateData.TEST_GETTER_STATIC;

import java.util.List;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;

import grondag.exotic_matter.model.BlockOrientationType;
import grondag.exotic_matter.model.MetaUsage;
import grondag.exotic_matter.model.PaintLayer;
import grondag.exotic_matter.model.RenderPassSet;
import grondag.exotic_matter.model.StateFormat;
import grondag.exotic_matter.model.Translucency;
import grondag.exotic_matter.render.RenderPass;
import grondag.exotic_matter.render.SideShape;
import grondag.exotic_matter.serialization.IMessagePlus;
import grondag.exotic_matter.serialization.IReadWriteNBT;
import grondag.exotic_matter.varia.Useful;
import grondag.exotic_matter.world.CornerJoinBlockState;
import grondag.exotic_matter.world.CornerJoinBlockStateSelector;
import grondag.exotic_matter.world.NeighborBlocks;
import grondag.exotic_matter.world.Rotation;
import grondag.exotic_matter.world.SimpleJoin;
import grondag.hard_science.Configurator;
import grondag.hard_science.Log;
import grondag.hard_science.init.ModNBTTag;
import grondag.hard_science.moving.Transform;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.color.BlockColorMapProvider;
import grondag.hard_science.superblock.color.ColorMap;
import grondag.hard_science.superblock.terrain.TerrainState;
import grondag.hard_science.superblock.texture.TexturePalletteRegistry.TexturePallette;
import grondag.hard_science.superblock.texture.Textures;
import grondag.hard_science.superblock.varia.BlockTests;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class ModelState implements IReadWriteNBT, IMessagePlus
{

    private boolean isStatic;
    private long bits0;
    private long bits1;
    private long bits2;
    private long bits3;

    private int hashCode = -1;

    /** contains indicators derived from shape and painters */
    private int stateFlags;

    public ModelState() { }

    public ModelState(int[] bits)
    {
        this.deserializeFromInts(bits);
    }
    
    public ModelState(long b0, long b1, long b2, long b3)
    {
        bits0 = b0;
        bits1 = b1;
        bits2 = b2;
        bits3 = b3;
    }

    public ModelState clone()
    {
        return new ModelState(bits0, bits1, bits2, bits3);
    }

    public int[] serializeToInts() 
    {
        int[] result = new int[8];
        result[0] = (int) (bits0 >> 32);
        result[1] = (int) (bits0);

        result[2] = (int) (bits1 >> 32);
        result[3] = (int) (bits1);

        result[4] = (int) (this.isStatic ? (bits2 >> 32) | Useful.INT_SIGN_BIT : (bits2 >> 32));
        result[5] = (int) (bits2);

        result[6] = (int) (bits3 >> 32);
        result[7] = (int) (bits3);
        return result;
    }
    
    /**
     * Note does not reset state flag - do that if calling on an existing instance.
     */
    private void deserializeFromInts(int [] bits)
    {
        // sign on third long word is used to store static indicator
        this.isStatic = (Useful.INT_SIGN_BIT & bits[4]) == Useful.INT_SIGN_BIT;

        this.bits0 = ((long)bits[0]) << 32 | (bits[1] & 0xffffffffL);
        this.bits1 = ((long)bits[2]) << 32 | (bits[3] & 0xffffffffL);
        this.bits2 = ((long)(Useful.INT_SIGN_BIT_INVERSE & bits[4])) << 32 | (bits[5] & 0xffffffffL);
        this.bits3 = ((long)bits[6]) << 32 | (bits[7] & 0xffffffffL);    
    }

    public long getBits0() {return this.bits0;}
    public long getBits1() {return this.bits1;}
    public long getBits2() {return this.bits2;}
    public long getBits3() {return this.bits3;}

    private void populateStateFlagsIfNeeded()
    {
        if(this.stateFlags == 0)
        {
            this.stateFlags = ModelStateFlagHelper.getFlags(this);
        }
    }

    private void clearStateFlags()
    {
        if(this.stateFlags != 0) 
        {
            this.stateFlags  = 0;
        }
    }

    /** 
     * Persisted but not part of hash nor included in equals comparison.
     * If true, refreshFromWorldState does nothing.
     */
    public boolean isStatic() { return this.isStatic; }

    public void setStatic(boolean isStatic) { this.isStatic = isStatic; }

    /**
     * Does NOT consider isStatic in comparison. <br><br>
     * 
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj)
    {
        if(this == obj) return true;

        if(obj instanceof ModelState)
        {
            ModelState other = (ModelState)obj;
            return this.bits0 == other.bits0
                    && this.bits1 == other.bits1
                    && this.bits2 == other.bits2
                    && this.bits3 == other.bits3;
        }

        return false;
    }

    public boolean equalsIncludeStatic(Object obj)
    {
        if(this == obj) return true;

        if(obj instanceof ModelState)
        {
            ModelState other = (ModelState)obj;
            return this.bits0 == other.bits0
                    && this.bits1 == other.bits1
                    && this.bits2 == other.bits2
                    && this.bits3 == other.bits3
                    && this.isStatic == other.isStatic;
        }

        return false;
    }

    private void invalidateHashCode()
    {
        if(this.hashCode != -1) this.hashCode = -1;
    }

    @Override
    public int hashCode()
    {
        if(hashCode == -1)
        {
            hashCode = (int) Useful.longHash(this.bits0 ^ this.bits1 ^ this.bits2 ^ this.bits3);
        }
        return hashCode;
    }

    /** returns self as convenience method */
    public ModelState refreshFromWorld(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        //            Output.getLog().info("ModelState.refreshFromWorld static=" + this.isStatic + " @" + pos.toString());
        if(this.isStatic) return this;

        populateStateFlagsIfNeeded();

        if(state.getBlock() instanceof SuperBlock)
        {
            this.setMetaData(state.getValue(SuperBlock.META));
        }
        else
        {
            // prevent strangeness - shouldn't get called by non-superblock but modded MC is crazy biz
            return this;
        }
        
        switch(this.getShape().meshFactory().stateFormat)
        {
        case BLOCK:

            if((stateFlags & STATE_FLAG_NEEDS_POS) == STATE_FLAG_NEEDS_POS) refreshBlockPosFromWorld(pos, 255);

            long b3 = bits3;

            NeighborBlocks<ModelState> neighbors = null;

            if((STATE_FLAG_NEEDS_CORNER_JOIN & stateFlags) == STATE_FLAG_NEEDS_CORNER_JOIN)
            {
                neighbors = new NeighborBlocks<>(world, pos, TEST_GETTER_STATIC);
                NeighborBlocks<ModelState>.NeighborTestResults tests = neighbors.getNeighborTestResults(((SuperBlock)state.getBlock()).blockJoinTest(world, state, pos, this));
                b3 = ModelStateData.P3B_BLOCK_JOIN.setValue(CornerJoinBlockStateSelector.findIndex(tests), b3);
            }
            else if ((STATE_FLAG_NEEDS_SIMPLE_JOIN & stateFlags) == STATE_FLAG_NEEDS_SIMPLE_JOIN)
            {
                neighbors = new NeighborBlocks<>(world, pos, TEST_GETTER_STATIC);
                NeighborBlocks<ModelState>.NeighborTestResults tests = neighbors.getNeighborTestResults(((SuperBlock)state.getBlock()).blockJoinTest(world, state, pos, this));
                b3 = ModelStateData.P3B_BLOCK_JOIN.setValue(SimpleJoin.getIndex(tests), b3);
            }

            if((STATE_FLAG_NEEDS_MASONRY_JOIN & stateFlags) == STATE_FLAG_NEEDS_MASONRY_JOIN)
            {
                if(neighbors == null) neighbors = new NeighborBlocks<>(world, pos, TEST_GETTER_STATIC);
                NeighborBlocks<ModelState>.NeighborTestResults masonryTests = neighbors.getNeighborTestResults(new BlockTests.SuperBlockMasonryMatch((SuperBlock) state.getBlock(), this.getSpecies(), pos));
                b3 = ModelStateData.P3B_MASONRY_JOIN.setValue(SimpleJoin.getIndex(masonryTests), b3);
            }

            bits3 = b3;

            break;

        case FLOW:
            // terrain blocks need larger position space to drive texture randomization because doesn't have per-block rotation or version
            if((stateFlags & STATE_FLAG_NEEDS_POS) == STATE_FLAG_NEEDS_POS) refreshBlockPosFromWorld(pos, 255);

            bits3 = ModelStateData.P3F_FLOW_JOIN.setValue(TerrainState.getBitsFromWorldStatically(this, (SuperBlock)state.getBlock(), state, world, pos), bits3);
            break;

        case MULTIBLOCK:
            break;

        default:
            break;

        }

        this.invalidateHashCode();

        return this;
    }

    /** 
     * Saves world block pos relative to cube boundary specified by mask.
     * Used by BigTex surface painting for texture randomization on non-multiblock shapes.
     */
    private void refreshBlockPosFromWorld(BlockPos pos, int mask)
    {
        long b2 = bits2;
        b2 = ModelStateData.P2_POS_X.setValue((pos.getX() & mask), b2);
        b2 = ModelStateData.P2_POS_Y.setValue((pos.getY() & mask), b2);
        b2 = ModelStateData.P2_POS_Z.setValue((pos.getZ() & mask), b2);
        bits2 = b2;
    }

    ////////////////////////////////////////////////////
    //  PACKER 0 ATTRIBUTES (NOT SHAPE-DEPENDENT)
    ////////////////////////////////////////////////////

    public ModelShape<?> getShape()
    {
        return ModelShape.get(ModelStateData.P0_SHAPE.getValue(bits0));
    }

    /**
     * Also resets shape-specific bits to default for the given shape.
     * Does nothing if shape is the same as existing.
     */
    public void setShape(ModelShape<?> shape)
    {
        if(shape.ordinal() != ModelStateData.P0_SHAPE.getValue(bits0))
        {
            bits0 = ModelStateData.P0_SHAPE.setValue(shape.ordinal(), bits0);
            bits2 = ModelStateData.P2_STATIC_SHAPE_BITS.setValue(shape.meshFactory().defaultShapeStateBits, bits2);
            invalidateHashCode();
            clearStateFlags();
        }
    }

    public ColorMap getColorMap(PaintLayer layer)
    {
        return BlockColorMapProvider.INSTANCE.getColorMap(ModelStateData.P0_PAINT_COLOR[layer.dynamicIndex].getValue(bits0));
    }

    public void setColorMap(PaintLayer layer, ColorMap map)
    {
        bits0 = ModelStateData.P0_PAINT_COLOR[layer.dynamicIndex].setValue(map.ordinal, bits0);
        invalidateHashCode();
    }

    /**
     * Used by placement logic to know if shape has any kind of orientation to it that can be selected during placement.
     */
    public BlockOrientationType orientationType()
    { 
        return getShape().meshFactory().orientationType(this);
    } 
    
    public EnumFacing.Axis getAxis()
    {
        return ModelStateData.P0_AXIS.getValue(bits0);
    }

    public void setAxis(EnumFacing.Axis axis)
    {
        bits0 = ModelStateData.P0_AXIS.setValue(axis, bits0);
        invalidateHashCode();
    }

    public boolean isAxisInverted()
    {
        return ModelStateData.P0_AXIS_INVERTED.getValue(bits0);
    }

    public void setAxisInverted(boolean isInverted)
    {
        bits0 = ModelStateData.P0_AXIS_INVERTED.setValue(isInverted, bits0);
        invalidateHashCode();
    }

    /**
     * For base/lamp paint layers, true means should be rendered in translucent render layer.
     * (Overlay textures always render in translucent layer.)
     * For all paint layers, true also means {@link #getTranslucency()} applies.
     */
    public boolean isTranslucent(PaintLayer layer)
    {
        return ModelStateData.P0_IS_TRANSLUCENT[layer.dynamicIndex].getValue(bits0);
    }

    /**
     * See {@link #isTranslucent(PaintLayer)}
     */
    public void setTranslucent(PaintLayer layer, boolean isTranslucent)
    {
        bits0 =  ModelStateData.P0_IS_TRANSLUCENT[layer.dynamicIndex].setValue(isTranslucent, bits0);
        clearStateFlags();
        invalidateHashCode();
    }

    public boolean isMiddleLayerEnabled()
    {
        return this.getTexture(PaintLayer.MIDDLE) != Textures.NONE;
    }

    public void setMiddleLayerEnabled(boolean isEnabled)
    {
        if(isEnabled && this.getTexture(PaintLayer.MIDDLE) == Textures.NONE)
        {
            this.setTexture(PaintLayer.MIDDLE, Textures.BLOCK_NOISE_STRONG);
        }
        else if(!isEnabled && this.getTexture(PaintLayer.MIDDLE) != Textures.NONE)
        {
            this.setTexture(PaintLayer.MIDDLE, Textures.NONE);
        }
    }

    public boolean isOuterLayerEnabled()
    {
        return this.getTexture(PaintLayer.OUTER) != Textures.NONE;
    }

    public void setOuterLayerEnabled(boolean isEnabled)
    {
        if(isEnabled && this.getTexture(PaintLayer.OUTER) == Textures.NONE)
        {
            this.setTexture(PaintLayer.OUTER, Textures.BLOCK_NOISE_STRONG);
        }
        else if(!isEnabled && this.getTexture(PaintLayer.OUTER) != Textures.NONE)
        {
            this.setTexture(PaintLayer.OUTER, Textures.NONE);
        }
    }

     /**
     * Should only be applied to rendering if {@link #isTranslucent(PaintLayer)} is true
     * for the paint layer being rendered. 
     */
    public Translucency getTranslucency()
    {
        return ModelStateData.P0_TRANSLUCENCY.getValue(bits0);
    }

    /**
     * Will be applied to rendering if {@link #isTranslucent(PaintLayer)} is true
     * for the paint layer being rendered.
     */
    public void setTranslucency(Translucency translucency)
    {
        bits0 = ModelStateData.P0_TRANSLUCENCY.setValue(translucency, bits0);
        invalidateHashCode();
    }

    ////////////////////////////////////////////////////
    //  PACKER 1 ATTRIBUTES (NOT SHAPE-DEPENDENT)
    ////////////////////////////////////////////////////

    public TexturePallette getTexture(PaintLayer layer)
    {
        return Textures.REGISTRY.get(ModelStateData.P1_PAINT_TEXTURE[layer.ordinal()].getValue(bits1));
    }

    public void setTexture(PaintLayer layer, TexturePallette tex)
    {
        bits1 = ModelStateData.P1_PAINT_TEXTURE[layer.ordinal()].setValue(tex.ordinal, bits1);
        invalidateHashCode();
        clearStateFlags();
    }

    /** 
     * Don't use this directly for rendering - user choice may not be workable.
     * Use {@link #getRenderMode(PaintLayer)} instead.
     */
    public boolean isFullBrightness(PaintLayer layer)
    {
        return ModelStateData.P1_PAINT_LIGHT[layer.dynamicIndex].getValue(bits1);
    }

    public void setFullBrightness(PaintLayer layer, boolean isFullBrightness)
    {
        bits1 = ModelStateData.P1_PAINT_LIGHT[layer.dynamicIndex].setValue(isFullBrightness, bits1);
        clearStateFlags();
        invalidateHashCode();
    }

    ////////////////////////////////////////////////////
    //  PACKER 2 ATTRIBUTES  (NOT SHAPE-DEPENDENT)
    ////////////////////////////////////////////////////

    public int getPosX()
    {
        return ModelStateData.P2_POS_X.getValue(bits2);
    }

    public void setPosX(int index)
    {
        bits2 = ModelStateData.P2_POS_X.setValue(index, bits2);
        invalidateHashCode();
    }

    public int getPosY()
    {
        return ModelStateData.P2_POS_Y.getValue(bits2);
    }

    public void setPosY(int index)
    {
        bits2 = ModelStateData.P2_POS_Y.setValue(index, bits2);
        invalidateHashCode();
    }

    public int getPosZ()
    {
        return ModelStateData.P2_POS_Z.getValue(bits2);
    }

    public void setPosZ(int index)
    {
        bits2 = ModelStateData.P2_POS_Z.setValue(index, bits2);
        invalidateHashCode();
    }

    /** Usage is determined by shape. Limited to 44 bits and does not update from world. */
    public long getStaticShapeBits()
    {
        return ModelStateData.P2_STATIC_SHAPE_BITS.getValue(bits2);
    }

    /** usage is determined by shape */        
    public void setStaticShapeBits(long bits)
    {
        bits2 = ModelStateData.P2_STATIC_SHAPE_BITS.setValue(bits, bits2);
        invalidateHashCode();
    }

    ////////////////////////////////////////////////////
    //  PACKER 3 ATTRIBUTES  (BLOCK FORMAT)
    ////////////////////////////////////////////////////

    /**
     * Will return 0 if model state does not include species.
     * This is more convenient than checking each place species is used.
     * @return
     */
    public int getSpecies()
    {
        this.populateStateFlagsIfNeeded();

        if(Configurator.BLOCKS.debugModelState && !this.hasSpecies())
            Log.warn("getSpecies on model state does not apply for shape");

        return this.hasSpecies() ? ModelStateData.P3B_SPECIES.getValue(bits3) : 0;
    }

    public void setSpecies(int species)
    {
        this.populateStateFlagsIfNeeded();

        if(Configurator.BLOCKS.debugModelState && !this.hasSpecies())
            Log.warn("setSpecies on model state does not apply for shape");

        if(this.hasSpecies())
        {
            bits3 = ModelStateData.P3B_SPECIES.setValue(species, bits3);
            invalidateHashCode();
        }
    }

    public CornerJoinBlockState getCornerJoin()
    {
        if(Configurator.BLOCKS.debugModelState)
        {
            populateStateFlagsIfNeeded();
            if((stateFlags & STATE_FLAG_NEEDS_CORNER_JOIN) == 0 || this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
                Log.warn("getCornerJoin on model state does not apply for shape");
        }

        return CornerJoinBlockStateSelector.getJoinState(MathHelper.clamp(ModelStateData.P3B_BLOCK_JOIN.getValue(bits3), 0, CornerJoinBlockStateSelector.BLOCK_JOIN_STATE_COUNT - 1));
    }

    public void setCornerJoin(CornerJoinBlockState join)
    {
        if(Configurator.BLOCKS.debugModelState)
        {
            populateStateFlagsIfNeeded();
            if((stateFlags & STATE_FLAG_NEEDS_CORNER_JOIN) == 0 || this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
                Log.warn("setCornerJoin on model state does not apply for shape");
        }

        bits3 = ModelStateData.P3B_BLOCK_JOIN.setValue(join.getIndex(), bits3);
        invalidateHashCode();
    }

    public SimpleJoin getSimpleJoin()
    {
        if(Configurator.BLOCKS.debugModelState && this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
            Log.warn("getSimpleJoin on model state does not apply for shape");


        // If this state is using corner join, join index is for a corner join
        // and so need to derive simple join from the corner join
        populateStateFlagsIfNeeded();
        return ((stateFlags & STATE_FLAG_NEEDS_CORNER_JOIN) == 0)
                ? new SimpleJoin(ModelStateData.P3B_BLOCK_JOIN.getValue(bits3))
                        : getCornerJoin().simpleJoin;
    }

    public void setSimpleJoin(SimpleJoin join)
    {
        if(Configurator.BLOCKS.debugModelState)
        {
            if(this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
            {
                Log.warn("Ignored setSimpleJoin on model state that does not apply for shape");
                return;
            }

            populateStateFlagsIfNeeded();
            if((stateFlags & STATE_FLAG_NEEDS_CORNER_JOIN) != 0)
            {
                Log.warn("Ignored setSimpleJoin on model state that uses corner join instead");
                return;
            }
        }

        bits3 = ModelStateData.P3B_BLOCK_JOIN.setValue(join.getIndex(), bits3);
        invalidateHashCode();
    }

    public SimpleJoin getMasonryJoin()
    {
        if(Configurator.BLOCKS.debugModelState && (this.getShape().meshFactory().stateFormat != StateFormat.BLOCK || (stateFlags & STATE_FLAG_NEEDS_CORNER_JOIN) == 0) || ((stateFlags & STATE_FLAG_NEEDS_MASONRY_JOIN) == 0))
            Log.warn("getMasonryJoin on model state does not apply for shape");

        populateStateFlagsIfNeeded();
        return new SimpleJoin(ModelStateData.P3B_MASONRY_JOIN.getValue(bits3));
    }

    public void setMasonryJoin(SimpleJoin join)
    {
        if(Configurator.BLOCKS.debugModelState)
        {
            populateStateFlagsIfNeeded();
            if(this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
            {
                Log.warn("Ignored setMasonryJoin on model state that does not apply for shape");
                return;
            }

            if(((stateFlags & STATE_FLAG_NEEDS_CORNER_JOIN) == 0) || ((stateFlags & STATE_FLAG_NEEDS_MASONRY_JOIN) == 0))
            {
                Log.warn("Ignored setMasonryJoin on model state for which it does not apply");
                return;
            }
        }

        bits3 = ModelStateData.P3B_MASONRY_JOIN.setValue(join.getIndex(), bits3);
        invalidateHashCode();
    }

    /**
     * For machines and other blocks with a privileged horizontal face, North is considered the zero rotation.
     */
    public Rotation getAxisRotation()
    {
        return ModelStateData.P3B_AXIS_ROTATION.getValue(bits3);
    }

    /**
     * For machines and other blocks with a privileged horizontal face, North is considered the zero rotation.
     */
    public void setAxisRotation(Rotation rotation)
    {
        populateStateFlagsIfNeeded();
        if(this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
        {
            if(Configurator.BLOCKS.debugModelState) Log.warn("Ignored setAxisRotation on model state that does not apply for shape");
            return;
        }

        if((stateFlags & STATE_FLAG_HAS_AXIS_ROTATION) == 0)
        {
            if(Configurator.BLOCKS.debugModelState) Log.warn("Ignored setAxisRotation on model state for which it does not apply");
            return;
        }

        bits3 = ModelStateData.P3B_AXIS_ROTATION.setValue(rotation, bits3);
        invalidateHashCode();
    }

    ////////////////////////////////////////////////////
    //  PACKER 3 ATTRIBUTES  (MULTI-BLOCK FORMAT)
    ////////////////////////////////////////////////////

    /** Multiblock shapes also get a full 64 bits of information - does not update from world */
    public long getMultiBlockBits()
    {
        if(Configurator.BLOCKS.debugModelState && this.getShape().meshFactory().stateFormat != StateFormat.MULTIBLOCK)
            Log.warn("getMultiBlockBits on model state does not apply for shape");

        return bits3;
    }

    /** Multiblock shapes also get a full 64 bits of information - does not update from world */
    public void setMultiBlockBits(long bits)
    {
        if(Configurator.BLOCKS.debugModelState && this.getShape().meshFactory().stateFormat != StateFormat.MULTIBLOCK)
            Log.warn("setMultiBlockBits on model state does not apply for shape");

        bits3 = bits;
        invalidateHashCode();
    }

    ////////////////////////////////////////////////////
    //  PACKER 3 ATTRIBUTES  (FLOWING TERRAIN FORMAT)
    ////////////////////////////////////////////////////

    public TerrainState getTerrainState()
    {
        if(Configurator.BLOCKS.debugModelState && this.getShape().meshFactory().stateFormat != StateFormat.FLOW)
            Log.warn("getTerrainState on model state does not apply for shape");

        return new TerrainState(ModelStateData.P3F_FLOW_JOIN.getValue(bits3));
    }

    public void setTerrainState(TerrainState flowState)
    {
        if(Configurator.BLOCKS.debugModelState && this.getShape().meshFactory().stateFormat != StateFormat.FLOW)
            Log.warn("setTerrainState on model state does not apply for shape");

        bits3 = ModelStateData.P3F_FLOW_JOIN.setValue(flowState.getStateKey(), bits3);
        invalidateHashCode();
    }


    ////////////////////////////////////////////////////
    //  SHAPE/STATE-DEPENDENT CONVENIENCE METHODS
    ////////////////////////////////////////////////////

    /**
     * Determines what rendering path should apply for the given paint layer
     * based on user choices and the constraints imposed by MC rendering.  
     */
    public RenderPass getRenderPass(PaintLayer layer)
    {
        boolean needsFlat = this.isFullBrightness(layer);
        
        switch(layer)
        {
        case BASE:
        case CUT:
        case LAMP:
        default:
            if(this.isTranslucent(layer))
            {
                return needsFlat ? RenderPass.TRANSLUCENT_FLAT : RenderPass.TRANSLUCENT_SHADED;
            }
            else
            {
                return needsFlat ? RenderPass.SOLID_FLAT : RenderPass.SOLID_SHADED;
            }
            
        case MIDDLE:
        case OUTER:
            return needsFlat ? RenderPass.TRANSLUCENT_FLAT : RenderPass.TRANSLUCENT_SHADED;
        
        }
    }

    public RenderPassSet getRenderPassSet()
    {
       this.populateStateFlagsIfNeeded();
       return STATE_ENUM_RENDER_PASS_SET.getValue(this.stateFlags);
    }
    
    public boolean hasAxis()
    {
        this.populateStateFlagsIfNeeded();
        return (this.stateFlags & STATE_FLAG_HAS_AXIS) == STATE_FLAG_HAS_AXIS;
    }

    public boolean hasAxisOrientation()
    {
        this.populateStateFlagsIfNeeded();
        return (this.stateFlags & STATE_FLAG_HAS_AXIS_ORIENTATION) == STATE_FLAG_HAS_AXIS_ORIENTATION;
    }

    /**
     * True if shape mesh generator can output lamp surface quads.
     */
    public boolean hasLampSurface()
    {
        return this.getShape().meshFactory().hasLampSurface(this);
    }
    
    /**
     * True if base paint layer is translucent or lamp paint layer is present and translucent.
     */
    public boolean hasTranslucentGeometry()
    {
        this.populateStateFlagsIfNeeded();
        return (this.stateFlags & STATE_FLAG_HAS_TRANSLUCENT_GEOMETRY) == STATE_FLAG_HAS_TRANSLUCENT_GEOMETRY;
    }

    public boolean hasAxisRotation()
    {
        this.populateStateFlagsIfNeeded();
        return (this.stateFlags & STATE_FLAG_HAS_AXIS_ROTATION) == STATE_FLAG_HAS_AXIS_ROTATION;
    }
    
    public boolean hasMasonryJoin()
    {
        this.populateStateFlagsIfNeeded();
        return (this.stateFlags & STATE_FLAG_NEEDS_MASONRY_JOIN) == STATE_FLAG_NEEDS_MASONRY_JOIN;
    }

    public boolean hasTextureRotation()
    {
        this.populateStateFlagsIfNeeded();
        return (this.stateFlags & STATE_FLAG_NEEDS_TEXTURE_ROTATION) == STATE_FLAG_NEEDS_TEXTURE_ROTATION;
    }

    /**
     * Means that one or more elements (like a texture) uses species.
     * Does not mean that the shape or block actually capture or generate species other than 0.
     */
    public boolean hasSpecies()
    {
        this.populateStateFlagsIfNeeded();
        return((this.stateFlags & STATE_FLAG_NEEDS_SPECIES) == STATE_FLAG_NEEDS_SPECIES);
    }

    /** Convenience method. Same as shape attribute. */
    public MetaUsage metaUsage()
    {
        return this.getShape().metaUsage();
    }

    /** Convenience method. Same as shape attribute. */
    public boolean isAxisOrthogonalToPlacementFace() 
    {
        return this.getShape().meshFactory().isAxisOrthogonalToPlacementFace();
    }


    /**
     * Retrieves block/item metadata that should apply to this modelState.
     */
    public int getMetaData()
    {
        switch(this.metaUsage())
        {
        case SHAPE:
            return this.getShape().meshFactory().getMetaData(this);

        case SPECIES:
            return this.hasSpecies() ? this.getSpecies() : 0;

        case NONE:
        default:
            if(Configurator.BLOCKS.debugModelState) Log.warn("ModelState.getMetaData called for inappropriate shape");
            return 0;
        }            
    }

    public void setMetaData(int meta)
    {
        switch(this.metaUsage())
        {
        case SHAPE:
            this.getShape().meshFactory().setMetaData(this, meta);
            break;

        case SPECIES:
            if(this.hasSpecies()) this.setSpecies(meta);
            break;

        case NONE:
        default:
            //NOOP
        }            
    }

    /** True if shape can be placed on itself to grow */
    public boolean isAdditive()
    {
        return this.getShape().meshFactory().isAdditive();
    }
    public SideShape sideShape(EnumFacing side)
    {
        return getShape().meshFactory().sideShape(this, side);
    }

    /** returns true if geometry is a full 1x1x1 cube. */
    public boolean isCube()
    {
        return getShape().meshFactory().isCube(this);
    }

    /** 
     * Rotate this block around the given orthogonalAxis if possible, making necessary changes to world state.
     * Return true if successful. 
     * @param blockState 
     */
    public boolean rotateBlock(IBlockState blockState, World world, BlockPos pos, EnumFacing axis, SuperBlock block)
    {
        return getShape().meshFactory().rotateBlock(blockState, world, pos, axis, block, this);
    }

    /** 
     * How much of the sky is occluded by the shape of this block?
     * Based on geometry alone, not transparency.
     * Returns 0 if no occlusion (unlikely result).
     * 1-15 if some occlusion.
     * 255 if fully occludes sky.
     */
    public int geometricSkyOcclusion()
    {
        return getShape().meshFactory().geometricSkyOcclusion(this);
    }

    /** 
     * Returns true if visual elements and geometry match.
     * Does not consider species in matching.
     */
    public boolean doShapeAndAppearanceMatch(ModelState other)
    {
        return (this.bits0 & ModelStateData.P0_APPEARANCE_COMPARISON_MASK) == (other.bits0 & ModelStateData.P0_APPEARANCE_COMPARISON_MASK)
                && (this.bits1 & ModelStateData.P1_APPEARANCE_COMPARISON_MASK) == (other.bits1 & ModelStateData.P1_APPEARANCE_COMPARISON_MASK)
                && (this.bits2 & ModelStateData.P2_APPEARANCE_COMPARISON_MASK) == (other.bits2 & ModelStateData.P2_APPEARANCE_COMPARISON_MASK);
    }

    /** 
     * Returns true if visual elements match.
     * Does not consider species or geometry in matching.
     */
    public boolean doesAppearanceMatch(ModelState other)
    {
        return (this.bits0 & ModelStateData.P0_APPEARANCE_COMPARISON_MASK_NO_GEOMETRY) == (other.bits0 & ModelStateData.P0_APPEARANCE_COMPARISON_MASK_NO_GEOMETRY)
                && (this.bits1 & ModelStateData.P1_APPEARANCE_COMPARISON_MASK) == (other.bits1 & ModelStateData.P1_APPEARANCE_COMPARISON_MASK);
    }

    /** 
     * Returns a copy of this model state with only the bits that matter for geometry.
     * Used as lookup key for block damage models.
     */
    public ModelState geometricState()
    {
        this.populateStateFlagsIfNeeded();
        ModelState result = new ModelState();
        result.setShape(this.getShape());
        result.setStaticShapeBits(this.getStaticShapeBits());

        switch(this.getShape().meshFactory().stateFormat)
        {
        case BLOCK:
            if(this.hasAxis()) result.setAxis(this.getAxis());
            if(this.hasAxisOrientation()) result.setAxisInverted(this.isAxisInverted());
            if(this.hasAxisRotation()) result.setAxisRotation(this.getAxisRotation());
            if((this.getShape().meshFactory().getStateFlags(this) & STATE_FLAG_NEEDS_CORNER_JOIN) == STATE_FLAG_NEEDS_CORNER_JOIN)
            {
                result.setCornerJoin(this.getCornerJoin());
            }
            else if((this.getShape().meshFactory().getStateFlags(this) & STATE_FLAG_NEEDS_SIMPLE_JOIN) == STATE_FLAG_NEEDS_SIMPLE_JOIN)  
            { 
                result.setSimpleJoin(this.getSimpleJoin());
            }
            break;

        case FLOW:
        case MULTIBLOCK:
            result.bits3 = this.bits3;
            break;

        default:
            break;

        }
        return result;
    }

    /**
     * Returns a list of collision boxes offset to the given world position 
     */
    public List<AxisAlignedBB> collisionBoxes(BlockPos offset)
    {
        return this.getShape().meshFactory().collisionHandler().getCollisionBoxes(this, offset);
    }

    public static @Nullable ModelState deserializeFromNBTIfPresent(NBTTagCompound tag)
    {
        if(tag != null && tag.hasKey(ModNBTTag.MODEL_STATE))
        {
            ModelState result = new ModelState();
            result.deserializeNBT(tag);
            return result;
        }
        return null;
    }
    
    /**
     * See {@link Transform#rotateFace(ModelState, EnumFacing)}
     */
    public EnumFacing rotateFace(EnumFacing face)
    {
        return Transform.rotateFace(this, face);
    }
    
    /**
     * Find appropriate transformation assuming base model is oriented to Y orthogonalAxis, positive.
     * This is different than the Minecraft/Forge default because I brain that way.<br><br>
     * See {@link Transform#getMatrix4f(ModelState)}
     */
    public Matrix4f getMatrix4f()
    {
        return Transform.getMatrix4f(this);
    }
    
    /** for compatibility with double-valued raw quad vertices */
    public Matrix4d getMatrix4d()
    {
        return new Matrix4d(this.getMatrix4f());
    }
    
    
    //TODO: serialize shape by system name to allow pack/mod changes in same world
    
    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        this.deserializeNBT(tag, ModNBTTag.MODEL_STATE);
    }
    
    /**
     * Use when you need to avoid tag name conflicts / have more than one.
     */
    public void deserializeNBT(NBTTagCompound tag, String tagName)
    {
        int[] stateBits = tag.getIntArray(tagName);
        if(stateBits == null || stateBits.length != 8)
        {
            Log.warn("Bad or missing data encounter during ModelState NBT deserialization.");
            return;
        }
        this.deserializeFromInts(stateBits);
        this.clearStateFlags();
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        this.serializeNBT(tag, ModNBTTag.MODEL_STATE);
    }

    /**
     * Use when you need to avoid tag name conflicts / have more than one.
     */
    public void serializeNBT(NBTTagCompound tag, String tagName)
    {
        tag.setIntArray(tagName, this.serializeToInts());
    }

    @Override
    public void fromBytes(PacketBuffer pBuff)
    {
        this.bits0 = pBuff.readLong();
        this.bits1 = pBuff.readLong();
        this.bits2 = pBuff.readLong();
        this.bits3 = pBuff.readLong();
    }

    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeLong(this.bits0);
        pBuff.writeLong(this.bits1);
        pBuff.writeLong(this.bits2);
        pBuff.writeLong(this.bits3);
    }
}