package grondag.hard_science.superblock.model.state;


import grondag.exotic_matter.model.PaintLayer;
import grondag.exotic_matter.model.RenderPassSet;
import grondag.exotic_matter.model.Translucency;
import grondag.exotic_matter.varia.BitPacker;
import grondag.exotic_matter.varia.BitPacker.BitElement.BooleanElement;
import grondag.exotic_matter.varia.BitPacker.BitElement.EnumElement;
import grondag.exotic_matter.varia.BitPacker.BitElement.IntElement;
import grondag.exotic_matter.varia.BitPacker.BitElement.LongElement;
import grondag.exotic_matter.world.CornerJoinBlockStateSelector;
import grondag.exotic_matter.world.IExtraStateFactory;
import grondag.exotic_matter.world.Rotation;
import grondag.exotic_matter.world.SimpleJoin;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.color.BlockColorMapProvider;
import grondag.hard_science.superblock.terrain.TerrainState;
import grondag.hard_science.superblock.texture.Textures;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ModelStateData
{
    public static final BitPacker PACKER_0 = new BitPacker();
    public static final IntElement P0_SHAPE = PACKER_0.createIntElement(ModelShape.MAX_SHAPES);
    public static final IntElement[] P0_PAINT_COLOR = new IntElement[PaintLayer.DYNAMIC_SIZE];
    public static final BooleanElement P0_AXIS_INVERTED = PACKER_0.createBooleanElement();
    public static final EnumElement<EnumFacing.Axis> P0_AXIS = PACKER_0.createEnumElement(EnumFacing.Axis.class);
    public static final BooleanElement[] P0_IS_TRANSLUCENT = new BooleanElement[PaintLayer.DYNAMIC_SIZE];
    public static final EnumElement<Translucency> P0_TRANSLUCENCY = PACKER_0.createEnumElement(Translucency.class);

    public static final BitPacker PACKER_1 = new BitPacker();
    public static final IntElement[] P1_PAINT_TEXTURE = new IntElement[PaintLayer.STATIC_SIZE];
    public static final BooleanElement[] P1_PAINT_LIGHT= new BooleanElement[PaintLayer.DYNAMIC_SIZE];

    /** note that sign bit on packer 2 is reserved to persist static state during serialization */ 
    public static final BitPacker PACKER_2 = new BitPacker();
    public static final IntElement P2_POS_X = PACKER_2.createIntElement(256);
    public static final IntElement P2_POS_Y = PACKER_2.createIntElement(256);
    public static final IntElement P2_POS_Z = PACKER_2.createIntElement(256);
    /** value semantics are owned by consumer - only constraints are size (39 bits) and does not update from world */
    public static final LongElement P2_STATIC_SHAPE_BITS = PACKER_2.createLongElement(1L << 39);

    public static final BitPacker PACKER_3_BLOCK = new BitPacker();
    public static final IntElement P3B_SPECIES = PACKER_3_BLOCK.createIntElement(16);
    public static final IntElement P3B_BLOCK_JOIN = PACKER_3_BLOCK.createIntElement(CornerJoinBlockStateSelector.BLOCK_JOIN_STATE_COUNT);
    public static final IntElement P3B_MASONRY_JOIN = PACKER_3_BLOCK.createIntElement(SimpleJoin.STATE_COUNT);
    public static final EnumElement<Rotation> P3B_AXIS_ROTATION = PACKER_3_BLOCK.createEnumElement(Rotation.class);

    public static final BitPacker PACKER_3_FLOW = new BitPacker();
    public static final LongElement P3F_FLOW_JOIN = PACKER_3_FLOW.createLongElement(TerrainState.STATE_BIT_MASK + 1);

    public static final BitPacker PACKER_3_MULTIBLOCK = new BitPacker();

    /** used to compare states quickly for border joins  */
    public static final long P0_APPEARANCE_COMPARISON_MASK;
    public static final long P1_APPEARANCE_COMPARISON_MASK;
    public static final long P2_APPEARANCE_COMPARISON_MASK;   

    /** used to compare states quickly for appearance match */
    public static final long P0_APPEARANCE_COMPARISON_MASK_NO_GEOMETRY;

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

    /**
     * Use this as factory for model state block tests that DON'T need to refresh from world.
     */
    public static final IExtraStateFactory<ModelState> TEST_GETTER_STATIC = new IExtraStateFactory<ModelState>()
    {
        @Override
        public ModelState get(IBlockAccess worldIn, BlockPos pos, IBlockState state)
        {
            Block block = state.getBlock();
            return (block instanceof SuperBlock) 
                    ? ((SuperBlock)block).getModelStateAssumeStateIsCurrent(state, worldIn, pos, false)
                    : null;
        }
    };
    
    /**
     * Use this as factory for model state block tests that DO need to refresh from world.
     */
    public static final IExtraStateFactory<ModelState> TEST_GETTER_DYNAMIC = new IExtraStateFactory<ModelState>()
    {
        @Override
        public ModelState get(IBlockAccess worldIn, BlockPos pos, IBlockState state)
        {
            Block block = state.getBlock();
            return (block instanceof SuperBlock) 
                    ? ((SuperBlock)block).getModelStateAssumeStateIsCurrent(state, worldIn, pos, true)
                    : null;
        }
    };
    
    public static final BitPacker STATE_PACKER = new BitPacker();
    
    
    /**
     * For readability.
     */
    public static final int STATE_FLAG_NONE = 0;

    
    /** see {@link #STATE_FLAG_IS_POPULATED} */
    public static final BooleanElement STATE_BIT_IS_POPULATED = STATE_PACKER.createBooleanElement();
    /* 
     * Enables lazy derivation - set after derivation is complete.
     * NB - check logic assumes that ALL bits are zero for simplicity.
     */
    public static final int STATE_FLAG_IS_POPULATED = (int) STATE_BIT_IS_POPULATED.comparisonMask();

    
    /** see {@link #STATE_FLAG_NEEDS_CORNER_JOIN} */
    public static final BooleanElement STATE_BIT_NEEDS_CORNER_JOIN = STATE_PACKER.createBooleanElement();
    /** 
     * Applies to block-type states.  
     * True if is a block type state and requires full join state.
     */
    public static final int STATE_FLAG_NEEDS_CORNER_JOIN = (int) STATE_BIT_NEEDS_CORNER_JOIN.comparisonMask();

    
    /** see {@link #STATE_FLAG_NEEDS_SIMPLE_JOIN} */
    public static final BooleanElement STATE_BIT_NEEDS_SIMPLE_JOIN = STATE_PACKER.createBooleanElement();
    /** 
     * Applies to block-type states.  
     * True if is a block type state and requires full join state.
     */
    public static final int STATE_FLAG_NEEDS_SIMPLE_JOIN = (int) STATE_BIT_NEEDS_SIMPLE_JOIN.comparisonMask();

    
    /** see {@link #STATE_FLAG_NEEDS_MASONRY_JOIN} */
    public static final BooleanElement STATE_BIT_NEEDS_MASONRY_JOIN = STATE_PACKER.createBooleanElement();
    /** 
     * Applies to block-type states.  
     * True if is a block type state and requires masonry join info.
     */
    public static final int STATE_FLAG_NEEDS_MASONRY_JOIN = (int) STATE_BIT_NEEDS_MASONRY_JOIN.comparisonMask();


    /** see {@link #STATE_FLAG_NEEDS_POS} */
    public static final BooleanElement STATE_BIT_NEEDS_POS = STATE_PACKER.createBooleanElement();
    /** 
     * True if position (big-tex) world state is needed. Applies for block and flow state formats.
     */
    public static final int STATE_FLAG_NEEDS_POS = (int) STATE_BIT_NEEDS_POS.comparisonMask();

    
    /** see {@link #STATE_FLAG_NEEDS_SPECIES} */
    public static final BooleanElement STATE_BIT_NEEDS_SPECIES = STATE_PACKER.createBooleanElement();
    public static final int STATE_FLAG_NEEDS_SPECIES = (int) STATE_BIT_NEEDS_SPECIES.comparisonMask();

    
    /** see {@link #STATE_FLAG_HAS_AXIS} */
    public static final BooleanElement STATE_BIT_HAS_AXIS = STATE_PACKER.createBooleanElement();
    public static final int STATE_FLAG_HAS_AXIS = (int) STATE_BIT_HAS_AXIS.comparisonMask();

    
    /** see {@link #STATE_FLAG_NEEDS_TEXTURE_ROTATION} */
    public static final BooleanElement STATE_BIT_NEEDS_TEXTURE_ROTATION = STATE_PACKER.createBooleanElement();
    public static final int STATE_FLAG_NEEDS_TEXTURE_ROTATION = (int) STATE_BIT_NEEDS_TEXTURE_ROTATION.comparisonMask();

    
    /** see {@link #STATE_FLAG_HAS_AXIS_ORIENTATION} */
    public static final BooleanElement STATE_BIT_HAS_AXIS_ORIENTATION = STATE_PACKER.createBooleanElement();
    public static final int STATE_FLAG_HAS_AXIS_ORIENTATION = (int) STATE_BIT_HAS_AXIS_ORIENTATION.comparisonMask();

    /** see {@link #STATE_FLAG_HAS_AXIS_ROTATION} */
    public static final BooleanElement STATE_BIT_HAS_AXIS_ROTATION = STATE_PACKER.createBooleanElement();
    /** Set if shape can be rotated around an axis. Only applies to block models; multiblock models manage this situationally. */
    public static final int STATE_FLAG_HAS_AXIS_ROTATION = (int) STATE_BIT_HAS_AXIS_ROTATION.comparisonMask();


    /** see {@link #STATE_FLAG_HAS_TRANSLUCENT_GEOMETRY} */
    public static final BooleanElement STATE_BIT_HAS_TRANSLUCENT_GEOMETRY = STATE_PACKER.createBooleanElement();
    /** Set if either Base/Cut or Lamp (if present) paint layers are translucent */
    public static final int STATE_FLAG_HAS_TRANSLUCENT_GEOMETRY = (int) STATE_BIT_HAS_TRANSLUCENT_GEOMETRY.comparisonMask();
    
    public static final EnumElement<RenderPassSet> STATE_ENUM_RENDER_PASS_SET = STATE_PACKER.createEnumElement(RenderPassSet.class);
    
    /** use this to turn off flags that should not be used with non-block state formats */
    public static final int STATE_FLAG_DISABLE_BLOCK_ONLY = ~(
            STATE_FLAG_NEEDS_CORNER_JOIN | STATE_FLAG_NEEDS_SIMPLE_JOIN | STATE_FLAG_NEEDS_MASONRY_JOIN
            | STATE_FLAG_NEEDS_SPECIES | STATE_FLAG_NEEDS_TEXTURE_ROTATION);
    
    //hide constructor
    private ModelStateData()
    {
        super();
    }
}
