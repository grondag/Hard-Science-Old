package grondag.adversity.niceblock;

import grondag.adversity.niceblock.base.BigBlock2;
import grondag.adversity.niceblock.base.ModelDispatcher2;
import grondag.adversity.niceblock.base.ModelFactory2;
import grondag.adversity.niceblock.base.NiceBlock2;
import grondag.adversity.niceblock.base.NiceBlockPlus2;
import grondag.adversity.niceblock.base.NiceItemBlock2;
import grondag.adversity.niceblock.base.NiceTileEntity2;
import grondag.adversity.niceblock.modelstate.ModelStateComponents;
import grondag.adversity.niceblock.support.BaseMaterial;
import grondag.adversity.niceblock.support.NiceBlockHighlighter;
import grondag.adversity.niceblock.support.NiceBlockStateMapper2;
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
public class NiceBlockRegistrar2
{

    private static final NiceBlockRegistrar2 instance = new NiceBlockRegistrar2();

    /**
     * NiceBlocks add themselves here so that we can easily iterate them during registration
     */
    public static LinkedList<NiceBlock2> allBlocks = new LinkedList<NiceBlock2>();

    /**
     * NiceItemBlocks add themselves here so that we can easily iterate them during registration
     */
    public static LinkedList<NiceItemBlock2> allItems = new LinkedList<NiceItemBlock2>();

    /**
     * Model dispatchers add themselves here for handling during model bake and texture stitch
     */
    public static LinkedList<ModelDispatcher2> allDispatchers = new LinkedList<ModelDispatcher2>();

    // DECLARE MODEL DISPATCH & BLOCK INSTANCES
    private final static ModelFactory2.ModelInputs RAW_FLEXSTONE_INPUTS = new ModelFactory2.ModelInputs("raw_flexstone", true, BlockRenderLayer.SOLID);
    private final static ColorModelFactory2 RAW_FLEXSTONE_MODEL = new ColorModelFactory2(RAW_FLEXSTONE_INPUTS, ModelStateComponents.COLORS_RAW_FLEXSTONE,
            ModelStateComponents.TEXTURE_4, ModelStateComponents.ROTATION);
    private static final ModelDispatcher2 RAW_FLEXSTONE_DISPATCH = new ModelDispatcher2(RAW_FLEXSTONE_MODEL);
    public static final NiceBlock2 RAW_FLEXSTONE_BLOCK = new NiceBlock2(RAW_FLEXSTONE_DISPATCH, BaseMaterial.FLEXSTONE, "raw");
    public static final NiceItemBlock2 RAW_FLEXSTONE_ITEM = new NiceItemBlock2(RAW_FLEXSTONE_BLOCK);
    
    private final static ModelFactory2.ModelInputs RAW_DURASTONE_INPUTS = new ModelFactory2.ModelInputs("raw_durastone", true, BlockRenderLayer.SOLID);
    private final static ColorModelFactory2 RAW_DURASTONE_MODEL = new ColorModelFactory2(RAW_DURASTONE_INPUTS, ModelStateComponents.COLORS_RAW_DURASTONE,
            ModelStateComponents.TEXTURE_4, ModelStateComponents.ROTATION);
    private static final ModelDispatcher2 RAW_DURASTONE_DISPATCH = new ModelDispatcher2(RAW_DURASTONE_MODEL);
    public static final NiceBlock2 RAW_DURASTONE_BLOCK = new NiceBlock2(RAW_DURASTONE_DISPATCH, BaseMaterial.DURASTONE, "raw");
    public static final NiceItemBlock2 RAW_DURASTONE_ITEM = new NiceItemBlock2(RAW_DURASTONE_BLOCK);

    private final static ModelFactory2.ModelInputs COLORED_STONE_INPUTS = new ModelFactory2.ModelInputs("colored_stone", true, BlockRenderLayer.SOLID);
    private final static ColorModelFactory2 COLORED_STONE_MODEL = new ColorModelFactory2(COLORED_STONE_INPUTS, ModelStateComponents.COLORS_BLOCK,
            ModelStateComponents.TEXTURE_4, ModelStateComponents.ROTATION);
    private static final ModelDispatcher2 COLORED_STONE_DISPATCH = new ModelDispatcher2(COLORED_STONE_MODEL);
    public static final NiceBlockPlus2 COLORED_STONE_BLOCK = new NiceBlockPlus2(COLORED_STONE_DISPATCH, BaseMaterial.FLEXSTONE, "colored");
    public static final NiceItemBlock2 COLORED_STONE_ITEM = new NiceItemBlock2(COLORED_STONE_BLOCK);
   
