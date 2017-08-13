package grondag.hard_science.simulator.job;

public interface IRequest
{
    public RequestStatus getStatus();
    public RequestPriority getPriority();
}
