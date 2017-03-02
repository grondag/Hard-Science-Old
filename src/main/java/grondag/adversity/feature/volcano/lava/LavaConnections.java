package grondag.adversity.feature.volcano.lava;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

import grondag.adversity.feature.volcano.lava.cell.LavaCell2;

public class LavaConnections
{
  
    /** contains all extant connections - used to process all connections with simple iteration */
    private LavaConnection2 connections[] = new LavaConnection2[0x10000];
    
    private AtomicInteger size = new AtomicInteger(0);
    
    private LavaConnection2 sortedArray[] = new LavaConnection2[0x10000];
    
    private boolean isSortCurrent = false;
    
    public void clear()
    {
        synchronized(this)
        {
            this.connections = new LavaConnection2[0x10000];
            this.sortedArray = new LavaConnection2[0x10000];
            this.isSortCurrent = false;
        }
    }
    
    public int size()
    {
        return size.get();
    }
    
    
    //TODO: make sure this is called during connection processing after NBT load to prevent overflow on large simulations
    /** 
     * Ensures processing array has sufficient storage.  
     * Should be called periodically to prevent overflow 
     * and free unused memory.
     */
    public void manageCapacity()
    {
        int capacity = this.connections.length - this.size.get();
        if(capacity < 0x8000)
        {
            this.connections = Arrays.copyOf(connections, this.connections.length + 0x10000);
            this.sortedArray = new LavaConnection2[this.connections.length];
            this.isSortCurrent = false;
        }
        else if(capacity >= 0x20000)
        {
            this.connections = Arrays.copyOf(connections, this.connections.length - 0x10000);
            this.sortedArray = new LavaConnection2[this.connections.length];
            this.isSortCurrent = false;
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
     * Validates all connections and removes
     * deleted or invalid connections from the storage array. 
     * NOT Thread-safe.
     */
    public void validateConnections()
    {
        int i = 0;
        while(i < this.size.get())
        {
            if(!connections[i].isValid())
            {
                connections[i].releaseCells();
                connections[i] = connections[this.size.decrementAndGet()];
                connections[this.size.get()] = null;
            }
            else
            {
                i++;
            }
        }
    }
    
    /** 
     * Adds connection to the storage array and marks sort dirty.
     * Does not do anything else.
     * Thread-safe.
     */
    public void addConnectionToArray(LavaConnection2 connection)
    {
        connections[size.getAndIncrement()] = connection;
        this.isSortCurrent = false;
    }
    
    public LavaConnection2[] values()
    {
        if(!isSortCurrent)
        {
            //TODO: stop at size
            Arrays.stream(connections).parallel().forEach(p -> {if(p != null) p.updateSortKey();});
            
            System.arraycopy(connections, 0, sortedArray, 0, this.size.get());
        
            //TODO: stop sorting at size
            
            //TODO: can sort into four buckets, with first bucket being most urgent outflow (if any) for a given cell
            // second bucket is second most urgent, etc. Some cells will have no outflows. A few will have four.
            // can then process each bucket with concurrency before moving on to the next bucket.
            // "with concurrency" means need to synchronize access both cells involved.
            
            Arrays.parallelSort(sortedArray, 
                    new Comparator<LavaConnection2>() {
                      @Override
                      public int compare(LavaConnection2 o1, LavaConnection2 o2)
                      {
                          return Long.compare(o1.getSortKey(), o2.getSortKey());
                      }});
        }
        
        return sortedArray;
        
    }
}
