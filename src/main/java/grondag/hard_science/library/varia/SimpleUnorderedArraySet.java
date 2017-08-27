package grondag.hard_science.library.varia;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Version of SimpleUnorderedArrayList that uses equals instead of = for search.
 * Does not support null values.
 */
public class SimpleUnorderedArraySet<T> extends AbstractUnorderedArrayList<T>
{
 
    /**
     * Will replace matching value if exists.
     * Returns existing value that was replaced if found, null otherwise.
     */
    @Nullable
    public T put(@Nonnull T newItem)
    {
        for(int i = this.size - 1; i >= 0; i--)
        {
            if(this.items[i].equals(newItem))
            {
                @SuppressWarnings("unchecked")
                T result = (T) this.items[i];
                this.items[i] = newItem;
                return result;
            }
        }
        super.add(newItem);
        return null;
    }

    /**
     * This will not update existing values.  
     * Use {@link #put(Object)} if you want that behavior.
     * Returns existing value if found, null otherwise.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public T putIfNotPresent(@Nonnull T newItem)
    {
        for(int i = this.size - 1; i >= 0; i--)
        {
            if(this.items[i].equals(newItem)) return (T) this.items[i];
        }
        super.add(newItem);
        return null;
    }
    
    
    /**
     * Returns item that equals the given object, if found. Null if not found.
     */
    @SuppressWarnings("unchecked")
    public T get(T itemToFind)
    {
        for(int i = this.size - 1; i >= 0; i--)
        {
            if(items[i].equals(itemToFind)) return (T) items[i];
        }
        
        return null;
    }
    
    @Override
    public int findIndex(T itemToFind)
    {
        for(int i = this.size - 1; i >= 0; i--)
        {
            if(items[i].equals(itemToFind)) return i;
        }
        
        return -1;
    }
   
    /**
     * Returns item that was removed if found, null if nothing found.
     */
    @Nullable
    public T removeIfPresent(T itemToRemove)
    {
        for(int i = this.size - 1; i >= 0; i--)
        {
            if(items[i].equals(itemToRemove))
            {
                @SuppressWarnings("unchecked")
                T result = (T) this.items[i];
                this.remove(i);
                return result;
            }
        }
        return null;
    }
    
    @Override
    public boolean contains(T itemToFind)
    {
        for(int i = this.size - 1; i >= 0; i--)
        {
            if(items[i].equals(itemToFind)) return true;
        }
        return false;
    }
}
