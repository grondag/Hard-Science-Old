package grondag.hard_science.init;

import grondag.hard_science.HardScience;
import grondag.hard_science.machines.BlockFabricatorMachine;
import grondag.hard_science.machines.BottomBusMachine;
import grondag.hard_science.machines.MiddleBusMachine;
import grondag.hard_science.machines.ModularTankMachine;
import grondag.hard_science.machines.SmartChestMachine;
import grondag.hard_science.machines.SolarAggregatorMachine;
import grondag.hard_science.machines.SolarCableMachine;
import grondag.hard_science.machines.PhotoElectricMachine;
import grondag.hard_science.machines.TopBusMachine;
import grondag.hard_science.machines.TransportTestMachine;
import grondag.hard_science.machines.WaterPumpMachine;
import grondag.hard_science.simulator.device.DeviceManager;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ModDevices
{
    public static void preInit(FMLPreInitializationEvent event) 
    {
        DeviceManager.register(HardScience.prefixResource("transport_test"), TransportTestMachine.class);
        DeviceManager.register(HardScience.prefixResource("block_fabricator"), BlockFabricatorMachine.class);
        DeviceManager.register(HardScience.prefixResource("smart_chest"), SmartChestMachine.class);
        DeviceManager.register(HardScience.prefixResource("solar_aggregator"), SolarAggregatorMachine.class);
        DeviceManager.register(HardScience.prefixResource("solar_cell"), PhotoElectricMachine.class);
        DeviceManager.register(HardScience.prefixResource("solar_cable"), SolarCableMachine.class);
        DeviceManager.register(HardScience.prefixResource("bottom_bus"), BottomBusMachine.class);
        DeviceManager.register(HardScience.prefixResource("middle_bus"), MiddleBusMachine.class);
        DeviceManager.register(HardScience.prefixResource("top_bus"), TopBusMachine.class);
        DeviceManager.register(HardScience.prefixResource("water_pump"), WaterPumpMachine.class);
        DeviceManager.register(HardScience.prefixResource("modular_tank"), ModularTankMachine.class);
    }
}
