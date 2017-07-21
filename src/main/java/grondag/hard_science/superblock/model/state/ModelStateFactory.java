package grondag.hard_science.superblock.model.state;


import java.util.List;

import grondag.hard_science.Log;
import grondag.hard_science.library.varia.BinaryEnumSet;
import grondag.hard_science.library.varia.BitPacker;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.library.varia.BitPacker.BitElement.BooleanElement;
import grondag.hard_science.library.varia.BitPacker.BitElement.EnumElement;
import grondag.hard_science.library.varia.BitPacker.BitElement.IntElement;
import grondag.hard_science.library.varia.BitPacker.BitElement.LongElement;
import grondag.hard_science.library.world.CornerJoinBlockState;
import grondag.hard_science.library.world.CornerJoinBlockStateSelector;
import grondag.hard_science.library.world.NeighborBlocks;
import grondag.hard_science.library.world.Rotation;
import grondag.hard_science.library.world.SimpleJoin;
import grondag.hard_science.library.world.NeighborBlocks.NeighborTestResults;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.collision.SideShape;
import grondag.hard_science.superblock.color.BlockColorMapProvider;
import grondag.hard_science.superblock.color.ColorMap;
import grondag.hard_science.superblock.model.shape.ModelShape;
import grondag.hard_science.superblock.terrain.TerrainState;
import grondag.hard_science.superblock.texture.Textures;
import grondag.hard_science.superblock.texture.TexturePalletteRegistry.TexturePallette;
import grondag.hard_science.superblock.varia.BlockTests;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class ModelStateFactory
{
    //    private static final IAlternator[] ROTATION_ALTERNATOR = new IAlternator[TextureScale.values().length];

    
    //package scope to allow inspection in test harness
    static final BitPacker PACKER_0 = new BitPacker();
    private static final EnumElement<ModelShape> P0_SHAPE = PACKER_0.createEnumElement(ModelShape.class);
    private static final IntElement[] P0_PAINT_COLOR = new IntElement[PaintLayer.DYNAMIC_SIZE];
    private static final BooleanElement P0_AXIS_INVERTED = PACKER_0.createBooleanElement();
    private static final EnumElement<EnumFacing.Axis> P0_AXIS = PACKER_0.createEnumElement(EnumFacing.Axis.class);
    private static final BooleanElement[] P0_IS_TRANSLUCENT = new BooleanElement[PaintLayer.DYNAMIC_SIZE];
    private static final EnumElement<Translucency> P0_TRANSLUCENCY = PACKER_0.createEnumElement(Translucency.class);

    static final BitPacker PACKER_1 = new BitPacker();
    private static final IntElement[] P1_PAINT_TEXTURE = new IntElement[PaintLayer.STATIC_SIZE];
    private static final BooleanElement[] P1_PAINT_LIGHT= new BooleanElement[PaintLayer.DYNAMIC_SIZE];

    /** note that sign bit on packer 2 is reserved to persist static state during serialization */ 
    static final BitPacker PACKER_2 = new BitPacker();
    private static final IntElement P2_POS_X = PACKER_2.createIntElement(256);
    private static final IntElement P2_POS_Y = PACKER_2.createIntElement(256);
    private static final IntElement P2_POS_Z = PACKER_2.createIntElement(256);
    /** value semantics are owned by consumer - only constraints are size (39 bits) and does not update from world */
    private static final LongElement P2_STATIC_SHAPE_BITS = PACKER_2.createLongElement(1L << 39);

    static final BitPacker PACKER_3_BLOCK = new BitPacker();
    private static final IntElement P3B_SPECIES = PACKER_3_BLOCK.createIntElement(16);
    private static final IntElement P3B_BLOCK_JOIN = PACKER_3_BLOCK.createIntElement(CornerJoinBlockStateSelector.BLOCK_JOIN_STATE_COUNT);
    private static final IntElement P3B_MASONRY_JOIN = PACKER_3_BLOCK.createIntElement(SimpleJoin.STATE_COUNT);
    private static final EnumElement<Rotation> P3B_MODEL_ROTATION = PACKER_3_BLOCK.createEnumElement(Rotation.class);

    static final BitPacker PACKER_3_FLOW = new BitPacker();
    private static final LongElement P3F_FLOW_JOIN = PACKER_3_FLOW.createLongElement(TerrainState.STATE_BIT_MASK + 1);

    static final BitPacker PACKER_3_MULTIBLOCK = new BitPacker();

    /** used to compare states quickly for border joins  */
    private static final long P0_APPEARANCE_COMPARISON_MASK;
    private static final long P1_APPEARANCE_COMPARISON_MASK;
    private static final long P2_APPEARANCE_COMPARISON_MASK;   

    /** used to compare states quickly for appearance match */
    private static final long P0_APPEARANCE_COMPARISON_MASK_NO_GEOMETRY;

    static
    {
        long borderMask0 = 0;
        long borderMask1 = 0;
        for(int i = 0; i < PaintLayer.STATIC_SIZE; i++)
        {
            P1_PAINT_TEXTURE[i] = PACKER_1.createIntElement(Textures.MAX_TEXTURES);
        }

        for(int i = 0; i < PaintLayer.DYNAMIC_SIZE; i++)
        {
            P0_PAINT_COLOR[i] = PACKER_0.createIntElement(BlockColorMapProvider.INSTANCE.getColorMapCount()); 
            P0_IS_TRANSLUCENT[i] = PACKER_0.createBooleanElement();
            P1_PAINT_LIGHT[i] = PACKER_1.createBooleanElement(); 

            borderMask0 |= P0_PAINT_COLOR[i].comparisonMask();
            borderMask0 |= P0_IS_TRANSLUCENT[i].comparisonMask();
            borderMask1 |= P1_PAINT_TEXTURE[i].comparisonMask();
            borderMask1 |= P1_PAINT_LIGHT[i].comparisonMask();
        }

        P0_APPEARANCE_COMPARISON_MASK_NO_GEOMETRY = borderMask0
                | P0_TRANSLUCENCY.comparisonMask();

        P0_APPEARANCE_COMPARISON_MASK = P0_APPEARANCE_COMPARISON_MASK_NO_GEOMETRY
                | P0_SHAPE.comparisonMask() 
                | P0_AXIS.comparisonMask()
                | P0_AXIS_INVERTED.comparisonMask();

        P1_APPEARANCE_COMPARISON_MASK = borderMask1;
        P2_APPEARANCE_COMPARISON_MASK = P2_STATIC_SHAPE_BITS.comparisonMask();
    }

    //hide constructor
    private ModelStateFactory()
    {
        super();
    }

    public static class ModelState
    {
        /**
         * For readability.
         */
        public static final int STATE_FLAG_NONE = 0;

        /* 
         * Enables lazy derivation - set after derivation is complete.
         * NB - check logic assumes that ALL bits are zero for simplicity.
         */
        public static final int STATE_FLAG_IS_POPULATED = 1;

        /** 
         * Applies to block-type states.  
         * True if is a block type state and requires full join state.
         */
        public static final int STATE_FLAG_NEEDS_CORNER_JOIN = STATE_FLAG_IS_POPULATED << 1;

        /** 
         * Applies to block-type states.  
         * True if is a block type state and requires full join state.
         */
        public static final int STATE_FLAG_NEEDS_SIMPLE_JOIN = STATE_FLAG_NEEDS_CORNER_JOIN << 1;

        /** 
         * Applies to block-type states.  
         * True if is a block type state and requires masonry join info.
         */
        public static final int STATE_FLAG_NEEDS_MASONRY_JOIN = STATE_FLAG_NEEDS_SIMPLE_JOIN << 1;

        /** 
         * True if position (big-tex) world state is needed. Applies for block and flow state formats.
         */
        public static final int STATE_FLAG_NEEDS_POS = STATE_FLAG_NEEDS_MASONRY_JOIN << 1;


        public static final int STATE_FLAG_NEEDS_SPECIES = STATE_FLAG_NEEDS_POS << 1;

        public static final int STATE_FLAG_HAS_AXIS = STATE_FLAG_NEEDS_SPECIES << 1;

        public static final int STATE_FLAG_NEEDS_TEXTURE_ROTATION = STATE_FLAG_HAS_AXIS << 1;

        public static final int STATE_FLAG_HAS_AXIS_ORIENTATION = STATE_FLAG_NEEDS_TEXTURE_ROTATION << 1;

        /** Set if shape can be rotated. Only applies to block models; multiblock models manage this situationally. */
        public static final int STATE_FLAG_HAS_MODEL_ROTATION = STATE_FLAG_HAS_AXIS_ORIENTATION << 1;

        /** Set if Base paint layer is non-translucent and should be rendered on Cutout Mipped render layer */
        public static final int STATE_FLAG_IS_BASE_CUTOUT = STATE_FLAG_HAS_MODEL_ROTATION << 1;

        /** Set if Lamp paint layer is non-translucent and should be rendered on Cutout Mipped render layer */
        public static final int STATE_FLAG_IS_LAMP_CUTOUT = STATE_FLAG_IS_BASE_CUTOUT << 1;

        ///////////////////////////////////
        // RENDER LAYER FLAGS
        // Note that order must match Enum ordinal so that BENUMSET_RENDER_LAYER can use these.
        ///////////////////////////////////
        
        /** Set if Solid render layer is present */
        public static final int STATE_FLAG_HAS_RENDER_LAYER_SOLID = STATE_FLAG_IS_LAMP_CUTOUT << 1;

        /** Set if Cutout Mipped render layer is present */
        public static final int STATE_FLAG_HAS_RENDER_LAYER_CUTOUT_MIPPED = STATE_FLAG_HAS_RENDER_LAYER_SOLID << 1;
        
        /** Set if Cutout render layer is present.  Not used, but needed for proper flag order. */
        public static final int STATE_FLAG_HAS_RENDER_LAYER_CUTOUT = STATE_FLAG_HAS_RENDER_LAYER_CUTOUT_MIPPED << 1;

        /** Set if Translucent render layer is present */
        public static final int STATE_FLAG_HAS_RENDER_LAYER_TRANSLUCENT = STATE_FLAG_HAS_RENDER_LAYER_CUTOUT << 1;
        
        /**
         * See {@link #getCanRenderInLayerFlags()}
         */
        private static final int RENDER_LAYER_FLAG_SHIFT = Useful.bitLength(STATE_FLAG_HAS_RENDER_LAYER_SOLID);

        ///////////////////////////////////
        // FULL BRIGHTNESS FLAGS
        // Note that order must match Enum ordinal so that BENUMSET_RENDER_LAYER can use these.
        ///////////////////////////////////
        
        /** Set if Solid render layer is present and full brightness */
        public static final int STATE_FLAG_FULLBRIGHT_RENDER_LAYER_SOLID = STATE_FLAG_HAS_RENDER_LAYER_TRANSLUCENT << 1;

        /** Set if Cutout Mipped render layer is present and full brightness */
        public static final int STATE_FLAG_FULLBRIGHT_RENDER_LAYER_CUTOUT_MIPPED = STATE_FLAG_FULLBRIGHT_RENDER_LAYER_SOLID << 1;

        /** Set if Cutout render layer is present and full brightness.  Not used, but needed for proper flag order.  */
        public static final int STATE_FLAG_FULLBRIGHT_RENDER_LAYER_CUTOUT = STATE_FLAG_FULLBRIGHT_RENDER_LAYER_CUTOUT_MIPPED << 1;
        
        /** Set if Translucent render layer is present and full brightness */
        public static final int STATE_FLAG_FULLBRIGHT_RENDER_LAYER_TRANSLUCENT = STATE_FLAG_FULLBRIGHT_RENDER_LAYER_CUTOUT << 1;

        /**
         * See {@link #getRenderLayerShadedFlags()}
         */
        private static final int FULLBRIGHT_FLAG_SHIFT = Useful.bitLength(STATE_FLAG_FULLBRIGHT_RENDER_LAYER_SOLID);
        
        /** Set if either Base/Cut or Lamp (if present) paint layers are translucent */
        public static final int STATE_FLAG_HAS_TRANSLUCENT_GEOMETRY = STATE_FLAG_FULLBRIGHT_RENDER_LAYER_TRANSLUCENT << 1;

        /** sign bit is used to indicate static state */
        private static final int INT_SIGN_BIT = 1 << 31;
        private static final int INT_SIGN_BIT_INVERSE = ~INT_SIGN_BIT;

        /** use this to turn off flags that should not be used with non-block state formats */
        public static final int STATE_FLAG_DISABLE_BLOCK_ONLY = ~(
                STATE_FLAG_NEEDS_CORNER_JOIN | STATE_FLAG_NEEDS_SIMPLE_JOIN | STATE_FLAG_NEEDS_MASONRY_JOIN
                | STATE_FLAG_NEEDS_SPECIES | STATE_FLAG_NEEDS_TEXTURE_ROTATION);


        public static final BinaryEnumSet<BlockRenderLayer> BENUMSET_RENDER_LAYER = new BinaryEnumSet<BlockRenderLayer>(BlockRenderLayer.class);

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
            // sign on third long word is used to store static indicator
            this.isStatic = (INT_SIGN_BIT & bits[4]) == INT_SIGN_BIT;

            bits0 = ((long)bits[0]) << 32 | (bits[1] & 0xffffffffL);
            bits1 = ((long)bits[2]) << 32 | (bits[3] & 0xffffffffL);
            bits2 = ((long)(INT_SIGN_BIT_INVERSE & bits[4])) << 32 | (bits[5] & 0xffffffffL);
            bits3 = ((long)bits[6]) << 32 | (bits[7] & 0xffffffffL);
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

        public int[] getBitsIntArray() 
        {
            int[] result = new int[8];
            result[0] = (int) (bits0 >> 32);
            result[1] = (int) (bits0);

            result[2] = (int) (bits1 >> 32);
            result[3] = (int) (bits1);

            result[4] = (int) (this.isStatic ? (bits2 >> 32) | INT_SIGN_BIT : (bits2 >> 32));
            result[5] = (int) (bits2);

            result[6] = (int) (bits3 >> 32);
            result[7] = (int) (bits3);
            return result;
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

            if(obj instanceof ModelState && obj != null)
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

            if(obj instanceof ModelState && obj != null)
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

            if(this.metaUsage() != MetaUsage.NONE)
            {
                this.setMetaData(state.getValue(SuperBlock.META));
            }

            switch(this.getShape().meshFactory().stateFormat)
            {
            case BLOCK:

                if((stateFlags & STATE_FLAG_NEEDS_POS) == STATE_FLAG_NEEDS_POS) refreshBlockPosFromWorld(pos, 255);

                long b3 = bits3;

                NeighborBlocks neighbors = null;

                if((STATE_FLAG_NEEDS_CORNER_JOIN & stateFlags) == STATE_FLAG_NEEDS_CORNER_JOIN)
                {
                    //                    Output.getLog().info("ModelState.refreshFromWorld corner join refresh @" + pos.toString());
                    neighbors = new NeighborBlocks(world, pos, false);
                    NeighborTestResults tests = neighbors.getNeighborTestResults(new BlockTests.SuperBlockBorderMatch((SuperBlock) state.getBlock(), this, true));


                    b3 = P3B_BLOCK_JOIN.setValue(CornerJoinBlockStateSelector.findIndex(tests), b3);
                }
                else if ((STATE_FLAG_NEEDS_SIMPLE_JOIN & stateFlags) == STATE_FLAG_NEEDS_SIMPLE_JOIN)
                {
                    neighbors = new NeighborBlocks(world, pos, false);
                    NeighborTestResults tests = neighbors.getNeighborTestResults(new BlockTests.SuperBlockBorderMatch((SuperBlock) state.getBlock(), this, true));
                    b3 = P3B_BLOCK_JOIN.setValue(SimpleJoin.getIndex(tests), b3);
                }

                if((STATE_FLAG_NEEDS_MASONRY_JOIN & stateFlags) == STATE_FLAG_NEEDS_MASONRY_JOIN)
                {
                    if(neighbors == null) neighbors = new NeighborBlocks(world, pos, false);
                    NeighborTestResults masonryTests = neighbors.getNeighborTestResults(new BlockTests.SuperBlockMasonryMatch((SuperBlock) state.getBlock(), this.getSpecies(), pos));
                    b3 = P3B_MASONRY_JOIN.setValue(SimpleJoin.getIndex(masonryTests), b3);
                }

                bits3 = b3;

                break;

            case FLOW:
                // terrain blocks need larger position space to drive texture randomization because doesn't have per-block rotation or version
                if((stateFlags & STATE_FLAG_NEEDS_POS) == STATE_FLAG_NEEDS_POS) refreshBlockPosFromWorld(pos, 255);

                bits3 = P3F_FLOW_JOIN.setValue(TerrainState.getBitsFromWorldStatically(this, (SuperBlock)state.getBlock(), state, world, pos), bits3);
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
            b2 = P2_POS_X.setValue((pos.getX() & mask), b2);
            b2 = P2_POS_Y.setValue((pos.getY() & mask), b2);
            b2 = P2_POS_Z.setValue((pos.getZ() & mask), b2);
            bits2 = b2;
        }

        ////////////////////////////////////////////////////
        //  PACKER 0 ATTRIBUTES (NOT SHAPE-DEPENDENT)
        ////////////////////////////////////////////////////

        public ModelShape getShape()
        {
            return P0_SHAPE.getValue(bits0);
        }

        /**
         * Also resets shape-specific bits to default for the given shape.
         * Does nothing if shape is the same as existing.
         */
        public void setShape(ModelShape shape)
        {
            if(shape != P0_SHAPE.getValue(bits0))
            {
                bits0 = P0_SHAPE.setValue(shape, bits0);
                bits2 = P2_STATIC_SHAPE_BITS.setValue(shape.meshFactory().defaultShapeStateBits, bits2);
                invalidateHashCode();
                clearStateFlags();
            }
        }

        public ColorMap getColorMap(PaintLayer layer)
        {
            return BlockColorMapProvider.INSTANCE.getColorMap(P0_PAINT_COLOR[layer.dynamicIndex].getValue(bits0));
        }

        public void setColorMap(PaintLayer layer, ColorMap map)
        {
            bits0 = P0_PAINT_COLOR[layer.dynamicIndex].setValue(map.ordinal, bits0);
            invalidateHashCode();
        }

        public EnumFacing.Axis getAxis()
        {
            return P0_AXIS.getValue(bits0);
        }

        public void setAxis(EnumFacing.Axis axis)
        {
            bits0 = P0_AXIS.setValue(axis, bits0);
            invalidateHashCode();
        }

        public boolean isAxisInverted()
        {
            return P0_AXIS_INVERTED.getValue(bits0);
        }

        public void setAxisInverted(boolean isInverted)
        {
            bits0 = P0_AXIS_INVERTED.setValue(isInverted, bits0);
            invalidateHashCode();
        }

        /**
         * For base/lamp paint layers, true means should be rendered in translucent render layer.
         * (Overlay textures always render in translucent layer.)
         * For all paint layers, true also means {@link #getTranslucency()} applies.
         */
        public boolean isTranslucent(PaintLayer layer)
        {
            return P0_IS_TRANSLUCENT[layer.dynamicIndex].getValue(bits0);
        }

        /**
         * See {@link #isTranslucent(PaintLayer)}
         */
        public void setTranslucent(PaintLayer layer, boolean isTranslucent)
        {
            bits0 =  P0_IS_TRANSLUCENT[layer.dynamicIndex].setValue(isTranslucent, bits0);
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

        /** returns true if any surface painter is configured to have the given layer */
        public boolean canRenderInLayer(BlockRenderLayer renderLayer)
        {
            this.populateStateFlagsIfNeeded();
            switch(renderLayer)
            {
            case SOLID:
                return (this.stateFlags & STATE_FLAG_HAS_RENDER_LAYER_SOLID) != 0;
                
            case CUTOUT_MIPPED:
                return (this.stateFlags & STATE_FLAG_HAS_RENDER_LAYER_CUTOUT_MIPPED) != 0;
                
            case TRANSLUCENT:
                return (this.stateFlags & STATE_FLAG_HAS_RENDER_LAYER_TRANSLUCENT) != 0;
                
            case CUTOUT:
            default:
                return false;
            }
        }

        /** 
         * Exposed for use as a lookup key in model dispatch logic. 
         * Identifies which block render layers can be rendered in this model.
         * Can be decoded with {@link #BENUMSET_RENDER_LAYER} 
         */
        public byte getCanRenderInLayerFlags() 
        { 
            this.populateStateFlagsIfNeeded();
            return (byte) ((this.stateFlags >> RENDER_LAYER_FLAG_SHIFT) & 0xF); 
        };

        /**
         * Should only be applied to rendering if {@link #isTranslucent(PaintLayer)} is true
         * for the paint layer being rendered. 
         */
        public Translucency getTranslucency()
        {
            return P0_TRANSLUCENCY.getValue(bits0);
        }

        /**
         * Will be applied to rendering if {@link #isTranslucent(PaintLayer)} is true
         * for the paint layer being rendered.
         */
        public void setTranslucency(Translucency translucency)
        {
            bits0 = P0_TRANSLUCENCY.setValue(translucency, bits0);
            invalidateHashCode();
        }

        ////////////////////////////////////////////////////
        //  PACKER 1 ATTRIBUTES (NOT SHAPE-DEPENDENT)
        ////////////////////////////////////////////////////

        public TexturePallette getTexture(PaintLayer layer)
        {
            return Textures.REGISTRY.get(P1_PAINT_TEXTURE[layer.ordinal()].getValue(bits1));
        }

        public void setTexture(PaintLayer layer, TexturePallette tex)
        {
            bits1 = P1_PAINT_TEXTURE[layer.ordinal()].setValue(tex.ordinal, bits1);
            invalidateHashCode();
            clearStateFlags();
        }

        /** 
         * Don't use this directly for rendering - user choice may not be workable.
         * Use {@link #getRenderMode(PaintLayer)} instead.
         */
        public boolean isFullBrightness(PaintLayer layer)
        {
            return P1_PAINT_LIGHT[layer.dynamicIndex].getValue(bits1);
        }

        public void setFullBrightness(PaintLayer layer, boolean isFullBrightness)
        {
            bits1 = P1_PAINT_LIGHT[layer.dynamicIndex].setValue(isFullBrightness, bits1);
            clearStateFlags();
            invalidateHashCode();
        }

        /** returns true if no surface painters for the given layer are configured for full brightness rendering */
        public boolean isLayerShaded(BlockRenderLayer renderLayer)
        {
            this.populateStateFlagsIfNeeded();
            switch(renderLayer)
            {
            case SOLID:
                return (this.stateFlags & STATE_FLAG_FULLBRIGHT_RENDER_LAYER_SOLID) == 0;
                
            case CUTOUT_MIPPED:
                return (this.stateFlags & STATE_FLAG_FULLBRIGHT_RENDER_LAYER_CUTOUT_MIPPED) == 0;
                
            case TRANSLUCENT:
                return (this.stateFlags & STATE_FLAG_FULLBRIGHT_RENDER_LAYER_TRANSLUCENT) == 0;
                
            case CUTOUT:
            default:
                return true;
            }
        }

        /** 
         * Exposed for use as a lookup key in model dispatch logic. 
         * Identifies which block render layers should be rendered with AO (not full brightness).
         * Can be decoded with {@link ModelState#BENUMSET_RENDER_LAYER} 
         */
        public byte getRenderLayerShadedFlags() 
        { 
            this.populateStateFlagsIfNeeded();
            return (byte) ((~(this.stateFlags >> FULLBRIGHT_FLAG_SHIFT)) & 0xF);
        };

        ////////////////////////////////////////////////////
        //  PACKER 2 ATTRIBUTES  (NOT SHAPE-DEPENDENT)
        ////////////////////////////////////////////////////

        public int getPosX()
        {
            return P2_POS_X.getValue(bits2);
        }

        public void setPosX(int index)
        {
            bits2 = P2_POS_X.setValue(index, bits2);
            invalidateHashCode();
        }

        public int getPosY()
        {
            return P2_POS_Y.getValue(bits2);
        }

        public void setPosY(int index)
        {
            bits2 = P2_POS_Y.setValue(index, bits2);
            invalidateHashCode();
        }

        public int getPosZ()
        {
            return P2_POS_Z.getValue(bits2);
        }

        public void setPosZ(int index)
        {
            bits2 = P2_POS_Z.setValue(index, bits2);
            invalidateHashCode();
        }

        /** Usage is determined by shape. Limited to 44 bits and does not update from world. */
        public long getStaticShapeBits()
        {
            return P2_STATIC_SHAPE_BITS.getValue(bits2);
        }

        /** usage is determined by shape */        
        public void setStaticShapeBits(long bits)
        {
            bits2 = P2_STATIC_SHAPE_BITS.setValue(bits, bits2);
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

            if(Log.DEBUG_MODE && !this.hasSpecies())
                Log.warn("getSpecies on model state does not apply for shape");

            return this.hasSpecies() ? P3B_SPECIES.getValue(bits3) : 0;
        }

        public void setSpecies(int species)
        {
            this.populateStateFlagsIfNeeded();

            if(Log.DEBUG_MODE && !this.hasSpecies())
                Log.warn("setSpecies on model state does not apply for shape");

            if(this.hasSpecies())
            {
                bits3 = P3B_SPECIES.setValue(species, bits3);
                invalidateHashCode();
            }
        }

        public CornerJoinBlockState getCornerJoin()
        {
            if(Log.DEBUG_MODE)
            {
                populateStateFlagsIfNeeded();
                if((stateFlags & STATE_FLAG_NEEDS_CORNER_JOIN) == 0 || this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
                    Log.warn("getCornerJoin on model state does not apply for shape");
            }

            return CornerJoinBlockStateSelector.getJoinState(P3B_BLOCK_JOIN.getValue(bits3));
        }

        public void setCornerJoin(CornerJoinBlockState join)
        {
            if(Log.DEBUG_MODE)
            {
                populateStateFlagsIfNeeded();
                if((stateFlags & STATE_FLAG_NEEDS_CORNER_JOIN) == 0 || this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
                    Log.warn("setCornerJoin on model state does not apply for shape");
            }

            bits3 = P3B_BLOCK_JOIN.setValue(join.getIndex(), bits3);
            invalidateHashCode();
        }

        public SimpleJoin getSimpleJoin()
        {
            if(Log.DEBUG_MODE && this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
                Log.warn("getSimpleJoin on model state does not apply for shape");


            // If this state is using corner join, join index is for a corner join
            // and so need to derive simple join from the corner join
            populateStateFlagsIfNeeded();
            return ((stateFlags & STATE_FLAG_NEEDS_CORNER_JOIN) == 0)
                    ? new SimpleJoin(P3B_BLOCK_JOIN.getValue(bits3))
                            : getCornerJoin().simpleJoin;
        }

        public void setSimpleJoin(SimpleJoin join)
        {
            if(Log.DEBUG_MODE)
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

            bits3 = P3B_BLOCK_JOIN.setValue(join.getIndex(), bits3);
            invalidateHashCode();
        }

        public SimpleJoin getMasonryJoin()
        {
            if(Log.DEBUG_MODE && (this.getShape().meshFactory().stateFormat != StateFormat.BLOCK || (stateFlags & STATE_FLAG_NEEDS_CORNER_JOIN) == 0) || ((stateFlags & STATE_FLAG_NEEDS_MASONRY_JOIN) == 0))
                Log.warn("getMasonryJoin on model state does not apply for shape");

            populateStateFlagsIfNeeded();
            return new SimpleJoin(P3B_MASONRY_JOIN.getValue(bits3));
        }

        public void setMasonryJoin(SimpleJoin join)
        {
            if(Log.DEBUG_MODE)
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

            bits3 = P3B_MASONRY_JOIN.setValue(join.getIndex(), bits3);
            invalidateHashCode();
        }


        public Rotation getModelRotation()
        {
            if(Log.DEBUG_MODE)
            {
                populateStateFlagsIfNeeded();
                if(this.getShape().meshFactory().stateFormat != StateFormat.BLOCK || (stateFlags & STATE_FLAG_HAS_MODEL_ROTATION) == 0)
                    Log.warn("getModelRotation on model state does not apply for shape");
            }
            return P3B_MODEL_ROTATION.getValue(bits3);
        }

        public void setModelRotation(Rotation rotation)
        {
            populateStateFlagsIfNeeded();
            if(this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
            {
                if(Log.DEBUG_MODE) Log.warn("Ignored setModelRotation on model state that does not apply for shape");
                return;
            }

            if((stateFlags & STATE_FLAG_HAS_MODEL_ROTATION) == 0)
            {
                if(Log.DEBUG_MODE) Log.warn("Ignored setModelRotation on model state for which it does not apply");
                return;
            }

            bits3 = P3B_MODEL_ROTATION.setValue(rotation, bits3);
            invalidateHashCode();
        }

        ////////////////////////////////////////////////////
        //  PACKER 3 ATTRIBUTES  (MULTI-BLOCK FORMAT)
        ////////////////////////////////////////////////////

        /** Multiblock shapes also get a full 64 bits of information - does not update from world */
        public long getMultiBlockBits()
        {
            if(Log.DEBUG_MODE && this.getShape().meshFactory().stateFormat != StateFormat.MULTIBLOCK)
                Log.warn("getMultiBlockBits on model state does not apply for shape");

            return bits3;
        }

        /** Multiblock shapes also get a full 64 bits of information - does not update from world */
        public void setMultiBlockBits(long bits)
        {
            if(Log.DEBUG_MODE && this.getShape().meshFactory().stateFormat != StateFormat.MULTIBLOCK)
                Log.warn("setMultiBlockBits on model state does not apply for shape");

            bits3 = bits;
            invalidateHashCode();
        }

        ////////////////////////////////////////////////////
        //  PACKER 3 ATTRIBUTES  (FLOWING TERRAIN FORMAT)
        ////////////////////////////////////////////////////

        public TerrainState getTerrainState()
        {
            if(Log.DEBUG_MODE && this.getShape().meshFactory().stateFormat != StateFormat.FLOW)
                Log.warn("getTerrainState on model state does not apply for shape");

            return new TerrainState(P3F_FLOW_JOIN.getValue(bits3));
        }

        public void setTerrainState(TerrainState flowState)
        {
            if(Log.DEBUG_MODE && this.getShape().meshFactory().stateFormat != StateFormat.FLOW)
                Log.warn("setTerrainState on model state does not apply for shape");

            bits3 = P3F_FLOW_JOIN.setValue(flowState.getStateKey(), bits3);
            invalidateHashCode();
        }


        ////////////////////////////////////////////////////
        //  SHAPE/STATE-DEPENDENT CONVENIENCE METHODS
        ////////////////////////////////////////////////////

        /**
         * Determines what block render layer and shading mode should be used
         * based on user choices and the constraints imposed by MC rendering.  
         * Not all combinations that can be requested will render successfully.  
         * Will provide closest possible match to what user requested.
         */
        public RenderMode getRenderMode(PaintLayer layer)
        {
            this.populateStateFlagsIfNeeded();
            
            switch(layer)
            {
            case BASE:
            case CUT:
                if(this.isTranslucent(PaintLayer.BASE))
                {
                    return (this.stateFlags & STATE_FLAG_FULLBRIGHT_RENDER_LAYER_TRANSLUCENT) == 0
                            ? RenderMode.TRANSLUCENT_SHADED 
                            : RenderMode.TRANSLUCENT_GLOW;
                }
                else if((this.stateFlags & STATE_FLAG_IS_BASE_CUTOUT) == 0)
                {
                    return (this.stateFlags & STATE_FLAG_FULLBRIGHT_RENDER_LAYER_SOLID) == 0
                            ? RenderMode.SOLID_SHADED 
                            : RenderMode.SOLID_GLOW;
                }
                else
                {
                    return (this.stateFlags & STATE_FLAG_FULLBRIGHT_RENDER_LAYER_CUTOUT_MIPPED) == 0
                            ? RenderMode.CUTOUT_SHADED 
                            : RenderMode.CUTOUT_GLOW;
                }
                
            case LAMP:
                if(this.isTranslucent(PaintLayer.LAMP))
                {
                    return (this.stateFlags & STATE_FLAG_FULLBRIGHT_RENDER_LAYER_TRANSLUCENT) == 0
                            ? RenderMode.TRANSLUCENT_SHADED 
                            : RenderMode.TRANSLUCENT_GLOW;
                }
                else if((this.stateFlags & STATE_FLAG_IS_LAMP_CUTOUT) == 0)
                {
                    return (this.stateFlags & STATE_FLAG_FULLBRIGHT_RENDER_LAYER_SOLID) == 0
                            ? RenderMode.SOLID_SHADED 
                            : RenderMode.SOLID_GLOW;
                }
                else
                {
                    return (this.stateFlags & STATE_FLAG_FULLBRIGHT_RENDER_LAYER_CUTOUT_MIPPED) == 0
                            ? RenderMode.CUTOUT_SHADED 
                            : RenderMode.CUTOUT_GLOW;
                }
                
            case MIDDLE:
            case OUTER:
                return (this.stateFlags & STATE_FLAG_FULLBRIGHT_RENDER_LAYER_TRANSLUCENT) == 0
                    ? RenderMode.TRANSLUCENT_SHADED 
                    : RenderMode.TRANSLUCENT_GLOW;
            }
            
            // never gets here
            return RenderMode.SOLID_SHADED;
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

        public boolean hasModelRotation()
        {
            this.populateStateFlagsIfNeeded();
            return (this.stateFlags & STATE_FLAG_HAS_MODEL_ROTATION) == STATE_FLAG_HAS_MODEL_ROTATION;
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

        public boolean hasSpecies()
        {
            this.populateStateFlagsIfNeeded();
            return((this.stateFlags & STATE_FLAG_NEEDS_SPECIES) == STATE_FLAG_NEEDS_SPECIES);
        }

        /** Convenience method. Same as shape attribute. */
        public MetaUsage metaUsage()
        {
            return this.getShape().metaUsage;
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
                if(Log.DEBUG_MODE) Log.warn("ModelState.getMetaData called for inappropriate shape");
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
                if(Log.DEBUG_MODE) Log.warn("ModelState.setMetaData called for inappropriate shape");
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
            return (this.bits0 & P0_APPEARANCE_COMPARISON_MASK) == (other.bits0 & P0_APPEARANCE_COMPARISON_MASK)
                    && (this.bits1 & P1_APPEARANCE_COMPARISON_MASK) == (other.bits1 & P1_APPEARANCE_COMPARISON_MASK)
                    && (this.bits2 & P2_APPEARANCE_COMPARISON_MASK) == (other.bits2 & P2_APPEARANCE_COMPARISON_MASK);
        }

        /** 
         * Returns true if visual elements match.
         * Does not consider species or geometry in matching.
         */
        public boolean doesAppearanceMatch(ModelState other)
        {
            return (this.bits0 & P0_APPEARANCE_COMPARISON_MASK_NO_GEOMETRY) == (other.bits0 & P0_APPEARANCE_COMPARISON_MASK_NO_GEOMETRY)
                    && (this.bits1 & P1_APPEARANCE_COMPARISON_MASK) == (other.bits1 & P1_APPEARANCE_COMPARISON_MASK);
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
                if(this.hasModelRotation()) result.setModelRotation(this.getModelRotation());
                if((this.getShape().meshFactory().stateFlags & STATE_FLAG_NEEDS_CORNER_JOIN) == STATE_FLAG_NEEDS_CORNER_JOIN)
                {
                    result.setCornerJoin(this.getCornerJoin());
                }
                else if((this.getShape().meshFactory().stateFlags & STATE_FLAG_NEEDS_SIMPLE_JOIN) == STATE_FLAG_NEEDS_SIMPLE_JOIN)  
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
    }
}
