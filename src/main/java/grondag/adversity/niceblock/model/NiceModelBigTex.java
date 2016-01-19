package grondag.adversity.niceblock.model;

import grondag.adversity.niceblock.NiceSubstance;
import grondag.adversity.niceblock.model.IModelController.Rotation;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent.Post;
import net.minecraftforge.client.event.TextureStitchEvent.Pre;
import net.minecraftforge.client.model.pipeline.LightUtil;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;

public class NiceModelBigTex extends NiceModel {

	protected final ModelControllerBigTex controller;

	/**
	 * Holds the baked models that will be returned for rendering based on
	 * extended state. Array is populated during the handleBake event.
	 */
	protected final List<BakedQuad>[][] faceQuads;

	protected final BigTexFacade[] facadeModels;

	protected TextureAtlasSprite textureSprite;

	/**
	 * Registers all textures that will be needed for this style/substance.
	 * Happens before model bake.
	 */
	@Override
	public void handleTexturePreStitch(Pre event) {
		event.map.registerSprite(new ResourceLocation(getController().getFirstTextureName(substance)));
	}

	@Override
	public void handleTexturePostStitch(Post event) {
		textureSprite = event.map.getAtlasSprite(getController().getFirstTextureName(substance));
		particleTexture = textureSprite;
	}

	protected NiceModelBigTex(NiceSubstance substance, ModelControllerBigTex controller) {
		super(substance);
		this.controller = controller;
		faceQuads = new List[6][256];
		facadeModels = new BigTexFacade[4096];
	}

	@Override
	public IModelController getController() {
		return controller;
	}

	private int[] vertexToInts(double x, double y, double z, float u, float v, int color) {


		return new int[] {
				Float.floatToRawIntBits((float) x),
				Float.floatToRawIntBits((float) y),
				Float.floatToRawIntBits((float) z),
				color,
				Float.floatToRawIntBits(textureSprite.getInterpolatedU(u)),
				Float.floatToRawIntBits(textureSprite.getInterpolatedV(v)),
				0
		};
	}

	private BakedQuad createQuad(Vertex v1, Vertex v2, Vertex v3, Vertex v4, EnumFacing side) {

		float shade = LightUtil.diffuseLight(side);

		int red = (int) (shade * 255f * ((controller.color >> 16 & 0xFF) / 255f));
		int green = (int) (shade * 255f * ((controller.color >> 8 & 0xFF) / 255f));
		int blue = (int) (shade * 255f * ((controller.color & 0xFF) / 255f));
		int alpha = controller.color >> 24 & 0xFF;

		int color = red | (green << 8) | (blue << 16) | (alpha << 24);

		for(int r= 0; r < this.controller.textureRotation.index; r++){
			rotateQuadUV(v1, v2, v3, v4);
		}
		
		int[] aint = Ints.concat(
				vertexToInts(v1.xCoord, v1.yCoord, v1.zCoord, v1.u, v1.v, color),
				vertexToInts(v2.xCoord, v2.yCoord, v2.zCoord, v2.u, v2.v, color),
				vertexToInts(v3.xCoord, v3.yCoord, v3.zCoord, v3.u, v3.v, color),
				vertexToInts(v4.xCoord, v4.yCoord, v4.zCoord, v4.u, v4.v, color)
				);
		
		// necessary to support forge lighting model
		net.minecraftforge.client.ForgeHooksClient.fillNormal(aint, side);
		
		return new BakedQuad(aint,-1, side);

	}

	/**
	 * Rotates face texture 90deg clockwise
	 */
	private void rotateQuadUV(Vertex v1, Vertex v2, Vertex v3, Vertex v4){
		float swapU = v1.u;
		float swapV = v1.v;
		v1.u = v2.u;
		v1.v = v2.v;
		v2.u = v3.u;
		v2.v = v3.v;
		v3.u = v4.u;
		v3.v = v4.v;
		v4.u = swapU;
		v4.v = swapV;
	}
	
	
	private static class Vertex extends Vec3 {
		private float u;
		private float v;

		public Vertex(float x, float y, float z, float u, float v) {
			super(x, y, z);
			this.u = u;
			this.v = v;
		}
	}

