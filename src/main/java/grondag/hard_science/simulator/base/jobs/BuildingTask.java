package grondag.hard_science.simulator.base.jobs;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.superblock.placement.spec.PlacementSpecEntry;
import net.minecraft.nbt.NBTTagCompound;

public abstract class BuildingTask extends AbstractTask
{
    /**
     * Use for new instances.
     */
    protected BuildingTask(PlacementSpecEntry entry)
    {
        super(true);
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
            this.entry = this.job.spec().entries().get(this.entryIndex);
        }
        return this.entry;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        super.deserializeNBT(tag);
        this.entryIndex = tag.getInteger(ModNBTTag.BUILDING_TASK_ENTRY_INDEX);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        super.serializeNBT(tag);
        tag.setInteger(ModNBTTag.BUILDING_TASK_ENTRY_INDEX, this.entryIndex);
    }
    
  

}