    private final static ModelFactory2.ModelInputs BIGTEX_INPUTS = new ModelFactory2.ModelInputs("bigtex_rock_test", true, BlockRenderLayer.SOLID);
    private final static BigTexModelFactory2 BIGTEX_MODEL = new BigTexModelFactory2(BIGTEX_INPUTS, ModelStateComponents.COLORS_BLOCK,
            ModelStateComponents.BIG_TEX_META_VARIED, ModelStateComponents.TEXTURE_1);
    private static final ModelDispatcher2 BIGTEX_DISPATCH = new ModelDispatcher2(BIGTEX_MODEL);
    public static final NiceBlockPlus2 BIGTEX_BLOCK = new NiceBlockPlus2(BIGTEX_DISPATCH, BaseMaterial.FLEXSTONE, "bigtex");
    public static final NiceItemBlock2 BIGTEX_ITEM = new NiceItemBlock2(BIGTEX_BLOCK);

    private final static ModelFactory2.ModelInputs BORDER_INPUTS = new ModelFactory2.ModelInputs("bordertest", true, BlockRenderLayer.TRANSLUCENT);
    private final static BorderModelFactory2 BORDER_MODEL = new BorderModelFactory2(BORDER_INPUTS, ModelStateComponents.COLORS_BLOCK,
            ModelStateComponents.CORNER_JOIN, ModelStateComponents.TEXTURE_1);

    private static final ModelDispatcher2 BORDER_BIGTEX_DISPATCH = new ModelDispatcher2(BIGTEX_MODEL, BORDER_MODEL);
    public static final BigBlock2 BORDER_BIGTEX_BLOCK = new BigBlock2(BORDER_BIGTEX_DISPATCH, BaseMaterial.FLEXSTONE, "border", NicePlacement.PLACEMENT_3x3x3);
    public static final NiceItemBlock2 BORDER_BIGTEX_ITEM = new NiceItemBlock2(BORDER_BIGTEX_BLOCK);

    private final static ModelFactory2.ModelInputs MASONRY_INPUTS = new ModelFactory2.ModelInputs("masonrytest", true, BlockRenderLayer.CUTOUT_MIPPED);
    private final static MasonryModelFactory2 MASONRY_MODEL = new MasonryModelFactory2(MASONRY_INPUTS, ModelStateComponents.COLORS_BLOCK,
            ModelStateComponents.MASONRY_JOIN, ModelStateComponents.TEXTURE_1);

    private static final ModelDispatcher2 MASONRY_BIGTEX_DISPATCH = new ModelDispatcher2(BIGTEX_MODEL, MASONRY_MODEL);
    public static final BigBlock2 MASONRY_BIGTEX_BLOCK = new BigBlock2(MASONRY_BIGTEX_DISPATCH, BaseMaterial.FLEXSTONE, "bigbrick", NicePlacement.PLACEMENT_2x1x1);
    public static final NiceItemBlock2 MASONRY_BIGTEX_ITEM = new NiceItemBlock2(MASONRY_BIGTEX_BLOCK);

    private static final ModelFactory2.ModelInputs COLUMN_INPUTS_BASE 
        = new ModelFactory2.ModelInputs("colored_stone", true, BlockRenderLayer.SOLID);

    private static final ModelFactory2.ModelInputs COLUMN_INPUTS_LAMP 
    = new ModelFactory2.ModelInputs("colored_stone", false, BlockRenderLayer.SOLID);

