package grondag.hard_science.library.varia;

/**
 * Lightweight, non-concurrent collection-like class for managing small unordered lists.
 * Uses = for comparison.
 * @author grondag
 *
 */
public class SimpleUnorderedArrayList<T> extends AbstractUnorderedArrayList<T>
{
    @Override
    public void add(T newItem)
    {
        super.add(newItem);
    }
    
    /**
     * Returns true if was added (not already present)
     */
    public boolean addIfNotPresent(T newItem)
    {
        for(int i = this.size - 1; i >= 0; i--)
        {
            if(items[i] == newItem) return false;
        }
        this.add(newItem);
        return true;
    }
    
    public void removeIfPresent(T target)
    {
        super.remove(target);
    }

}
