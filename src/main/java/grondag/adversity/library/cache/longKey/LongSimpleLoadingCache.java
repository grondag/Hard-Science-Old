package grondag.adversity.library.cache.longKey;

import grondag.adversity.library.Useful;

//import java.util.concurrent.atomic.AtomicInteger;

public class LongSimpleLoadingCache<V> implements ILongLoadingCache<V>
{
    public CacheOversizeHandler oversizeHandler = null;
    
    @Override
    public int size() { return activeState.size.get(); }
    
    private volatile LongCacheState<V> activeState;
    
    private Object writeLock = new Object();
    
    private volatile LongSimpleCacheLoader<V> loader;
    
    final static float LOAD_FACTOR = 0.75F;

    public LongSimpleLoadingCache(LongSimpleCacheLoader<V> loader, int startingCapacity)
    {
        if(startingCapacity < 64) startingCapacity = 64;
        this.loader = loader;
        this.activeState = new LongCacheState<V>(1 << (Long.SIZE - Long.numberOfLeadingZeros((long) (startingCapacity / LOAD_FACTOR))));
        this.clear();
    }

    /** releases loader and afterwards returns null for any keys not found */
    public ILongLoadingCache<V> getStaticCache()
    {
        return new LongSimpleStaticCache<V>(this.activeState);
    }

    @Override
    public LongSimpleCacheLoader<V> getLoader() { return this.loader; }
    
    @Override
    public void clear()
    {
        this.activeState = new LongCacheState<V>(this.activeState.capacity);
    }
    
    @Override
    public V get(long key)
    {
        LongCacheState<V> localState = activeState;
        
        // Zero value normally indicates an unused spot in key array
        // so requires special handling to prevent search weirdness.
        if(key == 0)
        {
            V value = localState.zeroValue.get();
            if(value == null)
            {
                value = loader.load(key);
                if(localState.zeroValue.compareAndSet(null, value))
                {
                    return value;
                }
                else
                {
                    //another thread got there first
                    return localState.zeroValue.get();
                }
            }
            return value;
        }
        
        int position = (int) (Useful.longHash(key) & localState.positionMask);
        
        do
        {
            if(localState.keys[position] == key) return localState.values[position];
            
            if(localState.keys[position] == 0) return load(localState, key, position);
            
            position = (position + 1) & localState.positionMask;
            
        } while (true);
    }
    
    private V load(LongCacheState<V> localState, long key, int position)
    {        
        // no need to handle zero key here - is handled as special case in get();
        final V result = loader.load(key);
        long currentKey;       
        
        do
        {
            synchronized(writeLock)
            {
                currentKey = localState.keys[position];
                if(currentKey == 0)
                {
                    //write value first in case another thread tries to read it based on key before we can write it
                    localState.values[position] = result;
                    localState.keys[position] = key;
                    break;
                }
            }
            
            // small chance another thread added our value before we got our lock
            if(currentKey == key) return localState.values[position];
            
            position = (position + 1) & localState.positionMask;
            
        } while(true);
        
        if(localState.size.incrementAndGet() == localState.maxFill && this.oversizeHandler != null) this.oversizeHandler.notifyOversize(); 
        return result;
    }
    
    private void expand() 
    {
        synchronized(writeLock)
        {
            int oldCapacity = activeState.capacity;
            int newCapacity = activeState.capacity << 1;
            LongCacheState<V> oldState = activeState;
            LongCacheState<V> newState = new LongCacheState<V>(newCapacity);
            int positionMask = newState.positionMask;
            newState.zeroValue.set(oldState.zeroValue.get());
            final long[] oldKeys = oldState.keys;
            final V[] oldValues = oldState.values;
            final long[] newKeys = newState.keys;
            final V[] newValues = newState.values;
            
            int newSize = 0;
       
            for(int j = oldCapacity - 1; j-- != 0;)
            {
                if(oldKeys[j] != 0)
                {
                    int position = (int) (Useful.longHash(oldKeys[j]) & positionMask);
                    if(newKeys[position] != 0)
                    {
                        do
                        {
                            position = (position + 1) & positionMask;
                        }
                        while (newKeys[position] != 0);
                    }
                    newKeys[position]= oldKeys[j];
                    newValues[position] = oldValues[j];
                    newSize++;
                }
            }
            
             newState.size.set(newSize);
            
            // make visible to readers
            activeState = newState;
        }
    }
    
    @Override
    public ILongLoadingCache<V> createNew(LongSimpleCacheLoader<V> loader, int startingCapacity)
    {
        return new LongSimpleLoadingCache<V>(loader, startingCapacity);
    }
    
    @Override
    public void setOversizeHandler(CacheOversizeHandler handler)
    {
        this.oversizeHandler = handler;
    }
}