    // need overlay on a separate layer to keep it out of AO lighting
    private static final ModelFactory2.ModelInputs COLUMN_INPUTS_OVERLAY 
    = new ModelFactory2.ModelInputs("colored_stone", true, BlockRenderLayer.CUTOUT_MIPPED);
    
    
    private static final ColumnSquareModelFactory2.ColumnSquareInputs COLUMN_INPUTS_2_INNER 
         = new ColumnSquareModelFactory2.ColumnSquareInputs(COLUMN_INPUTS_LAMP, 2, true, ColumnSquareModelFactory2.ModelType.LAMP_BASE);
    private static final ColumnSquareModelFactory2.ColumnSquareInputs COLUMN_INPUTS_2_OUTER 
    = new ColumnSquareModelFactory2.ColumnSquareInputs(COLUMN_INPUTS_OVERLAY, 2, true, ColumnSquareModelFactory2.ModelType.LAMP_OVERLAY);

    private final static ColumnSquareModelFactory2 COLUMN_MODEL_2_INNER 
        = new ColumnSquareModelFactory2(COLUMN_INPUTS_2_INNER, ModelStateComponents.COLORS_BLOCK,
            ModelStateComponents.CORNER_JOIN, ModelStateComponents.TEXTURE_1, ModelStateComponents.AXIS);
    private final static ColumnSquareModelFactory2 COLUMN_MODEL_2_OUTER 
        = new ColumnSquareModelFactory2(COLUMN_INPUTS_2_OUTER, ModelStateComponents.COLORS_BLOCK,
        ModelStateComponents.CORNER_JOIN, ModelStateComponents.TEXTURE_1, ModelStateComponents.AXIS);
    
    private static final ModelDispatcher2 COLUMN_2_DISPATCH = new ModelDispatcher2(COLUMN_MODEL_2_INNER , COLUMN_MODEL_2_OUTER);
    public static final ColumnSquareBlock2 COLUMN_2_BLOCK = (ColumnSquareBlock2) new ColumnSquareBlock2(COLUMN_2_DISPATCH, BaseMaterial.FLEXSTONE, "column_square_2");
    public static final NiceItemBlock2 COLUMN_2_ITEM = new NiceItemBlock2(COLUMN_2_BLOCK);

    
    
    private static final ColumnSquareModelFactory2.ColumnSquareInputs COLUMN_INPUTS_3 
        = new ColumnSquareModelFactory2.ColumnSquareInputs(COLUMN_INPUTS_BASE, 3, true, ColumnSquareModelFactory2.ModelType.NORMAL);
  
    private final static ColumnSquareModelFactory2 COLUMN_MODEL_3
        = new ColumnSquareModelFactory2(COLUMN_INPUTS_3, ModelStateComponents.COLORS_BLOCK,
        ModelStateComponents.CORNER_JOIN, ModelStateComponents.TEXTURE_1, ModelStateComponents.AXIS);
    
    private static final ModelDispatcher2 COLUMN_3_DISPATCH = new ModelDispatcher2(COLUMN_MODEL_3);
    public static final ColumnSquareBlock2 COLUMN_3_BLOCK = (ColumnSquareBlock2) new ColumnSquareBlock2(COLUMN_3_DISPATCH, BaseMaterial.FLEXSTONE, "column_square_3");
    public static final NiceItemBlock2 COLUMN_3_ITEM = new NiceItemBlock2(COLUMN_3_BLOCK);

    
    private static final ColumnSquareModelFactory2.ColumnSquareInputs COLUMN_INPUTS_4_INNER 
        = new ColumnSquareModelFactory2.ColumnSquareInputs(COLUMN_INPUTS_LAMP, 4, false, ColumnSquareModelFactory2.ModelType.LAMP_BASE);
    private static final ColumnSquareModelFactory2.ColumnSquareInputs COLUMN_INPUTS_4_OUTER 
        = new ColumnSquareModelFactory2.ColumnSquareInputs(COLUMN_INPUTS_OVERLAY, 4, false, ColumnSquareModelFactory2.ModelType.LAMP_OVERLAY);
    
