package grondag.adversity.niceblock.model;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.event.ModelBakeEvent;
import grondag.adversity.niceblock.NiceSubstance;
import grondag.adversity.niceblock.model.IModelController.Rotation;
import grondag.adversity.niceblock.model.NiceModel.Vertex;

public class NiceModelBorder extends NiceModel {
	
	protected final ModelControllerBorder controller;

	/**
	 * Dimensions are alternate and face ID.
	 */
	protected final List<BakedQuad>[][] faceQuads;

	/**
	 * Dimensions are alternate and face ID.
	 */
	protected final BorderFacade[] facadeModels;
	

	protected NiceModelBorder(NiceSubstance substance, ModelControllerBorder controller) {
		super(substance);
		this.controller = controller;
		faceQuads = new List[6][48];
		facadeModels = new BorderFacade[386];
	}

	@Override
	public IModelController getController() {
		return controller;
	}

	private List<BakedQuad> makeBorderFace(int textureOffset, Rotation rotation, boolean flipU, boolean flipV, EnumFacing face){
		float u0 = flipU ? 1 : 0;
		float v0 = flipV ? 1 : 0;
		float u1 = flipU ? 0 : 1;
		float v1 = flipV ? 0 : 1;
		
		TextureAtlasSprite textureSprite = 
				Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(controller.getTextureName(substance, textureOffset));

		List<BakedQuad> retval = Collections.EMPTY_LIST;
		
		switch(face){
		
		case UP:
			retval = new ImmutableList.Builder<BakedQuad>()
				.add(createQuad(
					new Vertex(0, 1, 0, u0, v0), new Vertex(0, 1, 1, u0, v1), new Vertex(1, 1, 1, u1, v1), new Vertex(1, 1, 0, u1, v0), 
					EnumFacing.UP, textureSprite, rotation, controller.color))
				.build();
	
		case DOWN:
			retval = new ImmutableList.Builder<BakedQuad>()
				.add(createQuad(
						new Vertex(1, 0, 1, u0, v1), new Vertex(0, 0, 1, u1, v1), new Vertex(0, 0, 0, u1, v0), new Vertex(1, 0, 0, u0, v0), 
					EnumFacing.DOWN, textureSprite, rotation, controller.color))
				.build();
	
		case WEST:
			retval = new ImmutableList.Builder<BakedQuad>()
				.add(createQuad(
					new Vertex(0, 0, 0, u0, v1), new Vertex(0, 0, 1, u1, v1), new Vertex(0, 1, 1, u1, v0), new Vertex(0, 1, 0, u0, v0), 
					EnumFacing.WEST, textureSprite, rotation, controller.color))
				.build();
	
		case EAST:
			retval = new ImmutableList.Builder<BakedQuad>()
				.add(createQuad(
					new Vertex(1, 0, 0, u1, v1), new Vertex(1, 1, 0, u1, v0), new Vertex(1, 1, 1, u0, v0), new Vertex(1, 0, 1, u0, v1),
					EnumFacing.EAST, textureSprite, rotation, controller.color))
				.build();
	
		case NORTH:
			retval = new ImmutableList.Builder<BakedQuad>()
				.add(createQuad(
					new Vertex(0, 0, 0, u1, v1), new Vertex(0, 1, 0, u1, v0), new Vertex(1, 1, 0, u0, v0), new Vertex(1, 0, 0, u0, v1),
					EnumFacing.NORTH, textureSprite, rotation, controller.color))
				.build();
	
		case SOUTH:
			retval = new ImmutableList.Builder<BakedQuad>()
				.add(createQuad(
					new Vertex(0, 0, 1, u0, v1), new Vertex(1, 0, 1, u1, v1), new Vertex(1, 1, 1, u1, v0), new Vertex(0, 1, 1, u0, v0), 
					EnumFacing.SOUTH, textureSprite, rotation, controller.color))
				.build();
		}
		
		return retval;
	}
	
