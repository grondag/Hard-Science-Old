package grondag.hard_science.simulator.base.jobs;

import javax.annotation.Nonnull;

import grondag.hard_science.library.serialization.ModNBTTag;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public abstract class BuildingTask extends AbstractTask
{
    private BlockPos pos;
  
    protected BuildingTask(@Nonnull BlockPos pos)
    {
        super(true);
        this.pos = pos;
    }
    
    protected BuildingTask()
    {
        super(false);
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        super.deserializeNBT(tag);
        this.pos = BlockPos.fromLong(tag.getLong(ModNBTTag.BUILDING_TASK_POSITION));
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        super.serializeNBT(tag);
        tag.setLong(ModNBTTag.BUILDING_TASK_POSITION, this.pos.toLong());
    }
    
    public BlockPos pos()
    {
        return this.pos;
    }

}
