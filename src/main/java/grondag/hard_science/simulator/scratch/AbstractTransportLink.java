package grondag.hard_science.simulator.scratch;


public abstract class AbstractTransportLink implements ITransport
{
    private final int id = ITransport.NEXT_TRANSPORT_ID.getAndIncrement();
    
    public int getTransportID() { return this.id; }
    
}