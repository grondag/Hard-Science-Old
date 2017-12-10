package grondag.hard_science.simulator.resource;

import net.minecraft.nbt.NBTTagCompound;

public interface IResourcePredicateWithQuantity<V extends StorageType<V>>
{

    IResourcePredicate<V> predicate();

    long getQuantity();

    /**
     * returns new value
     */
    public long changeQuantity(long delta);
    
    void setQuantity(long quantity);

    NBTTagCompound toNBT();

}