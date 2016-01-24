package grondag.adversity.niceblock.model;

import java.util.List;

import grondag.adversity.niceblock.NiceBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent.Post;
import net.minecraftforge.client.event.TextureStitchEvent.Pre;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.common.property.IExtendedBlockState;

public class NiceModelDual extends NiceModel {

	protected final NiceModel modelPrimary;
	protected final NiceModel modelSecondary;
	
	public NiceModelDual(NiceModel modelPrimary, NiceModel modelSecondary){
		super(modelPrimary.meta);
		this.modelPrimary = modelPrimary;
		this.modelSecondary = modelSecondary;
	}
	
	@Override
	public IBakedModel handleItemState(ItemStack stack) {
		return modelPrimary.handleItemState(stack);
	}

	@Override
	public IBakedModel handleBlockState(IBlockState state) {

		// Provide a default to contain the damage if we derp it up.
		IBakedModel retVal = modelPrimary.itemModel;

		// Really should ALWAYS be a NiceBlock instance but if someone goes
		// mucking about with the model registry crazy stuff could happen.
		if (state instanceof IExtendedBlockState && state.getBlock() instanceof NiceBlock) {
			
			IExtendedBlockState exState = (IExtendedBlockState) state;
			EnumWorldBlockLayer layer = MinecraftForgeClient.getRenderLayer();
			ModelRenderState renderState = exState.getValue(NiceBlock.MODEL_RENDER_STATE);

			if(modelPrimary.getController().canRenderInLayer(layer)){
				retVal = modelPrimary.getModelVariant(renderState.variant1);				
			} else if (modelSecondary.getController().canRenderInLayer(layer)){
				retVal = modelSecondary.getModelVariant(renderState.variant2);
			}
		}
		
		// May not be strictly needed, but doing in case something important happens in some model types.
		if (retVal instanceof ISmartBlockModel) {
			return ((ISmartBlockModel) retVal).handleBlockState(state);
		}

		return retVal;
	}

	@Override
	public List<BakedQuad> getFaceQuads(EnumFacing p_177551_1_) {
		return null;
	}

	@Override
	public List<BakedQuad> getGeneralQuads() {
		return null;
	}

	@Override
	public boolean isAmbientOcclusion() {
		return true;
	}

	@Override
	public boolean isGui3d() {
		return true;
	}

	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return modelPrimary.getParticleTexture();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return modelPrimary.getItemCameraTransforms();
	}

	@Override
	public void handleBakeEvent(ModelBakeEvent event) {
		modelPrimary.handleBakeEvent(event);
		modelSecondary.handleBakeEvent(event);
	}

	@Override
	public void handleTexturePreStitch(Pre event) {
		modelPrimary.handleTexturePreStitch(event);
		modelSecondary.handleTexturePreStitch(event);
	}

	@Override
	public void handleTexturePostStitch(Post event) {
		modelPrimary.handleTexturePostStitch(event);
		modelSecondary.handleTexturePostStitch(event);
	}

	/**
	 * Should not be called. Method is to support compound models like this class.
	 */
	@Override
	public ModelController getController() {
		return null;
	}

	/**
	 * Should not be called. Method is to support compound models like this class.
	 */
	@Override
	public IBakedModel getModelVariant(int variantID) {
		return null;
	}



}
