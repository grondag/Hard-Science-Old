//package grondag.hard_science.simulator.scratch;
//
//import grondag.hard_science.simulator.resource.IResouceRequest;
//import grondag.hard_science.simulator.wip.IResource;
//
//public interface ITransportRequest<T extends IResource<T>> extends IResouceRequest<T>
//{
//
//    /** 
//     * Packet that was or is expected to be delivered.  May be different
//     * than packet that was requested if insufficient capacity.
//     * Will be null if packet was lost, cancelled, etc.
//     * Sender of packet is responsible for not deducting from source
//     * more than is actually sent.  Best way to do this is to use deliveredPacket
//     * as soure for making those deductions.
//     */
//    public abstract IPacket<T> deliveredPacket();
//    
//    /**
//     * Expected or actual duration of transport, in ticks.
//     */
//    public abstract int getDurationTicks();
//    
//    /**
//     * Expected or actual cost of transport, in Joules
//     */
//    public abstract long getEnergyCost();
//    
//    /**
//     * True if the request can be executed. False if canceled, simulation, completed, etc.
//     */
//    public abstract boolean canExecute();    
//    
//    /** returns true if transport started (may or may not be complete), false if already started or not able to start */
//    public abstract boolean execute();
//    
//    /** returns true if fully cancelled.  False if not able or cancel not yet complete. */  
//    public abstract boolean cancel();
//    
//    /** will call argument if status changes.  Returns false if not supported */
//    public default boolean requestCallback(IScheduleCallback callback) { return false; };
//}
