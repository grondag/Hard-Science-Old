package grondag.adversity.superblock.model.shape;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.block.Block;
import net.minecraft.util.math.AxisAlignedBB;

public class CubeCollisionHandler implements ICollisionHandler
{

    public static CubeCollisionHandler INSTANCE = new CubeCollisionHandler();
    
    private static final List<AxisAlignedBB> FULL_BLOCK_AABB_LIST = ImmutableList.of(Block.FULL_BLOCK_AABB);
 
    @Override
    public List<AxisAlignedBB> getCollisionBoxes(ModelState modelState)
    {
        return FULL_BLOCK_AABB_LIST;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(ModelState modelState)
    {
        return Block.FULL_BLOCK_AABB;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox(ModelState modelState)
    {
        return Block.FULL_BLOCK_AABB;
    }
}