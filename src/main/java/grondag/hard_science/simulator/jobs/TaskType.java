package grondag.hard_science.simulator.jobs;

import java.util.function.Supplier;

import grondag.exotic_matter.varia.Useful;
import grondag.hard_science.init.ModNBTTag;
import grondag.hard_science.simulator.jobs.tasks.BlockFabricationTask;
import grondag.hard_science.simulator.jobs.tasks.BlockProcurementTask;
import grondag.hard_science.simulator.jobs.tasks.ExcavationTask;
import grondag.hard_science.simulator.jobs.tasks.PerpetualTask;
import grondag.hard_science.simulator.jobs.tasks.PlacementTask;
import net.minecraft.nbt.NBTTagCompound;

public enum TaskType
{
    NO_OPERATION(new Supplier<AbstractTask>() { @Override
    public AbstractTask get() {return null; }}),
    EXCAVATION(new Supplier<AbstractTask>() { @Override
    public AbstractTask get() {return new ExcavationTask(); }}),
    BLOCK_FABRICATION(new Supplier<AbstractTask>() { @Override
    public AbstractTask get() {return new BlockFabricationTask(); }}),
    PLACEMENT(new Supplier<AbstractTask>() { @Override
    public AbstractTask get() {return new PlacementTask(); }}),
    BLOCK_PROCUREMENT(new Supplier<AbstractTask>() { @Override
    public AbstractTask get() {return new BlockProcurementTask(); }}), 
//    SIMPLE_PROCUREMENT(new Supplier<AbstractTask>()
//    { 
//        @SuppressWarnings("rawtypes")
//        public AbstractTask get() {return new SimpleProcurementTask(); }
//    }),
//    DELIVERY(new Supplier<AbstractTask>()
//    { 
//        @SuppressWarnings("rawtypes")
//        public AbstractTask get() {return new DeliveryTask(); }
//    }), 
    PERPETUAL(new Supplier<AbstractTask>() { @Override
    public AbstractTask get() {return new PerpetualTask(); }}), 
    ;
    
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
