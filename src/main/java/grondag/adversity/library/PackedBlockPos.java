package grondag.adversity.library;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.Chunk;

public class PackedBlockPos
{
    private static final int WORLD_BOUNDARY = 30000000;
    private static final int CHUNK_BOUNDARY = WORLD_BOUNDARY >> 4;
    private static final int NUM_X_BITS = 1 + MathHelper.calculateLogBaseTwo(MathHelper.roundUpToPowerOfTwo(WORLD_BOUNDARY));
    private static final int NUM_Z_BITS = NUM_X_BITS;
    private static final int NUM_Y_BITS = 8;
    private static final int NUM_EXTRA_BITS = 3;
    
    private static final int Y_SHIFT = 0 + NUM_Z_BITS;
    private static final int X_SHIFT = Y_SHIFT + NUM_Y_BITS;
    private static final int EXTRA_SHIFT = X_SHIFT + NUM_Z_BITS;
    private static final long X_MASK = (1L << NUM_X_BITS) - 1L;
    private static final long Y_MASK = (1L << NUM_Y_BITS) - 1L;
    private static final long Z_MASK = (1L << NUM_Z_BITS) - 1L;
    private static final long EXTRA_MASK = (1L << NUM_EXTRA_BITS) - 1L;
    private static final long POSITION_MASK = (1L << EXTRA_SHIFT) - 1L;
    
    /** must be subtracted when packed values are added - otherwise boundary offsets get included twice */
    private static final long ADDITION_OFFSET = ((long)WORLD_BOUNDARY << X_SHIFT) | WORLD_BOUNDARY;
    
    private static final long X_INCREMENT = 1L << X_SHIFT;
    private static final long Y_INCREMENT = 1L << Y_SHIFT;
    private static final long Z_INCREMENT = 1L;
    
    /** 
     * Very similar to MC vanilla method on BlockPos but only uses 8 bits for Y axis instead of 12. 
     * As with that method, uses 26 bits each for X and Z, leaving 3 higher-order bits available for other information.
     * Not using the sign bit because it complicates things somewhat and not currently needed.
     */
    public static long pack(BlockPos pos)
    {
        return pack(pos.getX(), pos.getY(), pos.getZ());
    }
    
    public static long pack(BlockPos pos, int extra)
    {
        return pack(pos.getX(), pos.getY(), pos.getZ(), extra);
    }
    
    /**
     * Same as version that uses the BlockPos input but with primitive types as inputs.
     */
    public static long pack(int x, int y, int z)
    {
        return ((long)(x + WORLD_BOUNDARY) & X_MASK) << X_SHIFT | ((long)y & Y_MASK) << Y_SHIFT | ((long)(z + WORLD_BOUNDARY) & Z_MASK);
    }
    
    /**
     * Same as version that uses the BlockPos input but with primitive types as inputs.
     * This version includes 4 extra bits of data that can be used in any way needed.
     */
    public static long pack(int x, int y, int z, int extra)
    {
        return pack(x, y, z) | ((long)(extra & EXTRA_MASK) << EXTRA_SHIFT);
    }
    
    public static BlockPos unpack(long packedValue)
    {
        int i = (int)((packedValue >> X_SHIFT) & X_MASK) - WORLD_BOUNDARY;
        int j = (int)((packedValue >> Y_SHIFT) & Y_MASK);
        int k = (int)(packedValue & Z_MASK) - WORLD_BOUNDARY;
        return new BlockPos(i, j, k);
    }
    
    /** adds two packed block positions together */
    public static long add(long first, long second)
    {
        return first + second -ADDITION_OFFSET;
    }
    
    public static long up(long packedValue)
    {
        return packedValue + Y_INCREMENT;
    }
    
    public static long down(long packedValue)
    {
        return packedValue - Y_INCREMENT;
    }

    public static long down(long packedValue, int howFar)
    {
        return packedValue - Y_INCREMENT * howFar;
    }
    
    public static long east(long packedValue)
    {
        return packedValue + X_INCREMENT;
    }
    
    public static long west(long packedValue)
    {
        return packedValue - X_INCREMENT;
    }
    
    public static long north(long packedValue)
    {
        return packedValue - Z_INCREMENT;
    }
    
    public static long south(long packedValue)
    {
        return packedValue + Z_INCREMENT;
    }
    
    public static int getX(long packedValue)
    {
        return (int)((packedValue >> X_SHIFT) & X_MASK) - WORLD_BOUNDARY;
    }
    
    public static int getY(long packedValue)
    {
        return (int)((packedValue >> Y_SHIFT) & Y_MASK);
    }
    
    public static int getZ(long packedValue)
    {
        return (int)(packedValue & Z_MASK) - WORLD_BOUNDARY;
    }
    
    public static int getExtra(long packedValue)
    {
        return (int)((packedValue >> EXTRA_SHIFT) & EXTRA_MASK);
    }
    
    public static long setExtra(long packedValue, int extra)
    {
        return (packedValue & POSITION_MASK) | ((long)extra << EXTRA_SHIFT);
    }
    
    /** strips the extra bits if there are any */
    public static long getPosition(long packedValue)
    {
        return (packedValue & POSITION_MASK);
    }
    
    public static long getPackedChunkPos(long packedBlockPos)
    {
           return getPackedChunkPos(getX(packedBlockPos), getZ(packedBlockPos));
    }
    
    public static long getPackedChunkPos(int blockX, int blockZ)
    {
            return ((long)blockX >> 4) + CHUNK_BOUNDARY | (((long)blockZ >> 4) + CHUNK_BOUNDARY) << 32;
    }
    
    public static long getPackedChunkPos(BlockPos pos)
    {
            return getPackedChunkPos(pos.getX(), pos.getZ());
    }
    
    public static long getPackedChunkPos(Chunk chunk)
    {
            return ((long)chunk.xPosition) + CHUNK_BOUNDARY | (((long)chunk.zPosition) + CHUNK_BOUNDARY) << 32;
    }
    
    /** analog of Chunk.chunkXPos */
    public static int getChunkXPos(long packedChunkPos)
    {
        return (int)((packedChunkPos & 0xFFFFFFFF) - CHUNK_BOUNDARY);
    }
    
    /** analog of Chunk.chunkZPos */
    public static int getChunkZPos(long packedChunkPos)
    {
        return (int)(((packedChunkPos >> 32) & 0xFFFFFFFF) - CHUNK_BOUNDARY);
    }
    
    /** analog of Chunk.getXStart() */
    public static int getChunkXStart(long packedChunkPos)
    {
        return getChunkXPos(packedChunkPos) << 4;
    }
    
    /** analog of Chunk.getZStart() */
    public static int getChunkZStart(long packedChunkPos)
    {
        return getChunkZPos(packedChunkPos) << 4;
    }
}
