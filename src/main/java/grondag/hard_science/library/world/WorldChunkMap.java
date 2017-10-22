package grondag.hard_science.library.world;

import java.util.function.Function;

import net.minecraft.util.math.BlockPos;

public class WorldChunkMap<T> extends WorldMap<ChunkMap<T>>
{
    /**
     * 
     */
    private static final long serialVersionUID = -2658181151614806545L;

    private final Function<BlockPos, T> entryFactory;
    
    public WorldChunkMap(Function<BlockPos, T> entryFactory)
    {
        this.entryFactory = entryFactory;
    }
    
    @Override
    protected ChunkMap<T> load(int dimension)
    {
        return new ChunkMap<T>(entryFactory);
    }
}
