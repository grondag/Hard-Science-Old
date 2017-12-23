package grondag.hard_science.simulator.device.blocks;

import java.util.Collection;

import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.device.IDeviceComponent;
import grondag.hard_science.simulator.transport.endpoint.Port;
import grondag.hard_science.simulator.transport.endpoint.PortState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

/**
 * Block/block manager implementation for transport cabling/pipes.
 * Serializable because cable don't have distinct devices or tile entities
 * for each block and so have to store all extant block positions
 * as part of the serialized device.
 */
public class CableBlockHandler implements IDeviceBlockManager, IDeviceComponent, IReadWriteNBT
{
    private final IDevice owner;
    
    private Collection<IDeviceBlock> collection;
    
    public CableBlockHandler(IDevice owner)
    {
        this.owner = owner;
    }
    
    @Override
    public IDevice device()
    {
        return owner;
    }

    @Override
    public Collection<IDeviceBlock> blocks()
    {
        return this.collection;
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

    @SuppressWarnings("unused")
    private class BlockImpl implements IDeviceBlock
    {

        @Override
        public long packedBlockPos()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int dimensionID()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public IDevice device()
        {
            // TODO Auto-generated method stub
            return null;
        }

      

        @Override
        public void onRemoval()
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        public Iterable<PortState> getPorts(EnumFacing face)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public PortState getPort(Port port, EnumFacing face)
        {
            // TODO Auto-generated method stub
            return null;
        }
        
    }

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
}
