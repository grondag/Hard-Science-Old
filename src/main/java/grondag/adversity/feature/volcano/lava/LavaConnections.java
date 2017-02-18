package grondag.adversity.feature.volcano.lava;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import grondag.adversity.feature.volcano.lava.cell.LavaCell2;
import grondag.adversity.library.PackedBlockPos;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.EnumFacing;


public class LavaConnections
{
  
    private LavaConnection2 connections[] = new LavaConnection2[0xFFFF];
    
    /** first index that has not been used */
    private AtomicInteger firstUnusedIndex = new AtomicInteger(0);
    
    /** list of indexes below firstUnused that are empty */
    private ConcurrentLinkedQueue<Integer> unusedIndexes = new ConcurrentLinkedQueue<Integer>();
    
    private AtomicInteger size = new AtomicInteger(0);
    
    private LavaConnection2 sortedArray[] = new LavaConnection2[0xFFFF];
    
    private boolean isSortCurrent = false;
    
    public void clear()
    {
        synchronized(this)
        {
            Arrays.fill(connections, null);
            isSortCurrent = false;
        }
    }
    
    public int size()
    {
        return size.get();
    }
    
    public void createConnectionIfNotPresent(LavaCell2 first, LavaCell2 second)
    {
        if(!first.isConnectedTo(second))
        {
            LavaConnection2 newConnection = new LavaConnection2(first, second);
            this.addConnectionToArray(newConnection);
        }
    }
    
    public void removeConnection(LavaConnection2 connection)
    {
        if(!connection.isDeleted())
        {
            connection.releaseCells();
            this.removeCellFromArray(connection);
            connection.setDeleted();
        }
    }
    
    /** 
     * Adds connection to the storage array. 
     * Does not do anthing with the cells.
     * Thread-safe.
     */
    private void addConnectionToArray(LavaConnection2 connection)
    {
        int i;
        Integer index = unusedIndexes.poll();
        if(index == null)
        {
            i = firstUnusedIndex.getAndIncrement();
        }
        else
        {
            i = index;
        }
        connection.index = i;
        connections[i] = connection;
        size.incrementAndGet();
    }
    
    /** 
     * Removes connection from the storage array. 
     * Does not do anything with the cells.
     * Thread-safe.
     */
    private void removeCellFromArray(LavaConnection2 connection)
    {
        connections[connection.index] = null;
        unusedIndexes.add(connection.index);
        connection.index = -1;
        size.decrementAndGet();
    }
    
    public LavaConnection2[] values()
    {
        if(!isSortCurrent)
        {
            Arrays.stream(connections).parallel().forEach(p -> {if(p != null) p.updateSortKey();});
            
            System.arraycopy(connections, 0, sortedArray, 0, 0xFFFF);
        
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
    
    public void validateConnections(AbstractLavaSimulator sim)
    {
        Arrays.stream(connections).parallel().forEach(p ->
        {
            if(p != null && !p.isValid())
            {
                this.removeConnection(p);
            }
        });
    }
    
}
