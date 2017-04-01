package grondag.adversity.library;

public interface ISimpleListItem
{
    /** return true if this element should be purged from the list during next clean() */
    public abstract boolean isDeleted();
    
//    /** called before deleted items are removed from the list */
//    public default void onDeletion()
//    {
//        // default implementation does nothing
//    }
}