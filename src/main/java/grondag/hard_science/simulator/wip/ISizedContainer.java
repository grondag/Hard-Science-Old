package grondag.hard_science.simulator.wip;

public interface ISizedContainer
{
    long getCapacity();
    long usedCapacity();
    default long availableCapacity()
    {
        return this.getCapacity() - this.usedCapacity();
    }
}