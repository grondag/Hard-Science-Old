package grondag.hard_science.simulator.domain;

import java.util.HashMap;

import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class ProcessManager implements IReadWriteNBT, IDomainMember
{
    private final Domain domain;
    
    private final HashMap<IResource<?>, ProcessInfo> infos = new HashMap<>();

    private static class ProcessInfo
    {
        
        private final IResource<?> resource;
        private long minimumStockingLevel;
        private int priority;
        
        private ProcessInfo(IResource<?> resource)
        {
            this.resource = resource;
        }
        
        private ProcessInfo(NBTTagCompound tag)
        {
            this.resource = StorageType.fromNBTWithType(tag);
            this.minimumStockingLevel = tag.getLong(ModNBTTag.PROCESS_MIN_STOCKING_LEVEL);
            this.priority = tag.getInteger(ModNBTTag.PROCESS_STOCKING_PRIORITY);
        }
        
        private NBTTagCompound toNBT()
        {
            NBTTagCompound result = StorageType.toNBTWithType(resource);
            result.setLong(ModNBTTag.PROCESS_MIN_STOCKING_LEVEL, minimumStockingLevel);
            result.setInteger(ModNBTTag.PROCESS_STOCKING_PRIORITY, priority);
            return result;
        }
    }
    
    ProcessManager(Domain domain)
    {
        this.domain = domain;
    }
    
    public void setMinStockingLevel(IResource<?> resource, int level)
    {
        ProcessInfo pi = this.infos.get(resource);
        if(pi == null)
        {
            pi = new ProcessInfo(resource);
            this.infos.put(resource, pi);
        }
        pi.minimumStockingLevel = level;
    }
    
    public long getMinStockingLevel(IResource<?> resource)
    {
        ProcessInfo pi = this.infos.get(resource);
        return pi == null ? 0 : pi.minimumStockingLevel;
    }
    
    public void setStockingPriority(IResource<?> resource, int priority)
    {
        ProcessInfo pi = this.infos.get(resource);
        if(pi == null)
        {
            pi = new ProcessInfo(resource);
            this.infos.put(resource, pi);
        }
        pi.priority = priority;
    }
    
    public int getStockingPriority(IResource<?> resource)
    {
        ProcessInfo pi = this.infos.get(resource);
        return pi == null ? 0 : pi.priority;
    }
    
    @Override
    public Domain getDomain()
    {
        return this.domain;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        this.infos.clear();
        
        if(!this.infos.containsKey(ModNBTTag.PROCESS_SETTINGS)) return;
        
        NBTTagList tags = tag.getTagList(ModNBTTag.PROCESS_SETTINGS, 10);
        tags.forEach(t ->
        {
            ProcessInfo pi = new ProcessInfo((NBTTagCompound)t);
            this.infos.put(pi.resource, pi);
        });
        
        tag.setTag(ModNBTTag.PROCESS_SETTINGS, tags);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        if(this.infos.isEmpty()) return;
        
        NBTTagList tags = new NBTTagList();
        for(ProcessInfo pi : this.infos.values())
        {
            tags.appendTag(pi.toNBT());
        }
        tag.setTag(ModNBTTag.PROCESS_SETTINGS, tags);
    }
    
}
