package grondag.adversity.niceblock;

import grondag.adversity.feature.volcano.VolcanicLavaBlock;
import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.niceblock.base.ModelDispatcher;
import grondag.adversity.niceblock.base.ModelFactory;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.base.NiceBlockPlus;
import grondag.adversity.niceblock.base.NiceItemBlock;
import grondag.adversity.niceblock.base.NiceTileEntity;
import grondag.adversity.niceblock.block.BigBlock;
import grondag.adversity.niceblock.block.CSGBlock;
import grondag.adversity.niceblock.block.ColumnSquareBlock;
import grondag.adversity.niceblock.block.FlowDynamicBlock;
import grondag.adversity.niceblock.block.FlowSimpleBlock;
import grondag.adversity.niceblock.block.FlowStaticBlock;
import grondag.adversity.niceblock.block.HeightBlock;
import grondag.adversity.niceblock.model.BigTexModelFactory;
import grondag.adversity.niceblock.model.BorderModelFactory;
import grondag.adversity.niceblock.model.CSGModelFactory;
import grondag.adversity.niceblock.model.ColorModelFactory;
import grondag.adversity.niceblock.model.ColumnSquareModelFactory;
import grondag.adversity.niceblock.model.FlowModelFactory2;
import grondag.adversity.niceblock.model.HeightModelFactory;
import grondag.adversity.niceblock.model.MasonryModelFactory;
import grondag.adversity.niceblock.modelstate.ModelStateComponents;
import grondag.adversity.niceblock.support.BaseMaterial;
import grondag.adversity.niceblock.support.NiceBlockHighlighter;
import grondag.adversity.niceblock.support.NiceBlockStateMapper;
import grondag.adversity.niceblock.support.NicePlacement;

import java.io.IOException;
import java.util.LinkedList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Contains instances for all NiceBlocks and handles all creation and registration of same. Includes handling of associated items, textures and models. It is also the subscriber
 * for model bake and texture stitch events, but handles these simply by calling handler methods on the models associated with the blocks.
 */
public class NiceBlockRegistrar
{

    private static final NiceBlockRegistrar instance = new NiceBlockRegistrar();

    /**
     * NiceBlocks add themselves here so that we can easily iterate them during registration
     */
    public static LinkedList<NiceBlock> allBlocks = new LinkedList<NiceBlock>();

    /**
     * NiceItemBlocks add themselves here so that we can easily iterate them during registration
     */
    public static LinkedList<NiceItemBlock> allItems = new LinkedList<NiceItemBlock>();

    /**
     * Model dispatchers add themselves here for handling during model bake and texture stitch
     */
    public static LinkedList<ModelDispatcher> allDispatchers = new LinkedList<ModelDispatcher>();

    // DECLARE MODEL DISPATCH & BLOCK INSTANCES
    private final static ModelFactory.ModelInputs RAW_FLEXSTONE_INPUTS = new ModelFactory.ModelInputs("raw_flexstone", LightingMode.SHADED, BlockRenderLayer.SOLID);
    private final static ColorModelFactory RAW_FLEXSTONE_MODEL = new ColorModelFactory(RAW_FLEXSTONE_INPUTS, ModelStateComponents.COLORS_RAW_FLEXSTONE,
            ModelStateComponents.TEXTURE_4, ModelStateComponents.ROTATION);
    private static final ModelDispatcher RAW_FLEXSTONE_DISPATCH = new ModelDispatcher(RAW_FLEXSTONE_MODEL);
    public static final NiceBlock RAW_FLEXSTONE_BLOCK = new NiceBlock(RAW_FLEXSTONE_DISPATCH, BaseMaterial.FLEXSTONE, "raw");
    public static final NiceItemBlock RAW_FLEXSTONE_ITEM = new NiceItemBlock(RAW_FLEXSTONE_BLOCK);
    
    private final static ModelFactory.ModelInputs RAW_DURASTONE_INPUTS = new ModelFactory.ModelInputs("raw_durastone", LightingMode.SHADED, BlockRenderLayer.SOLID);
    private final static ColorModelFactory RAW_DURASTONE_MODEL = new ColorModelFactory(RAW_DURASTONE_INPUTS, ModelStateComponents.COLORS_RAW_DURASTONE,
            ModelStateComponents.TEXTURE_4, ModelStateComponents.ROTATION);
    private static final ModelDispatcher RAW_DURASTONE_DISPATCH = new ModelDispatcher(RAW_DURASTONE_MODEL);
    public static final NiceBlock RAW_DURASTONE_BLOCK = new NiceBlock(RAW_DURASTONE_DISPATCH, BaseMaterial.DURASTONE, "raw");
    public static final NiceItemBlock RAW_DURASTONE_ITEM = new NiceItemBlock(RAW_DURASTONE_BLOCK);

