package grondag.hard_science.init;

import grondag.hard_science.Configurator;
import grondag.hard_science.HardScience;
import grondag.hard_science.machines.impl.building.BlockFabricatorBlock;
import grondag.hard_science.machines.impl.logistics.BottomBusBlock;
import grondag.hard_science.machines.impl.logistics.ChemicalBatteryBlock;
import grondag.hard_science.machines.impl.logistics.MiddleBusBlock;
import grondag.hard_science.machines.impl.logistics.ModularTankBlock;
import grondag.hard_science.machines.impl.logistics.SmartChestBlock;
import grondag.hard_science.machines.impl.logistics.TopBusBlock;
import grondag.hard_science.machines.impl.logistics.TransportTestBlock;
import grondag.hard_science.machines.impl.logistics.WaterPumpBlock;
import grondag.hard_science.machines.impl.processing.CrushinatorBlock;
import grondag.hard_science.machines.impl.production.PhotoElectricBlock;
import grondag.hard_science.machines.impl.production.SolarCableBlock;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.block.SuperSimpleBlock;
import grondag.hard_science.superblock.color.BlockColorMapProvider;
import grondag.hard_science.superblock.color.Chroma;
import grondag.hard_science.superblock.color.Hue;
import grondag.hard_science.superblock.color.Luminance;
import grondag.hard_science.superblock.model.shape.ModelShape;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.model.state.PaintLayer;
import grondag.hard_science.superblock.terrain.DepletedFluidBlock;
import grondag.hard_science.superblock.terrain.TerrainBlockRegistry;
import grondag.hard_science.superblock.terrain.TerrainCubicBlock;
import grondag.hard_science.superblock.terrain.TerrainDynamicBlock;
import grondag.hard_science.superblock.terrain.TerrainStaticBlock;
import grondag.hard_science.superblock.texture.TexturePalletteRegistry.TexturePallette;
import grondag.hard_science.superblock.texture.Textures;
import grondag.hard_science.superblock.varia.BlockSubstance;
import grondag.hard_science.volcano.VolcanoBlock;
import grondag.hard_science.volcano.lava.CoolingBasaltBlock;
import grondag.hard_science.volcano.lava.LavaBlock;
import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@Mod.EventBusSubscriber
@ObjectHolder(HardScience.MODID)
public class ModBlocks
{
    public static final Block volcano_block = null;
    public static final Block basalt_cobble = null;
    public static final Block basalt_cool_dynamic_height = null;
    public static final Block basalt_cool_dynamic_filler = null;
    public static final Block basalt_cool_static_height = null;
    public static final Block basalt_cool_static_filler = null;
    public static final Block basalt_cut = null;
    public static final Block basalt_dynamic_cooling_height = null;
    public static final Block basalt_dynamic_cooling_filler = null;
    public static final Block basalt_dynamic_warm_height = null;
    public static final Block basalt_dynamic_warm_filler = null;
    public static final Block basalt_dynamic_hot_height = null;
    public static final Block basalt_dynamic_hot_filler = null;
    public static final Block basalt_dynamic_very_hot_height = null;
    public static final Block basalt_dynamic_very_hot_filler = null;

    public static final Block lava_dynamic_height = null;
    public static final Block lava_dynamic_filler = null;
    
