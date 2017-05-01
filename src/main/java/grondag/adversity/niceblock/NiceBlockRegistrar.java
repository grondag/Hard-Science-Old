package grondag.adversity.niceblock;

import grondag.adversity.feature.volcano.lava.CoolingBlock;
import grondag.adversity.feature.volcano.lava.VolcanicLavaBlock;
import grondag.adversity.init.ModItems;
import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.niceblock.base.ModelDispatcher;
import grondag.adversity.niceblock.base.ModelFactory;
import grondag.adversity.niceblock.base.ModelHolder;
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
import grondag.adversity.niceblock.model.shape.CSGModelFactory;
import grondag.adversity.niceblock.model.shape.FlowModelFactory;
import grondag.adversity.niceblock.model.shape.HeightModelFactory;
import grondag.adversity.niceblock.model.shape.painter.BigTexModelFactory;
import grondag.adversity.niceblock.model.shape.painter.BorderModelFactory;
import grondag.adversity.niceblock.model.shape.painter.ColorModelFactory;
import grondag.adversity.niceblock.model.shape.painter.MasonryModelFactory;
import grondag.adversity.niceblock.model.texture.TextureProvider;
import grondag.adversity.niceblock.model.texture.TextureProviders;
import grondag.adversity.niceblock.model.texture.TextureScale;
import grondag.adversity.niceblock.modelstate.ModelStateComponents;
import grondag.adversity.niceblock.support.BaseMaterial;
import grondag.adversity.niceblock.support.NiceBlockHighlighter;
import grondag.adversity.niceblock.support.NiceBlockStateMapper;
import grondag.adversity.niceblock.support.NicePlacement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.event.TextureStitchEvent.Pre;
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

    private final static ModelHolder RAW_FLEXSTONE_MODEL = new ModelHolder(new ColorModelFactory(ModelStateComponents.COLORS_BLOCK,
            ModelStateComponents.TEXTURE_4, ModelStateComponents.ROTATION), 
            TextureProviders.TEX_BLOCK_RAW_FLEXSTONE.getTextureState(true, LightingMode.SHADED, BlockRenderLayer.SOLID));
    private static final ModelDispatcher RAW_FLEXSTONE_DISPATCH = new ModelDispatcher(RAW_FLEXSTONE_MODEL);
    public static final NiceBlock RAW_FLEXSTONE_BLOCK = new NiceBlock(RAW_FLEXSTONE_DISPATCH, BaseMaterial.FLEXSTONE, "raw");
    public static final NiceItemBlock RAW_FLEXSTONE_ITEM = new NiceItemBlock(RAW_FLEXSTONE_BLOCK);
    
    private final static ModelHolder RAW_DURASTONE_MODEL = new ModelHolder(new ColorModelFactory(ModelStateComponents.COLORS_BLOCK,
            ModelStateComponents.TEXTURE_4, ModelStateComponents.ROTATION),
            TextureProviders.TEX_BLOCK_RAW_DURASTONE.getTextureState(true, LightingMode.SHADED, BlockRenderLayer.SOLID));
    private static final ModelDispatcher RAW_DURASTONE_DISPATCH = new ModelDispatcher(RAW_DURASTONE_MODEL);
    public static final NiceBlock RAW_DURASTONE_BLOCK = new NiceBlock(RAW_DURASTONE_DISPATCH, BaseMaterial.DURASTONE, "raw");
    public static final NiceItemBlock RAW_DURASTONE_ITEM = new NiceItemBlock(RAW_DURASTONE_BLOCK);

    private final static ModelHolder COLORED_STONE_MODEL = new ModelHolder(new ColorModelFactory(ModelStateComponents.COLORS_BLOCK,
            ModelStateComponents.TEXTURE_4, ModelStateComponents.ROTATION),
            TextureProviders.TEX_BLOCK_COLORED_STONE.getTextureState(true, LightingMode.SHADED, BlockRenderLayer.SOLID));
    private static final ModelDispatcher COLORED_STONE_DISPATCH = new ModelDispatcher(COLORED_STONE_MODEL);
    public static final NiceBlockPlus COLORED_STONE_BLOCK = new NiceBlockPlus(COLORED_STONE_DISPATCH, BaseMaterial.FLEXSTONE, "colored");
    public static final NiceItemBlock COLORED_STONE_ITEM = new NiceItemBlock(COLORED_STONE_BLOCK);
    
    private final static ModelHolder BIGTEX_MODEL = new ModelHolder(new BigTexModelFactory(ModelStateComponents.COLORS_BLOCK,
            ModelStateComponents.BIG_TEX_META_VARIED, ModelStateComponents.TEXTURE_1),
            TextureProviders.TEX_BT_WEATHERED_STONE.getTextureState(false, LightingMode.SHADED, BlockRenderLayer.SOLID));
    private static final ModelDispatcher BIGTEX_DISPATCH = new ModelDispatcher(BIGTEX_MODEL);
    public static final NiceBlockPlus BIGTEX_BLOCK = new NiceBlockPlus(BIGTEX_DISPATCH, BaseMaterial.FLEXSTONE, "bigtex");
    public static final NiceItemBlock BIGTEX_ITEM = new NiceItemBlock(BIGTEX_BLOCK);

    private final static ModelHolder BORDER_MODEL = new ModelHolder(new BorderModelFactory(ModelStateComponents.COLORS_BLOCK,
            ModelStateComponents.CORNER_JOIN, ModelStateComponents.TEXTURE_1),
            TextureProviders.TEX_BORDER_TEST.getTextureState(false, LightingMode.SHADED, BlockRenderLayer.TRANSLUCENT));

    private static final ModelDispatcher BORDER_BIGTEX_DISPATCH = new ModelDispatcher(BIGTEX_MODEL, BORDER_MODEL);
    public static final BigBlock BORDER_BIGTEX_BLOCK = new BigBlock(BORDER_BIGTEX_DISPATCH, BaseMaterial.FLEXSTONE, "border", NicePlacement.PLACEMENT_3x3x3);
    public static final NiceItemBlock BORDER_BIGTEX_ITEM = new NiceItemBlock(BORDER_BIGTEX_BLOCK);

    private final static ModelHolder MASONRY_MODEL = new ModelHolder(new MasonryModelFactory(ModelStateComponents.COLORS_BLOCK,
            ModelStateComponents.MASONRY_JOIN, ModelStateComponents.TEXTURE_1),
            TextureProviders.TEX_MASONRY_TEST.getTextureState(false, LightingMode.SHADED, BlockRenderLayer.CUTOUT_MIPPED));

    private static final ModelDispatcher MASONRY_BIGTEX_DISPATCH = new ModelDispatcher(BIGTEX_MODEL, MASONRY_MODEL);
    public static final BigBlock MASONRY_BIGTEX_BLOCK = new BigBlock(MASONRY_BIGTEX_DISPATCH, BaseMaterial.FLEXSTONE, "bigbrick", NicePlacement.PLACEMENT_2x1x1);
    public static final NiceItemBlock MASONRY_BIGTEX_ITEM = new NiceItemBlock(MASONRY_BIGTEX_BLOCK);

