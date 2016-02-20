package grondag.adversity.niceblock.newmodel;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BorderHelper extends BlockModelHelper
{

    protected BorderHelper(ModelDispatcherBase dispatcher)
    {
        super(dispatcher);
        // TODO Auto-generated constructor stub
    }

    @Override
    public ModelState getModelStateForBlock(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getSubItemCount()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ModelState getModelStateForItem(int itemIndex)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
