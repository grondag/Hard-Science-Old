package grondag.hard_science.simulator.wip;

import grondag.hard_science.simulator.wip.StorageType.StorageTypeStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemStorage extends AbstractStorage<StorageTypeStack>
{
    public ItemStorage(NBTTagCompound nbt) 
    {
        super(nbt);
    }


    @Override
    public StorageTypeStack storageType()
    {
        return StorageType.ITEM;
    }
}