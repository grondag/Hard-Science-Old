package grondag.hard_science.simulator.resource;

import javax.annotation.Nonnull;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.simulator.resource.StorageType.StorageTypePower;
import net.minecraft.nbt.NBTTagCompound;

public class PowerResourceWithQuantity extends AbstractResourceWithQuantity<StorageType.StorageTypePower>
{
    public PowerResourceWithQuantity(@Nonnull PowerResource resource, long quantity)
    {
        super(resource, quantity);
    }
    
    public PowerResourceWithQuantity()
    {
        super();
    }
    
    public PowerResourceWithQuantity(NBTTagCompound tag)
    {
        super(PowerResource.JOULES, tag.getLong(ModNBTTag.RESOURCE_QUANTITY));
    }
    
    @Override
    public NBTTagCompound toNBT()
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setLong(ModNBTTag.RESOURCE_QUANTITY, this.quantity);
        return tag;
    }
    
    @Override
    public StorageTypePower storageType()
    {
        return StorageType.POWER;
    }
}
