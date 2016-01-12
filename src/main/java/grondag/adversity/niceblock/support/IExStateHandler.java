package grondag.adversity.niceblock.support;

import grondag.adversity.niceblock.NiceBlock;
import grondag.adversity.niceblock.model.ModelCookbook;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;

public interface IExStateHandler {
	
	public IExtendedBlockState getExtendedState(IExtendedBlockState state, IBlockAccess world, BlockPos pos);

	public class ExStateHandlerDefault implements IExStateHandler{
		@Override
		public IExtendedBlockState getExtendedState(IExtendedBlockState state, IBlockAccess world, BlockPos pos) {
			return state;
		}
	}
	
	public class ExStateHandlerCookbooks implements IExStateHandler{

		private final ModelCookbook firstCookbook;
		private final ModelCookbook secondCookbook;
		
		public ExStateHandlerCookbooks(ModelCookbook firstCookbook, ModelCookbook secondCookbook){
			this.firstCookbook = firstCookbook;
			this.secondCookbook = secondCookbook;
		}
		@Override
		public IExtendedBlockState getExtendedState(IExtendedBlockState state, IBlockAccess world, BlockPos pos) {
			
			if(secondCookbook == null){
				return state.withProperty(NiceBlock.FIRST_MODEL_VARIANT,firstCookbook.getVariantID((IExtendedBlockState) state, world, pos));
			} else {
				return state.withProperty(NiceBlock.FIRST_MODEL_VARIANT,firstCookbook.getVariantID((IExtendedBlockState) state, world, pos))
						.withProperty(NiceBlock.SECOND_MODEL_VARIANT, secondCookbook.getVariantID((IExtendedBlockState) state, world, pos));
			}
		}
		
	}
	
	
}

