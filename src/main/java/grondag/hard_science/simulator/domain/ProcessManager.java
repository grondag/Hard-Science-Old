package grondag.hard_science.simulator.domain;

import java.util.HashMap;

import grondag.hard_science.Configurator;
import grondag.hard_science.Log;
import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class ProcessManager implements IReadWriteNBT, IDomainMember
{
    private final Domain domain;
    
    private final HashMap<IResource<?>, ProcessInfo> infos = new HashMap<>();
    
    public static class ProcessInfo
    {
        
        private final IResource<?> resource;
        private long minStockLevel;
        private long maxStockLevel;
        private int priority;
        
        private ProcessInfo(IResource<?> resource)
        {
            this.resource = resource;
        }
        
        private ProcessInfo(NBTTagCompound tag)
        {
            this.resource = StorageType.fromNBTWithType(tag);
            this.minStockLevel = tag.getLong(ModNBTTag.PROCESS_MIN_STOCKING_LEVEL);
            this.maxStockLevel = tag.getLong(ModNBTTag.PROCESS_MAX_STOCKING_LEVEL);
            this.priority = tag.getInteger(ModNBTTag.PROCESS_STOCKING_PRIORITY);
        }
        
        /**
         * Used for setting up default config file.
         * Note that fluid amounts are in liters.
         */
        public static String writeDefaultCSV(
                IResource<?> resource, 
                int priority,
                long minStockLevel,
                long maxStockLevel)
        {
            return  priority + "," +
                    minStockLevel + "," +
                    maxStockLevel + "," +
                    StorageType.toCSVWithType(resource);
        }
        
        private static ProcessInfo readDefaultCSV(String csv)
        {
            try
            {
                String args[] = csv.split(",");
                if(args.length > 3)
                {
                    int myLen = args[0].length() + args[1].length()
                            + args[2].length() + 3;
                    IResource<?> res = StorageType.fromCSVWithType(csv.substring(myLen));
                    ProcessInfo result = new ProcessInfo(res);
                    result.priority = Integer.parseInt(args[0]);
                    result.minStockLevel = VolumeUnits.liters2nL(Long.parseLong(args[1]));
                    result.maxStockLevel = VolumeUnits.liters2nL(Long.parseLong(args[2]));
                    return result;
                }
            }
            catch(Exception e)
            {
                Log.error("Unable to parse resource processing configuration", e);
            }
            return null;
        }
        
        private NBTTagCompound toNBT()
        {
            NBTTagCompound result = StorageType.toNBTWithType(resource);
            result.setLong(ModNBTTag.PROCESS_MIN_STOCKING_LEVEL, minStockLevel);
            result.setInteger(ModNBTTag.PROCESS_STOCKING_PRIORITY, priority);
            return result;
        }
        
        public IResource<?> resource() { return this.resource; }
        public long minStockLevel() { return this.minStockLevel; }
        public long maxStockLevel() { return this.maxStockLevel; }
        public int priority() { return this.priority; }
    }
    
    
    ProcessManager(Domain domain)
    {
        this.domain = domain;
        
        //load defaults
        for(String csv : Configurator.PROCESSING.resourceDefaults)
        {
            ProcessInfo info = ProcessInfo.readDefaultCSV(csv);
            if(info != null) this.infos.put(info.resource, info);
        }
        
    }
    
    public ProcessInfo getInfo(IResource<?> resource)
    {
        return this.infos.get(resource);
    }
    
    public void setMinStockLevel(IResource<?> resource, long level)
    {
        ProcessInfo pi = this.infos.get(resource);
        if(pi == null)
        {
            pi = new ProcessInfo(resource);
            this.infos.put(resource, pi);
        }
        pi.minStockLevel = level;
        this.domain.setDirty();
    }
    
    public void setMaxStockLevel(IResource<?> resource, long level)
    {
        ProcessInfo pi = this.infos.get(resource);
        if(pi == null)
        {
            pi = new ProcessInfo(resource);
            this.infos.put(resource, pi);
        }
        pi.maxStockLevel = level;
        this.domain.setDirty();
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
        this.domain.setDirty();
    }
    
    @Override
    public Domain getDomain()
    {
        return this.domain;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        if(!this.infos.containsKey(ModNBTTag.PROCESS_SETTINGS)) return;

        this.infos.clear();
        
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
