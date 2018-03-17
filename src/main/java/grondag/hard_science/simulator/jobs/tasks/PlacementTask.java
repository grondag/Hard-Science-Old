package grondag.hard_science.simulator.jobs.tasks;

import javax.annotation.Nonnull;

import grondag.hard_science.init.ModNBTTag;
import grondag.hard_science.simulator.domain.DomainManager;
import grondag.hard_science.simulator.jobs.AbstractTask;
import grondag.hard_science.simulator.jobs.TaskType;
import net.minecraft.nbt.NBTTagCompound;

/**
 * For placing blocks. Relies on procurement task for data.
 *
 */
public class PlacementTask extends AbstractTask
{
    private int procurementTaskID;
    
    /** 
     * Don't use directly - lazily deserialized.
     */
    private BlockProcurementTask procurementTask;
    
    /**
     * Use for new instances. Creates dependency on input task.
     */
    public PlacementTask(@Nonnull BlockProcurementTask procurementTask)
    {
        super(true);
        this.procurementTaskID = procurementTask.getId();
        this.procurementTask = procurementTask;
        AbstractTask.link(procurementTask, this);
    }
    
    /** Use for deserialization */
    public PlacementTask()
    {
        super(false);
    }
    
    @Override
    public TaskType requestType()
    {
        return TaskType.PLACEMENT;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        super.deserializeNBT(tag);
        this.procurementTaskID = tag.getInteger(ModNBTTag.PROCUREMENT_TASK_ID);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        super.serializeNBT(tag);
        tag.setInteger(ModNBTTag.PROCUREMENT_TASK_ID, this.procurementTaskID);
    }
    
    public BlockProcurementTask procurementTask()
    {
        if(this.procurementTask == null)
        {
            this.procurementTask = (BlockProcurementTask) DomainManager.taskFromId(procurementTaskID);
        }
        return this.procurementTask;
    }
}
