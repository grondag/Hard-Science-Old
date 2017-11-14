package grondag.hard_science.simulator.base.jobs;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.simulator.base.DomainManager;
import grondag.hard_science.simulator.base.jobs.tasks.BuildPlanningTask;
import grondag.hard_science.superblock.placement.AbstractPlacementSpec.PlacementSpecEntry;
import net.minecraft.nbt.NBTTagCompound;

public abstract class BuildingTask extends AbstractTask
{
    /**
     * Use for new instances.
     */
    protected BuildingTask(BuildPlanningTask planningTask, PlacementSpecEntry entry)
    {
        super(true);
        this.planningTaskID = planningTask.getId();
        this.entryIndex = entry.index();
        this.entry = entry;
    }
    
    /**
     * Use for deserialization.
     */
    protected BuildingTask()
    {
        super(false);
    }
    
    /**
     * Used to locate the planning task containing build
     * specification after deserialization.
     */
    protected int planningTaskID;
    
    /**
     * Identifies what position/stack we are handling within
     * the build specification.
     */
    protected int entryIndex;
    
    /**
     * The position/stack info we are handling.
     * Lazily retrieved via {@link #entry()}
     */
    private PlacementSpecEntry entry;
    
    /**
     * The position/stack info we are handling.
     */
    public PlacementSpecEntry entry()
    {
        if(this.entry == null)
        {
            BuildPlanningTask plan = (BuildPlanningTask) DomainManager.INSTANCE.assignedNumbersAuthority().taskIndex().get(this.planningTaskID);
            if(plan != null)
            {
                this.entry = plan.spec().entries().get(this.entryIndex);
            }
        }
        return this.entry;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        super.deserializeNBT(tag);
        this.planningTaskID = tag.getInteger(ModNBTTag.BUILDING_TASK_PLAN_TASK_ID);
        this.entryIndex = tag.getInteger(ModNBTTag.BUILDING_TASK_ENTRY_INDEX);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        super.serializeNBT(tag);
        tag.setInteger(ModNBTTag.BUILDING_TASK_PLAN_TASK_ID, this.planningTaskID);
        tag.setInteger(ModNBTTag.BUILDING_TASK_ENTRY_INDEX, this.entryIndex);
    }
    
  

}
