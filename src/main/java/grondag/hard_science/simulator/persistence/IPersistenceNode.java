package grondag.hard_science.simulator.persistence;

import grondag.exotic_matter.serialization.IReadWriteNBT;

/**
 * Should be implemented by all top-level nodes that need to be saved to world state.
 * Needs to be registered with PersistenceManager.  That is handled in the Simulation class.
 */
public interface IPersistenceNode extends IDirtKeeper, IReadWriteNBT
{
    public abstract String tagName();
}
