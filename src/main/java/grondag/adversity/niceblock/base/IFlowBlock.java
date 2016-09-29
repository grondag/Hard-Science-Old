package grondag.adversity.niceblock.base;

import grondag.adversity.niceblock.block.FlowSimpleBlock;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import grondag.adversity.niceblock.modelstate.ModelFlowJoinComponent;
import grondag.adversity.niceblock.modelstate.ModelStateComponents;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public interface IFlowBlock 
{
    
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
     * Use for height blocks.
     * Returns number of filler blocks needed above: 0, 1 or 2.
     */
    public static int topFillerNeeded(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos)
    {
        Block block = blockState.getBlock();
        if(!IFlowBlock.isBlockFlowHeight(block)) return 0;
//        if(block instanceof FlowSimpleBlock) return 0;
        FlowHeightState flowState = ModelFlowJoinComponent.getFlowState((NiceBlock) block, blockState, blockAccess, pos);
        return flowState.topFillerNeeded();
    }
    
    /** 
     * Returns true of geometry of flow block is a full cube.
     * Returns false if otherwise or if is not a flow block. 
     */
    public static boolean isFullCube(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos)
    {
        Block block = blockState.getBlock();
        if(!(block instanceof IFlowBlock)) return false;
        if(block instanceof FlowSimpleBlock) return true;
        FlowHeightState flowState = ((NiceBlock)block).getModelState(blockState, blockAccess, pos)
                .getValue(ModelStateComponents.FLOW_JOIN);
        return flowState.isFullCube();
    }
    
    /**
     * Use for filler blocks.
     * Returns values from +1 to +2.
     */
    public static int getYOffsetFromState(IBlockState state)
    {
        return state.getValue(NiceBlock.META) + 1;
    }
    
    /**
     * Use for filler blocks.
     * Stores values from -2 to -1 and +1 to +2.
     */
    public static IBlockState stateWithYOffset(IBlockState state, int value)
    {
        return state.withProperty(NiceBlock.META, Math.min(1, Math.max(0, value - 1)));
    }
    
}
