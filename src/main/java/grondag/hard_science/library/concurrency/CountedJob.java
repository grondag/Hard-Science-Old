package grondag.hard_science.library.concurrency;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

public class CountedJob<V> extends Job
{
    private final ICountedJobBacker backer;
    private final int batchSize;
    private final CountedJobTask<V> task;

    public CountedJob(ICountedJobBacker backer, CountedJobTask<V> task, int batchSize, boolean enablePerfCounting, String jobTitle, PerformanceCollector perfCollector)
    {
        super(enablePerfCounting, jobTitle, perfCollector);
        this.backer = backer;
        this.task = task;
        this.batchSize = batchSize;
    }
    
    public CountedJob(ICountedJobBacker backer, CountedJobTask<V> task, int batchSize, PerformanceCounter perfCounter)
    {
        super(perfCounter);
        this.backer = backer;
        this.task = task;
        this.batchSize = batchSize;
    }
    
    /**
     * Check occurs during the run.
     */
    @Override
    public boolean canRun()
    {
        return true;
    }

    @Override
    public boolean worthRunningParallel()
    {
        return this.backer.size() > batchSize;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int run()
    {
        Object[] items = backer.getOperands();
        
        for(int i = 0; i < items.length; i++)
        {
            if(items[i] == null) return i;
            task.doJobTask((V)items[i]);
        }
        return items.length;
    }

    @Override
    public int executeOn(Executor executor, List<RunnableFuture<Void>> futures)
    {
        Object[] operands = backer.getOperands();
        int size = Math.min(backer.size(), operands.length);
        
        if(size == 1 || this.batchSize == 1)
        {
            for(int i = 0; i < operands.length; i++)
            {
                if(operands[i] == null) break;
                
                @SuppressWarnings("unchecked")
                RunnableFuture<Void> f = new FutureTask<Void>(new SingleJobBatch((V)operands[i]), null);
                executor.execute(f);
                futures.add(f);
            }
        }
        else
        {
            int start = 0;
            while(start < operands.length && operands[start] != null)
            {
                int end = Math.min(operands.length, start + batchSize);
                RunnableFuture<Void> f = new FutureTask<Void>(new CountedJobBatch(start, end, operands), null);
                executor.execute(f);
                start = end;
                futures.add(f);
            }
        }
        
        // not exact, but close enough
        return size;
    }
    
    private class SingleJobBatch implements Runnable
    {
        private final V operand;
        
        private SingleJobBatch(V operand)
        {
            this.operand = operand;
        }
        
        @Override
        public void run()
        {
            task.doJobTask(operand);
        }
    }
    
    private class CountedJobBatch implements Runnable
    {
        private final int start;
        private final int end;
        private final Object[] operands;
        
        private CountedJobBatch(int start, int end, Object[] operands)
        {
            this.start = start;
            this.end = end;
            this.operands = operands;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public void run()
        {
            for(int i = start; i < end; i++)
            {
                if(operands[i] == null) break;
                task.doJobTask((V)operands[i]);
            }
        }
    }
}

