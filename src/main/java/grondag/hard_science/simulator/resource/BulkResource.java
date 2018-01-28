package grondag.hard_science.simulator.resource;


import grondag.hard_science.simulator.resource.StorageType.StorageTypeBulk;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Identifier for resources used within device processing or system accounting.
 * Private resources have no storage manager and no logistics service. 
 * They do not publish any events and cannot be transported.
 * They CAN be stored in isolated containers.
 */
public class BulkResource extends IForgeRegistryEntry.Impl<BulkResource> implements IResource<StorageTypeBulk>
{
    public BulkResource(ResourceLocation name)
    {
        this.setRegistryName(name);
    }
    
    public BulkResource(String name)
    {
        this.setRegistryName(name);
    }
    
    @Override
    public String displayName()
    {
        return this.getRegistryName().toString();
    }

    @Override
    public BulkResourceWithQuantity withQuantity(long quantity)
    {
        return new BulkResourceWithQuantity(this, quantity);
    }
    
    @Override
    public String toString()
    {
        return this.displayName();
    }

    @Override
    public int hashCode()
    {
        return this.getRegistryName().hashCode();
    }
  
    @Override
    public boolean isResourceEqual(IResource<?> other)
    {
        return this.equals(other);
    }

    @Override
    public boolean equals(Object other)
    {
        if(other == this) return true;
        if(other == null) return false;
        if(other instanceof BulkResource)
        {
            return ((BulkResource)other).getRegistryName().equals(this.getRegistryName());
        }
        return false;
    }
    
    @Override
    public StorageTypeBulk storageType()
    {
        return StorageType.PRIVATE;
    }
}
