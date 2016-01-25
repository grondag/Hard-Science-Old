package grondag.adversity.niceblock.model;

import grondag.adversity.niceblock.NiceBlock;
import grondag.adversity.niceblock.support.ICollisionHandler;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;

public class ModelControllerDual implements IModelController {

	public final ModelController controllerPrimary;
	public final ModelController controllerSecondary;
	
	public ModelControllerDual(ModelController primary, ModelController secondary) {
		this.controllerPrimary = primary;
		this.controllerSecondary = secondary;
	}

	@Override
	public NiceModel getModel(int meta) {
		return new NiceModelDual((NiceModel)controllerPrimary.getModel(meta),
				(NiceModel)controllerSecondary.getModel(meta));
	}

	@Override
	public ModelRenderState getRenderState(IExtendedBlockState state, IBlockAccess world, BlockPos pos) {
		return new ModelRenderState(
						controllerPrimary.getVariantID((IExtendedBlockState) state, world, pos),
						controllerSecondary.getVariantID((IExtendedBlockState) state, world, pos));
	}

	@Override
	public ICollisionHandler getCollisionHandler() {
		return controllerPrimary.getCollisionHandler();
	}

	@Override
	public boolean canRenderInLayer(EnumWorldBlockLayer layer) {
		return controllerPrimary.canRenderInLayer(layer) || controllerSecondary.canRenderInLayer(layer);
	}

	@Override
	public String getFirstTextureName(int meta) {
		return controllerPrimary.getFirstTextureName(meta);
	}

	@Override
	public String[] getAllTextures(int meta) {
		// TODO Auto-generated method stub
		return null;
	}

}
