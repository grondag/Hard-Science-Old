package grondag.hard_science.library.world;

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
        int x  = (pos.getX() + 2047 - origin.getX()) & 0xFFF;
        int z = (pos.getZ() + 2047 - origin.getZ()) & 0xFFF;
        int y = pos.getY() & 0xFF;
        return x | y << 12 | z << 20;
    }
    
    public static BlockPos getPos(int key, BlockPos origin)
    {
        int x  = (key & 0xFFF) - 2047 + origin.getX();
        int z = ((key >> 20) & 0xFFF) - 2047 + origin.getZ();
        int y = (key >> 12) & 0xFF;
        return new BlockPos(x, y, z);
    }
}
