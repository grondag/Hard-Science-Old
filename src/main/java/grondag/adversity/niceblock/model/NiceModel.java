package grondag.adversity.niceblock.model;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.primitives.Ints;

import grondag.adversity.niceblock.NiceBlock;
import grondag.adversity.niceblock.NiceSubstance;
import grondag.adversity.niceblock.model.IModelController.Rotation;
import grondag.adversity.niceblock.model.NiceModelBigTex.Vertex;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent.Pre;
import net.minecraftforge.client.event.TextureStitchEvent.Post;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.common.property.IExtendedBlockState;

/**
 * Container for NiceBlock model variants retrieved by the block render
 * dispatcher. Places itself in the model registry during model bake and returns
 * appropriate baked models via handleBlockState and handleItemState
 *
 * All models are pre-baked during handleBakeEvent and kept in an array for
 * retrieval at run time. While this can mean many baked models are kept in
 * memory, they don't take up that much space and this ensures minimal
 * processing during the render loop.
 *
 * Each NiceBlock will have a separate NiceModel for each substance and each
 * NiceModel can contain one or hundreds of variants selected via
 * handleBlockState.
 *
 * NiceModel doesn't understand how extended state is determined, it just
 * expects to get a model recipe index and alternate index so that it can find
 * the correct array element.
 *
 * Does not subscribe to any events directly. Event handlers for texture stitch
 * and model bake need get/retain a reference to this instance and pass in the
 * events.
 *
 * Originally intended to have multiple NiceModel types but so far have only
 * needed this one because most of the functionality is delegated to the model
 * cook book.
 */
public abstract class NiceModel implements IBakedModel, ISmartBlockModel, ISmartItemModel {

	/**
	 * Provides texture parameters.
	 */
	protected final NiceSubstance substance;

	protected TextureAtlasSprite particleTexture;

	protected IFlexibleBakedModel itemModel;

	public abstract IModelController getController();
	
	/**
	 * Create a model for this style/substance combination. Caller will
	 * typically create 16 of these per NiceBlock instance if all 16 substance
	 * metadata values are used.
	 *
	 * See class header and member descriptions for more info on what things do.
	 */
	protected NiceModel(NiceSubstance substance) {
		this.substance = substance;
	}

	/**
	 * The function parameter to bake method presumably provides a way to do
	 * fancy texture management via inversion of control, but we don't need that
	 * because we register all our textures in texture bake. This function
	 * simply provides access to the vanilla texture maps.
	 */
	protected Function<ResourceLocation, TextureAtlasSprite> textureGetter = new Function<ResourceLocation, TextureAtlasSprite>(){
		@Override
		public TextureAtlasSprite apply(ResourceLocation location)
		{
			return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
		}
	};
	
	/**
	 * Bakes all the models for this style/substance and caches them in array.
	 * Should happen after texture stitch and before any models are retrieved
	 * with handleBlockState.
	 */
	public abstract void handleBakeEvent(ModelBakeEvent event);

	/**
	 * Registers all textures that will be needed for this style/substance.
	 * Happens before model bake.
	 */
	public void handleTexturePreStitch(Pre event){
		for(String tex : getController().getAllTextures(substance)){
			event.map.registerSprite(new ResourceLocation(tex));
		}
	}
	
