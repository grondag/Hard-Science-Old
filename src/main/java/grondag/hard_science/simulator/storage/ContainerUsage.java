package grondag.hard_science.simulator.storage;

public enum ContainerUsage
{
    /**
     * All input and output is published to domain event manager
     * and contents are registered with domain storage manager.
     * (But only while the device is connected to the device manager.)<p>
     * 
     * All updates must occur on service thread for storage type.
     * (Again, only while the device is connected to the device manager.)
     * This includes actions on world thread! They must be
     * scheduled on service thread and wait for completion!<p>
     * 
     * Because all updates must occur on the same thread,
     * storage containers generally do not require synchronization.
     */
    STORAGE(true, true, true),

    /**
     * Contents are not listed in the domain storage manager.
     * And no storage events are fired. From the perspective
     * of the storage/transport network, the container is
     * always empty. (But capacity changes as buffer is filled and consumed.)<p>
     * 
     * All calls to add() must occur on the storage service thread.
     * (But only while the device is connected to the device manager.)
     * Other threads may call takeUpTo() and its derivatives
     * without restriction.  For this reason, all updates to
     * this object are synchronized.<p>
     * 
     * This arrangement allow the owning device to draw content 
     * from this buffer outside of the storage service thread. 
     * It also means the storage service can trust that increases 
     * in quantity stored can only happen as a result of an action 
     * occurring on the service thread.  (Always transfers in.)<p>
     * 
     * This is an important guarantee for the transport network, 
     * which assumes capacity for a transfer will remain available based on
     * prior simulation results within the same call.
     */
    BUFFER_IN(false, true, false),
    
    /**
     * All (non-simulated) calls to takeUpTo() and its derivatives 
     * are published to the domain event manager, and contents
     * ARE listed in the domain storage manager. (But only while
     * the device is connected to the device manager.)<p>
     * 
     * Other threads may call add() and its derivatives
     * without restriction. For this reason, all updates to
     * this object are synchronized. Notification of adds will be 
     * queued to the service thread after add it complete.
     * (If the device is connected to the device manager.)<p>
     * 
     * This arrangement allow the owning device to place content 
     * in this buffer outside of the storage service thread and
     * contents are still visible for retrieval on the transport network.<p>
     *  
     * It also means the storage service can trust that decreases
     * in quantity stored can only happen as a result of an action 
     * occurring on the service thread.  This is an important 
     * guarantee for the transport network, which assumes that
     * inventory for a transfer will remain available based on
     * prior simulation results within the same call.
     */
    BUFFER_OUT(true, false, true),
    
    /**
     * No actions and no content are published to domain event manager.
     * All access to the object is synchronized and actions can
     * take place on any thread.<p>
     * 
     * Isolated buffers are used for internal device buffers that
     * do not interact directly with the transport network.
     */
    BUFFER_ISOLATED(false, false, false);
    
    /**
     * True if contents are listed with the storage manager
     * and storage events will be fired WHILE CONNECTED
     */
    public final boolean isListed;
    
    /**
     * True if updates must be synchronized. (True for all except STORAGE)
     */
    public final boolean needsSynch;
    
    /**
     * True if calls to add() and its derivatives must always be called
     * from the storage service thread WHILE CONNECTED.
     */
    public final boolean isInputThreadRestricted;
    
    /**
     * True if calls to takeUpTo() and its derivatives must always be called
     * from the storage service thread WHILE CONNECTED.
     */
    public final boolean isOutputThreadRestricted;
    
    private ContainerUsage(
            boolean isListed,
            boolean isInputThreadRestricted,
            boolean isOutputThreadRestricted)
    {
        this.isListed = isListed;
        this.needsSynch = !(isInputThreadRestricted && isOutputThreadRestricted);
        this.isInputThreadRestricted = isInputThreadRestricted;
        this.isOutputThreadRestricted = isOutputThreadRestricted;
    }
}
