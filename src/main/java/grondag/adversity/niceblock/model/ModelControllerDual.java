package grondag.adversity.niceblock.model;

import grondag.adversity.niceblock.NiceBlock;
import grondag.adversity.niceblock.NiceSubstance;
import grondag.adversity.niceblock.support.ICollisionHandler;
import net.minecraft.client.resources.model.ModelResourceLocation;
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
	public NiceModel getModel(NiceSubstance substance) {
		return new NiceModelDual((NiceModel)controllerPrimary.getModel(substance),
				(NiceModel)controllerSecondary.getModel(substance));
	}

	@Override
	public IExtendedBlockState getExtendedState(IExtendedBlockState state, IBlockAccess world, BlockPos pos) {
		return state.withProperty(
				NiceBlock.MODEL_RENDER_STATE, 
				new ModelRenderState(
						controllerPrimary.getVariantID((IExtendedBlockState) state, world, pos),
						controllerSecondary.getVariantID((IExtendedBlockState) state, world, pos)));
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
	public String getParticleTextureName(NiceSubstance substance) {
		return controllerPrimary.getParticleTextureName(substance);
	}

	@Override
	public int getColorMultiplier(NiceSubstance substance, IBlockAccess worldIn, BlockPos pos, int renderPass) {
		
		EnumWorldBlockLayer layer = MinecraftForgeClient.getRenderLayer();

		if(controllerPrimary.canRenderInLayer(layer)){
			return controllerPrimary.useOverlayTextures ? substance.overlayColor : substance.baseColor;			
		} else if (controllerSecondary.canRenderInLayer(layer)){
			return controllerSecondary.useOverlayTextures ? substance.overlayColor : substance.baseColor;
		} else return 16777215;
	}

}
