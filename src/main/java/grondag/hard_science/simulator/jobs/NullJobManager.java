package grondag.hard_science.simulator.jobs;

import net.minecraft.nbt.NBTTagCompound;

/** version of job manager that eats all method calls */
public class NullJobManager extends JobManager
{
    public static final NullJobManager INSTANCE = new NullJobManager();
    private NullJobManager()
    {
        super(null);
    }
    
    @Override
    public void notifyReadyStatus(Job job)
    {
        // NOOP
    }
    
    @Override
    public void notifyTerminated(Job job)
    {
        // NOOP
    }
    
    @Override
    public void notifyPriorityChange(Job job)
    {
        // NOOP
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        // NOOP
    }
    
    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        // NOOP
    }
    
    @Override
    protected void setDirty()
    {
        // NOOP
    };
    
    
}
