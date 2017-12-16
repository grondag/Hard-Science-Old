package grondag.hard_science.simulator.transport.L1;

/**
 * Represents a physical connector that includes
 * one or more physical ports.
 *
 */
public interface IConnector
{
    public boolean canConnect(IConnector other);
}
