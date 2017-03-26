package grondag.adversity.library;

import java.util.ArrayList;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import grondag.adversity.Adversity;


public class Job<T>
{
    private final JobTask task;
    private final JobProvider provider;
    private final int batchSize;
    
    private final ArrayList<RunnableFuture<Void>> futures = new ArrayList<RunnableFuture<Void>>();
    
    private AtomicLong runTime = new AtomicLong(0);
    private AtomicInteger runCount = new AtomicInteger(0);
    
    public interface JobTask
    {
        public abstract void doJobTask(int index);
    }
    
    public interface JobProvider
    {
        public abstract int size();
    }

    public Job(JobTask task, JobProvider provider, int batchSize)
    {
        this.task = task;
        this.provider = provider;
        this.batchSize = batchSize;
    }
    
    public void clearStats()
    {
        this.runCount.set(0);
        this.runTime.set(0);
    }
    
    public int runCount() { return this.runCount.get(); }
    public long runTime() { return this.runTime.get(); }
    public long timePerRun() { return this.runCount.get() == 0 ? 0 : this.runTime.get() / this.runCount.get(); }
    public String stats() { return String.format("time this sample = %1$.3fs for %2$,d items @ %3$dns each."
            , ((double)runTime() / 1000000000), runCount(),  timePerRun()); }
    
    public void runOn(Executor executor)
    {
        long startTime;
        if(Adversity.DEBUG_MODE)
        {
            startTime = System.nanoTime();
            this.runCount.addAndGet(provider.size());
        }
        
        int size = provider.size();
        if(size <= batchSize)
        {
            for(int i = 0; i < size; i++)
            {
                task.doJobTask(i);
            }
        }
        else
        {
            boolean isDone = false;
            try 
            {
                futures.clear();
                int start = 0;
                while(start < size)
                {
                    int end = Math.min(size, start + batchSize);
                    RunnableFuture<Void> f = new FutureTask<Void>(new Batch(start, end), null);
                    executor.execute(f);
                    start = end;
                    futures.add(f);
                }
            
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
        
        if(Adversity.DEBUG_MODE)
        {
            this.runTime.addAndGet(System.nanoTime() - startTime);
        }
    }
    
    private class Batch implements Runnable
    {
        private final int start;
        private final int end;
        
        private Batch(int start, int end)
        {
            this.start = start;
            this.end = end;
        }
        
        @Override
        public void run()
        {
            for(int i = start; i < end; i++)
            {
                task.doJobTask(i);
            }
        }
        
    }
}