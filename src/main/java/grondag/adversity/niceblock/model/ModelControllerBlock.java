package grondag.adversity.niceblock.model;

import java.util.Map;

import com.google.common.collect.Maps;

import grondag.adversity.Adversity;
import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import grondag.adversity.niceblock.NiceStyle;
import grondag.adversity.niceblock.model.ModelCookbook.Ingredients;
import grondag.adversity.niceblock.newmodel.NiceBlock;
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
	
	
	public ModelControllerBlock(String textureName, int alternateCount, EnumWorldBlockLayer renderLayer, boolean isShaded, boolean useRotations){
		super(textureName, alternateCount, renderLayer, isShaded, useRotations);
	}
	
	public ModelControllerBlock(String textureName, int alternateCount){
		 this(textureName, alternateCount, EnumWorldBlockLayer.SOLID, true, true);
	}	
	
	@Override
	public NiceModel getModel(int meta) {
		return new NiceModelBlock(this, meta);
	}

}
