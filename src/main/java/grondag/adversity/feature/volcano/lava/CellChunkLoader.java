package grondag.adversity.feature.volcano.lava;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import gnu.trove.impl.sync.TSynchronizedLongSet;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import grondag.adversity.feature.volcano.lava.WorldStateBuffer.ChunkBuffer;
import grondag.adversity.feature.volcano.lava.cell.builder.ColumnChunkBuffer;
import grondag.adversity.library.PackedBlockPos;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.world.chunk.Chunk;
/**
 * Manages snapshots of chunk data to be used for creating and updating lava cells.
 */
public class CellChunkLoader
{

//    private final Long2ObjectMap<ColumnChunkBuffer> chunkBufferQueue = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<ColumnChunkBuffer>());
   
    private final ConcurrentLinkedQueue<ColumnChunkBuffer> chunkBufferQueue = new ConcurrentLinkedQueue<ColumnChunkBuffer>();

    
    private final TLongSet markedChunks = new TSynchronizedLongSet( new TLongHashSet());
    
    private final ConcurrentLinkedQueue<ColumnChunkBuffer> unusedBuffers = new ConcurrentLinkedQueue<ColumnChunkBuffer>();
    
    /**
     * TODO
     * ---------------
     * Persistence
     * Consider providing a way to refresh a single cell instead of a whole chunk
     * 
     */
    
//    /**
//     * Use this for chunks that have just had block updates applied, when we know that the world buffer is empty for that chunk.
//     * If any chunks have already been queued by earlier calls to this or other methods, will replace those chunks.
//     */
//    public void queueUpdatedChunks(List<Chunk> chunks)
//    {
//        for(Chunk c : chunks)
//        {
//            this.queueChunkBuffer(c);
//        }
//    }
    
    /**
     * Use this to identify chunks that need to have cells created or updated due to world events.
     * 1) If a chunk has lava but hasn't been synchronized with the world recently.
     * 2) When lava is added to the world causing cells for a new chunk to be loaded.
     * 3) When block update in the world occur to a chunk that has cells, indicating validation would be a good idea.
     * 
     * Note that this method does not actually buffer the chunks and put them in the queue. 
     * To do that, use {@link #queueMarkedChunks(WorldStateBuffer)}. Marking and then loading
     * separately prevent re-queuing the same chunk when multiple triggering world events occur in the same chunk.
     */
    public void markChunk(long packedChunkPos)
    {
        this.markedChunks.add(packedChunkPos);
    }

    /**
     * Use this to read and queue chunks marked earlier by {@link #markChunk(long)}.
     * If any chunks have already been queued by earlier calls to this or other methods, 
     * will replace those chunks with a more recent snapshot.
     */
    public void queueMarkedChunks(WorldStateBuffer worldBuffer)
    {
        for(long chunkPos : this.markedChunks.toArray())
        {
            ChunkBuffer chunkBuff = worldBuffer.getChunkBufferIfExists(chunkPos);
            
            if(chunkBuff == null)
            {
                // nothing in world buffer, so can use raw chunk from world
                this.queueChunkBuffer(worldBuffer.realWorld
                        .getChunkFromChunkCoords(PackedBlockPos.getChunkXPos(chunkPos), PackedBlockPos.getChunkZPos(chunkPos)));
            }
            else
            {
                // world buffer has changes, so have to use the chunk buffer
                this.queueChunkBuffer(chunkBuff);
            }
            this.markedChunks.remove(chunkPos);
        }
    }
    
    
    /**
     * Adds a snapshot of block data for this chunk to the set of chunks to be used to create/update new cells.
     * This version is for unbuffered chunks, where no simulation state exists different from the world.
     */
    private void queueChunkBuffer(Chunk chunk)
    {
        ColumnChunkBuffer newBuffer = this.getEmptyBuffer();
        newBuffer.readChunk(chunk);
        this.chunkBufferQueue.offer(newBuffer);
    }
    
    /**
     * Adds a snapshot of block data for this buffered chunk to the set of chunks to be used to create/update new cells.
     * This version is for buffered chunks, so reads in state that has not yet been written to the world.
     */
    private void queueChunkBuffer(ChunkBuffer chunkBuffer)
    {
        ColumnChunkBuffer newBuffer = this.getEmptyBuffer();
        newBuffer.readChunk(chunkBuffer);
        this.chunkBufferQueue.offer(newBuffer);    
    }
    
//    /** returns buffer if it exists, creates empty buffer if not */
//    private ColumnChunkBuffer getOrCreateBuffer(long packedChunkPos)
//    {
//        ColumnChunkBuffer buffer;
//        synchronized(chunkBufferQueue)
//        {
//            buffer = this.chunkBufferQueue.get(packedChunkPos);
//            if(buffer == null) 
//            {
//                buffer = getEmptyBuffer();
//                this.chunkBufferQueue.put(packedChunkPos, buffer);
//            }
//        }
//        return buffer;
//    }
    
    private ColumnChunkBuffer getEmptyBuffer()
    {
        ColumnChunkBuffer buffer = this.unusedBuffers.poll();
        if(buffer == null)
        {
            buffer = new ColumnChunkBuffer();
        }
        return buffer;
    }
    
    public boolean isEmpty()
    {
        return this.chunkBufferQueue.isEmpty();
    }
    
    /** 
     * Returns (and removes) one queued chunk.
     * No promises made about which one.
     * Returns null if empty.
     */
    
    public ColumnChunkBuffer poll()
    {
        return this.chunkBufferQueue.poll();
    }
    
  
}
