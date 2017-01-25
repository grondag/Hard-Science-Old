package grondag.adversity.feature.volcano.lava;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.CoolingBlock;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.simulator.Simulator;
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
    
    /** All chunks with update data. */
    private final ConcurrentHashMap<ChunkPos, ChunkBuffer> chunks = new ConcurrentHashMap<ChunkPos, ChunkBuffer>();
    
    private final ConcurrentLinkedQueue<ChunkBuffer> usedBuffers = new ConcurrentLinkedQueue<ChunkBuffer>();
    
    /**
     * For use in applyBlockUpdates. That routine is not re-entrant, so can keep and reuse one of these.
     */
    private final AdjustmentTracker tracker = new AdjustmentTracker();
    
    public WorldStateBuffer(World worldIn)
    {
        this.realWorld = worldIn;
    }
    
    public IBlockState getBlockState(BlockPos pos)
    {
        ChunkBuffer chunk = chunks.get(new ChunkPos(pos));
        
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
            this.chunks.remove(chunk.chunkpos);
            this.usedBuffers.add(chunk);
//            Adversity.log.info("Successful unqueud chunk update due to complete state reversion");
        }
    }
    
    private ChunkBuffer getChunkBuffer(BlockPos blockPos)
    {
        ChunkPos chunkPos = new ChunkPos(blockPos);
        
        ChunkBuffer chunk = chunks.get(chunkPos);
        
        if(chunk == null) 
        {
            chunk = this.usedBuffers.poll();
            if(chunk == null)
            {
                chunk = new ChunkBuffer(chunkPos, Simulator.instance.getCurrentSimTick());
            }
            else
            {
                chunk.renew(chunkPos, Simulator.instance.getCurrentSimTick());
            }
            chunks.put(chunkPos, chunk);
        }
        return chunk;
    }
    
    /** 
     * Makes the updates in the game world for up to chunkCount chunks.
     * Returns the number of blocks updated.
     * 
     */
    public int applyBlockUpdates(int chunkCount, LavaSimulator sim)
    {
        int updateCount = 0;
        
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
                    if(buff.requiredUpdateCount.get() != 0 || tickDiff >= 20)
                    {
                        int scoreCount = tickDiff < 20 ? buff.requiredUpdateCount.get() : buff.size();
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
                this.chunks.remove(best.chunkpos);
                updateCount += best.applyBlockUpdates(tracker, sim);
                this.usedBuffers.add(best);
            }
        }
        return updateCount;
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
        this.usedBuffers.clear();

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
            if(chunk.size() > 0)
            {
                i = chunk.writeSaveData(saveData, i);
            }
        }
        
        nbt.setIntArray(NBT_SAVE_DATA_TAG, saveData);

    }
    
    private static int getChunkStateKeyFromBlockPos(BlockPos pos)
    {
        return ((pos.getY() & 0xFF) << 8) | ((pos.getX() & 0xF) << 4) | (pos.getZ() & 0xF);
    }
    
    /** returns true an update occured */
    private boolean adjustFillIfNeeded(BlockPos pos, LavaSimulator sim)
    {
        IBlockState newState = IFlowBlock.adjustFillIfNeeded(realWorld, pos);
        
        if(newState == null) return false;
        
        if(newState.getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK)
        {
            sim.trackLavaFiller(pos);
        }
        else if(newState.getBlock() instanceof CoolingBlock)
        {
            sim.trackCoolingBlock(pos);
        }
        return true;
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
    
    /** 
     * Tracks an adjustment bit for every block in a chunk, plus a one-block border.
     * Valid x & z values are -1 to 16
     */
    public static class AdjustmentTracker
    {
        private BitSet bits = new BitSet( 18 * 18 * 256);
        
        private BitSet exclusions = new BitSet( 18 * 18 * 256);
        
        /** is only transiently used in getAdjustmentList */
        private ChunkPos chunkPos;
        
        public void clear()
        {
            bits.clear();
            exclusions.clear();
        }
        
        private static int getIndex(int x, int y, int z)
        {
            return (((x + 1) * 18 + (z + 1)) << 8) | y;
        }
        
        private BlockPos getBlockPos(int index)
        {
            int y = index & 0xFF;
            index = index >> 8;
            int z = (index % 18) - 1;
            int x = (index / 18) - 1;
            return new BlockPos(chunkPos.getXStart() + x, y, chunkPos.getZStart() + z);
        }
        
        /** 
         * Sets flag to true for all adjacent spaces that might be affected by a flow height block.
         * Assumes position is within the chunk being tracked. (Not the border of a neighbor chunk.)
         */
        public void setAdjustmentNeededAround(BlockPos pos)
        {
            int x = pos.getX() & 0xF;
            int z = pos.getZ() & 0xF;
            int minY = Math.max(0, pos.getY() - 2);
            int maxY = Math.min(255, pos.getY() + 2);
            
            for(int y = minY; y <= maxY; y++)
            {
                bits.set(getIndex(x - 1, y, z - 1), true);
                bits.set(getIndex(x - 1, y, z), true);
                bits.set(getIndex(x - 1, y, z + 1), true);
                
                bits.set(getIndex(x, y, z - 1), true);
                bits.set(getIndex(x, y, z), true);
                bits.set(getIndex(x, y, z + 1), true);
                
                bits.set(getIndex(x + 1, y, z - 1), true);
                bits.set(getIndex(x + 1, y, z), true);
                bits.set(getIndex(x + 1, y, z + 1), true);
            }
        }
        
        /** 
         * Prevent adjustment attempt when know won't be needed because placing a non-fill block there.
         * Assumes position is within the chunk being tracked.
         */
        public void excludeAdjustmentNeededAt(BlockPos pos)
        {
            exclusions.set(getIndex(pos.getX() & 0xF, pos.getY(), pos.getZ() & 0xF), true);
        }
        
        public Collection<BlockPos> getAdjustmentPositions(ChunkPos chunkPos)
        {
            this.chunkPos = chunkPos;
            bits.andNot(exclusions);
            return bits.stream().mapToObj(i -> getBlockPos(i)).collect(Collectors.toList());
        }
    }
    
    private static AtomicInteger recoveryCount = new AtomicInteger(0);
    private static AtomicInteger totalCount = new AtomicInteger(0);
    
    private class ChunkBuffer
    {
        private ChunkPos chunkpos;
        
        private int tickCreated;
        
        private AtomicInteger requiredUpdateCount = new AtomicInteger(0);
        
        private AtomicInteger dataCount = new AtomicInteger(0);
        
        /** shows number of updates per level*/
        private AtomicInteger[] levelCounts = new AtomicInteger[256];
        
        private BlockStateBuffer[] states = new BlockStateBuffer[0x10000];
                
        private ChunkBuffer(ChunkPos chunkPos, int tickCreated)
        {
            this.chunkpos = chunkPos;
            this.tickCreated = tickCreated;
            for(int i = 0; i < 256; i++)
            {
                levelCounts[i] = new AtomicInteger(0);
            }
        }
        
        private void renew(ChunkPos chunkPos, int tickCreated)
        {
            this.chunkpos = chunkPos;
            this.tickCreated = tickCreated;
            this.requiredUpdateCount.set(0);
            this.dataCount.set(0);
            for(int i = 0; i < 256; i++)
            {
                levelCounts[i].set(0);
            }
            Arrays.fill(states, null);
        }
        
        private IBlockState getBlockState(BlockPos pos)
        {
            BlockStateBuffer entry = states[getChunkStateKeyFromBlockPos(pos)];
            
            if(entry == null)
            {
                return realWorld.getBlockState(pos);
            }
            else
            {
                return entry.newState;
            }
        }
        
        /**
         * No mechanism here to make this threadsafe, however the lava simulator will never
         * attempt to update the same cell concurrently, so should never be a collision.
         */
        private void setBlockState(BlockPos pos, IBlockState newState, IBlockState expectedPriorState)
        {
            totalCount.incrementAndGet();
            
            int key = getChunkStateKeyFromBlockPos(pos);
            BlockStateBuffer state = states[key];
            if(state == null)
            {
                state = new BlockStateBuffer(pos, newState, expectedPriorState);
                this.states[key] = state;
                this.dataCount.incrementAndGet();
                this.levelCounts[pos.getY()].incrementAndGet();
                if(state.isRequired()) this.requiredUpdateCount.incrementAndGet();
            }
            else
            {
                if(newState == state.expectedPriorState)
                {
                    this.states[key] = null;
                    this.dataCount.decrementAndGet();
                    this.levelCounts[pos.getY()].decrementAndGet();
                    if(state.isRequired()) this.requiredUpdateCount.decrementAndGet();
                    
                    if((recoveryCount.getAndIncrement() & 0xFFF) == 0xFFF)
                        Adversity.log.info("BlockStateRecoveries = " + recoveryCount.get() + " of " + totalCount.get() + ", " + recoveryCount.get() * 100 / totalCount.get() + " percent recovery");
                }
                else
                {
                    boolean wasRequired = state.isRequired();
                    state.newState = newState;
                    if(state.isRequired())
                    {
                        if(!wasRequired) this.requiredUpdateCount.incrementAndGet();
                    }
                    else
                    {
                        if(wasRequired) this.requiredUpdateCount.decrementAndGet();
                    }
                }
            }
        }
        
        /** NOT thread safe */
        private int applyBlockUpdates(AdjustmentTracker tracker, LavaSimulator sim)
        {
            tracker.clear();
            int count = this.dataCount.get();
            int allRemaining = count;
            
            for(int y = 0; y < 256; y++)
            {
                if(this.levelCounts[y].get() > 0)
                {
                    int yRemaining = this.levelCounts[y].get();
                    int yMin = y << 8;
                    int yMax = yMin + 256;
                    
                    for(int i = yMin; i < yMax; i++)
                    {
                        if(this.states[i] != null)
                        {
                            BlockStateBuffer bsb = states[i];
                            
                            if(IFlowBlock.isFlowHeight(bsb.newState.getBlock()) || IFlowBlock.isFlowHeight(bsb.expectedPriorState.getBlock()))
                            {
                                tracker.setAdjustmentNeededAround(bsb.pos);
                            }
                            tracker.excludeAdjustmentNeededAt(bsb.pos);
                            realWorld.setBlockState(bsb.pos, bsb.newState, 3);
                            this.states[i] = null;
                            allRemaining--;
                            if(--yRemaining == 0) break;
                        }
                    }
                    this.levelCounts[y].set(0);
                }
                if(allRemaining == 0) break;
            }

            this.dataCount.set(0);
            this.requiredUpdateCount.set(0);
            
            count += tracker.getAdjustmentPositions(this.chunkpos).stream().mapToInt(p -> adjustFillIfNeeded(p, sim) ? 1 : 0).sum();
            
            return count;

        }
        
        private int size()
        {
            return this.dataCount.get();
        }
        
        /** returns the next index that should be used */
        private int writeSaveData(int[] saveData, int startingIndex)
        {
            int allRemaining = this.size();
            for(int y = 0; y < 256; y++)
            {
                if(this.levelCounts[y].get() > 0)
                {
                    int yRemaining = this.levelCounts[y].get();
                    int yMin = y << 8;
                    int yMax = yMin + 256;
                    
                    for(int i = yMin; i < yMax; i++)
                    {
                        BlockStateBuffer state = this.states[i];
                        if(state != null)
                        {
                            saveData[startingIndex++] = state.pos.getX();
                            saveData[startingIndex++] = state.pos.getY();
                            saveData[startingIndex++] = state.pos.getZ();
                            saveData[startingIndex++] = Block.getStateId(state.newState);
                            saveData[startingIndex++] = Block.getStateId(state.expectedPriorState);
                            allRemaining--;
                            if(--yRemaining == 0) break;
                        }
                    }
                }
                if(allRemaining == 0) break;
            }
            return startingIndex;
        }
    }
}
