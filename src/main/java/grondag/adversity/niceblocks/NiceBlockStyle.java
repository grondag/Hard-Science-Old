package grondag.adversity.niceblocks;

import grondag.adversity.niceblocks.client.INiceCookbook;
import grondag.adversity.niceblocks.client.NiceModel;
import grondag.adversity.niceblocks.client.NiceModelCubeOne;
import grondag.adversity.niceblocks.client.NiceCookbooks;

public enum NiceBlockStyle {
	RAW("_a", 0, 1, 4, true, NiceCookbooks.simple, NiceModelCubeOne.class),
	SMOOTH("_a", 4, 1, 4, true, NiceCookbooks.simple, NiceModelCubeOne.class),
	//LARGE_BRICKS("B", 16, 1, 8, true, NiceCookbooks.simple, NiceModelSimple.class),
	//SMALL_BRICKS("B", 24, 1, 8, true, NiceCookbooks.simple, NiceModelSimple.class),
	BIG_WORN("_a", 16, 48, 3, true, NiceCookbooks.bigBlocks, NiceModelCubeOne.class),
	BIG_WEATHERED("_a", 160, 48, 1, true, NiceCookbooks.bigBlocks, NiceModelCubeOne.class),
	BIG_ORNATE("_a", 208, 48, 1, true, NiceCookbooks.bigBlocks, NiceModelCubeOne.class),
	MASONRY_A("_b", 0, 16, 1, true, NiceCookbooks.masonry, NiceModelCubeOne.class );
	
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
	public final INiceCookbook cookbook;
	
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
		boolean useRotationsAsAlternates,  INiceCookbook cookbook, Class<?> modelClass){
		this.textureSuffix = textureSuffix;
		this.textureIndex = textureIndex;
		this.textureCount = textureCount;
		this.alternateCount = alternateCount;
		this.useRotationsAsAlternates = useRotationsAsAlternates;
		this.cookbook = cookbook;
		this.modelClass = modelClass;		
	}
	
	/**
	 * This string is used to identify the model in the model registry
	 * and thus to associate block state with models.
	 * Use this value any place we need to identify a style/substance combination.
	 * 
	 * A few styles share the same appearance. 
	 * If we concatenate style and substance we'll end up with redundant models.
	 * So instead, construct identifier with model, substance and texture info.
	 * If separate styles use the same appearance info, they will reuse the same model.
	 * 
	 * @param substance
	 * @return
	 */
	public String getResourceLocationForSubstance(NiceSubstance substance ){
		return modelClass.toString() + substance.id + textureSuffix + textureIndex + "_" + textureCount;
	}
	
}
