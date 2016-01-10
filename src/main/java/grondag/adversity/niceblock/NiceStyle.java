package grondag.adversity.niceblock;

import grondag.adversity.niceblock.model.ModelCookbook;
import grondag.adversity.niceblock.model.ModelCookbookColumnRound;
import grondag.adversity.niceblock.model.ModelCookbookColumnSquare;
import grondag.adversity.niceblock.model.ModelCookbookConnectedCorners;
import grondag.adversity.niceblock.model.ModelCookbookMasonry;
import grondag.adversity.niceblock.model.NiceModel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;

/**
 * Style governs the visual appearance and some behavior of a NiceBlock.
 * A NiceBlock can have only a single style, but multiple Substances.
 * Some styles are visually similar or identical but have distinct identities
 * to allow multi-blocks with connected textures that are adjacent to each other.
 * 
 */
public enum NiceStyle {
	RAW(new ModelCookbook(0, 4), null),
	SMOOTH(new ModelCookbook(4, 4), null),
	LARGE_BRICKS(new ModelCookbook(96, 4), null),
	SMALL_BRICKS(new ModelCookbook(120, 4), null),
	BIG_WORN(new ModelCookbookConnectedCorners(256, 3), null),
	BIG_WEATHERED(new ModelCookbookConnectedCorners(400, 1), null),
	BIG_ORNATE(new ModelCookbookConnectedCorners(448, 1), null),
	MASONRY_A(new ModelCookbookMasonry(16, 1), null),
	MASONRY_B(new ModelCookbookMasonry(32, 1), null),
	MASONRY_C(new ModelCookbookMasonry(48, 1), null),
	MASONRY_D(new ModelCookbookMasonry(64, 1), null),
	MASONRY_E(new ModelCookbookMasonry(80, 1), null),
	COLUMN_SQUARE_X(new ModelCookbookColumnSquare(8, 1, Axis.X), null),
	COLUMN_SQUARE_Y(new ModelCookbookColumnSquare(8, 1, Axis.Y), null),
	COLUMN_SQUARE_Z(new ModelCookbookColumnSquare(8, 1, Axis.Z), null),
	COLUMN_ROUND_X(new ModelCookbookColumnRound(4, 1, Axis.X), null),
	COLUMN_ROUND_Y(new ModelCookbookColumnRound(4, 1, Axis.Y), null),
	COLUMN_ROUND_Z(new ModelCookbookColumnRound(4, 1, Axis.Z), null),
	HOT_BASALT(new ModelCookbook(0, 4, false, EnumWorldBlockLayer.SOLID, true, true), 
			new ModelCookbook(0, 4, true, EnumWorldBlockLayer.TRANSLUCENT, false, true));

	/**
	 * Identifies the model cookbook that should always be used for
	 * extendedBlockState.  This cookbook also provides custom
	 * collision detection if needed.
	 */
	public final ModelCookbook firstCookbook;
	
	/**
	 * Identifies an optional model cookbook for overlay or
	 * other visual components that require a different render pass.
	 * This cookbook is not used for custom collision detection.
	 * MAY BE NULL.
	 */
	public final ModelCookbook secondCookbook;

	/**
	 * Instantiate a new style.  See elsewhere in this class for what stuff does.
	 */
	NiceStyle(ModelCookbook firstCookbook, ModelCookbook secondCookbook) {
		this.firstCookbook = firstCookbook;
		this.firstCookbook.setStyle(this);
	
		this.secondCookbook = secondCookbook;
		if(this.secondCookbook != null){
			this.secondCookbook.setStyle(this);
		}
	}
	
	// Used by NiceBlock to set extended state so that NiceModel knows what models to provide to renderer.
	public IExtendedBlockState getExtendedState(IExtendedBlockState state, IBlockAccess world, BlockPos pos) {
		if(secondCookbook == null){
			return state.withProperty(NiceBlock.FIRST_MODEL_VARIANT,firstCookbook.getVariantID((IExtendedBlockState) state, world, pos));
		} else {
			return state.withProperty(NiceBlock.FIRST_MODEL_VARIANT,firstCookbook.getVariantID((IExtendedBlockState) state, world, pos))
					.withProperty(NiceBlock.SECOND_MODEL_VARIANT, secondCookbook.getVariantID((IExtendedBlockState) state, world, pos));
		}
	}
}
