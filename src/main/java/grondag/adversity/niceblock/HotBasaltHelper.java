package grondag.adversity.niceblock;

import grondag.adversity.niceblock.base.BlockModelHelper;
import grondag.adversity.niceblock.base.ModelDispatcher;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.modelstate.ModelState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;

public class HotBasaltHelper extends BlockModelHelper
{

    public HotBasaltHelper(ModelDispatcher dispatcher)
    {
        super(dispatcher);
    }

    @Override
    public ModelState getModelStateForBlock(IBlockState state, IBlockAccess world, BlockPos pos, boolean doClientStateRefresh)
    {
        ModelState retVal = new ModelState(state.getValue(NiceBlock.META));
        if(doClientStateRefresh)
        {
            dispatcher.refreshClientShapeIndex(block, state, world, pos, retVal, true);
        }
        return retVal;
    }

    @Override
    public int getItemModelCount()
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
        return "" + I18n.translateToLocal(prefix) + " " + I18n.translateToLocal("basalt");
    }

    @Override
    public ModelState getModelStateForItemModel(int itemIndex)
    {
        return new ModelState().setShapeIndex(itemIndex * 16, BlockRenderLayer.TRANSLUCENT);
    }

    @Override 
    public boolean hasCustomBrightness()
    {
        return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT;
    }
    
    @Override
    public int getCustomBrightness(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return 15 << 20 | 15 << 4;
    }
    
    // necessary to offset the AO lighting effects of a fully lit translucent layer
    // values >= 15 are changed to 1 by rendering code
	public int getLightOpacity(IBlockState state) {
		return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT ? 0 : 255;
	}

	public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
		return getLightOpacity(state);
	}
}
