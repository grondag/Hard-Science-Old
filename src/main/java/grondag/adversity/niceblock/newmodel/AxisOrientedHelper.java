package grondag.adversity.niceblock.newmodel;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class AxisOrientedHelper extends ColoredBlockHelperPlus
{
    public AxisOrientedHelper(ModelDispatcherBase dispatcher)
    {
        super(dispatcher);
    }

    @Override
    public ModelState getModelStateForBlock(IBlockState state, IBlockAccess world, BlockPos pos, boolean doClientStateRefresh)
    {
        // TODO Auto-generated method stub
        return super.getModelStateForBlock(state, world, pos, doClientStateRefresh);
    }

    @Override
    public int getMetaForPlacedBlockFromStack(World worldIn, BlockPos posPlaced, BlockPos posOn, EnumFacing facing, ItemStack stack, EntityPlayer player)
    {
        // TODO Auto-generated method stub
        return super.getMetaForPlacedBlockFromStack(worldIn, posPlaced, posOn, facing, stack, player);
    }

    @Override
    public void updateItemStackForPickBlock(ItemStack stack, IBlockState blockState, ModelState modelState, NiceTileEntity niceTE)
    {
        // TODO Auto-generated method stub
        super.updateItemStackForPickBlock(stack, blockState, modelState, niceTE);
    }

    @Override
    public void updateTileEntityOnPlacedBlockFromStack(ItemStack stack, EntityPlayer player, World world, BlockPos pos, IBlockState newState,
            NiceTileEntity niceTE)
    {
        // TODO Auto-generated method stub
        super.updateTileEntityOnPlacedBlockFromStack(stack, player, world, pos, newState, niceTE);
    }

    @Override
    public int getItemModelCount()
    {
        // TODO Auto-generated method stub
        return super.getItemModelCount();
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        // TODO Auto-generated method stub
        super.addInformation(stack, playerIn, tooltip, advanced);
    }

    @Override
    public ModelState getModelStateForItemModel(int itemIndex)
    {
        ModelState modelState = new ModelState(0, itemIndex & 3);
        modelState.setClientShapeIndex(itemIndex / 3, EnumWorldBlockLayer.SOLID.ordinal());
        return modelState;
    }


}
