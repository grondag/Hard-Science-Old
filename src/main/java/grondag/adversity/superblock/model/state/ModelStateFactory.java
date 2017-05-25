package grondag.adversity.superblock.model.state;


import grondag.adversity.Output;
import grondag.adversity.library.Alternator;
import grondag.adversity.library.BinaryEnumSet;
import grondag.adversity.library.BitPacker;
import grondag.adversity.library.IAlternator;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.Useful;
import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.library.BitPacker.BitElement.EnumElement;
import grondag.adversity.library.BitPacker.BitElement.IntElement;
import grondag.adversity.library.BitPacker.BitElement.LongElement;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.library.joinstate.CornerJoinBlockState;
import grondag.adversity.library.joinstate.CornerJoinBlockStateSelector;
import grondag.adversity.library.joinstate.SimpleJoin;
import grondag.adversity.library.Rotation;
import grondag.adversity.library.BitPacker.BitElement.BooleanElement;
import grondag.adversity.niceblock.color.BlockColorMapProvider;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import grondag.adversity.niceblock.support.BlockTests;
import grondag.adversity.superblock.block.SuperBlock;
import grondag.adversity.superblock.model.layout.PaintLayer;
import grondag.adversity.superblock.model.shape.ModelShape;
import grondag.adversity.superblock.texture.Textures;
import grondag.adversity.superblock.texture.TexturePalletteProvider.TexturePallette;
import grondag.adversity.superblock.texture.TextureScale;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

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
    @SuppressWarnings("unchecked")
    private static final EnumElement<BlockRenderLayer>[] P0_PAINT_LAYER = new EnumElement[PaintLayer.DYNAMIC_SIZE];
    private static final BooleanElement[] P0_PAINT_LAYER_ENABLED = new BooleanElement[PaintLayer.DYNAMIC_SIZE];
    private static final BooleanElement P0_MASONRY_BORDER = PACKER_0.createBooleanElement();
    
    static final BitPacker PACKER_1 = new BitPacker();
    private static final IntElement[] P1_PAINT_TEXTURE = new IntElement[PaintLayer.values().length];
    @SuppressWarnings("unchecked")
    private static final EnumElement<LightingMode>[] P1_PAINT_LIGHT= new EnumElement[PaintLayer.DYNAMIC_SIZE];

    static final BitPacker PACKER_2 = new BitPacker();
    private static final IntElement P2_POS_X = PACKER_2.createIntElement(32);
    private static final IntElement P2_POS_Y = PACKER_2.createIntElement(32);
    private static final IntElement P2_POS_Z = PACKER_2.createIntElement(32);
    /** value semantics are owned by consumer - only constraints are size and does not update from world */
    private static final LongElement P2_STATIC_SHAPE_BITS = PACKER_2.createLongElement(1L << 49);

    static final BitPacker PACKER_3_BLOCK = new BitPacker();
    private static final IntElement P3B_SPECIES = PACKER_3_BLOCK.createIntElement(16);
    private static final IntElement P3B_BLOCK_JOIN = PACKER_3_BLOCK.createIntElement(CornerJoinBlockStateSelector.BLOCK_JOIN_STATE_COUNT);
    // these provide random alternate for texture version and rotation for single blocks or cubic groups of blocks 
    private static final IntElement P3B_BLOCK_VERSION[] = new IntElement[TextureScale.values().length];
    @SuppressWarnings("unchecked")
    private static final EnumElement<Rotation> P3B_BLOCK_ROTATION[] = new EnumElement[TextureScale.values().length];
    private static final IntElement P3B_MASONRY_JOIN = PACKER_3_BLOCK.createIntElement(SimpleJoin.STATE_COUNT);
    
    static final BitPacker PACKER_3_FLOW = new BitPacker();
    private static final LongElement P3F_FLOW_JOIN = PACKER_3_FLOW.createLongElement(FlowHeightState.STATE_BIT_MASK + 1);

    
    static
    {
        for(int i = 0; i < PaintLayer.STATIC_SIZE; i++)
        {
            // p0 reserve 7 bits for shape
            P1_PAINT_TEXTURE[i] = PACKER_1.createIntElement(Textures.MAX_TEXTURES); // 12 bits each x5 = 48
        }
        
        for(int i = 0; i < PaintLayer.DYNAMIC_SIZE; i++)
        {
            P0_PAINT_LAYER[i] = PACKER_0.createEnumElement(BlockRenderLayer.class); // 2 bits each x5 = 8
            P0_PAINT_LAYER_ENABLED[i] = PACKER_0.createBooleanElement();
            P0_PAINT_COLOR[i] = PACKER_0.createIntElement(BlockColorMapProvider.INSTANCE.getColorMapCount());  // 11 bits each x4 = 44
            P1_PAINT_LIGHT[i] = PACKER_1.createEnumElement(LightingMode.class); // 1 bit each x5 = 4
        }
        
        // TODO: for texture alternators, do we need to support a larger number of alternates for zoomed uniforms?
        // Will have 16 if we a zoom a texture with 4 alternates
        for(TextureScale scale : TextureScale.values())
        {
            ROTATION_ALTERNATOR[scale.ordinal()] = Alternator.getAlternator(4, 45927934, scale.power);
            BLOCK_ALTERNATOR[scale.ordinal()] = Alternator.getAlternator(8, 2953424, scale.power);
            P3B_BLOCK_VERSION[scale.ordinal()] = PACKER_3_BLOCK.createIntElement(8);
            P3B_BLOCK_ROTATION[scale.ordinal()] = PACKER_3_BLOCK.createEnumElement(Rotation.class);
        }
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
         * True if position (big-tex) world state is needed. Applies for block and flow state formats.
         */
        public static final int STATE_FLAG_NEEDS_POS = STATE_FLAG_NEEDS_SIMPLE_JOIN << 1;
        
        /** 
         * True if block version and rotation are needed. Applies for block formats.
         */
        public static final int STATE_FLAG_NEEDS_BLOCK_RANDOMS = STATE_FLAG_NEEDS_POS << 1;
        public static final int STATE_FLAG_NEEDS_2x2_BLOCK_RANDOMS = STATE_FLAG_NEEDS_BLOCK_RANDOMS << 1;
        public static final int STATE_FLAG_NEEDS_4x4_BLOCK_RANDOMS = STATE_FLAG_NEEDS_2x2_BLOCK_RANDOMS << 1;
        public static final int STATE_FLAG_NEEDS_8x8_BLOCK_RANDOMS = STATE_FLAG_NEEDS_4x4_BLOCK_RANDOMS << 1;
        public static final int STATE_FLAG_NEEDS_16x16_BLOCK_RANDOMS = STATE_FLAG_NEEDS_8x8_BLOCK_RANDOMS << 1;
        public static final int STATE_FLAG_NEEDS_32x32_BLOCK_RANDOMS = STATE_FLAG_NEEDS_16x16_BLOCK_RANDOMS << 1;
        
        private static final int INT_SIGN_BIT = 1 << 31;
        private static final int INT_SIGN_BIT_INVERSE = ~INT_SIGN_BIT;
        
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
                
                if(this.isPaintLayerEnabled(PaintLayer.BASE))
                {
                    layerFlags = BENUMSET_RENDER_LAYER.setFlagForValue(this.getRenderLayer(PaintLayer.BASE), layerFlags, true);
                    if(this.getLightingMode(PaintLayer.BASE) == LightingMode.FULLBRIGHT) 
                        shadedFlags = BENUMSET_RENDER_LAYER.setFlagForValue(this.getRenderLayer(PaintLayer.BASE), shadedFlags, false);
                    flags |= getTexture(PaintLayer.BASE).stateFlags;
                    flags |= getTexture(PaintLayer.CUT).stateFlags;
                }
 
                if(this.isPaintLayerEnabled(PaintLayer.DETAIL))
                {
                    layerFlags = BENUMSET_RENDER_LAYER.setFlagForValue(this.getRenderLayer(PaintLayer.DETAIL), layerFlags, true);
                    if(this.getLightingMode(PaintLayer.DETAIL) == LightingMode.FULLBRIGHT) 
                        shadedFlags = BENUMSET_RENDER_LAYER.setFlagForValue(this.getRenderLayer(PaintLayer.DETAIL), shadedFlags, false);
                    flags |= getTexture(PaintLayer.DETAIL).stateFlags;

                }
                
                if(this.isPaintLayerEnabled(PaintLayer.LAMP))
                {
                    layerFlags = BENUMSET_RENDER_LAYER.setFlagForValue(this.getRenderLayer(PaintLayer.LAMP), layerFlags, true);
                    if(this.getLightingMode(PaintLayer.LAMP) == LightingMode.FULLBRIGHT) 
                        shadedFlags = BENUMSET_RENDER_LAYER.setFlagForValue(this.getRenderLayer(PaintLayer.LAMP), shadedFlags, false);
                    flags |= getTexture(PaintLayer.LAMP).stateFlags;

                }
                
                if(this.isPaintLayerEnabled(PaintLayer.OVERLAY))
                {
                    layerFlags = BENUMSET_RENDER_LAYER.setFlagForValue(this.getRenderLayer(PaintLayer.OVERLAY), layerFlags, true);
                    if(this.getLightingMode(PaintLayer.OVERLAY) == LightingMode.FULLBRIGHT) 
                        shadedFlags = BENUMSET_RENDER_LAYER.setFlagForValue(this.getRenderLayer(PaintLayer.OVERLAY), shadedFlags, false);
                    flags |= getTexture(PaintLayer.OVERLAY).stateFlags;

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
        
        public boolean isStatic() { return this.isStatic; }
        public void setStatic(boolean isStatic) { this.isStatic = isStatic; }
        
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
 
                // for bigtex texture randomization
                if((stateFlags & STATE_FLAG_NEEDS_POS) == STATE_FLAG_NEEDS_POS) refreshBlockPosFromWorld(pos);
                
                long b3 = bits3;
                
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
                    NeighborTestResults tests = neighbors.getNeighborTestResults(new BlockTests.SuperBlockBorderMatch((SuperBlock) state.getBlock(), this.getSpecies()));
                    
                    
                    b3 = P3B_BLOCK_JOIN.setValue(CornerJoinBlockStateSelector.findIndex(tests), b3);
                }
                else if ((STATE_FLAG_NEEDS_SIMPLE_JOIN & stateFlags) == STATE_FLAG_NEEDS_SIMPLE_JOIN)
                {
                    neighbors = new NeighborBlocks(world, pos, false);
                    NeighborTestResults tests = neighbors.getNeighborTestResults(new BlockTests.SuperBlockBorderMatch((SuperBlock) state.getBlock(), this.getSpecies()));
                    b3 = P3B_BLOCK_JOIN.setValue(SimpleJoin.getIndex(tests), b3);
                }
           
                if(this.isMasonryBorder())
                {
                    if(neighbors == null) neighbors = new NeighborBlocks(world, pos, false);
                    NeighborTestResults masonryTests = neighbors.getNeighborTestResults(new BlockTests.SuperBlockMasonryMatch((SuperBlock) state.getBlock(), this.getSpecies(), pos));
                    b3 = P3B_MASONRY_JOIN.setValue(SimpleJoin.getIndex(masonryTests), b3);

                }


                
                bits3 = b3;
                
                break;

            case FLOW:
                // for bigtex texture randomization
                if((stateFlags & STATE_FLAG_NEEDS_POS) == STATE_FLAG_NEEDS_POS) refreshBlockPosFromWorld(pos);

                bits3 = P3F_FLOW_JOIN.setValue(FlowHeightState.getBitsFromWorldStatically((SuperBlock)state.getBlock(), state, world, pos), bits3);
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
         * Saves world block pos relative to 32x32x32 cube boundaries.
         * Used by BigTex surface painting for texture randomization on non-multiblock shapes.
         */
        private void refreshBlockPosFromWorld(BlockPos pos)
        {
            long b2 = bits2;
            b2 = P2_POS_X.setValue((pos.getX() & 31), b2);
            b2 = P2_POS_Y.setValue((pos.getY() & 31), b2);
            b2 = P2_POS_Z.setValue((pos.getZ() & 31), b2);
            bits2 = b2;
        }

        ////////////////////////////////////////////////////
        //  PACKER 0 ATTRIBUTES (NOT SHAPE-DEPENDENT)
        ////////////////////////////////////////////////////
        
        public ModelShape getShape()
        {
            return P0_SHAPE.getValue(bits0);
        }
        
        public void setShape(ModelShape shape)
        {
            bits0 = P0_SHAPE.setValue(shape, bits0);
            invalidateHashCode();
            clearStateFlags();
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
            //TODO: layer for overlay and detail always going to be tied to texture
            // save bits in underlying data structure?
            if(layer == PaintLayer.DETAIL || layer == PaintLayer.OVERLAY) return this.getTexture(layer).renderLayer;
            
            return P0_PAINT_LAYER[layer.dynamicIndex].getValue(bits0);
        }
        
        public void setRenderLayer(PaintLayer layer, BlockRenderLayer renderLayer)
        {
            bits0 = P0_PAINT_LAYER[layer.dynamicIndex].setValue(renderLayer, bits0);
            clearStateFlags();
            invalidateHashCode();
        }

        public boolean isPaintLayerEnabled(PaintLayer layer)
        {
            //TODO: base and lamp surfaces are always rendered - save a bit in underlying data structure?
            if(layer == PaintLayer.BASE || layer == PaintLayer.CUT || layer == PaintLayer.LAMP) return true;
            
            return P0_PAINT_LAYER_ENABLED[layer.dynamicIndex].getValue(bits0);
        }
        
        public void setPaintLayerEnabled(PaintLayer layer, boolean isEnabled)
        {
            bits0 = P0_PAINT_LAYER_ENABLED[layer.dynamicIndex].setValue(isEnabled, bits0);
            clearStateFlags();
            invalidateHashCode();
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
        
        public boolean isMasonryBorder()
        {
            return P0_MASONRY_BORDER.getValue(bits0);
        }
        
        public void setMasonryBorder(boolean isMasonry)
        {
            bits0 = P0_MASONRY_BORDER.setValue(isMasonry, bits0);
            invalidateHashCode();
        }


        ////////////////////////////////////////////////////
        //  PACKER 1 ATTRIBUTES (NOT SHAPE-DEPENDENT)
        ////////////////////////////////////////////////////
        
        public TexturePallette getTexture(PaintLayer layer)
        {
            return Textures.ALL_TEXTURES.get(P1_PAINT_TEXTURE[layer.ordinal()].getValue(bits1));
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
            if(Output.DEBUG_MODE && this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
                Output.getLog().warn("getRotation on model state does not apply for shape");
            
            populateStateFlagsIfNeeded();
            return P3B_BLOCK_ROTATION[scale.ordinal()].getValue(bits3);
        }
        
        public void setRotation(Rotation rotation, TextureScale scale)
        {
            if(Output.DEBUG_MODE && this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
                Output.getLog().warn("setRotation on model state does not apply for shape");
            
            populateStateFlagsIfNeeded();
            bits3 = P3B_BLOCK_ROTATION[scale.ordinal()].setValue(rotation, bits3);
            invalidateHashCode();
        }

        public int getBlockVersion(TextureScale scale)
        {
            if(Output.DEBUG_MODE && this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
                Output.getLog().warn("getBlockVersion on model state does not apply for shape");
            
            return P3B_BLOCK_VERSION[scale.ordinal()].getValue(bits3);
        }
        
        public void setBlockVersion(int version, TextureScale scale)
        {
            if(Output.DEBUG_MODE && this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
                Output.getLog().warn("setBlockVersion on model state does not apply for shape");
            
            bits3 = P3B_BLOCK_VERSION[scale.ordinal()].setValue(version, bits3);
            invalidateHashCode();
        }

        public int getSpecies()
        {
            if(Output.DEBUG_MODE && this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
                Output.getLog().warn("getSpecies on model state does not apply for shape");
            
            return P3B_SPECIES.getValue(bits3);
        }
        
        public void setSpecies(int species)
        {
            if(Output.DEBUG_MODE && this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
                Output.getLog().warn("setSpecies on model state does not apply for shape");
            
            bits3 = P3B_SPECIES.setValue(species, bits3);
            invalidateHashCode();
        }
        
        public CornerJoinBlockState getCornerJoin()
        {
            if(Output.DEBUG_MODE)
            {
                populateStateFlagsIfNeeded();
                if((stateFlags & STATE_FLAG_NEEDS_CORNER_JOIN) == 0 || this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
                    Output.getLog().warn("getCornerJoin on model state does not apply for shape");
            }
            
            return CornerJoinBlockStateSelector.getJoinState(P3B_BLOCK_JOIN.getValue(bits3));
        }
        
        public void setCornerJoin(CornerJoinBlockState join)
        {
            if(Output.DEBUG_MODE)
            {
                populateStateFlagsIfNeeded();
                if((stateFlags & STATE_FLAG_NEEDS_CORNER_JOIN) == 0 || this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
                    Output.getLog().warn("setCornerJoin on model state does not apply for shape");
            }
            
            bits3 = P3B_BLOCK_JOIN.setValue(join.getIndex(), bits3);
            invalidateHashCode();
        }
        
        public SimpleJoin getSimpleJoin()
        {
            if(Output.DEBUG_MODE && this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
                Output.getLog().warn("getSimpleJoin on model state does not apply for shape");
            
            
            // If this state is using corner join, join index is for a corner join
            // and so need to derive simple join from the corner join
            populateStateFlagsIfNeeded();
            return ((stateFlags & STATE_FLAG_NEEDS_CORNER_JOIN) == 0)
                    ? new SimpleJoin(P3B_BLOCK_JOIN.getValue(bits3))
                    : getCornerJoin().simpleJoin;
        }
        
        public void setSimpleJoin(SimpleJoin join)
        {
            if(Output.DEBUG_MODE)
            {
                if(this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
                {
                    Output.getLog().warn("Ignored setSimpleJoin on model state that does not apply for shape");
                    return;
                }
                
                populateStateFlagsIfNeeded();
                if((stateFlags & STATE_FLAG_NEEDS_CORNER_JOIN) != 0)
                {
                    Output.getLog().warn("Ignored setSimpleJoin on model state that uses corner join instead");
                    return;
                }
            }
            
            bits3 = P3B_BLOCK_JOIN.setValue(join.getIndex(), bits3);
            invalidateHashCode();
        }
        
        public SimpleJoin getMasonryJoin()
        {
            if(Output.DEBUG_MODE && (this.getShape().meshFactory().stateFormat != StateFormat.BLOCK || !this.isMasonryBorder()))
                Output.getLog().warn("getMasonryJoin on model state does not apply for shape");
            
            populateStateFlagsIfNeeded();
            return new SimpleJoin(P3B_MASONRY_JOIN.getValue(bits3));
        }
        
        public void setMasonryJoin(SimpleJoin join)
        {
            if(Output.DEBUG_MODE)
            {
                if(this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
                {
                    Output.getLog().warn("Ignored setMasonryJoin on model state that does not apply for shape");
                    return;
                }
                
                populateStateFlagsIfNeeded();
                if(((stateFlags & STATE_FLAG_NEEDS_CORNER_JOIN) != 0) || !this.isMasonryBorder())
                {
                    Output.getLog().warn("Ignored setMasonryJoin on model state for which it does not apply");
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
            if(Output.DEBUG_MODE && this.getShape().meshFactory().stateFormat != StateFormat.MULTIBLOCK)
                Output.getLog().warn("getMultiBlockBits on model state does not apply for shape");
            
            return bits3;
        }
        
        /** Multiblock shapes also get a full 64 bits of information - does not update from world */
        public void setMultiBlockBits(long bits)
        {
            if(Output.DEBUG_MODE && this.getShape().meshFactory().stateFormat != StateFormat.MULTIBLOCK)
                Output.getLog().warn("setMultiBlockBits on model state does not apply for shape");
            
            bits3 = bits;
            invalidateHashCode();
        }
        
        ////////////////////////////////////////////////////
        //  PACKER 3 ATTRIBUTES  (FLOWING TERRAIN FORMAT)
        ////////////////////////////////////////////////////
        
        public FlowHeightState getFlowState()
        {
            if(Output.DEBUG_MODE && this.getShape().meshFactory().stateFormat != StateFormat.FLOW)
                Output.getLog().warn("getFlowState on model state does not apply for shape");
                
            return new FlowHeightState(P3F_FLOW_JOIN.getValue(bits3));
        }
        
        public void setFlowState(FlowHeightState flowState)
        {
            if(Output.DEBUG_MODE && this.getShape().meshFactory().stateFormat != StateFormat.FLOW)
                Output.getLog().warn("setFlowState on model state does not apply for shape");

            bits3 = P3F_FLOW_JOIN.setValue(flowState.getStateKey(), bits3);
            invalidateHashCode();
        }
        
    }
}
