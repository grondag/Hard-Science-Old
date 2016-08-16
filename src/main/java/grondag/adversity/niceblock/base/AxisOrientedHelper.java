package grondag.adversity.niceblock.base;

import java.util.List;

import grondag.adversity.library.joinstate.CornerJoinBlockStateSelector;
import grondag.adversity.niceblock.ColorHelperPlus;
import grondag.adversity.niceblock.modelstate.ModelState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.World;

public class AxisOrientedHelper extends ColorHelperPlus
{
    public AxisOrientedHelper(ModelDispatcher dispatcher)
    {
        super(dispatcher);
    }
 
    @Override
    public int getMetaForPlacedBlockFromStack(World worldIn, BlockPos posPlaced, BlockPos posOn, EnumFacing facing, ItemStack stack, EntityPlayer player)
    {
        return facing.getAxis().ordinal();
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
    }

    @Override
    public ModelState getModelStateForItemModel(int itemIndex)
    {
        ModelState modelState = new ModelState(itemIndex);
        modelState.setShapeIndex(CornerJoinBlockStateSelector.BLOCK_JOIN_STATE_COUNT, BlockRenderLayer.SOLID);
        modelState.setShapeIndex(CornerJoinBlockStateSelector.BLOCK_JOIN_STATE_COUNT, BlockRenderLayer.CUTOUT);
        modelState.setShapeIndex(CornerJoinBlockStateSelector.BLOCK_JOIN_STATE_COUNT, BlockRenderLayer.CUTOUT_MIPPED);
        modelState.setShapeIndex(CornerJoinBlockStateSelector.BLOCK_JOIN_STATE_COUNT, BlockRenderLayer.TRANSLUCENT);
        return modelState;
    }
    
    @Override 
    public boolean hasCustomBrightness()
    {
        // lamp blocks have two layers, and the inner (lamp) layer is solid
        return false;
  //      return this.dispatcher instanceof ModelDispatcherLayered && MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.SOLID;
    }
    
//    @Override
//    public int getCustomBrightness(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
//        return 15 << 20 | 15 << 4; 
//    }
}
