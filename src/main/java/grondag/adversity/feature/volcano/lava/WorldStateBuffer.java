package grondag.adversity.feature.volcano.lava;

import java.util.Collection;
import java.util.LinkedList;

import grondag.adversity.Adversity;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.simulator.Simulator;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
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
    private final static int NBT_SAVE_DATA_WIDTH = 5;
    
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
    
    public void setBlockState(BlockPos pos, IBlockState newState, IBlockState expectedPriorState)
    {
        ChunkBuffer chunk = getChunkBuffer(pos);
        chunk.setBlockState(pos, newState, expectedPriorState);
        if(chunk.size() == 0)
        {
            this.chunks.remove(ChunkPos.asLong(chunk.chunkpos.chunkXPos, chunk.chunkpos.chunkZPos));
            this.usedBuffers.add(chunk);
            Adversity.log.info("Successful unqueud chunk update due to complete state reversion");
        }
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
        int currentTick = Simulator.instance.getCurrentSimTick();
        
        boolean maybeSomethingToDo = true;
        int foundCount = 0;
        
        //TODO: make tick diff configurable

        while(maybeSomethingToDo && foundCount++ < chunkCount)
        {
            ChunkBuffer best = null;
            int bestScore = 0;
            
            //TODO: maintain a sorted list of the top N chunks (N = chunkCount)
            //so that we don't have loop through the collection more than once.

            for(ChunkBuffer buff : this.chunks.values())
            {
                int tickDiff = currentTick - buff.tickCreated;
                if(tickDiff > 3)
                {
                    if(buff.requiredUpdateCount != 0 || tickDiff >= 20)
                    {
                        int scoreCount = tickDiff < 20 ? buff.requiredUpdateCount : buff.size();
                        if(best == null)
                        {
                             best = buff;
                             bestScore = scoreCount * tickDiff;
                        }
                        else
                        {   
                            int newScore = scoreCount * tickDiff;
                            if(newScore > bestScore)
                            {
                                best = buff;
                                bestScore = newScore;
                            }
                        }
                    }
                }
            }
            
            if(best == null)
            {
                maybeSomethingToDo = false;
            }
            else
            {
                this.chunks.remove(ChunkPos.asLong(best.chunkpos.chunkXPos, best.chunkpos.chunkZPos));
                blockCount += best.applyBlockUpdates();
                this.usedBuffers.add(best);
            }
        }
        return blockCount;
    }
    
    public void addUpdate(BlockStateBuffer update)
    {
        getChunkBuffer(update.pos).addUpdate(update);
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
            this.getChunkBuffer(pos).setBlockState(pos, Block.getStateById(saveData[i++]), Block.getStateById(saveData[i++]));
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
            for(BlockStateBuffer state: chunk.values())
            {
                saveData[i++] = state.pos.getX();
                saveData[i++] = state.pos.getY();
                saveData[i++] = state.pos.getZ();
                saveData[i++] = Block.getStateId(state.newState);
                saveData[i++] = Block.getStateId(state.expectedPriorState);
            }       
        }
        
        nbt.setIntArray(NBT_SAVE_DATA_TAG, saveData);

    }
    
    private static int getChunkStateKeyFromBlockPos(BlockPos pos)
    {
        return ((pos.getX() & 15) << 12) | ((pos.getY() & 255) << 4) | (pos.getZ() & 15);
    }
    
    public static class BlockStateBuffer
    {
        private BlockPos pos;
        private IBlockState newState;
        private IBlockState expectedPriorState;
        
        public boolean isRequired()
        {
            int oldHeight = IFlowBlock.getFlowHeightFromState(expectedPriorState);
            if(oldHeight > 0)
            {
                int newHeight = IFlowBlock.getFlowHeightFromState(newState);
                return newHeight > 0 && Math.abs(oldHeight - newHeight) > 2;
            }
            else
            {
                return true;
            }
        }
        
        public BlockStateBuffer( BlockPos pos, IBlockState newState, IBlockState expectedPriorState)
        {
            this.pos = pos;
            this.newState = newState;
            this.expectedPriorState = expectedPriorState;
        }
        
        public IBlockState getNewState() { return this.newState; }
        public IBlockState getExpectedPriorState() { return this.expectedPriorState; }
        public BlockPos getBlockPos() { return this.pos; }
    }
    
    private static int recoveryCount = 0;
    private static int totalCount = 0;
    
    private class ChunkBuffer
    {
        private ChunkPos chunkpos;
        
        private int tickCreated;
        
        private int requiredUpdateCount;
        
        private final Int2ObjectOpenHashMap<BlockStateBuffer> states = new Int2ObjectOpenHashMap<BlockStateBuffer>(32, 0.6F);
        
        private ChunkBuffer(BlockPos posWithinChunk, int tickCreated)
        {
            this.chunkpos = new ChunkPos(posWithinChunk);
            this.tickCreated = tickCreated;
        }
        
        private void renew(BlockPos posWithinChunk, int tickCreated)
        {
            this.chunkpos = new ChunkPos(posWithinChunk);
            this.tickCreated = tickCreated;
            this.requiredUpdateCount = 0;
            this.states.clear();
        }
        
        private IBlockState getBlockState(BlockPos pos)
        {
            BlockStateBuffer entry = states.get(getChunkStateKeyFromBlockPos(pos));
            
            if(entry == null)
            {
                return realWorld.getBlockState(pos);
            }
            else
            {
                return entry.newState;
            }
        }
        
        private void setBlockState(BlockPos pos, IBlockState newState, IBlockState expectedPriorState)
        {
            totalCount++;
            
            int key = getChunkStateKeyFromBlockPos(pos);
            BlockStateBuffer state = states.get(key);
            if(state == null)
            {
                state = new BlockStateBuffer(pos, newState, expectedPriorState);
                states.put(key, state);
                if(state.isRequired()) this.requiredUpdateCount++;
            }
            else
            {
                if(newState == state.expectedPriorState)
                {
                    states.remove(key);
                    if(state.isRequired()) this.requiredUpdateCount--;
    
                    if((recoveryCount++ & 0xFFF) == 0xFFF)
                        Adversity.log.info("BlockStateRecoveries = " + recoveryCount + " of " + totalCount + ", " + recoveryCount * 100 / totalCount + " percent recovery");
                }
                else
                {
                    boolean wasRequired = state.isRequired();
                    state.newState = newState;
                    if(state.isRequired())
                    {
                        if(!wasRequired) this.requiredUpdateCount++;
                    }
                    else
                    {
                        if(wasRequired) this.requiredUpdateCount--;
                    }
                }
            }
        }
        
        private void addUpdate(BlockStateBuffer update)
        {
            states.put(getChunkStateKeyFromBlockPos(update.pos), update);
        }
        
        private int applyBlockUpdates()
        {
            int count = this.states.size();
            for(BlockStateBuffer state : this.states.values())
            {
                realWorld.setBlockState(state.pos, state.newState, 3);
            }
            this.states.clear();
            return count;
        }
        
        private int size()
        {
            return this.states.size();
        }
        
        private Collection<BlockStateBuffer> values()
        {
            return this.states.values();
        }
        
     
    }
}
