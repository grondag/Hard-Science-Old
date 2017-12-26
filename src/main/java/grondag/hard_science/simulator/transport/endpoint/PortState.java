package grondag.hard_science.simulator.transport.endpoint;

import java.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.hard_science.Configurator;
import grondag.hard_science.Log;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.device.IDeviceComponent;
import grondag.hard_science.simulator.transport.carrier.CarrierCircuit;
import grondag.hard_science.simulator.transport.management.ConnectionManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

/**
 * Represents a physical port on a device.
 * Subclassed for the various port types.<p>
 * 
 * Methods related to network topology should ONLY be
 * called from the connection manager thread and will 
 * throw an exception if called otherwise.  This avoids
 * the need for synchronization of these methods.
 *
 */
public abstract class PortState implements IDeviceComponent
{
    private final Port port;
    
    private final BlockPos pos;
    
    private final EnumFacing face;
    
    /**
     * If port has mated, should contain a reference 
     * to a port on an adjacent (or wirelessly connected) device.
     */
    protected PortState mate;
    
    /**
     * See {@link #externalCircuit()}
     */
    protected CarrierCircuit externalCircuit;
    
    public PortState(Port port, @Nullable BlockPos pos, @Nullable EnumFacing face)
    {
        this.port = port;
        this.pos = pos;
        this.face = face;
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
     * Calls {@link IDevice#refreshTransport(grondag.hard_science.simulator.resource.StorageType)} 
     * if attachment is successful.<p>
     * 
     * Does NOT call attach on mated port. Caller must do so.
     * But does save reference to mate for notification and inspection.<p>
     * 
     * SHOULD ONLY BE CALLED FROM CONNECTION MANAGER THREAD.
     */
    public boolean attach(@Nonnull CarrierCircuit externalCircuit, @Nonnull PortState mate)
    {
        ConnectionManager.confirmNetworkThread();
        
        assert this.externalCircuit == null
                : "PortState attach request when already attached.";
        
        if(Configurator.logTransportNetwork) 
            Log.info("PortState.attach: port attach for %s to circuit %d with mate %s",
                    this.portName(),
                    externalCircuit.carrierAddress(),
                    mate.portName());
        
        // circuit / node will expect this before attachment
        this.externalCircuit = externalCircuit;
        if(externalCircuit.attach(this, false))
        {
            this.mate = mate;
            this.device().refreshTransport(this.port.storageType);
            return true;
        }
        else
        {
            this.externalCircuit = null;
            return false;
        }
    }
    
    /**
     * Detaches from externalCircuit and removes reference to it.
     * Also sets mate reference to null.<p>
     * 
     * Calls {@link IDevice#refreshTransport(grondag.hard_science.simulator.resource.StorageType)}<p>
     * 
     * Does NOT call mate detach  Is expected caller will get mate
     * reference before calling and ensure mate is also detached.<p>
     * 
     * SHOULD ONLY BE CALLED FROM CONNECTION MANAGER THREAD.
     */
    public void detach()
    {
        ConnectionManager.confirmNetworkThread();
        
        assert this.externalCircuit != null
                : "PortState dettach request when not attached.";
        
        if(Configurator.logTransportNetwork) 
            Log.info("PortState.detach: port detach for %s", this.portName());
        
        this.externalCircuit.detach(this);
        this.externalCircuit = null;
        this.mate = null;
        this.device().refreshTransport(this.port.storageType);
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
    
    /**
     * For use by merge/split operations.<p>
     * 
     * Swaps *any* non-null reference to the old circuit held by this port
     * to use the new non-null reference.  If does not holds a reference to a 
     * to the old circuit, does nothing.<p>
     * 
     * Calls {@link IDevice#refreshTransport(grondag.hard_science.simulator.resource.StorageType)} 
     * if any swap occurs.<p>
     * 
     * Does NOT otherwise perform any notifications or cause any side effects.
     * For example, does not add itself as a port on the new circuit.
     * Caller is expected to handle any such necessary accounting.<p>
     * 
     * SHOULD ONLY BE CALLED FROM CONNECTION MANAGER THREAD.
     */
    public void swapCircuit(@Nonnull CarrierCircuit oldCircuit, @Nonnull CarrierCircuit newCircuit)
    {
        ConnectionManager.confirmNetworkThread();
        if(this.externalCircuit == oldCircuit)
        {
            this.externalCircuit = newCircuit;
            this.device().refreshTransport(this.port.storageType);
        }
    }
    
    /**
     * If this port is a carrier port on a device with an internal carrier, 
     * iterates all other ports on the device attached to the same carrier.<p>
     * 
     * Always empty for direct ports<p>
     * 
     * With {@link #mate()}, enables search within a connected topology.
     */
    @Nonnull
    public Iterable<PortState> carrierMates()
    {
        return Collections.emptyList();
    }

    /**
     * For TOP and debug display.
     * Null for wireless.
     */
    @Nullable
    public BlockPos pos()
    {
        return this.pos;
    }
    
    /**
     * For TOP and debug display.
     * Null for wireless.
     */
    @Nullable
    public EnumFacing face()
    {
        return this.face;
    }
    
    /**
     * For TOP and debug display
     */
    public String portName()
    {
        return String.format(
            "%s %d/%d on %s @ %s", 
            this.port.toString(),
            this.internalCircuit() == null ? 0 : this.internalCircuit().carrierAddress(),
            this.externalCircuit() == null ? 0 : this.externalCircuit().carrierAddress(),
            this.device().machineName(),
            this.pos() == null || this.face() == null
                ? "N/A"
                : String.format("%d.%d.%d:%s", 
                        this.pos().getX(), 
                        this.pos().getY(), 
                        this.pos().getZ(), 
                        this.face().toString())
        );
    }
}
