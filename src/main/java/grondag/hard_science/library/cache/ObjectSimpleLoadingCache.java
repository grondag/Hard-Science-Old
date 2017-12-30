package grondag.hard_science.library.cache;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;

public class ObjectSimpleLoadingCache<K, V> implements ISimpleLoadingCache
{
    private final int capacity;
    private final int maxFill;
    protected final int positionMask;
    
    protected final ObjectSimpleCacheLoader<K, V> loader;
    
    private final AtomicInteger backupMissCount = new AtomicInteger(0);
    
    protected volatile ObjectCacheState activeState;
    private final AtomicReference<ObjectCacheState> backupState = new AtomicReference<ObjectCacheState>();
    
    private final Object writeLock = new Object();

    public ObjectSimpleLoadingCache(ObjectSimpleCacheLoader<K, V> loader, int maxSize)
    {
        this.capacity = 1 << (Long.SIZE - Long.numberOfLeadingZeros((long) (maxSize / LOAD_FACTOR)));
        this.maxFill = (int) (capacity * LOAD_FACTOR);
        this.positionMask = capacity * 2 - 1;
        this.loader = loader;
        this.activeState = new ObjectCacheState(this.capacity);
        this.clear();
    }

    public int size() { return activeState.size.get(); }
    
    public void clear()
    {
        this.activeState = new ObjectCacheState(this.capacity);
    }
    
    @SuppressWarnings("unchecked")
    public V get(@Nonnull K key)
    {
        if(key == null) return null;
        
        ObjectCacheState localState = activeState;
        
        int position = (key.hashCode() * 2) & positionMask;
        
        do
        {
            if(localState.kv[position] == null) return load(localState, key, position);

            if(localState.kv[position].equals(key)) return (V) localState.kv[position + 1];
            
            position = (position + 2) & positionMask;
            
        } while (true);
    }

    @SuppressWarnings("unchecked")
    protected V loadFromBackup(ObjectCacheState backup, final K key)
    {
        int position = (key.hashCode() * 2) & positionMask;
        do
        {
            if(backup.kv[position] == null)
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
            
            if(backup.kv[position].equals(key)) return (V) backup.kv[position + 1];
            
            position = (position + 2) & positionMask;
        } while(true);
    }
    
    
    @SuppressWarnings("unchecked")
    protected V load(ObjectCacheState localState, K key, int position)
    {        
        
        ObjectCacheState backupState = this.backupState.get();
        
        final V result = backupState == null ? loader.load(key) : loadFromBackup(backupState, key);
        
        do
        {
            Object currentKey;       
            synchronized(writeLock)
            {
                currentKey = localState.kv[position];
                if(currentKey == null)
                {
                    //write value start in case another thread tries to read it based on key before we can write it
                    localState.kv[position + 1] = result;
                    localState.kv[position] = key;
                    break;
                }
            }
            
            // small chance another thread added our value before we got our lock
            if(currentKey.equals(key)) return (V) localState.kv[position + 1];
            
            position = (position + 2) & positionMask;
            
        } while(true);
        
        if(localState.size.incrementAndGet() == this.maxFill)
        {
            ObjectCacheState newState = new ObjectCacheState(this.capacity);
            this.backupState.set(this.activeState);
            this.activeState = newState;
            this.backupMissCount.set(0);
        }
        return result;
    }
    
    public ObjectSimpleLoadingCache<K, V> createNew(ObjectSimpleCacheLoader<K, V> loader, int startingCapacity)
    {
        return new ObjectSimpleLoadingCache<K, V>(loader, startingCapacity);
    }
}