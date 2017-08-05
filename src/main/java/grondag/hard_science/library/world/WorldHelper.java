package grondag.hard_science.library.world;

import grondag.hard_science.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;

public class WorldHelper
{

    /** 
     * Sorts members of the BlockPos vector so that x is largest and z is smallest.
     * Useful when BlockPos represents a volume instead of a position.
     */
    public static BlockPos sortedBlockPos(BlockPos pos){

        if(pos.getX() > pos.getY()){
            if(pos.getY() > pos.getZ()){
                //x > y > z
                return pos;
            } else if (pos.getX() > pos.getZ()){
                //x > z > y
                return new BlockPos(pos.getX(), pos.getZ(), pos.getY());
            } else {
                //z > x > y
                return new BlockPos(pos.getZ(), pos.getX(), pos.getY());
            }
        } else if(pos.getX() > pos.getZ()){
            // y > x > z
            return new BlockPos(pos.getY(), pos.getX(), pos.getY());
        } else if(pos.getY() > pos.getZ()){
            // y > z > x
            return new BlockPos(pos.getY(), pos.getZ(), pos.getX());
        } else {
            // z > y >x
            return new BlockPos(pos.getZ(), pos.getY(), pos.getX());
        }
    }

    /**
     * Convenience method to keep code more readable.
     * Call with replaceVirtualBlocks = true to behave as if virtual blocks not present.
     * Should generally be true if placing a normal block.
     */
    public static boolean isBlockReplaceable(IBlockAccess worldIn, BlockPos pos, boolean replaceVirtualBlocks)
    {
        if(replaceVirtualBlocks)
        {
            return worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos);
        }
        else
        {
            Block block = worldIn.getBlockState(pos).getBlock();
            return block != ModBlocks.virtual_block && block.isReplaceable(worldIn, pos);
        }
        
    }

    /**
     * Returns the closest face adjacent to the hit face that is closest to the hit location on the given face.
     * There is probably a better way to do this. TBH, I may have been drinking when this code was written.
     */
    public static EnumFacing closestAdjacentFace(EnumFacing hitFace, float hitX, float hitY, float hitZ)
    {
        switch(hitFace.getAxis())
        {
            case X:
            {
                // absolute distance from center of the face along the orthogonalAxis
                float yDist = 0.5F - hitY + MathHelper.floor(hitY);
                float zDist = 0.5F - hitZ + MathHelper.floor(hitZ);
                if(Math.abs(yDist) > Math.abs(zDist))
                {
                    return yDist < 0 ? EnumFacing.UP: EnumFacing.DOWN;
                }
                else
                {
                    return zDist < 0 ? EnumFacing.SOUTH : EnumFacing.NORTH;
                }
            }
    
            case Y:
            {
                // absolute distance from center of the face along the orthogonalAxis
                float xDist = 0.5F - hitX + MathHelper.floor(hitX);
                float zDist = 0.5F - hitZ + MathHelper.floor(hitZ);
                if(Math.abs(xDist) > Math.abs(zDist))
                {
                    return xDist < 0 ? EnumFacing.EAST : EnumFacing.WEST;
                }
                else
                {
                    return zDist < 0 ? EnumFacing.SOUTH : EnumFacing.NORTH;
                }
            }
    
            case Z:
            {
                // absolute distance from center of the face along the orthogonalAxis
                float yDist = 0.5F - hitY + MathHelper.floor(hitY);
                float xDist = 0.5F - hitX + MathHelper.floor(hitX);
                if(Math.abs(yDist) > Math.abs(xDist))
                {
                    return yDist < 0 ? EnumFacing.UP: EnumFacing.DOWN;
                }
                else
                {
                    return xDist < 0 ? EnumFacing.EAST : EnumFacing.WEST;
                }
            }
            default:
                //whatever
                return hitFace.rotateY();
        }
    }
}
