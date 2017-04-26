package grondag.adversity.library.cache.longKey;

import grondag.adversity.library.Useful;

//import java.util.concurrent.atomic.AtomicInteger;

public class LongSimpleLoadingCache<V> implements ILongLoadingCache<V>
{
    
    @Override
    public int size() { return state.size.get(); }
    
    private volatile LongCacheState<V> state;
    
    private Object[] writeLock = new Object[64];
    private Object zeroLock = new Object();
    
    private volatile LongSimpleCacheLoader<V> loader;
    
    private final static float LOAD_FACTOR = 0.75F;

    public LongSimpleLoadingCache(LongSimpleCacheLoader<V> loader, int startingCapacity)
    {
        for(int i = 0; i < writeLock.length; i++)
        {
            writeLock[i] = new Object();
        }
        if(startingCapacity < 64) startingCapacity = 64;
        this.loader = loader;
        this.state = new LongCacheState<V>();
        this.state.capacity = 1 << (Long.SIZE - Long.numberOfLeadingZeros((long) (startingCapacity / LOAD_FACTOR)));
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
    @SuppressWarnings("unchecked")
    public void clear()
    {
        //TODO
        synchronized(writeLock)
        {
            int capacity = this.state.capacity;
            LongCacheState<V> newState = new LongCacheState<V>();
            newState.zeroValue = null;
            newState.capacity = capacity;
            newState.keys = new long[capacity];
            newState.values = (V[]) new Object[capacity];
            newState.positionMask = capacity - 1;
            newState.size.set(0);
            newState.maxFill = (int) (capacity * LOAD_FACTOR);
            state = newState;
        }
    }
    
    @Override
    public V get(long key)
    {
        
        LongCacheState<V> localState = state;
        
        // Zero value normally indicates an unused spot in key array
        // so requires special handling to prevent search weirdness.
        if(key == 0)
        {
            if(localState.zeroValue == null)
            {
                V value = loader.load(key);
                synchronized(zeroLock)
                {
                    localState = state;
                    if(localState.zeroValue == null)
                    {
                        localState.zeroValue = value;
                    }
                }
            }
            return localState.zeroValue;
        }
        
        long keyHash = Useful.longHash(key);
        int position = (int) (keyHash & localState.positionMask);
        long currentKey = localState.keys[position];       
     
        if(currentKey == key) 
        {
            return localState.values[position];
        }
        
        if(currentKey == 0) return load(localState, key, keyHash);

        while (true) 
        {
            position = (position + 1) & localState.positionMask;
            currentKey = localState.keys[position];
            
            if(currentKey == 0) return load(localState, key, keyHash);
            
            if(currentKey == key)
            {
                return localState.values[position];
            }
        }
    }
    
    private V load(LongCacheState<V> localState, long key, long keyHash)
    {        
        // no need to handle zero key here - is handled as special case in get();
        int position = (int) (keyHash & localState.positionMask);
        long currentKey = localState.keys[position];       

        // small chance another thread added our value before we got our lock
        if(currentKey == key) return localState.values[position];

        if(currentKey == 0)
        {
            V result = loader.load(key);
            synchronized(writeLock[position & 63])
            {
                //Abort save if another thread took our spot or expanded array.
                //Will simply reload if necessary next time.
                if(state.keys[position] == 0 && state.positionMask == localState.positionMask)
                {
                    //write value first in case another thread tries to read it based on key before we can write it
                    localState.values[position] = result;
                    localState.keys[position] = key;
                    if(localState.size.incrementAndGet() == localState.maxFill) expand(); 
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
                synchronized(writeLock[position & 63])
                {
                    //Abort save if another thread took our spot or expanded array.
                    //Will simply reload if necessary next time.
                    if(state.keys[position] == 0 && state.positionMask == localState.positionMask)
                    {
                        //write value first in case another thread tries to read it based on key before we can write it
                        localState.values[position] = result;
                        localState.keys[position] = key;
                        if(localState.size.incrementAndGet() == localState.maxFill) expand(); 
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
        //TODO
        synchronized(writeLock)
        {
            int oldCapacity = state.capacity;
            int newCapacity = state.capacity << 1;
            LongCacheState<V> oldState = state;
            LongCacheState<V> newState = new LongCacheState<V>();
            newState.capacity = newCapacity;
            newState.maxFill = (int) (newCapacity * LOAD_FACTOR);
            int positionMask = newCapacity - 1;
            newState.positionMask = positionMask;
            newState.zeroValue = oldState.zeroValue;
            final long[] oldKeys = oldState.keys;
            final V[] oldValues = oldState.values;
            final long[] newKeys = new long[newCapacity];
            final V[] newValues = (V[]) new Object[newCapacity];
            int position;
            
            int newSize = 0;
            
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
                    newSize++;
                }
            }
    
                     newState.size.set(newSize);
            newState.keys = newKeys;
            newState.values = newValues;
            
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