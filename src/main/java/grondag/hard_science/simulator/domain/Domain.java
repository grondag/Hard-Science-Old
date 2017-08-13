package grondag.hard_science.simulator.domain;

import grondag.hard_science.simulator.take2.IInventoryManager;
import grondag.hard_science.simulator.take2.StorageType;

public abstract class Domain
{
    public abstract IInventoryManager<? extends StorageType> findStorageManager(StorageType storageType);
    
    public static interface IDomainMember
    {
        public Domain domain();
    }

}
