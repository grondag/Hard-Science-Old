package grondag.hard_science.superblock.collision;

import java.util.List;
import java.util.stream.Collectors;

import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public interface ICollisionHandler
{
    public List<AxisAlignedBB> getCollisionBoxes(ModelState modelState);
    
    public default List<AxisAlignedBB> getCollisionBoxes(ModelState modelState, BlockPos offset)
    {
        return getCollisionBoxes(modelState).stream().map(aabb -> aabb.offset(offset)).collect(Collectors.toList());
    }
    
    public AxisAlignedBB getCollisionBoundingBox(ModelState modelState);
    
    public AxisAlignedBB getRenderBoundingBox(ModelState modelState);
 
}
