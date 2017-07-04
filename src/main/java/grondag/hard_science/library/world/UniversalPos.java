package grondag.hard_science.library.world;

import grondag.hard_science.library.varia.Useful;
import net.minecraft.util.math.BlockPos;

public class UniversalPos
{
    public final BlockPos pos;
    public final int dimensionID;
    
    public UniversalPos(BlockPos pos, int dimensionID)
    {
        this.pos = pos;
        this.dimensionID = dimensionID;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if( o == null || !(o instanceof UniversalPos)) return false;
        UniversalPos other = (UniversalPos)o;
        return this.pos.equals(other.pos) && this.dimensionID == other.dimensionID;
    }
    
    @Override
    public int hashCode()
    {
        return (int) Useful.longHash((long)this.pos.hashCode() | (this.dimensionID << 32));
    }
}
