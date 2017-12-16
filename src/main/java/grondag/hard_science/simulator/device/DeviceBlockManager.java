package grondag.hard_science.simulator.device;

import javax.annotation.Nullable;

import grondag.hard_science.simulator.transport.L1.IConnector;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.EnumFacing;

public class DeviceBlockManager
{
    private Int2ObjectOpenHashMap<Long2ObjectOpenHashMap<DeviceBlock>> worldBlocks
     = new Int2ObjectOpenHashMap<Long2ObjectOpenHashMap<DeviceBlock>>();

    private Long2ObjectOpenHashMap<DeviceBlock> getBlocksForDimension(int dimensionID)
    {
        Long2ObjectOpenHashMap<DeviceBlock> blocks = worldBlocks.get(dimensionID);
        if(blocks == null)
        {
            synchronized(worldBlocks)
            {
                blocks = worldBlocks.get(dimensionID);
                if(blocks == null)
                {
                    blocks = new Long2ObjectOpenHashMap<DeviceBlock>();
                    worldBlocks.put(dimensionID, blocks);
                }
            }
        }
        return blocks;
    }
    
    @Nullable
    public IConnector getConnector(int dimensionID, long packedBlockPos, EnumFacing face)
    {
        Long2ObjectOpenHashMap<DeviceBlock> blocks = this.getBlocksForDimension(dimensionID);
        if(blocks == null) return null;
        
        DeviceBlock block = blocks.get(packedBlockPos);
        if(block == null) return null;
        
        return block.getConnector(face);
    }
    
    public void addOrUpdateConnector(int dimensionID, long packedBlockPos, EnumFacing face, IConnector connector)
    {
        Long2ObjectOpenHashMap<DeviceBlock> blocks = this.getBlocksForDimension(dimensionID);
        DeviceBlock block = blocks.get(packedBlockPos);
        if(block == null)
        {
            synchronized(blocks)
            {
                block = blocks.get(packedBlockPos);
                if(block == null)
                {
                    block = new DeviceBlock(packedBlockPos);
                    blocks.put(packedBlockPos, block);
                }
            }
        }
        synchronized(block)
        {
            block.setConnector(face, connector);
        }
    }

    public void removeConnector(int dimensionID, long packedBlockPos, EnumFacing face, IConnector connector)
    {
        Long2ObjectOpenHashMap<DeviceBlock> blocks = this.getBlocksForDimension(dimensionID);
        DeviceBlock block = blocks.get(packedBlockPos);
        assert block != null : "Request to remove connection for missing device block";
        
        synchronized(block)
        {
            assert block.getConnector(face) == connector
                    : "Mismatched request to remove device connection";
            
            block.setConnector(face, null);
        }
        
        // maintain synchronization order to avoid deadlocks
        if(block.isEmpty())
        {
            synchronized(blocks)
            {
                synchronized(block)
                {      
                    // confirm
                    if(block.isEmpty())
                    {
                        blocks.remove(packedBlockPos);
                    }
                }
            }
        }
    }
}
