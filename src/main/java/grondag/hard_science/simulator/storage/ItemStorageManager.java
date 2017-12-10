package grondag.hard_science.simulator.storage;

import javax.annotation.Nonnull;

import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemStorageManager extends AbstractStorageManager<StorageTypeStack>
{
    public ItemStorageManager(Domain domain)
    {
        super(StorageTypeStack.ITEM, domain);
    }

    public ItemStorageManager(@Nonnull NBTTagCompound nbt, Domain domain)
    {
        this(domain);
        this.deserializeNBT(nbt);
    }
    
    @Override
    protected IStorage<StorageTypeStack> makeStorage(@Nonnull NBTTagCompound nbt)
    {
        return new ItemStorage(nbt);
    }

}