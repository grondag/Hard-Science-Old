package grondag.adversity.library;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.RunnableFuture;


public abstract class Job
{
    public abstract boolean canRun();
    
    public boolean PERFORMANCE_COUNTING_ENABLED = false;  
  
    /** if false, should just call run instead of executeOn() */
    public abstract boolean worthRunningParallel();
    
    /** 
     * Run as single batch on the calling thread 
     * Return value is the approximate number of things that ran - for performance counting.
     */
    public abstract int run();
    
    /** 
     * Submits all batches to the given executor and adds futures to the given list.
     * Return value is the approximate number of things that ran - for performance counting.
     */
    public abstract int executeOn(Executor executor, List<RunnableFuture<Void>> futures);
    
    private final ArrayList<RunnableFuture<Void>> futures = new ArrayList<RunnableFuture<Void>>();
    
    // not final so that we can share counters among same job
    public SimplePerformanceCounter perfCounter = new SimplePerformanceCounter();
    
    public void runOn(Executor executor)
    {

        if(PERFORMANCE_COUNTING_ENABLED) this.perfCounter.startRun();
        
        if(this.canRun())
        {
            if(this.worthRunningParallel())
            {
                boolean isDone = false;
                try 
                {
                    futures.clear();
                    if(PERFORMANCE_COUNTING_ENABLED) 
                        this.perfCounter.addCount(this.executeOn(executor, futures));
                    else
                        this.executeOn(executor, futures);
                    
                    for (RunnableFuture<Void> f : futures)
                    {
                        if (!f.isDone()) {
                            try 
                            {
                                f.get();
                            } 
                            catch (CancellationException ignore) {} 
                            catch (ExecutionException ignore) {}
                            catch (InterruptedException ignore) {}
                        }
                        f.cancel(true);
                    }
                    isDone = true;
                }
                finally 
                {
                    if (!isDone)
                    {
                        for (RunnableFuture<Void> f : futures)
                        {
                            f.cancel(true);
                        }
                    }
                }
            }
            else
            {
                if(this.PERFORMANCE_COUNTING_ENABLED) 
                    this.perfCounter.addCount(this.run());
                else
                    this.run();
            }
      
            if(this.PERFORMANCE_COUNTING_ENABLED) this.perfCounter.endRun();
        }
    
    }
}