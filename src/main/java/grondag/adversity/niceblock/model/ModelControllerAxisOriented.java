package grondag.adversity.niceblock.model;

import grondag.adversity.niceblock.NiceSubstance;
import grondag.adversity.niceblock.support.ICollisionHandler;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;

public class ModelControllerAxisOriented implements IModelController{

	private final NiceModel model;
	
	public ModelControllerAxisOriented(NiceModel model){
		this.model = model;
	}
	
	@Override
	public NiceModel getModel(NiceSubstance substance, ModelResourceLocation mrlBlock, ModelResourceLocation mrlItem) {
		return null;
	}

	@Override
	public IExtendedBlockState getExtendedState(IExtendedBlockState state, IBlockAccess world, BlockPos pos) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ICollisionHandler getCollisionHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canRenderInLayer(EnumWorldBlockLayer layer) {
		// TODO Auto-generated method stub
		return false;
	}

}
