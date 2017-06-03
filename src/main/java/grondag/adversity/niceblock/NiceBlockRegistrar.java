package grondag.adversity.niceblock;

import grondag.adversity.feature.volcano.lava.CoolingBlock;
import grondag.adversity.feature.volcano.lava.VolcanicLavaBlock;
import grondag.adversity.init.ModItems;
import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.niceblock.base.ModelDispatcher;
import grondag.adversity.niceblock.base.ModelHolder;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.base.NiceItemBlock;
import grondag.adversity.niceblock.base.NiceTileEntity;
import grondag.adversity.niceblock.block.CSGBlock;
import grondag.adversity.niceblock.block.FlowDynamicBlock;
import grondag.adversity.niceblock.block.FlowSimpleBlock;
import grondag.adversity.niceblock.block.FlowStaticBlock;
import grondag.adversity.niceblock.model.BigTexModelFactory;
import grondag.adversity.niceblock.model.CSGModelFactory;
import grondag.adversity.niceblock.model.FlowModelFactory;
import grondag.adversity.niceblock.model.texture.TextureProviders;
import grondag.adversity.niceblock.modelstate.ModelStateComponents;
import grondag.adversity.niceblock.support.BlockSubstance;
import grondag.adversity.niceblock.support.NiceBlockHighlighter;
import grondag.adversity.niceblock.support.NiceBlockStateMapper;
import java.io.IOException;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.client.event.ModelBakeEvent;
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

    //TODO: move all these to volcano package
    private final static ModelHolder HOT_FLOWING_LAVA_MODEL = new ModelHolder(new FlowModelFactory(true, ModelStateComponents.FLOW_JOIN,
            ModelStateComponents.FLOW_TEX, ModelStateComponents.TEXTURE_1, ModelStateComponents.ROTATION_NONE, ModelStateComponents.COLORS_WHITE),
            TextureProviders.TEX_BT_LAVA.getTextureState(false, LightingMode.FULLBRIGHT, BlockRenderLayer.SOLID));
    private static final ModelDispatcher HOT_FLOWING_LAVA_DISPATCH = new ModelDispatcher(HOT_FLOWING_LAVA_MODEL);    
    public static final VolcanicLavaBlock HOT_FLOWING_LAVA_HEIGHT_BLOCK = 
             new VolcanicLavaBlock(HOT_FLOWING_LAVA_DISPATCH, BlockSubstance.VOLCANIC_LAVA, "flow", false);
    public static final NiceItemBlock HOT_FLOWING_LAVA_HEIGHT_ITEM = new NiceItemBlock(HOT_FLOWING_LAVA_HEIGHT_BLOCK);
    public static final VolcanicLavaBlock HOT_FLOWING_LAVA_FILLER_BLOCK = 
             new VolcanicLavaBlock(HOT_FLOWING_LAVA_DISPATCH, BlockSubstance.VOLCANIC_LAVA, "fill", true);
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
    public static final NiceBlock COOL_FLOWING_BASALT_HEIGHT_BLOCK = new FlowDynamicBlock(COOL_FLOWING_BASALT_DISPATCH, BlockSubstance.BASALT, "flow", false)
            .setDropItem(ModItems.basalt_rubble);
    public static final NiceItemBlock COOL_FLOWING_BASALT_HEIGHT_ITEM = new NiceItemBlock(COOL_FLOWING_BASALT_HEIGHT_BLOCK);
    public static final NiceBlock COOL_FLOWING_BASALT_FILLER_BLOCK = new FlowDynamicBlock(COOL_FLOWING_BASALT_DISPATCH, BlockSubstance.BASALT, "fill", true)
            .setDropItem(ModItems.basalt_rubble);
    public static final NiceItemBlock COOL_FLOWING_BASALT_FILLER_ITEM = new NiceItemBlock(COOL_FLOWING_BASALT_FILLER_BLOCK);
    // STATIC VERSION
    public static final FlowStaticBlock COOL_STATIC_BASALT_HEIGHT_BLOCK = (FlowStaticBlock) new FlowStaticBlock(COOL_FLOWING_BASALT_DISPATCH, BlockSubstance.BASALT, "static_flow", false)
            .setDropItem(ModItems.basalt_rubble);
    public static final NiceItemBlock COOL_STATIC_BASALT_HEIGHT_ITEM = new NiceItemBlock(COOL_STATIC_BASALT_HEIGHT_BLOCK);
    public static final FlowStaticBlock COOL_STATIC_BASALT_FILLER_BLOCK = (FlowStaticBlock) new FlowStaticBlock(COOL_FLOWING_BASALT_DISPATCH, BlockSubstance.BASALT, "static_fill", true)
            .setDropItem(ModItems.basalt_rubble);
    public static final NiceItemBlock COOL_STATIC_BASALT_FILLER_ITEM = new NiceItemBlock(COOL_STATIC_BASALT_FILLER_BLOCK);
 
    
    // COOLING BASALT
    private static final ModelDispatcher HOT_FLOWING_BASALT_0_DISPATCH = new ModelDispatcher(HOT_FLOWING_LAVA_MODEL, HOT_FLOWING_BASALT_0_MODEL);    
    public static final NiceBlock HOT_FLOWING_BASALT_0_HEIGHT_BLOCK = new CoolingBlock(HOT_FLOWING_BASALT_0_DISPATCH, BlockSubstance.BASALT, "cooling_flow", false)
            .setCoolingBlockInfo((FlowDynamicBlock) COOL_FLOWING_BASALT_HEIGHT_BLOCK, 1).setDropItem(ModItems.basalt_rubble).setAllowSilkHarvest(false);
    public static final NiceItemBlock HOT_FLOWING_BASALT_0_HEIGHT_ITEM = new NiceItemBlock(HOT_FLOWING_BASALT_0_HEIGHT_BLOCK);
    public static final NiceBlock HOT_FLOWING_BASALT_0_FILLER_BLOCK = new CoolingBlock(HOT_FLOWING_BASALT_0_DISPATCH, BlockSubstance.BASALT, "cooling_fill", true)
            .setCoolingBlockInfo((FlowDynamicBlock) COOL_FLOWING_BASALT_FILLER_BLOCK, 1).setDropItem(ModItems.basalt_rubble).setAllowSilkHarvest(false);
    public static final NiceItemBlock HOT_FLOWING_BASALT_0_FILLER_ITEM = new NiceItemBlock(HOT_FLOWING_BASALT_0_FILLER_BLOCK);

    // WARM BASALT
    private static final ModelDispatcher HOT_FLOWING_BASALT_1_DISPATCH = new ModelDispatcher(HOT_FLOWING_LAVA_MODEL, HOT_FLOWING_BASALT_1_MODEL);
    public static final NiceBlock HOT_FLOWING_BASALT_1_HEIGHT_BLOCK = new CoolingBlock(HOT_FLOWING_BASALT_1_DISPATCH, BlockSubstance.BASALT, "warm_flow", false)
            .setCoolingBlockInfo((FlowDynamicBlock) HOT_FLOWING_BASALT_0_HEIGHT_BLOCK, 2).setDropItem(ModItems.basalt_rubble).setAllowSilkHarvest(false);
    public static final NiceItemBlock HOT_FLOWING_BASALT_1_HEIGHT_ITEM = new NiceItemBlock(HOT_FLOWING_BASALT_1_HEIGHT_BLOCK);
    public static final NiceBlock HOT_FLOWING_BASALT_1_FILLER_BLOCK = new CoolingBlock(HOT_FLOWING_BASALT_1_DISPATCH, BlockSubstance.BASALT, "warm_fill", true)
            .setCoolingBlockInfo((FlowDynamicBlock) HOT_FLOWING_BASALT_0_FILLER_BLOCK, 2).setDropItem(ModItems.basalt_rubble).setAllowSilkHarvest(false);
    public static final NiceItemBlock HOT_FLOWING_BASALT_1_FILLER_ITEM = new NiceItemBlock(HOT_FLOWING_BASALT_1_FILLER_BLOCK);
    
    // HOT BASALT
    private static final ModelDispatcher HOT_FLOWING_BASALT_2_DISPATCH = new ModelDispatcher(HOT_FLOWING_LAVA_MODEL, HOT_FLOWING_BASALT_2_MODEL);
    public static final NiceBlock HOT_FLOWING_BASALT_2_HEIGHT_BLOCK = new CoolingBlock(HOT_FLOWING_BASALT_2_DISPATCH, BlockSubstance.BASALT, "hot_flow", false)
            .setCoolingBlockInfo((FlowDynamicBlock) HOT_FLOWING_BASALT_1_HEIGHT_BLOCK, 3).setDropItem(ModItems.basalt_rubble).setAllowSilkHarvest(false);
    public static final NiceItemBlock HOT_FLOWING_BASALT_2_HEIGHT_ITEM = new NiceItemBlock(HOT_FLOWING_BASALT_2_HEIGHT_BLOCK);
    public static final NiceBlock HOT_FLOWING_BASALT_2_FILLER_BLOCK = new CoolingBlock(HOT_FLOWING_BASALT_2_DISPATCH, BlockSubstance.BASALT, "hot_fill", true)
            .setCoolingBlockInfo((FlowDynamicBlock) HOT_FLOWING_BASALT_1_FILLER_BLOCK, 3).setDropItem(ModItems.basalt_rubble).setAllowSilkHarvest(false);
    public static final NiceItemBlock HOT_FLOWING_BASALT_2_FILLER_ITEM = new NiceItemBlock(HOT_FLOWING_BASALT_2_FILLER_BLOCK);

    // VERY HOT BASALT
    private static final ModelDispatcher HOT_FLOWING_BASALT_3_DISPATCH = new ModelDispatcher(HOT_FLOWING_LAVA_MODEL, HOT_FLOWING_BASALT_3_MODEL);  
    public static final NiceBlock HOT_FLOWING_BASALT_3_HEIGHT_BLOCK = new CoolingBlock(HOT_FLOWING_BASALT_3_DISPATCH, BlockSubstance.BASALT, "very_hot_flow", false)
            .setCoolingBlockInfo((FlowDynamicBlock) HOT_FLOWING_BASALT_2_HEIGHT_BLOCK, 4).setDropItem(ModItems.basalt_rubble).setAllowSilkHarvest(false);
    public static final NiceItemBlock HOT_FLOWING_BASALT_3_HEIGHT_ITEM = new NiceItemBlock(HOT_FLOWING_BASALT_3_HEIGHT_BLOCK);
    public static final NiceBlock HOT_FLOWING_BASALT_3_FILLER_BLOCK = new CoolingBlock(HOT_FLOWING_BASALT_3_DISPATCH, BlockSubstance.BASALT, "very_hot_fill", true)
            .setCoolingBlockInfo((FlowDynamicBlock) HOT_FLOWING_BASALT_2_FILLER_BLOCK, 4).setDropItem(ModItems.basalt_rubble).setAllowSilkHarvest(false);
    public static final NiceItemBlock HOT_FLOWING_BASALT_3_FILLER_ITEM = new NiceItemBlock(HOT_FLOWING_BASALT_3_FILLER_BLOCK);
  
    
    // COOLING LAVA
    public static final NiceBlock HOT_STATIC_LAVA_HEIGHT_BLOCK =  new CoolingBlock(HOT_FLOWING_LAVA_DISPATCH, BlockSubstance.VOLCANIC_LAVA, "cooling_lava_flow", false)
            .setCoolingBlockInfo((FlowDynamicBlock) HOT_FLOWING_BASALT_3_HEIGHT_BLOCK, 4).setDropItem(ModItems.basalt_rubble).setAllowSilkHarvest(false);
   public static final NiceItemBlock HOT_STATIC_LAVA_HEIGHT_ITEM = new NiceItemBlock(HOT_STATIC_LAVA_HEIGHT_BLOCK);
   public static final NiceBlock HOT_STATIC_LAVA_FILLER_BLOCK = new CoolingBlock(HOT_FLOWING_LAVA_DISPATCH, BlockSubstance.VOLCANIC_LAVA, "cooling_lava_fill", true)
           .setCoolingBlockInfo((FlowDynamicBlock) HOT_FLOWING_BASALT_3_FILLER_BLOCK, 4).setDropItem(ModItems.basalt_rubble).setAllowSilkHarvest(false);
   public static final NiceItemBlock HOT_STATIC_LAVA_FILLER_ITEM = new NiceItemBlock(HOT_STATIC_LAVA_FILLER_BLOCK);
   
    private final static ModelHolder COOL_SQUARE_BASALT_MODEL 
        = new ModelHolder(new BigTexModelFactory(ModelStateComponents.COLORS_BASALT,
                ModelStateComponents.BIG_TEX_IGNORE_META, ModelStateComponents.TEXTURE_1),
                TextureProviders.TEX_BT_BASALT_CUT.getTextureState(false, LightingMode.SHADED, BlockRenderLayer.SOLID));
    private static final ModelDispatcher COOL_SQUARE_BASALT_DISPATCH = new ModelDispatcher(COOL_SQUARE_BASALT_MODEL);
    public static final NiceBlock COOL_SQUARE_BASALT_BLOCK = new FlowSimpleBlock(COOL_SQUARE_BASALT_DISPATCH, BlockSubstance.BASALT, "cool")
            .setDropItem(ModItems.basalt_cobble);
    public static final NiceItemBlock COOL_SQUARE_BASALT_ITEM = new NiceItemBlock(COOL_SQUARE_BASALT_BLOCK);
    
    private final static ModelHolder CSG_TEST_MODEL = new ModelHolder(new CSGModelFactory(ModelStateComponents.COLORS_BLOCK,
            ModelStateComponents.TEXTURE_4, ModelStateComponents.ROTATION),
            TextureProviders.TEX_BLOCK_COLORED_STONE.getTextureState(true, LightingMode.SHADED, BlockRenderLayer.SOLID));
    private static final ModelDispatcher CSG_TEST_DISPATCH = new ModelDispatcher(CSG_TEST_MODEL);
    public static final CSGBlock CSG_TEST_BLOCK = new CSGBlock(CSG_TEST_DISPATCH, BlockSubstance.FLEXSTONE, "csg_test");
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

        // no idea why this was here twice
//        for (ModelDispatcher dispatcher : allDispatchers)
//        {
//            dispatcher.handleBakeEvent(event);
//            // dispatcher.controller.getBakedModelFactory().handleBakeEvent(event);
//
//            event.getModelRegistry().putObject(new ModelResourceLocation(dispatcher.getModelResourceString()), dispatcher);
//        }

        for (NiceBlock block : allBlocks)
        {
            for (ItemStack stack : block.getSubItems())
            {
                event.getModelRegistry().putObject(new ModelResourceLocation(block.getRegistryName() + "." + stack.getMetadata(), "inventory"),
                        block.dispatcher);
            }
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
}
