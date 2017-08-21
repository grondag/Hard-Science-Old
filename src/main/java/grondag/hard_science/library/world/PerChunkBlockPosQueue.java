package grondag.hard_science.library.world;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import net.minecraft.util.math.BlockPos;

/**
 * Maintains a queue of block positions for each world chunk.
 * Per-chunk data is sparse.
 */
public class PerChunkBlockPosQueue
{
    private Long2ObjectOpenHashMap<LongArrayFIFOQueue> chunks = new Long2ObjectOpenHashMap<LongArrayFIFOQueue>();
    
    
    
    public void queue(BlockPos pos)
    {
        long packedChunkPos = PackedBlockPos.getPackedChunkPos(pos);
        
        LongArrayFIFOQueue chunkQueue = chunks.get(packedChunkPos);
        
        if(chunkQueue == null)
        {
            chunkQueue = new LongArrayFIFOQueue();
            chunks.put(packedChunkPos, chunkQueue);
        }
        chunkQueue.enqueue(PackedBlockPos.pack(pos));
    }
    
    /**
     * Returns next queued block pos in the same chunk as the given block pos.
     * Returns null of no positions are queued for that chunk.
     */
    public BlockPos dequeue(BlockPos pos)
    {
        long packedChunkPos = PackedBlockPos.getPackedChunkPos(pos);
        
        LongArrayFIFOQueue chunkQueue = chunks.get(packedChunkPos);
        
        if(chunkQueue == null) return null;
        
        if(chunkQueue.isEmpty())
        {
            chunks.remove(packedChunkPos);
            return null;
        }
        
        BlockPos result = PackedBlockPos.unpack(chunkQueue.dequeueLong());
        
        if(chunkQueue.isEmpty()) chunks.remove(packedChunkPos);
        
        return result;
    }
    
    public void clear()
    {
        this.chunks.forEach((k, v) -> v.clear());
        this.chunks.clear();
    }
    
}