    private final static ColumnSquareModelFactory2 COLUMN_MODEL_4_INNER 
       = new ColumnSquareModelFactory2(COLUMN_INPUTS_4_INNER, ModelStateComponents.COLORS_BLOCK,
           ModelStateComponents.CORNER_JOIN, ModelStateComponents.TEXTURE_1, ModelStateComponents.AXIS);
    private final static ColumnSquareModelFactory2 COLUMN_MODEL_4_OUTER 
       = new ColumnSquareModelFactory2(COLUMN_INPUTS_4_OUTER, ModelStateComponents.COLORS_BLOCK,
       ModelStateComponents.CORNER_JOIN, ModelStateComponents.TEXTURE_1, ModelStateComponents.AXIS);
    
    private static final ModelDispatcher2 COLUMN_4_DISPATCH = new ModelDispatcher2(COLUMN_MODEL_4_INNER, COLUMN_MODEL_4_OUTER);
    public static final ColumnSquareBlock2 COLUMN_4_BLOCK = (ColumnSquareBlock2) new ColumnSquareBlock2(COLUMN_4_DISPATCH, BaseMaterial.FLEXSTONE, "column_square_4");
    public static final NiceItemBlock2 COLUMN_4_ITEM = new NiceItemBlock2(COLUMN_4_BLOCK);

    
    private static final ColumnSquareModelFactory2.ColumnSquareInputs COLUMN_INPUTS_5_INNER 
        = new ColumnSquareModelFactory2.ColumnSquareInputs(COLUMN_INPUTS_LAMP, 5, false, ColumnSquareModelFactory2.ModelType.LAMP_BASE);
    private static final ColumnSquareModelFactory2.ColumnSquareInputs COLUMN_INPUTS_5_OUTER 
        = new ColumnSquareModelFactory2.ColumnSquareInputs(COLUMN_INPUTS_OVERLAY, 5, false, ColumnSquareModelFactory2.ModelType.LAMP_OVERLAY);
    
    private final static ColumnSquareModelFactory2 COLUMN_MODEL_5_INNER 
       = new ColumnSquareModelFactory2(COLUMN_INPUTS_5_INNER, ModelStateComponents.COLORS_BLOCK,
           ModelStateComponents.CORNER_JOIN, ModelStateComponents.TEXTURE_1, ModelStateComponents.AXIS);
    private final static ColumnSquareModelFactory2 COLUMN_MODEL_5_OUTER 
       = new ColumnSquareModelFactory2(COLUMN_INPUTS_5_OUTER, ModelStateComponents.COLORS_BLOCK,
       ModelStateComponents.CORNER_JOIN, ModelStateComponents.TEXTURE_1, ModelStateComponents.AXIS);
    
    private static final ModelDispatcher2 COLUMN_5_DISPATCH = new ModelDispatcher2(COLUMN_MODEL_5_INNER, COLUMN_MODEL_5_OUTER);
    public static final ColumnSquareBlock2 COLUMN_5_BLOCK = (ColumnSquareBlock2) new ColumnSquareBlock2(COLUMN_5_DISPATCH, BaseMaterial.FLEXSTONE, "column_square_5");
    public static final NiceItemBlock2 COLUMN_5_ITEM = new NiceItemBlock2(COLUMN_5_BLOCK);

