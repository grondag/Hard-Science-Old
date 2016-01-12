package grondag.adversity.niceblock.model;

import grondag.adversity.niceblock.NiceStyle.NiceStyleBasic;
import grondag.adversity.niceblock.NiceStyle.NiceStyleBigTex;
import grondag.adversity.niceblock.NiceSubstance;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent.Pre;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.client.model.ISmartItemModel;

public class NiceModelNew extends NiceModel {
	
	/**
	 * Controls model baking and selection via model cookbook.
	 */
	protected final NiceStyleBigTex style;


	public NiceModelNew(NiceStyleBigTex style, NiceSubstance substance, ModelResourceLocation mrlBlock, ModelResourceLocation mrlItem) {
		super(substance, mrlBlock, mrlItem);
		this.style = style;
	}

	@Override
	public IBakedModel handleItemState(ItemStack stack) {
		return null;
	}

	@Override
	public IBakedModel handleBlockState(IBlockState state) {
		return null;
	}

	@Override
	public List<BakedQuad> getFaceQuads(EnumFacing p_177551_1_) {
		return null;
	}

	@Override
	public List<BakedQuad> getGeneralQuads() {
		return null;
	}

	@Override
	public boolean isAmbientOcclusion() {
		return false;
	}

	@Override
	public boolean isGui3d() {
		return false;
	}

	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return null;
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return null;
	}

	@Override
	public void handleBakeEvent(ModelBakeEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleTextureStitchEvent(Pre event) {
		// TODO Auto-generated method stub
		
	}

}
