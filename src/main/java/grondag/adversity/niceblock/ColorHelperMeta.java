package grondag.adversity.niceblock;

import java.util.List;

import grondag.adversity.niceblock.base.BlockModelHelper;
import grondag.adversity.niceblock.base.ModelDispatcherBase;
import grondag.adversity.niceblock.base.ModelState;
import grondag.adversity.niceblock.base.NiceBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ColorHelperMeta extends BlockModelHelper
{
    public ColorHelperMeta(ModelDispatcherBase dispatcher)
    {
        super(dispatcher);
    }

    @Override
    public ModelState getModelStateForBlock(IBlockState state, IBlockAccess world, BlockPos pos, boolean doClientStateRefresh)
    {
        ModelState retVal = new ModelState(0, state.getValue(NiceBlock.META));
        if(doClientStateRefresh)
        {
            dispatcher.refreshClientShapeIndex(block, state, world, pos, retVal, true);
        }
        return retVal;
    }

    @Override
    public int getItemModelCount()
    {
        return dispatcher.getColorProvider().getColorCount();
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        super.addInformation(stack, playerIn, tooltip, advanced);
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