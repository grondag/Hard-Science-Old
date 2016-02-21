package grondag.adversity.niceblock.newmodel;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ColoredBlockHelperPlus extends ColoredBlockHelperMeta
{
    public ColoredBlockHelperPlus(ModelDispatcherBase dispatcher)
    {
        super(dispatcher);
    }

    @Override
    public ModelState getModelStateForBlock(IBlockState state, IBlockAccess world, BlockPos pos, boolean doClientStateRefresh)
    {
        ModelState retVal;
        NiceTileEntity niceTE = (NiceTileEntity)world.getTileEntity(pos);
        if (niceTE != null) 
        {
            if(doClientStateRefresh && niceTE.isClientShapeIndexDirty)
            {
                if(dispatcher.refreshClientShapeIndex(block, state, world, pos, niceTE.modelState))
                {
                    niceTE.markDirty();
                }
                niceTE.isClientShapeIndexDirty = false;
            }
            retVal = niceTE.modelState;
        }
        else
        {
            retVal = new ModelState();
        }
        return retVal;
    }
}