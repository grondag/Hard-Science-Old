package grondag.adversity.niceblock;

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
            ModelStateComponents.BIG_TEX_META_VARIED);
    private static final ModelDispatcher2 BIGTEX_DISPATCH = new ModelDispatcher2(BIGTEX_MODEL);
    public static final NiceBlockPlus2 BIGTEX_BLOCK = new NiceBlockPlus2(BIGTEX_DISPATCH, BaseMaterial.FLEXSTONE, "bigtex");
    public static final NiceItemBlock2 BIGTEX_ITEM = new NiceItemBlock2(BIGTEX_BLOCK);

    private final static ModelFactory2.ModelInputs BORDER_INPUTS = new ModelFactory2.ModelInputs("bordertest", true, BlockRenderLayer.TRANSLUCENT);
    private final static BorderModelFactory2 BORDER_MODEL = new BorderModelFactory2(BORDER_INPUTS, ModelStateComponents.COLORS_BLOCK,
            ModelStateComponents.CORNER_JOIN, ModelStateComponents.TEXTURE_1);
//    private static final ModelDispatcher2 BORDER_DISPATCH = new ModelDispatcher2(BORDER_MODEL);
//    public static final NiceBlockPlus2 BORDER_BLOCK = new NiceBlockPlus2(BORDER_DISPATCH, BaseMaterial.FLEXSTONE, "border");
//    public static final NiceItemBlock2 BORDER_ITEM = new NiceItemBlock2(BORDER_BLOCK);

    private static final ModelDispatcher2 BORDER_BIGTEX_DISPATCH = new ModelDispatcher2(BIGTEX_MODEL, BORDER_MODEL);
    public static final NiceBlockPlus2 BORDER_BIGTEX_BLOCK = new NiceBlockPlus2(BORDER_BIGTEX_DISPATCH, BaseMaterial.FLEXSTONE, "border");
    public static final NiceItemBlock2 BORDER_BIG_TEXITEM = new NiceItemBlock2(BORDER_BIGTEX_BLOCK);

    
    // public static final ModelDispatcherLayered MODEL_BORDER_TEST = new ModelDispatcherLayered(BlockColorMapProvider.INSTANCE, "colored_stone_0_0",
    // new BigTexController("bigtex_rock_test", BlockRenderLayer.SOLID, true, true),
    // new BorderController("bordertest", 1, BlockRenderLayer.TRANSLUCENT, true));
    // public static final NiceBlockPlus BLOCK_BORDERED = new NiceBlockPlus(new BigBlockHelper(MODEL_BORDER_TEST, (3 << 16) | (3 << 8) | 3), BaseMaterial.FLEXSTONE, "bordered");
    //
    // public static final ModelDispatcherLayered MODEL_BIGBRICK_TEST = new ModelDispatcherLayered(BlockColorMapProvider.INSTANCE, "colored_stone_0_0",
    // new BigTexController("bigtex_rock_test", BlockRenderLayer.SOLID, true, true),
    // new MasonryController("masonrytest", 1, BlockRenderLayer.CUTOUT_MIPPED, true));
    // public static final NiceBlockPlus BLOCK_BIGBRICK = new NiceBlockPlus(new BigBlockHelper(MODEL_BIGBRICK_TEST, (2 << 16) | (1 << 8) | 1), BaseMaterial.FLEXSTONE, "bigbrick");
    //
    // public static final ModelDispatcherLayered MODEL_COLUMN_SQUARE_2 = new ModelDispatcherLayered(BlockColorMapProvider.INSTANCE, "colored_stone_0_0",
    // new ColumnSquareController("colored_stone", 1, ColumnSquareController.ModelType.LAMP_BASE, true, 2, true),
    // new ColumnSquareController("colored_stone", 1, ColumnSquareController.ModelType.LAMP_OVERLAY, true, 2, true));
    // public static final Block BLOCK_COLUMN_SQUARE_2 = new ColumnSquareBlock(new AxisOrientedHelper(MODEL_COLUMN_SQUARE_2), BaseMaterial.FLEXSTONE, "column_square_2")
    // .setLightLevel(3F/15F);//.setLightOpacity(0);
    //
    // public static final ModelDispatcherBasic MODEL_COLUMN_SQUARE_3 = new ModelDispatcherBasic(BlockColorMapProvider.INSTANCE, "colored_stone_0_0",
    // new ColumnSquareController("colored_stone", 1, ColumnSquareController.ModelType.NORMAL, true, 3, true));
    // public static final NiceBlockPlus BLOCK_COLUMN_SQUARE_3 = new ColumnSquareBlock(new AxisOrientedHelper(MODEL_COLUMN_SQUARE_3), BaseMaterial.FLEXSTONE, "column_square_3");
    //
    // public static final ModelDispatcherLayered MODEL_COLUMN_SQUARE_4 = new ModelDispatcherLayered(BlockColorMapProvider.INSTANCE, "colored_stone_0_0",
    // new ColumnSquareController("colored_stone", 1, ColumnSquareController.ModelType.LAMP_BASE, true, 4, true),
    // new ColumnSquareController("colored_stone", 1, ColumnSquareController.ModelType.LAMP_OVERLAY, true, 4, true));
    // public static final Block BLOCK_COLUMN_SQUARE_4 = new ColumnSquareBlock(new AxisOrientedHelper(MODEL_COLUMN_SQUARE_4), BaseMaterial.FLEXSTONE, "column_square_4")
    // .setLightLevel(3F/15F);//.setLightOpacity(0);
    //
    // public static final ModelDispatcherBasic MODEL_COLUMN_SQUARE_5 = new ModelDispatcherBasic(BlockColorMapProvider.INSTANCE, "colored_stone_0_0",
    // new ColumnSquareController("colored_stone", 1, ColumnSquareController.ModelType.NORMAL, true, 5, false));
    // public static final NiceBlockPlus BLOCK_COLUMN_SQUARE_5 = new ColumnSquareBlock(new AxisOrientedHelper(MODEL_COLUMN_SQUARE_5), BaseMaterial.FLEXSTONE, "column_square_5");

    // public static final ModelDispatcherLayered MODEL_HOT_BASALT = new ModelDispatcherLayered(new NoColorMapProvider(4), "cool_basalt_0_0",
    // new ColorController("cool_basalt", 4, BlockRenderLayer.SOLID, true, true),
    // new HotBasaltController());
    // public static final NiceBlock BLOCK_HOT_BASALT = (NiceBlock) new HotBasaltBlock(new HotBasaltHelper(MODEL_HOT_BASALT), BaseMaterial.FLEXSTONE, "hot_basalt");
    //
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
