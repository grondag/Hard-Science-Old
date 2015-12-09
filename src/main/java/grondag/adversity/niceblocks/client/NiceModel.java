package grondag.adversity.niceblocks.client;

import grondag.adversity.niceblocks.NiceBlock;
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

	protected final ModelResourceLocation blockResourceLocation;
	protected final ModelResourceLocation itemResourceLocation;
	
	protected final NiceBlockStyle style;
	protected final NiceSubstance substance;
	
	protected final TextureAtlasSprite particleTexture;
	
	/** 
	 * Holds the baked models that will be returned for rendering based on extended state.
	 * Array is initialized during the handleBake event.
	 * Dimensions are alternate and recipe
	 */
	protected IFlexibleBakedModel[][] models; 

	
	public NiceModel(NiceBlockStyle style, NiceSubstance substance, ModelResourceLocation mrlBlock, ModelResourceLocation mrlItem){
		this.style = style;
		this.substance = substance;
		blockResourceLocation = mrlBlock;
		itemResourceLocation = mrlItem;
		this.models = new IFlexibleBakedModel[style.cookbook.getAlternateCount()][style.cookbook.getRecipeCount()];
		this.particleTexture = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(style.cookbook.getParticleTextureName(substance));
	}
	
	
	@Override
	public IBakedModel handleBlockState(IBlockState state) {
		
		// noted with interest...
		//	++    /**
		//++     * Queries if this block should render in a given layer.
		//++     * ISmartBlockModel can use MinecraftForgeClient.getRenderLayer to alter their model based on layer
		//++     */
		//++    public boolean canRenderInLayer(EnumWorldBlockLayer layer)
		//++    {
		//++        return func_180664_k() == layer;
		//++    }

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

			for(int alt = 0; alt < style.cookbook.getAlternateCount(); alt++){

				NiceCookbook.Ingredients ingredients = style.cookbook.getIngredients(substance, recipe, alt);

				IRetexturableModel template = (IRetexturableModel)event.modelLoader.getModel(new ModelResourceLocation(ingredients.modelName));

				IModel model=template.retexture(ingredients.textures);

				this.models[alt][recipe]=model.bake(ingredients.state, DefaultVertexFormats.BLOCK, textureGetter);
			}		
		}
		
		event.modelRegistry.putObject(blockResourceLocation, this);
		event.modelRegistry.putObject(itemResourceLocation, this);

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
		return true;
	}



	@Override
	public boolean isGui3d() {		// should never be called because we provide a separate IFlexibleBakedModel instance in handleBlockState
		return true;
	}



	@Override
	public boolean isBuiltInRenderer() {
		// should never be called because we provide a separate IFlexibleBakedModel instance in handleBlockState
		return false;
	}


	/**
	 * Used for block-breaking particles.
	 */
	@Override
	public TextureAtlasSprite getTexture() {
		return particleTexture;
	}


	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return ItemCameraTransforms.DEFAULT;
	}


	@Override
	public IBakedModel handleItemState(ItemStack stack) {
		return models[0][style.cookbook.getItemModelIndex()];
	}
}
