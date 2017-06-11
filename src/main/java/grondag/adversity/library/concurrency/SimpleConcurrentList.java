package grondag.adversity.library.concurrency;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import grondag.adversity.Log;
import grondag.adversity.library.concurrency.CountedJob.CountedJobProviderBacker;

/**
 * Provides functionality similar to an array list, but with low overhead and high concurrency 
 * for a specific set of use cases. 
 * Intended for use where insertion and indexing are parallel but other housekeeping tasks will 
 * only occur on a single thread, while nothing else is being done.
 * Iteration is as for an array (access to the array is provided) but not intended to happen concurrently with adds
 * because memory consistency is not guaranteed while adds are ongoing.<br><br>
 *  
 * It has *significant* limitations:
 *  1) Items can be added only at the end of the list, and only individually. Adding items is non-blocking. 
 *  2) Items can be removed only by providing a predicate function to do so.
 *  3) Removal methods are NOT thread-safe.  Caller must ensure other methods are not called while removal is in progress. 
 *  4) Insertion order is NOT maintained if items are removed.
 *   
 *   * @author grondag
 *
 */

public class SimpleConcurrentList<T> implements Iterable<T>, CountedJobProviderBacker<T>
{
    protected Object[]  items; 
    private volatile int capacity;
    private AtomicInteger size = new AtomicInteger(0);
    private volatile boolean isMaintaining = false;
    private int nextDeletionStartIndex = 0;
    
    private static final int DELETION_BATCH_SIZE = 1024;
    
    public static <V> SimpleConcurrentList<V> create(boolean enablePerformanceCounting, String listName, PerformanceCollector perfCollector)
    {
        return enablePerformanceCounting ? new Instrumented<V>(listName, perfCollector) : new SimpleConcurrentList<V>();
    }
    
    public static <V> SimpleConcurrentList<V> create(PerformanceCounter perfCounter)
    {
        return perfCounter == null ? new SimpleConcurrentList<V>() : new Instrumented<V>(perfCounter);
    }
    
    private SimpleConcurrentList()
    {
        this.capacity = 16;
        this.items = new Object[capacity];
    }
   
    public PerformanceCounter removalPerfCounter() { return null; }
    
    /**
     * @return Current number of items in the list.
     */
    public int size()
    {
        return this.size.get();
    }
    
    public boolean isEmpty()
    {
        return this.size.get() == 0;
    }
    
    /**
     * Adds item at end of list.
     * Safe for concurrent use with other adds.
     * Not safe for concurrent use with other operations.
     * @param item Thing to add
     */
    public void add(T item)
    {
        if(Log.DEBUG_MODE && this.isMaintaining)
            Log.warn("Unsupported add operation on simple concurrent list during maintenance operation");
        
        int index = this.size.getAndIncrement();
        if(index < this.capacity)
        {
            items[index] = item;
        }
        else
        {
            synchronized(this)
            {
                if(index >= this.capacity)
                {
                    int newCapacity = this.capacity * 2;
                    this.items = Arrays.copyOf(this.items, newCapacity);
                    this.capacity = newCapacity;
                }
                items[index] = item;
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public T get(int index)
    {
        return (T) items[index];
    }
    
    @Override
    public Object[] getOperands()
    {
        return this.items;
    }
    
    /**
     * Removes *some* deleted items in the list and compacts storage.
     * Call periodically to keep list clean.
     * ORDER OF ITEMS MAY CHANGE.
     * NOT THREAD SAFE
     * Caller must ensure no other methods are called while this method is ongoing.
     */
    @SuppressWarnings("unchecked")
    public void removeSomeDeletedItems(Predicate<T> trueIfDeleted)
    {
        if(this.size.get() == 0) return;
        
        // note - will not prevent add or iteration
        // so does not, by itself, ensure thread safety
        synchronized(this)
        {
            this.isMaintaining = true;
            
            int newSize = this.size.get();
            int start;
            int end;
            
            if(newSize > DELETION_BATCH_SIZE)
            {
                start = this.nextDeletionStartIndex;
                if(start >= newSize) start = 0;
                
                end = start + DELETION_BATCH_SIZE;
                if(end >= newSize)
                {
                    end = newSize;
                    this.nextDeletionStartIndex = 0;
                }
                else
                {
                    this.nextDeletionStartIndex = end;
                }
            }
            else
            {
                start = 0;
                end = newSize;
            }

            for(int i = start; i < newSize; i++)
            {
                Object item =  this.items[i];
        
                if(trueIfDeleted.test((T)item))
                {
                    items[i] = items[--newSize];
                    items[newSize] = null;
                }
            }
            
            this.size.set(newSize);
            
            this.isMaintaining = false;

        }
    }
    
    /**
     * Removes *all* deleted items in the list and compacts storage.
     * Use when you can't have nulls in an operation about to happen.
     * ORDER OF ITEMS MAY CHANGE.
     * NOT THREAD SAFE
     * Caller must ensure no other methods are called while this method is ongoing.
     */
    @SuppressWarnings("unchecked")
    public void removeAllDeletedItems(Predicate<T> trueIfDeleted)
    {
        if(this.size.get() == 0) return;
        
        // note - will not prevent add or iteration
        // so does not, by itself, ensure thread safety
        synchronized(this)
        {
            this.isMaintaining = true;
            
            int newSize = this.size();
            
            for(int i = 0; i < newSize; i++)
            {
                Object item =  this.items[i];
        
                if(trueIfDeleted.test((T)item))
                {
                    items[i] = items[--newSize];
                    items[newSize] = null;
                }
            }
            
            this.size.set(newSize);
            
            this.isMaintaining = false;

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
            this.isMaintaining = true;

            if(this.size.get() != 0)
            {
                Arrays.fill(items, null);
                this.size.set(0);
            }
            
            this.isMaintaining = false;
        }
    }
    
    @SuppressWarnings("unchecked")
    public Stream<T> stream(boolean isParallel)
    {
        if(Log.DEBUG_MODE && this.isMaintaining)
            Log.warn("Unsupported stream operation on simple concurrent list while maintenance operation ongoing");
        
        return (Stream<T>) StreamSupport.stream(Arrays.spliterator(items, 0, this.size.get()), isParallel);
    }

    @Override
    public Iterator<T> iterator()
    {
        return this.stream(false).iterator();
    }

    
    private static class Instrumented<T> extends SimpleConcurrentList<T>
    {
        private final PerformanceCounter removalPerfCounter;
        
        private Instrumented(String listName, PerformanceCollector perfCollector)
        {
            this.removalPerfCounter = PerformanceCounter.create(true, listName + " list item removal", perfCollector);
        }
        
        private Instrumented(PerformanceCounter perfCounter)
        {
            this.removalPerfCounter = perfCounter;
        }
        
        @Override
        public PerformanceCounter removalPerfCounter() { return this.removalPerfCounter; }
        
        @Override 
        public void removeSomeDeletedItems(Predicate<T> trueIfDeleted)
        {
            int startCount = this.size();
            this.removalPerfCounter.startRun();
            super.removeSomeDeletedItems(trueIfDeleted);
            this.removalPerfCounter.endRun();
            this.removalPerfCounter.addCount(startCount - this.size());
        }
    }
}
