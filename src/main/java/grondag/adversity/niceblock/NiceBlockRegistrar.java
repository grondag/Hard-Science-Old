package grondag.adversity.niceblock;

import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.niceblock.base.AxisOrientedHelper;
import grondag.adversity.niceblock.base.ModelDispatcher;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.base.NiceBlockPlus;
import grondag.adversity.niceblock.base.NiceTileEntity;
import grondag.adversity.niceblock.color.BlockColors;
import grondag.adversity.niceblock.color.FixedColors;
import grondag.adversity.niceblock.color.NiceHues;
import grondag.adversity.niceblock.color.NoColor;
import grondag.adversity.niceblock.color.HueSet.Tint;
import grondag.adversity.niceblock.color.NiceHues.Hue;
import grondag.adversity.niceblock.support.BaseMaterial;
import grondag.adversity.niceblock.support.NiceBlockHighlighter;
import grondag.adversity.niceblock.support.NiceBlockStateMapper;

import java.io.IOException;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
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
     * Model dispatchers add themselves here for handling during model bake and texture stitch
     */
    public static LinkedList<ModelDispatcher> allDispatchers = new LinkedList<ModelDispatcher>();

    // DECLARE MODEL DISPATCH & BLOCK INSTANCES
    public static final ModelDispatcher MODEL_FLEXSTONE_RAW = new ModelDispatcherBasic(new FixedColors(
            BlockColors.makeColorMap(Hue.YELLOW, Tint.WHITE)), "raw_flexstone_0_0",
            new ColorController("raw_flexstone", 4, BlockRenderLayer.SOLID, true, true));
    public static final NiceBlock BLOCK_FLEXSTONE_RAW = new NiceBlock(new ColorHelperMeta(MODEL_FLEXSTONE_RAW), BaseMaterial.FLEXSTONE, "raw");
    
    public static final ModelDispatcher MODEL_DURASTONE_RAW = new ModelDispatcherBasic(new FixedColors(
            BlockColors.makeColorMap(Hue.COBALT, Tint.WHITE)), "raw_durastone_0_0",
            new ColorController("raw_durastone", 4, BlockRenderLayer.SOLID, true, true));
    public static final NiceBlock BLOCK_DURASTONE_RAW = new NiceBlock(new ColorHelperMeta(MODEL_DURASTONE_RAW), BaseMaterial.DURASTONE, "raw");

    public static final ModelDispatcher MODEL_COLORED_STONE = new ModelDispatcherBasic(
            BlockColors.INSTANCE, "colored_stone_0_0",
            new ColorController("colored_stone", 4, BlockRenderLayer.SOLID, true, true));
    public static final NiceBlockPlus BLOCK_FLEXSTONE_COLORED = new NiceBlockPlus(new ColorHelperPlus(MODEL_COLORED_STONE), BaseMaterial.FLEXSTONE, "smooth");

    public static final ModelDispatcherLayered MODEL_HOT_BASALT = new ModelDispatcherLayered(new NoColor(4), "cool_basalt_0_0",
            new ColorController("cool_basalt", 4, BlockRenderLayer.SOLID, true, true),
            new HotBasaltController());
    public static final NiceBlock BLOCK_HOT_BASALT = (NiceBlock) new HotBasaltBlock(new HotBasaltHelper(MODEL_HOT_BASALT), BaseMaterial.FLEXSTONE, "hot_basalt");

    public static final ModelDispatcherBasic MODEL_FLOWING_LAVA = new ModelDispatcherBasic(new NoColor(16), "volcanic_lava_flow_0_0",
            new FlowController("volcanic_lava_flow", 1, BlockRenderLayer.SOLID, LightingMode.FULLBRIGHT));
    public static final NiceBlock BLOCK_FLOWING_LAVA = (NiceBlock) new FlowingLavaBlock(new FlowHeightHelper(MODEL_FLOWING_LAVA, 16), BaseMaterial.FLEXSTONE, "flowing_lava")
        .setLightLevel(3F/15F);
        
     public static final NiceBlock BLOCK_STATIC_LAVA = (NiceBlock) new StaticLavaBlock(new FlowHeightHelper(MODEL_FLOWING_LAVA, 16), BaseMaterial.FLEXSTONE, "static_lava")
        .setLightLevel(3F/15F);
     
    public static final ModelDispatcherBasic MODEL_COOL_BASALT = new ModelDispatcherBasic(new NoColor(16), "cool_basalt_0_0",
            new FlowController("cool_basalt", 1, BlockRenderLayer.SOLID, LightingMode.SHADED));
    public static final NiceBlock BLOCK_COOL_BASALT = (NiceBlock) new FlowHeightBlock(new FlowHeightHelper(MODEL_COOL_BASALT, 16), BaseMaterial.FLEXSTONE, "cool_basalt");

    public static final NiceBlock BLOCK_COOL_BASALT_FILLER = (NiceBlock) new FlowFillerBlock(new FlowHeightHelper(MODEL_COOL_BASALT, 16), BaseMaterial.FLEXSTONE, "cool_basalt_filler");

