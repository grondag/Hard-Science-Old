//package grondag.adversity.library.cache.longKey;
//
//import java.util.concurrent.atomic.AtomicLongArray;
//import java.util.concurrent.atomic.AtomicReferenceArray;
//
//import grondag.adversity.Adversity;
//import grondag.adversity.library.Useful;
//
///**
// * Experimental lock-free version.
// * Not as fast as locking version unfortunately because of overhead of address offset computation in AtomicLongArray 
// * @author grondag
// *
// * @param <V>
// */
//public class AtomicLongSimpleLoadingCache<V> implements ILongLoadingCache<V>
//{
//    @Override
//    public int size() { return state.size.get(); }
//    
//    private volatile AtomicLongCacheState<V> state;
//    
//    private final LongSimpleCacheLoader<V> loader;
//    
//    private final static float LOAD_FACTOR = 0.75F;
//
//    public AtomicLongSimpleLoadingCache(LongSimpleCacheLoader<V> loader, int startingCapacity)
//    {
//        this.loader = loader;
//        this.state = new AtomicLongCacheState<V>();
//        this.state.capacity = 1 << (Long.SIZE - Long.numberOfLeadingZeros((long) (startingCapacity / LOAD_FACTOR)) + 1);
//        this.clear();
//    }
//
//    /** 
//     * Read-only wrapper for our state that returns null for any keys not found.
//     * Wrapper *will* reflect any subsequent changes made to our state.
//     * Generally intended for use when going to release reference to this instance.
//     */
//    @Override
//    public ILongLoadingCache<V> getStaticCache()
//    {
//        return new AtomicLongSimpleStaticCache<V>(this.state);
//    }
//
//    @Override
//    public LongSimpleCacheLoader<V> getLoader() { return this.loader; }
//    
//    /**
//     * Not designed to be thread-safe.  Generally not used in concurrent context.
//     */
//    @Override
//    public void clear()
//    {
//        int capacity = this.state.capacity;
//        AtomicLongCacheState<V> newState = new AtomicLongCacheState<V>();
//        newState.capacity = capacity;
//        newState.keys = new AtomicLongArray(capacity + 1);
//        newState.values = new AtomicReferenceArray<V>(capacity + 1);
//        newState.positionMask = capacity - 1;
//        newState.zeroLocation = capacity;
//        newState.size.set(0);
//        newState.maxFill = (int) (capacity * LOAD_FACTOR);
//        state = newState;
//    }
//    
//    @Override
//    public V get(long key)
//    {        
//        AtomicLongCacheState<V> localState = state;
//        
//        // Zero value normally indicates an unused spot in key array
//        // so requires special handling to prevent search weirdness.
//        if(key == 0)
//        {
//            int zeroLocation = localState.zeroLocation;
//            V value = localState.values.get(zeroLocation);
//            
//            if(value == null)
//            {
//                value = loader.load(key);
//                if(!localState.values.compareAndSet(zeroLocation, null, value))
//                {
//                    // another thread got there first
//                    value = localState.values.get(zeroLocation);
//                }
//            }
//            
//            return value;
//        }
//        
//        int position = (int) (Useful.longHash(key) & localState.positionMask);
//        
//        
//        long keyAtPosition = localState.keys.get(position);
//        if(keyAtPosition == key) 
//            return returnValueSafely(localState, key, position);
//
//        V value = null;
//        
//        if(keyAtPosition == 0)
//        {
//            value = loader.load(key);
//            if(localState.keys.compareAndSet(position, 0, key)) 
//                return loadAndReturnValueAtPosition(localState, position, value);
//        }
//
//        while (true) 
//        {
//            position = (position + 1) & localState.positionMask;
//            
//            keyAtPosition = localState.keys.get(position);
//            if(keyAtPosition == key) 
//                return returnValueSafely(localState, key, position);
//
//            if(keyAtPosition == 0)
//            {
//                if(value == null) value = loader.load(key);
//                if(localState.keys.compareAndSet(position, 0, key)) 
//                    return loadAndReturnValueAtPosition(localState, position, value);
//            }
//        }
//    }
//    
//    private V returnValueSafely(AtomicLongCacheState<V> localState, long key, int position)
//    {
//        V result = localState.values.get(position);
//        if(result != null) return result;
//
//        // Another thread has updated key but hasn't yet updated the value.
//        // Should be very rare.  Retry several times until value appears.
//
//        result = localState.values.get(position);
//        if(result != null) return result;
//        
//        result = localState.values.get(position);
//        if(result != null) return result;
//        
//        result = localState.values.get(position);
//        if(result != null) return result;
//        
//        result = localState.values.get(position);
//        if(result != null) return result;
//        
//        result = localState.values.get(position);
//        if(result != null) return result;
//        
//        result = localState.values.get(position);
//        if(result != null) return result;
//        
//        result = localState.values.get(position);
//        if(result != null) return result;
//        
//        result = localState.values.get(position);
//        if(result != null) return result;
//        
//        result = localState.values.get(position);
//        if(result != null) return result;
//        
//        result = localState.values.get(position);
//        if(result != null) return result;
//        
//        result = localState.values.get(position);
//        if(result != null) return result;
//        
//        if(Adversity.DEBUG_MODE)
//            System.out.println("LongSimpleLoadingCache: returning new loaded value despite key hit because cached value not yet written by other thread.");
//        
//        // abort and return loaded value directly
//        return loader.load(key);
//    }
//    
//    private V loadAndReturnValueAtPosition(AtomicLongCacheState<V> localState, int position, V value)
//    {
//                
//        if(localState.values.compareAndSet(position, null, value))
//        {
//            // NB: Using == operator instead of >= ensures will be called exactly once
//            if(localState.size.incrementAndGet() == localState.maxFill) expand(); 
//            return value;
//        }
//        else
//        {
//            // We have the right key, but somehow another thread wrote the value first
//            // Probably can't happen because other thread would not have been able 
//            // to write they key value successfully, but handle it just in case.
//            if(Adversity.DEBUG_MODE) 
//                System.out.println("LongSimpleLoadingCache: Another thread wrote value other than thread that populated the key. Should never happen. Returning found value.");
//            
//            return localState.values.get(position);
//        }
//    }
//    
//    private void expand() 
//    {
//        AtomicLongCacheState<V> oldState = state;
//        AtomicLongCacheState<V> newState = new AtomicLongCacheState<V>();
//        int newCapacity = oldState.capacity << 1;
//        newState.capacity = newCapacity;
//        newState.maxFill = (int) (newCapacity * LOAD_FACTOR);
//        int positionMask = newCapacity - 1;
//        newState.positionMask = positionMask;
//        newState.zeroLocation = newCapacity;
//        final AtomicLongArray oldKeys = oldState.keys;
//        final AtomicReferenceArray<V> oldValues = oldState.values;
//        final AtomicLongArray newKeys = new AtomicLongArray(newCapacity + 1);
//        final AtomicReferenceArray<V> newValues = new AtomicReferenceArray<V>(newCapacity + 1);
//        int position;
//        
//        int newSize = 0;
//        
//        for(int i = oldState.capacity; i-- != 0;)
//        {
//            long oldKey = oldKeys.get(i);
//            if(oldKey != 0)
//            {
//                position = (int) (Useful.longHash(oldKey) & positionMask);
//                while(!newKeys.compareAndSet(position, 0, oldKey))
//                {
//                    position = (position + 1) & positionMask;
//                }
//                newValues.set(position, oldValues.get(i));
//                newSize++;
//            }
//        }
//
//        // transfer zero key value
//        if(oldValues.get(oldState.capacity) != null)
//        {
//            newValues.set(newCapacity, oldValues.get(oldState.capacity));
//            newSize++;
//        }
//        
//        newState.size.set(newSize);
//        newState.keys = newKeys;
//        newState.values = newValues;
//        
//        // make visible to readers
//        state = newState;
//    }
//
//    @Override
//    public ILongLoadingCache<V> createNew(LongSimpleCacheLoader<V> loader, int startingCapacity)
//    {
//        return new AtomicLongSimpleLoadingCache<V>(loader, startingCapacity);
//    }
//    
//}
