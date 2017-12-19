package grondag.hard_science.simulator.transport.endpoint;

import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Represents a physical connector that includes
 * one or more physical ports.
 *
 */
public class Connector extends IForgeRegistryEntry.Impl<Connector>
{
    public Connector(String name)
    {
        this.setRegistryName(name);
    }
}
