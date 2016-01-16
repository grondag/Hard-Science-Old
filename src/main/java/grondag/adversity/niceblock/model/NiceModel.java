package grondag.adversity.niceblock.model;

import java.util.List;

import com.google.common.base.Function;

import grondag.adversity.niceblock.NiceSubstance;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent.Pre;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.client.model.ISmartItemModel;
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

	public abstract ModelController getController();
	
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
	public abstract void handleTextureStitchEvent(Pre event);
	
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
	public abstract IBakedModel handleBlockState(IBlockState state);
	
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
	public abstract TextureAtlasSprite getParticleTexture();
	
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
		
}
