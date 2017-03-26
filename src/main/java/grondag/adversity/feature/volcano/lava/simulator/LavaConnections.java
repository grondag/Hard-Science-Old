package grondag.adversity.feature.volcano.lava.simulator;


import java.util.stream.Stream;

import grondag.adversity.library.SimpleConcurrentList;

public class LavaConnections extends SimpleConcurrentList<LavaConnection>
{
    
    @SuppressWarnings("unchecked")
    private SimpleConcurrentList<LavaConnection>[] sort = new SimpleConcurrentList[4];
    
    
    public static enum SortBucket
    {
        A, B, C, D
    }

   
    
    private boolean isSortCurrent = false;
    
    public LavaConnections()
    {
        super();
        this.sort[SortBucket.A.ordinal()] = new SimpleConcurrentList<LavaConnection>();
        this.sort[SortBucket.B.ordinal()] = new SimpleConcurrentList<LavaConnection>();
        this.sort[SortBucket.C.ordinal()] = new SimpleConcurrentList<LavaConnection>();
        this.sort[SortBucket.D.ordinal()] = new SimpleConcurrentList<LavaConnection>();
        this.isSortCurrent = false;
    }
    
    @Override
    public void clear()
    {
        synchronized(this)
        {
            super.clear();
            this.sort[SortBucket.A.ordinal()].clear();
            this.sort[SortBucket.B.ordinal()].clear();
            this.sort[SortBucket.C.ordinal()].clear();
            this.sort[SortBucket.D.ordinal()].clear();
        }
    }
    
    public void createConnectionIfNotPresent(LavaCell first, LavaCell second)
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
    
    private void createConnectionIfNotPresentInner(LavaCell first, LavaCell second)
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
                        LavaConnection newConnection = new LavaConnection(first, second);
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
        this.removeDeletedItems();
    }
    
    /** 
     * Adds connection to the storage array and marks sort dirty.
     * Does not do anything else.
     * Thread-safe.
     */
    public void addConnectionToArray(LavaConnection connection)
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
        for(SimpleConcurrentList<LavaConnection> bucket : this.sort)
        {
            bucket.clear();
        }
        
        LavaSimulator.LAVA_THREAD_POOL.submit(() ->
            this.stream(true).forEach(c -> {
                
//                if(c.firstCell.id == 1229 || c.secondCell.id == 1229)
//                    Adversity.log.info("boop");
                
                if(c != null && c.isActive()) this.sort[c.getSortBucket().ordinal()].add(c);
                
            })).join();
        
        this.isSortCurrent = true;
    }
    
    /**
     * Returns a stream of LavaConnection instances within the given sort bucket. Stream will be parallel if requested..
     */
    public Stream<LavaConnection> getSortStream(SortBucket bucket, boolean isParallel)
    {
        if(!isSortCurrent) refreshSortBuckets();
        return this.sort[bucket.ordinal()].stream(isParallel);
    }
}
