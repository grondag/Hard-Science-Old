package grondag.hard_science.library.world;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class ChunkBlockMap<T>
{
    protected final HashMap<BlockPos, T> blocks = new HashMap<BlockPos, T>();
    
    protected List<Pair<BlockPos, T>> sortedList;
    
    public final AxisAlignedBB chunkAABB;
//    public final int chunkX;
//    public final int chunkZ;
    
    private static final int CHUNK_START_MASK = ~0xF;
    
    
    public ChunkBlockMap(BlockPos pos)
    {
//        this.chunkX = pos.getX() >> 4;
//        this.chunkZ = pos.getZ() >> 4;
        this.chunkAABB = new AxisAlignedBB(
                pos.getX() & CHUNK_START_MASK,
                0,
                pos.getZ() & CHUNK_START_MASK,
                pos.getX() | 0xF,
                255,
                pos.getZ() | 0xF);
    }
    /**
     * ASSUMES POSITION IS IN THIS CHUNK!
     */
    public T get(BlockPos pos)
    {
        return this.get(pos);
    }
  
    /**
     * ASSUMES POSITION IS IN THIS CHUNK!
     */
    public boolean containsValueAt(BlockPos pos)
    {
        return this.blocks.containsKey(pos);
    }
    
    /**
     * ASSUMES POSITION IS IN THIS CHUNK!
     * Returns previous value.
     */
    public T put(BlockPos pos, T value)
    {
        this.sortedList = null;
        return this.blocks.put(pos, value);
    }
    
   
    /**
     * ASSUMES POSITION IS IN THIS CHUNK!
     * Returns previous value.
     */
    public T remove(BlockPos pos)
    {
        this.sortedList = null;
        return this.blocks.remove(pos);
    }
  
    public int size()
    {
        return this.blocks.size();
    }
    
    public boolean isEmpty()
    {
        return this.blocks.isEmpty();
    }
    
    public void clear()
    {
        this.blocks.clear();
    }
    
    /**
     * Sorted from bottom to top.
     */
    public List<Pair<BlockPos, T>> asSortedList()
    {
        if(this.sortedList == null)
        {
            if(this.blocks.isEmpty())
            {
                this.sortedList = Collections.emptyList();
            }
            else
            {
                this.sortedList = this.blocks.entrySet().stream()
                        .map(new Function<HashMap.Entry<BlockPos, T>, Pair<BlockPos, T>>(){

                            @Override
                            public Pair<BlockPos, T> apply(Entry<BlockPos, T> t)
                            {
                                return Pair.of(t.getKey(), t.getValue());
                            }})
                        .sorted(new Comparator<Pair<BlockPos, T>>() {

                            @Override
                            public int compare(Pair<BlockPos, T> o1, Pair<BlockPos, T> o2)
                            {
                                return Integer.compare(o1.getLeft().getY(), o2.getLeft().getY());
                            }})
                        .collect(ImmutableList.toImmutableList());
            }
        }
        return this.sortedList;
    }
}
