package grondag.hard_science.library.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;

public class SingleBlockRegion implements IBlockRegion
{

    public final BlockPos pos;
    
    public SingleBlockRegion(BlockPos pos)
    {
        this.pos = pos;
    }
    
    @Override
    public Iterable<MutableBlockPos> surfacePositions()
    {
        return BlockPos.getAllInBoxMutable(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public Iterable<MutableBlockPos> adjacentPositions()
    {
        return CubicBlockRegion.getAllOnBoxSurfaceMutable(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1, pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }

}
