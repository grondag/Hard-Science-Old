package grondag.hard_science.simulator.jobs;

import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.domain.DomainManager;
import grondag.hard_science.simulator.domain.IDomainMember;
import grondag.hard_science.simulator.persistence.AssignedNumber;
import grondag.hard_science.simulator.persistence.IDirtListener;
import grondag.hard_science.simulator.persistence.NullDirtListener;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class JobManager implements IReadWriteNBT, IDomainMember
{
    /**
     * Should be used for job/task accounting - not for any actual work done by tasks.
     */
    public static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(
        new ThreadFactory()
        {
            private AtomicInteger count = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r)
            {
                Thread thread = new Thread(r, "Hard Science Job Manager Thread -" + count.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            }
        });
    
    
    protected Domain domain;
    
    protected IDirtListener dirtListener = NullDirtListener.INSTANCE;
    /**
     * Job are containers that hold all of our tasks. <br>
     * Jobs manage task serialization and hold shared state like userName and priority. <br>
     * Jobs do not execute directly.
     */
    private final SimpleUnorderedArrayList<Job> jobs = new SimpleUnorderedArrayList<Job>();
    
    /**
     * Contains jobs with tasks that are ready for execution - will typically have a WAITING status.
     * Tasks are posted to the backlog by their parent job when the task becomes ready to execute.
     * Tasks are pulled from the front of the backlog by machines or processes that consume tasks.
     */
    @SuppressWarnings("unchecked")
    private final LinkedList<Job>[] backlogJobs = new LinkedList[RequestPriority.values().length];
    
    public JobManager(Domain domain)
    {
        this.domain = domain;
        this.dirtListener = domain == null ? NullDirtListener.INSTANCE : domain.getDirtListener();
    }

    /**
     * Removes job from backlog, if it is checked there.
     */
    private void removeJobFromBacklogSynchronously(Job job)
    {
        if(job.effectivePriority() != null)
        {
            LinkedList<Job> list = backlogJobs[job.effectivePriority().ordinal()];
            if(list != null && !list.isEmpty())
            {
                list.remove(job);
            }
        }                
    }
    
    /**
     * Removes job from backlog, if it is checked there,
     * and then adds it to the end of the backlog for the
     * job's current priority.
     * 
     * If job was already in backlog for its current priority
     * its position within the backlog remains unchanged.
     */
    private void addOrReplaceJobInBacklogSynchronously(Job job)
    {
        boolean didRemove = false;
        if(job.effectivePriority() != job.getPriority())
        {
            removeJobFromBacklogSynchronously(job);
            job.updateEffectivePriority();
            didRemove = true;
        }
        
        LinkedList<Job> list = backlogJobs[job.effectivePriority().ordinal()];
        if(list == null)
        {
            list = new LinkedList<Job>();
            list.add(job);
            backlogJobs[job.effectivePriority().ordinal()] = list;
        }
        else if(didRemove || !list.contains(job))
        {
            list.addLast(job);
        }
    }
    
    /** Asynchronously adds job to backlog */
    public void addJob(Job job)
    {
        EXECUTOR.execute(new Runnable()
        {
            @Override
            public void run()
            {
                job.onJobAdded(JobManager.this);
                jobs.addIfNotPresent(job);
                if(job.hasReadyWork()) addOrReplaceJobInBacklogSynchronously(job);
            }
        });
    }
    
    private static final Predicate<AbstractTask> MATCH_ANY_TASK = new Predicate<AbstractTask>()
    {
        @Override
        public boolean test(AbstractTask t)
        {
            return true;
        }
    };
    
    /**
     * Searches for the fist ready task of the given type that meets the given predicate.
     * The status of the task is immediately changed to ACTIVE when it is claimed.
     * Future will contain null if no ready task could be checked.
     */
    public Future<AbstractTask> claimReadyWork(TaskType taskType, @Nullable Predicate<AbstractTask> predicate)
    {
        return EXECUTOR.submit(new Callable<AbstractTask>()
        {
            @Override
            public AbstractTask call() throws Exception
            {
                Predicate<AbstractTask> effectivePredicate = predicate == null ? MATCH_ANY_TASK : predicate;
                
                for(LinkedList<Job> list : backlogJobs)
                {
                    if(list != null && !list.isEmpty())
                    {
                        for(Job j : list)
                        {
                            if(j.hasReadyWork())
                            {
                                for(AbstractTask t : j)
                                {
                                    if(t.requestType() == taskType
                                            && t.getStatus() == RequestStatus.READY
                                            && effectivePredicate.test(t))
                                    {
                                        t.claim();
                                        return t;
                                    }
                                }
                            }
                        }
                    }
                }
                return null;
            }
        });
    }
    
    /**
     * Called by a job if it becomes ready or unready to execute.
     * Job manager will assume hasReadyWork is opposite of prior value.
     * (Do not call unless value changes.)
     * Call executes asynchronously.
     */
    public void notifyReadyStatus(Job job)
    {
        EXECUTOR.execute(new Runnable()
        {
            @Override
            public void run()
            {
                if(job.hasReadyWork())
                    addOrReplaceJobInBacklogSynchronously(job);
                else
                    removeJobFromBacklogSynchronously(job);
            }
        });
    }
    
    /**
     * Called by a job when it terminates.
     * Call executes asynchronously.
     */
    public void notifyTerminated(Job job)
    {
        EXECUTOR.execute(new Runnable()
        {
            @Override
            public void run()
            {
                removeJobFromBacklogSynchronously(job);
                jobs.removeIfPresent(job);
                
                // clean up id registry
                for(AbstractTask task : job)
                {
                    DomainManager.instance().assignedNumbersAuthority().unregister(task);
                }
                DomainManager.instance().assignedNumbersAuthority().unregister(job);
            }
        });
    }
    
    /**
     * Called by a job if it has a change in priority.
     */
    public void notifyPriorityChange(Job job)
    {
        if(!job.isHeld() && job.hasReadyWork()) EXECUTOR.execute(new Runnable()
        {
            @Override
            public void run()
            {
                addOrReplaceJobInBacklogSynchronously(job);
            }
        });
    }
    
    /**
     * Called by job when it is held or released.
     */
    public void notifyHoldChange(Job job)
    {
        if(job.isHeld())
        {
            EXECUTOR.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    removeJobFromBacklogSynchronously(job);
                }
            });
        }
        else if(job.hasReadyWork())
        {
            EXECUTOR.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    addOrReplaceJobInBacklogSynchronously(job);
                }
            });
        }
        
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        NBTTagList nbtJobs = tag.getTagList(ModNBTTag.REQUEST_CHILDREN, 10);
        if( nbtJobs != null && !nbtJobs.hasNoTags())
        {
            for(NBTBase subTag : nbtJobs)
            {
                if(subTag != null)
                {
                    Job j = new Job(this, (NBTTagCompound) subTag);
                    if(!j.getStatus().isTerminated) this.jobs.add(j);
                }
            }   
        }        
    }

    /**
     * Called after all domain deserialization is complete.  
     * Hook for tasks to handle actions that may require other objects to be deserialized start.
     */
    public void afterDeserialization()
    {
        if(!this.jobs.isEmpty())
        {
            for(Job j : this.jobs)
            {
                j.afterDeserialization();
            }
        }    
    };
    
    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        if(!this.jobs.isEmpty())
        {
            NBTTagList nbtJobs = new NBTTagList();
            
            for(Job j : this.jobs)
            {
                nbtJobs.appendTag(j.serializeNBT());
            }
            tag.setTag(ModNBTTag.REQUEST_CHILDREN, nbtJobs);
        }        
    }
    
    @Override
    public Domain getDomain()
    {
        return this.domain;
    }
    
    protected void setDirty()
    {
        this.dirtListener.setDirty();
    }

    /**
     * Returns estimated count of tasks of given type in the queue.
     */
    public int getQueueDepth(TaskType blockFabrication)
    {
        // TODO Not a real implementation
        return this.domain.domainManager().assignedNumbersAuthority().getIndex(AssignedNumber.TASK).size();
    }
}
