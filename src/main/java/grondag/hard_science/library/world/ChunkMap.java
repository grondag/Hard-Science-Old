package grondag.hard_science.library.world;

import java.util.Iterator;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;

import grondag.exotic_matter.varia.PackedBlockPos;
import grondag.exotic_matter.varia.Useful;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

/**
 * Maintains a set of block positions for each world chunk.
 * Per-chunk data is sparse.
 */
public abstract class ChunkMap<T> implements Iterable<T>
{
    private final Long2ObjectOpenHashMap<T> chunks = new Long2ObjectOpenHashMap<T>();
//    private final BiFunction<? extends ChunkMap<T>, BlockPos, T> entryFactory;
    
    protected abstract T newEntry(BlockPos pos);
    
    public boolean contains(BlockPos pos)
    {
        long packedChunkPos = PackedBlockPos.getPackedChunkPos(pos);
        return chunks.containsKey(packedChunkPos);
    }
    
    public T getOrCreate(BlockPos pos)
    {
        long packedChunkPos = PackedBlockPos.getPackedChunkPos(pos);
        
        T result = chunks.get(packedChunkPos);
        
        if(result == null)
        {
            result = this.newEntry(pos);
            chunks.put(packedChunkPos, result);
        }
        return result;
    }
    
    public T getIfExists(BlockPos pos)
    {
        long packedChunkPos = PackedBlockPos.getPackedChunkPos(pos);
        
        return chunks.get(packedChunkPos);
    }
    
    public void remove(BlockPos pos)
    {
        long packedChunkPos = PackedBlockPos.getPackedChunkPos(pos);
        
        chunks.remove(packedChunkPos);
    }
    
    public void clear()
    {
        this.chunks.clear();
    }
        
    public Iterator<T> existingChunksNear(BlockPos pos, int chunkRadius)
    {
        return new AbstractIterator<T>()
        {
            private int i = 0;

            @Override
            protected T computeNext()
            {
                Vec3i offset = Useful.getDistanceSortedCircularOffset(i++);
                
                while(offset != null && offset.getY() <= chunkRadius)
                {
                    T result = getIfExists(pos.add(offset.getX() * 16, 0, offset.getZ() * 16));
                    if(result != null) return result;
                    offset = Useful.getDistanceSortedCircularOffset(i++);
                }

                return (T)this.endOfData();
            }
    
        };
    
    }

    @Override
    public Iterator<T> iterator()
    {
        return Iterators.unmodifiableIterator(this.chunks.values().iterator());
    }
}
