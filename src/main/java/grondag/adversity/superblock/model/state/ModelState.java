package grondag.adversity.superblock.model.state;


import grondag.adversity.library.Alternator;
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
import grondag.adversity.library.joinstate.SimpleJoinFaceState;
import grondag.adversity.library.Rotation;
import grondag.adversity.library.BitPacker.BitElement.BooleanElement;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.color.BlockColorMapProvider;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import grondag.adversity.niceblock.modelstate.ModelStateComponent.WorldRefreshType;
import grondag.adversity.superblock.model.painter.SurfacePainter;
import grondag.adversity.superblock.model.shape.ModelShape;
import grondag.adversity.superblock.model.shape.SurfaceType;
import grondag.adversity.superblock.texture.TextureProvider2;
import grondag.adversity.superblock.texture.Textures;
import grondag.adversity.superblock.texture.TextureProvider2.Texture;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ModelState
{
    public static final int MAX_PAINTERS = 4;
    
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
    
    static final BitPacker PACKER_1 = new BitPacker();
    @SuppressWarnings("unchecked")
    private static final EnumElement<SurfaceType>[] P1_SURFACE_TYPES = new EnumElement[MAX_PAINTERS];
    private static final IntElement[] P1_PAINT_TEXTURE = new IntElement[MAX_PAINTERS];
    private static final BooleanElement[] P1_PAINT_ROTATION= new BooleanElement[MAX_PAINTERS];

    static final BitPacker PACKER_2 = new BitPacker();
    @SuppressWarnings("unchecked")
    private static final EnumElement<BlockRenderLayer>[] P2_PAINT_LAYER = new EnumElement[MAX_PAINTERS];
    @SuppressWarnings("unchecked")
    private static final EnumElement<LightingMode>[] P2_PAINT_LIGHT= new EnumElement[MAX_PAINTERS];
    private static final EnumElement<EnumFacing.Axis> P2_AXIS = PACKER_2.createEnumElement(EnumFacing.Axis.class);
    private static final BooleanElement P2_AXIS_INVERTED = PACKER_2.createBooleanElement();

    static final BitPacker PACKER_3_BLOCK = new BitPacker();
    private static final IntElement P3B_SPECIES = PACKER_3_BLOCK.createIntElement(16);
    private static final IntElement P3B_BLOCK_VERSION = PACKER_3_BLOCK.createIntElement(8);
    private static final EnumElement<Rotation> P3B_BLOCK_ROTATION = PACKER_3_BLOCK.createEnumElement(Rotation.class);
    private static final IntElement P3B_BIGTEX_INDEX = PACKER_3_BLOCK.createIntElement(16 * 16 * 16);
    private static final IntElement P3B_BLOCK_JOIN = PACKER_3_BLOCK.createIntElement(CornerJoinBlockStateSelector.BLOCK_JOIN_STATE_COUNT);
//    private static final IntElement P3B_SIMPLE_JOIN = PACKER_3_BLOCK.createIntElement(SimpleJoin.STATE_COUNT);
    
    static final BitPacker PACKER_3_MULTIBLOCK = new BitPacker();
    private static final BooleanElement P3M_IS_CORNER = PACKER_3_MULTIBLOCK.createBooleanElement();
    private static final IntElement P3M_OFFSET_X = PACKER_3_MULTIBLOCK.createIntElement(256);
    private static final IntElement P3M_OFFSET_Y = PACKER_3_MULTIBLOCK.createIntElement(256);
    private static final IntElement P3M_OFFSET_Z = PACKER_3_MULTIBLOCK.createIntElement(256);
    
    private static final IntElement P3M_SCALE_X = PACKER_3_MULTIBLOCK.createIntElement(128 * 4);
    private static final IntElement P3M_SCALE_Y = PACKER_3_MULTIBLOCK.createIntElement(128 * 4);
    private static final IntElement P3M_SCALE_Z = PACKER_3_MULTIBLOCK.createIntElement(128 * 4);

    // zero value indicates solid shape
    private static final IntElement P3M_WALL_THICKNESS = PACKER_3_MULTIBLOCK.createIntElement(128 * 4);
    
    static final BitPacker PACKER_3_FLOW = new BitPacker();
    private static final LongElement P3F_FLOW_JOIN = PACKER_3_FLOW.createLongElement(FlowHeightState.STATE_BIT_MASK + 1);
    
    //TODO
    // move bigtex to p2, break out components, doesn't apply to multiblock
    // add shape-defined int to p2
    // add getters and setters and world update and tests for multiblock and flow elements
    // safety checks on shape-dependent get/set methods
    // use same bits for simple/corner joins and use simplest world state reader needed?

    static
    {
        for(int i = 0; i < MAX_PAINTERS; i++)
        {
            // p0 reserve 7 bits for shape
            P0_PAINTERS[i] = PACKER_0.createEnumElement(SurfacePainter.class);   // 3 bits each x4 = 12
            P0_PAINT_COLOR[i] = PACKER_0.createIntElement(BlockColorMapProvider.INSTANCE.getColorMapCount());  // 11 bits each x4 = 44

            
            P1_SURFACE_TYPES[i] = PACKER_1.createEnumElement(SurfaceType.class);  // 2 bits each  x4 = 8
            P1_PAINT_TEXTURE[i] = PACKER_1.createIntElement(Textures.MAX_TEXTURES); // 12 bits each x4 = 48
            P1_PAINT_ROTATION[i] = PACKER_1.createBooleanElement(); // 1 bit each x4 = 4

            P2_PAINT_LIGHT[i] = PACKER_2.createEnumElement(LightingMode.class); // 1 bit each x4 = 4
            P2_PAINT_LAYER[i] = PACKER_2.createEnumElement(BlockRenderLayer.class); // 2 bits each x4 = 8
        }
    }
    
    //hide constructor
    private ModelState()
    {
        super();
    }
    
    public static class StateValue
    {
        private boolean isStatic;
        private long bits0;
        private long bits1;
        private long bits2;
        private long bits3;
        
        private int hashCode = -1;
        
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
         * True if big-tex world state is needed. Applies for block and flow state formats.
         */
        public static final int STATE_FLAG_NEEDS_BIGTEX = STATE_FLAG_NEEDS_SIMPLE_JOIN << 1;
        
        /** 
         * True if block version and rotation are needed. Applies for block formats.
         */
        public static final int STATE_FLAG_NEEDS_BLOCK_RANDOMS= STATE_FLAG_NEEDS_BIGTEX << 1;
        
        /** 
         * True if meta values (species) is needed.
         */
        public static final int STATE_FLAG_NEEDS_SPECIES= STATE_FLAG_NEEDS_BLOCK_RANDOMS << 1;
        
        
        /** contains indicators derived from shape and painters */
        private byte stateFlags;
        
        public StateValue()
        {
            
        }
        
        public StateValue(boolean isStatic, long[] bits)
        {
            this.isStatic = isStatic;
            this.bits0 = bits[0];
            this.bits1 = bits[1];
            this.bits2 = bits[2];
            this.bits3 = bits[3];
        }
        
        private void populateStateFlagsIfNeeded()
        {
            if(stateFlags == 0)
            {
                stateFlags = (byte) (STATE_FLAG_IS_POPULATED | getShape().stateFlags
                        | getSurfacePainter(0).stateFlags | getSurfacePainter(1).stateFlags
                        | getSurfacePainter(2).stateFlags | getSurfacePainter(3).stateFlags);
            }
        }
        
        private void clearStateFlags()
        {
            if(this.stateFlags != 0) this.stateFlags  = 0;
        }
        
        public boolean isStatic() { return this.isStatic; }
        public void setStatic(boolean isStatic) { this.isStatic = isStatic; }
        
        public long[] getBits() 
        {
            long[] bits = new long[4];
            bits[0] = this.bits0;
            bits[1] = this.bits1;
            bits[2] = this.bits2;
            bits[3] = this.bits3;
            return bits;
        }
        
        @Override
        public boolean equals(Object obj)
        {
            if(this == obj) return true;
            
            if(obj instanceof StateValue && obj != null)
            {
                StateValue other = (StateValue)obj;
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
        
        public void refreshFromWorld(IBlockState state, IBlockAccess world, BlockPos pos)
        {
            populateStateFlagsIfNeeded();
            
            long localBits = bits3;
            
            switch(this.getShape().stateFormat)
            {
            case BLOCK:
                
                //TODO: see what elements are used by selected painters
                // then only update those elements

                localBits = P3B_BLOCK_VERSION.setValue(BLOCK_ALTERNATOR.getAlternate(pos), localBits);
                localBits = P3B_BLOCK_ROTATION.setValue(Rotation.values()[ROTATION_ALTERNATOR.getAlternate(pos)], localBits);
                localBits = P3B_BIGTEX_INDEX.setValue(((pos.getX() & 15) << 8) | ((pos.getY() & 15) << 4) | (pos.getZ() & 15), localBits);
                localBits = P3B_SPECIES.setValue(state.getValue(NiceBlock.META), localBits);

                                
                //TODO: simple and corner join - perhaps only check meta? Default to meta when placed that doesn't connect to 
                // different shapes, colors, substance, etc.  But would allow forced connections.  NOTE - would not work for columns.
                // Maybe have parameters on which block test to use, or based on shape?
                
                break;

            case FLOW:
                localBits = P3F_FLOW_JOIN.setValue(FlowHeightState.getBitsFromWorldStatically((NiceBlock)state.getBlock(), state, world, pos), localBits);
                break;
            
            case MULTIBLOCK:
                break;
                
            default:
                break;
            
            }
            
            bits3 = localBits;
            
        }

        
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
        
        public SurfaceType getSurfaceType(int painterIndex)
        {
            return P1_SURFACE_TYPES[painterIndex].getValue(bits1);
        }
        
        public void setSurfaceType(int painterIndex, SurfaceType surfaceType)
        {
            bits1 = P1_SURFACE_TYPES[painterIndex].setValue(surfaceType, bits1);
            invalidateHashCode();
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

        public Texture getTexture(int painterIndex)
        {
            return Textures.ALL_TEXTURES.get(P1_PAINT_TEXTURE[painterIndex].getValue(bits1));
        }
        
        public void setTexture(int painterIndex, Texture tex)
        {
            bits1 = P1_PAINT_TEXTURE[painterIndex].setValue(tex.ordinal, bits1);
            invalidateHashCode();
        }
        
        public LightingMode getLightingMode(int painterIndex)
        {
            return P2_PAINT_LIGHT[painterIndex].getValue(bits2);
        }
        
        public void setLightingMode(int painterIndex, LightingMode lightingMode)
        {
            bits2 = P2_PAINT_LIGHT[painterIndex].setValue(lightingMode, bits2);
            invalidateHashCode();
        }
        
        public BlockRenderLayer getRenderLayer(int painterIndex)
        {
            return P2_PAINT_LAYER[painterIndex].getValue(bits2);
        }
        
        public void setRenderLayer(int painterIndex, BlockRenderLayer renderLayer)
        {
            bits2 = P2_PAINT_LAYER[painterIndex].setValue(renderLayer, bits2);
            invalidateHashCode();
        }

        public boolean getRotationEnabled(int painterIndex)
        {
            return P1_PAINT_ROTATION[painterIndex].getValue(bits1);
        }
        
        public void setRotationEnabled(int painterIndex, boolean isEnabled)
        {
            bits1 = P1_PAINT_ROTATION[painterIndex].setValue(isEnabled, bits1);
            invalidateHashCode();
        }
        
        public EnumFacing.Axis getAxis()
        {
            return P2_AXIS.getValue(bits2);
        }
        
        public void setAxis(EnumFacing.Axis axis)
        {
            bits2 = P2_AXIS.setValue(axis, bits2);
            invalidateHashCode();
        }
        
        public boolean isAxisInverted()
        {
            return P2_AXIS_INVERTED.getValue(bits2);
        }
        
        public void setAxisInverted(boolean isInverted)
        {
            bits2 = P2_AXIS_INVERTED.setValue(isInverted, bits2);
            invalidateHashCode();
        }
        
        public Rotation getRotation()
        {
            populateStateFlagsIfNeeded();
            return P3B_BLOCK_ROTATION.getValue(bits3);
        }
        
        public void setRotation(Rotation rotation)
        {
            populateStateFlagsIfNeeded();
            bits3 = P3B_BLOCK_ROTATION.setValue(rotation, bits3);
            invalidateHashCode();
        }

        public int getBlockVersion()
        {
            return P3B_BLOCK_VERSION.getValue(bits3);
        }
        
        public void setBlockVersion(int version)
        {
            bits3 = P3B_BLOCK_VERSION.setValue(version, bits3);
            invalidateHashCode();
        }
        
        public int getBigTexIndex()
        {
            return P3B_BIGTEX_INDEX.getValue(bits3);
        }
        
        public void setBigTexIndex(int index)
        {
            bits3 = P3B_BIGTEX_INDEX.setValue(index, bits3);
            invalidateHashCode();
        }
        
        public int getSpecies()
        {
            return P3B_SPECIES.getValue(bits3);
        }
        
        public void setSpecies(int species)
        {
            bits3 = P3B_SPECIES.setValue(species, bits3);
            invalidateHashCode();
        }
        
        public CornerJoinBlockState getCornerJoin()
        {
            return CornerJoinBlockStateSelector.getJoinState(P3B_BLOCK_JOIN.getValue(bits3));
        }
        
        public void setCornerJoin(CornerJoinBlockState join)
        {
            bits3 = P3B_BLOCK_JOIN.setValue(join.getIndex(), bits3);
            invalidateHashCode();
        }
        
//        public SimpleJoin getSimpleJoin()
//        {
//            return new SimpleJoin(P3B_SIMPLE_JOIN.getValue(bits3));
//        }
//        
//        public void setSimpleJoin(SimpleJoin join)
//        {
//            bits3 = P3B_SIMPLE_JOIN.setValue(join.getIndex(), bits3);
//            invalidateHashCode();
//        }
    }
}
