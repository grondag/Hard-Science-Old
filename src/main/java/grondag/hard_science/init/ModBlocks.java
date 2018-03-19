package grondag.hard_science.init;

import grondag.exotic_matter.model.BlockColorMapProvider;
import grondag.exotic_matter.model.Chroma;
import grondag.exotic_matter.model.Hue;
import grondag.exotic_matter.model.ISuperBlock;
import grondag.exotic_matter.model.ISuperModelState;
import grondag.exotic_matter.model.ITexturePalette;
import grondag.exotic_matter.model.Luminance;
import grondag.exotic_matter.model.ModShapes;
import grondag.exotic_matter.model.PaintLayer;
import grondag.exotic_matter.model.TerrainBlockRegistry;
import grondag.hard_science.Configurator;
import grondag.hard_science.HardScience;
import grondag.hard_science.machines.impl.building.BlockFabricatorBlock;
import grondag.hard_science.machines.impl.logistics.BottomBusBlock;
import grondag.hard_science.machines.impl.logistics.ChemicalBatteryBlock;
import grondag.hard_science.machines.impl.logistics.MiddleBusBlock;
import grondag.hard_science.machines.impl.logistics.ModularTankBlock;
import grondag.hard_science.machines.impl.logistics.SmartChestBlock;
import grondag.hard_science.machines.impl.logistics.TopBusBlock;
import grondag.hard_science.machines.impl.logistics.WaterPumpBlock;
import grondag.hard_science.machines.impl.processing.DigesterBlock;
import grondag.hard_science.machines.impl.processing.MicronizerBlock;
import grondag.hard_science.machines.impl.production.PhotoElectricBlock;
import grondag.hard_science.machines.impl.production.SolarCableBlock;
import grondag.hard_science.superblock.block.SuperSimpleBlock;
import grondag.hard_science.superblock.model.state.ModelState;
import grondag.hard_science.superblock.terrain.DepletedFluidBlock;
import grondag.hard_science.superblock.terrain.TerrainCubicBlock;
import grondag.hard_science.superblock.terrain.TerrainDynamicBlock;
import grondag.hard_science.superblock.terrain.TerrainStaticBlock;
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
    public static final Block micronizer = null;
    public static final Block digester = null;
    
