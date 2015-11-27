package grondag.adversity.niceblocks;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import net.minecraft.util.EnumFacing.Axis;
import grondag.adversity.Adversity;
import grondag.adversity.niceblocks.client.NiceCookbook;
import grondag.adversity.niceblocks.client.NiceCookbookColumn;
import grondag.adversity.niceblocks.client.NiceCookbookConnectedCorners;
import grondag.adversity.niceblocks.client.NiceCookbookMasonry;
import grondag.adversity.niceblocks.client.NiceModel;

public enum NiceBlockStyle {
	RAW("a", 0, 1, 4, true, new NiceCookbook(), NiceModel.class),
	SMOOTH("a", 4, 1, 4, true, new NiceCookbook(), NiceModel.class),
	//LARGE_BRICKS("B", 16, 1, 8, true, new NiceCookbook(), NiceModel.class),
	//SMALL_BRICKS("B", 24, 1, 8, true, new NiceCookbook(), NiceModel.class),
	BIG_WORN("a", 16, 48, 3, true, new NiceCookbookConnectedCorners(), NiceModel.class),
	BIG_WEATHERED("a", 160, 48, 1, true, new NiceCookbookConnectedCorners(), NiceModel.class),
	BIG_ORNATE("a", 208, 48, 1, true, new NiceCookbookConnectedCorners(), NiceModel.class),
	MASONRY_A("b", 0, 16, 1, true, new NiceCookbookMasonry(), NiceModel.class),
	COLUMN_X("c", 0, 9, 1, false, new NiceCookbookColumn(Axis.X), NiceModel.class),
	COLUMN_Y("c", 0, 9, 1, false, new NiceCookbookColumn(Axis.Y), NiceModel.class),
	COLUMN_Z("c", 0, 9, 1, false, new NiceCookbookColumn(Axis.Z), NiceModel.class);
	
	/** 
	 * 	Identifies which resource texture file group is used for this style
	 */
	public final String textureSuffix;
	
	/** 
	 * Index of the first texture to be used for this style.
	 * 	The model will assume all textures are offset from this index.
	 */
	public final int textureIndex;
	
	/**
	 * Number of textures that should be loaded for each alternate
	 * So, the total number of textures would be textureCount * alternateCount.
	 */
	public final int textureCount;
	
	/** 
	 * How many versions of textures are provided in the atlas.
	 * (count includes the first texture)
	 * Does not include rotations.
	 */
	public final int alternateCount;
	
	
	/**
	 * If true, textures on each face can be rotated.
	 * Block will use a cookbook to handle selection of texture recipes
	 * to match the rotations if the faces have a visible orientation.
	 */
	public final boolean useRotationsAsAlternates; 
	
	/**
	 * Identifies the texture cookbook that should be used for extendedBlockState.
	 */
	public final NiceCookbook cookbook;
	
	/**
	 * Name of NiceModel class to use with this style.
	 */
	public final Class<?> modelClass;
	
	/**
	 * 
	 * @param textureSuffix
	 * @param textureIndex
	 * @param textureCount
	 * @param alternateCount
	 * @param useRotationsAsAlternates
	 * @param model
	 */
	NiceBlockStyle(String textureSuffix, int textureIndex, int textureCount, int alternateCount, 
		boolean useRotationsAsAlternates,  NiceCookbook cookbook, Class<?> modelClass){
		this.textureSuffix = textureSuffix;
		this.textureIndex = textureIndex;
		this.textureCount = textureCount;
		this.alternateCount = alternateCount;
		this.useRotationsAsAlternates = useRotationsAsAlternates;
		this.modelClass = modelClass;
		cookbook.setStyle(this);
		this.cookbook = cookbook;
	}
	
	
	/**
	 * Used to identify the model in the model registry
	 * and thus to associate block state with models.
	 * Use this value any place we need to identify a style/substance combination.
	 */
	public String getResourceLocationForSubstance(NiceSubstance substance ){
		return this.toString().toLowerCase() + "_" + substance.id;
	}
	
	
	/** 
	 * Generate the texture name associated with this style for a given substance and offset.
	 * Offsets are specific to a style. Cookbooks know which one to use for what purpose.
	 */
	public String buildTextureName(NiceSubstance substance, int offset){
		String basename = substance.resourceName() + "_" + this.textureSuffix;
		return "adversity:blocks/" + basename + "/" + basename + "_" + (offset >> 3) + "_" + (offset & 7);
	}
}
