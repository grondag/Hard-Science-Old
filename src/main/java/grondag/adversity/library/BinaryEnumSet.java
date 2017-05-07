package grondag.adversity.library;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for fast mapping of a enum to boolean values
 * serialized to a numeric primitive. 
 *  
 * @author grondag
 *
 */
public class BinaryEnumSet<T extends Enum<?>>
{
    private T[] values;
    
    public BinaryEnumSet(Class<T> e)
    {
        this.values = e.getEnumConstants();
    }
    
    /**
     * Number of distinct values for flag values produced and consumed by this instance.
     * Derivation is trivially simple. Main use is for clarity.
     */
    public int combinationCount()
    {
        return 2 << (values.length - 1);
    }
    
    public int getFlagsForIncludedValues(@SuppressWarnings("unchecked") T... included)
    {
        int result = 0;
        for(T e : included)
        {
            result |= (1 << e.ordinal());
        }
        return result;
    }
    
    public int getFlagsForIncludedValues(T v0, T v1, T v2, T v3)
    {
          return (1 << v0.ordinal()) | (1 << v1.ordinal()) | (1 << v2.ordinal()) | (1 << v3.ordinal());
    }
    
    public int getFlagsForIncludedValues(T v0, T v1, T v2)
    {
          return (1 << v0.ordinal()) | (1 << v1.ordinal()) | (1 << v2.ordinal());
    }
    
    public int getFlagsForIncludedValues(T v0, T v1)
    {
          return (1 << v0.ordinal()) | (1 << v1.ordinal());
    }
    
    public int getFlagForValue(T v0)
    {
          return (1 << v0.ordinal());
    }
    
    public int setFlagForValue(T v, int flagsIn, boolean isSet)
    {
        if(isSet)
        {
            return flagsIn | (1 << v.ordinal());
        }
        else
        {
            return flagsIn & ~(1 << v.ordinal());
        }
    }
    
    public boolean isFlagSetForValue(T v, int flagsIn)
    {
        return (flagsIn & (1 << v.ordinal())) != 0;
    }
    
    public List<T> getValuesForSetFlags(int flagsIn)
    {
        List<T> result = new ArrayList<T>(values.length);
        
        final int bitCount = Useful.bitLength(flagsIn);
        for(int i = 0; i < bitCount; i++)
        {
            if((flagsIn & (1 << i)) != 0)
            {
                result.add(values[i]);
            }
        }
        
        return result;
    }
}