    private final static ModelFactory.ModelInputs COLORED_STONE_INPUTS = new ModelFactory.ModelInputs("colored_stone", LightingMode.SHADED, BlockRenderLayer.SOLID);
    private final static ColorModelFactory COLORED_STONE_MODEL = new ColorModelFactory(COLORED_STONE_INPUTS, ModelStateComponents.COLORS_BLOCK,
            ModelStateComponents.TEXTURE_4, ModelStateComponents.ROTATION);
    private static final ModelDispatcher COLORED_STONE_DISPATCH = new ModelDispatcher(COLORED_STONE_MODEL);
    public static final NiceBlockPlus COLORED_STONE_BLOCK = new NiceBlockPlus(COLORED_STONE_DISPATCH, BaseMaterial.FLEXSTONE, "colored");
    public static final NiceItemBlock COLORED_STONE_ITEM = new NiceItemBlock(COLORED_STONE_BLOCK);
   
    private final static ModelFactory.ModelInputs BIGTEX_INPUTS = new ModelFactory.ModelInputs("weathered_smooth_stone", LightingMode.SHADED, BlockRenderLayer.SOLID);
    private final static BigTexModelFactory BIGTEX_MODEL = new BigTexModelFactory(BIGTEX_INPUTS, BigTexModelFactory.BigTexScale.LARGE, ModelStateComponents.COLORS_BLOCK,
            ModelStateComponents.BIG_TEX_META_VARIED, ModelStateComponents.TEXTURE_1);
    private static final ModelDispatcher BIGTEX_DISPATCH = new ModelDispatcher(BIGTEX_MODEL);
    public static final NiceBlockPlus BIGTEX_BLOCK = new NiceBlockPlus(BIGTEX_DISPATCH, BaseMaterial.FLEXSTONE, "bigtex");
    public static final NiceItemBlock BIGTEX_ITEM = new NiceItemBlock(BIGTEX_BLOCK);

    private final static ModelFactory.ModelInputs BORDER_INPUTS = new ModelFactory.ModelInputs("bordertest", LightingMode.SHADED, BlockRenderLayer.TRANSLUCENT);
    private final static BorderModelFactory BORDER_MODEL = new BorderModelFactory(BORDER_INPUTS, ModelStateComponents.COLORS_BLOCK,
            ModelStateComponents.CORNER_JOIN, ModelStateComponents.TEXTURE_1);

    private static final ModelDispatcher BORDER_BIGTEX_DISPATCH = new ModelDispatcher(BIGTEX_MODEL, BORDER_MODEL);
    public static final BigBlock BORDER_BIGTEX_BLOCK = new BigBlock(BORDER_BIGTEX_DISPATCH, BaseMaterial.FLEXSTONE, "border", NicePlacement.PLACEMENT_3x3x3);
    public static final NiceItemBlock BORDER_BIGTEX_ITEM = new NiceItemBlock(BORDER_BIGTEX_BLOCK);

    private final static ModelFactory.ModelInputs MASONRY_INPUTS = new ModelFactory.ModelInputs("masonrytest", LightingMode.SHADED, BlockRenderLayer.CUTOUT_MIPPED);
    private final static MasonryModelFactory MASONRY_MODEL = new MasonryModelFactory(MASONRY_INPUTS, ModelStateComponents.COLORS_BLOCK,
            ModelStateComponents.MASONRY_JOIN, ModelStateComponents.TEXTURE_1);

    private static final ModelDispatcher MASONRY_BIGTEX_DISPATCH = new ModelDispatcher(BIGTEX_MODEL, MASONRY_MODEL);
    public static final BigBlock MASONRY_BIGTEX_BLOCK = new BigBlock(MASONRY_BIGTEX_DISPATCH, BaseMaterial.FLEXSTONE, "bigbrick", NicePlacement.PLACEMENT_2x1x1);
    public static final NiceItemBlock MASONRY_BIGTEX_ITEM = new NiceItemBlock(MASONRY_BIGTEX_BLOCK);

    private static final ModelFactory.ModelInputs COLUMN_INPUTS_BASE 
        = new ModelFactory.ModelInputs("colored_stone", LightingMode.SHADED, BlockRenderLayer.SOLID);

    private static final ModelFactory.ModelInputs COLUMN_INPUTS_LAMP 
    = new ModelFactory.ModelInputs("colored_stone", LightingMode.FULLBRIGHT, BlockRenderLayer.SOLID);

    // need overlay on a separate layer to keep it out of AO lighting
    private static final ModelFactory.ModelInputs COLUMN_INPUTS_OVERLAY 
    = new ModelFactory.ModelInputs("colored_stone", LightingMode.SHADED, BlockRenderLayer.CUTOUT_MIPPED);
    
    
    private static final ColumnSquareModelFactory.ColumnSquareInputs COLUMN_INPUTS_2_INNER 
         = new ColumnSquareModelFactory.ColumnSquareInputs(COLUMN_INPUTS_LAMP, 2, true, ColumnSquareModelFactory.ModelType.LAMP_BASE);
    private static final ColumnSquareModelFactory.ColumnSquareInputs COLUMN_INPUTS_2_OUTER 
    = new ColumnSquareModelFactory.ColumnSquareInputs(COLUMN_INPUTS_OVERLAY, 2, true, ColumnSquareModelFactory.ModelType.LAMP_OVERLAY);

