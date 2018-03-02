package grondag.hard_science.init;

import grondag.hard_science.HardScience;
import grondag.hard_science.machines.impl.building.BlockFabricatorMachine;
import grondag.hard_science.machines.impl.logistics.BottomBusMachine;
import grondag.hard_science.machines.impl.logistics.ChemicalBatteryMachine;
import grondag.hard_science.machines.impl.logistics.MiddleBusMachine;
import grondag.hard_science.machines.impl.logistics.TankMachine;
import grondag.hard_science.machines.impl.logistics.SmartChestMachine;
import grondag.hard_science.machines.impl.logistics.TopBusMachine;
import grondag.hard_science.machines.impl.logistics.WaterPumpMachine;
import grondag.hard_science.machines.impl.processing.MicronizerMachine;
import grondag.hard_science.machines.impl.production.PhotoElectricMachine;
import grondag.hard_science.machines.impl.production.SolarCableMachine;
import grondag.hard_science.simulator.device.DeviceManager;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ModDevices
{
    public static void preInit(FMLPreInitializationEvent event) 
    {
        DeviceManager.register(HardScience.prefixResource("block_fabricator"), BlockFabricatorMachine.class);
        DeviceManager.register(HardScience.prefixResource("smart_chest"), SmartChestMachine.class);
        DeviceManager.register(HardScience.prefixResource("solar_cell"), PhotoElectricMachine.class);
        DeviceManager.register(HardScience.prefixResource("solar_cable"), SolarCableMachine.class);
        DeviceManager.register(HardScience.prefixResource("bottom_bus"), BottomBusMachine.class);
        DeviceManager.register(HardScience.prefixResource("middle_bus"), MiddleBusMachine.class);
        DeviceManager.register(HardScience.prefixResource("top_bus"), TopBusMachine.class);
        DeviceManager.register(HardScience.prefixResource("water_pump"), WaterPumpMachine.class);
        DeviceManager.register(HardScience.prefixResource("portable_tank"), TankMachine.class);
        DeviceManager.register(HardScience.prefixResource("chemical_battery"), ChemicalBatteryMachine.class);
        DeviceManager.register(HardScience.prefixResource("micronizer"), MicronizerMachine.class);
    }
}
