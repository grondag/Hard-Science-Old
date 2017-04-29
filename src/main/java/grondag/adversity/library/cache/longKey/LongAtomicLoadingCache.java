package grondag.adversity.library.cache.longKey;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import grondag.adversity.Adversity;
import static grondag.adversity.library.Danger.*;
import grondag.adversity.library.Useful;
import grondag.adversity.library.cache.SimpleLoadingCache;
import sun.misc.Unsafe;

@SuppressWarnings("unused")
public class LongAtomicLoadingCache<V>
{
    private final int capacity;
    private final int maxFill;
    protected final int positionMask;
    
    protected final LongSimpleCacheLoader<V> loader;
    
    private final AtomicInteger backupMissCount = new AtomicInteger(0);
    
    protected volatile LongCacheState<V> activeState;
    private final AtomicReference<LongCacheState<V>> backupState = new AtomicReference<LongCacheState<V>>();
     

    public LongAtomicLoadingCache(LongSimpleCacheLoader<V> loader, int maxSize)
    {
        this.capacity = 1 << (Long.SIZE - Long.numberOfLeadingZeros((long) (maxSize / SimpleLoadingCache.LOAD_FACTOR)));
        this.maxFill = (int) (capacity * SimpleLoadingCache.LOAD_FACTOR);
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
        long offset = longByteOffset(position);
        do
        {
            long currentKey = UNSAFE.getLongVolatile(localState.keys, offset);
            
            if(currentKey == key) return getValueEventually(localState, position, key);  
            
            if(currentKey == 0) return load(localState, key, position);
            
            position = (position + 1) & positionMask;
            offset = longByteOffset(position);
            
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
        long offset = longByteOffset(position);
        
        do
        {
            if(UNSAFE.compareAndSwapLong(localState.keys, offset, 0, key))
            {
                UNSAFE.putObjectVolatile(localState.values, objectByteOffset(position), result);
                break;
            }
            
            // small chance another thread added our value before we got our lock
            if(UNSAFE.getLongVolatile(localState.keys, offset) == key) return getValueEventually(localState, position, key);            
            position = (position + 1) & positionMask;
            offset = longByteOffset(position);
            
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
    
    @SuppressWarnings("unchecked")
    private V getValueEventually(LongCacheState<V> localState, int position, long key)
    {
        long offset = objectByteOffset(position);
        
        V result = (V) UNSAFE.getObjectVolatile(localState.values, offset);
        if(result != null) return result;

        // Another thread has updated key but hasn't yet updated the value.
        // Should be very rare.  Retry several times until value appears.

        result = (V) UNSAFE.getObjectVolatile(localState.values, offset);
        if(result != null) return result;
        
        result = (V) UNSAFE.getObjectVolatile(localState.values, offset);
        if(result != null) return result;
        
        result = (V) UNSAFE.getObjectVolatile(localState.values, offset);
        if(result != null) return result;
        
        result = (V) UNSAFE.getObjectVolatile(localState.values, offset);
        if(result != null) return result;
        
        result = (V) UNSAFE.getObjectVolatile(localState.values, offset);
        if(result != null) return result;
        
        result = (V) UNSAFE.getObjectVolatile(localState.values, offset);
        if(result != null) return result;
        
        result = (V) UNSAFE.getObjectVolatile(localState.values, offset);
        if(result != null) return result;
        
        result = (V) UNSAFE.getObjectVolatile(localState.values, offset);
        if(result != null) return result;
        
        result = (V) UNSAFE.getObjectVolatile(localState.values, offset);
        if(result != null) return result;
        
        result = (V) UNSAFE.getObjectVolatile(localState.values, offset);
        if(result != null) return result;
        
        result = (V) UNSAFE.getObjectVolatile(localState.values, offset);
        if(result != null) return result;
        
        if(Adversity.DEBUG_MODE)
            System.out.println("LongSimpleLoadingCache: returning new loaded value despite key hit because cached value not yet written by other thread.");
        
        // abort and return loaded value directly
        return loader.load(key);
    }
    
    public LongAtomicLoadingCache<V> createNew(LongSimpleCacheLoader<V> loader, int startingCapacity)
    {
        return new LongAtomicLoadingCache<V>(loader, startingCapacity);
    }
    
    /**
     * Identical to parent except avoids check for zero-valued keys.
     * Used in compound loader for sub caches that will never see the zero key.
     * @author grondag
     *
     * @param <V>
     */
    public static class NonZeroLongSimpleLoadingCache<V> extends LongAtomicLoadingCache<V>
    {

        public NonZeroLongSimpleLoadingCache(LongSimpleCacheLoader<V> loader, int maxSize)
        {
            super(loader, maxSize);
        }
        
        @Override
        public V get(long key)
        {
            LongCacheState<V> localState = activeState;
            
            int position = (int) (Useful.longHash(key) & positionMask);
            
            do
            {
                if(localState.keys[position] == key) return localState.values[position];
                
                if(localState.keys[position] == 0) return load(localState, key, position);
                
                position = (position + 1) & positionMask;
                
            } while (true);
        }
    }

}