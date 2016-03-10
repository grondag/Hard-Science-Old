package grondag.adversity.niceblock.newmodel;

import grondag.adversity.niceblock.newmodel.color.BlockColors;
import grondag.adversity.niceblock.newmodel.color.FixedColors;
import grondag.adversity.niceblock.newmodel.color.IColorProvider;
import grondag.adversity.niceblock.newmodel.color.HueSet.Tint;
import grondag.adversity.niceblock.newmodel.color.NiceHues.Hue;
import grondag.adversity.niceblock.newmodel.color.NiceHues;
import grondag.adversity.niceblock.newmodel.color.NiceColor;
import grondag.adversity.niceblock.newmodel.color.NoColor;
import grondag.adversity.niceblock.support.NiceBlockHighlighter;
import grondag.adversity.niceblock.support.NiceBlockStateMapper;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.ResourceLocation;
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
    public static LinkedList<ModelDispatcherBase> allDispatchers = new LinkedList<ModelDispatcherBase>();

    // DECLARE MODEL DISPATCH & BLOCK INSTANCES
    public static final ModelDispatcherBase MODEL_FLEXSTONE_RAW = new ModelDispatcherBasic(new FixedColors(
            BlockColors.makeColorMap(Hue.YELLOW, Tint.WHITE)), "raw_flexstone_0_0",
            new ColoredBlockController("raw_flexstone", 4, EnumWorldBlockLayer.SOLID, true, true));
    public static final NiceBlock BLOCK_FLEXSTONE_RAW = new NiceBlock(new ColoredBlockHelperMeta(MODEL_FLEXSTONE_RAW), BaseMaterial.FLEXSTONE, "raw");
    
    public static final ModelDispatcherBase MODEL_DURASTONE_RAW = new ModelDispatcherBasic(new FixedColors(
            BlockColors.makeColorMap(Hue.COBALT, Tint.WHITE)), "raw_durastone_0_0",
            new ColoredBlockController("raw_durastone", 4, EnumWorldBlockLayer.SOLID, true, true));
    public static final NiceBlock BLOCK_DURASTONE_RAW = new NiceBlock(new ColoredBlockHelperMeta(MODEL_DURASTONE_RAW), BaseMaterial.DURASTONE, "raw");

    public static final ModelDispatcherBase MODEL_COLORED_STONE = new ModelDispatcherBasic(
            BlockColors.INSTANCE, "colored_stone_0_0",
            new ColoredBlockController("colored_stone", 4, EnumWorldBlockLayer.SOLID, true, true));
    public static final NiceBlockPlus BLOCK_FLEXSTONE_COLORED = new NiceBlockPlus(new ColoredBlockHelperPlus(MODEL_COLORED_STONE), BaseMaterial.FLEXSTONE, "smooth");

    public static final ModelDispatcherLayered MODEL_HOT_BASALT = new ModelDispatcherLayered(new NoColor(4), "cool_basalt_0_0",
            new ColoredBlockController("cool_basalt", 4, EnumWorldBlockLayer.SOLID, true, true),
            new HotBasaltController());
    public static final NiceBlock BLOCK_HOT_BASALT = new NiceBlock(new HotBasaltHelper(MODEL_HOT_BASALT), BaseMaterial.FLEXSTONE, "hot_basalt");


    public static final ModelDispatcherLayered MODEL_BORDER_TEST = new ModelDispatcherLayered(BlockColors.INSTANCE, "colored_stone_0_0",
            new BigTexController("bigtex_rock_test", EnumWorldBlockLayer.SOLID, true, true),
            new BorderController("bordertest", 1, EnumWorldBlockLayer.TRANSLUCENT, true));
    public static final NiceBlockPlus BLOCK_BORDERED = new NiceBlockPlus(new BigBlockHelper(MODEL_BORDER_TEST, (3 << 16) | (3 << 8) | 3), BaseMaterial.FLEXSTONE, "bordered");

    public static final ModelDispatcherLayered MODEL_BIGBRICK_TEST = new ModelDispatcherLayered(BlockColors.INSTANCE, "colored_stone_0_0",
            new BigTexController("bigtex_rock_test", EnumWorldBlockLayer.SOLID, true, true),
            new MasonryController("masonrytest", 1, EnumWorldBlockLayer.CUTOUT_MIPPED, true));
    public static final NiceBlockPlus BLOCK_BIGBRICK = new NiceBlockPlus(new BigBlockHelper(MODEL_BIGBRICK_TEST, (2 << 16) | (1 << 8) | 1), BaseMaterial.FLEXSTONE, "bigbrick");

    public static final ModelDispatcherLayered MODEL_COLUMN_SQUARE_2 = new ModelDispatcherLayered(BlockColors.INSTANCE, "colored_stone_0_0",
            new ColumnSquareController("colored_stone", 1, ColumnSquareController.ModelType.LAMP_BASE, true, 2, true),
            new ColumnSquareController("colored_stone", 1, ColumnSquareController.ModelType.LAMP_OVERLAY, true, 2, true));
    public static final Block BLOCK_COLUMN_SQUARE_2 = new ColumnSquareBlock(new AxisOrientedHelper(MODEL_COLUMN_SQUARE_2), BaseMaterial.FLEXSTONE, "column_square_2")
        .setLightLevel(1/15);
    public static final ModelDispatcherBasic MODEL_COLUMN_SQUARE_3 = new ModelDispatcherBasic(BlockColors.INSTANCE, "colored_stone_0_0",
            new ColumnSquareController("colored_stone", 1, ColumnSquareController.ModelType.NORMAL, true, 3, true));
    public static final NiceBlockPlus BLOCK_COLUMN_SQUARE_3 = new ColumnSquareBlock(new AxisOrientedHelper(MODEL_COLUMN_SQUARE_3), BaseMaterial.FLEXSTONE, "column_square_3");

    public static final ModelDispatcherLayered MODEL_COLUMN_SQUARE_4 = new ModelDispatcherLayered(BlockColors.INSTANCE, "colored_stone_0_0",
            new ColumnSquareController("colored_stone", 1, ColumnSquareController.ModelType.LAMP_BASE, true, 4, true),
            new ColumnSquareController("colored_stone", 1, ColumnSquareController.ModelType.LAMP_OVERLAY, true, 4, true));
    public static final Block BLOCK_COLUMN_SQUARE_4 = new ColumnSquareBlock(new AxisOrientedHelper(MODEL_COLUMN_SQUARE_4), BaseMaterial.FLEXSTONE, "column_square_4")
            .setLightLevel(1/15);

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
        
        // Here because model generation is multi-threaded.
        // Was in a static{} block but want to ensure render thread 
        // does not access members before they can be initialized.
        ModelReference.setup();

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
//        for (NiceBlock block : allBlocks)
//        {
//            ModelResourceLocation itemModelResourceLocation = block.item.getModel(null, null, 0);
//            for (int i = 0; i < block.blockModelHelper.getMetaCount(); i++)
//            {
//                ModelLoader.setCustomModelResourceLocation(block.item, i, itemModelResourceLocation);
//            }
//        }
    }

    /**
     * Centralized event handler for NiceModel baking.
     */
    @SubscribeEvent
    public void onModelBakeEvent(ModelBakeEvent event) throws IOException
    {
        for (ModelDispatcherBase dispatcher : allDispatchers)
        {
            dispatcher.handleBakeEvent(event);
            //dispatcher.controller.getBakedModelFactory().handleBakeEvent(event);
            
            event.modelRegistry.putObject(new ModelResourceLocation(dispatcher.getModelResourceString()), dispatcher);
        }
        
        for (NiceBlock block : allBlocks)
        {
            for (int i = 0; i < block.blockModelHelper.getItemModelCount(); i++)
            {          
                ModelState modelState = block.blockModelHelper.getModelStateForItemModel(i);
                event.modelRegistry.putObject(new ModelResourceLocation(block.getRegistryName() + "." + i, "inventory"),
                        block.blockModelHelper.dispatcher.getItemModelForModelState(modelState));
            }
        }
    }

    /**
     * Centralized event handler for NiceModel texture stitch.
     */
    @SubscribeEvent
    public void stitcherEventPre(TextureStitchEvent.Pre event)
    {
        for (ModelDispatcherBase dispatcher : allDispatchers)
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
