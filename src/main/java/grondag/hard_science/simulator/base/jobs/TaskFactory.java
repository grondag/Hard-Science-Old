package grondag.hard_science.simulator.base.jobs;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.Useful;
import net.minecraft.nbt.NBTTagCompound;

/**
 * handles ugliness of request serialization/deserialization
 * @author grondag
 */
public class TaskFactory
{
    public static NBTTagCompound serializeTask(AbstractTask task)
    {
        NBTTagCompound result = task.serializeNBT();
        Useful.saveEnumToTag(result, ModNBTTag.REQUEST_TYPE, task.requestType());
        return result;
    }
    
    public static AbstractTask deserializeTask(NBTTagCompound tag, Job job)
    {
        switch(Useful.safeEnumFromTag(tag, ModNBTTag.REQUEST_TYPE, TaskType.NO_OPERATION))
        {
        case BLOCK_FABRICATION:
            return null;
        case EXCAVATION:
            return null;
        case PLACEMENT:
            return null;
            
        case NO_OPERATION:
        default:
            return null;
        
        }
    }
    
}
