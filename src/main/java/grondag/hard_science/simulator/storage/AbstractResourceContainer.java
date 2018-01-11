package grondag.hard_science.simulator.storage;

import java.util.function.Predicate;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.machines.support.ThroughputRegulator;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractResourceContainer<T extends StorageType<T>> implements IResourceContainer<T>
{
    protected final IDevice owner;
    private final ContainerUsage usage;
    protected long capacity = 2000;
    protected long used = 0;
    
    /**
     * If non-null, restricts what may be placed in this container.
     */
    protected Predicate<IResource<T>> predicate; 

    /**
     * Make this something other than the dummy regulator during
     * constructor if you want limits or accounting.
     */
    protected ThroughputRegulator regulator = ThroughputRegulator.DUMMY;
    
    public AbstractResourceContainer(IDevice owner, ContainerUsage usage)
    {
        this.owner = owner;
        this.usage = usage;
    }

    @Override
    public void serializeNBT(NBTTagCompound nbt)
    {
        nbt.setLong(ModNBTTag.STORAGE_CAPACITY, this.capacity);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        this.capacity = nbt.getLong(ModNBTTag.STORAGE_CAPACITY);
    }

    public IDevice device()
    {
        return this.owner;
    }

    @Override
    public ContainerUsage containerUsage()
    {
        return this.usage;
    }
    
    @Override
    public ThroughputRegulator getRegulator()
    {
        return this.regulator;
    }
    
    @Override
    public void setRegulator(ThroughputRegulator regulator)
    {
        this.regulator = regulator;
    }
    
    @Override
    public void setContentPredicate(Predicate<IResource<T>> predicate)
    {
        if(this.predicate != predicate && predicate != null)
        {
            if(this.used == 0)
            {
                this.predicate = predicate;
            }
            else assert false: "Attempt to configure non-empty resource container.";
        }
    }
    
    @Override
    public Predicate<IResource<T>> getContentPredicate()
    {
        return this.predicate;
    }
    
    @Override
    public long getCapacity()
    {
        return capacity;
    }

    @Override
    public void setCapacity(long capacity)
    {
        this.capacity = capacity;
    }

    @Override
    public long usedCapacity()
    {
        return this.used;
    }

}