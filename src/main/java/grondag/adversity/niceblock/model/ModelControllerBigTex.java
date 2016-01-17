package grondag.adversity.niceblock.model;

import com.google.common.collect.ImmutableMap;

import grondag.adversity.niceblock.NiceSubstance;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;

public class ModelControllerBigTex extends ModelController{

	protected ModelControllerBigTex(int bigTextureIndex, boolean useOverlayTextures, EnumWorldBlockLayer renderLayer, boolean isShaded, boolean useRotations) {
		super(bigTextureIndex, 1, useOverlayTextures, renderLayer, isShaded, useRotations);
	}

	@Override
	public int getVariantID(IExtendedBlockState state, IBlockAccess worldIn, BlockPos pos) {
		return (pos.getX() & 15) << 8 | (pos.getY() & 15) << 4 | (pos.getZ() & 15);
	}

	@Override
	protected String getTextureName(NiceSubstance substance, int offset) {
		String textureName = this.useOverlayTextures && substance.overlayTexture != null
				? substance.overlayTexture : substance.baseTexture;

		int position = this.textureIndex + offset;
				
		return "adversity:blocks/" + textureName + "/" + textureName + "_big_" + position + "_0";
	}

	@Override
	public NiceModel getModel(NiceSubstance substance) {
		return new NiceModelBigTex(substance, this);
	}

}
