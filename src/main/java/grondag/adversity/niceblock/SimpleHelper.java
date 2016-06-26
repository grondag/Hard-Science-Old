package grondag.adversity.niceblock;

import grondag.adversity.niceblock.base.BlockModelHelper;
import grondag.adversity.niceblock.base.ModelDispatcher;
import grondag.adversity.niceblock.base.ModelState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Simple block model helper for models that have only one color.
 */
public class SimpleHelper extends BlockModelHelper
{
    public final int itemCount;
    
    protected SimpleHelper(ModelDispatcher dispatcher, int itemCount)
    {
        super(dispatcher);
        this.itemCount = itemCount;
    }

    @Override
    public ModelState getModelStateForBlock(IBlockState state, IBlockAccess world, BlockPos pos, boolean doClientStateRefresh)
    {
        ModelState retVal = new ModelState(0);
        dispatcher.refreshClientShapeIndex(block, state, world, pos, retVal, true);
        return retVal;
    }

    @Override
    public int getItemModelCount()
    {
        return itemCount;
    }

    @Override
    public ModelState getModelStateForItemModel(int itemIndex)
    {
        return new ModelState(0).setShapeIndex(itemIndex, BlockRenderLayer.SOLID);
    }

}