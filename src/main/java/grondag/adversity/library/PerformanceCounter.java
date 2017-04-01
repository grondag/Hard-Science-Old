package grondag.adversity.library;

public class PerformanceCounter
{
    public static PerformanceCounter create(boolean enablePerformanceCounting, String title, PerformanceCollector collector)
    {
        return enablePerformanceCounting ? new RealPerformanceCounter(title, collector) : new PerformanceCounter();
    }
    private PerformanceCounter() {}
    
    public void clearStats() {}

    public void startRun() {}

    public void endRun() {}
   
    public void addCount(int howMuch) {}
   
    public int runCount() { return 0; }
    
    public long runTime() { return 0; }

    public long timePerRun() { return 0; }
  
    public String stats() { return "Performance counting disabled"; }
    
    private static class RealPerformanceCounter extends PerformanceCounter
    {
        long runTime = 0;
        int runCount = 0;
        long minTime = Long.MAX_VALUE;
        long maxTime = 0;
        final String title;
       
        long startTime;
        
        public RealPerformanceCounter(String title, PerformanceCollector collector)
        {
            this.title = title;
            if(collector != null)
            {
                collector.register(this);
            }
        }
        
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

        public int runCount()
        { return this.runCount; }

        public long runTime()
        { return this.runTime; }

        public long timePerRun()
        { return this.runCount == 0 ? 0 : this.runTime / this.runCount; }

        public String stats()
        { return this.title + String.format(": %1$.3fs for %2$,d items @ %3$,dns each. Min = %4$,dns Max = %5$,dns"
        , ((double)runTime() / 1000000000), runCount(),  timePerRun(), this.minTime, this.maxTime); }

    }

}