package grondag.hard_science.init;

import grondag.hard_science.HardScience;
import grondag.hard_science.simulator.resource.FluidResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.carrier.Channel;
import grondag.hard_science.simulator.transport.endpoint.PortConnector;
import grondag.hard_science.simulator.transport.endpoint.PortDescription;
import grondag.hard_science.simulator.transport.endpoint.PortFace;
import grondag.hard_science.simulator.transport.endpoint.PortFaces;
import grondag.hard_science.simulator.transport.endpoint.PortFunction;
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
    public static final PortLayout power_low_carrier_all = null;
    
    /**
     * For full-block machines needing only power and item transport.
     */
    public static final PortLayout non_fluid_low_carrier_all = null;
    
    /**
     * For water pump.
     */
    public static final PortLayout water_and_power_low = null;
    
    @SubscribeEvent
    public static void registerLayouts(RegistryEvent.Register<PortLayout> event) 
    {
        for(FluidResource f : ModFluids.ALL_USED_FLUIDS)
        {
            PortDescription<StorageTypeFluid> desc = PortDescription.find(
                    StorageType.FLUID, 
                    CarrierLevel.BOTTOM, 
                    PortFunction.CARRIER, 
                    PortConnector.STANDARD, 
                    Channel.channelForFluid(f));
            PortFace face = PortFace.find(desc);
            PortLayout layout = new PortLayout(f.getFluid().getName(),
                    face, face, face, face, face, face);
            event.getRegistry().register(layout);
        }
        
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
                HardScience.prefixResource("power_low_carrier_all"),
                PortFaces.STD_POWER_LOW_CARRIER,
                PortFaces.STD_POWER_LOW_CARRIER,
                PortFaces.STD_POWER_LOW_CARRIER,
                PortFaces.STD_POWER_LOW_CARRIER,
                PortFaces.STD_POWER_LOW_CARRIER,
                PortFaces.STD_POWER_LOW_CARRIER));
        
        event.getRegistry().register(new PortLayout(
                HardScience.prefixResource("non_fluid_low_carrier_all"),
                PortFaces.NON_FLUID_LOW_CARRIER,
                PortFaces.NON_FLUID_LOW_CARRIER,
                PortFaces.NON_FLUID_LOW_CARRIER,
                PortFaces.NON_FLUID_LOW_CARRIER,
                PortFaces.NON_FLUID_LOW_CARRIER,
                PortFaces.NON_FLUID_LOW_CARRIER));
        
        event.getRegistry().register(new PortLayout(
                HardScience.prefixResource("water_and_power_low"),
                PortFaces.STD_WATER_AND_POWER_LOW_CARRIER,
                PortFaces.STD_WATER_AND_POWER_LOW_CARRIER,
                PortFaces.STD_WATER_AND_POWER_LOW_CARRIER,
                PortFaces.STD_WATER_AND_POWER_LOW_CARRIER,
                PortFaces.STD_WATER_AND_POWER_LOW_CARRIER,
                PortFaces.STD_WATER_AND_POWER_LOW_CARRIER));
    }
}
