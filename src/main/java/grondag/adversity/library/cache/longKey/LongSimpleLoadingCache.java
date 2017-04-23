package grondag.adversity.library.cache.longKey;

import grondag.adversity.library.Useful;

//import java.util.concurrent.atomic.AtomicInteger;

public class LongSimpleLoadingCache<V> implements ILongLoadingCache<V>
{

    private volatile int capacity;
    private volatile int maxFill;
    private volatile int size;
    
    public int getSize() { return size; }
    
    private volatile LongCacheState<V> state;
    
//    public AtomicInteger calls = new AtomicInteger(0);
//    public AtomicInteger hits = new AtomicInteger(0);
//    public AtomicInteger searchCount = new AtomicInteger(0);
    
    private Object writeLock = new Object();
    
    private volatile LongSimpleCacheLoader<V> loader;
    
    private final static float LOAD_FACTOR = 0.75F;

    public LongSimpleLoadingCache(LongSimpleCacheLoader<V> loader, int startingCapacity)
    {
        this.loader = loader;
        capacity = 1 << (Long.SIZE - Long.numberOfLeadingZeros((long) (startingCapacity / LOAD_FACTOR)) + 1);
        this.clear();
    }

    /** releases loader and afterwards returns null for any keys not found */
    public LongSimpleStaticCache<V> getStaticCache()
    {
        return new LongSimpleStaticCache<V>(this.state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void clear()
    {
        synchronized(writeLock)
        {
            LongCacheState<V> newState = new LongCacheState<V>();
            newState.keys = new long[capacity + 1];
            newState.values = (V[]) new Object[capacity + 1];
            newState.positionMask = capacity - 1;
            newState.zeroLocation = capacity;
            state = newState;
            size = 0;
            maxFill = (int) (capacity * LOAD_FACTOR);
        }
    }
    
    @Override
    public V get(long key)
    {
//        calls.incrementAndGet();
        
        LongCacheState<V> localState = state;
        
        // Zero value normally indicates an unused spot in key array
        // so requires special handling to prevent search weirdness.
        if(key == 0)
        {
            if(localState.values[localState.zeroLocation] == null)
            {
                V value = loader.load(key);
                synchronized(writeLock)
                {
                    localState = state;
                    if(localState.values[localState.zeroLocation] == null)
                    {
                        localState.values[localState.zeroLocation] = value;
                    }
                }
            }
//            else
//            {
//                hits.incrementAndGet();
//            }
            return localState.values[localState.zeroLocation];
        }
        
        long keyHash = Useful.longHash(key);
        int position = (int) (keyHash & localState.positionMask);
        long currentKey = localState.keys[position];       
     
        if(currentKey == key) 
        {
//            hits.incrementAndGet();
            return localState.values[position];
        }
        
        if(currentKey == 0) return load(key, keyHash);

        while (true) 
        {
//            searchCount.incrementAndGet();
            position = (position + 1) & localState.positionMask;
            currentKey = localState.keys[position];
            
            if(currentKey == 0) return load(key, keyHash);
            
            if(currentKey == key)
            {
//                hits.incrementAndGet();
                return localState.values[position];
            }
        }
    }
    
    private V load(long key, long keyHash)
    {
        LongCacheState<V> localState = state;
        
        // no need to handle zero key here - is handled as special case in get();
        int position = (int) (keyHash & localState.positionMask);
        long currentKey = localState.keys[position];       

        // small chance another thread added our value before we got our lock
        if(currentKey == key) return localState.values[position];

        if(currentKey == 0)
        {
            V result = loader.load(key);
            synchronized(writeLock)
            {
                //Abort save if another thread took our spot or expanded array.
                //Will simply reload if necessary next time.
                if(state.keys[position] == 0 && state.positionMask == localState.positionMask)
                {
                    //write value first in case another thread tries to read it based on key before we can write it
                    localState.values[position] = result;
                    localState.keys[position] = key;
                    if(++size >= maxFill) expand(); 
                }
            }
            return result;
        }

        while (true) 
        {
            position = (position + 1) & localState.positionMask;
            currentKey = localState.keys[position];
            
            if(currentKey == 0)
            {
                V result = loader.load(key);
                synchronized(writeLock)
                {
                    //Abort save if another thread took our spot or expanded array.
                    //Will simply reload if necessary next time.
                    if(state.keys[position] == 0 && state.positionMask == localState.positionMask)
                    {
                        //write value first in case another thread tries to read it based on key before we can write it
                        localState.values[position] = result;
                        localState.keys[position] = key;
                        if(++size >= maxFill) expand(); 
                    }
                }
                return result;
            }
            
            // small chance another thread added our value before we got our lock
            if(currentKey == key) return localState.values[position];
        }
        
    }
    
    @SuppressWarnings("unchecked")
    private void expand() 
    {
        synchronized(writeLock)
        {
            int oldCapacity = capacity;
            capacity = capacity << 1;
            maxFill = (int) (capacity * LOAD_FACTOR);
            int positionMask = capacity - 1;
            LongCacheState<V> oldState = state;
            LongCacheState<V> newState = new LongCacheState<V>();
            newState.positionMask = positionMask;
            newState.zeroLocation = capacity;
            final long[] oldKeys = oldState.keys;
            final V[] oldValues = oldState.values;
            final long[] newKeys = new long[capacity + 1];
            final V[] newValues = (V[]) new Object[capacity + 1];
            int position;
            
            for(int i = oldCapacity; i-- != 0;)
            {
                if(oldKeys[i] != 0)
                {
                    position = (int) (Useful.longHash(oldKeys[i]) & positionMask);
                    if(newKeys[position] != 0)
                    {
                        while (!((newKeys[position = (position + 1) & positionMask]) == (0)));
                    }
                    newKeys[position] = oldKeys[i];
                    newValues[position] = oldValues[i];
                }
            }
    
            // transfer zero key value (keep simple by doing even if null)
            newValues[capacity] = oldValues[oldCapacity];
            
            newState.keys = newKeys;
            newState.values = newValues;
            
            // make visible to readers
            state = newState;
        }
    }
    
}
