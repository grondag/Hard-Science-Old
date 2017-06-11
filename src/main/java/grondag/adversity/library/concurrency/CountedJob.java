package grondag.adversity.library.concurrency;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

public class CountedJob<V> extends Job
{
    public interface CountedJobProviderBacker<V>
    {
        /** 
         * Returns an array where the contiguous elements from 0 to size() - 1 are operands.
         * Array MAY BE LONGER THAN size()! 
         */
        public abstract Object[] getOperands();
        public abstract int size();
        
    }
    
//    private static class MapBackerWrapper<V> implements CountedJobProviderBacker<V>
//    {
//        private final Map<?, V> map;
//        
//        private MapBackerWrapper(Map<?, V> map)
//        {
//            this.map = map;
//        }
//
//        @Override
//        public Object[] getOperands()
//        {
//            return map.values().toArray();
//        }
//
//        @Override
//        public int size()
//        {
//            return map.size();
//        }
//        
//    }
    
    public interface CountedJobTask<V>
    {
        public abstract void doJobTask(V operand);
    }
    

    private final CountedJobProviderBacker<V> backer;
    private final int batchSize;
    private final CountedJobTask<V> task;

    public CountedJob(CountedJobProviderBacker<V> backer, CountedJobTask<V> task, int batchSize, boolean enablePerfCounting, String jobTitle, PerformanceCollector perfCollector)
    {
        super(enablePerfCounting, jobTitle, perfCollector);
        this.backer = backer;
        this.task = task;
        this.batchSize = batchSize;
    }
    
    public CountedJob(CountedJobProviderBacker<V> backer, CountedJobTask<V> task, int batchSize, PerformanceCounter perfCounter)
    {
        super(perfCounter);
        this.backer = backer;
        this.task = task;
        this.batchSize = batchSize;
    }
//    public CountedJob(Map<?, V> backingMap, CountedJobTask<V> task, int batchSize)
//    {
//        this(new MapBackerWrapper<V>(backingMap), task, batchSize);
//    }
    
    @Override
    public boolean canRun()
    {
        return this.backer.size() > 0;
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
        
        int size = backer.size();
        if(size == 1) 
        {
            task.doJobTask((V)items[0]);
        }
        else
        {
            for(int i = 0; i < size; i++)
            {
                task.doJobTask((V)items[i]);
            }
        }
        return size;
    }

    @Override
    public int executeOn(Executor executor, List<RunnableFuture<Void>> futures)
    {
        int size = backer.size();
        Object[] operands = backer.getOperands();
        
        if(size == 1)
        {
            @SuppressWarnings("unchecked")
            RunnableFuture<Void> f = new FutureTask<Void>(new SingleJobBatch((V)operands[0]), null);
            executor.execute(f);
            futures.add(f);
        }
        else if(this.batchSize == 1)
        {
            for(int i = 0; i < size; i++)
            {
                @SuppressWarnings("unchecked")
                RunnableFuture<Void> f = new FutureTask<Void>(new SingleJobBatch((V)operands[i]), null);
                executor.execute(f);
                futures.add(f);
            }
        }
        else
        {
            int start = 0;
            while(start < size)
            {
                int end = Math.min(size, start + batchSize);
                RunnableFuture<Void> f = new FutureTask<Void>(new CountedJobBatch(start, end, operands), null);
                executor.execute(f);
                start = end;
                futures.add(f);
            }
        }
        
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
                task.doJobTask((V)operands[i]);
            }
        }
    }
}

