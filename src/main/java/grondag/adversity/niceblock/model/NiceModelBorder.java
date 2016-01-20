package grondag.adversity.niceblock.model;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.event.ModelBakeEvent;
import grondag.adversity.niceblock.NiceSubstance;

public class NiceModelBorder extends NiceModel {
	
	protected final ModelControllerBorder controller;

	/**
	 * Holds the baked models that will be returned for rendering based on
	 * extended state. Array is populated during the handleBake event.
	 */
	protected final List<BakedQuad>[][] faceQuads;

	protected final BorderFacade[] facadeModels;

	protected TextureAtlasSprite textureSprite;

	protected NiceModelBorder(NiceSubstance substance, ModelControllerBorder controller) {
		super(substance);
		this.controller = controller;
		faceQuads = new List[6][256];
		facadeModels = new BorderFacade[4096];
	}

	@Override
	public IModelController getController() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleBakeEvent(ModelBakeEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IBakedModel getModelVariant(int variantID) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private class BorderFacade implements IBakedModel {

		private final short upFace;
		private final short downFace;
		private final short eastFace;
		private final short westFace;
		private final short northFace;
		private final short southFace;
		
		public BorderFacade(int upFace, int downFace, int eastFace, int westFace, int northFace, int southFace) {
			this.upFace = (short) upFace;
			this.downFace = (short) downFace;
			this.eastFace = (short) eastFace;
			this.westFace = (short) westFace;
			this.northFace = (short) northFace;
			this.southFace = (short) southFace;
		}

		@Override
		public List<BakedQuad> getFaceQuads(EnumFacing face) {

			switch (face) {
			case DOWN:
				return faceQuads[EnumFacing.DOWN.ordinal()][downFace];
			case UP:
				return faceQuads[EnumFacing.UP.ordinal()][upFace];
			case NORTH:
				return faceQuads[EnumFacing.NORTH.ordinal()][northFace];
			case SOUTH:
				return faceQuads[EnumFacing.SOUTH.ordinal()][southFace];
			case EAST:
				return faceQuads[EnumFacing.EAST.ordinal()][eastFace];
			case WEST:
				return faceQuads[EnumFacing.WEST.ordinal()][westFace];
			}

			return Collections.emptyList();
		}

		@Override
		public List<BakedQuad> getGeneralQuads() {
			return Collections.emptyList();
		}

		@Override
		public boolean isAmbientOcclusion() {
			return controller.isShaded;
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
			return textureSprite;
		}

		@Override
		public ItemCameraTransforms getItemCameraTransforms() {
			return itemModel.getItemCameraTransforms();
		}

	}

}
