package grondag.adversity.niceblock.base;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NiceItemBlockColor extends NiceItemBlock2
{
    public NiceItemBlockColor(NiceBlock2 block)
    {
        super(block);
    }

    @Override
    public int getItemModelCount()
    {
        return (int) ((NiceBlock2)block).dispatcher.getStateSet().getFirstColorMapComponent().getValueCount();
    }

    @Override
    public List<ItemStack> getSubItems()
    {
        ImmutableList.Builder<ItemStack> itemBuilder = new ImmutableList.Builder<ItemStack>();
        for(int i = 0; i < getItemModelCount(); i++)
        {
            itemBuilder.add(new ItemStack(block.item, 1, i));
        }
        return itemBuilder.build();
    }

    @Override
    public long getModelKeyForSubItem(int itemIndex)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getMetaForPlacedBlockFromStack(World worldIn, BlockPos posPlaced, BlockPos posOn, EnumFacing facing, ItemStack stack, EntityPlayer player)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void updateTileEntityOnPlacedBlockFromStack(ItemStack stack, EntityPlayer player, World world, BlockPos pos, IBlockState newState,
            NiceTileEntity niceTE)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public long getModelKeyFromStack(ItemStack stack)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void updateItemStackForPickBlock(ItemStack stack, IBlockState blockState, NiceTileEntity niceTE)
    {
        // TODO Auto-generated method stub

    }

}
