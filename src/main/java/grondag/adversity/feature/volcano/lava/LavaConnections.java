package grondag.adversity.feature.volcano.lava;

import java.util.stream.Stream;

import grondag.adversity.feature.volcano.lava.cell.LavaCell2;
import grondag.adversity.library.SimpleConcurrentList;

public class LavaConnections extends SimpleConcurrentList<LavaConnection2>
{
    private final LavaSimulatorNew sim;
    
    @SuppressWarnings("unchecked")
    private SimpleConcurrentList<LavaConnection2>[] sort = new SimpleConcurrentList[4];
    
    private static final int CAPACITY_INCREMENT = 0x10000;
    
    public static enum SortBucket
    {
        A, B, C, D
    }

    private boolean isSortCurrent = false;
    
    public LavaConnections(LavaSimulatorNew sim)
    {
        super(CAPACITY_INCREMENT);
        this.sim = sim;
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
    
    
    //TODO: make sure this is called during connection processing after NBT load to prevent overflow on large simulations
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
    
    /** relies on caller to order parameters to avoid deadlock */
    private void createConnectionIfNotPresentInner(LavaCell2 first, LavaCell2 second)
    {
        synchronized(first)
        {
            synchronized(second)
            {
                if(!first.isConnectedTo(second))
                {
                    LavaConnection2 newConnection = new LavaConnection2(first, second);
                    this.addConnectionToArray(newConnection);
                }
            }
        }
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
            this.stream(true).forEach(p -> {if(p != null) this.sort[p.getSortBucket().ordinal()].add(p);;})).join();
        
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
