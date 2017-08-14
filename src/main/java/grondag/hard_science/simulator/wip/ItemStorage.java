package grondag.hard_science.simulator.wip;

import grondag.hard_science.simulator.wip.StorageType.StorageTypeStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemStorage extends AbstractStorage<StorageTypeStack>
{

    public ItemStorage()
    {
        super();
    }
    
    public ItemStorage(NBTTagCompound nbt)
    {
        super();
        this.deserializeNBT(nbt);
    }
    
    @Override
    protected IResource<StorageTypeStack> makeResource(NBTTagCompound nbt)
    {
        return new ItemResource(nbt);
    }

    @Override
    public StorageTypeStack storageType()
    {
        return StorageType.ITEM;
    }
}
