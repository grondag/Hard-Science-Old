package grondag.hard_science.simulator.transport.endpoint;

public enum PortType
{
    /**
     * AKA "Passthough" port.<p>
     * 
     * Port is attached to an internal carrier within the device.
     * Device might have other ports also attached to the same carrier.
     * Carrier signal can pass through the device through mated, non-switched
     * ports.<p>
     * 
     * This is the base port type for most simple machines.
     * Allows machines to be placed adjacent and form a simple bus with
     * a common carrier. <p>
     * 
     * This port type is also used for interconnects between bus/switch devices
     * (including cables) of the same capacity. Such devices will typically not
     * register transport nodes on the carrier because they aren't producer or
     * consumer end points for transport services.<p>
     * 
     * All rules for carrier ports apply to the ports within the device
     * and to ports attached to the same carrier outside the device: 
     * Must be same carrier type, same channel, limited number of 
     * device blocks per carrier. (Except at top level.)<p>
     * 
     * Can only mate with other carrier ports (of same level) or
     * with an uplink port of the next highest level.
     * 
     * When used to cross-connect top-level switches, these ports have no
     * channel (because top-level switches have no channels) and can only
     * connect two end points, directly, with no branching.  This constraint
     * is in place for item transport: items traveling at supersonic speeds
     * in a vacuum tube can only change direction (except for gentle curves)
     * within the switch itself.
     */
    CARRIER,
    
    /**
     * AKA "Isolated" port.<p>
     * 
     * A direct port has no internal carrier and must mate with a carrier port
     * Direct ports are isolated from other ports on the same
     * device and represents a "direct" attachment to that device. This port
     * type is the only option for higher capacity ports on non-switch/non-bus devices.
     * There would be no benefit to a base-capacity version of a direct port,
     * and they cannot be used to form or extend a bus, so they are only used
     * for machines that require high volume transport and thus need to be attached
     * directly to higher volume carriers.<p>
     */
    DIRECT,
    
    /**
     * Bridge ports connect switches and intermediate busses 
     * to a higher capacity switch or bus.
     * These ports have two carriers: internal and external. 
     * Bridge ports can only mate with carrier ports because
     * bridge ports do not provide an external carrier.
     * <p>
     * 
     * The internal carrier is that of the owning device and
     * is shared with other ports on the same switch/bus device. 
     * The external carrier is that that of the mated port
     * which must be a carrier port.
     */
    BRIDGE;
}
