package grondag.hard_science.simulator.transport;
//package grondag.hard_science.simulator.transport;
//
//import java.util.concurrent.atomic.AtomicInteger;
//
//
//
///**
// * Base class for network simulation.
// * 
// * Networks transfer a packet of some type.
// * 
// * Network is never persisted - always rebuilt on world reload.
// */
//public class TransportLink extends AbstractTransportLink
//{
//    
//    /**
//     * Max accumulated tokens;
//     */
//    private final int maxTokens;
//    
//    /**
//     * Tokens added each tick, up to max.
//     */
//    private final int tokensPerTick;
//    
//    protected int lastTickSeen = -1;
//    
//    /**
//     * Tokens that are available to be consumed right now. 
//     * Depleted whenever units are consumed and refreshed each tick
//     * according to tokensPerTick.
//     */
//    protected int currentTokens;
//    
//    protected TransportLink(PacketType type)
//    {
//        this.linkType = type;
//    }
//    
//    /**
//     * Caller should not call consumeCapacity()
//     * @param tick
//     * @return
//     */
//    public synchronized long getCapacity(int tick)
//    {
//        if(tick > lastTickSeen && this.currentCapacity < this.maxCapacity)
//        {
//            this.currentCapacity = Math.min(this.maxCapacity, currentCapacity + (tick - lastTickSeen) * this.capacityPerTick);
//        }
//        return this.currentCapacity;
//    }
//    
//    public synchronized void useCapacity(int units)
//    {
//        this.currentCapacity -= units;
//    }
//}
