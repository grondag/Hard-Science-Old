package grondag.hard_science.simulator.base.jobs;

import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.simulator.base.DomainManager.Domain;
import grondag.hard_science.simulator.persistence.IDirtListener;
import grondag.hard_science.simulator.persistence.IDirtListener.NullDirtListener;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class JobManager implements IReadWriteNBT //, IDomainMember
{
    protected static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    
//    protected Domain domain;
    
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
    
    /**
     * Removes job from backlog, if it is found there.
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
     * Removes job from backlog, if it is found there,
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
        job.setJobManager(this);
        EXECUTOR.execute(new Runnable()
        {
            @Override
            public void run()
            {
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
     * Future will contain null if no ready task could be found.
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
            }
        });
    }
    
    /**
     * Called by a job if it has a change in priority.
     */
    public void notifyPriorityChange(Job job)
    {
        if(job.hasReadyWork()) EXECUTOR.execute(new Runnable()
        {
            @Override
            public void run()
            {
                addOrReplaceJobInBacklogSynchronously(job);
            }
        });
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
                    this.jobs.add(j);
                }
            }   
        }        
    }

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
    
    public void setDomain(Domain domain)
    {
//        this.domain = domain;
        this.dirtListener = domain.getDirtListener();
    }

//    @Override
//    public Domain getDomain()
//    {
//        return this.domain;
//    }
    
    protected void setDirty()
    {
        this.dirtListener.setDirty();
    }
}
