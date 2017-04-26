package grondag.adversity.library.cache.longKey;

import grondag.adversity.library.Useful;

//import java.util.concurrent.atomic.AtomicInteger;

public class LongSimpleLoadingCache<V> implements ILongLoadingCache<V>
{
    
    @Override
    public int size() { return state.size.get(); }
    
    private volatile LongCacheState<V> state;
    
    private Object[] writeLock = new Object[64];
    
    private volatile LongSimpleCacheLoader<V> loader;
    
    final static float LOAD_FACTOR = 0.75F;

    public LongSimpleLoadingCache(LongSimpleCacheLoader<V> loader, int startingCapacity)
    {
        for(int i = 0; i < writeLock.length; i++)
        {
            writeLock[i] = new Object();
        }
        if(startingCapacity < 64) startingCapacity = 64;
        this.loader = loader;
        this.state = new LongCacheState<V>(1 << (Long.SIZE - Long.numberOfLeadingZeros((long) (startingCapacity / LOAD_FACTOR))));
        this.clear();
    }

    /** releases loader and afterwards returns null for any keys not found */
    public ILongLoadingCache<V> getStaticCache()
    {
        return new LongSimpleStaticCache<V>(this.state);
    }

    @Override
    public LongSimpleCacheLoader<V> getLoader() { return this.loader; }
    
    @Override
    public void clear()
    {
        this.state = new LongCacheState<V>(this.state.capacity);
    }
    
    @Override
    public V get(long key)
    {
        LongCacheState<V> localState = state;
        
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
        
        long keyHash = Useful.longHash(key);
        int position = (int) (keyHash & localState.positionMask);
        long currentKey = localState.keys[position >> localState.segmentShift][position & localState.indexMask];       
     
        if(currentKey == key) 
        {
            return localState.values[position >> localState.segmentShift][position & localState.indexMask];
        }
        
        if(currentKey == 0) return load(localState, key, position);

        while (true) 
        {
            position = (position + 1) & localState.positionMask;
            currentKey = localState.keys[position >> localState.segmentShift][position & localState.indexMask];
            
            if(currentKey == 0) return load(localState, key, position);
            
            if(currentKey == key)
            {
                return localState.values[position >> localState.segmentShift][position & localState.indexMask];
            }
        }
    }
    
    private V load(LongCacheState<V> localState, long key, int position)
    {        
        // no need to handle zero key here - is handled as special case in get();
        final V result = loader.load(key);
        int segment = position >> localState.segmentShift;
        long[] keySegment = localState.keys[segment];
        V[] valueSegment = localState.values[segment];
        int index = position & localState.indexMask;
        long currentKey;       
        
        synchronized(keySegment)
        {
            currentKey = keySegment[index];
            if(currentKey == 0)
            {
                //write value first in case another thread tries to read it based on key before we can write it
                valueSegment[index] = result;
                keySegment[index] = key;
            }
        }
        if(currentKey == 0)
        {
            if(localState.size.incrementAndGet() == localState.maxFill) expand(); 
            return result;
        }
        // small chance another thread added our value before we got our lock
        else if(currentKey == key) return valueSegment[index];
        
        while (true) 
        {
            position = (position + 1) & localState.positionMask;
            if(segment != position >> localState.segmentShift)
            {
                segment = position >> localState.segmentShift;
                keySegment = localState.keys[segment];
                valueSegment = localState.values[segment];
            }
            index = position & localState.indexMask;  
            
            synchronized(keySegment)
            {
                currentKey = keySegment[index];       
                if(currentKey == 0)
                {
                    //write value first in case another thread tries to read it based on key before we can write it
                    valueSegment[index] = result;
                    keySegment[index] = key;
                }
            }
            if(currentKey == 0)
            {
                if(localState.size.incrementAndGet() == localState.maxFill) expand(); 
                return result;
            }
            // small chance another thread added our value before we got our lock
            else if(currentKey == key) return valueSegment[index];
        }
        
    }
    
    private void expand() 
    {
        //TODO
        synchronized(this)
        {
            int oldCapacity = state.capacity;
            int newCapacity = state.capacity << 1;
            LongCacheState<V> oldState = state;
            LongCacheState<V> newState = new LongCacheState<V>(newCapacity);
            int positionMask = newState.positionMask;
            newState.zeroValue.set(oldState.zeroValue.get());
            final long[][] oldKeys = oldState.keys;
            final V[][] oldValues = oldState.values;
            final long[][] newKeys = newState.keys;
            final V[][] newValues = newState.values;
            
            int newSize = 0;
           
            for(int i = 0; i < 64; i++)
            {
                for(int j = oldCapacity >> 5; j-- != 0;)
                {
                    if(oldKeys[i][j] != 0)
                    {
                        int position = (int) (Useful.longHash(oldKeys[i][j]) & positionMask);
                        if(newKeys[position >> newState.segmentShift][position & newState.indexMask] != 0)
                        {
                            do
                            {
                                position = (position + 1) & positionMask;
                            }
                            while (newKeys[position >> newState.segmentShift][position & newState.indexMask] != 0);
                        }
                        newKeys[position >> newState.segmentShift][position & newState.indexMask]= oldKeys[i][j];
                        newValues[position >> newState.segmentShift][position & newState.indexMask] = oldValues[i][j];
                        newSize++;
                    }
                }
            }
             newState.size.set(newSize);
            
            // make visible to readers
            state = newState;
        }
    }
    
    @Override
    public ILongLoadingCache<V> createNew(LongSimpleCacheLoader<V> loader, int startingCapacity)
    {
        return new LongSimpleLoadingCache<V>(loader, startingCapacity);
    }
}