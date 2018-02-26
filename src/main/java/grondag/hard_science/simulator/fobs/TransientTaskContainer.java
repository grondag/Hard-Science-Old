package grondag.hard_science.simulator.fobs;

import grondag.hard_science.simulator.domain.Domain;

/**
 * Container for classes that aren't serialized or prioritized.
 * 
 * This container does not keep references to its tasks and
 * provides no way to retrieve tasks associated with it.
 */
public class TransientTaskContainer implements ITaskContainer
{
    private final Domain domain;

    public TransientTaskContainer(Domain domain)
    {
        this.domain = domain;
    }
    
    @Override
    public Domain getDomain()
    {
        return this.domain;
    }

    @Override
    public TaskPriority priority()
    {
        return TaskPriority.NONE;
    }
    
}
