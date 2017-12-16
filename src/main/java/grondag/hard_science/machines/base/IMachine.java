package grondag.hard_science.machines.base;

import grondag.hard_science.simulator.device.IDevice;
import net.minecraft.nbt.NBTTagCompound;

public interface IMachine extends IDevice
{
    @Override
    public default void serializeNBT(NBTTagCompound tag)
    {
        
    }

    @Override
    public default void deserializeNBT(NBTTagCompound tag)
    {
        
    }
 
    @Override
    public default void onConnect() {}
    
    @Override
    public default void onDisconnect() {}
}
