package grondag.adversity.niceblock;

import grondag.adversity.niceblock.model.IModelController;
import grondag.adversity.niceblock.model.IModelController.Rotation;
import grondag.adversity.niceblock.model.ModelControllerBigTex;
import grondag.adversity.niceblock.model.ModelControllerBlock;
import grondag.adversity.niceblock.model.ModelControllerBorder;
import grondag.adversity.niceblock.model.ModelControllerDual;
import grondag.adversity.niceblock.model.ModelCookbook;
import grondag.adversity.niceblock.model.ModelCookbookColumnRound;
import grondag.adversity.niceblock.model.ModelCookbookColumnSquare;
import grondag.adversity.niceblock.model.ModelCookbookMasonry;
import grondag.adversity.niceblock.model.ModelRenderState;
import grondag.adversity.niceblock.model.NiceModel;
import grondag.adversity.niceblock.model.NiceModelBasic;
import grondag.adversity.niceblock.support.ICollisionHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;

/**
 * Style governs the visual appearance and some behavior of a NiceBlock.
 * A NiceBlock can have only a single style.
 * Some styles are visually similar or identical but have distinct identities
 * to allow multi-blocks with connected textures that are adjacent to each other.
 * 
 */
public abstract class NiceStyle {
	
	public static final NiceStyle RAW = new NiceStyleOld(new ModelCookbook("raw", 4), null);
	public static final NiceStyle SMOOTH = new NiceStyleOld(new ModelCookbook("polished", 4), null);
//	public static final NiceStyle MASONRY_A = new NiceStyleOld(new ModelCookbookMasonry(16, 1), null);
//	public static final NiceStyle MASONRY_B = new NiceStyleOld(new ModelCookbookMasonry(32, 1), null);
//	public static final NiceStyle MASONRY_C = new NiceStyleOld(new ModelCookbookMasonry(48, 1), null);
//	public static final NiceStyle MASONRY_D = new NiceStyleOld(new ModelCookbookMasonry(64, 1), null);
//	public static final NiceStyle MASONRY_E = new NiceStyleOld(new ModelCookbookMasonry(80, 1), null);
	public static final NiceStyle COLUMN_SQUARE_X = new NiceStyleOld(new ModelCookbookColumnSquare("square_column", 1, Axis.X), null);
	public static final NiceStyle COLUMN_SQUARE_Y = new NiceStyleOld(new ModelCookbookColumnSquare("square_column", 1, Axis.Y), null);
	public static final NiceStyle COLUMN_SQUARE_Z = new NiceStyleOld(new ModelCookbookColumnSquare("square_column", 1, Axis.Z), null);
	public static final NiceStyle COLUMN_ROUND_X = new NiceStyleOld(new ModelCookbookColumnRound("polished", 1, Axis.X), null);
	public static final NiceStyle COLUMN_ROUND_Y = new NiceStyleOld(new ModelCookbookColumnRound("polished", 1, Axis.Y), null);
	public static final NiceStyle COLUMN_ROUND_Z = new NiceStyleOld(new ModelCookbookColumnRound("polished", 1, Axis.Z), null);
	public static final NiceStyle HOT_BASALT = new NiceStyleNew(
			new ModelControllerDual(
					new ModelControllerBlock("raw", 4, EnumWorldBlockLayer.SOLID, true, true),
					new ModelControllerBlock("hot_basalt_3", 4, EnumWorldBlockLayer.TRANSLUCENT, false, true)));

	public static final NiceStyle BIG_TEX = new NiceStyleNew(
			new ModelControllerDual(
					new ModelControllerBigTex("bigtex_rock_test", EnumWorldBlockLayer.SOLID, true, Rotation.ROTATE_NONE, false, false, NiceColor.STONE0),
					new ModelControllerBorder("bordertest", 1, EnumWorldBlockLayer.TRANSLUCENT, true, NiceColor.STONE0)));

//     public static final NiceStyle BIG_TEX = new NiceStyleNew(
//     new ModelControllerBorder("bordertest", 1, EnumWorldBlockLayer.TRANSLUCENT, true, NiceColor.STONE0));

	
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
		
		public NiceModel getModel(int meta){
			return new NiceModelBasic(this, meta);
		}
		
		@Override
		public ModelRenderState getRenderState(IExtendedBlockState state, IBlockAccess world, BlockPos pos) {
			
			if(secondCookbook == null){
				return new ModelRenderState(firstCookbook.getVariantID((IExtendedBlockState) state, world, pos), -1);
			} else {
				return new ModelRenderState(
								firstCookbook.getVariantID((IExtendedBlockState) state, world, pos),
								secondCookbook.getVariantID((IExtendedBlockState) state, world, pos));
			}
		}

		@Override
		public String getFirstTextureName(int meta) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String[] getAllTextures(int meta) {
			// TODO Auto-generated method stub
			return null;
		}

	}
}
