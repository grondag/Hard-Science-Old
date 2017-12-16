package grondag.hard_science.init;

import grondag.hard_science.HardScience;
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
    }
}