	/**
	 * Override to lookup retained references to texture sprites.
	 * Happens before model bake but after texture atlas is created.
	 */
	public void handleTexturePostStitch(Post event){
		particleTexture = event.map.getAtlasSprite(getController().getFirstTextureName(substance));
	}
	
	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return itemModel.getItemCameraTransforms();
	}

	/**
	 * Does the heavy lifting for the ISmartBlockModel interface. Determines
	 * block state and returns the appropriate baked model. See class header for
	 * more info.
	 */
	@Override
	public IBakedModel handleBlockState(IBlockState state) {
		// Provide a default to contain the damage if we derp it up.
		IBakedModel retVal = itemModel;

		// Really should ALWAYS be a NiceBlock instance but if someone goes
		// mucking about with the model registry crazy stuff could happen.
		if (state instanceof IExtendedBlockState && state.getBlock() instanceof NiceBlock) {
			
			IExtendedBlockState exState = (IExtendedBlockState) state;
			EnumWorldBlockLayer layer = MinecraftForgeClient.getRenderLayer();
			ModelRenderState renderState = exState.getValue(NiceBlock.MODEL_RENDER_STATE);

			if(getController().canRenderInLayer(layer)){
				retVal = getModelVariant(renderState.variant1);
			}
		}
		
		// May not be strictly needed, but doing in case something important happens in some model types.
		if (retVal instanceof ISmartBlockModel) {
			return ((ISmartBlockModel) retVal).handleBlockState(state);
		}

		return retVal;
	}
	
	/**
	 * The logic for handleBlockState.  
	 * Separate and public to enable composite models.
	 */
	public abstract IBakedModel getModelVariant(int variantID);
	
	@Override
	public IBakedModel handleItemState(ItemStack stack) {
		return itemModel;
	}
	
	/**
	 * Used for block-breaking particles.
	 */
	@Override
	public TextureAtlasSprite getParticleTexture(){
		return particleTexture;
	}
	
	/**
	 * Should never be called because we provide a separate IFlexibleBakedModel instance in handleBlockState
	 */
	@Override
	public List getFaceQuads(EnumFacing p_177551_1_) {
		return null;
	}

	/**
	 * Should never be called because we provide a separate IFlexibleBakedModel instance in handleBlockState
	 */
	@Override
	public List getGeneralQuads() {
		return null;
	}

	/**
	 * Should never be called because we provide a separate IFlexibleBakedModel instance in handleBlockState
	 */
	@Override
	public boolean isAmbientOcclusion() {
		return true;
	}

	/**
	 * Should never be called because we provide a separate IFlexibleBakedModel instance in handleBlockState
	 */
	@Override
	public boolean isGui3d() { 
		return true;
	}


	/**
	 * Should never be called because we provide a separate IFlexibleBakedModel instance in handleBlockState
	 */
	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}
		
	// UTILITIES
	
	/**
	 * Rotates face texture 90deg clockwise
	 */
	protected static void rotateQuadUV(Vertex v1, Vertex v2, Vertex v3, Vertex v4){
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
	
	
	protected static class Vertex extends Vec3 {
		protected float u;
		protected float v;

		protected Vertex(float x, float y, float z, float u, float v) {
			super(x, y, z);
			this.u = u;
			this.v = v;
		}
	}
	
	protected BakedQuad createQuad(Vertex v1, Vertex v2, Vertex v3, Vertex v4, EnumFacing side, TextureAtlasSprite sprite, Rotation rotation, int colorIn) {

		float shade = LightUtil.diffuseLight(side);

		int red = (int) (shade * 255f * ((colorIn >> 16 & 0xFF) / 255f));
		int green = (int) (shade * 255f * ((colorIn >> 8 & 0xFF) / 255f));
		int blue = (int) (shade * 255f * ((colorIn & 0xFF) / 255f));
		int alpha = colorIn >> 24 & 0xFF;

		int colorOut = red | (green << 8) | (blue << 16) | (alpha << 24);

		for(int r= 0; r < rotation.index; r++){
			rotateQuadUV(v1, v2, v3, v4);
		}
		
		int[] aint = Ints.concat(
				vertexToInts(v1.xCoord, v1.yCoord, v1.zCoord, v1.u, v1.v, colorOut, sprite),
				vertexToInts(v2.xCoord, v2.yCoord, v2.zCoord, v2.u, v2.v, colorOut, sprite),
				vertexToInts(v3.xCoord, v3.yCoord, v3.zCoord, v3.u, v3.v, colorOut, sprite),
				vertexToInts(v4.xCoord, v4.yCoord, v4.zCoord, v4.u, v4.v, colorOut, sprite)
				);
		
		// necessary to support forge lighting model
		net.minecraftforge.client.ForgeHooksClient.fillNormal(aint, side);
		
		return new BakedQuad(aint,-1, side);

	}
	
	private int[] vertexToInts(double x, double y, double z, float u, float v, int color, TextureAtlasSprite sprite) {


		return new int[] {
				Float.floatToRawIntBits((float) x),
				Float.floatToRawIntBits((float) y),
				Float.floatToRawIntBits((float) z),
				color,
				Float.floatToRawIntBits(sprite.getInterpolatedU(u)),
				Float.floatToRawIntBits(sprite.getInterpolatedV(v)),
				0
		};
	}

}
