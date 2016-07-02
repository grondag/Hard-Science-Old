package grondag.adversity.niceblock.base;

import net.minecraft.block.state.IBlockState;

public interface IFlowBlock 
{
    /**
     * Returns a value normalized from 1 to 16 irrespective of how many meta states are actually used  */
    public abstract int getRenderHeightFromState(IBlockState state);

}
