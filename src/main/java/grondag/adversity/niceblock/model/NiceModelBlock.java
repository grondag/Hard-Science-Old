package grondag.adversity.niceblock.model;

import java.io.IOException;
import java.util.Map;

import javax.vecmath.Vector3f;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import grondag.adversity.Adversity;
import grondag.adversity.niceblock.NiceBlock;
import grondag.adversity.niceblock.model.ModelCookbook.Ingredients;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent.Pre;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IModelState;
import net.minecraftforge.client.model.IRetexturableModel;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;

public class NiceModelBlock extends NiceModel {
	
	protected final ModelControllerBlock controller;
	
	/**
	 * Holds the baked models that will be returned for rendering based on
	 * extended state. Array is populated during the handleBake event.
	 */
	protected final IBakedModel[] models;

	protected NiceModelBlock(ModelControllerBlock controller, int meta) {
		super(meta);
		this.controller = controller;
		models = new IFlexibleBakedModel[controller.expandedAlternateCount];
	}
	
	@Override
	public IBakedModel getModelVariant(int variantID){
		if (variantID >= 0 && variantID < controller.expandedAlternateCount) {
			return models[variantID];
		} else {		
			Adversity.log.warn("Array index " + variantID + " out of bounds in " + this.getClass() + ". Using item model to prevent crash." );
			return itemModel;
		}
	}


	private IFlexibleBakedModel getBakedModelForExpandedAlternate(ModelBakeEvent event, int expanded, IModelState state){
		
		String baseModelName = controller.isShaded ? "adversity:block/cube_rotate_all_" : "adversity:block/cube_no_shade_rotate_all_";
		
		IRetexturableModel template;
		try {
			template = (IRetexturableModel) event.modelLoader.getModel(new ResourceLocation(baseModelName + controller.calcRotation(expanded).degrees));
			IModel model = template.retexture(controller.getTexturesForExpandedAlternate(meta, expanded));
			return model.bake(state, DefaultVertexFormats.ITEM, textureGetter);
		} catch (IOException e) {
			Adversity.log.error("Unable to load model " + baseModelName + " in " + this.getClass(), e);
			return null;
		}
	}
	
	@Override
	public void handleBakeEvent(ModelBakeEvent event) {
		
		for(int expanded = 0; expanded < controller.expandedAlternateCount; expanded++){
				models[expanded] = getBakedModelForExpandedAlternate(event, expanded, TRSRTransformation.identity());
		}
		
		/**
		 * Item model is the same as the first block model, except that we need to handle perspective.
		 * All the models we use implement IPerspectiveAwareModel but because they aren't loaded via the
		 * standard loader they don't have an IModelState that includes the necessary key.
		 * 
		 * To fix this, we replace the normal block state with a SimpleModelState instance
		 * that includes a third-person perspective and the normal block state as the default.
		 * This is retained by the Bake method for that model type and then applied via handlePerspective in that model.
		 */
			
		TRSRTransformation thirdperson = TRSRTransformation.blockCenterToCorner(new TRSRTransformation(
				new Vector3f(0, 1.5f / 16, -2.75f / 16),
				TRSRTransformation.quatFromYXZDegrees(new Vector3f(10, -45, 170)),
				new Vector3f(0.375f, 0.375f, 0.375f),
				null));

		itemModel = getBakedModelForExpandedAlternate(event, 0, 
				new SimpleModelState(ImmutableMap.of(TransformType.THIRD_PERSON, thirdperson), Optional.of(TRSRTransformation.identity())));
		
	}

	@Override
	public ModelController getController() {
		return controller;
	}
}
