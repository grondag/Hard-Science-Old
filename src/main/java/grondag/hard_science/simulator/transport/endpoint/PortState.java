package grondag.hard_science.simulator.transport.endpoint;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.hard_science.simulator.device.IDeviceComponent;
import grondag.hard_science.simulator.transport.carrier.CarrierCircuit;

/**
 * Represents a physical port on a device.
 * Subclassed for the various port types.
 */
public abstract class PortState implements IDeviceComponent
{
    private final Port port;
    
    /**
     * If port has mated, should contain a reference 
     * to a port on an adjacent (or wirelessly connected) device.
     */
    protected PortState mate;
    
    /**
     * See {@link #externalCircuit()}
     */
    protected CarrierCircuit externalCircuit;
    
    public PortState(Port port)
    {
        this.port = port;
    }

    /**
     * Identifies physical connector type and function<p>
     * 
     * Part of physical device configuration and cannot be changed
     * without tear-down and reset of any connections.
     */
    public Port port()
    {
        return this.port;
    }

    /**
     * Configured channel for internal carrier if this port
     * has an internal carrier. Used to segregate carrier circuits.  
     * Carrier ports with different channel values cannot mate and 
     * cannot coexist on the same carrier circuit.<p>
     * 
     * Note that internal/external carrier (and thus channel) are
     * always the same for carrier ports. Channel Will always be 
     * zero and should be ignored for direct ports and external bridge ports.
     * Direct ports and the external part of bridge ports always 
     * use the carrier channel of the external carrier attached.<p> 
     * 
     * Same is true for top-level carriers/ports (channel is zero/ignored)
     * because top-level carriers do not have channels<p>
     */
    public int getConfiguredChannel() { return 0; }

    /**
     * If port is mated and carrier circuit has formed, reference to the 
     * external carrier circuit. Null otherwise. <p>
     * 
     * Is always the same as {@link #internalCircuit()} for carrier ports.
     */
    @Nullable
    public CarrierCircuit externalCircuit()
    {
        return externalCircuit;
    }
    
    /**
     * Reference to device's internal carrier if there is one.
     * Will always be null for direct ports and will always
     * be the same as {@link #externalCircuit()} for carrier ports.<p>
     * 
     * Will also be null if no carrier or bridge ports on a device are mated.
     */
    @Nullable
    public abstract CarrierCircuit internalCircuit();
    
    /**
     * Attaches to the provided circuit and handles internal record keeping.
     * Returns false if attachment is not possible.<p>
     * 
     * Does NOT call attach on mated port. Caller must do so.
     * But does save reference to mate for notification and inspection.
     */
    public boolean attach(@Nonnull CarrierCircuit externalCircuit, @Nonnull PortState mate)
    {
        assert this.externalCircuit == null
                : "PortState attach request when already attached.";
        
        if(externalCircuit.attach(this, false))
        {
            this.mate = mate;
            this.externalCircuit = externalCircuit;
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Detaches from externalCircuit and removes reference to it.
     * Also calls detach on mate if has mate and mate not already detached.
     */
    public void detach()
    {
        assert this.externalCircuit != null
                : "PortState dettach request when not attached.";
        this.externalCircuit.detach(this);
        this.externalCircuit = null;
        
        if(this.mate != null)
        {
            // check necessary to avoid stack overflow
            if(this.mate.externalCircuit != null) this.mate.detach();
            this.mate = null;
        }
    }

    /**
     * See {@link #mate}
     */
    public PortState mate()
    {
        return this.mate;
    }
    
    public boolean isAttached()
    {
        return this.externalCircuit != null;
    }
}
