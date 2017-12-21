package grondag.hard_science.simulator.transport.management;

import java.util.Collection;

import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.endpoint.TransportNode;
import grondag.hard_science.simulator.transport.routing.IItinerary;
import net.minecraft.nbt.NBTTagCompound;

public class SimpleTransportManager implements ITransportManager
{

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void connect()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void disconnect()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public <T extends StorageType<T>> Collection<TransportNode> getNodes(T storageType)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends StorageType<T>> IItinerary<T> send(IResource<T> resource, long quantity, IDevice recipient, boolean connectedOnly, boolean simulate)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
