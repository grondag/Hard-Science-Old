package grondag.hard_science.simulator.device;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.hard_science.library.world.PackedBlockPos;
import grondag.hard_science.simulator.transport.L1.IConnector;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DeviceBlockManager
{
    private Int2ObjectOpenHashMap<Long2ObjectOpenHashMap<IDeviceBlock>> worldBlocks
     = new Int2ObjectOpenHashMap<Long2ObjectOpenHashMap<IDeviceBlock>>();

    private Long2ObjectOpenHashMap<IDeviceBlock> getBlocksForDimension(int dimensionID)
    {
        Long2ObjectOpenHashMap<IDeviceBlock> blocks = worldBlocks.get(dimensionID);
        if(blocks == null)
        {
            synchronized(worldBlocks)
            {
                blocks = worldBlocks.get(dimensionID);
                if(blocks == null)
                {
                    blocks = new Long2ObjectOpenHashMap<IDeviceBlock>();
                    worldBlocks.put(dimensionID, blocks);
                }
            }
        }
        return blocks;
    }
    
    @Nullable
    public IConnector getConnector(int dimensionID, long packedBlockPos, @Nonnull EnumFacing face)
    {
        IDeviceBlock block = this.getBlockDelegate(dimensionID, packedBlockPos);
        if(block == null) return null;
        return block.getConnector(face);
    }
    
    @Nullable
    public IConnector getConnector(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing face)
    {
        return this.getConnector(world.provider.getDimension(), PackedBlockPos.pack(pos), face);
    }
    
    @Nullable
    public IDeviceBlock getBlockDelegate(int dimensionID, long packedBlockPos)
    {
        Long2ObjectOpenHashMap<IDeviceBlock> blocks = this.getBlocksForDimension(dimensionID);
        return blocks == null ? null : blocks.get(packedBlockPos);
    }
    
    @Nullable
    public IDeviceBlock getBlockDelegate(@Nonnull World world, @Nonnull BlockPos pos)
    {
        return this.getBlockDelegate(world.provider.getDimension(), PackedBlockPos.pack(pos));
    }
    
    /**
     * Should be called by devices during {@link IDevice#onConnect()}
     * or whenever a connected device adds or changes a connection. 
     */
    public void addOrUpdateDelegate(@Nonnull IDeviceBlock block)
    {
        Long2ObjectOpenHashMap<IDeviceBlock> blocks = this.getBlocksForDimension(block.dimensionID());
        synchronized(blocks)
        {
            IDeviceBlock oldBlock = blocks.put(block.packedBlockPos(), block);
            if(oldBlock != null) disconnect(oldBlock, blocks);
            connect(block, blocks);
        }
    }
    
    private void connect(IDeviceBlock block, Long2ObjectOpenHashMap<IDeviceBlock> blocks)
    {
        
    }
    
    /**
     * Should be called by devices during {@link IDevice#onDisconnect()()}
     * or whenever a connected device removes a connection. 
     * Prior connection information is for assertion checking in test/dev env.
     */
    public void removeDelegate(@Nonnull IDeviceBlock block)
    {
        Long2ObjectOpenHashMap<IDeviceBlock> blocks = this.getBlocksForDimension(block.dimensionID());
        synchronized(blocks)
        {
            assert blocks.remove(block.packedBlockPos()) == block
                    : "Mismatched request to remove device block";
            disconnect(block, blocks);
        }
    }
    
    private void disconnect(IDeviceBlock block, Long2ObjectOpenHashMap<IDeviceBlock> blocks)
    {
        
    }
}
