package grondag.adversity.feature.volcano.lava;


import grondag.adversity.library.ISimpleListItem;
import grondag.adversity.library.PackedBlockPos;
import grondag.adversity.library.Useful;
import net.minecraft.util.math.BlockPos;

/**
 * Just like a BlockPos but with an extra integer tick value.
 * Tick value does not affect hashcode or equals, 
 * so in a set two values with same BlockPos will collide (as intended).
 */
public class AgedBlockPos implements ISimpleListItem
{
    private int tick;
    public final long packedBlockPos;
    private boolean isDeleted = false;
    
    public AgedBlockPos(BlockPos pos, int tick)
    {
        this(PackedBlockPos.pack(pos), tick);
    }
    
    public AgedBlockPos(long packedBlockPos, int tick)
    {
        this.packedBlockPos = packedBlockPos;
        this.tick = tick;
    }
    
    public void setTick(int tickIn)
    {
        this.tick = tickIn;
    }

    public int getTick()
    {
        return this.tick;
    }
    
    public void setDeleted()
    {
        this.isDeleted = true;
    }
    
    @Override
    public int hashCode()
    {
        return (int) (Useful.longHash(this.packedBlockPos) & 0xFFFFFFFF);
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == this)
        {
            return true;
        }
        else if(obj instanceof AgedBlockPos)
        {
            return this.packedBlockPos == ((AgedBlockPos)obj).packedBlockPos;
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean isDeleted()
    {
         return this.isDeleted;
    }
 
}