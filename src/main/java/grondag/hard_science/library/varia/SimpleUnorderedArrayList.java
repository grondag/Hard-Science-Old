package grondag.hard_science.library.varia;

import java.util.Arrays;

import grondag.hard_science.Log;

/**
 * Lightweight, non-concurrent collection-like class for managing small unordered lists.
 * @author grondag
 *
 */
public class SimpleUnorderedArrayList<T>
{

    private Object[] items = new Object[4];
    
    private int size = 0;
    
    public int size()
    {
        return this.size;
    }
    
    public boolean isEmpty()
    {
        return this.size == 0;
    }
    
    public void add(T newItem)
    {
        if(this.size == this.items.length)
        {
            this.increaseCapacity();
        }
        this.items[size++] = newItem;
    }
    
    public void addIfNotPresent(T newItem)
    {
        for(int i = this.size - 1; i >= 0; i--)
        {
            if(items[i] == newItem) return;
        }
        this.add(newItem);
    }
    
    /** returns index of item if it exists in this list. -1 if not. */
    public int findIndex(T itemToFind)
    {
        for(int i = this.size - 1; i >= 0; i--)
        {
            if(items[i] == itemToFind) return i;
        }
        
        return -1;
    }
    
    @SuppressWarnings("unchecked")
    public T get(int index)
    {
        return (T) this.items[index];
    }
    
    /** Does NOT preserve order! */
    public void remove(int index)
    {
        if(Log.DEBUG_MODE && this.isEmpty())
        {
            Log.warn("SimpleUnoderedArrayList detected attempt to remove item from empty list.");
        }
        
        this.size--;
        if(index < size)
        {
            this.items[index] = this.items[size];
        }
        this.items[size] = null;
    }
    
    public void removeIfPresent(T itemToRemove)
    {
        for(int i = this.size - 1; i >= 0; i--)
        {
            if(items[i] == itemToRemove)
            {
                this.remove(i);
                return;
            }
        }
    }
    
    public boolean contains(T itemToFind)
    {
        for(int i = this.size - 1; i >= 0; i--)
        {
            if(items[i] == itemToFind) return true;
        }
        return false;
    }
    
    public void clear()
    {
        if(this.size == 0) return;
        
        for(int i = this.size - 1; i >= 0; i--)
        {
            items[i] = null;
        }
        this.size = 0;
    }
    
    private void increaseCapacity()
    {
        int newCapacity = this.items.length * 2;
        this.items = Arrays.copyOf(this.items, newCapacity);
    }
    
    /**
     * Returns a copy of the underlying array for populated elements.
     */
    public Object[] toArray()
    {
        return Arrays.copyOf(items, this.size);
    }
}
