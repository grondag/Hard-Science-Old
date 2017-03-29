package grondag.adversity.feature.volcano.lava.simulator;


import java.util.concurrent.Executor;

import grondag.adversity.library.CountedJob;
import grondag.adversity.library.Job;
import grondag.adversity.library.SimpleConcurrentList;
import grondag.adversity.library.SimplePerformanceCounter;
import grondag.adversity.library.CountedJob.CountedJobTask;

public class LavaConnections extends SimpleConcurrentList<LavaConnection>
{
    private final LavaSimulator sim;
    
    @SuppressWarnings("unchecked")
    private final SimpleConcurrentList<LavaConnection>[] sort = new SimpleConcurrentList[4];

    public final Job[] firstStepJob = new CountedJob[4];  
    public final Job[] stepJob = new CountedJob[4];
    
    private final CountedJobTask<LavaConnection> firstStepTask = new CountedJobTask<LavaConnection>()
    {
        @Override
        public void doJobTask(LavaConnection operand)
        {
            operand.doFirstStep(sim.getTickIndex());
        }
    };
    
    private final CountedJobTask<LavaConnection> stepTask = new CountedJobTask<LavaConnection>()
    {
        @Override
        public void doJobTask(LavaConnection operand)
        {
            operand.doStep();
        }
    };
    
    private final CountedJobTask<LavaConnection> sortTask = new CountedJobTask<LavaConnection>()
    {
        @Override
        public void doJobTask(LavaConnection operand)
        {
            SortBucket b = operand.getSortBucket();
            if(b != null) sort[b.ordinal()].add(operand);
        }
    };
    
//    public final SimplePerformanceCounter sortRefreshPerf = new SimplePerformanceCounter();
    
    public static enum SortBucket
    {
        A, B, C, D
    }

    // TODO: make configurable?
    private static final int BATCH_SIZE = 4096;
    
    private final Job sortJob = new CountedJob(this, sortTask, BATCH_SIZE);

    private boolean isSortCurrent = false;
    
    public LavaConnections(LavaSimulator sim)
    {
        super();
        this.sim = sim;
        this.sort[SortBucket.A.ordinal()] = new SimpleConcurrentList<LavaConnection>();
        this.sort[SortBucket.B.ordinal()] = new SimpleConcurrentList<LavaConnection>();
        this.sort[SortBucket.C.ordinal()] = new SimpleConcurrentList<LavaConnection>();
        this.sort[SortBucket.D.ordinal()] = new SimpleConcurrentList<LavaConnection>();
        
        this.firstStepJob[SortBucket.A.ordinal()] = new CountedJob<LavaConnection>(this.sort[SortBucket.A.ordinal()] , firstStepTask, BATCH_SIZE);  
        this.firstStepJob[SortBucket.B.ordinal()] = new CountedJob<LavaConnection>(this.sort[SortBucket.B.ordinal()] , firstStepTask, BATCH_SIZE); 
        this.firstStepJob[SortBucket.C.ordinal()] = new CountedJob<LavaConnection>(this.sort[SortBucket.C.ordinal()] , firstStepTask, BATCH_SIZE); 
        this.firstStepJob[SortBucket.D.ordinal()] = new CountedJob<LavaConnection>(this.sort[SortBucket.D.ordinal()] , firstStepTask, BATCH_SIZE); 
        
        // share same perf counter
        this.firstStepJob[SortBucket.B.ordinal()].perfCounter = this.firstStepJob[SortBucket.A.ordinal()].perfCounter;
        this.firstStepJob[SortBucket.C.ordinal()].perfCounter = this.firstStepJob[SortBucket.A.ordinal()].perfCounter;
        this.firstStepJob[SortBucket.D.ordinal()].perfCounter = this.firstStepJob[SortBucket.A.ordinal()].perfCounter;
        
        this.stepJob[SortBucket.A.ordinal()] = new CountedJob<LavaConnection>(this.sort[SortBucket.A.ordinal()] , stepTask, BATCH_SIZE);  
        this.stepJob[SortBucket.B.ordinal()] = new CountedJob<LavaConnection>(this.sort[SortBucket.B.ordinal()] , stepTask, BATCH_SIZE); 
        this.stepJob[SortBucket.C.ordinal()] = new CountedJob<LavaConnection>(this.sort[SortBucket.C.ordinal()] , stepTask, BATCH_SIZE); 
        this.stepJob[SortBucket.D.ordinal()] = new CountedJob<LavaConnection>(this.sort[SortBucket.D.ordinal()] , stepTask, BATCH_SIZE); 
        
        // share same perf counter
        this.stepJob[SortBucket.B.ordinal()].perfCounter = this.stepJob[SortBucket.A.ordinal()].perfCounter;
        this.stepJob[SortBucket.C.ordinal()].perfCounter = this.stepJob[SortBucket.A.ordinal()].perfCounter;
        this.stepJob[SortBucket.D.ordinal()].perfCounter = this.stepJob[SortBucket.A.ordinal()].perfCounter;
        
        this.isSortCurrent = false;
        
    }
    
    @Override
    public void clear()
    {
        synchronized(this)
        {
            super.clear();
            this.sort[SortBucket.A.ordinal()].clear();
            this.sort[SortBucket.B.ordinal()].clear();
            this.sort[SortBucket.C.ordinal()].clear();
            this.sort[SortBucket.D.ordinal()].clear();
        }
    }
    
    public void createConnectionIfNotPresent(LavaCell first, LavaCell second)
    {
        if(first.id < second.id)
        {
            this.createConnectionIfNotPresentInner(first, second);
        }
        else
        {
            this.createConnectionIfNotPresentInner(second, first);
        }
    }
    
    private void createConnectionIfNotPresentInner(LavaCell first, LavaCell second)
    {
        boolean isIncomplete = true;
        do
        {
            if(first.tryLock())
            {
                if(second.tryLock())
                {
                    if(!first.isConnectedTo(second))
                    {
                        LavaConnection newConnection = new LavaConnection(first, second);
                        this.addConnectionToArray(newConnection);
                    }
                    
                    isIncomplete = false;
                    second.unlock();
                }
                first.unlock();
            }
        } while(isIncomplete);
    }
    
    /** 
     * Adds connection to the storage array and marks sort dirty.
     * Does not do anything else.
     * Thread-safe.
     */
    public void addConnectionToArray(LavaConnection connection)
    {
        this.add(connection);
        this.isSortCurrent = false;
    }
    
    public void invalidateSortBuckets()
    {
        this.isSortCurrent = false;
    }
    
    public void refreshSortBucketsIfNeeded(Executor executor)
    {
        if(this.isSortCurrent) return;
        
//        sortRefreshPerf.startRun();
        
        for(SimpleConcurrentList<LavaConnection> bucket : this.sort)
        {
            bucket.clear();
        }
        
        this.sortJob.runOn(executor);
        
        this.isSortCurrent = true;
        
//        sortRefreshPerf.endRun();
    }
    
//    /**
//     * Returns a stream of LavaConnection instances within the given sort bucket. Stream will be parallel if requested..
//     */
//    public Stream<LavaConnection> getSortStream(SortBucket bucket, boolean isParallel)
//    {
//        if(!isSortCurrent) refreshSortBuckets();
//        return this.sort[bucket.ordinal()].stream(isParallel);
//    }
}
