package grondag.adversity.library;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fast, concurrent, array-based collection of integer values.
 * Size is dynamic.
 */
public class FastIntQueue
{
    private volatile int[] values;;
    
    /** capacity to add when full.  0 means double size */
    private final int sizeIncrement;
    
    AtomicInteger size = new AtomicInteger(0);
    
    public FastIntQueue()
    {
        this(8, 0);
    }
    
    public FastIntQueue(int startingCapacity)
    {
        this(startingCapacity, 0);
    }
    
    public FastIntQueue(int startingCapacity, int sizeIncrement)
    {
        this.sizeIncrement = Math.max(0, sizeIncrement);
        this.values = new int[Math.max(1,  startingCapacity)];
    }
    
    public void add(int i)
    {
        int location = size.getAndIncrement();
        if(location > values.length)
        {
            synchronized(this)
            {
                // confirm not already expanded by another thread
                if(location > values.length)
                {
                    values = Arrays.copyOf(values, sizeIncrement == 0 ? values.length * 2 : values.length + sizeIncrement);
                }
            }
        }
        values[location] = i;
    }
    
   public void 
}
