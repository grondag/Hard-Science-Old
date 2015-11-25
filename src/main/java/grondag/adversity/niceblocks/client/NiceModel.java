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
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.common.property.IExtendedBlockState;

public abstract class NiceModel implements IBakedModel, ISmartBlockModel, ISmartItemModel {

	protected final String myResourceLocation;
	protected final String textureBaseName;
	
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
		this.textureBaseName = substance.resourceName() + "_" + style.textureSuffix;;
		this.models = new IFlexibleBakedModel[style.alternateCount * (style.useRotationsAsAlternates ? 4 : 1)][style.cookbook.getRecipeCount()];
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
	
	
	/**
	 * This method MUST be overridden to create the necessary baked models
	 *
	 */
	public void handleBakeEvent(ModelBakeEvent event) throws IOException
	{
		event.modelRegistry.putObject(myResourceLocation, this);
		//TODO: something with items?
		//event.modelRegistry.putObject(ClientProxy.itemLocation, customModel);

	}
	
	public void handleTextureStitchEvent(TextureStitchEvent.Pre event){
		for (int alt = 0; alt < style.alternateCount; alt++){
			for (int tex = 0 ; tex < style.textureCount ; tex++){
				event.map.registerSprite(new ResourceLocation(buildTextureName(textureBaseName, (alt * style.textureCount) + style.textureIndex + tex)));
			}
		}
	}
	
	protected static String buildTextureName(String basename, int offset){
		return "adversity:blocks/" + basename + "/" + basename + "_" + (offset >> 3) + "_" + (offset & 7);
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
