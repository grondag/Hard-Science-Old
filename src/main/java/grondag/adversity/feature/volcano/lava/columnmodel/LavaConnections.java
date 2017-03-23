package grondag.adversity.feature.volcano.lava.columnmodel;

import java.util.stream.Stream;

import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.lava.AbstractLavaSimulator;
import grondag.adversity.library.SimpleConcurrentList;

public class LavaConnections extends SimpleConcurrentList<LavaConnection2>
{
    
    @SuppressWarnings("unchecked")
    private SimpleConcurrentList<LavaConnection2>[] sort = new SimpleConcurrentList[4];
    
    private static final int CAPACITY_INCREMENT = 0x10000;
    
    public static enum SortBucket
    {
        A, B, C, D
    }

    private boolean isSortCurrent = false;
    
    public LavaConnections()
    {
        super(CAPACITY_INCREMENT);
        this.clear();
    }
    
    @Override
    public void clear()
    {
        synchronized(this)
        {
            super.clear();
            this.resize(CAPACITY_INCREMENT);
            this.setupSortLists();
        }
    }
    
    private void setupSortLists()
    {
        this.sort[SortBucket.A.ordinal()] = new SimpleConcurrentList<LavaConnection2>(this.capacity());
        this.sort[SortBucket.B.ordinal()] = new SimpleConcurrentList<LavaConnection2>(this.capacity() / 2);
        this.sort[SortBucket.C.ordinal()] = new SimpleConcurrentList<LavaConnection2>(this.capacity() / 4);
        this.sort[SortBucket.D.ordinal()] = new SimpleConcurrentList<LavaConnection2>(this.capacity() / 4);
        this.isSortCurrent = false;
    }
    
    
    /** 
     * Ensures processing array has sufficient storage.  
     * Should be called periodically to prevent overflow 
     * and free unused memory.
     */
    public void manageCapacity()
    {
        if(this.availableCapacity() < CAPACITY_INCREMENT / 2)
        {
            this.setMode(ListMode.MAINTAIN);
            int newCapacity = this.capacity() + CAPACITY_INCREMENT;
            this.resize(newCapacity);
            this.setMode(ListMode.ADD);
            this.setupSortLists();
        }
        else if(this.availableCapacity() >= CAPACITY_INCREMENT * 2)
        {
            this.setMode(ListMode.MAINTAIN);
            int newCapacity = this.capacity() - CAPACITY_INCREMENT;
            this.setMode(ListMode.ADD);
            this.resize(newCapacity);
            this.setupSortLists();
        }
    }
    
    public void createConnectionIfNotPresent(LavaCell2 first, LavaCell2 second)
    {
        if(first.id < second.id)
        {
            this.createConnectionIfNotPresentInner(first, second);
        }
        else
        {
            this.createConnectionIfNotPresentInner(second, first);
        }
    }
    
    private void createConnectionIfNotPresentInner(LavaCell2 first, LavaCell2 second)
    {
        boolean isIncomplete = true;
        do
        {
            if(first.tryLock())
            {
                if(second.tryLock())
                {
                    if(!first.isConnectedTo(second))
                    {
                        LavaConnection2 newConnection = new LavaConnection2(first, second);
                        this.addConnectionToArray(newConnection);
                    }
                    
                    isIncomplete = false;
                    second.unlock();
                }
                first.unlock();
            }
        } while(isIncomplete);
    }
    
    /** 
     * Removes deleted or invalid connections from the storage array. 
     * NOT Thread-safe.
     */
    public void validateConnections()
    {
        this.setMode(ListMode.MAINTAIN);
        this.removeDeletedItems();
        this.setMode(ListMode.ADD);
    }
    
    /** 
     * Adds connection to the storage array and marks sort dirty.
     * Does not do anything else.
     * Thread-safe.
     */
    public void addConnectionToArray(LavaConnection2 connection)
    {
        this.add(connection);
        this.isSortCurrent = false;
    }
    
    public void invalidateSortBuckets()
    {
        this.isSortCurrent = false;
    }
    
    private void refreshSortBuckets()
    {
        for(SimpleConcurrentList<LavaConnection2> bucket : this.sort)
        {
            bucket.setMode(ListMode.MAINTAIN);
            bucket.clear();
            bucket.setMode(ListMode.ADD);
        }
        
        this.setMode(ListMode.INDEX);
        AbstractLavaSimulator.LAVA_THREAD_POOL.submit(() ->
            this.stream(true).forEach(c -> {
                
//                if(c.firstCell.id == 1229 || c.secondCell.id == 1229)
//                    Adversity.log.info("boop");
                
                if(c != null && c.isActive()) this.sort[c.getSortBucket().ordinal()].add(c);
                
            })).join();
        
        for(SortBucket b : SortBucket.values())
        {
            this.sort[b.ordinal()].setMode(ListMode.INDEX);
        }
        
        this.setMode(ListMode.ADD);
        
        this.isSortCurrent = true;
    }
    
    /**
     * Returns a stream of LavaConnection instances within the given sort bucket. Stream will be parallel if requested..
     */
    public Stream<LavaConnection2> getSortStream(SortBucket bucket, boolean isParallel)
    {
        if(!isSortCurrent) refreshSortBuckets();
        return this.sort[bucket.ordinal()].stream(isParallel);
    }
}
