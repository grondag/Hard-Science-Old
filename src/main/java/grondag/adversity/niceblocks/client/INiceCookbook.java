package grondag.adversity.niceblocks.client;

import grondag.adversity.niceblocks.NiceBlock2.EnumStyle;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;

/**
 * 
 * @author grondag
 *
 * Used by NiceBlock to determine extended block state.
 * This, combined with style, substance and model, determines how block appears in game.
 */
public interface INiceCookbook {
	
	public int getRecipeCount();
	
	public IExtendedBlockState getExtendedState(IExtendedBlockState state, IBlockAccess worldIn, BlockPos pos);
}

