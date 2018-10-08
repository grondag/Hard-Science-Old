package grondag.hard_science.simulator.jobs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.exotic_matter.serialization.NBTDictionary;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

/**
 * Task with a position attribute.
 */
public abstract class AbstractPositionedTask extends AbstractTask
{
    private static final String NBT_TASK_POSITION = NBTDictionary.claim("taskPos");

    private BlockPos pos;
  
    protected AbstractPositionedTask(BlockPos pos)
    {
        super(true);
        this.pos = pos;
    }
    
    protected AbstractPositionedTask()
    {
        super(false);
    }

    @Override
    public void deserializeNBT(@Nullable NBTTagCompound tag)
    {
        super.deserializeNBT(tag);
        this.pos = BlockPos.fromLong(tag.getLong(NBT_TASK_POSITION));
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        super.serializeNBT(tag);
        tag.setLong(NBT_TASK_POSITION, this.pos.toLong());
    }
    
    public BlockPos pos()
    {
        return this.pos;
    }

}
