package grondag.adversity.niceblock.newmodel;

import grondag.adversity.niceblock.support.NicePlacement;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

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

    // Can't pass metadata to block state for blocks that are meant to be tile entities
    // because the item metadata value (colorIndex) will be out of range.  
    // placeBlockAt will give the colorIndex value to the TE state.
    @Override
    public int getMetaForPlacedBlockFromStack(World worldIn, BlockPos pos, EnumFacing facing, ItemStack stack)
    {
        return 0;
    }
    
    
}