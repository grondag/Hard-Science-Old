package grondag.hard_science.simulator.base.jobs;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Test;

import grondag.hard_science.simulator.jobs.AbstractTask;
import grondag.hard_science.simulator.jobs.Job;
import grondag.hard_science.simulator.jobs.JobManager;
import grondag.hard_science.simulator.jobs.RequestPriority;
import grondag.hard_science.simulator.jobs.RequestStatus;
import grondag.hard_science.simulator.jobs.TaskType;
import grondag.hard_science.simulator.jobs.tasks.BlockFabricationTask;
import grondag.hard_science.simulator.jobs.tasks.ExcavationTask;
import grondag.hard_science.simulator.jobs.tasks.PlacementTask;

public class JobTest
{

    @Test
    public void test() throws InterruptedException, ExecutionException
    {
        JobManager jm = new JobManager();
        
        ExcavationTask e1 = new ExcavationTask();
        ExcavationTask e2 = new ExcavationTask();
        ExcavationTask e3 = new ExcavationTask();
        
        BlockFabricationTask f1 = new BlockFabricationTask();
        BlockFabricationTask f2 = new BlockFabricationTask();
        BlockFabricationTask f3 = new BlockFabricationTask();
        
        PlacementTask p1 = new PlacementTask();
        PlacementTask p2 = new PlacementTask();
        PlacementTask p3 = new PlacementTask();
        
        AbstractTask.link(e1, p1);
        AbstractTask.link(e2, p2);
        AbstractTask.link(e3, p3);
        
        AbstractTask.link(f1, p1);
        AbstractTask.link(f2, p2);
        AbstractTask.link(f3, p3);
        
        AbstractTask.link(p1, p2);
        AbstractTask.link(p2, p3);
        
        Job j1 = new Job();
        j1.addTasks(e1, e2, e3, f1, f2, f3, p1, p2);
        
        assert j1.hasReadyWork();
        assert j1.getStatus() == RequestStatus.READY;
        
        jm.addJob(j1);
        
        Future<AbstractTask> f = jm.claimReadyWork(TaskType.PLACEMENT, null);
        assert f.get() == null;
        
        f = jm.claimReadyWork(TaskType.EXCAVATION, null);
        assert f.get() == e1;
        
        f = jm.claimReadyWork(TaskType.BLOCK_FABRICATION, null);
        assert f.get() == f1;
        
        f = jm.claimReadyWork(TaskType.PLACEMENT, null);
        assert f.get() == null;

        e1.complete();
        f1.complete();
        
        // dependencies met, p1 should now be ready
        f = jm.claimReadyWork(TaskType.PLACEMENT, null);
        assert f.get() == p1;
        
        // no other placements should be ready
        f = jm.claimReadyWork(TaskType.PLACEMENT, null);
        assert f.get() == null;
        
        // if abandon p1, should get it back again
        p1.abandon();
        
        f = jm.claimReadyWork(TaskType.PLACEMENT, null);
        assert f.get() == p1;
        
        p1.complete();
        
        f = jm.claimReadyWork(TaskType.PLACEMENT, null);
        assert f.get() == null;
        
        ExcavationTask e4 = new ExcavationTask();
        ExcavationTask e5 = new ExcavationTask();
        
        Job j2 = new Job();
        j2.addTasks(e4, e5);
        
        jm.addJob(j2);
        
        // j2 has same priority as j1 so should get next task from j1
        f = jm.claimReadyWork(TaskType.EXCAVATION, null);
        assert f.get() == e2;
        e2.complete();
        
        // now reprioritize j2 ahead of j1
        j2.setPriority(RequestPriority.HIGH);
        
        // now should get task from j2
        f = jm.claimReadyWork(TaskType.EXCAVATION, null);
        assert f.get() == e4;
        
        // j2 doesn't have fab tasks, so should still get that from j1
        f = jm.claimReadyWork(TaskType.BLOCK_FABRICATION, null);
        assert f.get() == f2;
        
        // now cancel j2, should get e3
        e4.abandon();
        j2.cancel();
        
        f = jm.claimReadyWork(TaskType.EXCAVATION, null);
        assert f.get() == e3;
    }

}
