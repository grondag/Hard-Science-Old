package grondag.adversity.niceblock.base;

import grondag.adversity.niceblock.FlowHeightState;
import net.minecraft.block.state.IBlockState;

public interface IFlowBlock 
{
    public static boolean isDynamic()
    {
        return true;
    }
    
    public interface IHeightBlock
    {
        /**
         * Returns a value from 1 to 16 to indicate the center height of this block
         */
        public static int getFlowHeightFromState(IBlockState state)
        {
            return 16 - state.getValue(NiceBlock.META);
        }
        
        /** 
         * Stores a value from 1 to 16 to indicate the center height of this block 
         */
        public static IBlockState stateWithFlowHeight(IBlockState state, int value)
        {
            return state.withProperty(NiceBlock.META, Math.min(15, Math.max(0,value - 16)));
        }
    }
    
    public interface IFillerBlock
    {
        /**
         * Returns values from -2 to -1 and +1 to +2.
         */
        public static int getYOffsetFromState(IBlockState state)
        {
            return FlowHeightState.getYOffsetFromNibble(state.getValue(NiceBlock.META));
        }
        
        /**
         * Stores values from -2 to -1 and +1 to +2.
         */
        public static IBlockState stateWithYOffset(IBlockState state, int value)
        {
            return state.withProperty(NiceBlock.META, FlowHeightState.getNibbleWithYOffset(value));
        }
    }
}
