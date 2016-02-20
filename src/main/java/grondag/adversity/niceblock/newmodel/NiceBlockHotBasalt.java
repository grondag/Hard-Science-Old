package grondag.adversity.niceblock.newmodel;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import grondag.adversity.niceblock.NiceStyle;
import grondag.adversity.niceblock.support.NicePlacement;

public class NiceBlockHotBasalt extends NiceBlock {

	public NiceBlockHotBasalt(BlockModelHelper blockModelHelper) {
		super(blockModelHelper, BaseMaterial.FLEXSTONE, "hot_basalt");
	}

	@Override
	public int getMixedBrightnessForBlock(IBlockAccess worldIn, BlockPos pos) {
		if(MinecraftForgeClient.getRenderLayer() != EnumWorldBlockLayer.SOLID){
			return 0xFFFFFFFF; 
		} else {
			return super.getMixedBrightnessForBlock(worldIn, pos);
		}
	}
}
