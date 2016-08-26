package grondag.adversity.niceblock.base;

import grondag.adversity.niceblock.modelstate.ModelState;

public interface SimpleCacheLoader<V>
{
    abstract public V load(ModelState state);
}
