package grondag.hard_science.simulator.base.jobs.tasks;

import grondag.hard_science.simulator.base.jobs.AbstractTask;
import grondag.hard_science.simulator.base.jobs.TaskType;
import grondag.hard_science.superblock.placement.spec.AbstractPlacementSpec;
import grondag.hard_science.superblock.placement.spec.AbstractPlacementSpec.PlacementSpecEntry;

/**
 * Stores the placement specification for a construction 
 * job and generates all the necessary tasks in the job. <p>
 *
 * Runs on the build manager execution queue and submits self for execution.<p>
 */
public class BuildPlanningTask  extends AbstractTask
{
    private AbstractPlacementSpec spec;
    
    /** ID of next spec entry we need to plan.  */
    private int indexOfNextSpecToPlan = 0;
    
    /** use this constructor to create new jobs */
    public BuildPlanningTask(AbstractPlacementSpec spec)
    {
        super(true);
        this.spec = spec;
    }
    
    /** This constructor meant for deserialization only. */
    public BuildPlanningTask()
    {
        super(false);
    }
    
    @Override
    public TaskType requestType()
    {
        return TaskType.BUILD_PLANNING;
    }
    
    
    private void planEntry(PlacementSpecEntry entry)
    {
        if(entry.isExcavation())
        {
            // schedule excavation if necessary
            ExcavationTask exTask = new ExcavationTask(entry);
            link(this, exTask);
            entry.excavationTaskID = exTask.getId();
            this.job.addTask(exTask);
        }
        else
        {
            // schedule procurement/fabrication
            BlockProcurementTask procTask = new BlockProcurementTask(entry);
            link(this, procTask);
            entry.procurementTaskID = procTask.getId();
            this.job.addTask(procTask);
            
            // schedule placement
            PlacementTask placeTask = new PlacementTask(entry);
            link(procTask, placeTask);
            entry.placementTaskID = placeTask.getId();
            this.job.addTask(placeTask);
        }
    }
}
