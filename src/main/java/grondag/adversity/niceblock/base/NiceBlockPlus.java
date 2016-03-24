package grondag.adversity.niceblock.base;

import grondag.adversity.niceblock.support.BaseMaterial;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class NiceBlockPlus extends NiceBlock implements ITileEntityProvider {

	public NiceBlockPlus(BlockModelHelper blockModelHelper, BaseMaterial material, String styleName)
	{
		super(blockModelHelper, material, styleName);
	}
		
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new NiceTileEntity();		
	}

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
    {
        Item item = Item.getItemFromBlock(this);
        if (item == null)
        {
            return null;
        }
        ItemStack stack = new ItemStack(item, 1, 0);
        
        IBlockState blockState = world.getBlockState(pos);
        ModelState modelState = blockModelHelper.getModelStateForBlock(blockState, world, pos, false);
        NiceTileEntity niceTE = (NiceTileEntity)world.getTileEntity(pos);
        if (niceTE != null) 
        {
            blockModelHelper.updateItemStackForPickBlock(stack, blockState, modelState, niceTE);
        }

        return stack;
    }
}
