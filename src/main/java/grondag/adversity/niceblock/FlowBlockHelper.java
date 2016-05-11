package grondag.adversity.niceblock;

import grondag.adversity.niceblock.base.BlockModelHelper;
import grondag.adversity.niceblock.base.ModelDispatcher;
import grondag.adversity.niceblock.base.ModelState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class FlowBlockHelper extends BlockModelHelper
{

    public final int levelCount;
    
    protected FlowBlockHelper(ModelDispatcher dispatcher, int levelCount)
    {
        super(dispatcher);
        this.levelCount = levelCount;
    }

    @Override
    public ModelState getModelStateForBlock(IBlockState state, IBlockAccess world, BlockPos pos, boolean doClientStateRefresh)
    {
        ModelState retVal = new ModelState(0, 0);
        dispatcher.refreshClientShapeIndex(block, state, world, pos, retVal, true);
        return retVal;
    }

    @Override
    public int getItemModelCount()
    {
        return levelCount;
    }

    @Override
    public ModelState getModelStateForItemModel(int itemIndex)
    {
        int level = (levelCount - itemIndex) * 15 / levelCount;
        return new ModelState(0, 0).setClientShapeIndex(level | level << 4 | level << 8 | level << 12, 0);
    }

}
