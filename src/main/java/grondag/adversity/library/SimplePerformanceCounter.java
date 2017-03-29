package grondag.adversity.library;

public class SimplePerformanceCounter
{
    private long runTime = 0;
    private int runCount = 0;
    private long minTime = Long.MAX_VALUE;
    private long maxTime = 0;
   
    private long startTime;
    
    public void clearStats()
    {
        this.runCount = 0;
        this.runTime = 0;
        this.minTime = Long.MAX_VALUE;
        this.maxTime = 0;
    }

    public void startRun()
    {
        this.startTime = System.nanoTime();
    }
    
    public void endRun()
    {
        long time = System.nanoTime() - startTime;
        if(time > this.maxTime) this.maxTime = time;
        if(time < this.minTime) this.minTime = time;
        this.runTime += time;
    }
    
    public void addCount(int howMuch)
    {
        this.runCount += howMuch;
    }
    
    public int runCount() { return this.runCount; }
    public long runTime() { return this.runTime; }
    public long timePerRun() { return this.runCount == 0 ? 0 : this.runTime / this.runCount; }
    public String stats() { return String.format("time this sample = %1$.3fs for %2$,d items @ %3$,dns each. Min = %4$,dns Max = %5$,dns"
            , ((double)runTime() / 1000000000), runCount(),  timePerRun(), this.minTime, this.maxTime); }
}
