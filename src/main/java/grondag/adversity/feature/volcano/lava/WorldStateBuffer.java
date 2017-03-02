package grondag.adversity.feature.volcano.lava;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.CoolingBlock;
import grondag.adversity.library.PackedBlockPos;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.block.FlowStaticBlock;
import grondag.adversity.simulator.Simulator;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

public class WorldStateBuffer implements IBlockAccess
{
    private final static String NBT_SAVE_DATA_TAG = "WrldBuff";
    private final static int NBT_SAVE_DATA_WIDTH = 5;
    
    /** tracks number of calls to setBlockState since last clearStatistics */
    private int stateSetCount = 0;
    
    /** tracks number of world state changes since last clearStatistics */
    private int stateApplicationCount = 0;        
    
    public final World realWorld;
    
    /** All chunks with update data. */
    private final ConcurrentHashMap<Long, ChunkBuffer> chunks = new ConcurrentHashMap<Long, ChunkBuffer>();
    
    private final ConcurrentLinkedQueue<ChunkBuffer> usedBuffers = new ConcurrentLinkedQueue<ChunkBuffer>();
    
    /**
     * For use in applyBlockUpdates. That routine is not re-entrant, so can keep and reuse one of these.
     */
    private final AdjustmentTracker tracker = new AdjustmentTracker();
    
    public WorldStateBuffer(World worldIn)
    {
        this.realWorld = worldIn;
    }
    //TODO - avoid BlockPos and call Chunk version after getChunkByChunkCoord
    
    public IBlockState getBlockState(BlockPos pos)
    {
        return this.getBlockState(pos.getX(), pos.getY(), pos.getZ());
    }
    
    public IBlockState getBlockState(long packedPos)
    {
        return this.getBlockState(PackedBlockPos.getX(packedPos), PackedBlockPos.getY(packedPos), PackedBlockPos.getZ(packedPos));
    }
    
    public IBlockState getBlockState(int x, int y, int z)
    {        
//        if(x==478 && y == 9 && z == -1231)
//            Adversity.log.info("boop");
        
        ChunkBuffer chunk = chunks.get(PackedBlockPos.getPackedChunkPos(x, z));
        
        if(chunk == null) 
        {
//            Adversity.log.info("blockstate from world @" + x + ", " + y + ", " + z + " = " + 
//                    this.realWorld.getChunkFromChunkCoords(x >> 4, z >> 4).getBlockState(x, y, z).toString());
            
            return this.realWorld.getChunkFromChunkCoords(x >> 4, z >> 4).getBlockState(x, y, z);
        }
        else
        {
            return chunk.getBlockState(x, y, z);
        }
    }
    
    public void setBlockState(long packedPos, IBlockState newState, IBlockState expectedPriorState)
    {
        this.setBlockState(PackedBlockPos.getX(packedPos), PackedBlockPos.getY(packedPos), PackedBlockPos.getZ(packedPos), newState, expectedPriorState);
    }
    
    public void setBlockState(int x, int y, int z, IBlockState newState, IBlockState expectedPriorState)
    {
//        Adversity.log.info("blockstate buffer update @" + x + ", " + y + ", " + z + " = " + 
//                newState.toString() + " from " + expectedPriorState.toString());
//        
//        if(x==478 && y == 9 && z == -1231)
//            Adversity.log.info("boop");
        
        if(Adversity.DEBUG_MODE) this.stateSetCount++;
        
        getChunkBuffer(x, z).setBlockState(x, y, z, newState, expectedPriorState);
    }
    
    private ChunkBuffer getChunkBuffer(int blockX, int blockZ)
    {
        long packedChunkPos = PackedBlockPos.getPackedChunkPos(blockX, blockZ);
        
        ChunkBuffer chunk = chunks.get(packedChunkPos);
        
        if(chunk == null) 
        {
            synchronized(chunks)
            {
                chunk = chunks.get(packedChunkPos);
                if(chunk == null) 
                {
                    chunk = this.usedBuffers.poll();
                    if(chunk == null)
                    {
                        chunk = new ChunkBuffer(packedChunkPos, Simulator.instance.getCurrentSimTick());
                    }
                    else
                    {
                        chunk.renew(packedChunkPos, Simulator.instance.getCurrentSimTick());
                    }
                    chunks.put(packedChunkPos, chunk);
                }
            }
        }
        return chunk;
    }
    
    /**
     * For use by cell chunk buffer manager.
     */
    public ChunkBuffer getChunkBufferIfExists(long packedChunkPos)
    {
        return chunks.get(packedChunkPos);
    }
    
