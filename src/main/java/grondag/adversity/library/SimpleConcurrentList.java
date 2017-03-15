package grondag.adversity.library;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import grondag.adversity.Adversity;

/**
 * Provides functionality similar to an array list, but with low overhead and high concurrency 
 * for a specific set of use cases. 
 * Intended for use where insertion and indexing are parallel but other housekeeping tasks will 
 * only occur on a single thread, while nothing else is being done.
 * Iteration is as for an array (access to the array is provided) but not intended to happen concurrently with adds
 * because memory consistency is not guaranteed while adds are ongoing.
 *  
 * It has *significant* limitations:
 *  1) Items can be added only at the end of the list, and only individually. Adding items is non-blocking. 
 *  2) No operation is provided to remove individual items. Items must implement ISimpleListItem to enable batch deletes. 
 *  3) The clean() method will remove items that are deleted.  
 *  4) clean() is NOT thread-safe.  Caller must ensure other methods are not called while clean() is in progress. 
 *  5) List capacity is dynamic but not automatically so - it can be changed with resize().
 *  6) resize() is NOT thread-safe.  Caller must ensure other methods are not called while resize() is in progress. 
 *  7) Insertion order is NOT maintained if items are deleted.
 *   
 *   * @author grondag
 *
 */

public class SimpleConcurrentList<T extends ISimpleListItem> implements Iterable<T>
{
    private Object[]  items; 
    private int capacity;
    private AtomicInteger size = new AtomicInteger(0);
    
    public static enum ListMode
    {
        /** ok to add items to the list 0 - fully concurrent with other additions. No other operations expected. */
        ADD,
        /** ok to iterate and index the array - fully concurrent for indexing operations. No other operations expected.*/
        INDEX,
        /** ok to call housekeeping methods (clear, removeDeleted). These operations are synchronized.  No other operations expected */
        MAINTAIN
    }
    
    private volatile ListMode mode = ListMode.ADD;
    
    public SimpleConcurrentList(int capacity)
    {
        this.capacity = capacity;
        this.items = new Object[capacity];
    }
    
    /**
     * Set mode of expected operation. Allows detection of improper use.
     */
    public void setMode(ListMode newMode)
    {
        synchronized(this)
        {
            this.mode = newMode;
        }
    }
    
    /**
     * @return Current number of items in the list.
     */
    public int size()
    {
        return this.size.get();
    }
    
    /**
     * @return current capacity of the list
     */
    public int capacity()
    {
        return this.capacity;
    }
    
    /**
     * @return count of items that can be added to the list before it is full
     */
    public int availableCapacity()
    {
        return this.capacity - this.size.get();
    }
    
    /**
     * Adds item at end of list.
     * Safe for concurrent use with other adds.
     * Not safe for concurrent use with other operations.
     * Will generate an array out of bounds exception if list is full.
     * @param item Thing to add
     */
    public void add(T item)
    {
        if(Adversity.DEBUG_MODE && this.mode != ListMode.ADD)
            Adversity.log.warn("Unsupported add operation on simple concurrent list while mode = " + this.mode);
        
        items[size.getAndIncrement()] = item;
    }
    
    /**
     * Change capacity to given size, or current number of items, whichever is more.
     * NOT THREAD SAFE
     * Caller must ensure no other methods are called while this method is ongoing.
     * @param newCapacity
     */
    public void resize(int newCapacity)
    {
        
        // note - will not prevent add or iteration
        // so does not, by itself, ensure thread safety
        synchronized(this)
        {
            if(Adversity.DEBUG_MODE && this.mode != ListMode.MAINTAIN)
                Adversity.log.warn("Unsupported resize operation on simple concurrent list while mode = " + this.mode);

            int c = Math.max(newCapacity, this.size.get());
            if(c != this.capacity)
            {
                this.capacity = c;
                this.items = Arrays.copyOf(this.items, c);
            }
        }
    }
    

    
    /**
     * Removes deleted items in the list and compacts storage.
     * ORDER OF ITEMS MAY CHANGE.
     * NOT THREAD SAFE
     * Caller must ensure no other methods are called while this method is ongoing.
     */
    public void removeDeletedItems()
    {
        ArrayList l;
        // note - will not prevent add or iteration
        // so does not, by itself, ensure thread safety
        synchronized(this)
        {
            if(Adversity.DEBUG_MODE && this.mode != ListMode.MAINTAIN)
                Adversity.log.warn("Unsupported removeDeletedItems operation on simple concurrent list while mode = " + this.mode);


            int i = 0;
            while(i < this.size.get())
            {
                @SuppressWarnings("unchecked")
                T item =  (T) this.items[i];

                if(item.isDeleted())
                {
                    item.onDeletion();
                    items[i] = items[this.size.decrementAndGet()];
                    items[this.size.get()] = null;
                }
                else
                {
                    i++;
                }
            }
        }
    }
    
    /**
     * Removes all items in the list, ensuring no references are held.
     * NOT THREAD SAFE
     * Caller must ensure no other methods are called while this method is ongoing.
     */
    public void clear()
    {
        // note - will not prevent add or iteration
        // so does not, by itself, ensure thread safety
        synchronized(this)
        {
            if(Adversity.DEBUG_MODE && this.mode != ListMode.MAINTAIN)
                Adversity.log.warn("Unsupported clear operation on simple concurrent list while mode = " + this.mode);

            if(this.size.get() != 0)
            {
                Arrays.fill(items, null);
                this.size.set(0);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public Stream<T> stream(boolean isParallel)
    {
        if(Adversity.DEBUG_MODE && this.mode != ListMode.INDEX)
            Adversity.log.warn("Unsupported stream operation on simple concurrent list while mode = " + this.mode);
        
        return (Stream<T>) StreamSupport.stream(Arrays.spliterator(items, 0, this.size.get()), isParallel);
    }

    @Override
    public Iterator<T> iterator()
    {
        return this.stream(false).iterator();
    }
}