    private final static ColumnSquareModelFactory COLUMN_MODEL_2_INNER 
        = new ColumnSquareModelFactory(COLUMN_INPUTS_2_INNER, ModelStateComponents.COLORS_BLOCK,
            ModelStateComponents.CORNER_JOIN, ModelStateComponents.TEXTURE_1, ModelStateComponents.AXIS);
    private final static ColumnSquareModelFactory COLUMN_MODEL_2_OUTER 
        = new ColumnSquareModelFactory(COLUMN_INPUTS_2_OUTER, ModelStateComponents.COLORS_BLOCK,
        ModelStateComponents.CORNER_JOIN, ModelStateComponents.TEXTURE_1, ModelStateComponents.AXIS);
    
    private static final ModelDispatcher COLUMN_2_DISPATCH = new ModelDispatcher(COLUMN_MODEL_2_INNER , COLUMN_MODEL_2_OUTER);
    public static final ColumnSquareBlock COLUMN_2_BLOCK = (ColumnSquareBlock) new ColumnSquareBlock(COLUMN_2_DISPATCH, BaseMaterial.FLEXSTONE, "column_square_2");
    public static final NiceItemBlock COLUMN_2_ITEM = new NiceItemBlock(COLUMN_2_BLOCK);

    
    
    private static final ColumnSquareModelFactory.ColumnSquareInputs COLUMN_INPUTS_3 
        = new ColumnSquareModelFactory.ColumnSquareInputs(COLUMN_INPUTS_BASE, 3, true, ColumnSquareModelFactory.ModelType.NORMAL);
  
    private final static ColumnSquareModelFactory COLUMN_MODEL_3
        = new ColumnSquareModelFactory(COLUMN_INPUTS_3, ModelStateComponents.COLORS_BLOCK,
        ModelStateComponents.CORNER_JOIN, ModelStateComponents.TEXTURE_1, ModelStateComponents.AXIS);
    
    private static final ModelDispatcher COLUMN_3_DISPATCH = new ModelDispatcher(COLUMN_MODEL_3);
    public static final ColumnSquareBlock COLUMN_3_BLOCK = (ColumnSquareBlock) new ColumnSquareBlock(COLUMN_3_DISPATCH, BaseMaterial.FLEXSTONE, "column_square_3");
    public static final NiceItemBlock COLUMN_3_ITEM = new NiceItemBlock(COLUMN_3_BLOCK);

    
    private static final ColumnSquareModelFactory.ColumnSquareInputs COLUMN_INPUTS_4_INNER 
        = new ColumnSquareModelFactory.ColumnSquareInputs(COLUMN_INPUTS_LAMP, 4, false, ColumnSquareModelFactory.ModelType.LAMP_BASE);
    private static final ColumnSquareModelFactory.ColumnSquareInputs COLUMN_INPUTS_4_OUTER 
        = new ColumnSquareModelFactory.ColumnSquareInputs(COLUMN_INPUTS_OVERLAY, 4, false, ColumnSquareModelFactory.ModelType.LAMP_OVERLAY);
    
    private final static ColumnSquareModelFactory COLUMN_MODEL_4_INNER 
       = new ColumnSquareModelFactory(COLUMN_INPUTS_4_INNER, ModelStateComponents.COLORS_BLOCK,
           ModelStateComponents.CORNER_JOIN, ModelStateComponents.TEXTURE_1, ModelStateComponents.AXIS);
    private final static ColumnSquareModelFactory COLUMN_MODEL_4_OUTER 
       = new ColumnSquareModelFactory(COLUMN_INPUTS_4_OUTER, ModelStateComponents.COLORS_BLOCK,
       ModelStateComponents.CORNER_JOIN, ModelStateComponents.TEXTURE_1, ModelStateComponents.AXIS);
    
    private static final ModelDispatcher COLUMN_4_DISPATCH = new ModelDispatcher(COLUMN_MODEL_4_INNER, COLUMN_MODEL_4_OUTER);
    public static final ColumnSquareBlock COLUMN_4_BLOCK = (ColumnSquareBlock) new ColumnSquareBlock(COLUMN_4_DISPATCH, BaseMaterial.FLEXSTONE, "column_square_4");
    public static final NiceItemBlock COLUMN_4_ITEM = new NiceItemBlock(COLUMN_4_BLOCK);

    
    private static final ColumnSquareModelFactory.ColumnSquareInputs COLUMN_INPUTS_5_INNER 
        = new ColumnSquareModelFactory.ColumnSquareInputs(COLUMN_INPUTS_LAMP, 5, false, ColumnSquareModelFactory.ModelType.LAMP_BASE);
    private static final ColumnSquareModelFactory.ColumnSquareInputs COLUMN_INPUTS_5_OUTER 
        = new ColumnSquareModelFactory.ColumnSquareInputs(COLUMN_INPUTS_OVERLAY, 5, false, ColumnSquareModelFactory.ModelType.LAMP_OVERLAY);
    
