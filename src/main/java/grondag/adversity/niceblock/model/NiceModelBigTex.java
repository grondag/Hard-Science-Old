package grondag.adversity.niceblock.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent.Pre;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IModelState;
import net.minecraftforge.client.model.IRetexturableModel;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.common.property.IExtendedBlockState;
import grondag.adversity.Adversity;
import grondag.adversity.niceblock.NiceBlock;
import grondag.adversity.niceblock.NiceSubstance;

public class NiceModelBigTex extends NiceModel {

	protected final ModelControllerBigTex controller;
	
	/**
	 * Holds the baked models that will be returned for rendering based on
	 * extended state. Array is populated during the handleBake event.
	 */
	protected final List<BakedQuad>[][] faceQuads;

	protected final BigTexFacade[] facadeModels;
	
	protected TextureAtlasSprite textureSprite;
	
	protected TextureAtlasSprite getTextureSprite(){
		// lazy lookup to ensure happens after texture atlas has been created
		if (textureSprite == null) {
			textureSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(controller.getFirstTextureName(substance));
		}
		return textureSprite;
	}
	
	protected NiceModelBigTex(NiceSubstance substance, ModelControllerBigTex controller) {
		super(substance);
		this.controller = controller;
		faceQuads = new List[6][controller.useRotatedTexturesAsAlternates ? 256 : 64];
		facadeModels = new BigTexFacade[4096];
	}
	
	@Override
	public IModelController getController() {
		return controller;
	}
	
    private int[] vertexToInts(double x, double y, double z, float u, float v) {
        return new int[] {
                Float.floatToRawIntBits((float) x),
                Float.floatToRawIntBits((float) y),
                Float.floatToRawIntBits((float) z),
                -1,
                Float.floatToRawIntBits(getTextureSprite().getInterpolatedU(u)),
                Float.floatToRawIntBits(getTextureSprite().getInterpolatedV(v)),
                0
        };
    }

    private BakedQuad createQuad(Vertex v1, Vertex v2, Vertex v3, Vertex v4,  EnumFacing side) {
        return new BakedQuad(Ints.concat(
                vertexToInts(v1.x, v1.y, v1.z, v1.u, v1.v),
                vertexToInts(v2.x, v2.y, v2.z, v2.u, v2.v),
                vertexToInts(v3.x, v3.y, v3.z, v3.u, v3.v),
                vertexToInts(v4.x, v4.y, v4.z, v4.u, v4.v)
        ), -1, side);
    }
	
    private static class Vertex{
    	private final float x;
    	private final float y;
    	private final float z;
    	private final float u;
    	private final float v;
    	
    	public Vertex(float x, float y, float z, float u, float v){
    		this.x = x;
    		this.y = y;
    		this.z = z;
    		this.u = u;
    		this.v = v;
    	}
    }
    
    
	@Override
	public void handleBakeEvent(ModelBakeEvent event) {

		final float UVSTEP = 1 / 16;
		
		for(int i=0; i < 64; i++){
			
			float u0 = i & 15;
			float v0 = i >> 4;
			float u1 = u0 + UVSTEP;
			float v1 = v0 + UVSTEP;
			
			faceQuads[EnumFacing.UP.ordinal()][i] = 
					new ImmutableList.Builder<BakedQuad>()
					.add(createQuad(
							new Vertex(0,1,0,u0,v0), new Vertex(0,1,1,u0,v1), new Vertex(1,1,1,u1,v1), new Vertex(1,1,0,u1,v0), EnumFacing.UP))
					.build();
			
			faceQuads[EnumFacing.DOWN.ordinal()][i] = 
					new ImmutableList.Builder<BakedQuad>()
					.add(createQuad(
							new Vertex(0,0,0,u0,v0), new Vertex(0,0,1,u0,v1), new Vertex(1,0,1,u1,v1), new Vertex(1,0,0,u1,v0), EnumFacing.DOWN))
					.build();

			faceQuads[EnumFacing.EAST.ordinal()][i] = 
					new ImmutableList.Builder<BakedQuad>()
					.add(createQuad(
							new Vertex(1,0,0,u0,v0), new Vertex(1,0,1,u0,v1), new Vertex(1,1,1,u1,v1), new Vertex(1,1,0,u1,v0), EnumFacing.EAST))
					.build();

			faceQuads[EnumFacing.WEST.ordinal()][i] = 
					new ImmutableList.Builder<BakedQuad>()
					.add(createQuad(
							new Vertex(0,0,0,u0,v0), new Vertex(0,0,1,u0,v1), new Vertex(0,1,1,u1,v1), new Vertex(0,1,0,u1,v0), EnumFacing.WEST))
					.build();

			faceQuads[EnumFacing.NORTH.ordinal()][i] = 
					new ImmutableList.Builder<BakedQuad>()
					.add(createQuad(
							new Vertex(0,0,1,u0,v0), new Vertex(0,1,1,u0,v1), new Vertex(1,1,1,u1,v1), new Vertex(1,0,1,u1,v0), EnumFacing.NORTH))
					.build();

			faceQuads[EnumFacing.SOUTH.ordinal()][i] = 
					new ImmutableList.Builder<BakedQuad>()
					.add(createQuad(
							new Vertex(0,0,0,u0,v0), new Vertex(0,1,0,u0,v1), new Vertex(1,1,0,u1,v1), new Vertex(1,0,0,u1,v0), EnumFacing.SOUTH))
					.build();
		}
		
		for(int x = 0; x < 16; x++){
			for(int y = 0; y < 16; y++){
				for(int z = 0; z < 16; z++){
					facadeModels[x * 64 + y * 16 + z] = new BigTexFacade((y << 4 | z), (x << 4 | z), (x << 4 | y));
				}
			}
		}
	}

	@Override
	public IBakedModel getModelVariant(int variantID) {
		return facadeModels[variantID];
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return getTextureSprite();
	}

	private class BigTexFacade implements IBakedModel {
		
		protected final int faceSelector;
		
		public BigTexFacade(int xFace, int yFace, int zFace){
			faceSelector = (xFace & 15) << 8 | (yFace & 15) << 4 | (zFace & 15);
		}
		
		@Override
		public List<BakedQuad> getFaceQuads(EnumFacing face) {
			switch(face){
			case DOWN:
				return faceQuads[0][(faceSelector >> 4) & 15];
			case UP:
				return faceQuads[1][(faceSelector >> 4) & 15];
			case NORTH:
				return faceQuads[2][faceSelector & 15];
			case SOUTH:
				return faceQuads[3][faceSelector & 15];
			case EAST:
				return faceQuads[4][(faceSelector >> 8) & 15];
			case WEST:
				return faceQuads[5][(faceSelector >> 8) & 15];
			}
			return Collections.emptyList();
		}

		@Override
		public List<BakedQuad> getGeneralQuads() {
            return Collections.emptyList();
		}

		@Override
		public boolean isAmbientOcclusion() {
			return !controller.isShaded;
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
			return NiceModelBigTex.this.getTextureSprite();
		}

		@Override
		public ItemCameraTransforms getItemCameraTransforms() {
			return itemModel.getItemCameraTransforms();
		}
		
	}
	
}
