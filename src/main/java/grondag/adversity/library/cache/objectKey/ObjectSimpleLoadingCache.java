package grondag.adversity.library.cache.objectKey;

public class ObjectSimpleLoadingCache<K, V> implements IObjectLoadingCache<K, V>
{

    private volatile int capacity;
    private volatile int maxFill;
    private volatile int size;
    
    public int getSize() { return size; }
    
    private volatile ObjectCacheState<K, V> state;
    
//    public AtomicInteger calls = new AtomicInteger(0);
//    public AtomicInteger hits = new AtomicInteger(0);
//    public AtomicInteger searchCount = new AtomicInteger(0);
    
    private Object writeLock = new Object();
    
    private volatile ObjectSimpleCacheLoader<K, V> loader;
    
    private final static float LOAD_FACTOR = 0.75F;

    public ObjectSimpleLoadingCache(ObjectSimpleCacheLoader<K, V> loader, int startingCapacity)
    {
        this.loader = loader;
        capacity = 1 << (Long.SIZE - Long.numberOfLeadingZeros((long) (startingCapacity / LOAD_FACTOR)) + 1);
        this.clear();
    }

    /** releases loader and afterwards returns null for any keys not found */
    public ObjectSimpleStaticCache<K, V> getStaticCache()
    {
        return new ObjectSimpleStaticCache<K, V>(this.state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void clear()
    {
        synchronized(writeLock)
        {
            ObjectCacheState<K, V> newState = new ObjectCacheState<K, V>();
            newState.keys = (K[]) new Object[capacity + 1];
            newState.values = (V[]) new Object[capacity + 1];
            newState.positionMask = capacity - 1;
            newState.nullLocation = capacity;
            state = newState;
            size = 0;
            maxFill = (int) (capacity * LOAD_FACTOR);
        }
    }
    
    @Override
    public V get(K key)
    {
//        calls.incrementAndGet();
        
        ObjectCacheState<K, V> localState = state;
        
        // Null value normally indicates an unused spot in key array
        // so requires special handling to prevent search weirdness.
        if(key == null)
        {
            if(localState.values[localState.nullLocation] == null)
            {
                synchronized(writeLock)
                {
                    localState = state;
                    if(localState.values[localState.nullLocation] == null)
                    {
                        localState.values[localState.nullLocation] = loader.load(key);
                    }
                }
            }
//            else
//            {
//                hits.incrementAndGet();
//            }
            return localState.values[localState.nullLocation];
        }
        
        int keyHash = key.hashCode();
        int position = keyHash & localState.positionMask;
        K currentKey = localState.keys[position];       
     
        if(currentKey == null) return load(key, keyHash);

        if(currentKey.equals(key)) 
        {
//            hits.incrementAndGet();
            return localState.values[position];
        }

        while (true) 
        {
//            searchCount.incrementAndGet();
            position = (position + 1) & localState.positionMask;
            currentKey = localState.keys[position];
            
            if(currentKey == null) return load(key, keyHash);
            
            if(currentKey.equals(key))
            {
//                hits.incrementAndGet();
                return localState.values[position];
            }
        }
    }
    
    private V load(K key, int keyHash)
    {
        ObjectCacheState<K, V> localState = state;
        
        // no need to handle null key here - is handled as special case in get();
        int position = keyHash & localState.positionMask;
        K currentKey = localState.keys[position];       

        if(currentKey == null)
        {
            V result = loader.load(key);
            synchronized(writeLock)
            {
                //Abort save if another thread took our spot or expanded array.
                //Will simply reload if necessary next time.
                if(state.keys[position] == null && state.positionMask == localState.positionMask)
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
        if(currentKey.equals(key)) return localState.values[position];
        
        while (true) 
        {
            position = (position + 1) & localState.positionMask;
            currentKey = localState.keys[position];
            
            if(currentKey == null)
            {
                V result = loader.load(key);
                synchronized(writeLock)
                {
                    //Abort save if another thread took our spot or expanded array.
                    //Will simply reload if necessary next time.
                    if(state.keys[position] == null && state.positionMask == localState.positionMask)
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
            if(currentKey.equals(key)) return localState.values[position];
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
            ObjectCacheState<K, V> oldState = state;
            ObjectCacheState<K, V> newState = new ObjectCacheState<K, V>();
            newState.positionMask = positionMask;
            newState.nullLocation = capacity;
            final K[] oldKeys = oldState.keys;
            final V[] oldValues = oldState.values;
            final K[] newKeys = (K[]) new Object[capacity + 1];
            final V[] newValues = (V[]) new Object[capacity + 1];
            int position;
            
            for(int i = oldCapacity; i-- != 0;)
            {
                if(oldKeys[i] != null)
                {
                    position = (int) (oldKeys[i].hashCode() & positionMask);
                    if(newKeys[position] != null)
                    {
                        while (!((newKeys[position = (position + 1) & positionMask]) == null));
                    }
                    newKeys[position] = oldKeys[i];
                    newValues[position] = oldValues[i];
                }
            }
    
            // transfer null key value (keep simple by doing even if null)
            newValues[capacity] = oldValues[oldCapacity];
            
            newState.keys = newKeys;
            newState.values = newValues;
            
            // make visible to readers
            state = newState;
        }
    }
    
}