    public static final Block smart_chest = null;
    public static final Block block_fabricator = null;
    public static final Block solar_cell = null;
    public static final Block extension_bus = null;
    public static final Block intermediate_bus = null;
    public static final Block depleted_fluid = null;
    public static final Block crushinator = null;
    
//    public static final Block solar_aggregator = null;
    
    
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) 
    {
        ModelState workingModel;
        
        workingModel = new ModelState();
        
        workingModel = new ModelState();
        workingModel.setShape(ModelShape.CUBE);
        workingModel.setTexture(PaintLayer.BASE, Textures.BLOCK_COBBLE);
        workingModel.setColorMap(PaintLayer.BASE, BlockColorMapProvider.COLOR_BASALT);
        event.getRegistry().register(new SuperSimpleBlock("basalt_cobble", BlockSubstance.BASALT, workingModel));

        if(Configurator.VOLCANO.enableVolcano)
        {
            event.getRegistry().register(new VolcanoBlock());
            
            
            workingModel = new ModelState();
            workingModel.setShape(ModelShape.TERRAIN_HEIGHT);
            workingModel.setTexture(PaintLayer.BASE, Textures.BIGTEX_BASALT_COOL_ZOOM);
            workingModel.setColorMap(PaintLayer.BASE, BlockColorMapProvider.COLOR_BASALT);
            workingModel.setTexture(PaintLayer.CUT, Textures.BIGTEX_BASALT_CUT);
            workingModel.setColorMap(PaintLayer.CUT, BlockColorMapProvider.COLOR_BASALT);
            
            Block dynamicBasaltHeight = new TerrainDynamicBlock("basalt_cool_dynamic_height", BlockSubstance.BASALT, workingModel.clone(), false);
            Block staticBasaltHeight = new TerrainStaticBlock("basalt_cool_static_height", BlockSubstance.BASALT, workingModel.clone(), false);
    
            event.getRegistry().register(dynamicBasaltHeight);
            event.getRegistry().register(staticBasaltHeight);
    
            workingModel = workingModel.clone();
            workingModel.setShape(ModelShape.TERRAIN_FILLER);
    
            Block dynamicBasaltFiller = new TerrainDynamicBlock("basalt_cool_dynamic_filler", BlockSubstance.BASALT, workingModel.clone(), true);
            Block staticBasaltFiller = new TerrainStaticBlock("basalt_cool_static_filler", BlockSubstance.BASALT, workingModel.clone(), true);
    
            event.getRegistry().register(dynamicBasaltFiller);
            event.getRegistry().register(staticBasaltFiller);
            
            TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.registerFiller(dynamicBasaltHeight, dynamicBasaltFiller);
            TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.registerFiller(staticBasaltHeight, staticBasaltFiller);
            
            TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.registerStateTransition(dynamicBasaltHeight, staticBasaltHeight);
            TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.registerStateTransition(dynamicBasaltFiller, staticBasaltFiller);
            
            workingModel = new ModelState();
            workingModel.setShape(ModelShape.CUBE);
            workingModel.setTexture(PaintLayer.BASE, Textures.BIGTEX_BASALT_CUT_ZOOM);
            workingModel.setColorMap(PaintLayer.BASE, BlockColorMapProvider.COLOR_BASALT);
            Block cubicBasalt  = new TerrainCubicBlock("basalt_cut", BlockSubstance.BASALT, workingModel.clone());
            event.getRegistry().register(cubicBasalt);
            
            TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.registerCubic(dynamicBasaltHeight, cubicBasalt);
            TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.registerCubic(dynamicBasaltFiller, cubicBasalt);
            TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.registerCubic(staticBasaltHeight, cubicBasalt);
            TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.registerCubic(staticBasaltFiller, cubicBasalt);
            
            
            workingModel = new ModelState();
            workingModel.setShape(ModelShape.TERRAIN_HEIGHT);
            workingModel.setTexture(PaintLayer.BASE, Textures.BIGTEX_LAVA);
            workingModel.setColorMap(PaintLayer.BASE, BlockColorMapProvider.INSTANCE.getColorMap(Hue.RED, Chroma.WHITE, Luminance.BRILLIANT));
            workingModel.setFullBrightness(PaintLayer.BASE, true);
            workingModel.setTexture(PaintLayer.MIDDLE, Textures.BIGTEX_BASALT_HINT);
            workingModel.setColorMap(PaintLayer.MIDDLE, BlockColorMapProvider.INSTANCE.getColorMap(Hue.VERMILLION, Chroma.ULTRA_ACCENT, Luminance.MEDIUM_DARK));
            workingModel.setFullBrightness(PaintLayer.MIDDLE, false);
            
            Block dynamicLavaHeight = new LavaBlock("lava_dynamic_height", BlockSubstance.VOLCANIC_LAVA, workingModel, false);
            
            workingModel = workingModel.clone();
            workingModel.setShape(ModelShape.TERRAIN_FILLER);
            Block dynamicLavaFiller = new LavaBlock("lava_dynamic_filler", BlockSubstance.VOLCANIC_LAVA, workingModel, true);
    
            event.getRegistry().register(dynamicLavaHeight);
            event.getRegistry().register(dynamicLavaFiller);
    
            TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.registerFiller(dynamicLavaHeight, dynamicLavaFiller);
            
            // COOLING BASALT
            Block dynamicCoolingBasaltHeight = makeCoolingBasalt("basalt_dynamic_cooling_height", Textures.BIGTEX_BASALT_COOLING, false);
            Block dynamicCoolingBasaltFiller = makeCoolingBasalt("basalt_dynamic_cooling_filler", Textures.BIGTEX_BASALT_COOLING, true);        
            event.getRegistry().register(dynamicCoolingBasaltHeight);
            event.getRegistry().register(dynamicCoolingBasaltFiller);
            TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.registerFiller(dynamicCoolingBasaltHeight, dynamicCoolingBasaltFiller);
    
            // WARM BASALT
            Block dynamicWarmBasaltHeight = makeCoolingBasalt("basalt_dynamic_warm_height", Textures.BIGTEX_BASALT_WARM, false);
            Block dynamicWarmBasaltFiller = makeCoolingBasalt("basalt_dynamic_warm_filler", Textures.BIGTEX_BASALT_WARM, true);        
            event.getRegistry().register(dynamicWarmBasaltHeight);
            event.getRegistry().register(dynamicWarmBasaltFiller);
            TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.registerFiller(dynamicWarmBasaltHeight, dynamicWarmBasaltFiller);
            
            // HOT BASALT
            Block dynamicHotBasaltHeight = makeCoolingBasalt("basalt_dynamic_hot_height", Textures.BIGTEX_BASALT_HOT, false);
            Block dynamicHotBasaltFiller = makeCoolingBasalt("basalt_dynamic_hot_filler", Textures.BIGTEX_BASALT_HOT, true);        
            event.getRegistry().register(dynamicHotBasaltHeight);
            event.getRegistry().register(dynamicHotBasaltFiller);
            TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.registerFiller(dynamicHotBasaltHeight, dynamicHotBasaltFiller);
            
            // VERY HOT BASALT
            Block dynamicVeryHotBasaltHeight = makeCoolingBasalt("basalt_dynamic_very_hot_height", Textures.BIGTEX_BASALT_VERY_HOT, false);
            Block dynamicVeryHotBasaltFiller = makeCoolingBasalt("basalt_dynamic_very_hot_filler", Textures.BIGTEX_BASALT_VERY_HOT, true);        
            event.getRegistry().register(dynamicVeryHotBasaltHeight);
            event.getRegistry().register(dynamicVeryHotBasaltFiller);
            TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.registerFiller(dynamicVeryHotBasaltHeight, dynamicVeryHotBasaltFiller);
        }
        
        event.getRegistry().register(new DepletedFluidBlock()
                .setRegistryName("depleted_fluid")
                .setUnlocalizedName("depeleted_fluid"));
        
        // MACHINE BLOCKS
        event.getRegistry().register(new SmartChestBlock("smart_chest"));
        event.getRegistry().register(new BlockFabricatorBlock("block_fabricator"));
        event.getRegistry().register(new PhotoElectricBlock("solar_cell"));
        event.getRegistry().register(new SolarCableBlock("solar_cable"));
        event.getRegistry().register(new BottomBusBlock("bottom_bus"));
        event.getRegistry().register(new MiddleBusBlock("middle_bus"));
        event.getRegistry().register(new TopBusBlock("top_bus"));
        event.getRegistry().register(new ModularTankBlock("water_tank", ModBulkResources.WATER.fluidResource()));
        event.getRegistry().register(new ModularTankBlock("h2o_tank", ModBulkResources.H2O_FLUID.fluidResource()));
        event.getRegistry().register(new ModularTankBlock("ammonia_tank", ModBulkResources.AMMONIA_GAS.fluidResource()));
        event.getRegistry().register(new WaterPumpBlock("water_pump"));
        event.getRegistry().register(new ChemicalBatteryBlock("chemical_battery"));
        event.getRegistry().register(new TransportTestBlock("transport_test"));
        event.getRegistry().register(new CrushinatorBlock("crushinator"));
    }
    
    private static Block makeCoolingBasalt(String name, TexturePallette tex, boolean  isFiller) 
    {
        ModelState model = new ModelState();
        model.setShape(isFiller ? ModelShape.TERRAIN_FILLER : ModelShape.TERRAIN_HEIGHT);
        model.setTexture(PaintLayer.BASE, Textures.BIGTEX_LAVA);
        model.setColorMap(PaintLayer.BASE, BlockColorMapProvider.INSTANCE.getColorMap(Hue.TORCH, Chroma.PURE_NETURAL, Luminance.BRILLIANT));
        model.setFullBrightness(PaintLayer.BASE, true);
        model.setTexture(PaintLayer.MIDDLE, tex);
        model.setColorMap(PaintLayer.MIDDLE, BlockColorMapProvider.INSTANCE.getColorMap(Hue.COBALT, Chroma.NEUTRAL, Luminance.MEDIUM_DARK));
        model.setMiddleLayerEnabled(true);
        
        return new CoolingBasaltBlock(name, BlockSubstance.BASALT, model, isFiller).setAllowSilkHarvest(false);
    }
    
    public static void init(FMLInitializationEvent event) 
    {   
        if(Configurator.VOLCANO.enableVolcano)
        {
            // these have to be in init so that object holders are populated
            ((SuperBlock)ModBlocks.basalt_cut).setDropItem(ModItems.basalt_cobble);
    
            ((SuperBlock)ModBlocks.basalt_cool_dynamic_height).setDropItem(ModItems.basalt_rubble);
            ((SuperBlock)ModBlocks.basalt_cool_dynamic_filler).setDropItem(ModItems.basalt_rubble);
            ((SuperBlock)ModBlocks.basalt_cool_static_height).setDropItem(ModItems.basalt_rubble);
            ((SuperBlock)ModBlocks.basalt_cool_static_filler).setDropItem(ModItems.basalt_rubble);
            
            ((SuperBlock)ModBlocks.basalt_dynamic_cooling_height).setDropItem(ModItems.basalt_rubble);
            ((SuperBlock)ModBlocks.basalt_dynamic_cooling_filler).setDropItem(ModItems.basalt_rubble);
            ((SuperBlock)ModBlocks.basalt_dynamic_warm_height).setDropItem(ModItems.basalt_rubble);
            ((SuperBlock)ModBlocks.basalt_dynamic_warm_filler).setDropItem(ModItems.basalt_rubble);
            ((SuperBlock)ModBlocks.basalt_dynamic_hot_height).setDropItem(ModItems.basalt_rubble);
            ((SuperBlock)ModBlocks.basalt_dynamic_hot_filler).setDropItem(ModItems.basalt_rubble);
            ((SuperBlock)ModBlocks.basalt_dynamic_very_hot_height).setDropItem(ModItems.basalt_rubble);
            ((SuperBlock)ModBlocks.basalt_dynamic_very_hot_filler).setDropItem(ModItems.basalt_rubble);
            
            ((CoolingBasaltBlock)ModBlocks.basalt_dynamic_cooling_height).setCoolingBlockInfo((TerrainDynamicBlock) ModBlocks.basalt_cool_dynamic_height, 1);
            ((CoolingBasaltBlock)ModBlocks.basalt_dynamic_cooling_filler).setCoolingBlockInfo((TerrainDynamicBlock) ModBlocks.basalt_cool_dynamic_filler, 1);
            ((CoolingBasaltBlock)ModBlocks.basalt_dynamic_warm_height).setCoolingBlockInfo((TerrainDynamicBlock) ModBlocks.basalt_dynamic_cooling_height, 2);
            ((CoolingBasaltBlock)ModBlocks.basalt_dynamic_warm_filler).setCoolingBlockInfo((TerrainDynamicBlock) ModBlocks.basalt_dynamic_cooling_filler, 2);
            ((CoolingBasaltBlock)ModBlocks.basalt_dynamic_hot_height).setCoolingBlockInfo((TerrainDynamicBlock) ModBlocks.basalt_dynamic_warm_height, 3);
            ((CoolingBasaltBlock)ModBlocks.basalt_dynamic_hot_filler).setCoolingBlockInfo((TerrainDynamicBlock) ModBlocks.basalt_dynamic_warm_filler, 3);
            ((CoolingBasaltBlock)ModBlocks.basalt_dynamic_very_hot_height).setCoolingBlockInfo((TerrainDynamicBlock) ModBlocks.basalt_dynamic_hot_height, 4);
            ((CoolingBasaltBlock)ModBlocks.basalt_dynamic_very_hot_filler).setCoolingBlockInfo((TerrainDynamicBlock) ModBlocks.basalt_dynamic_hot_filler, 4);
        }
    }
}
