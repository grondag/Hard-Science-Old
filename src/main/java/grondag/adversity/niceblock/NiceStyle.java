package grondag.adversity.niceblock;

import grondag.adversity.niceblock.model.ModelCookbook;
import grondag.adversity.niceblock.model.ModelCookbookColumnRound;
import grondag.adversity.niceblock.model.ModelCookbookColumnSquare;
import grondag.adversity.niceblock.model.ModelCookbookConnectedCorners;
import grondag.adversity.niceblock.model.ModelCookbookMasonry;
import grondag.adversity.niceblock.model.NiceModel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.model.ModelResourceLocation;
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
public class NiceStyle {
	
	public static final NiceStyle RAW = new NiceStyle(new ModelCookbook(0, 4), null);
	public static final NiceStyle SMOOTH = new NiceStyle(new ModelCookbook(4, 4), null);
	public static final NiceStyle LARGE_BRICKS = new NiceStyle(new ModelCookbook(96, 4), null);
	public static final NiceStyle SMALL_BRICKS = new NiceStyle(new ModelCookbook(120, 4), null);
	public static final NiceStyle BIG_WORN = new NiceStyle(new ModelCookbookConnectedCorners(256, 3), null);
	public static final NiceStyle BIG_WEATHERED = new NiceStyle(new ModelCookbookConnectedCorners(400, 1), null);
	public static final NiceStyle BIG_ORNATE = new NiceStyle(new ModelCookbookConnectedCorners(448, 1), null);
	public static final NiceStyle MASONRY_A = new NiceStyle(new ModelCookbookMasonry(16, 1), null);
	public static final NiceStyle MASONRY_B = new NiceStyle(new ModelCookbookMasonry(32, 1), null);
	public static final NiceStyle MASONRY_C = new NiceStyle(new ModelCookbookMasonry(48, 1), null);
	public static final NiceStyle MASONRY_D = new NiceStyle(new ModelCookbookMasonry(64, 1), null);
	public static final NiceStyle MASONRY_E = new NiceStyle(new ModelCookbookMasonry(80, 1), null);
	public static final NiceStyle COLUMN_SQUARE_X = new NiceStyle(new ModelCookbookColumnSquare(8, 1, Axis.X), null);
	public static final NiceStyle COLUMN_SQUARE_Y = new NiceStyle(new ModelCookbookColumnSquare(8, 1, Axis.Y), null);
	public static final NiceStyle COLUMN_SQUARE_Z = new NiceStyle(new ModelCookbookColumnSquare(8, 1, Axis.Z), null);
	public static final NiceStyle COLUMN_ROUND_X = new NiceStyle(new ModelCookbookColumnRound(4, 1, Axis.X), null);
	public static final NiceStyle COLUMN_ROUND_Y = new NiceStyle(new ModelCookbookColumnRound(4, 1, Axis.Y), null);
	public static final NiceStyle COLUMN_ROUND_Z = new NiceStyle(new ModelCookbookColumnRound(4, 1, Axis.Z), null);
	public static final NiceStyle HOT_BASALT = new NiceStyle(new ModelCookbook(0, 4, false, EnumWorldBlockLayer.SOLID, true, true), 
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
	
	/**
	 * TODO: docs
	 */
	public NiceModel getModel(NiceSubstance substance, ModelResourceLocation mrlBlock, ModelResourceLocation mrlItem){
		return new NiceModel(this, substance, mrlBlock, mrlItem);
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
