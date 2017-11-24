package grondag.hard_science.library.world;

import javax.annotation.Nullable;

import grondag.hard_science.library.varia.Useful;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

/**
 * Integer version of the vanilla MC AABB class.
 */
public class IntegerAABB
{
    public static final IntegerAABB FULL_BLOCK_BOX = new IntegerAABB(0, 0, 0, 1, 1, 1);
    
    public static class Builder
    {
        private int minX;
        private int minY;
        private int minZ;
        private int maxX;
        private int maxY;
        private int maxZ;
        private boolean seenFirst = false;
        
        public void add(int x1, int y1, int z1, int x2, int y2, int z2)
        {
            if(seenFirst)
            {
                this.minX = Useful.min(this.minX, x1, x2);
                this.minY = Useful.min(this.minY, y1, y2);
                this.minZ = Useful.min(this.minZ, z1, z2);
                this.maxX = Useful.max(this.maxX, x1, x2);
                this.maxY = Useful.max(this.maxY, y1, y2);
                this.maxZ = Useful.max(this.maxZ, z1, z2);
            }
            else
            {   
                this.seenFirst = true;
                this.minX = Math.min(x1, x2);
                this.minY = Math.min(y1, y2);
                this.minZ = Math.min(z1, z2);
                this.maxX = Math.max(x1, x2);
                this.maxY = Math.max(y1, y2);
                this.maxZ = Math.max(z1, z2);
            }
        }
        
        public void add(BlockPos pos)
        {
            this.add(pos.getX(), pos.getY(), pos.getZ(), (pos.getX() + 1), (pos.getY() + 1), (pos.getZ() + 1));
        }
        
        @Nullable
        public IntegerAABB build()
        {
            return this.seenFirst ? new IntegerAABB(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ) : null;
        }
        
    }
    protected final int minX;
    protected final int minY;
    protected final int minZ;
    protected final int maxX;
    protected final int maxY;
    protected final int maxZ;

    public IntegerAABB(int x1, int y1, int z1, int x2, int y2, int z2)
    {
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }

    public IntegerAABB(BlockPos pos)
    {
        this(pos.getX(), pos.getY(), pos.getZ(), (pos.getX() + 1), (pos.getY() + 1), (pos.getZ() + 1));
    }

    /**
     * Differs from normal AABB in that it ensures the AABB includes both positions. Order does not matter.
     */
    public IntegerAABB(BlockPos pos1, BlockPos pos2)
    {
        this.minX = Math.min(pos1.getX(), pos2.getX());
        this.minY = Math.min(pos1.getY(), pos2.getY());
        this.minZ = Math.min(pos1.getZ(), pos2.getZ());
        this.maxX = Math.max(pos1.getX(), pos2.getX()) + 1;
        this.maxY = Math.max(pos1.getY(), pos2.getY()) + 1;
        this.maxZ = Math.max(pos1.getZ(), pos2.getZ()) + 1;
    }
 
    private AxisAlignedBB aabb = null;
    
    public AxisAlignedBB toAABB()
    {
        if(this.aabb == null)
        {
            this.aabb = new AxisAlignedBB(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
        }
        return this.aabb;
    }
    
    private BlockPos minPos = null;
    
    public BlockPos minPos()
    {
        if(this.minPos == null)
        {
            this.minPos = new BlockPos(this.minX, this.minY, this.minZ);
        }
        return this.minPos;
    }
    
 private BlockPos maxPos = null;
    
    public BlockPos maxPos()
    {
        if(this.maxPos == null)
        {
            this.maxPos = new BlockPos(this.maxX - 1, this.maxY - 1, this.maxZ - 1);
        }
        return this.maxPos;
    }
    
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        else if (!(other instanceof IntegerAABB))
        {
            return false;
        }
        else
        {
            IntegerAABB otherBox = (IntegerAABB)other;

            return this.minX == otherBox.minX
                 & this.minY == otherBox.minY
                 & this.minZ == otherBox.minZ
                 & this.maxX == otherBox.maxX
                 & this.maxY == otherBox.maxY
                 & this.maxZ == otherBox.maxZ;
        }
    }

    public int hashCode()
    {
        long i = this.minX;
        int j = (int)(i ^ i >>> 32);
        i = this.minY;
        j = 31 * j + (int)(i ^ i >>> 32);
        i = this.minZ;
        j = 31 * j + (int)(i ^ i >>> 32);
        i = this.maxX;
        j = 31 * j + (int)(i ^ i >>> 32);
        i = this.maxY;
        j = 31 * j + (int)(i ^ i >>> 32);
        i = this.maxZ;
        j = 31 * j + (int)(i ^ i >>> 32);
        return j;
    }

