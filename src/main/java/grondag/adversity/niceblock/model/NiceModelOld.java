package grondag.adversity.niceblock.model;

import grondag.adversity.niceblock.NiceBlock;
import grondag.adversity.niceblock.NiceStyle;
import grondag.adversity.niceblock.NiceStyle.NiceStyleBasic;
import grondag.adversity.niceblock.NiceSubstance;
import grondag.adversity.niceblock.support.IExStateHandler;

import java.io.IOException;
import java.util.List;

import javax.vecmath.Vector3f;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IRetexturableModel;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.property.IExtendedBlockState;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

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
public class NiceModelOld extends NiceModel {


	/**
	 * Controls model baking and selection via model cookbook.
	 */
	protected final NiceStyleBasic style;

	/**
	 * Holds the baked models that will be returned for rendering based on
	 * extended state. Array is initialized during the handleBake event.
	 */
	protected final IFlexibleBakedModel[] primaryModels;
	
	/**
	 * Holds second-pass baked models that will be returned for rendering based on
	 * extended state for two-pass rendering. Array is initialized during the handleBake event.
	 */
	protected final IFlexibleBakedModel[] secondaryModels;
	
	protected IFlexibleBakedModel itemModel;

	/**
	 * Create a model for this style/substance combination. Caller will
	 * typically create 16 of these per NiceBlock instance if all 16 substance
	 * metadata values are used.
	 *
	 * See class header and member descriptions for more info on what things do.
	 */
	public NiceModelOld(NiceStyleBasic style, NiceSubstance substance, ModelResourceLocation mrlBlock, ModelResourceLocation mrlItem) {
		super(substance, mrlBlock, mrlItem);
		this.style = style;
		primaryModels = new IFlexibleBakedModel[style.firstCookbook.getAlternateCount() * style.firstCookbook.getRecipeCount()];
		
		if(style.secondCookbook != null){
			secondaryModels = new IFlexibleBakedModel[style.secondCookbook.getAlternateCount() * style.secondCookbook.getRecipeCount()];
		} else {
			secondaryModels = null;
		}
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

			if(layer == style.firstCookbook.getRenderLayer()){
				retVal = primaryModels[exState.getValue(NiceBlock.FIRST_MODEL_VARIANT)];
			} else if (style.secondCookbook != null && layer == style.secondCookbook.getRenderLayer()){
				retVal = secondaryModels[exState.getValue(NiceBlock.SECOND_MODEL_VARIANT)];
			}
		}
		
		
		// May not be strictly needed, but doing in case something important happens in some model types.
		if (retVal instanceof ISmartBlockModel) {
			return ((ISmartBlockModel) retVal).handleBlockState(state);
		}

		return retVal;

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
	 * Registers all textures that will be needed for this style/substance.
	 * Happens before model bake.
	 */
	public void handleTextureStitchEvent(TextureStitchEvent.Pre event) {
		for (int alt = 0; alt < style.firstCookbook.alternateCount; alt++) {
			for (int tex = 0; tex < style.firstCookbook.getTextureCount(); tex++) {
				event.map.registerSprite(new ResourceLocation(style.firstCookbook.getTextureName(substance, alt * style.firstCookbook.getTextureCount() + style.firstCookbook.textureIndex + tex)));
			}
		}
		
		if(style.secondCookbook != null){
			for (int alt = 0; alt < style.secondCookbook.alternateCount; alt++) {
				for (int tex = 0; tex < style.secondCookbook.getTextureCount(); tex++) {
					event.map.registerSprite(new ResourceLocation(style.secondCookbook.getTextureName(substance, alt * style.secondCookbook.getTextureCount() + style.secondCookbook.textureIndex + tex)));
				}
			}
		}
	}

	/**
	 * Bakes all the models for this style/substance and caches them in array.
	 * Should happen after texture stitch and before any models are retrieved
	 * with handleBlockState.
	 */
	public void handleBakeEvent(ModelBakeEvent event)  {
		for (int recipe = 0; recipe < style.firstCookbook.getRecipeCount(); recipe++) {
			for (int alt = 0; alt < style.firstCookbook.getAlternateCount(); alt++) {
				ModelCookbook.Ingredients ingredients = style.firstCookbook.getIngredients(substance, recipe, alt);
				IRetexturableModel template;
				try {
					template = (IRetexturableModel) event.modelLoader.getModel(new ResourceLocation(ingredients.modelName));
					IModel model = template.retexture(ingredients.textures);
					primaryModels[style.firstCookbook.calcVariantID(recipe, alt)] = model.bake(ingredients.state, DefaultVertexFormats.ITEM, textureGetter);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		if(style.secondCookbook != null){
			for (int recipe = 0; recipe < style.secondCookbook.getRecipeCount(); recipe++) {
				for (int alt = 0; alt < style.secondCookbook.getAlternateCount(); alt++) {
					ModelCookbook.Ingredients ingredients = style.secondCookbook.getIngredients(substance, recipe, alt);
					IRetexturableModel template;
					try {
						template = (IRetexturableModel) event.modelLoader.getModel(new ResourceLocation(ingredients.modelName));
						IModel model = template.retexture(ingredients.textures);
						secondaryModels[style.secondCookbook.calcVariantID(recipe, alt)] = model.bake(ingredients.state, DefaultVertexFormats.ITEM, textureGetter);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		/**
		 * Item model is the same as one of the block models, except that we need to handle perspective.
		 * All the models we use implement IPerspectiveAwareModel but because they aren't loaded via the
		 * standard loader they don't have an IModelState that includes the necessary key.
		 * 
		 * To fix this, we replace the normal block state with a SimpleModelState instance
		 * that includes a third-person perspective and the normal block state as the default.
		 * This is retained by the Bake method for that model type and then applied via handlePerspective in that model.
		 */
		
		ModelCookbook.Ingredients ingredients = style.firstCookbook.getIngredients(substance, style.firstCookbook.getItemModelIndex(), 0);
		IRetexturableModel template;
		try {
			template = (IRetexturableModel) event.modelLoader.getModel(new ResourceLocation(ingredients.modelName));
			IModel model = template.retexture(ingredients.textures);
			
			TRSRTransformation thirdperson = TRSRTransformation.blockCenterToCorner(new TRSRTransformation(
					new Vector3f(0, 1.5f / 16, -2.75f / 16),
					TRSRTransformation.quatFromYXZDegrees(new Vector3f(10, -45, 170)),
					new Vector3f(0.375f, 0.375f, 0.375f),
					null));

					itemModel = model.bake(new SimpleModelState(ImmutableMap.of(TransformType.THIRD_PERSON, thirdperson), Optional.of(ingredients.state)),
							DefaultVertexFormats.ITEM, textureGetter);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		event.modelRegistry.putObject(blockResourceLocation, this);
		event.modelRegistry.putObject(itemResourceLocation, this);
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

	/**
	 * Used for block-breaking particles.
	 */
	@Override
	public TextureAtlasSprite getParticleTexture() {
		// lazy lookup to ensure happens after texture atlas has been created
		if (particleTexture == null) {
			particleTexture = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(style.firstCookbook.getParticleTextureName(substance));
		}
		return particleTexture;
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return itemModel.getItemCameraTransforms();
	}

	@Override
	public IBakedModel handleItemState(ItemStack stack) {
		return itemModel;
	}



	@Override
	public IExStateHandler getExStateHandler() {
		return new IExStateHandler.ExStateHandlerCookbooks(style.firstCookbook, style.secondCookbook);
	}
	
	

}