    private final static ColumnSquareModelFactory COLUMN_MODEL_5_INNER 
       = new ColumnSquareModelFactory(COLUMN_INPUTS_5_INNER, ModelStateComponents.COLORS_BLOCK,
           ModelStateComponents.CORNER_JOIN, ModelStateComponents.TEXTURE_1, ModelStateComponents.AXIS);
    private final static ColumnSquareModelFactory COLUMN_MODEL_5_OUTER 
       = new ColumnSquareModelFactory(COLUMN_INPUTS_5_OUTER, ModelStateComponents.COLORS_BLOCK,
       ModelStateComponents.CORNER_JOIN, ModelStateComponents.TEXTURE_1, ModelStateComponents.AXIS);
    
    private static final ModelDispatcher COLUMN_5_DISPATCH = new ModelDispatcher(COLUMN_MODEL_5_INNER, COLUMN_MODEL_5_OUTER);
    public static final ColumnSquareBlock COLUMN_5_BLOCK = (ColumnSquareBlock) new ColumnSquareBlock(COLUMN_5_DISPATCH, BaseMaterial.FLEXSTONE, "column_square_5");
    public static final NiceItemBlock COLUMN_5_ITEM = new NiceItemBlock(COLUMN_5_BLOCK);

    private final static ModelFactory.ModelInputs COOL_SQUARE_BASALT_INPUTS = new ModelFactory.ModelInputs("basalt_cut", LightingMode.SHADED, BlockRenderLayer.SOLID);
    private final static BigTexModelFactory COOL_SQUARE_BASALT_MODEL 
        = new BigTexModelFactory(COOL_SQUARE_BASALT_INPUTS, BigTexModelFactory.BigTexScale.LARGE, ModelStateComponents.COLORS_BASALT,
                ModelStateComponents.BIG_TEX_IGNORE_META, ModelStateComponents.TEXTURE_1);
    private static final ModelDispatcher COOL_SQUARE_BASALT_DISPATCH = new ModelDispatcher(COOL_SQUARE_BASALT_MODEL);
    public static final FlowSimpleBlock COOL_SQUARE_BASALT_BLOCK = new FlowSimpleBlock(COOL_SQUARE_BASALT_DISPATCH, BaseMaterial.BASALT, "cool");
    public static final NiceItemBlock COOL_SQUARE_BASALT_ITEM = new NiceItemBlock(COOL_SQUARE_BASALT_BLOCK);

    
//    private final static ModelFactory.ModelInputs HOT_SQUARE_BASALT_INPUTS = new ModelFactory.ModelInputs("hot_basalt", false, BlockRenderLayer.TRANSLUCENT);
//    private final static SpeciesModelFactory HOT_SQUARE_BASALT_MODEL 
//        = new SpeciesModelFactory(HOT_SQUARE_BASALT_INPUTS, ModelStateComponents.COLORS_WHITE,
//            ModelStateComponents.TEXTURE_4, ModelStateComponents.ROTATION, ModelStateComponents.SPECIES_4);
//    private static final ModelDispatcher HOT_SQUARE_BASALT_DISPATCH = new ModelDispatcher( COOL_SQUARE_BASALT_MODEL, HOT_SQUARE_BASALT_MODEL);
//    public static final NiceBlock HOT_SQUARE_BASALT_BLOCK = (NiceBlock) new HotBasaltBlock(HOT_SQUARE_BASALT_DISPATCH, BaseMaterial.BASALT, "hot");
//    public static final NiceItemBlock HOT_SQUARE_BASALT_ITEM = new NiceItemBlock(HOT_SQUARE_BASALT_BLOCK);

    
    private final static ModelFactory.ModelInputs HEIGHT_STONE_INPUTS = new ModelFactory.ModelInputs("colored_stone", LightingMode.SHADED, BlockRenderLayer.SOLID);
    private final static HeightModelFactory HEIGHT_STONE_MODEL = new HeightModelFactory(HEIGHT_STONE_INPUTS, ModelStateComponents.COLORS_BLOCK,
            ModelStateComponents.TEXTURE_4, ModelStateComponents.ROTATION, ModelStateComponents.SPECIES_16);
    private static final ModelDispatcher HEIGHT_STONE_DISPATCH = new ModelDispatcher(HEIGHT_STONE_MODEL);
    public static final NiceBlockPlus HEIGHT_STONE_BLOCK = new HeightBlock(HEIGHT_STONE_DISPATCH, BaseMaterial.FLEXSTONE, "stacked");
    public static final NiceItemBlock HEIGHT_STONE_ITEM = new NiceItemBlock(HEIGHT_STONE_BLOCK);

    
    private final static ModelFactory.ModelInputs HOT_FLOWING_LAVA_INPUTS = new ModelFactory.ModelInputs("lava", LightingMode.FULLBRIGHT, BlockRenderLayer.SOLID);
    private final static FlowModelFactory2 HOT_FLOWING_LAVA_MODEL = new FlowModelFactory2(HOT_FLOWING_LAVA_INPUTS, true, ModelStateComponents.FLOW_JOIN,
            ModelStateComponents.FLOW_TEX, ModelStateComponents.TEXTURE_1, ModelStateComponents.ROTATION_NONE, ModelStateComponents.COLORS_WHITE);
    private static final ModelDispatcher HOT_FLOWING_LAVA_DISPATCH = new ModelDispatcher(HOT_FLOWING_LAVA_MODEL);    
    public static final VolcanicLavaBlock HOT_FLOWING_LAVA_HEIGHT_BLOCK = 
             new VolcanicLavaBlock(HOT_FLOWING_LAVA_DISPATCH, BaseMaterial.VOLCANIC_LAVA, "flow", false);
    
