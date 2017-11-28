package grondag.hard_science.simulator.resource;

import grondag.hard_science.library.serialization.IReadWriteNBT;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Tracks all claims for resources in a domain, issues claim tickets
 * based on priority and seniority of requests, notifies claimants
 * when claimed resources become available or unavailable.
 * 
 * Will probably need an inner class to manage the demand for a 
 * specific resource, or perhaps a group of fungible resources. 
 * "Broker"?
 */
public abstract class AbstractDemandManager<T extends StorageType<T>> implements IReadWriteNBT
{

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        // TODO Auto-generated method stub
        
    }

}
