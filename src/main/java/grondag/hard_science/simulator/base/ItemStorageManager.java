package grondag.hard_science.simulator.base;

import javax.annotation.Nonnull;

import grondag.hard_science.simulator.base.StorageType.StorageTypeStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemStorageManager extends AbstractStorageManager<StorageTypeStack>
{
    public ItemStorageManager()
    {
        super(StorageTypeStack.ITEM);
    }

    public ItemStorageManager(@Nonnull NBTTagCompound nbt)
    {
        super(StorageTypeStack.ITEM);
        this.deserializeNBT(nbt);
    }
    
    @Override
    protected IStorage<StorageTypeStack> makeStorage(@Nonnull NBTTagCompound nbt)
    {
        return new ItemStorage(nbt);
    }

}