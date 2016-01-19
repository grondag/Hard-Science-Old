package grondag.adversity.niceblock;

import grondag.adversity.niceblock.model.IModelController;
import grondag.adversity.niceblock.model.IModelController.Rotation;
import grondag.adversity.niceblock.model.ModelControllerBigTex;
import grondag.adversity.niceblock.model.ModelControllerBlock;
import grondag.adversity.niceblock.model.ModelControllerDual;
import grondag.adversity.niceblock.model.ModelCookbook;
import grondag.adversity.niceblock.model.ModelCookbookColumnRound;
import grondag.adversity.niceblock.model.ModelCookbookColumnSquare;
import grondag.adversity.niceblock.model.ModelCookbookConnectedCorners;
import grondag.adversity.niceblock.model.ModelCookbookMasonry;
import grondag.adversity.niceblock.model.ModelRenderState;
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
	
	public static final NiceStyle RAW = new NiceStyleOld(new ModelCookbook(0, 4), null);
	public static final NiceStyle SMOOTH = new NiceStyleOld(new ModelCookbook(4, 4), null);
	public static final NiceStyle LARGE_BRICKS = new NiceStyleOld(new ModelCookbook(96, 4), null);
	public static final NiceStyle SMALL_BRICKS = new NiceStyleOld(new ModelCookbook(120, 4), null);
	public static final NiceStyle BIG_WORN = new NiceStyleOld(new ModelCookbookConnectedCorners(256, 3), null);
	public static final NiceStyle BIG_WEATHERED = new NiceStyleOld(new ModelCookbookConnectedCorners(400, 1), null);
	public static final NiceStyle BIG_ORNATE = new NiceStyleOld(new ModelCookbookConnectedCorners(448, 1), null);
	public static final NiceStyle MASONRY_A = new NiceStyleOld(new ModelCookbookMasonry(16, 1), null);
	public static final NiceStyle MASONRY_B = new NiceStyleOld(new ModelCookbookMasonry(32, 1), null);
	public static final NiceStyle MASONRY_C = new NiceStyleOld(new ModelCookbookMasonry(48, 1), null);
	public static final NiceStyle MASONRY_D = new NiceStyleOld(new ModelCookbookMasonry(64, 1), null);
	public static final NiceStyle MASONRY_E = new NiceStyleOld(new ModelCookbookMasonry(80, 1), null);
	public static final NiceStyle COLUMN_SQUARE_X = new NiceStyleOld(new ModelCookbookColumnSquare(8, 1, Axis.X), null);
	public static final NiceStyle COLUMN_SQUARE_Y = new NiceStyleOld(new ModelCookbookColumnSquare(8, 1, Axis.Y), null);
	public static final NiceStyle COLUMN_SQUARE_Z = new NiceStyleOld(new ModelCookbookColumnSquare(8, 1, Axis.Z), null);
	public static final NiceStyle COLUMN_ROUND_X = new NiceStyleOld(new ModelCookbookColumnRound(4, 1, Axis.X), null);
	public static final NiceStyle COLUMN_ROUND_Y = new NiceStyleOld(new ModelCookbookColumnRound(4, 1, Axis.Y), null);
	public static final NiceStyle COLUMN_ROUND_Z = new NiceStyleOld(new ModelCookbookColumnRound(4, 1, Axis.Z), null);
	public static final NiceStyle HOT_BASALT = new NiceStyleNew(
			new ModelControllerDual(
					new ModelControllerBlock(0, 4, false, EnumWorldBlockLayer.SOLID, true, true),
					new ModelControllerBlock(0, 4, true, EnumWorldBlockLayer.TRANSLUCENT, false, true)));

	public static final NiceStyle BIG_TEX = new NiceStyleNew(
					new ModelControllerBigTex(0, false, EnumWorldBlockLayer.SOLID, true, Rotation.ROTATE_NONE, false, false, 0xBBB9AB));

	
	public abstract IModelController getModelController();
	
	public static class NiceStyleNew extends NiceStyle {

		final IModelController controller;
		
		public NiceStyleNew(IModelController controller){
			this.controller = controller;
		}
		
		@Override
		public IModelController getModelController() {
			return controller;
		}
		
	}
	
	
	public static class NiceStyleOld extends NiceStyle implements IModelController{
		
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
		NiceStyleOld(ModelCookbook firstCookbook, ModelCookbook secondCookbook) {
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
		
		public NiceModel getModel(NiceSubstance substance){
			return new NiceModelBasic(this, substance);
		}
		
		@Override
		public IExtendedBlockState getExtendedState(IExtendedBlockState state, IBlockAccess world, BlockPos pos) {
			
			if(secondCookbook == null){
				return state.withProperty(
						NiceBlock.MODEL_RENDER_STATE, 
						new ModelRenderState(firstCookbook.getVariantID((IExtendedBlockState) state, world, pos), -1));
			} else {
				return state.withProperty(
						NiceBlock.MODEL_RENDER_STATE, 
						new ModelRenderState(
								firstCookbook.getVariantID((IExtendedBlockState) state, world, pos),
								secondCookbook.getVariantID((IExtendedBlockState) state, world, pos)));
			}
		}

		@Override
		public String getFirstTextureName(NiceSubstance substance) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String[] getAllTextures(NiceSubstance substance) {
			// TODO Auto-generated method stub
			return null;
		}

	}
}
