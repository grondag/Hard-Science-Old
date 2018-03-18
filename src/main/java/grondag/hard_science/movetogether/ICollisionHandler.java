package grondag.hard_science.movetogether;

import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public interface ICollisionHandler
{
    
    public default List<AxisAlignedBB> getCollisionBoxes(ISuperModelState modelState, BlockPos offset)
    {
        return getCollisionBoxes(modelState).stream().map(aabb -> aabb.offset(offset)).collect(Collectors.toList());
    }
    
    public default List<AxisAlignedBB> getCollisionBoxes(ISuperModelState modelState)
    {
        return CollisionBoxDispatcher.INSTANCE.getCollisionBoxes(modelState);
    }

    public default AxisAlignedBB getCollisionBoundingBox(ISuperModelState modelState)
    {
        return Block.FULL_BLOCK_AABB;
    }

    public default AxisAlignedBB getRenderBoundingBox(ISuperModelState modelState)
    {
        return Block.FULL_BLOCK_AABB;
    }
 
}
