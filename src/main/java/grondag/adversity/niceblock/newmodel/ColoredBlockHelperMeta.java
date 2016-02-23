package grondag.adversity.niceblock.newmodel;

import java.util.List;

import grondag.adversity.niceblock.support.NicePlacement;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
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
    public ModelState getModelStateForBlock(IBlockState state, IBlockAccess world, BlockPos pos, boolean doClientStateRefresh)
    {
        ModelState retVal = new ModelState(0, state.getValue(NiceBlock.META));
        if(doClientStateRefresh)
        {
            dispatcher.refreshClientShapeIndex(block, state, world, pos, retVal);
        }
        return retVal;
    }

    @Override
    public int getItemModelCount()
    {
        return dispatcher.getColorProvider().getColorCount();
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        return baseDisplayName;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        String colorName = dispatcher.getColorProvider().getColor(stack.getMetadata()).colorMapName;
        if(colorName !="")
        {
            tooltip.add(colorName);
        }
        
    }
    @Override
    public ModelState getModelStateForItemModel(int itemIndex)
    {
        return new ModelState(0, itemIndex);
    }


}