//    private static final ModelAppearance COLUMN_INPUTS_BASE 
//        = new ModelAppearance("colored_stone", LightingMode.SHADED, BlockRenderLayer.SOLID);
//
//    private static final ModelAppearance COLUMN_INPUTS_LAMP 
//    = new ModelAppearance("colored_stone", LightingMode.FULLBRIGHT, BlockRenderLayer.SOLID);
//
//    // need overlay on a separate layer to keep it out of AO lighting
//    private static final ModelAppearance COLUMN_INPUTS_OVERLAY 
//    = new ModelAppearance("colored_stone", LightingMode.SHADED, BlockRenderLayer.CUTOUT_MIPPED);
//    
//    
//    private static final ColumnSquareModelFactory.ColumnSquareInputs COLUMN_INPUTS_2_INNER 
//         = new ColumnSquareModelFactory.ColumnSquareInputs(COLUMN_INPUTS_LAMP, 2, true, ColumnSquareModelFactory.ModelType.LAMP_BASE);
//    private static final ColumnSquareModelFactory.ColumnSquareInputs COLUMN_INPUTS_2_OUTER 
//    = new ColumnSquareModelFactory.ColumnSquareInputs(COLUMN_INPUTS_OVERLAY, 2, true, ColumnSquareModelFactory.ModelType.LAMP_OVERLAY);
//
//    private final static ColumnSquareModelFactory COLUMN_MODEL_2_INNER 
//        = new ColumnSquareModelFactory(COLUMN_INPUTS_2_INNER, ModelStateComponents.COLORS_BLOCK,
//            ModelStateComponents.CORNER_JOIN, ModelStateComponents.TEXTURE_1, ModelStateComponents.AXIS);
//    private final static ColumnSquareModelFactory COLUMN_MODEL_2_OUTER 
//        = new ColumnSquareModelFactory(COLUMN_INPUTS_2_OUTER, ModelStateComponents.COLORS_BLOCK,
//        ModelStateComponents.CORNER_JOIN, ModelStateComponents.TEXTURE_1, ModelStateComponents.AXIS);
//    
//    private static final ModelDispatcher COLUMN_2_DISPATCH = new ModelDispatcher(COLUMN_MODEL_2_INNER , COLUMN_MODEL_2_OUTER);
//    public static final ColumnSquareBlock COLUMN_2_BLOCK = (ColumnSquareBlock) new ColumnSquareBlock(COLUMN_2_DISPATCH, BaseMaterial.FLEXSTONE, "column_square_2");
//    public static final NiceItemBlock COLUMN_2_ITEM = new NiceItemBlock(COLUMN_2_BLOCK);
//
//    
//    
//    private static final ColumnSquareModelFactory.ColumnSquareInputs COLUMN_INPUTS_3 
//        = new ColumnSquareModelFactory.ColumnSquareInputs(COLUMN_INPUTS_BASE, 3, true, ColumnSquareModelFactory.ModelType.NORMAL);
//  
//    private final static ColumnSquareModelFactory COLUMN_MODEL_3
//        = new ColumnSquareModelFactory(COLUMN_INPUTS_3, ModelStateComponents.COLORS_BLOCK,
//        ModelStateComponents.CORNER_JOIN, ModelStateComponents.TEXTURE_1, ModelStateComponents.AXIS);
//    
//    private static final ModelDispatcher COLUMN_3_DISPATCH = new ModelDispatcher(COLUMN_MODEL_3);
//    public static final ColumnSquareBlock COLUMN_3_BLOCK = (ColumnSquareBlock) new ColumnSquareBlock(COLUMN_3_DISPATCH, BaseMaterial.FLEXSTONE, "column_square_3");
//    public static final NiceItemBlock COLUMN_3_ITEM = new NiceItemBlock(COLUMN_3_BLOCK);
//
//    
//    private static final ColumnSquareModelFactory.ColumnSquareInputs COLUMN_INPUTS_4_INNER 
//        = new ColumnSquareModelFactory.ColumnSquareInputs(COLUMN_INPUTS_LAMP, 4, false, ColumnSquareModelFactory.ModelType.LAMP_BASE);
//    private static final ColumnSquareModelFactory.ColumnSquareInputs COLUMN_INPUTS_4_OUTER 
//        = new ColumnSquareModelFactory.ColumnSquareInputs(COLUMN_INPUTS_OVERLAY, 4, false, ColumnSquareModelFactory.ModelType.LAMP_OVERLAY);
//    
//    private final static ColumnSquareModelFactory COLUMN_MODEL_4_INNER 
//       = new ColumnSquareModelFactory(COLUMN_INPUTS_4_INNER, ModelStateComponents.COLORS_BLOCK,
//           ModelStateComponents.CORNER_JOIN, ModelStateComponents.TEXTURE_1, ModelStateComponents.AXIS);
//    private final static ColumnSquareModelFactory COLUMN_MODEL_4_OUTER 
//       = new ColumnSquareModelFactory(COLUMN_INPUTS_4_OUTER, ModelStateComponents.COLORS_BLOCK,
//       ModelStateComponents.CORNER_JOIN, ModelStateComponents.TEXTURE_1, ModelStateComponents.AXIS);
//    
//    private static final ModelDispatcher COLUMN_4_DISPATCH = new ModelDispatcher(COLUMN_MODEL_4_INNER, COLUMN_MODEL_4_OUTER);
//    public static final ColumnSquareBlock COLUMN_4_BLOCK = (ColumnSquareBlock) new ColumnSquareBlock(COLUMN_4_DISPATCH, BaseMaterial.FLEXSTONE, "column_square_4");
//    public static final NiceItemBlock COLUMN_4_ITEM = new NiceItemBlock(COLUMN_4_BLOCK);
//
//    
//    private static final ColumnSquareModelFactory.ColumnSquareInputs COLUMN_INPUTS_5_INNER 
//        = new ColumnSquareModelFactory.ColumnSquareInputs(COLUMN_INPUTS_LAMP, 5, false, ColumnSquareModelFactory.ModelType.LAMP_BASE);
//    private static final ColumnSquareModelFactory.ColumnSquareInputs COLUMN_INPUTS_5_OUTER 
//        = new ColumnSquareModelFactory.ColumnSquareInputs(COLUMN_INPUTS_OVERLAY, 5, false, ColumnSquareModelFactory.ModelType.LAMP_OVERLAY);
//    
//    private final static ColumnSquareModelFactory COLUMN_MODEL_5_INNER 
//       = new ColumnSquareModelFactory(COLUMN_INPUTS_5_INNER, ModelStateComponents.COLORS_BLOCK,
//           ModelStateComponents.CORNER_JOIN, ModelStateComponents.TEXTURE_1, ModelStateComponents.AXIS);
//    private final static ColumnSquareModelFactory COLUMN_MODEL_5_OUTER 
//       = new ColumnSquareModelFactory(COLUMN_INPUTS_5_OUTER, ModelStateComponents.COLORS_BLOCK,
//       ModelStateComponents.CORNER_JOIN, ModelStateComponents.TEXTURE_1, ModelStateComponents.AXIS);
//    
//    private static final ModelDispatcher COLUMN_5_DISPATCH = new ModelDispatcher(COLUMN_MODEL_5_INNER, COLUMN_MODEL_5_OUTER);
//    public static final ColumnSquareBlock COLUMN_5_BLOCK = (ColumnSquareBlock) new ColumnSquareBlock(COLUMN_5_DISPATCH, BaseMaterial.FLEXSTONE, "column_square_5");
//    public static final NiceItemBlock COLUMN_5_ITEM = new NiceItemBlock(COLUMN_5_BLOCK);

    private final static ModelHolder HEIGHT_STONE_MODEL = new ModelHolder(new HeightModelFactory(ModelStateComponents.COLORS_BLOCK,
            ModelStateComponents.TEXTURE_4, ModelStateComponents.ROTATION, ModelStateComponents.SPECIES_16),
            TextureProviders.TEX_BLOCK_COLORED_STONE.getTextureState(true, LightingMode.SHADED, BlockRenderLayer.SOLID));
    private static final ModelDispatcher HEIGHT_STONE_DISPATCH = new ModelDispatcher(HEIGHT_STONE_MODEL);
    public static final NiceBlockPlus HEIGHT_STONE_BLOCK = new HeightBlock(HEIGHT_STONE_DISPATCH, BaseMaterial.FLEXSTONE, "stacked");
    public static final NiceItemBlock HEIGHT_STONE_ITEM = new NiceItemBlock(HEIGHT_STONE_BLOCK);

    //TODO: move all these to volcano package
    private final static ModelHolder HOT_FLOWING_LAVA_MODEL = new ModelHolder(new FlowModelFactory(true, ModelStateComponents.FLOW_JOIN,
            ModelStateComponents.FLOW_TEX, ModelStateComponents.TEXTURE_1, ModelStateComponents.ROTATION_NONE, ModelStateComponents.COLORS_WHITE),
            TextureProviders.TEX_BT_LAVA.getTextureState(false, LightingMode.FULLBRIGHT, BlockRenderLayer.SOLID));
    private static final ModelDispatcher HOT_FLOWING_LAVA_DISPATCH = new ModelDispatcher(HOT_FLOWING_LAVA_MODEL);    
    public static final VolcanicLavaBlock HOT_FLOWING_LAVA_HEIGHT_BLOCK = 
             new VolcanicLavaBlock(HOT_FLOWING_LAVA_DISPATCH, BaseMaterial.VOLCANIC_LAVA, "flow", false);
    public static final NiceItemBlock HOT_FLOWING_LAVA_HEIGHT_ITEM = new NiceItemBlock(HOT_FLOWING_LAVA_HEIGHT_BLOCK);
    public static final VolcanicLavaBlock HOT_FLOWING_LAVA_FILLER_BLOCK = 
             new VolcanicLavaBlock(HOT_FLOWING_LAVA_DISPATCH, BaseMaterial.VOLCANIC_LAVA, "fill", true);
    public static final NiceItemBlock HOT_FLOWING_LAVA_FILLER_ITEM = new NiceItemBlock(HOT_FLOWING_LAVA_FILLER_BLOCK);
    
    private final static ModelHolder COOL_FLOWING_BASALT_MODEL = new ModelHolder(new FlowModelFactory(true, ModelStateComponents.FLOW_JOIN,
            ModelStateComponents.FLOW_TEX, ModelStateComponents.TEXTURE_1, ModelStateComponents.ROTATION_NONE, ModelStateComponents.COLORS_BASALT),
            TextureProviders.TEX_BT_BASALT_COOL.getTextureState(false, LightingMode.SHADED, BlockRenderLayer.SOLID));
    
    private final static ModelHolder HOT_FLOWING_BASALT_0_MODEL = new ModelHolder(new FlowModelFactory(false, ModelStateComponents.FLOW_JOIN,
            ModelStateComponents.FLOW_TEX, ModelStateComponents.TEXTURE_1, ModelStateComponents.ROTATION_NONE, ModelStateComponents.COLORS_BASALT),
            TextureProviders.TEX_BT_BASALT_COOLING.getTextureState(false, LightingMode.SHADED, BlockRenderLayer.TRANSLUCENT));

    private final static ModelHolder HOT_FLOWING_BASALT_1_MODEL = new ModelHolder(new FlowModelFactory(false, ModelStateComponents.FLOW_JOIN,
            ModelStateComponents.FLOW_TEX, ModelStateComponents.TEXTURE_1, ModelStateComponents.ROTATION_NONE, ModelStateComponents.COLORS_BASALT),
            TextureProviders.TEX_BT_BASALT_WARM.getTextureState(false, LightingMode.SHADED, BlockRenderLayer.TRANSLUCENT));

    private final static ModelHolder HOT_FLOWING_BASALT_2_MODEL = new ModelHolder(new FlowModelFactory(false, ModelStateComponents.FLOW_JOIN,
            ModelStateComponents.FLOW_TEX, ModelStateComponents.TEXTURE_1, ModelStateComponents.ROTATION_NONE, ModelStateComponents.COLORS_BASALT),
            TextureProviders.TEX_BT_BASALT_HOT.getTextureState(false, LightingMode.SHADED, BlockRenderLayer.TRANSLUCENT));

    private final static ModelHolder HOT_FLOWING_BASALT_3_MODEL = new ModelHolder(new FlowModelFactory(false, ModelStateComponents.FLOW_JOIN,
            ModelStateComponents.FLOW_TEX, ModelStateComponents.TEXTURE_1, ModelStateComponents.ROTATION_NONE, ModelStateComponents.COLORS_BASALT),
            TextureProviders.TEX_BT_BASALT_VERY_HOT.getTextureState(false, LightingMode.SHADED, BlockRenderLayer.TRANSLUCENT));

    // COOL BASALT
    private static final ModelDispatcher COOL_FLOWING_BASALT_DISPATCH = new ModelDispatcher(COOL_FLOWING_BASALT_MODEL); 
    // DYNAMIC VERSION
    public static final NiceBlock COOL_FLOWING_BASALT_HEIGHT_BLOCK = new FlowDynamicBlock(COOL_FLOWING_BASALT_DISPATCH, BaseMaterial.BASALT, "flow", false)
            .setDropItem(ModItems.basalt_rubble);
    public static final NiceItemBlock COOL_FLOWING_BASALT_HEIGHT_ITEM = new NiceItemBlock(COOL_FLOWING_BASALT_HEIGHT_BLOCK);
    public static final NiceBlock COOL_FLOWING_BASALT_FILLER_BLOCK = new FlowDynamicBlock(COOL_FLOWING_BASALT_DISPATCH, BaseMaterial.BASALT, "fill", true)
            .setDropItem(ModItems.basalt_rubble);
    public static final NiceItemBlock COOL_FLOWING_BASALT_FILLER_ITEM = new NiceItemBlock(COOL_FLOWING_BASALT_FILLER_BLOCK);
    // STATIC VERSION
    public static final FlowStaticBlock COOL_STATIC_BASALT_HEIGHT_BLOCK = (FlowStaticBlock) new FlowStaticBlock(COOL_FLOWING_BASALT_DISPATCH, BaseMaterial.BASALT, "static_flow", false)
            .setDropItem(ModItems.basalt_rubble);
    public static final NiceItemBlock COOL_STATIC_BASALT_HEIGHT_ITEM = new NiceItemBlock(COOL_STATIC_BASALT_HEIGHT_BLOCK);
    public static final FlowStaticBlock COOL_STATIC_BASALT_FILLER_BLOCK = (FlowStaticBlock) new FlowStaticBlock(COOL_FLOWING_BASALT_DISPATCH, BaseMaterial.BASALT, "static_fill", true)
            .setDropItem(ModItems.basalt_rubble);
    public static final NiceItemBlock COOL_STATIC_BASALT_FILLER_ITEM = new NiceItemBlock(COOL_STATIC_BASALT_FILLER_BLOCK);
 
    
    // COOLING BASALT
    private static final ModelDispatcher HOT_FLOWING_BASALT_0_DISPATCH = new ModelDispatcher(HOT_FLOWING_LAVA_MODEL, HOT_FLOWING_BASALT_0_MODEL);    
    public static final NiceBlock HOT_FLOWING_BASALT_0_HEIGHT_BLOCK = new CoolingBlock(HOT_FLOWING_BASALT_0_DISPATCH, BaseMaterial.BASALT, "cooling_flow", false)
            .setCoolingBlockInfo((FlowDynamicBlock) COOL_FLOWING_BASALT_HEIGHT_BLOCK, 1).setDropItem(ModItems.basalt_rubble).setAllowSilkHarvest(false);
    public static final NiceItemBlock HOT_FLOWING_BASALT_0_HEIGHT_ITEM = new NiceItemBlock(HOT_FLOWING_BASALT_0_HEIGHT_BLOCK);
    public static final NiceBlock HOT_FLOWING_BASALT_0_FILLER_BLOCK = new CoolingBlock(HOT_FLOWING_BASALT_0_DISPATCH, BaseMaterial.BASALT, "cooling_fill", true)
            .setCoolingBlockInfo((FlowDynamicBlock) COOL_FLOWING_BASALT_FILLER_BLOCK, 1).setDropItem(ModItems.basalt_rubble).setAllowSilkHarvest(false);
    public static final NiceItemBlock HOT_FLOWING_BASALT_0_FILLER_ITEM = new NiceItemBlock(HOT_FLOWING_BASALT_0_FILLER_BLOCK);

    // WARM BASALT
    private static final ModelDispatcher HOT_FLOWING_BASALT_1_DISPATCH = new ModelDispatcher(HOT_FLOWING_LAVA_MODEL, HOT_FLOWING_BASALT_1_MODEL);
    public static final NiceBlock HOT_FLOWING_BASALT_1_HEIGHT_BLOCK = new CoolingBlock(HOT_FLOWING_BASALT_1_DISPATCH, BaseMaterial.BASALT, "warm_flow", false)
            .setCoolingBlockInfo((FlowDynamicBlock) HOT_FLOWING_BASALT_0_HEIGHT_BLOCK, 2).setDropItem(ModItems.basalt_rubble).setAllowSilkHarvest(false);
    public static final NiceItemBlock HOT_FLOWING_BASALT_1_HEIGHT_ITEM = new NiceItemBlock(HOT_FLOWING_BASALT_1_HEIGHT_BLOCK);
    public static final NiceBlock HOT_FLOWING_BASALT_1_FILLER_BLOCK = new CoolingBlock(HOT_FLOWING_BASALT_1_DISPATCH, BaseMaterial.BASALT, "warm_fill", true)
            .setCoolingBlockInfo((FlowDynamicBlock) HOT_FLOWING_BASALT_0_FILLER_BLOCK, 2).setDropItem(ModItems.basalt_rubble).setAllowSilkHarvest(false);
    public static final NiceItemBlock HOT_FLOWING_BASALT_1_FILLER_ITEM = new NiceItemBlock(HOT_FLOWING_BASALT_1_FILLER_BLOCK);
    
    // HOT BASALT
    private static final ModelDispatcher HOT_FLOWING_BASALT_2_DISPATCH = new ModelDispatcher(HOT_FLOWING_LAVA_MODEL, HOT_FLOWING_BASALT_2_MODEL);
    public static final NiceBlock HOT_FLOWING_BASALT_2_HEIGHT_BLOCK = new CoolingBlock(HOT_FLOWING_BASALT_2_DISPATCH, BaseMaterial.BASALT, "hot_flow", false)
            .setCoolingBlockInfo((FlowDynamicBlock) HOT_FLOWING_BASALT_1_HEIGHT_BLOCK, 3).setDropItem(ModItems.basalt_rubble).setAllowSilkHarvest(false);
    public static final NiceItemBlock HOT_FLOWING_BASALT_2_HEIGHT_ITEM = new NiceItemBlock(HOT_FLOWING_BASALT_2_HEIGHT_BLOCK);
    public static final NiceBlock HOT_FLOWING_BASALT_2_FILLER_BLOCK = new CoolingBlock(HOT_FLOWING_BASALT_2_DISPATCH, BaseMaterial.BASALT, "hot_fill", true)
            .setCoolingBlockInfo((FlowDynamicBlock) HOT_FLOWING_BASALT_1_FILLER_BLOCK, 3).setDropItem(ModItems.basalt_rubble).setAllowSilkHarvest(false);
    public static final NiceItemBlock HOT_FLOWING_BASALT_2_FILLER_ITEM = new NiceItemBlock(HOT_FLOWING_BASALT_2_FILLER_BLOCK);

    // VERY HOT BASALT
    private static final ModelDispatcher HOT_FLOWING_BASALT_3_DISPATCH = new ModelDispatcher(HOT_FLOWING_LAVA_MODEL, HOT_FLOWING_BASALT_3_MODEL);  
    public static final NiceBlock HOT_FLOWING_BASALT_3_HEIGHT_BLOCK = new CoolingBlock(HOT_FLOWING_BASALT_3_DISPATCH, BaseMaterial.BASALT, "very_hot_flow", false)
            .setCoolingBlockInfo((FlowDynamicBlock) HOT_FLOWING_BASALT_2_HEIGHT_BLOCK, 4).setDropItem(ModItems.basalt_rubble).setAllowSilkHarvest(false);
    public static final NiceItemBlock HOT_FLOWING_BASALT_3_HEIGHT_ITEM = new NiceItemBlock(HOT_FLOWING_BASALT_3_HEIGHT_BLOCK);
    public static final NiceBlock HOT_FLOWING_BASALT_3_FILLER_BLOCK = new CoolingBlock(HOT_FLOWING_BASALT_3_DISPATCH, BaseMaterial.BASALT, "very_hot_fill", true)
            .setCoolingBlockInfo((FlowDynamicBlock) HOT_FLOWING_BASALT_2_FILLER_BLOCK, 4).setDropItem(ModItems.basalt_rubble).setAllowSilkHarvest(false);
    public static final NiceItemBlock HOT_FLOWING_BASALT_3_FILLER_ITEM = new NiceItemBlock(HOT_FLOWING_BASALT_3_FILLER_BLOCK);
  
    
    // COOLING LAVA
    public static final NiceBlock HOT_STATIC_LAVA_HEIGHT_BLOCK =  new CoolingBlock(HOT_FLOWING_LAVA_DISPATCH, BaseMaterial.VOLCANIC_LAVA, "cooling_lava_flow", false)
            .setCoolingBlockInfo((FlowDynamicBlock) HOT_FLOWING_BASALT_3_HEIGHT_BLOCK, 4).setDropItem(ModItems.basalt_rubble).setAllowSilkHarvest(false);
   public static final NiceItemBlock HOT_STATIC_LAVA_HEIGHT_ITEM = new NiceItemBlock(HOT_STATIC_LAVA_HEIGHT_BLOCK);
   public static final NiceBlock HOT_STATIC_LAVA_FILLER_BLOCK = new CoolingBlock(HOT_FLOWING_LAVA_DISPATCH, BaseMaterial.VOLCANIC_LAVA, "cooling_lava_fill", true)
           .setCoolingBlockInfo((FlowDynamicBlock) HOT_FLOWING_BASALT_3_FILLER_BLOCK, 4).setDropItem(ModItems.basalt_rubble).setAllowSilkHarvest(false);
   public static final NiceItemBlock HOT_STATIC_LAVA_FILLER_ITEM = new NiceItemBlock(HOT_STATIC_LAVA_FILLER_BLOCK);
   
    
    private final static ModelHolder BASALT_COBBLE_MODEL = new ModelHolder(new ColorModelFactory(ModelStateComponents.COLORS_BASALT,
            ModelStateComponents.TEXTURE_4, ModelStateComponents.ROTATION),
            TextureProviders.TEX_BLOCK_COBBLE.getTextureState(true, LightingMode.SHADED, BlockRenderLayer.SOLID));
    private static final ModelDispatcher BASALT_COBBLE_DISPATCH = new ModelDispatcher(BASALT_COBBLE_MODEL);
    public static final NiceBlock BASALT_COBBLE_BLOCK = new NiceBlock(BASALT_COBBLE_DISPATCH, BaseMaterial.BASALT, "basalt_cobble");
    public static final NiceItemBlock BASALT_COBBLE_ITEM = new NiceItemBlock(BASALT_COBBLE_BLOCK);

    private final static ModelHolder COLORED_COBBLE_MODEL = new ModelHolder(new ColorModelFactory(ModelStateComponents.COLORS_BLOCK,
            ModelStateComponents.TEXTURE_4, ModelStateComponents.ROTATION),
            TextureProviders.TEX_BLOCK_COBBLE.getTextureState(true, LightingMode.SHADED, BlockRenderLayer.SOLID));
    private static final ModelDispatcher COLORED_COBBLE_DISPATCH = new ModelDispatcher(COLORED_COBBLE_MODEL);
    public static final NiceBlock COBBLE_FLEXSTONE_BLOCK = new NiceBlockPlus(COLORED_COBBLE_DISPATCH, BaseMaterial.FLEXSTONE, "cobble");
    public static final NiceItemBlock COBBLE_FLEXSTONE_ITEM = new NiceItemBlock(COBBLE_FLEXSTONE_BLOCK);

    public static final NiceBlock COBBLE_DURASTONE_BLOCK = new NiceBlockPlus(COLORED_COBBLE_DISPATCH, BaseMaterial.DURASTONE, "cobble");
    public static final NiceItemBlock COBBLE_DURASTONE_ITEM = new NiceItemBlock(COBBLE_DURASTONE_BLOCK);

    
    private final static ModelHolder COOL_SQUARE_BASALT_MODEL 
        = new ModelHolder(new BigTexModelFactory(ModelStateComponents.COLORS_BASALT,
                ModelStateComponents.BIG_TEX_IGNORE_META, ModelStateComponents.TEXTURE_1),
                TextureProviders.TEX_BT_BASALT_CUT.getTextureState(false, LightingMode.SHADED, BlockRenderLayer.SOLID));
    private static final ModelDispatcher COOL_SQUARE_BASALT_DISPATCH = new ModelDispatcher(COOL_SQUARE_BASALT_MODEL);
    public static final NiceBlock COOL_SQUARE_BASALT_BLOCK = new FlowSimpleBlock(COOL_SQUARE_BASALT_DISPATCH, BaseMaterial.BASALT, "cool")
            .setDropItem(BASALT_COBBLE_ITEM);
    public static final NiceItemBlock COOL_SQUARE_BASALT_ITEM = new NiceItemBlock(COOL_SQUARE_BASALT_BLOCK);
    
    private final static ModelHolder COLORED_BASALT_MODEL 
        = new ModelHolder(new BigTexModelFactory(ModelStateComponents.COLORS_BLOCK,
                ModelStateComponents.BIG_TEX_IGNORE_META, ModelStateComponents.TEXTURE_1),
                TextureProviders.TEX_BT_BASALT_CUT.getTextureState(false, LightingMode.SHADED, BlockRenderLayer.SOLID));
    private static final ModelDispatcher COLORED_BASALT_DISPATCH = new ModelDispatcher(COLORED_BASALT_MODEL);
    
    public static final NiceBlock CUT_ROCK_FLEXSTONE_BLOCK = new NiceBlockPlus(COLORED_BASALT_DISPATCH, BaseMaterial.FLEXSTONE, "cut_rock");
    public static final NiceItemBlock CUT_ROCK_FLEXSTONE_ITEM = new NiceItemBlock(CUT_ROCK_FLEXSTONE_BLOCK);
    
    public static final NiceBlock CUT_ROCK_DURASTONE_BLOCK = new NiceBlockPlus(COLORED_BASALT_DISPATCH, BaseMaterial.DURASTONE, "cut_rock");
    public static final NiceItemBlock CUT_ROCK_DURASTONE_ITEM = new NiceItemBlock(CUT_ROCK_DURASTONE_BLOCK);

    private final static ModelHolder CSG_TEST_MODEL = new ModelHolder(new CSGModelFactory(ModelStateComponents.COLORS_BLOCK,
            ModelStateComponents.TEXTURE_4, ModelStateComponents.ROTATION),
            TextureProviders.TEX_BLOCK_COLORED_STONE.getTextureState(true, LightingMode.SHADED, BlockRenderLayer.SOLID));
    private static final ModelDispatcher CSG_TEST_DISPATCH = new ModelDispatcher(CSG_TEST_MODEL);
    public static final CSGBlock CSG_TEST_BLOCK = new CSGBlock(CSG_TEST_DISPATCH, BaseMaterial.FLEXSTONE, "csg_test");
    public static final NiceItemBlock CSG_TEST_ITEM = new NiceItemBlock(CSG_TEST_BLOCK);

    static
    {
        ((FlowDynamicBlock)COOL_FLOWING_BASALT_HEIGHT_BLOCK).setStaticVersion(COOL_STATIC_BASALT_HEIGHT_BLOCK);
        ((FlowDynamicBlock)COOL_FLOWING_BASALT_FILLER_BLOCK).setStaticVersion(COOL_STATIC_BASALT_FILLER_BLOCK);
    
        ((FlowDynamicBlock)HOT_FLOWING_BASALT_0_HEIGHT_BLOCK).setStaticVersion(null);
        ((FlowDynamicBlock)HOT_FLOWING_BASALT_0_FILLER_BLOCK).setStaticVersion(null);   
        
        ((FlowDynamicBlock)HOT_FLOWING_BASALT_1_HEIGHT_BLOCK).setStaticVersion(null);
        ((FlowDynamicBlock)HOT_FLOWING_BASALT_1_FILLER_BLOCK).setStaticVersion(null);
        
        ((FlowDynamicBlock)HOT_FLOWING_BASALT_2_HEIGHT_BLOCK).setStaticVersion(null);
        ((FlowDynamicBlock)HOT_FLOWING_BASALT_2_FILLER_BLOCK).setStaticVersion(null);   
        
        ((FlowDynamicBlock)HOT_FLOWING_BASALT_3_HEIGHT_BLOCK).setStaticVersion(null);
        ((FlowDynamicBlock)HOT_FLOWING_BASALT_3_FILLER_BLOCK).setStaticVersion(null);   
        
        ((FlowDynamicBlock)HOT_FLOWING_LAVA_HEIGHT_BLOCK).setStaticVersion(null);
        ((FlowDynamicBlock)HOT_FLOWING_LAVA_FILLER_BLOCK).setStaticVersion(null);   
        
        ((FlowStaticBlock)COOL_STATIC_BASALT_HEIGHT_BLOCK).setDynamicVersion((FlowDynamicBlock)COOL_FLOWING_BASALT_HEIGHT_BLOCK);
        ((FlowStaticBlock)COOL_STATIC_BASALT_FILLER_BLOCK).setDynamicVersion((FlowDynamicBlock)COOL_FLOWING_BASALT_FILLER_BLOCK);   
    
    }
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
     * Register all textures that will be needed for associated models. 
     * Happens before model bake.
     */
    @SubscribeEvent
    public void stitcherEventPre(TextureStitchEvent.Pre event)
    {
        ArrayList<String> textureList = new ArrayList<String>();
        
        for (TextureProvider t : TextureProviders.ALL_TEXTURE_PROVIDERS)
        {
            t.addTexturesForPrestich(textureList);
        }
        
        for(String s : textureList)
        {
            event.getMap().registerSprite(new ResourceLocation(s));
        }
    }

    //TODO: this sucks
    public static NiceBlock getFillerBlock(Block blockIn)
    {
        if (blockIn == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK
                || blockIn == NiceBlockRegistrar.HOT_STATIC_LAVA_HEIGHT_BLOCK)
            return NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK;
        
        else if(blockIn == NiceBlockRegistrar.HOT_FLOWING_BASALT_3_HEIGHT_BLOCK)
            return NiceBlockRegistrar.HOT_FLOWING_BASALT_3_FILLER_BLOCK;

        else if(blockIn == NiceBlockRegistrar.HOT_FLOWING_BASALT_2_HEIGHT_BLOCK)
            return NiceBlockRegistrar.HOT_FLOWING_BASALT_2_FILLER_BLOCK;

        else if(blockIn == NiceBlockRegistrar.HOT_FLOWING_BASALT_1_HEIGHT_BLOCK)
            return NiceBlockRegistrar.HOT_FLOWING_BASALT_1_FILLER_BLOCK;

        else if(blockIn == NiceBlockRegistrar.HOT_FLOWING_BASALT_0_HEIGHT_BLOCK)
            return NiceBlockRegistrar.HOT_FLOWING_BASALT_0_FILLER_BLOCK;

        else 
            return NiceBlockRegistrar.COOL_FLOWING_BASALT_FILLER_BLOCK;
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
