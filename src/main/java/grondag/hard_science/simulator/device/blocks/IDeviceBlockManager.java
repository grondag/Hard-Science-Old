package grondag.hard_science.simulator.device.blocks;

import java.util.Collection;

/**
 * Manages the device block delegates for a device.
 * 
 * Hierarchical structure
 *  Device
 *      DeviceBlock(s)
 *          Connector Instance : Connection Instance
 *              Port Instance : Transport Node
 * 
 */
public interface IDeviceBlockManager
{
    /**
     * All device blocks for this device.
     */
    public Collection<IDeviceBlock> blocks();
    
    /** 
     * Will be called by owning device when added to world.
     * Should register all device blocks with DeviceWorldManager.
     * Happens before transport manager connect.
     */
    public void connect();
    
    /** 
     * Will be called by owning device when removed from world. 
     * Should unregister all device blocks with DeviceWorldManager.
     */
    public void disconnect();
}