    public static final NiceItemBlock HOT_FLOWING_LAVA_HEIGHT_ITEM = new NiceItemBlock(HOT_FLOWING_LAVA_HEIGHT_BLOCK);
    public static final VolcanicLavaBlock HOT_FLOWING_LAVA_FILLER_BLOCK = 
             new VolcanicLavaBlock(HOT_FLOWING_LAVA_DISPATCH, BaseMaterial.VOLCANIC_LAVA, "fill", true);
    public static final NiceItemBlock HOT_FLOWING_LAVA_FILLER_ITEM = new NiceItemBlock(HOT_FLOWING_LAVA_FILLER_BLOCK);
    
    private final static ModelFactory.ModelInputs COOL_FLOWING_BASALT_INPUTS = new ModelFactory.ModelInputs("basalt_cool", LightingMode.SHADED, BlockRenderLayer.SOLID);
    private final static FlowModelFactory2 COOL_FLOWING_BASALT_MODEL = new FlowModelFactory2(COOL_FLOWING_BASALT_INPUTS, true, ModelStateComponents.FLOW_JOIN,
            ModelStateComponents.FLOW_TEX, ModelStateComponents.TEXTURE_1, ModelStateComponents.ROTATION_NONE, ModelStateComponents.COLORS_BASALT);
    
    private final static ModelFactory.ModelInputs HOT_FLOWING_BASALT_0_INPUTS = new ModelFactory.ModelInputs("basalt_cooling", LightingMode.SHADED, BlockRenderLayer.TRANSLUCENT);
    private final static FlowModelFactory2 HOT_FLOWING_BASALT_0_MODEL = new FlowModelFactory2(HOT_FLOWING_BASALT_0_INPUTS, false, ModelStateComponents.FLOW_JOIN,
            ModelStateComponents.FLOW_TEX, ModelStateComponents.TEXTURE_1, ModelStateComponents.ROTATION_NONE, ModelStateComponents.COLORS_BASALT);

    private final static ModelFactory.ModelInputs HOT_FLOWING_BASALT_1_INPUTS = new ModelFactory.ModelInputs("basalt_warm", LightingMode.SHADED, BlockRenderLayer.TRANSLUCENT);
    private final static FlowModelFactory2 HOT_FLOWING_BASALT_1_MODEL = new FlowModelFactory2(HOT_FLOWING_BASALT_1_INPUTS, false, ModelStateComponents.FLOW_JOIN,
            ModelStateComponents.FLOW_TEX, ModelStateComponents.TEXTURE_1, ModelStateComponents.ROTATION_NONE, ModelStateComponents.COLORS_BASALT);

    private final static ModelFactory.ModelInputs HOT_FLOWING_BASALT_2_INPUTS = new ModelFactory.ModelInputs("basalt_hot", LightingMode.SHADED, BlockRenderLayer.TRANSLUCENT);
    private final static FlowModelFactory2 HOT_FLOWING_BASALT_2_MODEL = new FlowModelFactory2(HOT_FLOWING_BASALT_2_INPUTS, false, ModelStateComponents.FLOW_JOIN,
            ModelStateComponents.FLOW_TEX, ModelStateComponents.TEXTURE_1, ModelStateComponents.ROTATION_NONE, ModelStateComponents.COLORS_BASALT);

