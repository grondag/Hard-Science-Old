package grondag.hard_science.init;

import java.util.HashMap;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.materials.MatterColors;
import grondag.hard_science.simulator.resource.FluidResource;
import grondag.hard_science.simulator.transport.carrier.Channel;
import grondag.hard_science.simulator.transport.endpoint.PortLayout;
import grondag.hard_science.superblock.texture.Textures;
import jline.internal.Log;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ModFluids
{
    /**
     * All fluids this mod uses, included fluids from other modes.
     */
    public final static ImmutableList<FluidResource> ALL_USED_FLUIDS;
    
    /**
     * Port layouts for low-level fluid carriers. Here because
     * breaks object holders in the port layouts registration class.
     */
    public static final HashMap<FluidResource, PortLayout> FLUID_CARRIERS = new HashMap<FluidResource, PortLayout>();
    
    /**
     * Purified water
     */
    public static final int WATER_CHANNEL;
    public static final FluidResource WATER_RESOURCE;
    
    public static final Fluid H2O;
    public static final int H2O_CHANNEL;
    public static final FluidResource H2O_RESOURCE;
    
    public static final Fluid AMMONIA;
    public static final int AMMONIA_CHANNEL;
    public static final FluidResource AMMONIA_RESOURCE;
    
//    public static final Fluid OXYGEN;
//    public static final int OXYGEN_CHANNEL;
    
//    public static final Fluid CO2;
//    public static final int CO2_CHANNEL;
    
    static
    {
        ImmutableList.Builder<FluidResource> builder = ImmutableList.builder();
        
        ResourceLocation liquidResource = new ResourceLocation(Textures.BIGTEX_FLUID_VORTEX.getSampleTextureName());
        ResourceLocation gasResource = new ResourceLocation(Textures.BIGTEX_CLOUDS.getSampleTextureName());
        WATER_CHANNEL = Channel.channelForFluid(FluidRegistry.WATER);
        WATER_RESOURCE = new FluidResource(FluidRegistry.WATER, null);
        builder.add(WATER_RESOURCE);
                
        H2O = new Fluid("purified_water", liquidResource, liquidResource, MatterColors.WATER);
        H2O_CHANNEL = Channel.channelForFluid(H2O);
        H2O_RESOURCE = new FluidResource(H2O, null);
        builder.add(H2O_RESOURCE);

        AMMONIA = new Fluid("ammonia", gasResource, gasResource, MatterColors.AMMONIA);
        AMMONIA.setGaseous(true);
        AMMONIA_CHANNEL = Channel.channelForFluid(AMMONIA);
        AMMONIA_RESOURCE = new FluidResource(AMMONIA, null);
        builder.add(AMMONIA_RESOURCE);
        
        ALL_USED_FLUIDS = builder.build();
    }
    
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) 
    {
        // force initialization
        Log.info("Registering Hard Science fluids");
    }
}
