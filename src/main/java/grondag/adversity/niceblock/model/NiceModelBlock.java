package grondag.adversity.niceblock.model;

import java.io.IOException;
import java.util.List;
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
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
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
	
	protected IBakedModel itemModel;
	
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
			Adversity.log.warn("Array index " + variantID + " out of bounds in " + this.getClass() + ". Using missing model to prevent crash." );
			return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel();
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
		 * For overlay models, bump out a bit to prevent depth fighting.
		 */
		if(getController().renderLayer == EnumWorldBlockLayer.SOLID)
		{
		    itemModel = models[0];
		} 
		else
		{
    		TRSRTransformation bumpOut = TRSRTransformation.blockCenterToCorner(new TRSRTransformation(
    				new Vector3f(0, 0, 0),
    				null,
    				new Vector3f(1.0002f, 1.00021f, 1.0002f),
    				null));
    
    		itemModel = getBakedModelForExpandedAlternate(event, 0, bumpOut);
		}
		
	}

	@Override
	public ModelController getController() {
		return controller;
	}

    @Override
    protected List<BakedQuad> getItemQuads()
    {
        return NiceModel.getItemQuadsFromModel(itemModel);
    }
}
