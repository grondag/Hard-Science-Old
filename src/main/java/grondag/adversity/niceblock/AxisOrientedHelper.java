package grondag.adversity.niceblock;

import java.util.List;

import grondag.adversity.library.joinstate.CornerJoinBlockStateSelector;
import grondag.adversity.niceblock.base.ModelDispatcher;
import grondag.adversity.niceblock.base.ModelState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;

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
        ModelState modelState = new ModelState(0, itemIndex);
        modelState.setClientShapeIndex(CornerJoinBlockStateSelector.BLOCK_JOIN_STATE_COUNT, BlockRenderLayer.SOLID.ordinal());
        modelState.setClientShapeIndex(CornerJoinBlockStateSelector.BLOCK_JOIN_STATE_COUNT, BlockRenderLayer.CUTOUT.ordinal());
        modelState.setClientShapeIndex(CornerJoinBlockStateSelector.BLOCK_JOIN_STATE_COUNT, BlockRenderLayer.CUTOUT_MIPPED.ordinal());
        modelState.setClientShapeIndex(CornerJoinBlockStateSelector.BLOCK_JOIN_STATE_COUNT, BlockRenderLayer.TRANSLUCENT.ordinal());
        return modelState;
    }
    
    @Override 
    public boolean hasCustomBrightness()
    {
        // lamp blocks have two layers, and the inner (lamp) layer is solid
        return this.dispatcher instanceof ModelDispatcherLayered && MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.SOLID;
    }
    
    @Override
    public int getCustomBrightness(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return 15 << 20 | 15 << 4; 
    }
}
