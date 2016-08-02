package grondag.adversity.niceblock;

import java.util.List;

import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.support.ICollisionHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CSGController extends ColorController implements ICollisionHandler
{

    public CSGController(String textureName, int alternateCount, BlockRenderLayer renderLayer, boolean isShaded, boolean useRotations)
    {
        super(textureName, alternateCount, renderLayer, isShaded, useRotations);
        this.bakedModelFactory = new CSGModelFactory(this);
    }

    @Override
    public ICollisionHandler getCollisionHandler()
    {
        return this;
    }

    @Override
    public long getCollisionKey(World worldIn, BlockPos pos, IBlockState state)
    {
        Block block = state.getBlock();
        if(block instanceof CSGBlock)
        {
            return this.getDynamicShapeIndex((NiceBlock) block, state, worldIn, pos);
        }
        else
        {
            return 0;
        }
    }

    @Override
    public List<AxisAlignedBB> getModelBounds(long collisionKey)
    {
        return CollisionBoxGenerator.makeCollisionBox(((CSGModelFactory)this.bakedModelFactory).makeRawQuads(collisionKey));
    }

}
