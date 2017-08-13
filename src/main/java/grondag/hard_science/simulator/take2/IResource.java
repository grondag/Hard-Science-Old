package grondag.hard_science.simulator.take2;

/** 
 * A resource is something that can be produced and consumed.
 * Most resources can be stored. (Computation can't.)
 * Resources with a storage type can also have a location.
 * Time is not a resource because it cannot be produced.
 */
public interface IResource<V extends StorageType>
{
    public V storageType();
    
    public int computeResourceHashCode();
    public boolean isResourceEqual(IResource<V> other);
    
    public AbstractResourceBroker<V> resourceBroker();
    
    
//    /**
//     * If true, resource can be represented by a single quantity
//     * of a standard form of this resource.
//     */
//    public default boolean isFungible() { return false; }
//    
//    
//    public interface IFungibleResource<V extends StorageType> extends IResource<V>
//    {
//        public default boolean isFungible() { return true; }
//        
//        /**
//         * The standard unit of denomination for fungible resources.
//         * @return
//         */
//        public IResourceStack<V> standardForm();
//        
//        public List<IResourceStack<V>> allForms();
//        
//        public IResourceStack<V> convertToStandardForm(IResourceStack<V> stack);
//        
//        /**
//         * Left return value will be empty if conversion is complete.
//         * Otherwise left value contains any portion that could not be converted.
//         * 
//         * FIXME: what about conversion costs & time?
//         * FIXME: what if multiple conversion methods?
//         */
//        public Pair<IResourceStack<V>, IResourceStack<V>> convert(IResourceStack<V> fromStack, IResourceStack<V> toStack);
//    
//    }
   
}
