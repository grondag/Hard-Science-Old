package grondag.hard_science.simulator.transport.management;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import grondag.hard_science.simulator.transport.carrier.Carrier;
import grondag.hard_science.simulator.transport.carrier.Channel;
import grondag.hard_science.simulator.transport.routing.Legs;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * Transport manager for single carrier
 */
public class FluidTransportManager extends AbstractTransportManager<StorageTypeFluid>
{
    /** 
     * Used to cache legs information if this device is
     * attached to more than one circuit.  Otherwise will
     * rely on cache in the single circuit and will always be null.
     */
    Int2ObjectOpenHashMap<Legs<StorageTypeFluid>> legs = null;
    
    public FluidTransportManager(IDevice owner)
    {
        super(owner, StorageType.FLUID);
    }

    @Override
    public Legs<StorageTypeFluid> legs(IResource<StorageTypeFluid> forResource)
    {
        if(this.circuits.isEmpty()) return Legs.emptyLegs();

        int channel = Channel.channelForFluid(forResource);
        if(!Channel.isRealChannel(channel)) return Legs.emptyLegs();
        
        // if only a single circuit, rely on legs in it
        if(this.circuits.size() == 1)
        {
            return this.circuits.get(0).channel == channel
                    ? this.circuits.get(0).legs()
                    : Legs.emptyLegs();
        }
        
        // more than one circuit, so need to use map
        if(this.legs == null) this.legs = new Int2ObjectOpenHashMap<Legs<StorageTypeFluid>>();
        
        Legs<StorageTypeFluid> result = this.legs.get(channel);
        if(result == null || !result.isCurrent())
        {
            List<Carrier<StorageTypeFluid>> channelCircuits = this.circuits.stream()
                    .filter(c -> c.channel == channel)
                    .collect(ImmutableList.toImmutableList());
            
            result = channelCircuits.isEmpty()
                    ? Legs.emptyLegs()
                    : new Legs<StorageTypeFluid>(channelCircuits);
            
            this.legs.put(channel, result);
        }
        return result;
    }

    
    
    @Override
    public void refreshTransport()
    {
        super.refreshTransport();
        
        // force rebuild of legs
        if(this.legs != null) this.legs.clear();;
    }
}