	@Override
	public void handleBakeEvent(ModelBakeEvent event) {

		for(EnumFacing face: EnumFacing.values()){
			faceQuads[face.ordinal()][0] = makeBorderFace( 4, Rotation.ROTATE_NONE, false, false, face);
			faceQuads[face.ordinal()][1] = makeBorderFace( 3, Rotation.ROTATE_180, false, false, face);
			faceQuads[face.ordinal()][2] = makeBorderFace( 3, Rotation.ROTATE_270, false, false, face);
			faceQuads[face.ordinal()][3] = makeBorderFace( 1, Rotation.ROTATE_180, false, false, face);
			faceQuads[face.ordinal()][4] = makeBorderFace( 3, Rotation.ROTATE_NONE, false, false, face);
			faceQuads[face.ordinal()][5] = makeBorderFace( 2, Rotation.ROTATE_90, false, false, face);
			faceQuads[face.ordinal()][6] = makeBorderFace( 1, Rotation.ROTATE_270, false, false, face);
			faceQuads[face.ordinal()][7] = makeBorderFace( 0, Rotation.ROTATE_270, false, false, face);
			faceQuads[face.ordinal()][8] = makeBorderFace( 3, Rotation.ROTATE_90, false, false, face);
			faceQuads[face.ordinal()][9] = makeBorderFace( 1, Rotation.ROTATE_90, false, false, face);
			faceQuads[face.ordinal()][10] = makeBorderFace( 2, Rotation.ROTATE_NONE, false, false, face);
			faceQuads[face.ordinal()][11] = makeBorderFace( 0, Rotation.ROTATE_180, false, false, face);
			faceQuads[face.ordinal()][12] = makeBorderFace( 1, Rotation.ROTATE_NONE, false, false, face);
			faceQuads[face.ordinal()][13] = makeBorderFace( 0, Rotation.ROTATE_90, false, false, face);
			faceQuads[face.ordinal()][14] = makeBorderFace( 0, Rotation.ROTATE_NONE, false, false, face);
			faceQuads[face.ordinal()][15] = new ImmutableList.Builder<BakedQuad>().build(, face); // NO BORDER
			faceQuads[face.ordinal()][16] = makeBorderFace( 7, Rotation.ROTATE_180, false, false, face);
			faceQuads[face.ordinal()][17] = makeBorderFace( 7, Rotation.ROTATE_270, false, false, face);
			faceQuads[face.ordinal()][18] = makeBorderFace( 7, Rotation.ROTATE_NONE, false, false, face);
			faceQuads[face.ordinal()][19] = makeBorderFace( 7, Rotation.ROTATE_90, false, false, face);
			faceQuads[face.ordinal()][20] = makeBorderFace( 5, Rotation.ROTATE_270, false, false, face);
			faceQuads[face.ordinal()][21] = makeBorderFace( 5, Rotation.ROTATE_270, false, true, face);
			faceQuads[face.ordinal()][22] = makeBorderFace( 6, Rotation.ROTATE_270, false, false, face);
			faceQuads[face.ordinal()][23] = makeBorderFace( 5, Rotation.ROTATE_180, false, false, face);
			faceQuads[face.ordinal()][24] = makeBorderFace( 5, Rotation.ROTATE_180, true, false, face);
			faceQuads[face.ordinal()][25] = makeBorderFace( 6, Rotation.ROTATE_180, false, false, face);
			faceQuads[face.ordinal()][26] = makeBorderFace( 5, Rotation.ROTATE_90, false, false, face);
			faceQuads[face.ordinal()][27] = makeBorderFace( 5, Rotation.ROTATE_90, false, true, face);
			faceQuads[face.ordinal()][28] = makeBorderFace( 6, Rotation.ROTATE_90, false, false, face);
			faceQuads[face.ordinal()][29] = makeBorderFace( 5, Rotation.ROTATE_NONE, false, false, face);
			faceQuads[face.ordinal()][30] = makeBorderFace( 5, Rotation.ROTATE_NONE, true, false, face);
			faceQuads[face.ordinal()][31] = makeBorderFace( 6, Rotation.ROTATE_NONE, false, false, face);
			faceQuads[face.ordinal()][32] = new ImmutableList.Builder<BakedQuad>().build(, face); // NULL FACE
			faceQuads[face.ordinal()][33] = makeBorderFace( 8, Rotation.ROTATE_270, false, false, face);
			faceQuads[face.ordinal()][34] = makeBorderFace( 8, Rotation.ROTATE_NONE, false, false, face);
			faceQuads[face.ordinal()][35] = makeBorderFace( 9, Rotation.ROTATE_NONE, false, false, face);
			faceQuads[face.ordinal()][36] = makeBorderFace( 8, Rotation.ROTATE_90, false, false, face);
			faceQuads[face.ordinal()][37] = makeBorderFace( 10, Rotation.ROTATE_90, false, false, face);
			faceQuads[face.ordinal()][38] = makeBorderFace( 9, Rotation.ROTATE_90, false, false, face);
			faceQuads[face.ordinal()][39] = makeBorderFace( 11, Rotation.ROTATE_270, false, false, face);
			faceQuads[face.ordinal()][40] = makeBorderFace( 8, Rotation.ROTATE_180, false, false, face);
			faceQuads[face.ordinal()][41] = makeBorderFace( 9, Rotation.ROTATE_270, false, false, face);
			faceQuads[face.ordinal()][42] = makeBorderFace( 10, Rotation.ROTATE_NONE, false, false, face);
			faceQuads[face.ordinal()][43] = makeBorderFace( 11, Rotation.ROTATE_180, false, false, face);
			faceQuads[face.ordinal()][44] = makeBorderFace( 9, Rotation.ROTATE_180, false, false, face);
			faceQuads[face.ordinal()][45] = makeBorderFace( 11, Rotation.ROTATE_90, false, false, face);
			faceQuads[face.ordinal()][46] = makeBorderFace( 11, Rotation.ROTATE_NONE, false, false, face);
			faceQuads[face.ordinal()][47] = makeBorderFace( 12, Rotation.ROTATE_NONE, false, false, face);
		}
	}

	@Override
	public IBakedModel getModelVariant(int variantID) {
		return facadeModels[variantID];
	}
	
	private class BorderFacade implements IBakedModel {

		private final byte upFace;
		private final byte downFace;
		private final byte eastFace;
		private final byte westFace;
		private final byte northFace;
		private final byte southFace;
		
		public BorderFacade(int upFace, int downFace, int eastFace, int westFace, int northFace, int southFace) {
			this.upFace = (byte) upFace;
			this.downFace = (byte) downFace;
			this.eastFace = (byte) eastFace;
			this.westFace = (byte) westFace;
			this.northFace = (byte) northFace;
			this.southFace = (byte) southFace;
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
			return NiceModelBorder.this.getParticleTexture();
		}

		@Override
		public ItemCameraTransforms getItemCameraTransforms() {
			return itemModel.getItemCameraTransforms();
		}

	}

}
