package grondag.hard_science.init;

import grondag.hard_science.HardScience;
import grondag.hard_science.machines.BlockFabricatorMachine;
import grondag.hard_science.machines.SmartChestMachine;
import grondag.hard_science.machines.SolarAggregatorMachine;
import grondag.hard_science.simulator.device.DeviceManager;
import grondag.hard_science.simulator.device.impl.TestDevice;
import grondag.hard_science.simulator.storage.ItemStorage;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ModDevices
{
    public static void preInit(FMLPreInitializationEvent event) 
    {
        DeviceManager.register(HardScience.MODID + ":test_device", TestDevice.class);
        DeviceManager.register(HardScience.MODID + ":item_storage", ItemStorage.class);
        DeviceManager.register(HardScience.MODID + ":block_fabricator", BlockFabricatorMachine.class);
        DeviceManager.register(HardScience.MODID + ":smart_chest", SmartChestMachine.class);
        DeviceManager.register(HardScience.MODID + ":solar_aggregator", SolarAggregatorMachine.class);
    }
}
