package grondag.hard_science.superblock.collision;

import java.util.List;
import java.util.stream.Collectors;

import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.block.Block;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public interface ICollisionHandler
{
    
    public default List<AxisAlignedBB> getCollisionBoxes(ModelState modelState, BlockPos offset)
    {
        return getCollisionBoxes(modelState).stream().map(aabb -> aabb.offset(offset)).collect(Collectors.toList());
    }
    
    public default List<AxisAlignedBB> getCollisionBoxes(ModelState modelState)
    {
        return CollisionBoxDispatcher.INSTANCE.getCollisionBoxes(modelState);
    }

    public default AxisAlignedBB getCollisionBoundingBox(ModelState modelState)
    {
        return Block.FULL_BLOCK_AABB;
    }

    public default AxisAlignedBB getRenderBoundingBox(ModelState modelState)
    {
        return Block.FULL_BLOCK_AABB;
    }
 
}
