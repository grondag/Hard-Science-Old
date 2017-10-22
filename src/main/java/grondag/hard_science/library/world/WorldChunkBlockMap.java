package grondag.hard_science.library.world;

import java.util.function.Function;

import net.minecraft.util.math.BlockPos;

public class WorldChunkBlockMap<T> extends WorldMap<ChunkMap<ChunkBlockMap<T>>>
{

    /**
     * 
     */
    private static final long serialVersionUID = 4048164246377574473L;

    @Override
    protected ChunkMap<ChunkBlockMap<T>> load(int dimension)
    {
        return new ChunkMap<ChunkBlockMap<T>>(new Function<BlockPos, ChunkBlockMap<T>>(){

            @Override
            public ChunkBlockMap<T> apply(BlockPos t)
            {
                return new ChunkBlockMap<T>(t);
            }});
    }
}