    private final static ModelFactory2.ModelInputs COOL_SQUARE_BASALT_INPUTS = new ModelFactory2.ModelInputs("cool_basalt", true, BlockRenderLayer.SOLID);
    private final static ColorModelFactory2 COOL_SQUARE_BASALT_MODEL 
        = new ColorModelFactory2(COOL_SQUARE_BASALT_INPUTS, ModelStateComponents.COLORS_WHITE,
            ModelStateComponents.TEXTURE_4, ModelStateComponents.ROTATION);
    private static final ModelDispatcher2 COOL_SQUARE_BASALT_DISPATCH = new ModelDispatcher2(COOL_SQUARE_BASALT_MODEL);
    public static final NiceBlock2 COOL_SQUARE_BASALT_BLOCK = new NiceBlock2(COOL_SQUARE_BASALT_DISPATCH, BaseMaterial.FLEXSTONE, "cool_basalt");
    public static final NiceItemBlock2 COOL_SQUARE_BASALT_ITEM = new NiceItemBlock2(COOL_SQUARE_BASALT_BLOCK);

    
    private final static ModelFactory2.ModelInputs HOT_SQUARE_BASALT_INPUTS = new ModelFactory2.ModelInputs("hot_basalt", false, BlockRenderLayer.TRANSLUCENT);
    private final static SpeciesModelFactory HOT_SQUARE_BASALT_MODEL 
        = new SpeciesModelFactory(HOT_SQUARE_BASALT_INPUTS, ModelStateComponents.COLORS_WHITE,
            ModelStateComponents.TEXTURE_4, ModelStateComponents.ROTATION, ModelStateComponents.SPECIES_4);
    private static final ModelDispatcher2 HOT_SQUARE_BASALT_DISPATCH = new ModelDispatcher2( COOL_SQUARE_BASALT_MODEL, HOT_SQUARE_BASALT_MODEL);
    public static final NiceBlock2 HOT_SQUARE_BASALT_BLOCK = (NiceBlock2) new HotBasaltBlock(HOT_SQUARE_BASALT_DISPATCH, BaseMaterial.FLEXSTONE, "hot_basalt");
    public static final NiceItemBlock2 HOT_SQUARE_BASALT_ITEM = new NiceItemBlock2(HOT_SQUARE_BASALT_BLOCK);

    
    private final static ModelFactory2.ModelInputs HEIGHT_STONE_INPUTS = new ModelFactory2.ModelInputs("colored_stone", true, BlockRenderLayer.SOLID);
    private final static HeightModelFactory2 HEIGHT_STONE_MODEL = new HeightModelFactory2(HEIGHT_STONE_INPUTS, ModelStateComponents.COLORS_BLOCK,
            ModelStateComponents.TEXTURE_4, ModelStateComponents.ROTATION, ModelStateComponents.SPECIES_16);
    private static final ModelDispatcher2 HEIGHT_STONE_DISPATCH = new ModelDispatcher2(HEIGHT_STONE_MODEL);
    public static final NiceBlockPlus2 HEIGHT_STONE_BLOCK = new HeightBlock2(HEIGHT_STONE_DISPATCH, BaseMaterial.FLEXSTONE, "variable_height");
    public static final NiceItemBlock2 HEIGHT_STONE_ITEM = new NiceItemBlock2(HEIGHT_STONE_BLOCK);


    // flow height block - dynamic
    // filler for above
    // flow height block - static
    // filler for above
    // face culling for flow blocks
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
        for (NiceBlock2 block : allBlocks)
        {
            GameRegistry.register(block);
        }
        
        // REGISTER ALL ITEMS
        for (NiceItemBlock2 item : allItems)
        {
            item.registerSelf();
        }
        

        if (event.getSide() == Side.CLIENT)
        {
            for (NiceBlock2 block : allBlocks)
            {
                ModelLoader.setCustomStateMapper(block, NiceBlockStateMapper2.instance);

                for (ItemStack stack : block.getSubItems())
                {
                    ModelResourceLocation itemModelResourceLocation = new ModelResourceLocation(((NiceBlock2) block).getRegistryName() + "." + stack.getMetadata(), "inventory");
                    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), stack.getMetadata(), itemModelResourceLocation);
                }
            }
        }

        GameRegistry.registerTileEntity(NiceTileEntity2.class, "nicetileentity2");

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
        for (NiceBlock2 block : allBlocks)
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
        for (ModelDispatcher2 dispatcher : allDispatchers)
        {
            dispatcher.handleBakeEvent(event);
            // dispatcher.controller.getBakedModelFactory().handleBakeEvent(event);

            event.getModelRegistry().putObject(new ModelResourceLocation(dispatcher.getModelResourceString()), dispatcher);
        }

        for (ModelDispatcher2 dispatcher : allDispatchers)
        {
            dispatcher.handleBakeEvent(event);
            // dispatcher.controller.getBakedModelFactory().handleBakeEvent(event);

            event.getModelRegistry().putObject(new ModelResourceLocation(dispatcher.getModelResourceString()), dispatcher);
        }

        for (NiceBlock2 block : allBlocks)
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
        for (ModelDispatcher2 dispatcher : allDispatchers)
        {
            dispatcher.handleTexturePreStitch(event);
        }

        for (ModelDispatcher2 dispatcher : allDispatchers)
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
