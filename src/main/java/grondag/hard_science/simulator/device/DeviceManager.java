package grondag.hard_science.simulator.device;

import javax.annotation.Nullable;

import grondag.exotic_matter.concurrency.CountedJob;
import grondag.exotic_matter.concurrency.CountedJobTask;
import grondag.exotic_matter.concurrency.Job;
import grondag.exotic_matter.concurrency.PerformanceCollector;
import grondag.exotic_matter.concurrency.SimpleCountedJobBacker;
import grondag.exotic_matter.serialization.NBTDictionary;
import grondag.exotic_matter.simulator.ISimulationTickable;
import grondag.exotic_matter.simulator.Simulator;
import grondag.exotic_matter.simulator.persistence.ISimulationTopNode;
import grondag.hard_science.Configurator;
import grondag.hard_science.HardScience;
import grondag.hard_science.simulator.device.blocks.DeviceWorldManager;
import grondag.hard_science.simulator.device.blocks.IDeviceBlock;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.World;

public class DeviceManager implements ISimulationTopNode, ISimulationTickable
{
    ///////////////////////////////////////////////////////////
    //  STATIC MEMBERS
    ///////////////////////////////////////////////////////////
    
    private static final String NBT_DEVICE_MANAGER_DEVICES = NBTDictionary.claim("dmDevices");
    private static final String NBT_DEVICE_MANAGER_SELF = NBTDictionary.claim("devMgr");
    private static final String NBT_DEVICE_MANAGER_DEVICE_TYPE = NBTDictionary.claim("dmDevType");
    
    /**
     * A bit ugly but covenient.  Set to null when any 
     * instance is created, retrieved lazily from Simulator.
     */
    private static DeviceManager instance;
    
    public static DeviceManager instance()
    {
        if(instance == null)
        {
            instance = Simulator.instance().getNode(DeviceManager.class);
        }
        return instance;
    }

    private static final int BATCH_SIZE = 100;
    
    private static final RegistryNamespaced < ResourceLocation, Class <? extends IDevice >> REGISTRY = new RegistryNamespaced < ResourceLocation, Class <? extends IDevice >> ();
    
    public static void register(String id, Class <? extends IDevice > clazz)
    {
        REGISTRY.putObject(new ResourceLocation(id), clazz);
    }
    
