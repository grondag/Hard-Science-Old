package grondag.hard_science.simulator.jobs;

import javax.annotation.Nonnull;

import grondag.hard_science.library.serialization.ModNBTTag;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

/**
 * Task with a position attribute.
 */
public abstract class AbstractPositionedTask extends AbstractTask
{
    private BlockPos pos;
  
    protected AbstractPositionedTask(@Nonnull BlockPos pos)
    {
        super(true);
        this.pos = pos;
    }
    
    protected AbstractPositionedTask()
    {
        super(false);
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        super.deserializeNBT(tag);
        this.pos = BlockPos.fromLong(tag.getLong(ModNBTTag.TASK_POSITION));
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        super.serializeNBT(tag);
        tag.setLong(ModNBTTag.TASK_POSITION, this.pos.toLong());
    }
    
    public BlockPos pos()
    {
        return this.pos;
    }

}