    /** 
     * Makes the updates in the game world for up to chunkCount chunks.
     * Returns list of chunks that were updated so that they can be used for cell validation.
     * 
     */
    //public List<Chunk> applyBlockUpdates(int chunkCount, AbstractLavaSimulator sim)
    public void applyBlockUpdates(int chunkCount, AbstractLavaSimulator sim)
    {
        int currentTick = Simulator.instance.getCurrentSimTick();
        
        boolean maybeSomethingToDo = true;
        int foundCount = 0;
        
        
//        ArrayList<Chunk> result = new ArrayList<Chunk>();
        
        //TODO: make tick diff configurable
        
        // should not be getting concurrent blockstate calls, so use this
        // opportunity to clean out empty chunk buffers
        Iterator<ChunkBuffer> things = chunks.values().iterator();
        while(things.hasNext())
        {
            ChunkBuffer buff = things.next();
            if(buff.size() == 0)
            {
                things.remove();
                this.usedBuffers.add(buff);
                
//                if(Adversity.DEBUG_MODE)
//                    Adversity.log.info("Successful unqueud chunk buffer due to complete state reversion");
            }
            
        }
       

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
//                result.add(this.realWorld.getChunkFromChunkCoords(PackedBlockPos.getChunkXPos(best.packedChunkpos), PackedBlockPos.getChunkZPos(best.packedChunkpos)));
                this.chunks.remove(best.packedChunkpos);
                best.applyBlockUpdates(tracker, sim);
                this.usedBuffers.add(best);
            }
        }
        
