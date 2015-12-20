package grondag.adversity.niceblocks;

import net.minecraft.util.EnumFacing.Axis;

public enum NiceBlockStyle {
	RAW("", 0, 1, 4, true, new NiceCookbook(), NiceModel.class, NiceBlock.LAYER_SOLID),
	SMOOTH("", 4, 1, 4, true, new NiceCookbook(), NiceModel.class, NiceBlock.LAYER_SOLID),
	LARGE_BRICKS("", 96, 6, 4, true, new NiceCookbook(), NiceModel.class, NiceBlock.LAYER_SOLID),
	SMALL_BRICKS("", 120, 6, 4, true, new NiceCookbook(), NiceModel.class, NiceBlock.LAYER_SOLID),
	BIG_WORN("", 256, 48, 3, true, new NiceCookbookConnectedCorners(), NiceModel.class, NiceBlock.LAYER_SOLID),
	BIG_WEATHERED("", 400, 48, 1, true, new NiceCookbookConnectedCorners(), NiceModel.class, NiceBlock.LAYER_SOLID),
	BIG_ORNATE("", 448, 48, 1, true, new NiceCookbookConnectedCorners(), NiceModel.class, NiceBlock.LAYER_SOLID),
	MASONRY_A("", 16, 16, 1, true, new NiceCookbookMasonry(), NiceModel.class, NiceBlock.LAYER_SOLID),
	MASONRY_B("", 32, 16, 1, true, new NiceCookbookMasonry(), NiceModel.class, NiceBlock.LAYER_SOLID),
	MASONRY_C("", 48, 16, 1, true, new NiceCookbookMasonry(), NiceModel.class, NiceBlock.LAYER_SOLID),
	MASONRY_D("", 64, 16, 1, true, new NiceCookbookMasonry(), NiceModel.class, NiceBlock.LAYER_SOLID),
	MASONRY_E("", 80, 16, 1, true, new NiceCookbookMasonry(), NiceModel.class, NiceBlock.LAYER_SOLID),
	COLUMN_SQUARE_X("", 8, 9, 1, false, new NiceCookbookColumnSquare(Axis.X), NiceModel.class, NiceBlock.LAYER_CUTOUT),
	COLUMN_SQUARE_Y("", 8, 9, 1, false, new NiceCookbookColumnSquare(Axis.Y), NiceModel.class, NiceBlock.LAYER_CUTOUT),
	COLUMN_SQUARE_Z("", 8, 9, 1, false, new NiceCookbookColumnSquare(Axis.Z), NiceModel.class, NiceBlock.LAYER_CUTOUT),
	COLUMN_ROUND_X("", 4, 1, 1, false, new NiceCookbookColumnRound(Axis.X), NiceModel.class, NiceBlock.LAYER_CUTOUT),
	COLUMN_ROUND_Y("", 4, 1, 1, false, new NiceCookbookColumnRound(Axis.Y), NiceModel.class, NiceBlock.LAYER_CUTOUT),
	COLUMN_ROUND_Z("", 4, 1, 1, false, new NiceCookbookColumnRound(Axis.Z), NiceModel.class, NiceBlock.LAYER_CUTOUT);

	/** convenience factory method */
	public static NicePlacement makeColumnPlacerSquare() {
		return new NicePlacement.PlacementColumn(NiceBlockStyle.COLUMN_SQUARE_X, NiceBlockStyle.COLUMN_SQUARE_Y,
				NiceBlockStyle.COLUMN_SQUARE_Z);
	}
	
	/** convenience factory method */
	public static NicePlacement makeColumnPlacerRound() {
		return new NicePlacement.PlacementColumn(NiceBlockStyle.COLUMN_ROUND_X, NiceBlockStyle.COLUMN_ROUND_Y,
				NiceBlockStyle.COLUMN_ROUND_Z);
	}

	/** convenience factory method */
	public static NicePlacement makeMasonryPlacer() {
		return new NicePlacement.PlacementMasonry(NiceBlockStyle.MASONRY_A, NiceBlockStyle.MASONRY_B,
				NiceBlockStyle.MASONRY_C, NiceBlockStyle.MASONRY_D, NiceBlockStyle.MASONRY_E);
	}

	/**
	 * Identifies which resource texture file group is used for this style
	 */
	public final String			textureSuffix;

	/**
	 * Index of the first texture to be used for this style. The model will
	 * assume all textures are offset from this index.
	 */
	public final int			textureIndex;

	/**
	 * Number of textures that should be loaded for each alternate So, the total
	 * number of textures would be textureCount * alternateCount.
	 */
	public final int			textureCount;

	/**
	 * How many versions of textures are provided in the atlas. (count includes
	 * the first texture) Does not include rotations.
	 */
	public final int			alternateCount;

	/**
	 * If true, textures on each face can be rotated. Block will use a cookbook
	 * to handle selection of texture recipes to match the rotations if the
	 * faces have a visible orientation.
	 */
	public final boolean		useRotationsAsAlternates;

	/**
	 * Identifies the texture cookbook that should be used for
	 * extendedBlockState.
	 */
	public final NiceCookbook	cookbook;

	/**
	 * Name of NiceModel class to use with this style.
	 */
	public final Class<?>		modelClass;

	/**
	 * Governs behavior of NiceBlock.canRenderInLayer() Allows for rendering in
	 * multiple layers.
	 */
	public final int			renderLayerFlags;

	/**
	 *
	 */
	NiceBlockStyle(String textureSuffix, int textureIndex, int textureCount, int alternateCount,
			boolean useRotationsAsAlternates, NiceCookbook cookbook, Class<?> modelClass, int renderLayerFlags) {
		this.textureSuffix = textureSuffix;
		this.textureIndex = textureIndex;
		this.textureCount = textureCount;
		this.alternateCount = alternateCount;
		this.useRotationsAsAlternates = useRotationsAsAlternates;
		this.modelClass = modelClass;
		cookbook.setStyle(this);
		this.cookbook = cookbook;
		this.renderLayerFlags = renderLayerFlags;
	}

	/**
	 * Generate the texture name associated with this style for a given
	 * substance and offset. Offsets are specific to a style. Cookbooks know
	 * which one to use for what purpose.
	 */
	public String buildTextureName(NiceSubstance substance, int offset) {
		String basename = substance.resourceName() + (textureSuffix == "" ? "" : "_") + textureSuffix;
		return "adversity:blocks/" + basename + "/" + basename + "_" + (offset >> 3) + "_" + (offset & 7);
	}
}
