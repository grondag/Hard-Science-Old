package grondag.adversity.niceblock.model;

import java.util.Map;

import com.google.common.collect.Maps;

import grondag.adversity.Adversity;
import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import grondag.adversity.niceblock.NiceBlock;
import grondag.adversity.niceblock.NiceStyle;
import grondag.adversity.niceblock.NiceSubstance;
import grondag.adversity.niceblock.model.ModelCookbook.Ingredients;
import grondag.adversity.niceblock.support.ICollisionHandler;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;

public class ModelControllerBlock extends ModelController{

	/**
	 * Types
	 * 
	 * Same texture on all faces w/ alternates - based on position
	 * Textures specific to face w/ alternates - based on position
	 * Connected textures - no border - requires corner tests
	 * Connected textures - with border - requires conditional corner tests
	 * Big texture - based on position
	 * Compound controller - two layers
	 * 
	 * State
	 * Render layer
	 * int textureIndex, 
	 * int alternateCount, 
	 * boolean useSecondaryTextures, 
	 * EnumWorldBlockLayer renderLayer, 
	 * boolean isShaded, 
	 * boolean useRotations
	 * 
	 */
	
	
	public ModelControllerBlock(int textureIndex, int alternateCount, boolean useOverlayTextures, EnumWorldBlockLayer renderLayer, boolean isShaded, boolean useRotations){
		super(textureIndex, alternateCount, useOverlayTextures, renderLayer, isShaded, useRotations);
	}
	
	public ModelControllerBlock(int textureIndex, int alternateCount){
		 this(textureIndex, alternateCount, false, EnumWorldBlockLayer.SOLID, true, true);
	}	
	
	@Override
	public NiceModel getModel(NiceSubstance substance) {
		return new NiceModelBlock(substance, this);
	}

	@Override
	public IExtendedBlockState getExtendedState(IExtendedBlockState state, IBlockAccess world, BlockPos pos) {
		return state.withProperty(
				NiceBlock.MODEL_RENDER_STATE, 
				new ModelRenderState(getVariantID((IExtendedBlockState) state, world, pos), -1));
	}

}
