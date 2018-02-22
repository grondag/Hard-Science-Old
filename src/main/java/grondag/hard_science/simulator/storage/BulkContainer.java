package grondag.hard_science.simulator.storage;

import grondag.hard_science.machines.matbuffer.BulkBufferPurpose;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeBulk;

public class BulkContainer extends ResourceContainer<StorageTypeBulk>
{
    public final BulkBufferPurpose bufferPurpose;
    
    public BulkContainer(IDevice owner, BulkBufferPurpose purpose)
    {
        super(new BulkInner(owner));
        this.bufferPurpose = purpose;
        this.setCapacity(VolumeUnits.liters2nL(1));
    }
    
    private static class BulkInner extends AbstractSingleResourceContainer<StorageTypeBulk>
    {
        public BulkInner(IDevice owner)
        {
            super(owner, ContainerUsage.BUFFER_ISOLATED);
        }

        @Override
        public StorageTypeBulk storageType()
        {
            return StorageType.PRIVATE;
        }
    }
}
