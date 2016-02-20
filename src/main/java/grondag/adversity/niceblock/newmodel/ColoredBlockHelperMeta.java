package grondag.adversity.niceblock.newmodel;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ColoredBlockHelperMeta extends BlockModelHelper
{
    public ColoredBlockHelperMeta(ModelDispatcherBase dispatcher)
    {
        super(dispatcher);
    }

    @Override
    public ModelState getModelStateForBlock(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        ModelState retVal = new ModelState(0, state.getValue(NiceBlock.META));
        dispatcher.refreshClientShapeIndex(block, state, world, pos, retVal);
        return retVal;
    }

    @Override
    public int getSubItemCount()
    {
        return dispatcher.getColorProvider().getColorCount();
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        String colorName = dispatcher.getColorProvider().getColor(stack.getMetadata()).vectorName;
        return baseDisplayName + (colorName == "" ? "" : ", " + colorName);
    }

    @Override
    public ModelState getModelStateForItem(int itemIndex)
    {
        return new ModelState(0, itemIndex);
    }
}