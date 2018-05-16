package grondag.hard_science.simulator.jobs.tasks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.exotic_matter.serialization.NBTDictionary;
import grondag.hard_science.simulator.jobs.AbstractTask;
import grondag.hard_science.simulator.jobs.ITask;
import grondag.hard_science.simulator.jobs.TaskType;
import net.minecraft.nbt.NBTTagCompound;

public class BlockFabricationTask extends AbstractTask
{
    private static final String NBT_PROCUREMENT_TASK_ID = NBTDictionary.claim("procTaskID");

    private int procurementTaskID;
    
    /** 
     * Don't use directly - lazily deserialized.
     */
    private @Nullable BlockProcurementTask procurementTask;
    
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
    public void deserializeNBT(@Nullable NBTTagCompound tag)
    {
        super.deserializeNBT(tag);
        this.procurementTaskID = tag.getInteger(NBT_PROCUREMENT_TASK_ID);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        super.serializeNBT(tag);
        tag.setInteger(NBT_PROCUREMENT_TASK_ID, this.procurementTaskID);
    }
    
    @SuppressWarnings("null")
    public BlockProcurementTask procurementTask()
    {
        if(this.procurementTask == null)
        {
            this.procurementTask = (BlockProcurementTask) ITask.taskFromId(procurementTaskID);
        }
        return this.procurementTask;
    }
}
