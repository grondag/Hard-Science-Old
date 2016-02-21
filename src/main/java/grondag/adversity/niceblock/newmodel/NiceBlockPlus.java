package grondag.adversity.niceblock.newmodel;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import grondag.adversity.Adversity;
import grondag.adversity.niceblock.NiceStyle;
import grondag.adversity.niceblock.support.NicePlacement;

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
    public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos)
    {
        Item item = Item.getItemFromBlock(this);
        if (item == null)
        {
            return null;
        }

        IBlockState blockState = world.getBlockState(pos);
        ModelState modelState = blockModelHelper.getModelStateForBlock(blockState, world, pos, false);
        ItemStack retVal = new ItemStack(item, 1, modelState.getColorIndex());
 //       retVal.setTagCompound(modelState.getNBT());
        return retVal;
    }
 
}
