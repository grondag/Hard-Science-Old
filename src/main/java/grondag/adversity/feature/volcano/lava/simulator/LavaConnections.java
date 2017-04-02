package grondag.adversity.feature.volcano.lava.simulator;


import java.util.concurrent.Executor;

import grondag.adversity.library.CountedJob;
import grondag.adversity.library.Job;
import grondag.adversity.library.SimpleConcurrentList;
import grondag.adversity.library.CountedJob.CountedJobTask;
import grondag.adversity.simulator.Simulator;

@SuppressWarnings("unused")
public class LavaConnections
{
    private final SimpleConcurrentList<LavaConnection> connectionList;
    
    @SuppressWarnings("unchecked")
    private final SimpleConcurrentList<LavaConnection>[] sort = new SimpleConcurrentList[4];

    public final Job[] firstStepJob = new CountedJob[4];  
    public final Job[] stepJob = new CountedJob[4];
    
    private final CountedJobTask<LavaConnection> firstStepTask = new CountedJobTask<LavaConnection>()
    {
        @Override
        public void doJobTask(LavaConnection operand)
        {
            operand.doFirstStep();
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
    
    private final CountedJobTask<LavaConnection> setupTickTask = new CountedJobTask<LavaConnection>()
    {
        @Override
        public void doJobTask(LavaConnection operand)
        {
            operand.setupTick();
        }
    };
    
//    public final SimplePerformanceCounter sortRefreshPerf = new SimplePerformanceCounter();
    
    public static enum SortBucket
    {
        A, B, C, D
    }
    
    public static enum FlowDirection
    {
        ONE_TO_TWO, TWO_TO_ONE, NONE
    }

    // TODO: make configurable?
    private static final int BATCH_SIZE = 4096;
    
    private final Job sortJob;

    public final Job setupTickJob;   
    
    private boolean isSortCurrent = false;
    
    public LavaConnections(LavaSimulator sim)
    {
        super();
        connectionList = SimpleConcurrentList.create(LavaSimulator.ENABLE_PERFORMANCE_COUNTING, "Lava Connections", sim.perfCollectorOffTick);
        
        this.sort[SortBucket.A.ordinal()] = SimpleConcurrentList.create(LavaSimulator.ENABLE_PERFORMANCE_COUNTING, "Sort Bucket", sim.perfCollectorOffTick);
        this.sort[SortBucket.B.ordinal()] = SimpleConcurrentList.create(this.sort[SortBucket.A.ordinal()].removalPerfCounter());
        this.sort[SortBucket.C.ordinal()] = SimpleConcurrentList.create(this.sort[SortBucket.A.ordinal()].removalPerfCounter());
        this.sort[SortBucket.D.ordinal()] = SimpleConcurrentList.create(this.sort[SortBucket.A.ordinal()].removalPerfCounter());
        
        this.firstStepJob[SortBucket.A.ordinal()] = new CountedJob<LavaConnection>(this.sort[SortBucket.A.ordinal()] , firstStepTask, BATCH_SIZE,
                LavaSimulator.ENABLE_PERFORMANCE_COUNTING, "First Flow Step", sim.perfCollectorOffTick);  
        this.firstStepJob[SortBucket.B.ordinal()] = new CountedJob<LavaConnection>(this.sort[SortBucket.B.ordinal()] , firstStepTask, BATCH_SIZE,
                this.firstStepJob[SortBucket.A.ordinal()].perfCounter); 
        this.firstStepJob[SortBucket.C.ordinal()] = new CountedJob<LavaConnection>(this.sort[SortBucket.C.ordinal()] , firstStepTask, BATCH_SIZE,
                this.firstStepJob[SortBucket.A.ordinal()].perfCounter); 
        this.firstStepJob[SortBucket.D.ordinal()] = new CountedJob<LavaConnection>(this.sort[SortBucket.D.ordinal()] , firstStepTask, BATCH_SIZE,
                this.firstStepJob[SortBucket.A.ordinal()].perfCounter); 
        
        this.stepJob[SortBucket.A.ordinal()] = new CountedJob<LavaConnection>(this.sort[SortBucket.A.ordinal()] , stepTask, BATCH_SIZE,
                LavaSimulator.ENABLE_PERFORMANCE_COUNTING, "Flow Step", sim.perfCollectorOffTick);  
        this.stepJob[SortBucket.B.ordinal()] = new CountedJob<LavaConnection>(this.sort[SortBucket.B.ordinal()] , stepTask, BATCH_SIZE,
                this.stepJob[SortBucket.A.ordinal()].perfCounter); 
        this.stepJob[SortBucket.C.ordinal()] = new CountedJob<LavaConnection>(this.sort[SortBucket.C.ordinal()] , stepTask, BATCH_SIZE,
                this.stepJob[SortBucket.A.ordinal()].perfCounter); 
        this.stepJob[SortBucket.D.ordinal()] = new CountedJob<LavaConnection>(this.sort[SortBucket.D.ordinal()] , stepTask, BATCH_SIZE,
                this.stepJob[SortBucket.A.ordinal()].perfCounter); 
        
        sortJob = new CountedJob<LavaConnection>(this.connectionList, sortTask, BATCH_SIZE,
                LavaSimulator.ENABLE_PERFORMANCE_COUNTING, "Connection Sorting", sim.perfCollectorOffTick);    

        setupTickJob = new CountedJob<LavaConnection>(this.connectionList, setupTickTask, BATCH_SIZE,
                LavaSimulator.ENABLE_PERFORMANCE_COUNTING, "Tick Setup", sim.perfCollectorOffTick);    
        
        this.isSortCurrent = false;
        
    }
    
    public void clear()
    {
        synchronized(this)
        {
            this.connectionList.clear();
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
        this.connectionList.add(connection);
        this.isSortCurrent = false;
    }
    
    public void removeDeletedItems()
    {
        this.connectionList.removeDeletedItems();
    }
    
    public int size()
    {
        return this.connectionList.size();
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
