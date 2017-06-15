package grondag.adversity.superblock.model.shape;

import java.util.List;
import java.util.stream.Collectors;

import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
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

    /**
     *  Used by collision box dispatcher to extract a lookup key from model state.
     *  Only needs to be overridden if is shape is using the collision box dispatcher.
     *  Should only vary for state components that affect geometry.
     *  (Can't use the entire modelstate as the key.)
     */
    public default long collisionKey(ModelState modelState) { return 0; }
}
