package grondag.adversity.feature.volcano.lava;


import net.minecraft.util.math.BlockPos;

/**
 * Just like a BlockPos but with an extra integer tick value.
 * Tick value does not affect hashcode or equals, 
 * so in a set two values with same BlockPos will collide (as intended).
 */
public class AgedBlockPos 
{
    private int tick;
    public final BlockPos pos;
    
    public AgedBlockPos(BlockPos pos, int tick)
    {
        this.pos = pos;
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
    
    @Override
    public int hashCode()
    {
        return this.pos.hashCode();
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
            return this.pos.equals(((AgedBlockPos)obj).pos);
        }
        else
        {
            return false;
        }
    }
    

    
//    @Override
//    public int compareTo(AgedBlockPos other)
//    {
//        return ComparisonChain.start()
//                .compare(this.tick, other.tick)
//                .compare(this.sequenceID, other.sequenceID)
//                .result();
//    }
}
