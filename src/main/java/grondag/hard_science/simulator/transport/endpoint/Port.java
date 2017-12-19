package grondag.hard_science.simulator.transport.endpoint;

import grondag.hard_science.simulator.resource.StorageType;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Represents a single, resource-specific port
 * within a connnector.
 */
public class Port extends IForgeRegistryEntry.Impl<Port>
{
    public final StorageType<?> storageType;
    
    public Port(String name, StorageType<?> storageType)
    {
        this.setRegistryName(name);
        this.storageType = storageType;
    }
}
