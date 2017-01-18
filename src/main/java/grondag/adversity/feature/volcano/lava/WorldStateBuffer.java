package grondag.adversity.feature.volcano.lava;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.commons.lang3.tuple.Pair;

import grondag.adversity.Adversity;
import grondag.adversity.simulator.Simulator;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

public class WorldStateBuffer implements IBlockAccess
{
    private final static String NBT_SAVE_DATA_TAG = "WrldBuff";
    private final static int NBT_SAVE_DATA_WIDTH = 4;
    
    public final World realWorld;
    
    private final Long2ObjectOpenHashMap<ChunkBuffer> chunks = new Long2ObjectOpenHashMap<ChunkBuffer>();
    private final LinkedList<ChunkBuffer> updateQueue = new LinkedList<ChunkBuffer>();
    
    private final LinkedList<ChunkBuffer> usedBuffers = new LinkedList<ChunkBuffer>();
    
    public WorldStateBuffer(World worldIn)
    {
        this.realWorld = worldIn;
    }
    
    public IBlockState getBlockState(BlockPos pos)
    {
        ChunkBuffer chunk = chunks.get(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
        
        if(chunk == null) 
        {
            return this.realWorld.getBlockState(pos);
        }
        else
        {
            return chunk.getBlockState(pos);
        }
    }
    
    public void setBlockState(BlockPos pos, IBlockState state)
    {
        getChunkBuffer(pos).setBlockState(pos, state);
    }
    
    private ChunkBuffer getChunkBuffer(BlockPos pos)
    {
        ChunkBuffer chunk = chunks.get(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
        
        if(chunk == null) 
        {
            chunk = this.usedBuffers.pollFirst();
            if(chunk == null)
            {
                chunk = new ChunkBuffer(pos, Simulator.instance.getCurrentSimTick());
            }
            else
            {
                chunk.renew(pos, Simulator.instance.getCurrentSimTick());
            }
            chunks.put(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4), chunk);
            updateQueue.add(chunk);
        }
        return chunk;
    }
    
    /** 
     * Makes the updates in the game world for up to chunkCount chunks.
     * Returns the number of blocks updated.
     * 
     */
    public int applyBlockUpdates(int chunkCount)
    {
        int blockCount = 0;
        int chunksDone = 0;
        //TODO: make configurable
        int firstEligibleTick = Simulator.instance.getCurrentSimTick() - 4;
                
        while(chunksDone++ < chunkCount && !this.updateQueue.isEmpty() && this.updateQueue.getFirst().tickCreated <= firstEligibleTick)
        {
            ChunkBuffer chunk = this.updateQueue.pollFirst();
            this.chunks.remove(ChunkPos.asLong(chunk.chunkpos.chunkXPos, chunk.chunkpos.chunkZPos));
            blockCount += chunk.applyBlockUpdates();
            this.usedBuffers.add(chunk);
        }
        return blockCount;
    }
    
    public void addUpdates(Collection<Pair<BlockPos, IBlockState>> updates)
    {
        for(Pair<BlockPos, IBlockState> pair : updates)
        {
            getChunkBuffer(pair.getLeft()).addUpdate(pair);
        }
    }

    @Override
    public boolean isAirBlock(BlockPos pos)
    {
        return this.getBlockState(pos).getBlock().isAir(this.getBlockState(pos), this, pos);
    }
    
    // FOLLOWING ARE UNSUPPORTED
    
    @Override
    public TileEntity getTileEntity(BlockPos pos)
    {
        return this.realWorld.getTileEntity(pos);
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue)
    {
        return this.realWorld.getCombinedLight(pos, lightValue);
    }

    @Override
    public Biome getBiome(BlockPos pos)
    {
        return this.realWorld.getBiome(pos);
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction)
    {
        return this.realWorld.getStrongPower(pos);
    }

    @Override
    public WorldType getWorldType()
    {
        return this.realWorld.getWorldType();
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default)
    {
        return this.realWorld.isSideSolid(pos, side, _default);
    }
    
    public void readFromNBT(NBTTagCompound nbt)
    {
        this.chunks.clear();
        this.updateQueue.clear();

        int[] saveData = nbt.getIntArray(NBT_SAVE_DATA_TAG);

        //confirm correct size
        if(saveData == null || saveData.length % NBT_SAVE_DATA_WIDTH != 0)
        {
            Adversity.log.warn("Invalid save data loading world state buffer. Blocks updates may have been lost.");
            return;
        }

        int i = 0;
//        this.isLoading = true;
        while(i < saveData.length)
        {
            BlockPos pos = new BlockPos(saveData[i++], saveData[i++], saveData[i++]);
            this.getChunkBuffer(pos).setBlockState(pos, Block.getStateById(saveData[i++]));
        }
//        this.isLoading = false;

        Adversity.log.info("Loaded " + i / NBT_SAVE_DATA_WIDTH + " world updates.");
    }

    public void writeToNBT(NBTTagCompound nbt)
    {
        int recordCount = 0;
        for(ChunkBuffer chunk : this.chunks.values())
        {
            recordCount+= chunk.size();
        }
        
        Adversity.log.info("Saving " + recordCount + " world updates.");
        
        int[] saveData = new int[recordCount * NBT_SAVE_DATA_WIDTH];
        int i = 0;

        for(ChunkBuffer chunk : this.chunks.values())
        {
            for(Pair<BlockPos, IBlockState> pair: chunk.values())
            {
                saveData[i++] = pair.getLeft().getX();
                saveData[i++] = pair.getLeft().getY();
                saveData[i++] = pair.getLeft().getZ();
                saveData[i++] = Block.getStateId(pair.getRight());
            }       
        }
        
        nbt.setIntArray(NBT_SAVE_DATA_TAG, saveData);

    }
    
    private static int getChunkStateKeyFromBlockPos(BlockPos pos)
    {
        return ((pos.getX() & 15) << 12) | ((pos.getY() & 255) << 4) | (pos.getZ() & 15);
    }
    
    private class ChunkBuffer
    {
        private ChunkPos chunkpos;
        
        private int tickCreated;
        
        private final Int2ObjectOpenHashMap<Pair<BlockPos, IBlockState>> states = new Int2ObjectOpenHashMap<Pair<BlockPos, IBlockState>>(32, 0.6F);
        
        private ChunkBuffer(BlockPos posWithinChunk, int tickCreated)
        {
            this.chunkpos = new ChunkPos(posWithinChunk);
            this.tickCreated = tickCreated;
        }
        
        private void renew(BlockPos posWithinChunk, int tickCreated)
        {
            this.chunkpos = new ChunkPos(posWithinChunk);
            this.tickCreated = tickCreated;
            this.states.clear();
        }
        
        private IBlockState getBlockState(BlockPos pos)
        {
            Pair<BlockPos, IBlockState> entry = states.get(getChunkStateKeyFromBlockPos(pos));
            
            if(entry == null)
            {
                return realWorld.getBlockState(pos);
            }
            else
            {
                return entry.getRight();
            }
        }
        
        private void setBlockState(BlockPos pos, IBlockState state)
        {
            states.put(getChunkStateKeyFromBlockPos(pos), Pair.of(pos, state));
        }
        
        private void addUpdate(Pair<BlockPos, IBlockState> update)
        {
            states.put(getChunkStateKeyFromBlockPos(update.getLeft()), update);
        }
        
        private int applyBlockUpdates()
        {
            int count = this.states.size();
            for(Pair<BlockPos, IBlockState> pair : this.states.values())
            {
                realWorld.setBlockState(pair.getLeft(), pair.getRight(), 3);
            }
            this.states.clear();
            return count;
        }
        
        private int size()
        {
            return this.states.size();
        }
        
        private Collection<Pair<BlockPos, IBlockState>> values()
        {
            return this.states.values();
        }
    }
}