    /**
     * Creates a new {@link IntegerAABB} that has been contracted by the given amount, with positive changes
     * decreasing max values and negative changes increasing min values.
     * <br/>
     * If the amount to contract by is larger than the length of a side, then the side will wrap.
     *  
     * @return A new modified bounding box.
     */
    public IntegerAABB contract(int x, int y, int z)
    {
        int d0 = this.minX;
        int d1 = this.minY;
        int d2 = this.minZ;
        int d3 = this.maxX;
        int d4 = this.maxY;
        int d5 = this.maxZ;

        if (x < 0.0D)
        {
            d0 -= x;
        }
        else if (x > 0.0D)
        {
            d3 -= x;
        }

        if (y < 0.0D)
        {
            d1 -= y;
        }
        else if (y > 0.0D)
        {
            d4 -= y;
        }

        if (z < 0.0D)
        {
            d2 -= z;
        }
        else if (z > 0.0D)
        {
            d5 -= z;
        }

        return new IntegerAABB(d0, d1, d2, d3, d4, d5);
    }

    /**
     * Creates a new {@link IntegerAABB} that has been expanded by the given amount, with positive changes increasing
     * max values and negative changes decreasing min values.
    
     * @return A modified bounding box that will always be equal or greater in volume to this bounding box.
     */
    public IntegerAABB expand(int x, int y, int z)
    {
        int d0 = this.minX;
        int d1 = this.minY;
        int d2 = this.minZ;
        int d3 = this.maxX;
        int d4 = this.maxY;
        int d5 = this.maxZ;

        if (x < 0.0D)
        {
            d0 += x;
        }
        else if (x > 0.0D)
        {
            d3 += x;
        }

        if (y < 0.0D)
        {
            d1 += y;
        }
        else if (y > 0.0D)
        {
            d4 += y;
        }

        if (z < 0.0D)
        {
            d2 += z;
        }
        else if (z > 0.0D)
        {
            d5 += z;
        }

        return new IntegerAABB(d0, d1, d2, d3, d4, d5);
    }

    /**
     * Creates a new {@link IntegerAABB} that has been contracted by the given amount in both directions. Negative
     * values will shrink the AABB instead of expanding it.
     * <br/>
     * Side lengths will be increased by 2 times the value of the parameters, since both min and max are changed.
     * <br/>
     * If contracting and the amount to contract by is larger than the length of a side, then the side will wrap.
     *  
     * @return A modified bounding box.
     */
    public IntegerAABB grow(int x, int y, int z)
    {
        int d0 = this.minX - x;
        int d1 = this.minY - y;
        int d2 = this.minZ - z;
        int d3 = this.maxX + x;
        int d4 = this.maxY + y;
        int d5 = this.maxZ + z;
        return new IntegerAABB(d0, d1, d2, d3, d4, d5);
    }

    /**
     * Creates a new {@link IntegerAABB} that is expanded by the given value in all directions. Equivalent to {@link
     * #grow(int, int, int)} with the given value for all 3 params. Negative values will shrink the AABB.
     * <br/>
     * Side lengths will be increased by 2 times the value of the parameter, since both min and max are changed.
     * <br/>
     * If contracting and the amount to contract by is larger than the length of a side, then the side will wrap.
     *  
     * @return A modified AABB.
     */
    public IntegerAABB grow(int value)
    {
        return this.grow(value, value, value);
    }

    public IntegerAABB intersect(IntegerAABB other)
    {
        int d0 = Math.max(this.minX, other.minX);
        int d1 = Math.max(this.minY, other.minY);
        int d2 = Math.max(this.minZ, other.minZ);
        int d3 = Math.min(this.maxX, other.maxX);
        int d4 = Math.min(this.maxY, other.maxY);
        int d5 = Math.min(this.maxZ, other.maxZ);
        return new IntegerAABB(d0, d1, d2, d3, d4, d5);
    }

    public IntegerAABB union(IntegerAABB other)
    {
        int d0 = Math.min(this.minX, other.minX);
        int d1 = Math.min(this.minY, other.minY);
        int d2 = Math.min(this.minZ, other.minZ);
        int d3 = Math.max(this.maxX, other.maxX);
        int d4 = Math.max(this.maxY, other.maxY);
        int d5 = Math.max(this.maxZ, other.maxZ);
        return new IntegerAABB(d0, d1, d2, d3, d4, d5);
    }

    /**
     * Offsets the current bounding box by the specified amount.
     */
    public IntegerAABB offset(int x, int y, int z)
    {
        return new IntegerAABB(this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z);
    }

    public IntegerAABB offset(BlockPos pos)
    {
        return new IntegerAABB(this.minX + (int)pos.getX(), this.minY + (int)pos.getY(), this.minZ + (int)pos.getZ(), this.maxX + (int)pos.getX(), this.maxY + (int)pos.getY(), this.maxZ + (int)pos.getZ());
    }

