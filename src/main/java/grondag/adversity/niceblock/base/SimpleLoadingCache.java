package grondag.adversity.niceblock.base;

//import java.util.concurrent.atomic.AtomicInteger;

public class SimpleLoadingCache<V>
{
    private class CacheState
    {
        private long[] keys;
        private V[] values;
        private int positionMask;
        private int zeroLocation;
    }
    private volatile int capacity;
    private volatile int maxFill;
    private volatile int size;
    
    private volatile CacheState state;
    
//    public AtomicInteger calls = new AtomicInteger(0);
//    public AtomicInteger hits = new AtomicInteger(0);
//    public AtomicInteger searchCount = new AtomicInteger(0);
    
    private Object writeLock = new Object();
    
    private final SimpleCacheLoader<V> loader;
    
    private final static float LOAD_FACTOR = 0.75F;

    public SimpleLoadingCache(SimpleCacheLoader<V> loader, int startingCapacity)
    {
        this.loader = loader;
        capacity = 1 << (Long.SIZE - Long.numberOfLeadingZeros((long) (startingCapacity / LOAD_FACTOR)) + 1);
        this.clear();
    }
    
    @SuppressWarnings("unchecked")
    public void clear()
    {
        synchronized(writeLock)
        {
            CacheState newState = new CacheState();
            newState.keys = new long[capacity + 1];
            newState.values = (V[]) new Object[capacity + 1];
            newState.positionMask = capacity - 1;
            newState.zeroLocation = capacity;
            state = newState;
            size = 0;
            maxFill = (int) (capacity * LOAD_FACTOR);
        }
    }
    
    public V get(long key)
    {
//        calls.incrementAndGet();
        
        CacheState localState = state;
        
        // Zero value normally indicates an unused spot in key array
        // so requires special handling to prevent search weirdness.
        if(key == 0)
        {
            if(localState.values[localState.zeroLocation] == null)
            {
                synchronized(writeLock)
                {
                    localState = state;
                    if(localState.values[localState.zeroLocation] == null)
                    {
                        localState.values[localState.zeroLocation] = loader.load(key);
                    }
                }
            }
//            else
//            {
//                hits.incrementAndGet();
//            }
            return localState.values[localState.zeroLocation];
        }
        
        long keyHash = mix(key);
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
        CacheState localState = state;
        
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
                    localState.keys[position] = key;
                    localState.values[position] = result;
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
                        localState.keys[position] = key;
                        localState.values[position] = result;
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
            CacheState oldState = state;
            CacheState newState = new CacheState();
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
                    position = (int) (mix(oldKeys[i]) & positionMask);
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
    
    /** see http://brpreiss.com/books/opus4/html/page214.html */
    private static long mix(long l) 
    {
        // constant is golden ratio
        long h = l * 0x9E3779B97F4A7C15L;
        h ^= h >>> 32;
        return h ^ (h >>> 16);
    }
    

}
