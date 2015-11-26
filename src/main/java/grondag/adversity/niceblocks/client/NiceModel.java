package grondag.adversity.niceblocks.client;

import grondag.adversity.niceblocks.NiceBlock;
import grondag.adversity.niceblocks.NiceBlock2;
import grondag.adversity.niceblocks.NiceBlockStyle;
import grondag.adversity.niceblocks.NiceSubstance;

import java.io.IOException;
import java.util.List;

import com.google.common.base.Function;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IRetexturableModel;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.common.property.IExtendedBlockState;

public class NiceModel implements IBakedModel, ISmartBlockModel, ISmartItemModel {

	protected final String myResourceLocation;
	
	protected final NiceBlockStyle style;
	protected final NiceSubstance substance;
	
	
	/** 
	 * Holds the baked models that will be returned for rendering based on extended state.
	 * Array is initialized during the handleBake event.
	 * Dimensions are alternate and recipe
	 */
	protected IFlexibleBakedModel[][] models; 

	
	NiceModel(NiceBlockStyle style, NiceSubstance substance){
		this.style = style;
		this.substance = substance;
		myResourceLocation = style.getResourceLocationForSubstance(substance);
		this.models = new IFlexibleBakedModel[style.cookbook.getAlternateCount()][style.cookbook.getRecipeCount()];
	}
	
	
	@Override
	public IBakedModel handleBlockState(IBlockState state) {

		Minecraft mc = Minecraft.getMinecraft();
		BlockRendererDispatcher blockRendererDispatcher = mc.getBlockRendererDispatcher();
		BlockModelShapes blockModelShapes = blockRendererDispatcher.getBlockModelShapes();
		IBakedModel retVal = blockModelShapes.getModelManager().getMissingModel();

		if (state instanceof IExtendedBlockState && state.getBlock() instanceof NiceBlock) {
			IExtendedBlockState exState = (IExtendedBlockState) state;
			retVal = models[exState.getValue(NiceBlock.PROP_ALTERNATE)][exState.getValue(NiceBlock.PROP_RECIPE)];
		}
		
		return retVal;
	}
	
	protected Function<ResourceLocation, TextureAtlasSprite> textureGetter = new Function<ResourceLocation, TextureAtlasSprite>()
	{
		@Override
		public TextureAtlasSprite apply(ResourceLocation location)
		{
			return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
		}
	};
	
	
	public void handleBakeEvent(ModelBakeEvent event) throws IOException
	{

		for(int recipe =0 ; recipe < style.cookbook.getRecipeCount() ; recipe++){

			for(int alt = 0; alt < style.alternateCount; alt++){

				NiceCookbook.Ingredients ingredients = style.cookbook.getIngredients(substance, recipe, alt);

				IRetexturableModel template = (IRetexturableModel)event.modelLoader.getModel(new ModelResourceLocation(ingredients.modelName));

				IModel model=template.retexture(ingredients.textures);

				this.models[alt][recipe]=model.bake(ingredients.state, DefaultVertexFormats.BLOCK, textureGetter);
			}		
		}
		
		event.modelRegistry.putObject(myResourceLocation, this);

	}
	
	
	public void handleTextureStitchEvent(TextureStitchEvent.Pre event){
		for (int alt = 0; alt < style.alternateCount; alt++){
			for (int tex = 0 ; tex < style.textureCount ; tex++){
				event.map.registerSprite(new ResourceLocation(style.buildTextureName(substance, (alt * style.textureCount) + style.textureIndex + tex)));
			}
		}
	}
	
	@Override
	public List getFaceQuads(EnumFacing p_177551_1_) {
		// should never be called because we provide a separate IFlexibleBakedModel instance in handleBlockState
		return null;
	}



	@Override
	public List getGeneralQuads() {
		// should never be called because we provide a separate IFlexibleBakedModel instance in handleBlockState
		return null;
	}



	@Override
	public boolean isAmbientOcclusion() {
		// should never be called because we provide a separate IFlexibleBakedModel instance in handleBlockState
		return false;
	}



	@Override
	public boolean isGui3d() {
		// should never be called because we provide a separate IFlexibleBakedModel instance in handleBlockState
		return false;
	}



	@Override
	public boolean isBuiltInRenderer() {
		// should never be called because we provide a separate IFlexibleBakedModel instance in handleBlockState
		return false;
	}



	@Override
	public TextureAtlasSprite getTexture() {
		// should never be called because we provide a separate IFlexibleBakedModel instance in handleBlockState
		return null;
	}



	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		// should never be called because we provide a separate IFlexibleBakedModel instance in handleBlockState
		return null;
	}


	@Override
	public IBakedModel handleItemState(ItemStack stack) {
		// should never be called because we provide a separate IFlexibleBakedModel instance in handleBlockState
		return null;
	}
}
