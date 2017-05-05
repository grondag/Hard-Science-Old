package grondag.adversity.superblock.model.state;


import grondag.adversity.library.Alternator;
import grondag.adversity.library.BitPacker;
import grondag.adversity.library.IAlternator;
import grondag.adversity.library.Useful;
import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.library.BitPacker.BitElement.EnumElement;
import grondag.adversity.library.BitPacker.BitElement.IntElement;
import grondag.adversity.library.joinstate.CornerJoinBlockState;
import grondag.adversity.library.joinstate.CornerJoinBlockStateSelector;
import grondag.adversity.library.joinstate.SimpleJoin;
import grondag.adversity.library.joinstate.SimpleJoinFaceState;
import grondag.adversity.library.Rotation;
import grondag.adversity.library.BitPacker.BitElement.BooleanElement;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.color.BlockColorMapProvider;
import grondag.adversity.niceblock.color.ColorMap;
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
  
    
    //package scope to allow inspection in test harness
    static final BitPacker PACKER_0 = new BitPacker();
    private static final EnumElement<ModelShape> P0_SHAPE = PACKER_0.createEnumElement(ModelShape.class);
    @SuppressWarnings("unchecked")
    private static final EnumElement<SurfacePainter>[] P0_PAINTERS = new EnumElement[MAX_PAINTERS];
    private static final IntElement[] P0_PAINT_COLOR = new IntElement[MAX_PAINTERS];
    @SuppressWarnings("unchecked")
    private static final EnumElement<LightingMode>[] P0_PAINT_LIGHT= new EnumElement[MAX_PAINTERS];
    
    static final BitPacker PACKER_1 = new BitPacker();
    @SuppressWarnings("unchecked")
    private static final EnumElement<SurfaceType>[] P1_SURFACE_TYPES = new EnumElement[MAX_PAINTERS];
    private static final IntElement[] P1_PAINT_TEXTURE = new IntElement[MAX_PAINTERS];
    private static final BooleanElement[] P1_PAINT_ROTATION= new BooleanElement[MAX_PAINTERS];

    static final BitPacker PACKER_2 = new BitPacker();
    @SuppressWarnings("unchecked")
    private static final EnumElement<BlockRenderLayer>[] P2_PAINT_LAYER = new EnumElement[MAX_PAINTERS];
    private static final EnumElement<EnumFacing.Axis> P2_AXIS = PACKER_2.createEnumElement(EnumFacing.Axis.class);
    private static final IntElement P2_BLOCK_VERSION = PACKER_2.createIntElement(8);
    private static final EnumElement<Rotation> P2_ROTATION = PACKER_2.createEnumElement(Rotation.class);
    private static final IntElement P2_BIGTEX_INDEX = PACKER_2.createIntElement(16 * 16 * 16);
    private static final IntElement P2_SPECIES = PACKER_2.createIntElement(16);
    private static final IntElement P2_CORNER_JOIN = PACKER_2.createIntElement(CornerJoinBlockStateSelector.BLOCK_JOIN_STATE_COUNT);
    private static final IntElement P2_SIMPLE_JOIN = PACKER_2.createIntElement(SimpleJoin.STATE_COUNT);
    
    // future components?
    // bigtex version (32 or maybe 64 variants per 16x16x16 nibble)
    // isUpright (show if shape is flipped along axis)
    // isCorner - origin of multiblock is lower corner instead of block center
    // offset - position relative to multiblock origin - 21 bits
    static
    {
        for(int i = 0; i < MAX_PAINTERS; i++)
        {
            // p0 has 3 bits to start
            P0_PAINT_LIGHT[i] = PACKER_0.createEnumElement(LightingMode.class); // 1 bit each x4 = 4
            P0_PAINTERS[i] = PACKER_0.createEnumElement(SurfacePainter.class);   // 3 bits each x4 = 12
            P0_PAINT_COLOR[i] = PACKER_0.createIntElement(BlockColorMapProvider.INSTANCE.getColorMapCount());  // 11 bits each x4 = 44

            
            P1_SURFACE_TYPES[i] = PACKER_1.createEnumElement(SurfaceType.class);  // 2 bits each  x4 = 8
            P1_PAINT_TEXTURE[i] = PACKER_1.createIntElement(Textures.MAX_TEXTURES); // 12 bits each x4 = 48
            P1_PAINT_ROTATION[i] = PACKER_1.createBooleanElement(); // 1 bit each x4 = 4

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
            this.hashCode = -1;
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
            //TODO: see what elements are used by shape and by selected painters
            // then only update those elements
            
            //TODO: simple and corner join - perhaps only check meta? Default to meta when placed that doesn't connect to 
            // different shapes, colors, substance, etc.  But would allow forced connections.  NOTE - would not work for columns.
            // Maybe have parameters on which block test to use, or based on shape?
            
            long localBits = bits2;
            
            localBits = P2_BLOCK_VERSION.setBits(BLOCK_ALTERNATOR.getAlternate(pos), localBits);
            
            localBits = P2_ROTATION.setBits(Rotation.values()[ROTATION_ALTERNATOR.getAlternate(pos)], localBits);
            
            localBits = P2_BIGTEX_INDEX.setBits(((pos.getX() & 15) << 8) | ((pos.getY() & 15) << 4) | (pos.getZ() & 15), localBits);

            localBits = P2_SPECIES.setBits(state.getValue(NiceBlock.META), localBits);
            
            bits2 = localBits;
            
        }

        
        public ModelShape getShape()
        {
            return P0_SHAPE.getValue(bits0);
        }
        
        public void setShape(ModelShape shape)
        {
            bits0 = P0_SHAPE.setBits(shape, bits0);
            invalidateHashCode();
        }
        
        public SurfacePainter getSurfacePainter(int painterIndex)
        {
            return P0_PAINTERS[painterIndex].getValue(bits0);
        }
        
        public void setSurfacePainter(int painterIndex, SurfacePainter painter)
        {
            bits0 = P0_PAINTERS[painterIndex].setBits(painter, bits0);
        }
        
        public SurfaceType getSurfaceType(int painterIndex)
        {
            return P1_SURFACE_TYPES[painterIndex].getValue(bits1);
        }
        
        public void setSurfaceType(int painterIndex, SurfaceType surfaceType)
        {
            bits1 = P1_SURFACE_TYPES[painterIndex].setBits(surfaceType, bits1);
        }
        
        public ColorMap getColorMap(int painterIndex)
        {
            return BlockColorMapProvider.INSTANCE.getColorMap(P0_PAINT_COLOR[painterIndex].getValue(bits0));
        }
        
        public void setColorMap(int painterIndex, ColorMap map)
        {
            bits0 = P0_PAINT_COLOR[painterIndex].setBits(map.ordinal, bits0);
        }

        public Texture getTexture(int painterIndex)
        {
            return Textures.ALL_TEXTURES.get(P1_PAINT_TEXTURE[painterIndex].getValue(bits1));
        }
        
        public void setTexture(int painterIndex, Texture tex)
        {
            bits1 = P1_PAINT_TEXTURE[painterIndex].setBits(tex.ordinal, bits1);
        }
        
        public LightingMode getLightingMode(int painterIndex)
        {
            return P0_PAINT_LIGHT[painterIndex].getValue(bits0);
        }
        
        public void setLightingMode(int painterIndex, LightingMode lightingMode)
        {
            bits0 = P0_PAINT_LIGHT[painterIndex].setBits(lightingMode, bits0);
        }
        
        public BlockRenderLayer getRenderLayer(int painterIndex)
        {
            return P2_PAINT_LAYER[painterIndex].getValue(bits2);
        }
        
        public void setRenderLayer(int painterIndex, BlockRenderLayer renderLayer)
        {
            bits2 = P2_PAINT_LAYER[painterIndex].setBits(renderLayer, bits2);
        }

        public boolean getRotationEnabled(int painterIndex)
        {
            return P1_PAINT_ROTATION[painterIndex].getValue(bits1);
        }
        
        public void setRotationEnabled(int painterIndex, boolean isEnabled)
        {
            bits1 = P1_PAINT_ROTATION[painterIndex].setBits(isEnabled, bits1);
        }
        
        public EnumFacing.Axis getAxis()
        {
            return P2_AXIS.getValue(bits2);
        }
        
        public void setAxis(EnumFacing.Axis axis)
        {
            bits2 = P2_AXIS.setBits(axis, bits2);
        }
        
        public Rotation getRotation()
        {
            return P2_ROTATION.getValue(bits2);
        }
        
        public void setRotation(Rotation rotation)
        {
            bits2 = P2_ROTATION.setBits(rotation, bits2);
        }

        public int getBlockVersion()
        {
            return P2_BLOCK_VERSION.getValue(bits2);
        }
        
        public void setBlockVersion(int version)
        {
            bits2 = P2_BLOCK_VERSION.setBits(version, bits2);
        }
        
        public int getBigTexIndex()
        {
            return P2_BIGTEX_INDEX.getValue(bits2);
        }
        
        public void setBigTexIndex(int index)
        {
            bits2 = P2_BIGTEX_INDEX.setBits(index, bits2);
        }
        
        public int getSpecies()
        {
            return P2_SPECIES.getValue(bits2);
        }
        
        public void setSpecies(int species)
        {
            bits2 = P2_SPECIES.setBits(species, bits2);
        }
        
        public CornerJoinBlockState getCornerJoin()
        {
            return CornerJoinBlockStateSelector.getJoinState(P2_CORNER_JOIN.getValue(bits2));
        }
        
        public void setCornerJoin(CornerJoinBlockState join)
        {
            bits2 = P2_CORNER_JOIN.setBits(join.getIndex(), bits2);
        }
        
        public SimpleJoin getSimpleJoin()
        {
            return new SimpleJoin(P2_SIMPLE_JOIN.getValue(bits2));
        }
        
        public void setSimpleJoin(SimpleJoin join)
        {
            bits2 = P2_SIMPLE_JOIN.setBits(join.getIndex(), bits2);
        }
    }
}