    public IntegerAABB offset(Vec3i vec)
    {
        return this.offset(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * if instance and the argument bounding boxes overlap in the Y and Z dimensions, calculate the offset between them
     * in the X dimension.  return var2 if the bounding boxes do not overlap or if var2 is closer to 0 then the
     * calculated offset.  Otherwise return the calculated offset.
     */
    public int calculateXOffset(IntegerAABB other, int offsetX)
    {
        if (other.maxY > this.minY && other.minY < this.maxY && other.maxZ > this.minZ && other.minZ < this.maxZ)
        {
            if (offsetX > 0.0D && other.maxX <= this.minX)
            {
                int d1 = this.minX - other.maxX;

                if (d1 < offsetX)
                {
                    offsetX = d1;
                }
            }
            else if (offsetX < 0.0D && other.minX >= this.maxX)
            {
                int d0 = this.maxX - other.minX;

                if (d0 > offsetX)
                {
                    offsetX = d0;
                }
            }

            return offsetX;
        }
        else
        {
            return offsetX;
        }
    }

    /**
     * if instance and the argument bounding boxes overlap in the X and Z dimensions, calculate the offset between them
     * in the Y dimension.  return var2 if the bounding boxes do not overlap or if var2 is closer to 0 then the
     * calculated offset.  Otherwise return the calculated offset.
     */
    public int calculateYOffset(IntegerAABB other, int offsetY)
    {
        if (other.maxX > this.minX && other.minX < this.maxX && other.maxZ > this.minZ && other.minZ < this.maxZ)
        {
            if (offsetY > 0.0D && other.maxY <= this.minY)
            {
                int d1 = this.minY - other.maxY;

                if (d1 < offsetY)
                {
                    offsetY = d1;
                }
            }
            else if (offsetY < 0.0D && other.minY >= this.maxY)
            {
                int d0 = this.maxY - other.minY;

                if (d0 > offsetY)
                {
                    offsetY = d0;
                }
            }

            return offsetY;
        }
        else
        {
            return offsetY;
        }
    }

    /**
     * if instance and the argument bounding boxes overlap in the Y and X dimensions, calculate the offset between them
     * in the Z dimension.  return var2 if the bounding boxes do not overlap or if var2 is closer to 0 then the
     * calculated offset.  Otherwise return the calculated offset.
     */
    public int calculateZOffset(IntegerAABB other, int offsetZ)
    {
        if (other.maxX > this.minX && other.minX < this.maxX && other.maxY > this.minY && other.minY < this.maxY)
        {
            if (offsetZ > 0.0D && other.maxZ <= this.minZ)
            {
                int d1 = this.minZ - other.maxZ;

                if (d1 < offsetZ)
                {
                    offsetZ = d1;
                }
            }
            else if (offsetZ < 0.0D && other.minZ >= this.maxZ)
            {
                int d0 = this.maxZ - other.minZ;

                if (d0 > offsetZ)
                {
                    offsetZ = d0;
                }
            }

            return offsetZ;
        }
        else
        {
            return offsetZ;
        }
    }

    /**
     * Checks if the bounding box intersects with another.
     */
    public boolean intersects(IntegerAABB other)
    {
        return this.intersects(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
    }

    public boolean intersects(int x1, int y1, int z1, int x2, int y2, int z2)
    {
        return this.minX < x2 && this.maxX > x1 && this.minY < y2 && this.maxY > y1 && this.minZ < z2 && this.maxZ > z1;
    }

    /**
     * Returns if the supplied Vec3D is completely inside the bounding box
     */
    public boolean contains(Vec3d vec)
    {
        if (vec.x > this.minX && vec.x < this.maxX)
        {
            if (vec.y > this.minY && vec.y < this.maxY)
            {
                return vec.z > this.minZ && vec.z < this.maxZ;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    /**
     * Returns if the supplied BlockPos is include inside the bounding box
     */
    public boolean contains(BlockPos pos)
    {
        if (pos.getX() >= this.minX && pos.getX() < this.maxX)
        {
            if (pos.getY() >= this.minY && pos.getY() < this.maxY)
            {
                return pos.getZ() >= this.minZ && pos.getZ() < this.maxZ;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Returns the average length of the edges of the bounding box.
     */
    public double getAverageEdgeLength()
    {
        int d0 = this.maxX - this.minX;
        int d1 = this.maxY - this.minY;
        int d2 = this.maxZ - this.minZ;
        return (d0 + d1 + d2) / 3.0D;
    }

    /**
     * Creates a new {@link IntegerAABB} that is expanded by the given value in all directions. Equivalent to {@link
     * #grow(int)} with value set to the negative of the value provided here. Passing a negative value to this method
     * values will grow the AABB.
     * <br/>
     * Side lengths will be decreased by 2 times the value of the parameter, since both min and max are changed.
     * <br/>
     * If contracting and the amount to contract by is larger than the length of a side, then the side will wrap.
     *  
     * @return A modified AABB.
     */
    public IntegerAABB shrink(int value)
    {
        return this.grow(-value);
    }

    public String toString()
    {
        return "box[" + this.minX + ", " + this.minY + ", " + this.minZ + " -> " + this.maxX + ", " + this.maxY + ", " + this.maxZ + "]";
    }

    public BlockPos getCenter()
    {
        return new BlockPos(this.minX + (this.maxX - this.minX) * 0.5D, this.minY + (this.maxY - this.minY) * 0.5D, this.minZ + (this.maxZ - this.minZ) * 0.5D);
    }
}

