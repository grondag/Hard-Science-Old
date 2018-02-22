package grondag.hard_science.init;

import grondag.hard_science.HardScience;
import grondag.hard_science.simulator.transport.endpoint.PortFaces;
import grondag.hard_science.simulator.transport.endpoint.PortLayout;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@Mod.EventBusSubscriber
@ObjectHolder(HardScience.MODID)
public class ModPortLayouts
{
    
    public static final PortLayout empty = null;
    /**
     * Includes compact power on sides.
     */
    public static final PortLayout utb_low_carrier_all = null;
    public static final PortLayout utb_mid_carrier_all = null;
    public static final PortLayout utb_mid_bridge_all = null;
    public static final PortLayout utb_top_carrier_all = null;
    public static final PortLayout utb_top_bridge_all = null;
    /**
     * Standard power on bottom and compact power on sides.
     */
    public static final PortLayout solar_cell = null;
    
    /**
     * For full-block batteries or power-only machines.
     */
    public static final PortLayout power_low_carrier_flex_all = null;
    
    /**
     * For full-block machines needing only power and item transport.
     */
    public static final PortLayout non_fluid_low_carrier_all = null;
    
    
    @SubscribeEvent
    public static void registerLayouts(RegistryEvent.Register<PortLayout> event) 
    {
        event.getRegistry().register(new PortLayout(
                ModRegistries.EMPTY_KEY,
                PortFaces.EMPTY_FACE,
                PortFaces.EMPTY_FACE,
                PortFaces.EMPTY_FACE,
                PortFaces.EMPTY_FACE,
                PortFaces.EMPTY_FACE,
                PortFaces.EMPTY_FACE));

        event.getRegistry().register(new PortLayout(
                HardScience.prefixResource("utb_low_carrier_all"),
                PortFaces.UTB_LOW_CARRIER,
                PortFaces.UTB_LOW_CARRIER,
                PortFaces.UTB_LOW_CARRIER_WITH_COMPACT_POWER,
                PortFaces.UTB_LOW_CARRIER_WITH_COMPACT_POWER,
                PortFaces.UTB_LOW_CARRIER_WITH_COMPACT_POWER,
                PortFaces.UTB_LOW_CARRIER_WITH_COMPACT_POWER));
        
        event.getRegistry().register(new PortLayout(
                HardScience.prefixResource("utb_mid_bridge_all"),
                PortFaces.UTB_MID_BRIDGE,
                PortFaces.UTB_MID_BRIDGE,
                PortFaces.UTB_MID_BRIDGE,
                PortFaces.UTB_MID_BRIDGE,
                PortFaces.UTB_MID_BRIDGE,
                PortFaces.UTB_MID_BRIDGE));
        
        event.getRegistry().register(new PortLayout(
                HardScience.prefixResource("utb_mid_carrier_all"),
                PortFaces.UTB_MID_CARRIER,
                PortFaces.UTB_MID_CARRIER,
                PortFaces.UTB_MID_CARRIER,
                PortFaces.UTB_MID_CARRIER,
                PortFaces.UTB_MID_CARRIER,
                PortFaces.UTB_MID_CARRIER));
        
        event.getRegistry().register(new PortLayout(
                HardScience.prefixResource("utb_top_bridge_all"),
                PortFaces.UTB_TOP_BRIDGE,
                PortFaces.UTB_TOP_BRIDGE,
                PortFaces.UTB_TOP_BRIDGE,
                PortFaces.UTB_TOP_BRIDGE,
                PortFaces.UTB_TOP_BRIDGE,
                PortFaces.UTB_TOP_BRIDGE));
        
        event.getRegistry().register(new PortLayout(
                HardScience.prefixResource("utb_top_carrier_all"),
                PortFaces.UTB_TOP_CARRIER,
                PortFaces.UTB_TOP_CARRIER,
                PortFaces.UTB_TOP_CARRIER,
                PortFaces.UTB_TOP_CARRIER,
                PortFaces.UTB_TOP_CARRIER,
                PortFaces.UTB_TOP_CARRIER));
        
        event.getRegistry().register(new PortLayout(
                HardScience.prefixResource("solar_cell"),
                PortFaces.EMPTY_FACE,
                PortFaces.STD_POWER_LOW_CARRIER,
                PortFaces.COMPACT_POWER_LOW_CARRIER,
                PortFaces.COMPACT_POWER_LOW_CARRIER,
                PortFaces.COMPACT_POWER_LOW_CARRIER,
                PortFaces.COMPACT_POWER_LOW_CARRIER));
        
        event.getRegistry().register(new PortLayout(
                HardScience.prefixResource("power_low_carrier_flex_all"),
                PortFaces.FLEX_POWER_LOW_CARRIER,
                PortFaces.FLEX_POWER_LOW_CARRIER,
                PortFaces.FLEX_POWER_LOW_CARRIER,
                PortFaces.FLEX_POWER_LOW_CARRIER,
                PortFaces.FLEX_POWER_LOW_CARRIER,
                PortFaces.FLEX_POWER_LOW_CARRIER));
        
        event.getRegistry().register(new PortLayout(
                HardScience.prefixResource("non_fluid_low_carrier_all"),
                PortFaces.NON_FLUID_LOW_CARRIER,
                PortFaces.NON_FLUID_LOW_CARRIER,
                PortFaces.NON_FLUID_LOW_CARRIER,
                PortFaces.NON_FLUID_LOW_CARRIER,
                PortFaces.NON_FLUID_LOW_CARRIER,
                PortFaces.NON_FLUID_LOW_CARRIER));
        
    }
}