//        return result;
    }
    
    public void clearStatistics()
    {
        this.stateApplicationCount = 0;
        this.stateSetCount = 0;
    }
    
    public int getStateSetCount()
    {
        return this.stateSetCount;
    }
    
    public int getStateApplicationCount()
    {
        return this.stateApplicationCount;
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
            int x = saveData[i++];
            int y = saveData[i++];
            int z = saveData[i++];
            this.getChunkBuffer(x, z).setBlockState(x, y, z, Block.getStateById(saveData[i++]), Block.getStateById(saveData[i++]));
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
    
    private static int getChunkStateKeyFromBlockPos(int x, int y, int z)
    {
        return ((y & 0xFF) << 8) | ((x & 0xF) << 4) | (z & 0xF);
    }
    
    /** returns true an update occured */
    private boolean adjustFillIfNeeded(BlockPos pos, AbstractLavaSimulator sim)
    {
//        if(pos.getX()==59 && (pos.getY() == 69 || pos.getY() == 70) && pos.getZ() == 129)
//            Adversity.log.info("boop");
        IBlockState baseState = realWorld.getBlockState(pos);
        if(baseState.getBlock() == NiceBlockRegistrar.COOL_SQUARE_BASALT_BLOCK)
        {
            if( !IFlowBlock.shouldBeFullCube(baseState, realWorld, pos))
            {
                realWorld.setBlockState(pos, NiceBlockRegistrar.COOL_FLOWING_BASALT_HEIGHT_BLOCK.getDefaultState().withProperty(NiceBlock.META, baseState.getValue(NiceBlock.META)));
                return true;
            }
            else
            {
                return false;
            }
        }
        else if(baseState.getBlock() == NiceBlockRegistrar.COOL_FLOWING_BASALT_HEIGHT_BLOCK)
        {
            if(IFlowBlock.shouldBeFullCube(baseState, realWorld, pos))
            {
                realWorld.setBlockState(pos, NiceBlockRegistrar.COOL_SQUARE_BASALT_BLOCK.getDefaultState().withProperty(NiceBlock.META, baseState.getValue(NiceBlock.META)));
                return true;
            }
            else
            {
                return false;
            }
        }
        
        
        IBlockState newState = IFlowBlock.adjustFillIfNeeded(realWorld, pos);
        
        if(newState == null)
        {
            // replace static flow height blocks with dynamic version
            if(baseState.getBlock() instanceof FlowStaticBlock)
            {
                ((FlowStaticBlock)baseState.getBlock()).makeDynamic(baseState, realWorld, pos);
                return true;
            }
            else
            {
                return false;
            }
        }
        
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
//        private BlockPos pos;
        private IBlockState newState;
        private IBlockState expectedPriorState;
        
        public boolean isRequired()
        {
            // deflect strangeness
            if(this.newState == null || this.expectedPriorState == null) return false;

            if(this.expectedPriorState.getMaterial() == Material.AIR)
            {
                // treat transition between small block and air as low priority for world update
                // helps prevent large number of world block updates due to vertical instability
                int newHeight = IFlowBlock.getFlowHeightFromState(newState);
                return newHeight > 2;
            }
            
            int oldHeight = IFlowBlock.getFlowHeightFromState(expectedPriorState);
            if(oldHeight > 0)
            {
                if(newState.getMaterial() == Material.AIR)
                {
                    // treat transition between small block and air as low priority for world update
                    // helps prevent large number of world block updates due to vertical instability
                    return oldHeight > 2;
                }
                else
                {
                    //if old and new are same flow blocks, defer small height changes
                    if(newState.getBlock() == expectedPriorState.getBlock())
                    {
                        int newHeight = IFlowBlock.getFlowHeightFromState(newState);
                        return Math.abs(oldHeight - newHeight) > 2;
                    }
                    else
                    {
                        // required if different blocks, even if height is similar
                        return true;
                    }
                }
            }
            else
            {
                //implies change to block (other than air to/for small flow)
                return true;
            }
        }
        
        public BlockStateBuffer( IBlockState newState, IBlockState expectedPriorState)
        {
//            this.pos = pos;
            this.newState = newState;
            this.expectedPriorState = expectedPriorState;
        }
        
        public IBlockState getNewState() { return this.newState; }
        public IBlockState getExpectedPriorState() { return this.expectedPriorState; }
//        public BlockPos getBlockPos() { return this.pos; }
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
        private int chunkXStart;
        private int chunkZStart;
        
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
            return new BlockPos(this.chunkXStart + x, y, this.chunkZStart + z);
        }
        
        /** 
         * Sets flag to true for all adjacent spaces that might be affected by a flow height block.
         * Assumes position is within the chunk being tracked. (Not the border of a neighbor chunk.)
         */
        public void setAdjustmentNeededAround(int xIn, int yIn, int zIn)
        {
            int x = xIn & 0xF;
            int z = zIn & 0xF;
            int minY = Math.max(0, yIn - 2);
            int maxY = Math.min(255, yIn + 2);
            
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
        public void excludeAdjustmentNeededAt(int xIn, int yIn, int zIn)
        {
            exclusions.set(getIndex(xIn & 0xF, yIn, zIn & 0xF), true);
        }
        
        public Collection<BlockPos> getAdjustmentPositions(long packedChunkPos)
        {
            this.chunkXStart = PackedBlockPos.getChunkXStart(packedChunkPos);
            this.chunkZStart = PackedBlockPos.getChunkZStart(packedChunkPos);
            bits.andNot(exclusions);
            return bits.stream().mapToObj(i -> getBlockPos(i)).collect(Collectors.toList());
        }
    }
    
//    private static AtomicInteger recoveryCount = new AtomicInteger(0);
    private static AtomicInteger totalCount = new AtomicInteger(0);
    
    public class ChunkBuffer
    {
        private long packedChunkpos;
        
        private int tickCreated;
        
        private AtomicInteger requiredUpdateCount = new AtomicInteger(0);
        
        private AtomicInteger dataCount = new AtomicInteger(0);
        
        /** shows number of updates per level*/
        private AtomicInteger[] levelCounts = new AtomicInteger[256];
        
        private BlockStateBuffer[] states = new BlockStateBuffer[0x10000];
                
        private ChunkBuffer(long packedChunkpos, int tickCreated)
        {
            this.packedChunkpos = packedChunkpos;
            this.tickCreated = tickCreated;
            for(int i = 0; i < 256; i++)
            {
                levelCounts[i] = new AtomicInteger(0);
            }
        }
        
        private void renew(long packedChunkpos, int tickCreated)
        {
            this.packedChunkpos = packedChunkpos;
            this.tickCreated = tickCreated;
            this.requiredUpdateCount.set(0);
            this.dataCount.set(0);
            for(int i = 0; i < 256; i++)
            {
                levelCounts[i].set(0);
            }
            Arrays.fill(states, null);
        }
        
        public IBlockState getBlockState(int x, int y, int z)
        {
            BlockStateBuffer entry = states[getChunkStateKeyFromBlockPos(x, y, z)];
            
            if(entry == null)
            {
//                Adversity.log.info("blockstate from world @" + x + ", " + y + ", " + z + " = " + 
//                        realWorld.getChunkFromChunkCoords(x >> 4, z >> 4).getBlockState(x, y, z).toString());
                
                return realWorld.getChunkFromChunkCoords(x >> 4, z >> 4).getBlockState(x, y, z);
            }
            else
            {
//                Adversity.log.info("blockstate from buffer @" + x + ", " + y + ", " + z + " = " + 
//                        entry.newState.toString());
                
                return entry.newState;
            }
        }
        
        /**
         * No mechanism here to make this threadsafe, however the lava simulator will never
         * attempt to update the same cell concurrently, so should never be a collision.
         */
        private void setBlockState(int x, int y, int z, IBlockState newState, IBlockState expectedPriorState)
        {
            totalCount.incrementAndGet();
            
            int key = getChunkStateKeyFromBlockPos(x, y, z);
            BlockStateBuffer state = states[key];
            if(state == null)
            {
                state = new BlockStateBuffer(newState, expectedPriorState);
                this.states[key] = state;
                this.dataCount.incrementAndGet();
                this.levelCounts[y].incrementAndGet();
                if(state.isRequired()) this.requiredUpdateCount.incrementAndGet();
            }
            else
            {
                if(newState == state.expectedPriorState)
                {
//                    Adversity.log.info("Block state removed due to reversion");
                    this.states[key] = null;
                    this.dataCount.decrementAndGet();
                    this.levelCounts[y].decrementAndGet();
                    if(state.isRequired()) this.requiredUpdateCount.decrementAndGet();
                    
//                    if((recoveryCount.getAndIncrement() & 0xFFF) == 0xFFF)
//                        Adversity.log.info("BlockStateRecoveries = " + recoveryCount.get() + " of " + totalCount.get() + ", " + recoveryCount.get() * 100 / totalCount.get() + " percent recovery");
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
        private void applyBlockUpdates(AdjustmentTracker tracker, AbstractLavaSimulator sim)
        {

            tracker.clear();
            int count = this.dataCount.get();
            int allRemaining = count;
            
            int chunkStartX = PackedBlockPos.getChunkXStart(this.packedChunkpos);
            int chunkStartZ = PackedBlockPos.getChunkZStart(this.packedChunkpos);
            
//            Adversity.log.info(sim.getTickIndex() + " Applying " + count + " block updates for chunk with startX=" + chunkStartX + " and startZ=" + chunkStartZ);
            
            for(int y = 0; y < 256; y++)
            {
                if(this.levelCounts[y].get() > 0)
                {
                    int statesRemainingThisY = this.levelCounts[y].get();
                    int indexStart = y << 8;
                    int indexEnd = indexStart + 256;
                    
                    for(int i = indexStart; i < indexEnd; i++)
                    {
                        if(this.states[i] != null)
                        {
                            BlockStateBuffer bsb = states[i];
                            
                            int x = chunkStartX +  ((i >> 4) & 0xF);
                            int z = chunkStartZ + (i & 0xF);
                            
                            if(IFlowBlock.isFlowHeight(bsb.newState.getBlock()))
                            {
                                tracker.setAdjustmentNeededAround(x, y, z);
                                // set to height block so no need to look for filler
                                tracker.excludeAdjustmentNeededAt(x, y, z);
                            }
                            else if(IFlowBlock.isFlowHeight(bsb.expectedPriorState.getBlock()))
                            {
                                // difference here is simply that we allow fillers in the block being set
                                tracker.setAdjustmentNeededAround(x, y, z);
                            }
                            
                            
//                            Adversity.log.info("applying blockstate to world @" + x + ", " + y + ", " + z + " = " + 
//                                    bsb.newState.toString());
//                            
//                            if(x==59 && (y == 69 || y == 70) && z == 129)
//                                Adversity.log.info("boop");
                      
                            realWorld.setBlockState(new BlockPos( x, y, z), bsb.newState, 3);
                            
                            this.states[i] = null;
                            allRemaining--;
                            if(--statesRemainingThisY == 0) break;
                        }
                    }
                    this.levelCounts[y].set(0);
                }
                if(allRemaining == 0) break;
            }

            this.dataCount.set(0);
            this.requiredUpdateCount.set(0);
            
            for(BlockPos p : tracker.getAdjustmentPositions(this.packedChunkpos))
            {
                if(adjustFillIfNeeded(p, sim)) count++;
            }
            
            if(Adversity.DEBUG_MODE) stateApplicationCount += count;

        }
        
        private int size()
        {
            return this.dataCount.get();
        }
        
        /** returns the next index that should be used */
        private int writeSaveData(int[] saveData, int startingIndex)
        {
            int allRemaining = this.size();
            int chunkStartX = PackedBlockPos.getChunkXStart(this.packedChunkpos);
            int chunkStartZ = PackedBlockPos.getChunkZStart(this.packedChunkpos);
            
            for(int y = 0; y < 256; y++)
            {
                if(this.levelCounts[y].get() > 0)
                {
                    int statesRemainingThisY = this.levelCounts[y].get();
                    int indexStart = y << 8;
                    int indexEnd = indexStart + 256;
                    
                    for(int i = indexStart; i < indexEnd; i++)
                    {
                        BlockStateBuffer state = this.states[i];
                        if(state != null)
                        {
                            saveData[startingIndex++] = chunkStartX + ((i  >> 4) & 0xF);
                            saveData[startingIndex++] = y;
                            saveData[startingIndex++] = chunkStartZ + (i & 0xF);
                            saveData[startingIndex++] = Block.getStateId(state.newState);
                            saveData[startingIndex++] = Block.getStateId(state.expectedPriorState);
                            allRemaining--;
                            if(--statesRemainingThisY == 0) break;
                        }
                    }
                }
                if(allRemaining == 0) break;
            }
            return startingIndex;
        }
        
        public long getPackedChunkPos()
        {
            return this.packedChunkpos;
        }
    }
    
 
}
