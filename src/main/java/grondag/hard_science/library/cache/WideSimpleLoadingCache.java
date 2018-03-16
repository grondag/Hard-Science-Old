package grondag.hard_science.library.cache;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import grondag.exotic_matter.varia.Useful;


public class WideSimpleLoadingCache<V> implements ISimpleLoadingCache
{
    private final int capacity;
    private final int maxFill;
    protected final int positionMask;
    
    protected final WideSimpleCacheLoader<V> loader;
    
    private final AtomicInteger backupMissCount = new AtomicInteger(0);
    
    protected volatile WideCacheState<V> activeState;
    private final AtomicReference<WideCacheState<V>> backupState = new AtomicReference<WideCacheState<V>>();
    
    private final Object writeLock = new Object();

    public WideSimpleLoadingCache(WideSimpleCacheLoader<V> loader, int maxSize)
    {
        this.capacity = 1 << (Long.SIZE - Long.numberOfLeadingZeros((long) (maxSize / ISimpleLoadingCache.LOAD_FACTOR)));
        this.maxFill = (int) (capacity * ISimpleLoadingCache.LOAD_FACTOR);
        this.positionMask = (capacity * 2) - 1;
        this.loader = loader;
        this.activeState = new WideCacheState<V>(this.capacity);
        this.clear();
    }

    public int size() { return activeState.size.get(); }
    
    public void clear()
    {
        this.activeState = new WideCacheState<V>(this.capacity);
    }
    
    public V get(long key1, long key2)
    {
        WideCacheState<V> localState = activeState;
        
        // Zero value normally indicates an unused spot in key array
        // so requires privileged handling to prevent search weirdness.
        if(key1 == 0 && key2 == 0)
        {
            V value = localState.zeroValue.get();
            if(value == null)
            {
                value = loader.load(0, 0);
                if(localState.zeroValue.compareAndSet(null, value))
                {
                    return value;
                }
                else
                {
                    //another thread got there start
                    return localState.zeroValue.get();
                }
            }
            return value;
        }
        
        int position = (((int)Useful.longHash(key1) ^ (int)Useful.longHash(key2)) * 2) & positionMask;
        
        do
        {
            if(localState.keys[position] == key1 && localState.keys[position + 1] == key2) return localState.values[position >> 1];
            
            if(localState.keys[position] == 0 && localState.keys[position + 1] == 0) return load(localState, key1, key2, position);
            
            position = (position + 2) & positionMask;
            
        } while (true);
    }

    protected V loadFromBackup(WideCacheState<V> backup, final long key1, final long key2)
    {
        int position = (((int)Useful.longHash(key1) ^ (int)Useful.longHash(key2)) * 2) & positionMask;
        do
        {
            if(backup.keys[position] == key1 && backup.keys[position + 1] == key2) return backup.values [position >> 1];
            if(backup.keys[position] == 0 && backup.keys[position + 1] == 0)
            {
                if((backupMissCount.incrementAndGet() & 0xFF) == 0xFF) 
                {
                    if(backupMissCount.get() > activeState.size.get() / 2)
                    {
                        backupState.compareAndSet(backup, null);
                    }
                }
                return loader.load(key1, key2);
            }
            position = (position + 2) & positionMask;
        } while(true);
    }
    
    
    protected V load(WideCacheState<V> localState, long key1, long key2, int position)
    {        
        // no need to handle zero key here - is handled as privileged case in get();
        
        WideCacheState<V> backupState = this.backupState.get();
        
        final V result = backupState == null ? loader.load(key1, key2) : loadFromBackup(backupState, key1, key2);
        
        do
        {
            long currentKey1, currentKey2;       
            
            synchronized(writeLock)
            {
                currentKey1 = localState.keys[position];
                currentKey2 = localState.keys[position + 1];
                if(currentKey1 == 0 && currentKey2 == 0)
                {
                    //write value start in case another thread tries to read it based on key before we can write it
                    localState.values[position >> 1] = result;
                    localState.keys[position] = key1;
                    localState.keys[position + 1] = key2;
                    break;
                }
            }
            
            // small chance another thread added our value before we got our lock
            if(currentKey1 == key1 && currentKey2 == key2) return localState.values[position >> 1];
            
            position = (position + 2) & positionMask;
            
        } while(true);
        
        if(localState.size.incrementAndGet() == this.maxFill)
        {
            WideCacheState<V> newState = new WideCacheState<V>(this.capacity);
            // doing this means we don't have to handle zero value in backup cache value lookup
            newState.zeroValue.set(this.activeState.zeroValue.get());
            this.backupState.set(this.activeState);
            this.activeState = newState;
            this.backupMissCount.set(0);
        }
        
        return result;
    }
    
    /** for test harness */
    public WideSimpleLoadingCache<V> createNew(WideSimpleCacheLoader<V> loader, int startingCapacity)
    {
        return new WideSimpleLoadingCache<V>(loader, startingCapacity);
    }
}