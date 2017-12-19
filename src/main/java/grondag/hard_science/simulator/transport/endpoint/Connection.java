package grondag.hard_science.simulator.transport.endpoint;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraftforge.registries.IForgeRegistryEntry;

public class Connection extends IForgeRegistryEntry.Impl<Connection>
{
    public final List<Port> ports;
    
    public Connection(String name, Port... ports)
    {
        this.setRegistryName(name);
        this.ports = ImmutableList.copyOf(ports);
    }
}
