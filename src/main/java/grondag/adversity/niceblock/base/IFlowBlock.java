package grondag.adversity.niceblock.base;

import net.minecraft.block.state.IBlockState;

public interface IFlowBlock 
{
    public abstract float getRenderHeightFromState(IBlockState state);

}
