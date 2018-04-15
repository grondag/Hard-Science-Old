package grondag.hard_science.init;

import grondag.exotic_matter.terrain.DepletedFluidBlock;
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
import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@Mod.EventBusSubscriber
@ObjectHolder(HardScience.MODID)
@SuppressWarnings("null")
public class ModBlocks
{
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
        
        event.getRegistry().register(new DepletedFluidBlock()
                .setRegistryName("depleted_fluid")
                .setUnlocalizedName("depeleted_fluid")
                .setCreativeTab(HardScience.tabMod));
        
        // MACHINE BLOCKS
        event.getRegistry().register(new SmartChestBlock("smart_chest", false).setCreativeTab(HardScience.tabMod));
        event.getRegistry().register(new SmartChestBlock("smart_bin", true).setCreativeTab(HardScience.tabMod));
        event.getRegistry().register(new BlockFabricatorBlock("block_fabricator").setCreativeTab(HardScience.tabMod));
        event.getRegistry().register(new PhotoElectricBlock("solar_cell").setCreativeTab(HardScience.tabMod));
        event.getRegistry().register(new SolarCableBlock("solar_cable").setCreativeTab(HardScience.tabMod));
        event.getRegistry().register(new BottomBusBlock("bottom_bus").setCreativeTab(HardScience.tabMod));
        event.getRegistry().register(new MiddleBusBlock("middle_bus").setCreativeTab(HardScience.tabMod));
        event.getRegistry().register(new TopBusBlock("top_bus").setCreativeTab(HardScience.tabMod));
        event.getRegistry().register(new ModularTankBlock("tank_single_wet", 100, true, ModFluids.PREDICATE_NORMAL_FLUIDS).setCreativeTab(HardScience.tabMod));
        event.getRegistry().register(new ModularTankBlock("tank_multi_wet", 50, false, ModFluids.PREDICATE_NORMAL_FLUIDS).setCreativeTab(HardScience.tabMod));
        event.getRegistry().register(new ModularTankBlock("tank_single_dry", 100, true, ModFluids.PREDICATE_BULK_SOLIDS).setCreativeTab(HardScience.tabMod));
        event.getRegistry().register(new ModularTankBlock("tank_multi_dry", 50, false, ModFluids.PREDICATE_BULK_SOLIDS).setCreativeTab(HardScience.tabMod));
        event.getRegistry().register(new ModularTankBlock("tank_single_gas", 100, true, ModFluids.PREDICATE_LIGHT_PRESSURE).setCreativeTab(HardScience.tabMod));
        event.getRegistry().register(new ModularTankBlock("tank_multi_gas", 50, false, ModFluids.PREDICATE_LIGHT_PRESSURE).setCreativeTab(HardScience.tabMod));
        event.getRegistry().register(new ModularTankBlock("tank_single_pressure", 50, true, ModFluids.PREDICATE_HIGH_PRESSURE).setCreativeTab(HardScience.tabMod));
        event.getRegistry().register(new WaterPumpBlock("water_pump").setCreativeTab(HardScience.tabMod));
        event.getRegistry().register(new ChemicalBatteryBlock("chemical_battery").setCreativeTab(HardScience.tabMod));
        event.getRegistry().register(new MicronizerBlock("micronizer").setCreativeTab(HardScience.tabMod));
        event.getRegistry().register(new DigesterBlock("digester").setCreativeTab(HardScience.tabMod));
    }
    
    
    public static void init(FMLInitializationEvent event) 
    {
        
    }
}
