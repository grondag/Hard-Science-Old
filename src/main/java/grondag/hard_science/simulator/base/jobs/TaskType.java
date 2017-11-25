package grondag.hard_science.simulator.base.jobs;

import java.util.function.Supplier;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.simulator.base.jobs.tasks.BlockFabricationTask;
import grondag.hard_science.simulator.base.jobs.tasks.BlockProcurementTask;
import grondag.hard_science.simulator.base.jobs.tasks.ExcavationTask;
import grondag.hard_science.simulator.base.jobs.tasks.PlacementTask;
import net.minecraft.nbt.NBTTagCompound;

public enum TaskType
{
    NO_OPERATION(new Supplier<AbstractTask>() { public AbstractTask get() {return null; }}),
    EXCAVATION(new Supplier<AbstractTask>() { public AbstractTask get() {return new ExcavationTask(); }}),
    BLOCK_FABRICATION(new Supplier<AbstractTask>() { public AbstractTask get() {return new BlockFabricationTask(); }}),
    PLACEMENT(new Supplier<AbstractTask>() { public AbstractTask get() {return new PlacementTask(); }}),
    BLOCK_PROCUREMENT(new Supplier<AbstractTask>() { public AbstractTask get() {return new BlockProcurementTask(); }});
    
    private final Supplier<AbstractTask> supplier;
    
    private TaskType(Supplier<AbstractTask> supplier)
    {
        this.supplier = supplier;
    }
    
    public static NBTTagCompound serializeTask(AbstractTask task)
    {
        NBTTagCompound result = task.serializeNBT();
        Useful.saveEnumToTag(result, ModNBTTag.REQUEST_TYPE, task.requestType());
        return result;
    }
    
    public static AbstractTask deserializeTask(NBTTagCompound tag, Job job)
    {
        AbstractTask result = Useful.safeEnumFromTag(tag, ModNBTTag.REQUEST_TYPE, TaskType.NO_OPERATION).supplier.get();
        if(result != null)
        {
            result.job = job;
            result.deserializeNBT(tag);
            result.onLoaded();
        }
        return result;
    }
}
