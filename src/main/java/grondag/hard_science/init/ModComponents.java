package grondag.hard_science.init;

import grondag.hard_science.HardScience;
import grondag.hard_science.machines.support.PhotoElectricCell;
import grondag.hard_science.machines.support.PolyethyleneFuelCell;
import grondag.hard_science.simulator.device.ComponentRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Registers device components that can be configured and serialized
 * at run time.  Does NOT necessarily include all device components.
 * Some components are not serialized and/or have other means of 
 * instantiation.
 */
public class ModComponents
{
    public static final ResourceLocation PE_FUEL_CELL = new ResourceLocation(HardScience.MODID, "pe_fuel_cell");
    public static final ResourceLocation PHOTO_ELECTRIC_CELL = new ResourceLocation(HardScience.MODID, "photo_electric_cell");

    public static void preInit(FMLPreInitializationEvent event) 
    {
        ComponentRegistry.register(PE_FUEL_CELL, PolyethyleneFuelCell.class);
        ComponentRegistry.register(PHOTO_ELECTRIC_CELL, PhotoElectricCell.class);
    }
}
