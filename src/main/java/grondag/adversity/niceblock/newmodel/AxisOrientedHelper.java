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
import net.minecraftforge.client.MinecraftForgeClient;

public class AxisOrientedHelper extends ColoredBlockHelperPlus
{
    public AxisOrientedHelper(ModelDispatcherBase dispatcher)
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
        modelState.setClientShapeIndex(0, EnumWorldBlockLayer.SOLID.ordinal());
        return modelState;
    }
    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean isFullBlock() {
        return false;
    }

    @Override
    public boolean isFullCube() {
        return false;
    }
    
    @Override 
    public boolean hasCustomBrightness()
    {
        // lamp blocks have two layers, and the inner (lamp) layer is solid
        return this.dispatcher instanceof ModelDispatcherLayered && MinecraftForgeClient.getRenderLayer() == EnumWorldBlockLayer.SOLID;
    }
    
    @Override
    public int getCustomBrightness(IBlockAccess worldIn, BlockPos pos) {
        return 15 << 20 | 15 << 4; 
    }
}
