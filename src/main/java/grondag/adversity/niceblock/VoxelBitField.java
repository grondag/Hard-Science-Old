package grondag.adversity.niceblock;

import java.util.BitSet;


public class VoxelBitField
{
    private final int power;
    
    private final BitSet bits;
    
    /**
     * Power is how many bits per dimension.
     * Equates to powers of 2.
     */
    public VoxelBitField(int power)
    {
        this.power = power;
        this.bits = new BitSet(1 << (power * 3));
    }
    
    public int getBitsPerAxis()
    {
        return 1 << power;
    }
    
    private int getAddress(int x, int y, int z)
    {
        return x | (y << power) | (z << (power * 2));
    }
    
    public void setFilled(int x, int y, int z, boolean isFilled)
    {
        bits.set(getAddress(x, y, z), isFilled);
    }
    
    /** Sets bits in bounding box.Inclusive of bounds. */
    public void setFilled(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean isFilled)
    {
        for(int z = minZ; z <= maxZ; z++)
        {
            for(int y = minY; y <= maxY; y++)
            {
                // + 1 because BitSet addressing not inclusive
                bits.set(getAddress(minX, y, z), getAddress(maxX, y, z) + 1, isFilled);
            }
        }
    }
    
    public boolean isFilled(int x, int y, int z)
    {
        return bits.get(getAddress(x, y, z));
    }
    
    /**
     * Returns true if all bits in bounding box are filled.
     */
    public boolean isFilled(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
    {
        boolean retVal=true;
        for(int z = minZ; z <= maxZ; z++)
        {
            for(int y = minY; y <= maxY; y++)
            {
//                for(int x = minX; x <= maxX; x++)
//                {
//                    retVal = retVal && bits.get(getAddress(x, y, z));
//                }
                // + 1 because BitSet addressing not inclusive
                retVal = retVal && bits.get(getAddress(minX, y, z), getAddress(maxX, y, z) + 1).cardinality() == (maxX - minX + 1);
            }
        }
        return retVal;
    }
    

    
}
