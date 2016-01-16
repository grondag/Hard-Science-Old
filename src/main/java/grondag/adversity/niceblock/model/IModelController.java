package grondag.adversity.niceblock.model;

import grondag.adversity.niceblock.NiceSubstance;
import grondag.adversity.niceblock.support.ICollisionHandler;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;


/**
 * Creates and manages extended state for a NiceModel.
 * NiceModel will generally be coupled to a specific implementation.
 */
public interface IModelController {
	
	/**
	 * Used by NiceBlockRegistrar to register models for block/style/substance combinations.
	 */
	public abstract NiceModel getModel(NiceSubstance substance);
	
	/**
	 * Used by NiceBlock to generate appropriate render state.
	 */
	public abstract IExtendedBlockState getExtendedState(IExtendedBlockState state, IBlockAccess world, BlockPos pos);
	
	/**
	 * Override if special collision handling is needed due to non-cubic shape.
	 */
	public ICollisionHandler getCollisionHandler();
	
	/**
	 * Used by NiceBlock to control rendering.
	 */
	public boolean canRenderInLayer(EnumWorldBlockLayer layer);
	
	/**
	 * Tells NiceModel which texture to use for block-breaking particles.
	 */
	public String getParticleTextureName(NiceSubstance substance);
	
}
