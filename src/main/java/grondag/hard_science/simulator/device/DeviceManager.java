package grondag.hard_science.simulator.device;

import javax.annotation.Nullable;

import grondag.hard_science.Configurator;
import grondag.hard_science.Log;
import grondag.hard_science.library.concurrency.CountedJob;
import grondag.hard_science.library.concurrency.CountedJobTask;
import grondag.hard_science.library.concurrency.Job;
import grondag.hard_science.library.concurrency.PerformanceCollector;
import grondag.hard_science.library.concurrency.SimpleCountedJobBacker;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.simulator.ISimulationTickable;
import grondag.hard_science.simulator.Simulator;
import grondag.hard_science.simulator.device.blocks.DeviceWorldManager;
import grondag.hard_science.simulator.device.blocks.IDeviceBlock;
import grondag.hard_science.simulator.persistence.IPersistenceNode;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.World;

public class DeviceManager implements IPersistenceNode, ISimulationTickable
{
    ///////////////////////////////////////////////////////////
    //  STATIC MEMBERS
    ///////////////////////////////////////////////////////////

    public static final DeviceManager RAW_INSTANCE_DO_NOT_USE = new DeviceManager();
 
    private static final int BATCH_SIZE = 100;
    
    private static final RegistryNamespaced < ResourceLocation, Class <? extends IDevice >> REGISTRY = new RegistryNamespaced < ResourceLocation, Class <? extends IDevice >> ();

    public static DeviceManager instance()
    {
        Simulator.loadSimulatorIfNotLoaded();
        return RAW_INSTANCE_DO_NOT_USE;
    }
    
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
        String s = compound.getString(ModNBTTag.DEVICE_TYPE);
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
            Log.error("Failed to create device {}", s, throwable1);
        }

        if (device != null)
        {
            try
            {
                device.deserializeNBT(compound);
            }
            catch (Throwable throwable)
            {
                Log.error("Failed to load data for device {}", s, throwable);
                device = null;
            }
        }
        else
        {
            Log.warn("Skipping device with id {}", (Object)s);
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
    //  RAW_INSTANCE_DO_NOT_USE MEMBERS
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
    
    private DeviceManager()
    {
        // on-tick jobs
        this.onTickJob = new CountedJob<IDevice>(this.onTickJobBacker, this.onTickTask, BATCH_SIZE, 
                Configurator.MACHINES.enablePerformanceLogging, "Machine on-tick update processing", this.perfCollectorOnTick);    

        // off-tick jobs
        this.offTickJob = new CountedJob<IDevice>(this.offTickJobBacker, this.offTickTask, BATCH_SIZE, 
                Configurator.MACHINES.enablePerformanceLogging, "Machine off-stick update processing", this.perfCollectorOffTick);   

    }
    
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
            Log.info("DeviceManager.addDevice: " + device.getId());
        
        assert this.devices.put(device.getId(), device) == null
                : "Duplicate device registration.";
        this.onTickJobBacker.setDirty();
        this.offTickJobBacker.setDirty();
        this.isDirty = true;
        
        if(Configurator.logDeviceChanges)
            Log.info("DeviceManager device count = " + this.devices.size());

    }
    
    public void removeDeviceInconveniently(IDevice device)
    {
        if(device == null)
        {
            assert false : "Received request to remove null device.";
            return;
        }
        
        if(Configurator.logDeviceChanges)
            Log.info("DeviceManager.removeDevice: " + device.getId());

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
            Log.info("DeviceManager device count = " + this.devices.size());
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
    public void unload()
    {
        this.clear();
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        if(tag == null) return;
        
        NBTTagList nbtDevices = tag.getTagList(ModNBTTag.DEVICE_MANAGER_DEVICES, 10);
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
                        Log.error("Error saving device state because " + device.getClass() + " is missing a mapping");
                    }
                    else
                    {
                        NBTTagCompound deviceTag = device.serializeNBT();
                        deviceTag.setString(ModNBTTag.DEVICE_TYPE, resourcelocation.toString());
                        nbtDevices.appendTag(deviceTag);
                    }
                }
            }
        }
        tag.setTag(ModNBTTag.DEVICE_MANAGER_DEVICES, nbtDevices);        
    }

    @Override
    public String tagName()
    {
        return ModNBTTag.DEVICE_MANAGER;
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