    @Nullable
    public static ResourceLocation getKey(Class <? extends IDevice > clazz)
    {
        return REGISTRY.getNameForObject(clazz);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Nullable
    public static IDevice create(NBTTagCompound compound)
    {
        IDevice device = null;
        String s = compound.getString(NBT_DEVICE_MANAGER_DEVICE_TYPE);
        Class <? extends IDevice > oclass = null;

        try
        {
            oclass = (Class)REGISTRY.getObject(new ResourceLocation(s));

            if (oclass != null)
            {
                device = oclass.newInstance();
            }
        }
        catch (Throwable throwable1)
        {
            HardScience.INSTANCE.error("Failed to create device {}", s, throwable1);
        }

        if (device != null)
        {
            try
            {
                device.deserializeNBT(compound);
            }
            catch (Throwable throwable)
            {
                HardScience.INSTANCE.error("Failed to load data for device {}", s, throwable);
                device = null;
            }
        }
        else
        {
            HardScience.INSTANCE.warn("Skipping device with id {}", (Object)s);
        }

        return device;
    }
    
    public static IDevice getDevice(int deviceId)
    {
        return instance().devices.get(deviceId);
    }
    
    public static IDevice getDevice(World world, BlockPos pos)
    {
        IDeviceBlock block =  blockManager().getBlockDelegate(world, pos);
        return block == null ? null : block.device();
    }
    
    /**
     * Records device for access and persistence.</p>
     * Note that adding a device does NOT connect it.
     * That is done either after deserialization (for existing devices)
     * or after block placement (for new devices)
     */
    public static void addDevice(IDevice device)
    {
        instance().addDeviceInconveniently(device);
    }
    
    public static void removeDevice(IDevice device)
    {
        instance().removeDeviceInconveniently(device);
    }
    
    public static DeviceWorldManager blockManager()
    {
        return instance().deviceBlocks;
    }
    
    ///////////////////////////////////////////////////////////
    //  INSTANCE MEMBERS
    ///////////////////////////////////////////////////////////
    
    private final Int2ObjectOpenHashMap<IDevice> devices =
            new Int2ObjectOpenHashMap<IDevice>();
    
    private final DeviceWorldManager deviceBlocks = new DeviceWorldManager();
    
    
    private SimpleCountedJobBacker onTickJobBacker = new SimpleCountedJobBacker()
    {
        @Override
        protected Object[] generateOperands()
        {
            return devices.values().stream().filter(d -> d.doesUpdateOnTick()).toArray();
        }
    };
    
    private SimpleCountedJobBacker offTickJobBacker = new SimpleCountedJobBacker()
    {
        @Override
        protected Object[] generateOperands()
        {
            return devices.values().stream().filter(d -> d.doesUpdateOffTick()).toArray();
        }
    };
    
    private boolean isDirty = false;
    
    public final PerformanceCollector perfCollectorOnTick = new PerformanceCollector("Machine Simulator On tick");
    public final PerformanceCollector perfCollectorOffTick = new PerformanceCollector("Machine Simulator Off tick");
    
    public final Job onTickJob;  
    public final Job offTickJob;  
    
    private final CountedJobTask<IDevice> offTickTask = new CountedJobTask<IDevice>() 
    {
        @Override
        public void doJobTask(IDevice operand)
        {
            operand.doOffTick();
        }
    };
    
    private final CountedJobTask<IDevice> onTickTask = new CountedJobTask<IDevice>() 
    {
        @Override
        public void doJobTask(IDevice operand)
        {
            operand.doOnTick();
        }
    };
    
    public DeviceManager()
    {
        // force refresh of singleton access method
        instance = null;
        
        // on-tick jobs
        this.onTickJob = new CountedJob<IDevice>(this.onTickJobBacker, this.onTickTask, BATCH_SIZE, 
                Configurator.MACHINES.enablePerformanceLogging, "Machine on-tick update processing", this.perfCollectorOnTick);    

        // off-tick jobs
        this.offTickJob = new CountedJob<IDevice>(this.offTickJobBacker, this.offTickTask, BATCH_SIZE, 
                Configurator.MACHINES.enablePerformanceLogging, "Machine off-stick update processing", this.perfCollectorOffTick);   

    }
    
    @Override
    public void afterDeserialization()
    {
        for(IDevice device : this.devices.values())
        {
            device.onConnect();
        }
    }
    
    public void addDeviceInconveniently(IDevice device)
    {
        if(Configurator.logDeviceChanges)
            HardScience.INSTANCE.info("DeviceManager.addDevice: " + device.getId());
        
        assert this.devices.put(device.getId(), device) == null
                : "Duplicate device registration.";
        this.onTickJobBacker.setDirty();
        this.offTickJobBacker.setDirty();
        this.isDirty = true;
        
        if(Configurator.logDeviceChanges)
            HardScience.INSTANCE.info("DeviceManager device count = " + this.devices.size());

    }
    
    public void removeDeviceInconveniently(IDevice device)
    {
        if(device == null)
        {
            assert false : "Received request to remove null device.";
            return;
        }
        
        if(Configurator.logDeviceChanges)
            HardScience.INSTANCE.info("DeviceManager.removeDevice: " + device.getId());

        IDevice oldDevice = this.devices.remove(device.getId());
        if(oldDevice == device)
        {
            device.onDisconnect();
        }
        else if(oldDevice == null)
        {
            device.onDisconnect();
            assert false : "Removal request device mismatch";
        }
        else
        {
            assert false : "Removal request for missing device.";
        }        
        this.isDirty = true;
        this.onTickJobBacker.setDirty();
        this.offTickJobBacker.setDirty();
        
        if(Configurator.logDeviceChanges)
            HardScience.INSTANCE.info("DeviceManager device count = " + this.devices.size());
    }
    
    @Override
    public boolean isSaveDirty()
    {
        return this.isDirty;
    }

    @Override
    public void setSaveDirty(boolean isDirty)
    {
        this.isDirty = isDirty;
    }

    public void clear()
    {
        this.devices.clear();
        this.deviceBlocks.clear();
    }
    
    /**
     * Called by simulator at shutdown
     */
    @Override
    public void unload()
    {
        this.clear();
    }
    
    @Override
    public void deserializeNBT(@Nullable NBTTagCompound tag)
    {
        if(tag == null) return;
        
        NBTTagList nbtDevices = tag.getTagList(NBT_DEVICE_MANAGER_DEVICES, 10);
        if( nbtDevices != null && !nbtDevices.hasNoTags())
        {
            for (int i = 0; i < nbtDevices.tagCount(); ++i)
            {
                IDevice device = create(nbtDevices.getCompoundTagAt(i));
                if(device != null) this.devices.put(device.getId(), device);
            }   
        }
        this.onTickJobBacker.setDirty();
        this.offTickJobBacker.setDirty();
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        NBTTagList nbtDevices = new NBTTagList();
        
        if(!devices.isEmpty())
        {
            for (IDevice device : this.devices.values())
            {
                if(device.doesPersist())
                {
                    ResourceLocation resourcelocation = REGISTRY.getNameForObject(device.getClass());
                    
                    if (resourcelocation == null)
                    {
                        HardScience.INSTANCE.error("Error saving device state because " + device.getClass() + " is missing a mapping");
                    }
                    else
                    {
                        NBTTagCompound deviceTag = device.serializeNBT();
                        deviceTag.setString(NBT_DEVICE_MANAGER_DEVICE_TYPE, resourcelocation.toString());
                        nbtDevices.appendTag(deviceTag);
                    }
                }
            }
        }
        tag.setTag(NBT_DEVICE_MANAGER_DEVICES, nbtDevices);        
    }

    @Override
    public String tagName()
    {
        return NBT_DEVICE_MANAGER_SELF;
    }

    @Override
    public void doOnTick()
    {
        this.onTickJob.run();
    }

    @Override
    public void doOffTick()
    {
        this.offTickJob.runOn(Simulator.SIMULATION_POOL);        
    }

}
