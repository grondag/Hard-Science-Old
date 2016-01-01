package grondag.adversity.niceblock;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import grondag.adversity.niceblock.support.NicePlacement;

public class NiceBlockHotBasalt extends NiceBlock {

	public NiceBlockHotBasalt(String name, NiceStyle style, NicePlacement placer, NiceSubstance[] substances) {
		super(name, style, placer, substances);
	}

	@Override
	public int getMixedBrightnessForBlock(IBlockAccess worldIn, BlockPos pos) {
		if(MinecraftForgeClient.getRenderLayer() == EnumWorldBlockLayer.SOLID){
			return 0xFFFFFFFF; 
		} else {
			return super.getMixedBrightnessForBlock(worldIn, pos);
		}
	}
}