    private final static ModelFactory.ModelInputs HOT_FLOWING_BASALT_3_INPUTS = new ModelFactory.ModelInputs("basalt_very_hot", LightingMode.SHADED, BlockRenderLayer.TRANSLUCENT);
    private final static FlowModelFactory2 HOT_FLOWING_BASALT_3_MODEL = new FlowModelFactory2(HOT_FLOWING_BASALT_3_INPUTS, false, ModelStateComponents.FLOW_JOIN,
            ModelStateComponents.FLOW_TEX, ModelStateComponents.TEXTURE_1, ModelStateComponents.ROTATION_NONE, ModelStateComponents.COLORS_BASALT);

    private static final ModelDispatcher HOT_FLOWING_BASALT_0_DISPATCH = new ModelDispatcher(HOT_FLOWING_LAVA_MODEL, HOT_FLOWING_BASALT_0_MODEL);    
    public static final FlowDynamicBlock HOT_FLOWING_BASALT_0_HEIGHT_BLOCK = new FlowDynamicBlock(HOT_FLOWING_BASALT_0_DISPATCH, BaseMaterial.BASALT, "cooling_flow", false);
    public static final NiceItemBlock HOT_FLOWING_BASALT_0_HEIGHT_ITEM = new NiceItemBlock(HOT_FLOWING_BASALT_0_HEIGHT_BLOCK);
    public static final FlowDynamicBlock HOT_FLOWING_BASALT_0_FILLER_BLOCK = new FlowDynamicBlock(HOT_FLOWING_BASALT_0_DISPATCH, BaseMaterial.BASALT, "cooling_fill", true);
    public static final NiceItemBlock HOT_FLOWING_BASALT_0_FILLER_ITEM = new NiceItemBlock(HOT_FLOWING_BASALT_0_FILLER_BLOCK);

    private static final ModelDispatcher HOT_FLOWING_BASALT_1_DISPATCH = new ModelDispatcher(HOT_FLOWING_LAVA_MODEL, HOT_FLOWING_BASALT_1_MODEL);    
    public static final FlowDynamicBlock HOT_FLOWING_BASALT_1_HEIGHT_BLOCK = new FlowDynamicBlock(HOT_FLOWING_BASALT_1_DISPATCH, BaseMaterial.BASALT, "warm_flow", false);
    public static final NiceItemBlock HOT_FLOWING_BASALT_1_HEIGHT_ITEM = new NiceItemBlock(HOT_FLOWING_BASALT_1_HEIGHT_BLOCK);
    public static final FlowDynamicBlock HOT_FLOWING_BASALT_1_FILLER_BLOCK = new FlowDynamicBlock(HOT_FLOWING_BASALT_1_DISPATCH, BaseMaterial.BASALT, "warm_fill", true);
    public static final NiceItemBlock HOT_FLOWING_BASALT_1_FILLER_ITEM = new NiceItemBlock(HOT_FLOWING_BASALT_1_FILLER_BLOCK);

    private static final ModelDispatcher HOT_FLOWING_BASALT_2_DISPATCH = new ModelDispatcher(HOT_FLOWING_LAVA_MODEL, HOT_FLOWING_BASALT_2_MODEL);    
    public static final FlowDynamicBlock HOT_FLOWING_BASALT_2_HEIGHT_BLOCK = new FlowDynamicBlock(HOT_FLOWING_BASALT_2_DISPATCH, BaseMaterial.BASALT, "hot_flow", false);
    public static final NiceItemBlock HOT_FLOWING_BASALT_2_HEIGHT_ITEM = new NiceItemBlock(HOT_FLOWING_BASALT_2_HEIGHT_BLOCK);
    public static final FlowDynamicBlock HOT_FLOWING_BASALT_2_FILLER_BLOCK = new FlowDynamicBlock(HOT_FLOWING_BASALT_2_DISPATCH, BaseMaterial.BASALT, "hot_fill", true);
    public static final NiceItemBlock HOT_FLOWING_BASALT_2_FILLER_ITEM = new NiceItemBlock(HOT_FLOWING_BASALT_2_FILLER_BLOCK);

    private static final ModelDispatcher HOT_FLOWING_BASALT_3_DISPATCH = new ModelDispatcher(HOT_FLOWING_LAVA_MODEL, HOT_FLOWING_BASALT_3_MODEL);    
    public static final FlowDynamicBlock HOT_FLOWING_BASALT_3_HEIGHT_BLOCK = new FlowDynamicBlock(HOT_FLOWING_BASALT_3_DISPATCH, BaseMaterial.BASALT, "very_hot_flow", false);
    public static final NiceItemBlock HOT_FLOWING_BASALT_3_HEIGHT_ITEM = new NiceItemBlock(HOT_FLOWING_BASALT_3_HEIGHT_BLOCK);
    public static final FlowDynamicBlock HOT_FLOWING_BASALT_3_FILLER_BLOCK = new FlowDynamicBlock(HOT_FLOWING_BASALT_3_DISPATCH, BaseMaterial.BASALT, "very_hot_fill", true);
    public static final NiceItemBlock HOT_FLOWING_BASALT_3_FILLER_ITEM = new NiceItemBlock(HOT_FLOWING_BASALT_3_FILLER_BLOCK);

