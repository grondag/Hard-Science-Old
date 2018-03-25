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
import grondag.hard_science.machines.impl.processing.DigesterMachine;
import grondag.hard_science.machines.impl.processing.MicronizerMachine;
import grondag.hard_science.machines.impl.production.PhotoElectricMachine;
import grondag.hard_science.machines.impl.production.SolarCableMachine;
import grondag.hard_science.simulator.device.DeviceManager;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ModDevices
{
    public static void preInit(FMLPreInitializationEvent event) 
    {
        DeviceManager.register(HardScience.INSTANCE.prefixResource("block_fabricator"), BlockFabricatorMachine.class);
        DeviceManager.register(HardScience.INSTANCE.prefixResource("smart_chest"), SmartChestMachine.Flexible.class);
        DeviceManager.register(HardScience.INSTANCE.prefixResource("smart_bin"), SmartChestMachine.Dedicated.class);
        DeviceManager.register(HardScience.INSTANCE.prefixResource("solar_cell"), PhotoElectricMachine.class);
        DeviceManager.register(HardScience.INSTANCE.prefixResource("solar_cable"), SolarCableMachine.class);
        DeviceManager.register(HardScience.INSTANCE.prefixResource("bottom_bus"), BottomBusMachine.class);
        DeviceManager.register(HardScience.INSTANCE.prefixResource("middle_bus"), MiddleBusMachine.class);
        DeviceManager.register(HardScience.INSTANCE.prefixResource("top_bus"), TopBusMachine.class);
        DeviceManager.register(HardScience.INSTANCE.prefixResource("water_pump"), WaterPumpMachine.class);
        DeviceManager.register(HardScience.INSTANCE.prefixResource("flexible_tank"), TankMachine.Flexible.class);
        DeviceManager.register(HardScience.INSTANCE.prefixResource("dedicated_tank"), TankMachine.Dedicated.class);
        DeviceManager.register(HardScience.INSTANCE.prefixResource("chemical_battery"), ChemicalBatteryMachine.class);
        DeviceManager.register(HardScience.INSTANCE.prefixResource("micronizer"), MicronizerMachine.class);
        DeviceManager.register(HardScience.INSTANCE.prefixResource("digester"), DigesterMachine.class);
    }
}
