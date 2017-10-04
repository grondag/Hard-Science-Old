//package grondag.hard_science.simulator.scratch;
//
//public abstract class AbstractScheduledTransportResult<T extends IPacket> implements ITransportRequest<T>
//{
//    private TransportRequestStatus status;
//    
//    @Override
//    public final TransportRequestStatus getStatus()
//    {
//        return this.status;
//    }
//
//    protected final void setStatus(TransportRequestStatus newStatus)
//    {
//        if(this.status != newStatus)
//        {
//            this.status = newStatus;
//            this.doStatusCallback();
//        }
//    }
//
//    
//    /** called when status changes.  For implementing callback. */
//    protected void doStatusCallback() { };
//   
//}
