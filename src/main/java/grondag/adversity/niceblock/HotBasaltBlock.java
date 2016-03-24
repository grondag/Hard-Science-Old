package grondag.adversity.niceblock;

import grondag.adversity.niceblock.base.BlockModelHelper;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.support.BaseMaterial;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.client.MinecraftForgeClient;

public class HotBasaltBlock extends NiceBlock {
	
    public HotBasaltBlock(BlockModelHelper blockModelHelper, BaseMaterial material, String styleName) {
		super(blockModelHelper, material, styleName);
	}

	// helps to prevent some weird lighting artifacts
    @Override
    public boolean isTranslucent(IBlockState state)
    {
    	return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT;
    }
}
