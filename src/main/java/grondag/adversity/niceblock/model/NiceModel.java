package grondag.adversity.niceblock.model;

import grondag.adversity.niceblock.NiceSubstance;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent.Pre;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.client.model.ISmartItemModel;

public abstract class NiceModel implements IBakedModel, ISmartBlockModel, ISmartItemModel {
	/**
	 * Identify THIS INSTANCE in the model registry. Needs to be consistent with
	 * block state mapping for the block/substance represented by this model.
	 * Does NOT identify the model(s) that will be returned by handleBlockState.
	 */
	protected final ModelResourceLocation blockResourceLocation;

	/**
	 * Same as blockResourceLocation but for the item.
	 */
	protected final ModelResourceLocation itemResourceLocation;

	/**
	 * Provides texture parameters.
	 */
	protected final NiceSubstance substance;

	protected TextureAtlasSprite particleTexture;

	protected IFlexibleBakedModel itemModel;

	/**
	 * Create a model for this style/substance combination. Caller will
	 * typically create 16 of these per NiceBlock instance if all 16 substance
	 * metadata values are used.
	 *
	 * See class header and member descriptions for more info on what things do.
	 */
	protected NiceModel(NiceSubstance substance, ModelResourceLocation mrlBlock, ModelResourceLocation mrlItem) {
		this.substance = substance;
		blockResourceLocation = mrlBlock;
		itemResourceLocation = mrlItem;
	}

	public abstract void handleBakeEvent(ModelBakeEvent event);

	public abstract void handleTextureStitchEvent(Pre event);
		
}
