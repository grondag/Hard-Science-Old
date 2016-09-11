package grondag.adversity.library;

import net.minecraft.util.math.BlockPos;

/**
 * Stores XYZ offsets in a 32-bit integer that also acts as a hash.
 * Uses 8 bits for Y and 12 bits each for X & Z.
 * Y values are stored as an absolute coordinate (not an offset)
 * with values from 0 to 255.  (This covers all possible values, no offset needed.)
 * 
 * X and Z values range from -2047 to +2047.
 * 
 * 
 * need to find by
 * lowest
 * closest
 */
public class RelativeBlockPos
{
    public static int getKey(BlockPos pos, BlockPos origin)
    {
        BlockPos offset = pos.subtract(origin);
        int x  = (offset.getX() + 2047) & 0xFFF;
        int z = (offset.getZ() + 2047) & 0xFFF;
        int y = offset.getY() & 0xFF;
        return x | y << 12 | z << 20;
    }
    
    public static BlockPos getPos(int key, BlockPos origin)
    {
        int x  = (key & 0xFFF) - 2047;
        int z = ((key >> 20) & 0xFFF) - 2047;
        int y = (key >> 12) & 0xFF;
        return origin.add(x, y, z);
    }
}
