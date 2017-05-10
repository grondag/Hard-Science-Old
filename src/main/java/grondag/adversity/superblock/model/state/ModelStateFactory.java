package grondag.adversity.superblock.model.state;


import grondag.adversity.Output;
import grondag.adversity.library.Alternator;
import grondag.adversity.library.BinaryEnumSet;
import grondag.adversity.library.BitPacker;
import grondag.adversity.library.IAlternator;
import grondag.adversity.library.Useful;
import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.library.BitPacker.BitElement.EnumElement;
import grondag.adversity.library.BitPacker.BitElement.IntElement;
import grondag.adversity.library.BitPacker.BitElement.LongElement;
import grondag.adversity.library.joinstate.CornerJoinBlockState;
import grondag.adversity.library.joinstate.CornerJoinBlockStateSelector;
import grondag.adversity.library.joinstate.SimpleJoin;
import grondag.adversity.library.Rotation;
import grondag.adversity.library.BitPacker.BitElement.BooleanElement;
import grondag.adversity.niceblock.color.BlockColorMapProvider;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import grondag.adversity.superblock.block.SuperBlock;
import grondag.adversity.superblock.model.painter.SurfacePainter;
import grondag.adversity.superblock.model.painter.surface.Surface;
import grondag.adversity.superblock.model.shape.ModelShape;
import grondag.adversity.superblock.texture.Textures;
import grondag.adversity.superblock.texture.TexturePalletteProvider.TexturePallette;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ModelStateFactory
{
    public static final int MAX_PAINTERS = 4;
    public static final int MAX_SURFACES = 4;
    
    private static final IAlternator ROTATION_ALTERNATOR = Alternator.getAlternator(4, 45927934);
    private static final IAlternator BLOCK_ALTERNATOR = Alternator.getAlternator(8, 2953424);
  
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
    @SuppressWarnings("unchecked")
    private static final EnumElement<SurfacePainter>[] P0_PAINTERS = new EnumElement[MAX_PAINTERS];
    private static final IntElement[] P0_PAINT_COLOR = new IntElement[MAX_PAINTERS];
    private static final BooleanElement P0_AXIS_INVERTED = PACKER_0.createBooleanElement();
    private static final EnumElement<EnumFacing.Axis> P0_AXIS = PACKER_0.createEnumElement(EnumFacing.Axis.class);
    
    static final BitPacker PACKER_1 = new BitPacker();
    private static final IntElement[] P1_SURFACE_INDEX = new IntElement[MAX_PAINTERS];
    private static final IntElement[] P1_PAINT_TEXTURE = new IntElement[MAX_PAINTERS];
    private static final BooleanElement[] P1_PAINT_ROTATION= new BooleanElement[MAX_PAINTERS];
    @SuppressWarnings("unchecked")
    private static final EnumElement<LightingMode>[] P1_PAINT_LIGHT= new EnumElement[MAX_PAINTERS];

    static final BitPacker PACKER_2 = new BitPacker();
    @SuppressWarnings("unchecked")
    private static final EnumElement<BlockRenderLayer>[] P2_PAINT_LAYER = new EnumElement[MAX_PAINTERS];
    private static final IntElement P2_POS_X = PACKER_2.createIntElement(16);
    private static final IntElement P2_POS_Y = PACKER_2.createIntElement(16);
    private static final IntElement P2_POS_Z = PACKER_2.createIntElement(16);
    /** value semantics are owned by consumer - only constraints are size and does not update from world */
    private static final LongElement P2_STATIC_SHAPE_BITS = PACKER_2.createLongElement(1L << 44);

    static final BitPacker PACKER_3_BLOCK = new BitPacker();
    private static final IntElement P3B_SPECIES = PACKER_3_BLOCK.createIntElement(16);
    private static final IntElement P3B_BLOCK_VERSION = PACKER_3_BLOCK.createIntElement(8);
    private static final EnumElement<Rotation> P3B_BLOCK_ROTATION = PACKER_3_BLOCK.createEnumElement(Rotation.class);
    private static final IntElement P3B_BLOCK_JOIN = PACKER_3_BLOCK.createIntElement(CornerJoinBlockStateSelector.BLOCK_JOIN_STATE_COUNT);
    
    static final BitPacker PACKER_3_FLOW = new BitPacker();
    private static final LongElement P3F_FLOW_JOIN = PACKER_3_FLOW.createLongElement(FlowHeightState.STATE_BIT_MASK + 1);

    
    static
    {
        for(int i = 0; i < MAX_PAINTERS; i++)
        {
            // p0 reserve 7 bits for shape
            P0_PAINTERS[i] = PACKER_0.createEnumElement(SurfacePainter.class);   // 3 bits each x4 = 12
            P0_PAINT_COLOR[i] = PACKER_0.createIntElement(BlockColorMapProvider.INSTANCE.getColorMapCount());  // 11 bits each x4 = 44

            
            P1_SURFACE_INDEX[i] = PACKER_1.createIntElement(MAX_SURFACES);  // 2 bits each  x4 = 8
            P1_PAINT_TEXTURE[i] = PACKER_1.createIntElement(Textures.MAX_TEXTURES); // 12 bits each x4 = 48
            P1_PAINT_ROTATION[i] = PACKER_1.createBooleanElement(); // 1 bit each x4 = 4
            P1_PAINT_LIGHT[i] = PACKER_1.createEnumElement(LightingMode.class); // 1 bit each x4 = 4

            P2_PAINT_LAYER[i] = PACKER_2.createEnumElement(BlockRenderLayer.class); // 2 bits each x4 = 8
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
        public static final int STATE_FLAG_NEEDS_BLOCK_RANDOMS= STATE_FLAG_NEEDS_POS << 1;
        
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
        private byte stateFlags;
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
                
                for(int i = 0; i < 4; i++)
                {
                    SurfacePainter p =  getSurfacePainter(i);
                    if(p != SurfacePainter.NONE)
                    {
                        flags |= p.stateFlags;
                        layerFlags = BENUMSET_RENDER_LAYER.setFlagForValue(this.getRenderLayer(i), layerFlags, true);
                        
                        if(this.getLightingMode(i) == LightingMode.FULLBRIGHT) 
                            shadedFlags = BENUMSET_RENDER_LAYER.setFlagForValue(this.getRenderLayer(i), shadedFlags, false);
                    }
                }
                this.stateFlags = (byte) flags;
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
            if(this.isStatic) return this;
            
            populateStateFlagsIfNeeded();
            
            if((stateFlags & STATE_FLAG_NEEDS_POS) == STATE_FLAG_NEEDS_POS)
            {
                long b2 = bits2;
                b2 = P2_POS_X.setValue((pos.getX() & 15), b2);
                b2 = P2_POS_Y.setValue((pos.getY() & 15), b2);
                b2 = P2_POS_Z.setValue((pos.getZ() & 15), b2);
                bits2 = b2;
            }
            
            switch(this.getShape().meshFactory().stateFormat)
            {
            case BLOCK:
                
                long b3 = bits3;
                
                if((STATE_FLAG_NEEDS_BLOCK_RANDOMS & stateFlags) == STATE_FLAG_NEEDS_BLOCK_RANDOMS)
                {
                    b3 = P3B_BLOCK_VERSION.setValue(BLOCK_ALTERNATOR.getAlternate(pos), b3);
                    b3 = P3B_BLOCK_ROTATION.setValue(Rotation.values()[ROTATION_ALTERNATOR.getAlternate(pos)], b3);
                }
                
                //TODO: simple and corner join - perhaps only check meta? Default to meta when placed that doesn't connect to 
                // different shapes, colors, substance, etc.  But would allow forced connections.  NOTE - would not work for columns.
                // Maybe have parameters on which block test to use, or based on shape?
                if((STATE_FLAG_NEEDS_CORNER_JOIN & stateFlags) == STATE_FLAG_NEEDS_CORNER_JOIN)
                {
//                    b3 = CornerJoinBlockStateSelector.findIndex(tests)
                }
                else if ((STATE_FLAG_NEEDS_SIMPLE_JOIN & stateFlags) == STATE_FLAG_NEEDS_SIMPLE_JOIN)
                {
                    
                }
                
                bits3 = b3;
                
                break;

            case FLOW:
                bits3 = P3F_FLOW_JOIN.setValue(FlowHeightState.getBitsFromWorldStatically((SuperBlock)state.getBlock(), state, world, pos), bits3);
                break;
            
            case MULTIBLOCK:
                break;
                
            default:
                break;
            
            }
            
            return this;
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
        
        public SurfacePainter getSurfacePainter(int painterIndex)
        {
            return P0_PAINTERS[painterIndex].getValue(bits0);
        }
        
        public void setSurfacePainter(int painterIndex, SurfacePainter painter)
        {
            bits0 = P0_PAINTERS[painterIndex].setValue(painter, bits0);
            invalidateHashCode();
            clearStateFlags();
        }
        
        public ColorMap getColorMap(int painterIndex)
        {
            return BlockColorMapProvider.INSTANCE.getColorMap(P0_PAINT_COLOR[painterIndex].getValue(bits0));
        }
        
        public void setColorMap(int painterIndex, ColorMap map)
        {
            bits0 = P0_PAINT_COLOR[painterIndex].setValue(map.ordinal, bits0);
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

        ////////////////////////////////////////////////////
        //  PACKER 1 ATTRIBUTES (NOT SHAPE-DEPENDENT)
        ////////////////////////////////////////////////////
        
        public Surface getSurface(int painterIndex)
        {
            return getShape().meshFactory().surfaces.get(P1_SURFACE_INDEX[painterIndex].getValue(bits1));
        }
        
        /**
         * Doesn't check that the provided surface actually belongs to the shape
         * that is set in this state.  Be good. :-)
         */
        public void setSurface(int painterIndex, Surface surface)
        {
            bits1 = P1_SURFACE_INDEX[painterIndex].setValue(surface.ordinal, bits1);
            invalidateHashCode();
        }
        
        public TexturePallette getTexture(int painterIndex)
        {
            return Textures.ALL_TEXTURES.get(P1_PAINT_TEXTURE[painterIndex].getValue(bits1));
        }
        
        public void setTexture(int painterIndex, TexturePallette tex)
        {
            bits1 = P1_PAINT_TEXTURE[painterIndex].setValue(tex.ordinal, bits1);
            invalidateHashCode();
        }
        
        public LightingMode getLightingMode(int painterIndex)
        {
            return P1_PAINT_LIGHT[painterIndex].getValue(bits1);
        }
        
        public void setLightingMode(int painterIndex, LightingMode lightingMode)
        {
            bits1 = P1_PAINT_LIGHT[painterIndex].setValue(lightingMode, bits1);
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
        public byte getRenderLayerShadedFlags() { return this.renderLayerShadedFlags; };
        
        public boolean getRotationEnabled(int painterIndex)
        {
            return P1_PAINT_ROTATION[painterIndex].getValue(bits1);
        }
        
        public void setRotationEnabled(int painterIndex, boolean isEnabled)
        {
            bits1 = P1_PAINT_ROTATION[painterIndex].setValue(isEnabled, bits1);
            invalidateHashCode();
        }
        
        ////////////////////////////////////////////////////
        //  PACKER 2 ATTRIBUTES  (NOT SHAPE-DEPENDENT)
        ////////////////////////////////////////////////////
        
        public BlockRenderLayer getRenderLayer(int painterIndex)
        {
            return P2_PAINT_LAYER[painterIndex].getValue(bits2);
        }
        
        public void setRenderLayer(int painterIndex, BlockRenderLayer renderLayer)
        {
            bits2 = P2_PAINT_LAYER[painterIndex].setValue(renderLayer, bits2);
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
        public byte getCanRenderInLayerFlags() { return this.renderLayerEnabledFlags; };
        
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
        
        public Rotation getRotation()
        {
            if(Output.DEBUG_MODE && this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
                Output.getLog().warn("getRotation on model state does not apply for shape");
            
            populateStateFlagsIfNeeded();
            return P3B_BLOCK_ROTATION.getValue(bits3);
        }
        
        public void setRotation(Rotation rotation)
        {
            if(Output.DEBUG_MODE && this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
                Output.getLog().warn("setRotation on model state does not apply for shape");
            
            populateStateFlagsIfNeeded();
            bits3 = P3B_BLOCK_ROTATION.setValue(rotation, bits3);
            invalidateHashCode();
        }

        public int getBlockVersion()
        {
            if(Output.DEBUG_MODE && this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
                Output.getLog().warn("setBlockVersion on model state does not apply for shape");
            
            return P3B_BLOCK_VERSION.getValue(bits3);
        }
        
        public void setBlockVersion(int version)
        {
            if(Output.DEBUG_MODE && this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
                Output.getLog().warn("setBlockVersion on model state does not apply for shape");
            
            bits3 = P3B_BLOCK_VERSION.setValue(version, bits3);
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
            if(Output.DEBUG_MODE && this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
                Output.getLog().warn("setCornerJoin on model state does not apply for shape");
            
            return CornerJoinBlockStateSelector.getJoinState(P3B_BLOCK_JOIN.getValue(bits3));
        }
        
        public void setCornerJoin(CornerJoinBlockState join)
        {
            if(Output.DEBUG_MODE && this.getShape().meshFactory().stateFormat != StateFormat.BLOCK)
                Output.getLog().warn("setCornerJoin on model state does not apply for shape");
            
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
