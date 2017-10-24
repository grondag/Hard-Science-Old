package grondag.hard_science.simulator.base;

public interface IRequest
{
    /** Value for requestID that means no request. Assigned numbers authority should never assign this number. */
    public static final int NO_REQUEST = 0;
    
    public int requestID();
    public RequestStatus getStatus();
    public RequestPriority getPriority();
}
