package grondag.hard_science.init;

import grondag.hard_science.HardScience;
import grondag.hard_science.machines.BlockFabricatorMachine;
import grondag.hard_science.machines.BottomBusMachine;
import grondag.hard_science.machines.WaterPumpMachine;
import grondag.hard_science.machines.MiddleBusMachine;
import grondag.hard_science.machines.ModularTankMachine;
import grondag.hard_science.machines.SmartChestMachine;
import grondag.hard_science.machines.SolarAggregatorMachine;
import grondag.hard_science.machines.SolarCableMachine;
import grondag.hard_science.machines.SolarCellMachine;
import grondag.hard_science.machines.TopBusMachine;
import grondag.hard_science.machines.TransportTestMachine;
import grondag.hard_science.simulator.device.DeviceManager;
import grondag.hard_science.simulator.storage.ItemStorage;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ModDevices
{
    public static void preInit(FMLPreInitializationEvent event) 
    {
        DeviceManager.register(HardScience.MODID + ":transport_test", TransportTestMachine.class);
        DeviceManager.register(HardScience.MODID + ":item_storage", ItemStorage.class);
        DeviceManager.register(HardScience.MODID + ":block_fabricator", BlockFabricatorMachine.class);
        DeviceManager.register(HardScience.MODID + ":smart_chest", SmartChestMachine.class);
        DeviceManager.register(HardScience.MODID + ":solar_aggregator", SolarAggregatorMachine.class);
        DeviceManager.register(HardScience.MODID + ":solar_cell", SolarCellMachine.class);
        DeviceManager.register(HardScience.MODID + ":solar_cable", SolarCableMachine.class);
        DeviceManager.register(HardScience.MODID + ":bottom_bus", BottomBusMachine.class);
        DeviceManager.register(HardScience.MODID + ":middle_bus", MiddleBusMachine.class);
        DeviceManager.register(HardScience.MODID + ":top_bus", TopBusMachine.class);
        DeviceManager.register(HardScience.MODID + ":water_pump", WaterPumpMachine.class);
        DeviceManager.register(HardScience.MODID + ":modular_tank", ModularTankMachine.class);
        
    }
}