    private static final ModelDispatcher COOL_FLOWING_BASALT_DISPATCH = new ModelDispatcher(COOL_FLOWING_BASALT_MODEL); 
    
    public static final FlowStaticBlock COOL_STATIC_BASALT_HEIGHT_BLOCK = new FlowStaticBlock(COOL_FLOWING_BASALT_DISPATCH, BaseMaterial.BASALT, "static_flow", false);
    public static final NiceItemBlock COOL_STATIC_BASALT_HEIGHT_ITEM = new NiceItemBlock(COOL_STATIC_BASALT_HEIGHT_BLOCK);
    public static final FlowStaticBlock COOL_STATIC_BASALT_FILLER_BLOCK = new FlowStaticBlock(COOL_FLOWING_BASALT_DISPATCH, BaseMaterial.BASALT, "static_fill", true);
    public static final NiceItemBlock COOL_STATIC_BASALT_FILLER_ITEM = new NiceItemBlock(COOL_STATIC_BASALT_FILLER_BLOCK);
   
    public static final FlowDynamicBlock COOL_FLOWING_BASALT_HEIGHT_BLOCK = new FlowDynamicBlock(COOL_FLOWING_BASALT_DISPATCH, BaseMaterial.BASALT, "flow", false, COOL_STATIC_BASALT_HEIGHT_BLOCK);
    public static final NiceItemBlock COOL_FLOWING_BASALT_HEIGHT_ITEM = new NiceItemBlock(COOL_FLOWING_BASALT_HEIGHT_BLOCK);
    public static final FlowDynamicBlock COOL_FLOWING_BASALT_FILLER_BLOCK = new FlowDynamicBlock(COOL_FLOWING_BASALT_DISPATCH, BaseMaterial.BASALT, "fill", true, COOL_STATIC_BASALT_FILLER_BLOCK);
    public static final NiceItemBlock COOL_FLOWING_BASALT_FILLER_ITEM = new NiceItemBlock(COOL_FLOWING_BASALT_FILLER_BLOCK);


 

    
    private final static ModelFactory.ModelInputs CSG_TEST_INPUTS = new ModelFactory.ModelInputs("colored_stone", LightingMode.SHADED, BlockRenderLayer.SOLID);
    private final static CSGModelFactory CSG_TEST_MODEL = new CSGModelFactory(CSG_TEST_INPUTS, ModelStateComponents.COLORS_WHITE,
            ModelStateComponents.TEXTURE_4, ModelStateComponents.ROTATION);
    private static final ModelDispatcher CSG_TEST_DISPATCH = new ModelDispatcher(CSG_TEST_MODEL);
    public static final CSGBlock CSG_TEST_BLOCK = new CSGBlock(CSG_TEST_DISPATCH, BaseMaterial.FLEXSTONE, "csg_test");
    public static final NiceItemBlock CSG_TEST_ITEM = new NiceItemBlock(CSG_TEST_BLOCK);

    
    // csg block

    
    // public static final ModelDispatcherBasic MODEL_LAVA = new ModelDispatcherBasic(new NoColorMapProvider(16), "volcanic_lava_flow_0_0",
    // new FlowController("volcanic_lava_flow", 1, BlockRenderLayer.SOLID, LightingMode.FULLBRIGHT));
    // public static final NiceBlock BLOCK_LAVA = (NiceBlock) new LavaBlock(new FlowHeightHelper(MODEL_LAVA, 16), BaseMaterial.FLEXSTONE, "flowing_lava")
    // .setLightLevel(3F/15F);
    //
    //
    // public static final ModelDispatcherBasic MODEL_COOL_BASALT = new ModelDispatcherBasic(new NoColorMapProvider(16), "cool_basalt_0_0",
    // new FlowController("cool_basalt", 1, BlockRenderLayer.SOLID, LightingMode.SHADED));
    // public static final NiceBlock BLOCK_COOL_BASALT = (NiceBlock) new FlowHeightBlock(new FlowHeightHelper(MODEL_COOL_BASALT, 16), BaseMaterial.FLEXSTONE, "cool_basalt");
    //
    // public static final NiceBlock BLOCK_COOL_BASALT_FILLER = (NiceBlock) new FlowFillerBlock(new FlowHeightHelper(MODEL_COOL_BASALT, 5), BaseMaterial.FLEXSTONE,
    // "cool_basalt_filler");
    //
    // public static final ModelDispatcher MODEL_CSG_TEST = new ModelDispatcherBasic(
    // BlockColorMapProvider.INSTANCE, "colored_stone_0_0",
    // new CSGController("colored_stone", 1, BlockRenderLayer.SOLID, true, false));
    // public static final CSGBlock BLOCK_CSG_TEST = new CSGBlock(new ColorHelperPlus(MODEL_CSG_TEST), BaseMaterial.FLEXSTONE, "CSG");
    //

