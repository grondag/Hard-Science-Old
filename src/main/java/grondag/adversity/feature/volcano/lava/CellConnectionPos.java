package grondag.adversity.feature.volcano.lava;

import com.google.common.collect.ComparisonChain;

import grondag.adversity.Adversity;
import grondag.adversity.library.Useful;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

/**
 * Identifies location of a cell connection in the world.
 * Similar to BlockPos.  
 * Is in fact implemented as the lower-valued block position
 * and an indicator for the axis on which the higher-valued position is located.
 */
public class CellConnectionPos implements Comparable<CellConnectionPos>
{
    private final BlockPos lowerPos;
    private final EnumFacing.Axis axis;
    
    public CellConnectionPos(BlockPos lowerPos, EnumFacing.Axis axis)
    {
        this.lowerPos = lowerPos;
        this.axis = axis;
    }
    
    /**
     * Returns appropriate value given two adjacent cells.
     * Will barf if cells are not adjacent.
     */
    public CellConnectionPos(BlockPos pos1, BlockPos pos2)
    {
        int xDiff = pos2.getX() - pos1.getX();
        int yDiff = pos2.getY() - pos1.getY();
        int zDiff = pos2.getZ() - pos1.getZ();
        
        //TODO: disable in release or make better
        if(Math.abs(xDiff) + Math.abs(yDiff) + Math.abs(zDiff) != 1)
        {
            System.out.println("CellConnectionPos constructor BlockPos inputs not adjacent.  This should never happen. Weirdness may ensue.");
        }
        
        if(xDiff == 0)
        {
            if(yDiff == 0)
            {
                this.axis = EnumFacing.Axis.Z;
                if(zDiff > 0)
                    this.lowerPos = pos1;
                else
                    this.lowerPos = pos2;
            }
            else
            {
                this.axis = EnumFacing.Axis.Y;
                if(yDiff > 0)
                    this.lowerPos = pos1;
                else
                    this.lowerPos = pos2;
            }
        }
        else
        {
            this.axis = EnumFacing.Axis.X;
            if(xDiff > 0)
                this.lowerPos = pos1;
            else
                this.lowerPos = pos2;
        }
    }
    
    @Override
    public int compareTo(CellConnectionPos o)
    {
        return ComparisonChain.start()
                .compare(this.lowerPos.getY(), o.lowerPos.getY())
                .compare(this.lowerPos.getX(), o.lowerPos.getX())
                .compare(this.lowerPos.getZ(), o.lowerPos.getZ())
                .compare(this.axis, o.axis)
                .result();
    }
    
    public BlockPos getLowerPos()
    {
        return this.lowerPos;
    }
    
    public BlockPos getUpperPos()
    {
        switch(this.axis)
        {
        case X:
            return this.lowerPos.add(1, 0, 0);
            
        case Y:
            return this.lowerPos.add(0, 1, 0);

        case Z:
        default:
            return this.lowerPos.add(0, 0, 1);
        }
    }
    
    public EnumFacing.Axis getAxis()
    {
        return this.axis;
    }
    
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        else if (!(other instanceof CellConnectionPos))
        {
            return false;
        }
        else
        {
            CellConnectionPos otherCell = (CellConnectionPos)other;
            
            return this.axis == otherCell.axis
                    && this.lowerPos.getX() == otherCell.lowerPos.getX()
                    && this.lowerPos.getY() == otherCell.lowerPos.getY()
                    && this.lowerPos.getZ() == otherCell.lowerPos.getZ();
        }
    }

    public int hashCode()
    {
        return this.lowerPos.hashCode() << 2 | this.axis.ordinal();
    }

}
