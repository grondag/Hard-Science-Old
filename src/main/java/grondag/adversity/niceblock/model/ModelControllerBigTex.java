package grondag.adversity.niceblock.model;

import com.google.common.collect.ImmutableMap;

import grondag.adversity.Adversity;
import grondag.adversity.niceblock.NiceSubstance;
import grondag.adversity.niceblock.model.IModelController.Rotation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;

public class ModelControllerBigTex extends ModelController{

	protected final Rotation textureRotation;
	protected final boolean flipU;
	protected final boolean flipV;


	public ModelControllerBigTex(int bigTextureIndex, boolean useOverlayTextures, EnumWorldBlockLayer renderLayer, boolean isShaded, Rotation textureRotation, boolean flipU, boolean flipV) {
		super(bigTextureIndex, 1, useOverlayTextures, renderLayer, isShaded, false);
		this.textureRotation = textureRotation;
		this.flipU = flipU;
		this.flipV = flipV;
	}

	@Override
	public int getVariantID(IExtendedBlockState state, IBlockAccess worldIn, BlockPos pos) {
		return ((pos.getX() & 15) << 8) | ((pos.getY() & 15) << 4) | (pos.getZ() & 15);
	}

	@Override
	protected String getTextureName(NiceSubstance substance, int offset) {
//		String textureName = this.useOverlayTextures && substance.overlayTexture != null
//				? substance.overlayTexture : substance.baseTexture;

		int position = this.textureIndex + offset;
				
		return "adversity:blocks/bigtex_" + position + "_0";
		//return "adversity:blocks/bigstonetest";
	}

	@Override
	public NiceModel getModel(NiceSubstance substance) {
		return new NiceModelBigTex(substance, this);
	}

}
