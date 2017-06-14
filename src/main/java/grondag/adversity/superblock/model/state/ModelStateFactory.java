package grondag.adversity.superblock.model.state;


import grondag.adversity.Log;
import grondag.adversity.library.render.LightingMode;
import grondag.adversity.library.varia.BinaryEnumSet;
import grondag.adversity.library.varia.BitPacker;
import grondag.adversity.library.varia.Useful;
import grondag.adversity.library.varia.BitPacker.BitElement.BooleanElement;
import grondag.adversity.library.varia.BitPacker.BitElement.EnumElement;
import grondag.adversity.library.varia.BitPacker.BitElement.IntElement;
import grondag.adversity.library.varia.BitPacker.BitElement.LongElement;
import grondag.adversity.library.world.Alternator;
import grondag.adversity.library.world.CornerJoinBlockState;
import grondag.adversity.library.world.CornerJoinBlockStateSelector;
import grondag.adversity.library.world.IAlternator;
import grondag.adversity.library.world.NeighborBlocks;
import grondag.adversity.library.world.Rotation;
import grondag.adversity.library.world.SimpleJoin;
import grondag.adversity.library.world.NeighborBlocks.NeighborTestResults;
import grondag.adversity.superblock.block.SuperBlock;
import grondag.adversity.superblock.color.BlockColorMapProvider;
import grondag.adversity.superblock.color.ColorMap;
import grondag.adversity.superblock.model.shape.ModelShape;
import grondag.adversity.superblock.model.shape.SideShape;
import grondag.adversity.superblock.terrain.TerrainState;
import grondag.adversity.superblock.texture.Textures;
import grondag.adversity.superblock.texture.TexturePalletteRegistry.TexturePallette;
import grondag.adversity.superblock.varia.BlockTests;
import grondag.adversity.superblock.texture.TextureScale;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class ModelStateFactory
{
    private static final IAlternator[] ROTATION_ALTERNATOR = new IAlternator[TextureScale.values().length];
            
           // Alternator.getAlternator(4, 45927934);
    private static final IAlternator[] BLOCK_ALTERNATOR = new IAlternator[TextureScale.values().length]; //Alternator.getAlternator(8, 2953424);

  
    public static enum StateFormat
    {
        /** use for flowing terrain shapes */
        FLOW,
        /** use for single blocks */
        BLOCK,
        /** use for sliced multi-block shapes (sphere, cylinder, etc.) */
        MULTIBLOCK
    }
    //package scope to allow inspection in test harness
    static final BitPacker PACKER_0 = new BitPacker();
    private static final EnumElement<ModelShape> P0_SHAPE = PACKER_0.createEnumElement(ModelShape.class);
    private static final IntElement[] P0_PAINT_COLOR = new IntElement[PaintLayer.DYNAMIC_SIZE];
    private static final BooleanElement P0_AXIS_INVERTED = PACKER_0.createBooleanElement();
    private static final EnumElement<EnumFacing.Axis> P0_AXIS = PACKER_0.createEnumElement(EnumFacing.Axis.class);
    private static final EnumElement<BlockRenderLayer> P0_RENDER_LAYER_BASE = PACKER_0.createEnumElement(BlockRenderLayer.class);
    private static final EnumElement<BlockRenderLayer> P0_RENDER_LAYER_LAMP = PACKER_0.createEnumElement(BlockRenderLayer.class);
    
    // Note: if pressed for space could save a couple bits by creating a NONE texture that disables layer when selected
//    private static final BooleanElement P0_LAYER_ENABLED_OVERLAY = PACKER_0.createBooleanElement();
//    private static final BooleanElement P0_LAYER_ENABLED_DETAIL = PACKER_0.createBooleanElement();
    private static final EnumElement<Translucency> P0_TRANSLUCENCY = PACKER_0.createEnumElement(Translucency.class);
    
    static final BitPacker PACKER_1 = new BitPacker();
    private static final IntElement[] P1_PAINT_TEXTURE = new IntElement[PaintLayer.STATIC_SIZE];
    @SuppressWarnings("unchecked")
    private static final EnumElement<LightingMode>[] P1_PAINT_LIGHT= new EnumElement[PaintLayer.DYNAMIC_SIZE];

    static final BitPacker PACKER_2 = new BitPacker();
    private static final IntElement P2_POS_X = PACKER_2.createIntElement(256);
    private static final IntElement P2_POS_Y = PACKER_2.createIntElement(256);
    private static final IntElement P2_POS_Z = PACKER_2.createIntElement(256);
    /** value semantics are owned by consumer - only constraints are size and does not update from world */
    private static final LongElement P2_STATIC_SHAPE_BITS = PACKER_2.createLongElement(1L << 40);

    static final BitPacker PACKER_3_BLOCK = new BitPacker();
    private static final IntElement P3B_SPECIES = PACKER_3_BLOCK.createIntElement(16);
    private static final IntElement P3B_BLOCK_JOIN = PACKER_3_BLOCK.createIntElement(CornerJoinBlockStateSelector.BLOCK_JOIN_STATE_COUNT);
    // these provide random alternate for texture version and rotation for single blocks or cubic groups of blocks 
    private static final IntElement P3B_BLOCK_VERSION[] = new IntElement[TextureScale.values().length];
    @SuppressWarnings("unchecked")
    private static final EnumElement<Rotation> P3B_BLOCK_ROTATION[] = new EnumElement[TextureScale.values().length];
    private static final IntElement P3B_MASONRY_JOIN = PACKER_3_BLOCK.createIntElement(SimpleJoin.STATE_COUNT);
    
    static final BitPacker PACKER_3_FLOW = new BitPacker();
    private static final LongElement P3F_FLOW_JOIN = PACKER_3_FLOW.createLongElement(TerrainState.STATE_BIT_MASK + 1);

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
            P1_PAINT_TEXTURE[i] = PACKER_1.createIntElement(Textures.MAX_TEXTURES); // 12 bits each x4 = 48
        }
        
        for(int i = 0; i < PaintLayer.DYNAMIC_SIZE; i++)
        {
            P0_PAINT_COLOR[i] = PACKER_0.createIntElement(BlockColorMapProvider.INSTANCE.getColorMapCount());  // 11 bits each x4 = 44
            P1_PAINT_LIGHT[i] = PACKER_1.createEnumElement(LightingMode.class); // 1 bit each x4 = 4
            
            borderMask0 |= P0_PAINT_COLOR[i].comparisonMask();
            borderMask1 |= P1_PAINT_TEXTURE[i].comparisonMask();
            borderMask1 |= P1_PAINT_LIGHT[i].comparisonMask();
        }
        
        for(TextureScale scale : TextureScale.values())
        {
            ROTATION_ALTERNATOR[scale.ordinal()] = Alternator.getAlternator(4, 45927934, scale.power);
            BLOCK_ALTERNATOR[scale.ordinal()] = Alternator.getAlternator(8, 2953424, scale.power);
            P3B_BLOCK_VERSION[scale.ordinal()] = PACKER_3_BLOCK.createIntElement(8);
            P3B_BLOCK_ROTATION[scale.ordinal()] = PACKER_3_BLOCK.createEnumElement(Rotation.class);
        }
        
        P0_APPEARANCE_COMPARISON_MASK_NO_GEOMETRY = borderMask0
                | P0_RENDER_LAYER_BASE.comparisonMask() 
                | P0_RENDER_LAYER_LAMP.comparisonMask()
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
        private static final int STATE_FLAG_IS_POPULATED = 1;
        
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
        
        /** 
         * True if block version and rotation are needed. Applies for block formats.
         */
        public static final int STATE_FLAG_NEEDS_BLOCK_RANDOMS = STATE_FLAG_NEEDS_POS << 1;
        public static final int STATE_FLAG_NEEDS_2x2_BLOCK_RANDOMS = STATE_FLAG_NEEDS_BLOCK_RANDOMS << 1;
        public static final int STATE_FLAG_NEEDS_4x4_BLOCK_RANDOMS = STATE_FLAG_NEEDS_2x2_BLOCK_RANDOMS << 1;
        public static final int STATE_FLAG_NEEDS_8x8_BLOCK_RANDOMS = STATE_FLAG_NEEDS_4x4_BLOCK_RANDOMS << 1;
        public static final int STATE_FLAG_NEEDS_16x16_BLOCK_RANDOMS = STATE_FLAG_NEEDS_8x8_BLOCK_RANDOMS << 1;
        public static final int STATE_FLAG_NEEDS_32x32_BLOCK_RANDOMS = STATE_FLAG_NEEDS_16x16_BLOCK_RANDOMS << 1;
        
        public static final int STATE_FLAG_NEEDS_SPECIES = STATE_FLAG_NEEDS_32x32_BLOCK_RANDOMS << 1;
        
        public static final int STATE_FLAG_HAS_AXIS = STATE_FLAG_NEEDS_SPECIES << 1;
        
        public static final int STATE_FLAG_NEEDS_ROTATION = STATE_FLAG_HAS_AXIS << 1;
        
        public static final int STATE_FLAG_HAS_AXIS_ORIENTATION = STATE_FLAG_NEEDS_ROTATION << 1;
        
        /** sign bit is used to indicate static state */
        private static final int INT_SIGN_BIT = 1 << 31;
        private static final int INT_SIGN_BIT_INVERSE = ~INT_SIGN_BIT;
        
        // used to check if any block alternates are needed */
        private static final int STATE_FLAG_NEEDS_ANY_BLOCK_RANDOM = STATE_FLAG_NEEDS_BLOCK_RANDOMS | STATE_FLAG_NEEDS_2x2_BLOCK_RANDOMS | STATE_FLAG_NEEDS_4x4_BLOCK_RANDOMS
                | STATE_FLAG_NEEDS_8x8_BLOCK_RANDOMS | STATE_FLAG_NEEDS_16x16_BLOCK_RANDOMS | STATE_FLAG_NEEDS_32x32_BLOCK_RANDOMS;

        /** use this to turn off flags that should not be used with non-block state formats */
        private static final int STATE_FLAG_DISABLE_BLOCK_ONLY = ~(
                  STATE_FLAG_NEEDS_CORNER_JOIN | STATE_FLAG_NEEDS_SIMPLE_JOIN | STATE_FLAG_NEEDS_MASONRY_JOIN
                | STATE_FLAG_NEEDS_ANY_BLOCK_RANDOM | STATE_FLAG_NEEDS_SPECIES | STATE_FLAG_NEEDS_ROTATION);

                
        public static final BinaryEnumSet<BlockRenderLayer> BENUMSET_RENDER_LAYER = new BinaryEnumSet<BlockRenderLayer>(BlockRenderLayer.class);
        
        private boolean isStatic;
        private long bits0;
        private long bits1;
        private long bits2;
        private long bits3;
        
        private int hashCode = -1;
        
        /** contains indicators derived from shape and painters */
        private int stateFlags;
        
        /** Bits set to indicate which layers can be rendered. Needed for fast lookup during render loop. */
        private byte renderLayerEnabledFlags;
        /** 
         * Bits set to indicate which layers are shaded (use ambient occlusion). 
         * False if full brightness lighting mode. True if layer not included.
         * Note default value is all true;
         */
        private byte renderLayerShadedFlags = 0xF;
        
        public ModelState() { }


        
        public ModelState(int[] bits)
        {
            // sign on first word is used to store static indicator
            this.isStatic = (INT_SIGN_BIT & bits[0]) == INT_SIGN_BIT;
            
            bits0 = ((long) (INT_SIGN_BIT_INVERSE & bits[0])) << 32 | (bits[1] & 0xffffffffL);
            bits1 = ((long)bits[2]) << 32 | (bits[3] & 0xffffffffL);
            bits2 = ((long)bits[4]) << 32 | (bits[5] & 0xffffffffL);
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
            result[0] = (int) (this.isStatic ? (bits0 >> 32) | INT_SIGN_BIT : (bits0 >> 32));
            result[1] = (int) (bits0);
            
            result[2] = (int) (bits1 >> 32);
            result[3] = (int) (bits1);
            
            result[4] = (int) (bits2 >> 32);
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
            if(stateFlags == 0)
            {
                int flags = STATE_FLAG_IS_POPULATED | getShape().meshFactory().stateFlags;
                int layerFlags = 0;
                int shadedFlags = 0xF; // default to all true (shaded)
                
                layerFlags = BENUMSET_RENDER_LAYER.setFlagForValue(this.getRenderLayer(PaintLayer.BASE), layerFlags, true);
                if(this.getLightingMode(PaintLayer.BASE) == LightingMode.FULLBRIGHT) 
                    shadedFlags = BENUMSET_RENDER_LAYER.setFlagForValue(this.getRenderLayer(PaintLayer.BASE), shadedFlags, false);
                flags |= getTexture(PaintLayer.BASE).stateFlags;
                flags |= getTexture(PaintLayer.CUT).stateFlags;
 
                layerFlags = BENUMSET_RENDER_LAYER.setFlagForValue(this.getRenderLayer(PaintLayer.LAMP), layerFlags, true);
                if(this.getLightingMode(PaintLayer.LAMP) == LightingMode.FULLBRIGHT) 
                    shadedFlags = BENUMSET_RENDER_LAYER.setFlagForValue(this.getRenderLayer(PaintLayer.LAMP), shadedFlags, false);
                flags |= getTexture(PaintLayer.LAMP).stateFlags;
                
                if(this.isDetailLayerEnabled())
                {
                    layerFlags = BENUMSET_RENDER_LAYER.setFlagForValue(this.getRenderLayer(PaintLayer.DETAIL), layerFlags, true);
                    if(this.getLightingMode(PaintLayer.DETAIL) == LightingMode.FULLBRIGHT) 
                        shadedFlags = BENUMSET_RENDER_LAYER.setFlagForValue(this.getRenderLayer(PaintLayer.DETAIL), shadedFlags, false);
                    flags |= getTexture(PaintLayer.DETAIL).stateFlags;

                }
                
                if(this.isOverlayLayerEnabled())
                {
                    layerFlags = BENUMSET_RENDER_LAYER.setFlagForValue(this.getRenderLayer(PaintLayer.OVERLAY), layerFlags, true);
                    if(this.getLightingMode(PaintLayer.OVERLAY) == LightingMode.FULLBRIGHT) 
                        shadedFlags = BENUMSET_RENDER_LAYER.setFlagForValue(this.getRenderLayer(PaintLayer.OVERLAY), shadedFlags, false);
                    flags |= getTexture(PaintLayer.OVERLAY).stateFlags;

                }
                
                // turn off flags that don't apply to non-block formats if we aren' tone
                if(this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
                {
                    flags &= STATE_FLAG_DISABLE_BLOCK_ONLY;
                }
                
                this.stateFlags = flags;
                this.renderLayerEnabledFlags = (byte) layerFlags;
                this.renderLayerShadedFlags = (byte) shadedFlags;
            }
        }
        
        private void clearStateFlags()
        {
            if(this.stateFlags != 0) 
            {
                this.stateFlags  = 0;
                this.renderLayerEnabledFlags = 0;
                this.renderLayerShadedFlags = 0xF;
            }
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
        public boolean hasBlockVersions()
        {
            this.populateStateFlagsIfNeeded();
            return (this.stateFlags & STATE_FLAG_NEEDS_ANY_BLOCK_RANDOM) > 0;
        }
        
        public boolean hasMasonryJoin()
        {
            this.populateStateFlagsIfNeeded();
            return (this.stateFlags & STATE_FLAG_NEEDS_MASONRY_JOIN) == STATE_FLAG_NEEDS_MASONRY_JOIN;
        }
        
        public boolean hasRotation()
        {
            this.populateStateFlagsIfNeeded();
            return (this.stateFlags & STATE_FLAG_NEEDS_ROTATION) == STATE_FLAG_NEEDS_ROTATION;
        }
        
        public boolean hasSpecies()
        {
            this.populateStateFlagsIfNeeded();
            return((this.stateFlags & STATE_FLAG_NEEDS_SPECIES) == STATE_FLAG_NEEDS_SPECIES);
        }
        
        public boolean isSpeciesUsedForShape()
        {
            return this.getShape().meshFactory().isSpeciesUsedForShape();
        }
        
        /** True if shape can be placed on itself to grow */
        public boolean isAdditive()
        {
            return this.getShape().meshFactory().isAdditive();
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
            
            switch(this.getShape().meshFactory().stateFormat)
            {
            case BLOCK:
 
                // for bigtex texture randomization - cubic painters only need 32 blocks of resolution
                // to be effective at texture randomization because they have block version and rotation as well
                if((stateFlags & STATE_FLAG_NEEDS_POS) == STATE_FLAG_NEEDS_POS) refreshBlockPosFromWorld(pos, 31);
                
                long b3 = bits3;
                
                if((this.stateFlags & STATE_FLAG_NEEDS_SPECIES) == STATE_FLAG_NEEDS_SPECIES)
                {
                     b3 = P3B_SPECIES.setValue(state.getValue(SuperBlock.META), b3);                  
                }
                
                for(TextureScale scale : TextureScale.values())
                {
                    if((scale.modelStateFlag & stateFlags) == scale.modelStateFlag)
                    {
                        b3 = P3B_BLOCK_VERSION[scale.ordinal()].setValue(BLOCK_ALTERNATOR[scale.ordinal()].getAlternate(pos), b3);
                        b3 = P3B_BLOCK_ROTATION[scale.ordinal()].setValue(Rotation.values()[ROTATION_ALTERNATOR[scale.ordinal()].getAlternate(pos)], b3);
                    }
                }

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
        
        public BlockRenderLayer getRenderLayer(PaintLayer layer)
        {
            switch(layer)
            {
            case DETAIL:
            case OVERLAY:
                return this.getTexture(layer).renderLayer;

            case LAMP:
                return P0_RENDER_LAYER_LAMP.getValue(bits0);
                
            case BASE:
            case CUT:
            default:
                return P0_RENDER_LAYER_BASE.getValue(bits0);
            
            }
        }
        
        public void setRenderLayer(PaintLayer layer, BlockRenderLayer renderLayer)
        {
            
            switch(layer)
            {
            case DETAIL:
            case OVERLAY:
            default:
                if(Log.DEBUG_MODE)
                    Log.warn("setRenderLayer on model state does not apply for given paint layer.");
                return;

            case LAMP:
                bits0 = P0_RENDER_LAYER_LAMP.setValue(renderLayer, bits0);
                
            case BASE:
            case CUT:
                bits0 = P0_RENDER_LAYER_BASE.setValue(renderLayer, bits0);
            }
            clearStateFlags();
            invalidateHashCode();
        }

        public boolean isDetailLayerEnabled()
        {
            return this.getTexture(PaintLayer.DETAIL) != Textures.NONE;
        }
        
        public void setDetailLayerEnabled(boolean isEnabled)
        {
            if(isEnabled && this.getTexture(PaintLayer.DETAIL) == Textures.NONE)
            {
                this.setTexture(PaintLayer.DETAIL, Textures.BLOCK_RAW_FLEXSTONE);
            }
            else if(!isEnabled && this.getTexture(PaintLayer.DETAIL) != Textures.NONE)
            {
                this.setTexture(PaintLayer.DETAIL, Textures.NONE);
            }
        }
        
        public boolean isOverlayLayerEnabled()
        {
            return this.getTexture(PaintLayer.OVERLAY) != Textures.NONE;
        }
        
        public void setOverlayLayerEnabled(boolean isEnabled)
        {
            if(isEnabled && this.getTexture(PaintLayer.OVERLAY) == Textures.NONE)
            {
                this.setTexture(PaintLayer.OVERLAY, Textures.BLOCK_RAW_FLEXSTONE);
            }
            else if(!isEnabled && this.getTexture(PaintLayer.OVERLAY) != Textures.NONE)
            {
                this.setTexture(PaintLayer.OVERLAY, Textures.NONE);
            }
        }
        
        /** returns true if any surface painter is configured to have the given layer */
        public boolean canRenderInLayer(BlockRenderLayer renderLayer)
        {
            this.populateStateFlagsIfNeeded();
            return BENUMSET_RENDER_LAYER.isFlagSetForValue(renderLayer, this.renderLayerEnabledFlags);
        }
        
        /** 
         * Exposed for use as a lookup key in model dispatch logic. 
         * Identifies which block render layers can be rendered in this model.
         * Can be decoded with {@link #BENUMSET_RENDER_LAYER} 
         */
        public byte getCanRenderInLayerFlags() 
        { 
            this.populateStateFlagsIfNeeded();
            return this.renderLayerEnabledFlags; 
        };
        
        public Translucency getTranslucency()
        {
            return P0_TRANSLUCENCY.getValue(bits0);
        }
        
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
        
        public LightingMode getLightingMode(PaintLayer layer)
        {
            return P1_PAINT_LIGHT[layer.dynamicIndex].getValue(bits1);
        }
        
        public void setLightingMode(PaintLayer layer, LightingMode lightingMode)
        {
            bits1 = P1_PAINT_LIGHT[layer.dynamicIndex].setValue(lightingMode, bits1);
            clearStateFlags();
            invalidateHashCode();
        }
        
        /** returns true if no surface painters for the given layer are configured for full brightness rendering */
        public boolean isLayerShaded(BlockRenderLayer renderLayer)
        {
            this.populateStateFlagsIfNeeded();
            return BENUMSET_RENDER_LAYER.isFlagSetForValue(renderLayer, this.renderLayerShadedFlags);
        }
        
        /** 
         * Exposed for use as a lookup key in model dispatch logic. 
         * Identifies which block render layers should be rendered with AO (not full brightness).
         * Can be decoded with {@link ModelState#BENUMSET_RENDER_LAYER} 
         */
        public byte getRenderLayerShadedFlags() 
        { 
            this.populateStateFlagsIfNeeded();
            return this.renderLayerShadedFlags; 
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
        
        public Rotation getRotation(TextureScale scale)
        {
            if(Log.DEBUG_MODE && this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
                Log.warn("getRotation on model state does not apply for shape");
            
            populateStateFlagsIfNeeded();
            return P3B_BLOCK_ROTATION[scale.ordinal()].getValue(bits3);
        }
        
        public void setRotation(Rotation rotation, TextureScale scale)
        {
            if(Log.DEBUG_MODE && this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
                Log.warn("setRotation on model state does not apply for shape");
            
            populateStateFlagsIfNeeded();
            bits3 = P3B_BLOCK_ROTATION[scale.ordinal()].setValue(rotation, bits3);
            invalidateHashCode();
        }

        public int getBlockVersion(TextureScale scale)
        {
            if(Log.DEBUG_MODE && this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
                Log.warn("getBlockVersion on model state does not apply for shape");
            
            return P3B_BLOCK_VERSION[scale.ordinal()].getValue(bits3);
        }
        
        public void setBlockVersion(int version, TextureScale scale)
        {
            if(Log.DEBUG_MODE && this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
                Log.warn("setBlockVersion on model state does not apply for shape");
            
            bits3 = P3B_BLOCK_VERSION[scale.ordinal()].setValue(version, bits3);
            invalidateHashCode();
        }

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
                if(this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
                {
                    Log.warn("Ignored setMasonryJoin on model state that does not apply for shape");
                    return;
                }
                
                populateStateFlagsIfNeeded();
                if(((stateFlags & STATE_FLAG_NEEDS_CORNER_JOIN) == 0) || ((stateFlags & STATE_FLAG_NEEDS_MASONRY_JOIN) == 0))
                {
                    Log.warn("Ignored setMasonryJoin on model state for which it does not apply");
                    return;
                }
            }
            
            bits3 = P3B_MASONRY_JOIN.setValue(join.getIndex(), bits3);
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
        //  SHAPE-DEPENDENT CONVENIENCE METHODS
        ////////////////////////////////////////////////////

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
         * Rotate this block around the given axis if possible, making necessary changes to world state.
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
            return this.getShape().meshFactory().geometricModelState(this);
        }
    }
}
