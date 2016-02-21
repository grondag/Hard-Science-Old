package grondag.adversity.niceblock.newmodel;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.common.registry.LanguageRegistry;

public class HotBasaltHelper extends BlockModelHelper
{

    protected HotBasaltHelper(ModelDispatcherBase dispatcher)
    {
        super(dispatcher);
    }

    @Override
    public ModelState getModelStateForBlock(IBlockState state, IBlockAccess world, BlockPos pos, boolean doClientStateRefresh)
    {
        ModelState retVal = new ModelState();
        if(doClientStateRefresh)
        {
            dispatcher.refreshClientShapeIndex(block, state, world, pos, retVal);
        }
        return retVal;
    }

    @Override
    public int getSubItemCount()
    {
        return 4;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        String prefix;
        switch(stack.getMetadata())
        {
        case 0:
            prefix = "cooling";
            break;
        case 1:
            prefix = "warm";
            break;
        case 2:
            prefix = "hot";
            break;
        default:
            prefix = "very_hot";
        }
        return LanguageRegistry.instance().getStringLocalization(prefix)
                + " " + LanguageRegistry.instance().getStringLocalization("basalt");
    }

    @Override
    public ModelState getModelStateForItem(int itemIndex)
    {
        return new ModelState().setClientShapeIndex(itemIndex * 16, EnumWorldBlockLayer.TRANSLUCENT.ordinal());
    }

}