	@Override
	public void handleBakeEvent(ModelBakeEvent event) {

		textureSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(getController().getFirstTextureName(substance));

		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {

				int index = i << 4 | j;
				float u0 = controller.flipU ? 16-i : i;
				float v0 = controller.flipV ? 16-j : j;
				float u1 = u0 + (controller.flipU ? -1 : 1);
				float v1 = v0 + (controller.flipV ? -1 : 1);

				faceQuads[EnumFacing.UP.ordinal()][index] =
						new ImmutableList.Builder<BakedQuad>()
						.add(createQuad(
										new Vertex(0, 1, 0, u0, v0), new Vertex(0, 1, 1, u0, v1), new Vertex(1, 1, 1, u1, v1), new Vertex(1, 1, 0, u1, v0), EnumFacing.UP))
								.build();

				faceQuads[EnumFacing.DOWN.ordinal()][index] =
						new ImmutableList.Builder<BakedQuad>()
						.add(createQuad(
										new Vertex(1, 0, 1, u0, v1), new Vertex(0, 0, 1, u1, v1), new Vertex(0, 0, 0, u1, v0), new Vertex(1, 0, 0, u0, v0), EnumFacing.DOWN))
								.build();

				faceQuads[EnumFacing.WEST.ordinal()][index] =
						new ImmutableList.Builder<BakedQuad>()
								.add(createQuad(
								new Vertex(0, 0, 0, u0, v1), new Vertex(0, 0, 1, u1, v1), new Vertex(0, 1, 1, u1, v0), new Vertex(0, 1, 0, u0, v0), EnumFacing.WEST))
								.build();

				faceQuads[EnumFacing.EAST.ordinal()][index] =
						new ImmutableList.Builder<BakedQuad>()
								.add(createQuad(
										new Vertex(1, 0, 0, u1, v1), new Vertex(1, 1, 0, u1, v0), new Vertex(1, 1, 1, u0, v0), new Vertex(1, 0, 1, u0, v1), EnumFacing.EAST))
								.build();

				faceQuads[EnumFacing.NORTH.ordinal()][index] =
						new ImmutableList.Builder<BakedQuad>()
								.add(createQuad(
								new Vertex(0, 0, 0, u1, v1), new Vertex(0, 1, 0, u1, v0), new Vertex(1, 1, 0, u0, v0), new Vertex(1, 0, 0, u0, v1), EnumFacing.NORTH))
								.build();

				faceQuads[EnumFacing.SOUTH.ordinal()][index] =
						new ImmutableList.Builder<BakedQuad>()
								.add(createQuad(
								new Vertex(0, 0, 1, u0, v1), new Vertex(1, 0, 1, u1, v1), new Vertex(1, 1, 1, u1, v0), new Vertex(0, 1, 1, u0, v0), EnumFacing.SOUTH))
								.build();
			}
		}

		int xOff = 0;
		for (int x = 0; x < 16; x++) {

			int yOff = 0;
			for (int y = 0; y < 16; y++) {
				int zOff = 0;
				for (int z = 0; z < 16; z++) {
					
					// Really can't be null because all cases handled.
					// Initializing to silence eclipse warning.
					BigTexFacade facade = null;
					
					// clockwise rotations
					switch(this.controller.textureRotation){
					case ROTATE_NONE:
						facade = new BigTexFacade(
								(((x + yOff) & 0xF) << 4) | ((z + yOff) & 0xF), 
								((~(x + yOff) & 0xF) << 4) | ((z + yOff) & 0xF), 
								(~(y + xOff)  & 0xF) | ((~(z + xOff) & 0xF) << 4), 
								(~(y + xOff)  & 0xF) | (((z + xOff) & 0xF)  << 4),
								((~(x + zOff) & 0xF) << 4) | (~(y + zOff) & 0xF),
								(((x + zOff) & 0xF) << 4) | (~(y + zOff) & 0xF));
						break;
					case ROTATE_90:
						 facade = new BigTexFacade(
								(((z + yOff) & 0xF) << 4) | (~(x + yOff) & 0xF), 
								(((z + yOff) & 0xF) << 4) | ((x + yOff) & 0xF), 
								((z + xOff)  & 0xF) | ((~(y + xOff) & 0xF) << 4), 
								(~(z + xOff)  & 0xF) | ((~(y + xOff) & 0xF)  << 4),
								((~(y + zOff) & 0xF) << 4) | ((x + zOff) & 0xF),
								((~(y + zOff) & 0xF) << 4) | (~(x + zOff) & 0xF));
						break;
					case ROTATE_180:
						 facade = new BigTexFacade(
								((~(x + yOff) & 0xF) << 4) | (~(z + yOff) & 0xF), 
								(((x + yOff) & 0xF) << 4) | (~(z + yOff) & 0xF), 
								((y + xOff)  & 0xF) | (((z + xOff) & 0xF) << 4), 
								((y + xOff)  & 0xF) | ((~(z + xOff) & 0xF)  << 4),
								(((x + zOff) & 0xF) << 4) | ((y + zOff) & 0xF),
								((~(x + zOff) & 0xF) << 4) | ((y + zOff) & 0xF));
						break;
					case ROTATE_270:
						facade = new BigTexFacade(
								((~(z + yOff) & 0xF) << 4) | ((x + yOff) & 0xF), 
								((~(z + yOff) & 0xF) << 4) | (~(x + yOff) & 0xF), 
								(~(z + xOff)  & 0xF) | (((y + xOff) & 0xF) << 4), 
								((z + xOff)  & 0xF) | (((y + xOff) & 0xF)  << 4),
								(((y + zOff) & 0xF) << 4) | (~(x + zOff) & 0xF),
								(((y + zOff) & 0xF) << 4) | ((x + zOff) & 0xF));
						break;
					}
					
					facadeModels[(x << 8) | (y << 4) | z] = facade;
							
					zOff += 7;
				}
				yOff +=7;
			}
			xOff +=7;
		}
	}

	@Override
	public IBakedModel getModelVariant(int variantID) {
		return facadeModels[variantID];
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return textureSprite;
	}

	private class BigTexFacade implements IBakedModel {

		private final short upFace;
		private final short downFace;
		private final short eastFace;
		private final short westFace;
		private final short northFace;
		private final short southFace;
		
		public BigTexFacade(int upFace, int downFace, int eastFace, int westFace, int northFace, int southFace) {
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
