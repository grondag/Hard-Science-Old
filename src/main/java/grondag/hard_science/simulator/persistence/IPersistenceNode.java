package grondag.hard_science.simulator.persistence;

import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.simulator.persistence.IDirtListener.IDirtKeeper;

/**
 * Should be implemented by all top-level nodes that need to be saved to world state.
 * Needs to be registered with PersistenceManager.  That is handled in the Simulation class.
 */
public interface IPersistenceNode extends IDirtKeeper, IReadWriteNBT
{
    public abstract String tagName();
}
