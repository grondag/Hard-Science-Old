package grondag.adversity.niceblock.newmodel;

import grondag.adversity.niceblock.support.NicePlacement;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
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
    // updateTileEntityOnPlacedBlockFromStack will give the colorIndex value to the TE state.
    @Override
    public int getMetaForPlacedBlockFromStack(World worldIn, BlockPos posPlaced, BlockPos posOn, EnumFacing facing, ItemStack stack, EntityPlayer player)
    {
        return 0;
    }

    @Override
    public void updateItemStackForPickBlock(ItemStack stack, IBlockState blockState, ModelState modelState, NiceTileEntity niceTE)
    {
        super.updateItemStackForPickBlock(stack, blockState, modelState, niceTE);
        stack.setItemDamage(modelState.getColorIndex());
    }

    @Override
    public void updateTileEntityOnPlacedBlockFromStack(ItemStack stack, EntityPlayer player, World world, BlockPos pos, IBlockState newState, NiceTileEntity niceTE)
    {
        super.updateTileEntityOnPlacedBlockFromStack(stack, player, world, pos, newState, niceTE);
        niceTE.modelState.setColorIndex(stack.getMetadata());
        niceTE.markDirty();
    }
    
    
}