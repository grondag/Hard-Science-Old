package grondag.hard_science.simulator.base.jobs.tasks;

import javax.annotation.Nonnull;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.simulator.base.jobs.AbstractTask;
import grondag.hard_science.simulator.base.jobs.TaskType;
import net.minecraft.nbt.NBTTagCompound;

public class BlockFabricationTask extends AbstractTask
{
    private int procurementTaskID;
    
    /** 
     * Don't use directly - lazily deserialized.
     */
    private BlockProcurementTask procurementTask;
    
    /**
     * Use for new instances. Automatically
     * make procurement task dependent on this task.
     */
    public BlockFabricationTask(@Nonnull BlockProcurementTask procurementTask)
    {
        super(true);
        this.procurementTaskID = procurementTask.getId();
        this.procurementTask = procurementTask;
        AbstractTask.link(this, procurementTask);
    }
    
    /** Use for deserialization */
    public BlockFabricationTask()
    {
        super(false);
    }
    
    @Override
    public TaskType requestType()
    {
        return TaskType.BLOCK_FABRICATION;
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
            this.procurementTask = (BlockProcurementTask) this.getDomain().domainManager().assignedNumbersAuthority().taskIndex().get(procurementTaskID);
        }
        return this.procurementTask;
    }
}
