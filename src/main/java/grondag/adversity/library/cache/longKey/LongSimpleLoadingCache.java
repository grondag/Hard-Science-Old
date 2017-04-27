package grondag.adversity.library.cache.longKey;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import grondag.adversity.library.Useful;

//import java.util.concurrent.atomic.AtomicInteger;

public class LongSimpleLoadingCache<V> implements ILongLoadingCache<V>
{
    private final int capacity;
    private final int maxFill;
    private final int positionMask;
    
    private final LongSimpleCacheLoader<V> primaryLoader;
    private final LongSimpleCacheLoader<V> backupLoader = new BackupLoadHandler();
    private volatile LongSimpleCacheLoader<V> activeLoader;
    
    private final AtomicInteger backupMissCount = new AtomicInteger(0);
    
    private volatile LongCacheState<V> activeState;
    private final AtomicReference<LongCacheState<V>> backupState = new AtomicReference<LongCacheState<V>>();
    
    private Object writeLock = new Object();
    
    final static float LOAD_FACTOR = 0.75F;

    public LongSimpleLoadingCache(LongSimpleCacheLoader<V> loader, int maxSize)
    {
        this.capacity = 1 << (Long.SIZE - Long.numberOfLeadingZeros((long) (maxSize / LOAD_FACTOR)));
        this.maxFill = (int) (capacity * LongSimpleLoadingCache.LOAD_FACTOR);
        this.positionMask = capacity - 1;
        this.primaryLoader = loader;
        this.activeLoader = loader;
        this.activeState = new LongCacheState<V>(this.capacity);
        this.clear();
    }

    @Override
    public int size() { return activeState.size.get(); }
    
    @Override
    public void clear()
    {
        this.activeState = new LongCacheState<V>(this.capacity);
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
                value = primaryLoader.load(0);
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
        
        int position = (int) (Useful.longHash(key) & positionMask);
        
        do
        {
            if(localState.keys[position] == key) return localState.values[position];
            
            if(localState.keys[position] == 0) return load(localState, key, position);
            
            position = (position + 1) & positionMask;
            
        } while (true);
    }
    
    
    private class BackupLoadHandler implements LongSimpleCacheLoader<V>
    {
        @Override
        public V load(final long key)
        {
            LongCacheState<V> backup = backupState.get();
            int position = (int) (Useful.longHash(key) & positionMask);
            do
            {
                if(backup.keys[position] == key) return backup.values[position];
                if(backup.keys[position] == 0)
                {
                    if((backupMissCount.incrementAndGet() & 0xFF) == 0xFF) 
                    {
                        if(backupMissCount.get() > activeState.size.get() / 2)
                        {
                            activeLoader = primaryLoader;
                            backupState.compareAndSet(backup, null);
                        }
                    }
                    return primaryLoader.load(key);
                }
                position = (position + 1) & positionMask;
            } while(true);
        }
    }
    
    private V load(LongCacheState<V> localState, long key, int position)
    {        
        // no need to handle zero key here - is handled as special case in get();
        final V result = activeLoader.load(key);
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
            
            position = (position + 1) & positionMask;
            
        } while(true);
        
        if(localState.size.incrementAndGet() == this.maxFill)
        {
            LongCacheState<V> newState = new LongCacheState<V>(this.capacity);
            // doing this means we don't have to handle zero value in backup cache value lookup
            newState.zeroValue.set(this.activeState.zeroValue.get());
            this.backupState.set(this.activeState);
            this.activeState = newState;
            this.activeLoader = backupLoader;
        }
        return result;
    }
    
    @Override
    public ILongLoadingCache<V> createNew(LongSimpleCacheLoader<V> loader, int startingCapacity)
    {
        return new LongSimpleLoadingCache<V>(loader, startingCapacity);
    }
}