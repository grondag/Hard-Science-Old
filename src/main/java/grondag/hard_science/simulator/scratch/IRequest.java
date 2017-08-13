package grondag.hard_science.simulator.scratch;

public interface IRequest
{
    public RequestStatus getStatus();
    public RequestPriority getPriority();
}
