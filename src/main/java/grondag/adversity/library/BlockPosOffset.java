package grondag.adversity.library;

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
public class BlockPosOffset
{

}