//    public static final ModelDispatcherBasic MODEL_COOL_BASALT = new ModelDispatcherBasic(new NoColor(16), "cool_basalt_0_0",
//            new HeightController("cool_basalt", 1, BlockRenderLayer.SOLID, true, false));
//    public static final NiceBlock BLOCK_COOL_BASALT = (NiceBlock) new HeightBlock(new SimpleHelper(MODEL_COOL_BASALT, 5), BaseMaterial.FLEXSTONE, "cool_basalt");

    
    public static final ModelDispatcherLayered MODEL_BORDER_TEST = new ModelDispatcherLayered(BlockColors.INSTANCE, "colored_stone_0_0",
            new BigTexController("bigtex_rock_test", BlockRenderLayer.SOLID, true, true),
            new BorderController("bordertest", 1, BlockRenderLayer.TRANSLUCENT, true));
    public static final NiceBlockPlus BLOCK_BORDERED = new NiceBlockPlus(new BigBlockHelper(MODEL_BORDER_TEST, (3 << 16) | (3 << 8) | 3), BaseMaterial.FLEXSTONE, "bordered");

    public static final ModelDispatcherLayered MODEL_BIGBRICK_TEST = new ModelDispatcherLayered(BlockColors.INSTANCE, "colored_stone_0_0",
            new BigTexController("bigtex_rock_test", BlockRenderLayer.SOLID, true, true),
            new MasonryController("masonrytest", 1, BlockRenderLayer.CUTOUT_MIPPED, true));
    public static final NiceBlockPlus BLOCK_BIGBRICK = new NiceBlockPlus(new BigBlockHelper(MODEL_BIGBRICK_TEST, (2 << 16) | (1 << 8) | 1), BaseMaterial.FLEXSTONE, "bigbrick");

    public static final ModelDispatcherLayered MODEL_COLUMN_SQUARE_2 = new ModelDispatcherLayered(BlockColors.INSTANCE, "colored_stone_0_0",
            new ColumnSquareController("colored_stone", 1, ColumnSquareController.ModelType.LAMP_BASE, true, 2, true),
            new ColumnSquareController("colored_stone", 1, ColumnSquareController.ModelType.LAMP_OVERLAY, true, 2, true));
    public static final Block BLOCK_COLUMN_SQUARE_2 = new ColumnSquareBlock(new AxisOrientedHelper(MODEL_COLUMN_SQUARE_2), BaseMaterial.FLEXSTONE, "column_square_2")
        .setLightLevel(3F/15F);//.setLightOpacity(0);
    
    public static final ModelDispatcherBasic MODEL_COLUMN_SQUARE_3 = new ModelDispatcherBasic(BlockColors.INSTANCE, "colored_stone_0_0",
            new ColumnSquareController("colored_stone", 1, ColumnSquareController.ModelType.NORMAL, true, 3, true));
    public static final NiceBlockPlus BLOCK_COLUMN_SQUARE_3 = new ColumnSquareBlock(new AxisOrientedHelper(MODEL_COLUMN_SQUARE_3), BaseMaterial.FLEXSTONE, "column_square_3");

    public static final ModelDispatcherLayered MODEL_COLUMN_SQUARE_4 = new ModelDispatcherLayered(BlockColors.INSTANCE, "colored_stone_0_0",
            new ColumnSquareController("colored_stone", 1, ColumnSquareController.ModelType.LAMP_BASE, true, 4, true),
            new ColumnSquareController("colored_stone", 1, ColumnSquareController.ModelType.LAMP_OVERLAY, true, 4, true));
    public static final Block BLOCK_COLUMN_SQUARE_4 = new ColumnSquareBlock(new AxisOrientedHelper(MODEL_COLUMN_SQUARE_4), BaseMaterial.FLEXSTONE, "column_square_4")
            .setLightLevel(3F/15F);//.setLightOpacity(0);

    public static final ModelDispatcherBasic MODEL_COLUMN_SQUARE_5 = new ModelDispatcherBasic(BlockColors.INSTANCE, "colored_stone_0_0",
            new ColumnSquareController("colored_stone", 1, ColumnSquareController.ModelType.NORMAL, true, 5, false));
    public static final NiceBlockPlus BLOCK_COLUMN_SQUARE_5 = new ColumnSquareBlock(new AxisOrientedHelper(MODEL_COLUMN_SQUARE_5), BaseMaterial.FLEXSTONE, "column_square_5");
    
    // declare the block instances
    // public static final NiceBlock raw1 = new NiceBlock(NiceStyle.RAW, new PlacementSimple(),
    // BaseMaterial.FLEXSTONE, 1);
    //
    // public static final NiceBlock smooth1 = new NiceBlock(NiceStyle.SMOOTH,
    // new PlacementSimple(), BaseMaterial.FLEXSTONE, 1);
    // public static final NiceBlock largeBrick1 = new NiceBlock("large_brick_1", NiceStyle.LARGE_BRICKS,
    // new PlacementSimple(), BaseMaterial.REFORMED_STONE, 1);
    // public static final NiceBlock smallBrick1 = new NiceBlock("small_brick_1", NiceStyle.SMALL_BRICKS,
    // new PlacementSimple(), BaseMaterial.REFORMED_STONE, 1);
    // public static final NiceBlock bigBlockA1 = new NiceBlock("big_block_a_1", NiceStyle.BIG_WORN,
    // new PlacementBigBlock(), BaseMaterial.REFORMED_STONE, 1);
    // public static final NiceBlock bigBlockB1 = new NiceBlock("big_block_b_1", NiceStyle.BIG_WORN,
    // new PlacementBigBlock(), BaseMaterial.REFORMED_STONE, 1);
    // public static final NiceBlock bigBlockC1 = new NiceBlock("big_block_c_1", NiceStyle.BIG_WORN,
    // new PlacementBigBlock(), BaseMaterial.REFORMED_STONE, 1);
    // public static final NiceBlock bigBlockD1 = new NiceBlock("big_block_d_1", NiceStyle.BIG_WORN,
    // new PlacementBigBlock(), BaseMaterial.REFORMED_STONE, 1);
    // public static final NiceBlock bigBlockE1 = new NiceBlock("big_block_e_1", NiceStyle.BIG_WORN,
    // new PlacementBigBlock(), BaseMaterial.REFORMED_STONE, 1);
    // public static final NiceBlock bigWeathered1 = new NiceBlock("big_weathered_1", NiceStyle.BIG_WEATHERED,
    // new PlacementBigBlock(), BaseMaterial.REFORMED_STONE, 1);
    // public static final NiceBlock bigOrnate1 = new NiceBlock("big_ornate_1", NiceStyle.BIG_ORNATE,
    // new PlacementBigBlock(), BaseMaterial.REFORMED_STONE, 1);
    // public static final NiceBlock masonryA1 = new NiceBlock("masonry_a_1", NiceStyle.MASONRY_A,
    // NicePlacement.makeMasonryPlacer(), BaseMaterial.REFORMED_STONE, 1);
    // public static final NiceBlock masonryB1 = new NiceBlock("masonry_b_1", NiceStyle.MASONRY_B,
    // NicePlacement.makeMasonryPlacer(), BaseMaterial.REFORMED_STONE, 1);
    // public static final NiceBlock masonryC1 = new NiceBlock("masonry_c_1", NiceStyle.MASONRY_C,
    // NicePlacement.makeMasonryPlacer(), BaseMaterial.REFORMED_STONE, 1);
    // public static final NiceBlock masonryD1 = new NiceBlock("masonry_d_1", NiceStyle.MASONRY_D,
    // NicePlacement.makeMasonryPlacer(), BaseMaterial.REFORMED_STONE, 1);
    // public static final NiceBlock masonryE1 = new NiceBlock("masonry_e_1", NiceStyle.MASONRY_E,
    // NicePlacement.makeMasonryPlacer(), BaseMaterial.REFORMED_STONE, 1);

    // public static final NiceBlock columnSquareX1 = new NiceBlock(NiceStyle.COLUMN_SQUARE_X,
    // NicePlacement.makeColumnPlacerSquare(), BaseMaterial.FLEXSTONE, 1);
    // public static final NiceBlock columnSquareY1 = new NiceBlock(NiceStyle.COLUMN_SQUARE_Y,
    // NicePlacement.makeColumnPlacerSquare(), BaseMaterial.FLEXSTONE, 1);
    // public static final NiceBlock columnSquareZ1 = new NiceBlock(NiceStyle.COLUMN_SQUARE_Z,
    // NicePlacement.makeColumnPlacerSquare(), BaseMaterial.FLEXSTONE, 1);
    //
    // public static final NiceBlockNonCubic columnRoundX1 = new NiceBlockNonCubic(NiceStyle.COLUMN_ROUND_X,
    // NicePlacement.makeColumnPlacerRound(), BaseMaterial.FLEXSTONE, 1);
    // public static final NiceBlockNonCubic columnRoundY1 = new NiceBlockNonCubic(NiceStyle.COLUMN_ROUND_Y,
    // NicePlacement.makeColumnPlacerRound(), BaseMaterial.FLEXSTONE, 1);
    // public static final NiceBlockNonCubic columnRoundZ1 = new NiceBlockNonCubic(NiceStyle.COLUMN_ROUND_Z,
    // NicePlacement.makeColumnPlacerRound(), BaseMaterial.FLEXSTONE, 1);

    // public static final NiceBlock hotBasalt = new NiceBlockHotBasalt("hot_basalt", NiceStyle.HOT_BASALT, new PlacementSimple(),
    // substance16Group[1]);

    // public static final NiceBlock hotBasalt = new NiceBlockHotBasalt(NiceStyle.HOT_BASALT,
    // new PlacementSimple(), BaseMaterial.FLEXSTONE, 1);
    //
    // public static final NiceBlock bigTex = new NiceBlockPlus(NiceStyle.BIG_TEX,
    // new PlacementSimple(), BaseMaterial.FLEXSTONE, 16);

    /**
     * Use to generate model resource location names with a consistent convention.
     */
    // public static String getModelResourceNameForBlock(NiceBlock block) {
    // return Adversity.MODID + ":" + block.getUnlocalizedName();
    // }

    /**
     * Handles all the plumbing needed to make a block work except for the instantiation. It should never be necessary to call this method directly. This is called during pre-init
     * for every block in allBlocks collection. NiceBlocks add themselves to the allBlocks collection automatically at instantiation.
     */
    /**
     * private static void registerBlockCompletely(NiceBlock block, FMLPreInitializationEvent event) {
     * 
     * // actually register the block! Hurrah! GameRegistry.registerBlock(block, NiceItemBlock.class); block.item.registerSelf();
     * 
     * if (event.getSide() == Side.CLIENT) {
     * 
     * // ModelResourceLocation mrlBlock = new ModelResourceLocation(getModelResourceNameForBlock(block));
     * 
     * // Blocks need custom state mapper for two reasons // 1) To avoid creating mappings for unused substance indexes // (metadata values) // 2) To point them to our custom model
     * instead of looking for a // json file ModelLoader.setCustomStateMapper(block, NiceBlockStateMapper.instance);
     * 
     * // ModelResourceLocation mrlItem = new ModelResourceLocation(getModelResourceNameFromMeta(block, i), // "inventory");
     * 
     * // ModelLoader.setCustomModelResourceLocation(block.item, i, mrlItem);
     * 
     * 
     * 
     * // prevents console spam about missing item models //ModelBakery.addVariantName(block.item, getModelResourceNameFromMeta(block, i));
     * 
     * // ModelBakery.registerItemVariants(item, names);
     * 
     * // Create model for later event handling. // NiceModel model = block.style.getModelController().getModel(i); // allModels.add(new ModelRegistration(model, mrlBlock,
     * mrlItem));
     * 
     * 
     * // ModelResourceLocation blueModel = new ModelResourceLocation(getRegistryName() + "_blue", "inventory"); // ModelResourceLocation redModel = new
     * ModelResourceLocation(getRegistryName() + "_red", "inventory"); // // ModelBakery.registerItemVariants(this, blueModel, redModel); // //
     * ModelLoader.setCustomMeshDefinition(this, stack -> { // if (isBlue(stack)) { // return blueModel; // } else { // return redModel; // } // });
     * 
     * } }
     */

    public static void preInit(FMLPreInitializationEvent event)
    {
        // SET UP COLOR ATLAS
        {
            NiceHues.INSTANCE.writeColorAtlas(event.getModConfigurationDirectory());
        }
        
        // REGISTER ALL BLOCKS
        for (NiceBlock block : allBlocks)
        {
            GameRegistry.registerBlock(block, null, block.getRegistryName());
            block.item.registerSelf();

            if (event.getSide() == Side.CLIENT)
            {
                ModelLoader.setCustomStateMapper(block, NiceBlockStateMapper.instance);
                
                for (int i = 0; i < block.blockModelHelper.getItemModelCount(); i++)
                {
                    ModelResourceLocation itemModelResourceLocation = 
                        new ModelResourceLocation(((NiceBlock)block).getRegistryName() + "." + i, "inventory");
                    ModelLoader.setCustomModelResourceLocation(block.item, i, itemModelResourceLocation);
                }
            }
        }
        
        GameRegistry.registerTileEntity(NiceTileEntity.class, "nicetileentity");

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
            //Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(block.blockModelHelper.dispatcher, block);
            Minecraft.getMinecraft().getItemColors().registerItemColorHandler(block.item, block);
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
            //dispatcher.controller.getBakedModelFactory().handleBakeEvent(event);
            
            event.getModelRegistry().putObject(new ModelResourceLocation(dispatcher.getModelResourceString()), dispatcher);
        }
        
        for (NiceBlock block : allBlocks)
        {
            for (int i = 0; i < block.blockModelHelper.getItemModelCount(); i++)
            {          
                event.getModelRegistry().putObject(new ModelResourceLocation(block.getRegistryName() + "." + i, "inventory"),
                        block.blockModelHelper.dispatcher);
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
