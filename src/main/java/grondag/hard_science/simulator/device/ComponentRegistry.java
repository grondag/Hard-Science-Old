package grondag.hard_science.simulator.device;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.exotic_matter.serialization.IReadWriteNBT;
import grondag.hard_science.Log;
import grondag.hard_science.init.ModNBTTag;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;

public class ComponentRegistry
{
    public static final ComponentRegistry INSTANCE = new ComponentRegistry();
    
    private static final RegistryNamespaced < ResourceLocation, Class <? extends IDeviceComponent >> REGISTRY = new RegistryNamespaced < ResourceLocation, Class <? extends IDeviceComponent >> ();

    public static void register(ResourceLocation id, Class <? extends IDeviceComponent > clazz)
    {
        REGISTRY.putObject(id, clazz);
    }
    
    public static void register(String id, Class <? extends IDeviceComponent > clazz)
    {
        register(new ResourceLocation(id), clazz);
    }
    
    @Nullable
    public static ResourceLocation getKey(Class <? extends IDeviceComponent > clazz)
    {
        return REGISTRY.getNameForObject(clazz);
    }
    
    @Nullable
    public static IDeviceComponent fromNBT(IDevice owner, NBTTagCompound tag)
    {
        String componentName = tag.getString(ModNBTTag.DEVICE_COMPONENT_TYPE);
        IDeviceComponent result = create(owner, new ResourceLocation(componentName));
        if(result != null && result instanceof IReadWriteNBT)
        {
            ((IReadWriteNBT)result).deserializeNBT(tag);
        }
        return result;
    }
    
    
    public static NBTTagCompound toNBT(@Nonnull IDeviceComponent component)
    {
        NBTTagCompound result = new NBTTagCompound();
        
        ResourceLocation resourcelocation = REGISTRY.getNameForObject(component.getClass());
        
        if (resourcelocation != null)
        {
            result.setString(ModNBTTag.DEVICE_COMPONENT_TYPE, resourcelocation.toString());
        }
        
        if(component instanceof IReadWriteNBT)
        {
            ((IReadWriteNBT)component).serializeNBT(result);
        }
        return result;
    }
    
    @Nullable
    public static IDeviceComponent create(IDevice owner, String componentName)
    {
        return create(owner, new ResourceLocation(componentName));
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Nullable
    public static IDeviceComponent create(IDevice owner, ResourceLocation component)
    {
        IDeviceComponent result = null;
        Class <? extends IDeviceComponent > oclass = null;

        try
        {
            oclass = (Class)REGISTRY.getObject(component);

            if (oclass != null)
            {
                result = oclass.getConstructor(IDevice.class).newInstance(owner);
            }
        }
        catch (Throwable throwable1)
        {
            Log.error("Failed to create device component {}", component, throwable1);
        }

        return result;
    }
}