//    public static final Block solar_aggregator = null;
    
    
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) 
    {
        ISuperModelState workingModel;
        
        workingModel = new ModelState();
        
        workingModel = new ModelState();
        workingModel.setShape(ModShapes.CUBE);
        workingModel.setTexture(PaintLayer.BASE, ModTextures.BLOCK_COBBLE);
        workingModel.setColorMap(PaintLayer.BASE, BlockColorMapProvider.COLOR_BASALT);
        event.getRegistry().register(new SuperSimpleBlock("basalt_cobble", ModSubstances.BASALT, workingModel));

        if(Configurator.VOLCANO.enableVolcano)
        {
            event.getRegistry().register(new VolcanoBlock());
            
            
            workingModel = new ModelState();
            workingModel.setShape(ModShapes.TERRAIN_HEIGHT);
            workingModel.setTexture(PaintLayer.BASE, ModTextures.BIGTEX_BASALT_COOL_ZOOM);
            workingModel.setColorMap(PaintLayer.BASE, BlockColorMapProvider.COLOR_BASALT);
            workingModel.setTexture(PaintLayer.CUT, ModTextures.BIGTEX_BASALT_CUT);
            workingModel.setColorMap(PaintLayer.CUT, BlockColorMapProvider.COLOR_BASALT);
            
            Block dynamicBasaltHeight = new TerrainDynamicBlock("basalt_cool_dynamic_height", ModSubstances.BASALT, workingModel.clone(), false);
            Block staticBasaltHeight = new TerrainStaticBlock("basalt_cool_static_height", ModSubstances.BASALT, workingModel.clone(), false);
    
            event.getRegistry().register(dynamicBasaltHeight);
            event.getRegistry().register(staticBasaltHeight);
    
            workingModel = workingModel.clone();
            workingModel.setShape(ModShapes.TERRAIN_FILLER);
    
            Block dynamicBasaltFiller = new TerrainDynamicBlock("basalt_cool_dynamic_filler", ModSubstances.BASALT, workingModel.clone(), true);
            Block staticBasaltFiller = new TerrainStaticBlock("basalt_cool_static_filler", ModSubstances.BASALT, workingModel.clone(), true);
    
            event.getRegistry().register(dynamicBasaltFiller);
            event.getRegistry().register(staticBasaltFiller);
            
            TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.registerFiller(dynamicBasaltHeight, dynamicBasaltFiller);
            TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.registerFiller(staticBasaltHeight, staticBasaltFiller);
            
            TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.registerStateTransition(dynamicBasaltHeight, staticBasaltHeight);
            TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.registerStateTransition(dynamicBasaltFiller, staticBasaltFiller);
            
            workingModel = new ModelState();
            workingModel.setShape(ModShapes.CUBE);
            workingModel.setTexture(PaintLayer.BASE, ModTextures.BIGTEX_BASALT_CUT_ZOOM);
            workingModel.setColorMap(PaintLayer.BASE, BlockColorMapProvider.COLOR_BASALT);
            Block cubicBasalt  = new TerrainCubicBlock("basalt_cut", ModSubstances.BASALT, workingModel.clone());
            event.getRegistry().register(cubicBasalt);
            
            TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.registerCubic(dynamicBasaltHeight, cubicBasalt);
            TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.registerCubic(dynamicBasaltFiller, cubicBasalt);
            TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.registerCubic(staticBasaltHeight, cubicBasalt);
            TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.registerCubic(staticBasaltFiller, cubicBasalt);
            
            
            workingModel = new ModelState();
            workingModel.setShape(ModShapes.TERRAIN_HEIGHT);
            workingModel.setTexture(PaintLayer.BASE, ModTextures.BIGTEX_LAVA);
            workingModel.setColorMap(PaintLayer.BASE, BlockColorMapProvider.INSTANCE.getColorMap(Hue.RED, Chroma.WHITE, Luminance.BRILLIANT));
            workingModel.setFullBrightness(PaintLayer.BASE, true);
            workingModel.setTexture(PaintLayer.MIDDLE, ModTextures.BIGTEX_BASALT_HINT);
            workingModel.setColorMap(PaintLayer.MIDDLE, BlockColorMapProvider.INSTANCE.getColorMap(Hue.VERMILLION, Chroma.ULTRA_ACCENT, Luminance.MEDIUM_DARK));
            workingModel.setFullBrightness(PaintLayer.MIDDLE, false);
            
            Block dynamicLavaHeight = new LavaBlock("lava_dynamic_height", ModSubstances.VOLCANIC_LAVA, workingModel, false);
            
            workingModel = workingModel.clone();
            workingModel.setShape(ModShapes.TERRAIN_FILLER);
            Block dynamicLavaFiller = new LavaBlock("lava_dynamic_filler", ModSubstances.VOLCANIC_LAVA, workingModel, true);
    
            event.getRegistry().register(dynamicLavaHeight);
            event.getRegistry().register(dynamicLavaFiller);
    
            TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.registerFiller(dynamicLavaHeight, dynamicLavaFiller);
            
            // COOLING BASALT
            Block dynamicCoolingBasaltHeight = makeCoolingBasalt("basalt_dynamic_cooling_height", ModTextures.BIGTEX_BASALT_COOLING, false);
            Block dynamicCoolingBasaltFiller = makeCoolingBasalt("basalt_dynamic_cooling_filler", ModTextures.BIGTEX_BASALT_COOLING, true);        
            event.getRegistry().register(dynamicCoolingBasaltHeight);
            event.getRegistry().register(dynamicCoolingBasaltFiller);
            TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.registerFiller(dynamicCoolingBasaltHeight, dynamicCoolingBasaltFiller);
    
            // WARM BASALT
            Block dynamicWarmBasaltHeight = makeCoolingBasalt("basalt_dynamic_warm_height", ModTextures.BIGTEX_BASALT_WARM, false);
            Block dynamicWarmBasaltFiller = makeCoolingBasalt("basalt_dynamic_warm_filler", ModTextures.BIGTEX_BASALT_WARM, true);        
            event.getRegistry().register(dynamicWarmBasaltHeight);
            event.getRegistry().register(dynamicWarmBasaltFiller);
            TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.registerFiller(dynamicWarmBasaltHeight, dynamicWarmBasaltFiller);
            
            // HOT BASALT
            Block dynamicHotBasaltHeight = makeCoolingBasalt("basalt_dynamic_hot_height", ModTextures.BIGTEX_BASALT_HOT, false);
            Block dynamicHotBasaltFiller = makeCoolingBasalt("basalt_dynamic_hot_filler", ModTextures.BIGTEX_BASALT_HOT, true);        
            event.getRegistry().register(dynamicHotBasaltHeight);
            event.getRegistry().register(dynamicHotBasaltFiller);
            TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.registerFiller(dynamicHotBasaltHeight, dynamicHotBasaltFiller);
            
            // VERY HOT BASALT
            Block dynamicVeryHotBasaltHeight = makeCoolingBasalt("basalt_dynamic_very_hot_height", ModTextures.BIGTEX_BASALT_VERY_HOT, false);
            Block dynamicVeryHotBasaltFiller = makeCoolingBasalt("basalt_dynamic_very_hot_filler", ModTextures.BIGTEX_BASALT_VERY_HOT, true);        
            event.getRegistry().register(dynamicVeryHotBasaltHeight);
            event.getRegistry().register(dynamicVeryHotBasaltFiller);
            TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.registerFiller(dynamicVeryHotBasaltHeight, dynamicVeryHotBasaltFiller);
        }
        
        event.getRegistry().register(new DepletedFluidBlock()
                .setRegistryName("depleted_fluid")
                .setUnlocalizedName("depeleted_fluid"));
        
        // MACHINE BLOCKS
        event.getRegistry().register(new SmartChestBlock("smart_chest", false));
        event.getRegistry().register(new SmartChestBlock("smart_bin", true));
        event.getRegistry().register(new BlockFabricatorBlock("block_fabricator"));
        event.getRegistry().register(new PhotoElectricBlock("solar_cell"));
        event.getRegistry().register(new SolarCableBlock("solar_cable"));
        event.getRegistry().register(new BottomBusBlock("bottom_bus"));
        event.getRegistry().register(new MiddleBusBlock("middle_bus"));
        event.getRegistry().register(new TopBusBlock("top_bus"));
        event.getRegistry().register(new ModularTankBlock("tank_single_wet", 100, true, ModFluids.PREDICATE_NORMAL_FLUIDS));
        event.getRegistry().register(new ModularTankBlock("tank_multi_wet", 50, false, ModFluids.PREDICATE_NORMAL_FLUIDS));
        event.getRegistry().register(new ModularTankBlock("tank_single_dry", 100, true, ModFluids.PREDICATE_BULK_SOLIDS));
        event.getRegistry().register(new ModularTankBlock("tank_multi_dry", 50, false, ModFluids.PREDICATE_BULK_SOLIDS));
        event.getRegistry().register(new ModularTankBlock("tank_single_gas", 100, true, ModFluids.PREDICATE_LIGHT_PRESSURE));
        event.getRegistry().register(new ModularTankBlock("tank_multi_gas", 50, false, ModFluids.PREDICATE_LIGHT_PRESSURE));
        event.getRegistry().register(new ModularTankBlock("tank_single_pressure", 50, true, ModFluids.PREDICATE_HIGH_PRESSURE));
        event.getRegistry().register(new WaterPumpBlock("water_pump"));
        event.getRegistry().register(new ChemicalBatteryBlock("chemical_battery"));
        event.getRegistry().register(new MicronizerBlock("micronizer"));
        event.getRegistry().register(new DigesterBlock("digester"));
    }
    
    private static Block makeCoolingBasalt(String name, ITexturePalette tex, boolean  isFiller) 
    {
        ISuperModelState model = new ModelState();
        model.setShape(isFiller ? ModShapes.TERRAIN_FILLER : ModShapes.TERRAIN_HEIGHT);
        model.setTexture(PaintLayer.BASE, ModTextures.BIGTEX_LAVA);
        model.setColorMap(PaintLayer.BASE, BlockColorMapProvider.INSTANCE.getColorMap(Hue.TORCH, Chroma.PURE_NETURAL, Luminance.BRILLIANT));
        model.setFullBrightness(PaintLayer.BASE, true);
        model.setTexture(PaintLayer.MIDDLE, tex);
        model.setColorMap(PaintLayer.MIDDLE, BlockColorMapProvider.INSTANCE.getColorMap(Hue.COBALT, Chroma.NEUTRAL, Luminance.MEDIUM_DARK));
        model.setMiddleLayerEnabled(true);
        
        return new CoolingBasaltBlock(name, ModSubstances.BASALT, model, isFiller).setAllowSilkHarvest(false);
    }
    
    public static void init(FMLInitializationEvent event) 
    {   
        if(Configurator.VOLCANO.enableVolcano)
        {
            // these have to be in init so that object holders are populated
            ((ISuperBlock)ModBlocks.basalt_cut).setDropItem(ModItems.basalt_cobble);
    
            ((ISuperBlock)ModBlocks.basalt_cool_dynamic_height).setDropItem(ModItems.basalt_rubble);
            ((ISuperBlock)ModBlocks.basalt_cool_dynamic_filler).setDropItem(ModItems.basalt_rubble);
            ((ISuperBlock)ModBlocks.basalt_cool_static_height).setDropItem(ModItems.basalt_rubble);
            ((ISuperBlock)ModBlocks.basalt_cool_static_filler).setDropItem(ModItems.basalt_rubble);
            
            ((ISuperBlock)ModBlocks.basalt_dynamic_cooling_height).setDropItem(ModItems.basalt_rubble);
            ((ISuperBlock)ModBlocks.basalt_dynamic_cooling_filler).setDropItem(ModItems.basalt_rubble);
            ((ISuperBlock)ModBlocks.basalt_dynamic_warm_height).setDropItem(ModItems.basalt_rubble);
            ((ISuperBlock)ModBlocks.basalt_dynamic_warm_filler).setDropItem(ModItems.basalt_rubble);
            ((ISuperBlock)ModBlocks.basalt_dynamic_hot_height).setDropItem(ModItems.basalt_rubble);
            ((ISuperBlock)ModBlocks.basalt_dynamic_hot_filler).setDropItem(ModItems.basalt_rubble);
            ((ISuperBlock)ModBlocks.basalt_dynamic_very_hot_height).setDropItem(ModItems.basalt_rubble);
            ((ISuperBlock)ModBlocks.basalt_dynamic_very_hot_filler).setDropItem(ModItems.basalt_rubble);
            
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
