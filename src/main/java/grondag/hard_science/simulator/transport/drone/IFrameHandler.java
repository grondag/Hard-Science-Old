// FIXME: remove
//package grondag.hard_science.simulator.transport.drone;
//
///**
// * Implement on classes that enqueue frames on transport media
// * to get callbacks when something happens.
// */
//public interface IFrameHandler
//{
//    /**
//     * Called just before an enqueued frame is put on the media.
//     * Use this to do actual extraction from producer nodes. <p>
//     * 
//     * Return false to cancel the frame and remove it from the queue.
//     */
//    public boolean onDeparture(Frame<?> frame);
//    
//    /**
//     * Called right after an enqueued frame arrives at the destination node.
//     * Use this to do actual insertion into consumer nodes. <p>
//     * 
//     * Return false to refuse acceptance. That will cause {@link #onLoss(Frame)}
//     * to be called.
//     */
//    public boolean onArrival(Frame<?> frame);
//    
//    /**
//     * Called when an enqueued frame is removed from the queue
//     * due to some failure or change. If called, {@link #onDeparture(Frame)} 
//     * has not run.
//     */
//    public void onAbandon(Frame<?> frame);
//    
//    /**
//     * Called when an enqueued frame already in transit 
//     * cannot be delivered and no more attempts will be made.
//     * If called, means {@link #onDeparture(Frame)} has already run.
//     */
//    public void onLoss(Frame<?> frame);
//}
