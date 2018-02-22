package grondag.hard_science.simulator.transport.carrier;

import grondag.hard_science.Log;
import grondag.hard_science.simulator.resource.FluidResource;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

/**
 * Channels are integer IDs used to tag transport ports
 * and circuits to constrain connectivity. This class
 * defines constants and utility methods for working
 * with channel values.<p>
 * 
 * Item and power circuits/ports use device species to
 * determine channel, or one of the constants defined below.
 * Different tiers of circuit do NOT need the same channel.
 * For convenience, this logic is codified in 
 * {@link StorageType#channelsSpanLevels()}<p>
 * 
 * Fluid circuit/ports use MC Forge fluid registry ID as the
 * channel for circuits/ports handling a fluid.  Unlike Item
 * and power connections, ports and circuits at every 
 * tier must have compatible channels in order to connect.
 * This constraints models the in-world requirement to prevent 
 * fluids from sharing pipes so they don't mix.<p>
 * 
 * Note that channel values for fluids are determined at run time
 * and should never be serialized. Always use {@link #channelForFluid(Fluid)}
 * to obtain fluid channel values. Once obtained, value will not 
 * change until MC restarts.<p>
 * 
 * By convention, top-level item and power ports/circuits
 * should always used TOP_CHANNEL as their channel so that they pass all
 * tests for channel matching without special handling.
 *
 */
public class Channel
{
    /**
     * Lowest value that will be used for channel.  
     * No special constants should be defined with a value less than this.
     * Needed for serializing port descriptions.
     */
    public static final int MIN_CHANNEL_VALUE = -8;
    
    /**
     * Max value that will be used for channel.  
     * Item and power channels only go up 15 but fluid
     * channels are limited only by the number of fluids
     * registered in the game.  So, setting an arbitrary
     * limit and will warn if exceeded.<p>
     * 
     * Note this max value is also the value used to 
     * represent the "top" channel for top-level item/power
     * carrier circuits.
     */
    public static final int MAX_CHANNEL_VALUE = 500;

    
    public static final int TOP_CHANNEL = MAX_CHANNEL_VALUE;
    
    /**
     * Means the port doesn't have a channel yet but could.
     * Port should not connect until a channel is set.
     */
    public static final int CONFIGURABLE_NOT_SET = -1;
    
    /**
     * Means the port should have a channel but doesn't
     * or something is preventing the channel from being recognized.
     * Port should not connect until a channel is set.
     */
    public static final int INVALID_CHANNEL = -2;
    
    /**
     * Means the port will be configured to follow the 
     * device channel when it is created. Intended for use
     * in port layout and should not be encountered in device ports. <p>
     * 
     * Generally only applies only to carrier ports. 
     * Does not apply to fluid ports nor to the external
     * side of bridge ports.
     */
    public static final int CONFIGURABLE_FOLLOWS_DEVICE = -3;

    
    /**
     * Cache fluid ID map from Forge.  Note that we never
     * serialize the IDs, we only use them as transient identifiers.
     */
    private static final Object2IntOpenHashMap<Fluid> fluidIDs;

    static
    {
        fluidIDs = new Object2IntOpenHashMap<Fluid>();
        fluidIDs.defaultReturnValue(INVALID_CHANNEL);
    }
    
    public static int channelForFluid(IResource<StorageTypeFluid> forResource)
    {
        return channelForFluid((FluidResource)forResource);
    }
    
    public static int channelForFluid(FluidResource forResource)
    {
        return forResource == null 
            ? INVALID_CHANNEL 
            : channelForFluid(forResource.getFluid());
    }
    
    public static int channelForFluid(Fluid fluid)
    {
        return fluid == null 
                ? INVALID_CHANNEL
                : fluidIDs.getInt(fluid);
    }

    public static boolean doesFluidMatchChannel(int channel, IResource<StorageTypeFluid> forResource)
    {
        return doesFluidMatchChannel(channel, (FluidResource)forResource);
    }
    
    /**
     * True if the circuit channel matches the fluid of the given bulkResource
     */
    public static boolean doesFluidMatchChannel(int channel, FluidResource forResource)
    {
        return forResource != null 
            && forResource == StorageType.FLUID.emptyResource
            && doesFluidMatchChannel(channel, forResource.getFluid());
    }
    
    /**
     * True if the circuit channel matches the fluid of the given bulkResource
     */
    public static boolean doesFluidMatchChannel(int channel, Fluid forFluid)
    {
        return forFluid != null && channel == channelForFluid(forFluid);
    }

    /**
     * True if the channel is likely to be a valid value and
     * not one of the symbolic constants defined in this class.
     */
    public static boolean isRealChannel(int channel)
    {
        return channel >= 0;
    }
    
    /**
     * Encapsulates the logic that top-level, non-fluid carriers
     * should always used {@link #TOP_CHANNEL} as their channel.
     * Returns input channel if logic does not apply.
     */
    public static int channelOverride(int channel, CarrierLevel level, StorageType<?> storageType)
    {
        return level.isTop() && !storageType.channelsSpanLevels()
                ? TOP_CHANNEL
                : channel;
    }
    
    /**
     * Called from common event handler when fluid is registered.
     */
    public static void addFluid(String fluidName, int fluidID)
    {
        if(fluidID >= MAX_CHANNEL_VALUE)
            Log.warn("Max fluid channel transport value exceeded. Too many fluids. Unexpected behavior may result.");
        
        fluidIDs.put(FluidRegistry.getFluid(fluidName), fluidID);
    }
}