    public static void preInit(FMLPreInitializationEvent event)
    {
        // SET UP COLOR ATLAS
        // {
        // NiceHues.INSTANCE.writeColorAtlas(event.getModConfigurationDirectory());
        // }

        // REGISTER ALL BLOCKS
        for (NiceBlock block : allBlocks)
        {
            GameRegistry.register(block);
        }
        
        // REGISTER ALL ITEMS
        for (NiceItemBlock item : allItems)
        {
            item.registerSelf();
        }
        

        if (event.getSide() == Side.CLIENT)
        {
            for (NiceBlock block : allBlocks)
            {
                ModelLoader.setCustomStateMapper(block, NiceBlockStateMapper.instance);

                for (ItemStack stack : block.getSubItems())
                {
                    ModelResourceLocation itemModelResourceLocation = new ModelResourceLocation(((NiceBlock) block).getRegistryName() + "." + stack.getMetadata(), "inventory");
                    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), stack.getMetadata(), itemModelResourceLocation);
                }
            }
        }

        GameRegistry.registerTileEntity(NiceTileEntity.class, "nicetileentity2");

        if (event.getSide() == Side.CLIENT)
        {
            // Register event handlers for nice blocks (they are in this class)
            MinecraftForge.EVENT_BUS.register(instance);

            // Register custom block highlighter for blocks with irregular hitboxes.
            MinecraftForge.EVENT_BUS.register(NiceBlockHighlighter.instance);
        }
    }

    public static void init(FMLInitializationEvent event)
    {
        for (NiceBlock block : allBlocks)
        {
            // won't work in pre-init because BlockColors/ItemColors aren't instantiated yet
            // Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(block.blockModelHelper.dispatcher, block);
            Minecraft.getMinecraft().getItemColors().registerItemColorHandler(DummyColorHandler.INSTANCE, block);
        }
    }

    private static class DummyColorHandler implements IItemColor
    {
        private static final DummyColorHandler INSTANCE = new DummyColorHandler();
        
        @Override
        public int getColorFromItemstack(ItemStack stack, int tintIndex) {
            return 0xFFFFFFFF;
        }
    }
    /**
     * Centralized event handler for NiceModel baking.
     */
    @SubscribeEvent
    public void onModelBakeEvent(ModelBakeEvent event) throws IOException
    {
        for (ModelDispatcher dispatcher : allDispatchers)
        {
            dispatcher.handleBakeEvent(event);
            // dispatcher.controller.getBakedModelFactory().handleBakeEvent(event);

            event.getModelRegistry().putObject(new ModelResourceLocation(dispatcher.getModelResourceString()), dispatcher);
        }

        for (ModelDispatcher dispatcher : allDispatchers)
        {
            dispatcher.handleBakeEvent(event);
            // dispatcher.controller.getBakedModelFactory().handleBakeEvent(event);

            event.getModelRegistry().putObject(new ModelResourceLocation(dispatcher.getModelResourceString()), dispatcher);
        }

        for (NiceBlock block : allBlocks)
        {
            for (ItemStack stack : block.getSubItems())
            {
                event.getModelRegistry().putObject(new ModelResourceLocation(block.getRegistryName() + "." + stack.getMetadata(), "inventory"),
                        block.dispatcher);
            }
        }
    }

    /**
     * Centralized event handler for NiceModel texture stitch.
     */
    @SubscribeEvent
    public void stitcherEventPre(TextureStitchEvent.Pre event)
    {
        for (ModelDispatcher dispatcher : allDispatchers)
        {
            dispatcher.handleTexturePreStitch(event);
        }

        for (ModelDispatcher dispatcher : allDispatchers)
        {
            dispatcher.handleTexturePreStitch(event);
        }
    }

    // /**
    // * Centralized event handler for NiceModel texture stitch.
    // */
    // @SubscribeEvent
    // public void stitcherEventPost(TextureStitchEvent.Post event) {
    // for (ModelRegistration reg : allModels) {
    // reg.model.handleTexturePostStitch(event);
    // }
    // }

    // /**
    // * Contains stuff we need to replace model references during model bake.
    // */
    // private static class ModelRegistration{
    // public final NiceModel model;
    // public final ModelResourceLocation mrlBlock;
    // public final ModelResourceLocation mrlItem;
    //
    // public ModelRegistration(NiceModel model, ModelResourceLocation mrlBlock, ModelResourceLocation mrlItem){
    // this.model = model;
    // this.mrlBlock = mrlBlock;
    // this.mrlItem = mrlItem;
    // }
    // }

}
