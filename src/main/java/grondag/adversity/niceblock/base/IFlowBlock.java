package grondag.adversity.niceblock.base;

import grondag.adversity.niceblock.modelstate.FlowHeightState;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

public interface IFlowBlock 
{
//    public boolean isDynamic();
    
    /** height block if false, filler if true */
    public boolean isFiller();
    
    /**
     * Convenience method to check for filler block. 
     */
    public static boolean isBlockFlowFiller(Block block)
    {
        return block instanceof IFlowBlock && ((IFlowBlock)block).isFiller();
    }
    
    /**
     * Convenience method to check for height block. 
     */
    public static boolean isBlockFlowHeight(Block block)
    {
        return block instanceof IFlowBlock && !((IFlowBlock)block).isFiller();
    }
    
    /**
     * Use for height blocks.
     * Returns a value from 1 to 16 to indicate the center height of this block
     */
    public static int getFlowHeightFromState(IBlockState state)
    {
        return 16 - state.getValue(NiceBlock.META);
    }
    
    /** 
     * Use for height blocks.
     * Stores a value from 1 to 16 to indicate the center height of this block 
     */
    public static IBlockState stateWithFlowHeight(IBlockState state, int value)
    {
        return state.withProperty(NiceBlock.META, Math.min(15, Math.max(0,value - 16)));
    }

    /**
     * Use for filler blocks.
     * Returns values from -2 to -1 and +1 to +2.
     */
    public static int getYOffsetFromState(IBlockState state)
    {
        return FlowHeightState.getYOffsetFromTriad(state.getValue(NiceBlock.META));
    }
    
    /**
     * Use for filler blocks.
     * Stores values from -2 to -1 and +1 to +2.
     */
    public static IBlockState stateWithYOffset(IBlockState state, int value)
    {
        return state.withProperty(NiceBlock.META, FlowHeightState.getTriadWithYOffset(value));
    }
    
}
