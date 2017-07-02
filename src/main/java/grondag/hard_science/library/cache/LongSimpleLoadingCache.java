package grondag.hard_science.library.cache;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import grondag.hard_science.library.varia.Useful;


public class LongSimpleLoadingCache<V> implements ISimpleLoadingCache
{
    private final int capacity;
    private final int maxFill;
    protected final int positionMask;
    
    protected final LongSimpleCacheLoader<V> loader;
    
    private final AtomicInteger backupMissCount = new AtomicInteger(0);
    
    protected volatile LongCacheState<V> activeState;
    private final AtomicReference<LongCacheState<V>> backupState = new AtomicReference<LongCacheState<V>>();
    
    private final Object writeLock = new Object();

    public LongSimpleLoadingCache(LongSimpleCacheLoader<V> loader, int maxSize)
    {
        this.capacity = 1 << (Long.SIZE - Long.numberOfLeadingZeros((long) (maxSize / ISimpleLoadingCache.LOAD_FACTOR)));
        this.maxFill = (int) (capacity * ISimpleLoadingCache.LOAD_FACTOR);
        this.positionMask = capacity - 1;
        this.loader = loader;
        this.activeState = new LongCacheState<V>(this.capacity);
        this.clear();
    }

    public int size() { return activeState.size.get(); }
    
    public void clear()
    {
        this.activeState = new LongCacheState<V>(this.capacity);
    }
    
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
                value = loader.load(0);
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

    protected V loadFromBackup(LongCacheState<V> backup, final long key)
    {
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
                        backupState.compareAndSet(backup, null);
                    }
                }
                return loader.load(key);
            }
            position = (position + 1) & positionMask;
        } while(true);
    }
    
    
    protected V load(LongCacheState<V> localState, long key, int position)
    {        
        // no need to handle zero key here - is handled as special case in get();
        
        LongCacheState<V> backupState = this.backupState.get();
        
        final V result = backupState == null ? loader.load(key) : loadFromBackup(backupState, key);
        
        do
        {
            long currentKey;       
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
            this.backupMissCount.set(0);
        }
        
        return result;
    }
    
    /** for test harness */
    public LongSimpleLoadingCache<V> createNew(LongSimpleCacheLoader<V> loader, int startingCapacity)
    {
        return new LongSimpleLoadingCache<V>(loader, startingCapacity);
    }
}