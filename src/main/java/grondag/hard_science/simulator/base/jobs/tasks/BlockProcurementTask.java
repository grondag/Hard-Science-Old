package grondag.hard_science.simulator.base.jobs.tasks;

import javax.annotation.Nonnull;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.simulator.base.jobs.BuildingTask;
import grondag.hard_science.simulator.base.jobs.TaskType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class BlockProcurementTask extends BuildingTask
{
    private ItemStack stack;
    
    /**
     * Use for new instances.
     */
    public BlockProcurementTask(@Nonnull BlockPos pos, @Nonnull ItemStack stack)
    {
        super(pos);
        this.stack = stack;
    }
    
    /** Use for deserialization */
    public BlockProcurementTask()
    {
        super();
    }
    
    @Override
    public TaskType requestType()
    {
        return TaskType.BLOCK_PROCUREMENT;
    }
    
    public ItemStack stack()
    {
        return this.stack;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        super.deserializeNBT(tag);
        this.stack = new ItemStack(tag.getCompoundTag(ModNBTTag.BUILDING_TASK_STACK));
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        super.serializeNBT(tag);
        tag.setTag(ModNBTTag.BUILDING_TASK_STACK, stack.serializeNBT());
    }
    
    
}
