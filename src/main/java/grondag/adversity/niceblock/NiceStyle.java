package grondag.adversity.niceblock;

import grondag.adversity.niceblock.model.IModelController;
import grondag.adversity.niceblock.model.ModelCookbook;
import grondag.adversity.niceblock.model.ModelCookbookColumnRound;
import grondag.adversity.niceblock.model.ModelCookbookColumnSquare;
import grondag.adversity.niceblock.model.ModelCookbookConnectedCorners;
import grondag.adversity.niceblock.model.ModelCookbookMasonry;
import grondag.adversity.niceblock.model.ModelRenderData;
import grondag.adversity.niceblock.model.NiceModel;
import grondag.adversity.niceblock.model.NiceModelBasic;
import grondag.adversity.niceblock.support.ICollisionHandler;
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
public abstract class NiceStyle {
	
	public static final NiceStyle RAW = new NiceStyleBasic(new ModelCookbook(0, 4), null);
	public static final NiceStyle SMOOTH = new NiceStyleBasic(new ModelCookbook(4, 4), null);
	public static final NiceStyle LARGE_BRICKS = new NiceStyleBasic(new ModelCookbook(96, 4), null);
	public static final NiceStyle SMALL_BRICKS = new NiceStyleBasic(new ModelCookbook(120, 4), null);
	public static final NiceStyle BIG_WORN = new NiceStyleBasic(new ModelCookbookConnectedCorners(256, 3), null);
	public static final NiceStyle BIG_WEATHERED = new NiceStyleBasic(new ModelCookbookConnectedCorners(400, 1), null);
	public static final NiceStyle BIG_ORNATE = new NiceStyleBasic(new ModelCookbookConnectedCorners(448, 1), null);
	public static final NiceStyle MASONRY_A = new NiceStyleBasic(new ModelCookbookMasonry(16, 1), null);
	public static final NiceStyle MASONRY_B = new NiceStyleBasic(new ModelCookbookMasonry(32, 1), null);
	public static final NiceStyle MASONRY_C = new NiceStyleBasic(new ModelCookbookMasonry(48, 1), null);
	public static final NiceStyle MASONRY_D = new NiceStyleBasic(new ModelCookbookMasonry(64, 1), null);
	public static final NiceStyle MASONRY_E = new NiceStyleBasic(new ModelCookbookMasonry(80, 1), null);
	public static final NiceStyle COLUMN_SQUARE_X = new NiceStyleBasic(new ModelCookbookColumnSquare(8, 1, Axis.X), null);
	public static final NiceStyle COLUMN_SQUARE_Y = new NiceStyleBasic(new ModelCookbookColumnSquare(8, 1, Axis.Y), null);
	public static final NiceStyle COLUMN_SQUARE_Z = new NiceStyleBasic(new ModelCookbookColumnSquare(8, 1, Axis.Z), null);
	public static final NiceStyle COLUMN_ROUND_X = new NiceStyleBasic(new ModelCookbookColumnRound(4, 1, Axis.X), null);
	public static final NiceStyle COLUMN_ROUND_Y = new NiceStyleBasic(new ModelCookbookColumnRound(4, 1, Axis.Y), null);
	public static final NiceStyle COLUMN_ROUND_Z = new NiceStyleBasic(new ModelCookbookColumnRound(4, 1, Axis.Z), null);
	public static final NiceStyle HOT_BASALT = new NiceStyleBasic(new ModelCookbook(0, 4, false, EnumWorldBlockLayer.SOLID, true, true), 
			new ModelCookbook(0, 4, true, EnumWorldBlockLayer.TRANSLUCENT, false, true));
	
	public abstract IModelController getModelController();
	
	
	public static class NiceStyleBasic extends NiceStyle implements IModelController{
		
		@Override
		public IModelController getModelController(){
			return this;
		}
		
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
		NiceStyleBasic(ModelCookbook firstCookbook, ModelCookbook secondCookbook) {
			this.firstCookbook = firstCookbook;
			this.firstCookbook.setStyle(this);
		
			this.secondCookbook = secondCookbook;
			if(this.secondCookbook != null){
				this.secondCookbook.setStyle(this);
			}
		}
		
		/**
		 * Override if special collision handling is needed due to non-cubic shape.
		 */
		@Override
		public ICollisionHandler getCollisionHandler() {
			return firstCookbook.getCollisionHandler();
		}
		
		@Override
		public boolean canRenderInLayer(EnumWorldBlockLayer layer) {
			return firstCookbook.getRenderLayer() == layer
					|| (secondCookbook != null && secondCookbook.getRenderLayer() == layer);
		}
		
		public NiceModel getModel(NiceSubstance substance, ModelResourceLocation mrlBlock, ModelResourceLocation mrlItem){
			return new NiceModelBasic(this, substance, mrlBlock, mrlItem);
		}
		
		@Override
		public IExtendedBlockState getExtendedState(IExtendedBlockState state, IBlockAccess world, BlockPos pos) {
			
			if(secondCookbook == null){
				return state.withProperty(
						NiceBlock.FIRST_MODEL_VARIANT, firstCookbook.getVariantID((IExtendedBlockState) state, world, pos));
			} else {
				return state
						.withProperty(NiceBlock.FIRST_MODEL_VARIANT, firstCookbook.getVariantID((IExtendedBlockState) state, world, pos))
						.withProperty(NiceBlock.SECOND_MODEL_VARIANT, secondCookbook.getVariantID((IExtendedBlockState) state, world, pos));
			}
		}
	}
}
