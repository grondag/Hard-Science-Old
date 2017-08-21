package grondag.hard_science.library.world;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.World;

/**
 * Keeps lazily-loaded objects, one-per dimension.
 * Not thread-safe.
 */
public abstract class PerWorldReference<T> extends Int2ObjectOpenHashMap<T>
{
    /**
     * 
     */
    private static final long serialVersionUID = 318003886323074885L;

    @Override
    public T get(int dimension)
    {
        T result = super.get(dimension);
        if(result == null)
        {
            result = load(dimension);
            super.put(dimension, result);
        }
        return result;
    }
    
    public T get(World world)
    {
        return this.get(world.provider.getDimension());
    }
    
    protected abstract T load(int dimension);
}
