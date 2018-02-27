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
    STORAGE(true),

    /**
     * For holding device input locally after it is received
     * from the stroage/transport network. Contents are not 
     * listed in the domain storage manager and no storage 
     * events are fired.<p>
     * 
     * Updates to this object should be synchronized because
     * can occur on different threads.<p>
     */
    BUFFER_IN(false),
    
    /**
     * For holding device output locally before it is put
     * on the stroage/transport network. Contents are not 
     * listed in the domain storage manager and no storage 
     * events are fired.<p>
     * 
     * Updates to this object should be synchronized because
     * can occur on different threads.<p>
     */
    BUFFER_OUT(false);
    
    
    /**
     * True if contents are listed with the storage manager
     * and storage events will be fired WHILE CONNECTED
     */
    public final boolean isListed;
    
    
    private ContainerUsage(boolean isListed)
    {
        this.isListed = isListed;
    }
}
