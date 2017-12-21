package grondag.hard_science.simulator.transport.endpoint;

public abstract class PortInstance
{
    private final Port port;
    
    protected PortInstance(Port port)
    {
        this.port = port;
    }

    public Port port()
    {
        return port;
    }
}
