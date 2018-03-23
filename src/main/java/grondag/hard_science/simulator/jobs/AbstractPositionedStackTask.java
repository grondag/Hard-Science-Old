package grondag.hard_science.simulator.jobs;

import javax.annotation.Nonnull;

import grondag.exotic_matter.serialization.NBTDictionary;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

/**
 * Task with position and stack attributes
 */
public abstract class AbstractPositionedStackTask extends AbstractPositionedTask
{
    private static final String NBT_TASK_STACK = NBTDictionary.claim("taskStack");

    private ItemStack stack;
    
    /**
     * Use for new instances.
     */
    public AbstractPositionedStackTask(@Nonnull BlockPos pos, @Nonnull ItemStack stack)
    {
        super(pos);
        this.stack = stack;
    }
    
    /** Use for deserialization */
    public AbstractPositionedStackTask()
    {
        super();
    }
    
    public ItemStack getStack()
    {
        return this.stack;
    }
    
    public void setStack(ItemStack stack)
    {
        this.stack = stack;
        this.setDirty();
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        super.deserializeNBT(tag);
        this.stack = new ItemStack(tag.getCompoundTag(NBT_TASK_STACK));
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        super.serializeNBT(tag);
        tag.setTag(NBT_TASK_STACK, stack.serializeNBT());
    }
}
