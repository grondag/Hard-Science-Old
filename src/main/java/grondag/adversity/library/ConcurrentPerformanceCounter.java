package grondag.adversity.library;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ConcurrentPerformanceCounter
{
    private AtomicLong runTime = new AtomicLong(0);
    private AtomicInteger runCount = new AtomicInteger(0);

    public void clearStats()
    {
        this.runCount.set(0); 
        this.runTime.set(0);
    }

    public long startRun()
    {
        return System.nanoTime();
    }
    
    public void endRun(long startTime)
    {
        this.runTime.addAndGet(System.nanoTime() - startTime);
    }
    
    public void addCount(int howMuch)
    {
        this.runCount.addAndGet(howMuch);
    }
    
    public int runCount() { return this.runCount.get(); }
    public long runTime() { return this.runTime.get(); }
    public long timePerRun() { return this.runCount.get() == 0 ? 0 : this.runTime.get() / this.runCount.get(); }
    public String stats() { return String.format("time this sample = %1$.3fs for %2$,d items @ %3$dns each."
            , ((double)runTime() / 1000000000), runCount(),  timePerRun()); }
}
