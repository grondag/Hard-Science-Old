package grondag.hard_science.init;

import java.util.HashMap;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.matter.Matter;
import grondag.hard_science.matter.MatterPhase;
import grondag.hard_science.matter.Matters;
import grondag.hard_science.simulator.resource.FluidResource;
import grondag.hard_science.simulator.transport.endpoint.PortLayout;
import jline.internal.Log;
import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ModFluids
{
    
    /**
     * All fluids that should have fluid ports/pipes/tanks, including fluids from vanilla other modes.
     */
    public final static ImmutableList<FluidResource> ALL_PORTED_FLUIDS;
    
    /**
     * Port layouts for low-level fluid carriers. Here because
     * breaks object holders in the port layouts registration class.
     */
    public static final HashMap<FluidResource, PortLayout> FLUID_CARRIERS = new HashMap<FluidResource, PortLayout>();
    
    /**
     * Purified water
     */
//    public static final int WATER_CHANNEL;
//    public static final FluidResource WATER_RESOURCE;
//    
//    public static final Fluid H2O;
//    public static final int H2O_CHANNEL;
//    public static final FluidResource H2O_RESOURCE;
    
//    public static final Fluid AMMONIA;
//    public static final int AMMONIA_CHANNEL;
//    public static final FluidResource AMMONIA_RESOURCE;
//    
//    public static final Fluid FRESH_AIR;
//    public static final int FRESH_AIR_CHANNEL;
//    public static final FluidResource FRESH_AIR_RESOURCE;
//    
//    public static final Fluid RETURN_AIR;
//    public static final int RETURN_AIR_CHANNEL;
//    public static final FluidResource RETURN_AIR_RESOURCE;
//    
//    public static final Fluid OXYGEN_GAS;
//    public static final int OXYGEN_GAS_CHANNEL;
//    public static final FluidResource OXYGEN_GAS_RESOURCE;
//    
//    public static final Fluid HYDROGEN_GAS;
//    public static final int HYDROGEN_GAS_CHANNEL;
//    public static final FluidResource HYDROGEN_GAS_RESOURCE;
//    
//    public static final Fluid ETHENE_GAS;
//    public static final int ETHENE_GAS_CHANNEL;
//    public static final FluidResource ETHENE_GAS_RESOURCE;
//    
//    public static final Fluid FLOWABLE_GRAPHITE;
//    public static final FluidResource FLOWABLE_GRAPHITE_RESOURCE;
//    
//    public static final Fluid FLOWABLE_MINERAL_FILLER;
//    public static final FluidResource FLOWABLE_MINERAL_FILLER_RESOURCE;
//    
//    public static final Fluid RAW_MINERAL_DUST;
//    public static final FluidResource RAW_MINERAL_DUST_RESOURCE;
//    
//    public static final Fluid DEPLETED_MINERAL_DUST;
//    public static final FluidResource DEPLETED_MINERAL_DUST_RESOURCE;
    
//    public static final Fluid OXYGEN;
//    public static final int OXYGEN_CHANNEL;
    
//    public static final Fluid CO2;
//    public static final int CO2_CHANNEL;
    
    static
    {
        ImmutableList.Builder<FluidResource> builder = ImmutableList.builder();
        
        for(Matter m : Matters.all().values())
        {
            m.register();
            if(m.phase() != MatterPhase.SOLID)
            {
                builder.add(m.resource());
            }
        }
//        WATER_CHANNEL = Channel.channelForFluid(FluidRegistry.WATER);
//        WATER_RESOURCE = new FluidResource(FluidRegistry.WATER, null);
//        builder.add(WATER_RESOURCE);
//                
//        H2O = new Fluid("purified_water", liquidResource, liquidResource, MatterColors.WATER);
//        FluidRegistry.registerFluid(H2O);
//        H2O_CHANNEL = Channel.channelForFluid(H2O);
//        H2O_RESOURCE = new FluidResource(H2O, null);
//        builder.add(H2O_RESOURCE);
//
//        AMMONIA = new Fluid("ammonia", gasResource, gasResource, MatterColors.AMMONIA);
//        FluidRegistry.registerFluid(AMMONIA);
//        AMMONIA.setGaseous(true);
//        AMMONIA_CHANNEL = Channel.channelForFluid(AMMONIA);
//        AMMONIA_RESOURCE = new FluidResource(AMMONIA, null);
//        builder.add(AMMONIA_RESOURCE);
//        
//        FRESH_AIR = new Fluid("fresh_air", gasResource, gasResource, MatterColors.FRESH_AIR);
//        FluidRegistry.registerFluid(FRESH_AIR);
//        FRESH_AIR.setGaseous(true);
//        FRESH_AIR_CHANNEL = Channel.channelForFluid(FRESH_AIR);
//        FRESH_AIR_RESOURCE = new FluidResource(FRESH_AIR, null);
//        builder.add(FRESH_AIR_RESOURCE);
//       
//        RETURN_AIR = new Fluid("return_air", gasResource, gasResource, MatterColors.RETURN_AIR);
//        FluidRegistry.registerFluid(RETURN_AIR);
//        RETURN_AIR.setGaseous(true);
//        RETURN_AIR_CHANNEL = Channel.channelForFluid(RETURN_AIR);
//        RETURN_AIR_RESOURCE = new FluidResource(RETURN_AIR, null);
//        builder.add(RETURN_AIR_RESOURCE);
//        
//        OXYGEN_GAS = new Fluid("oxygen_gas", gasResource, gasResource, MatterColors.OXYGEN);
//        FluidRegistry.registerFluid(OXYGEN_GAS);
//        OXYGEN_GAS.setGaseous(true);
//        OXYGEN_GAS_CHANNEL = Channel.channelForFluid(OXYGEN_GAS);
//        OXYGEN_GAS_RESOURCE = new FluidResource(OXYGEN_GAS, null);
//        builder.add(OXYGEN_GAS_RESOURCE);
//        
//        HYDROGEN_GAS = new Fluid("hydrogen_gas", gasResource, gasResource, MatterColors.HYDROGEN);
//        FluidRegistry.registerFluid(HYDROGEN_GAS);
//        HYDROGEN_GAS.setGaseous(true);
//        HYDROGEN_GAS_CHANNEL = Channel.channelForFluid(HYDROGEN_GAS);
//        HYDROGEN_GAS_RESOURCE = new FluidResource(HYDROGEN_GAS, null);
//        builder.add(HYDROGEN_GAS_RESOURCE);
//        
//        ETHENE_GAS = new Fluid("ethene_gas", gasResource, gasResource, MatterColors.ETHENE);
//        FluidRegistry.registerFluid(ETHENE_GAS);
//        ETHENE_GAS.setGaseous(true);
//        ETHENE_GAS_CHANNEL = Channel.channelForFluid(ETHENE_GAS);
//        ETHENE_GAS_RESOURCE = new FluidResource(ETHENE_GAS, null);
//        builder.add(ETHENE_GAS_RESOURCE);
//        
//        FLOWABLE_GRAPHITE = new Fluid("flowable_graphite", flowableResource, flowableResource, MatterColors.GRAPHITE);
//        FluidRegistry.registerFluid(FLOWABLE_GRAPHITE);
//        FLOWABLE_GRAPHITE_RESOURCE = new FluidResource(FLOWABLE_GRAPHITE, null);
//        
//        FLOWABLE_MINERAL_FILLER = new Fluid("flowable_mineral_filler", flowableResource, flowableResource, MatterColors.DEPLETED_MINERAL_DUST);
//        FluidRegistry.registerFluid(FLOWABLE_MINERAL_FILLER);
//        FLOWABLE_MINERAL_FILLER_RESOURCE = new FluidResource(FLOWABLE_MINERAL_FILLER, null);
//
//        RAW_MINERAL_DUST = new Fluid("raw_mineral_dust", flowableResource, flowableResource, MatterColors.RAW_MINERAL_DUST);
//        FluidRegistry.registerFluid(RAW_MINERAL_DUST);
//        RAW_MINERAL_DUST_RESOURCE = new FluidResource(RAW_MINERAL_DUST, null);
//
//        DEPLETED_MINERAL_DUST = new Fluid("depleted_mineral_dust", flowableResource, flowableResource, MatterColors.DEPLETED_MINERAL_DUST);
//        FluidRegistry.registerFluid(DEPLETED_MINERAL_DUST);
//        DEPLETED_MINERAL_DUST_RESOURCE = new FluidResource(DEPLETED_MINERAL_DUST, null);
        
        ALL_PORTED_FLUIDS = builder.build();
    }
    
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) 
    {
        // force execution of static block
        Log.info("Registering Hard Science fluids");
    }
}
