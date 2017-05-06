package grondag.adversity.library;

import java.util.ArrayList;
import java.util.Comparator;

import grondag.adversity.Output;

public class PerformanceCollector
{
    private ArrayList<PerformanceCounter> counters = new ArrayList<PerformanceCounter>();
    
    private final String title;
    
    public PerformanceCollector(String title)
    {
        this.title = title;
    };
    
    public void register(PerformanceCounter counter)
    {
        this.counters.add(counter);
    }
    
    public void clear()
    {
        this.counters.clear();
    }
    
    public void outputStats()
    {
        this.counters.sort(new Comparator<PerformanceCounter> () 
        {

            @Override
            public int compare(PerformanceCounter o1, PerformanceCounter o2)
            {
                return Long.compare(o2.runTime(), o1.runTime());
            }
        });
        
        long total = 0;
        for(PerformanceCounter counter : this.counters)
        {
            total += counter.runTime();
        }
        if(total == 0) total = 1;  // prevent div by zero below
        
        Output.getLog().info("======================================================================================");
        Output.getLog().info("Performance Measurement for " + this.title );
        Output.getLog().info("--------------------------------------------------------------------------------------");
        for(PerformanceCounter counter : this.counters)
        {
            Output.getLog().info((counter.runTime() * 100 / total) + "% " + counter.stats());
        }
        Output.getLog().info("--------------------------------------------------------------------------------------");
        Output.getLog().info(String.format("TOTAL TIME = %1$.3fs (%2$,dns)", (double)total/1000000000L, total));
    }
    
    public void clearStats()
    {
        for(PerformanceCounter counter : this.counters)
        {
            counter.clearStats();
        }
    }
    
}
