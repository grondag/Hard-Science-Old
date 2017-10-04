//package grondag.hard_science.simulator.scratch;
//
//import grondag.hard_science.simulator.wip.IResource;
//import grondag.hard_science.simulator.wip.IResourceLocation;
//import grondag.hard_science.simulator.wip.StorageType;
//
//public interface IResouceTransportRequest<V extends StorageType>
//{
//    public IResource<V> getResource();
//    
//    /**
//     * If non-null, search results will favor resources closer to this location.
//     * Must be non-null for requests that are to be delivered. 
//     */
//    public IResourceLocation<V> getDestination();
//    
//    /** see {@link #getDestination()} */
//    public void setDestination(IResourceLocation<V> destination);
//    
//    
//    public IResourceLocation<V> getOrigin();
//    
//    public void setOrigin(IResourceLocation<V> origin);
//}
