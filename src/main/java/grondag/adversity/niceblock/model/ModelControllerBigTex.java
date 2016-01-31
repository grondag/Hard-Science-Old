package grondag.adversity.niceblock.model;

import com.google.common.collect.ImmutableMap;

import grondag.adversity.Adversity;
import grondag.adversity.niceblock.model.IModelController.Rotation;
import grondag.adversity.niceblock.newmodel.NiceColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;

public class ModelControllerBigTex extends ModelController{

	protected final Rotation textureRotation;
	protected final boolean flipU;
	protected final boolean flipV;

	protected final NiceColor firstColor;

	public ModelControllerBigTex(String textureName, EnumWorldBlockLayer renderLayer, boolean isShaded, Rotation textureRotation, boolean flipU, boolean flipV, NiceColor firstColor) {
		super(textureName, 1, renderLayer, isShaded, false);
		this.firstColor = firstColor;
		this.textureRotation = textureRotation;
		this.flipU = flipU;
		this.flipV = flipV;
	}

	@Override
	public int getVariantID(IExtendedBlockState state, IBlockAccess worldIn, BlockPos pos) {
		return ((pos.getX() & 15) << 8) | ((pos.getY() & 15) << 4) | (pos.getZ() & 15);
	}

	@Override
	public NiceModel getModel(int meta) {
		return new NiceModelBigTex(this, meta, NiceColor.values()[firstColor.ordinal() + meta]);
	}

	protected String getTextureName(int offset) {
		return "adversity:blocks/" + textureName;
	}


}
