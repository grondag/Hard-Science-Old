package grondag.adversity.niceblock.newmodel;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import grondag.adversity.Adversity;
import grondag.adversity.niceblock.NiceStyle;
import grondag.adversity.niceblock.support.NicePlacement;

public class NiceBlockPlus extends NiceBlock implements ITileEntityProvider {

	public NiceBlockPlus(BlockModelHelper blockModelHelper, BaseMaterial material)
	{
		super(blockModelHelper, material);
	}
		
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new NiceTileEntity();		
	}


}
