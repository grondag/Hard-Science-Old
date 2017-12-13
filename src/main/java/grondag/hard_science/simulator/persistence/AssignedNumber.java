package grondag.hard_science.simulator.persistence;

public enum AssignedNumber
{
    DOMAIN,
    STORAGE,
    MACHINE,
    JOB,
    TASK, 
    BUILD,
    TRANSPORT_NODE_ITEM,
    TRANSPORT_NODE_FLUID,
    TRANSPORT_NODE_POWER;
    
    public final String tagName;
    
    private AssignedNumber()
    {
        this.tagName = "hsanum" + this.ordinal();
    }
    
}
