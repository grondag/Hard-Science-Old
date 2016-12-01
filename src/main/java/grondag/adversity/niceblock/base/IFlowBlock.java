package grondag.adversity.niceblock.base;

import grondag.adversity.Adversity;
import grondag.adversity.config.Config;
import grondag.adversity.feature.volcano.LavaManager;
import grondag.adversity.niceblock.block.FlowDynamicBlock;
import grondag.adversity.niceblock.block.FlowSimpleBlock;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import grondag.adversity.niceblock.modelstate.ModelFlowJoinComponent;
import grondag.adversity.niceblock.modelstate.ModelStateComponents;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public interface IFlowBlock 
{
    public static final Block FLOW_BLOCK_INDICATOR = new Block(Material.AIR);
    
    /**
     * Convenience method to check for flow block. 
     */
    public static boolean isFlowBlock(Block block)
    {
        return block.isAssociatedBlock(FLOW_BLOCK_INDICATOR);
    }
    
    /**
     * Convenience method to check for filler block. 
     */
    public static boolean isFlowFiller(Block block)
    {
        return block.isAssociatedBlock(FLOW_BLOCK_INDICATOR) && ((NiceBlock)block).isFlowFiller();
    }
    
    /**
     * Convenience method to check for height block. 
     */
    public static boolean isFlowHeight(Block block)
    {
        return block.isAssociatedBlock(FLOW_BLOCK_INDICATOR) && ((NiceBlock)block).isFlowHeight();
    }
    
    /**
     * Use for height blocks.
     * Returns a value from 1 to 12 to indicate the center height of this block
     */
    public static int getFlowHeightFromState(IBlockState state)
    {
        if(!(state.getBlock() instanceof NiceBlock))
        {
            Adversity.log.info("derp");
        }
        return Math.max(1, FlowHeightState.BLOCK_LEVELS_INT - state.getValue(NiceBlock.META));
    }
    
    /** 
     * Use for height blocks.
     * Stores a value from 1 to 12 to indicate the center height of this block 
     */
    public static IBlockState stateWithFlowHeight(IBlockState state, int value)
    {
        return state.withProperty(NiceBlock.META, Math.min(11, Math.max(0, FlowHeightState.BLOCK_LEVELS_INT - value)));
    }

    /**
     * Use for height blocks.
     * Returns number of filler blocks needed above: 0, 1 or 2.
     * Is not perfect predictor so check filler block geometry after placement.
     */
    public static int topFillerNeeded(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos)
    {
        Block block = blockState.getBlock();
        if(!IFlowBlock.isFlowHeight(block)) return 0;
//        if(block instanceof FlowSimpleBlock) return 0;
        FlowHeightState flowState = ModelFlowJoinComponent.getFlowState((NiceBlock) block, blockState, blockAccess, pos);
        return flowState.topFillerNeeded();
    }
    
    /** 
     * Returns true of geometry of flow block should be a full cube based on self and neighboring flow blocks.
     * Returns false if otherwise or if is not a flow block. 
     */
    public static boolean shouldBeFullCube(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos)
    {
        Block block = blockState.getBlock();
        if(!isFlowBlock(block)) return false;
        return ModelFlowJoinComponent.getFlowState((NiceBlock)block, blockState, blockAccess, pos).isFullCube();
    }
    
    
    /** 
     * Returns true if geometry of flow block has nothing in it.
     * Returns false if otherwise or if is not a flow block. 
     */
    public static boolean isEmpty(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos)
    {
        Block block = blockState.getBlock();
        if(!isFlowBlock(block)) return false;
        if(block instanceof FlowSimpleBlock) return true;
        FlowHeightState flowState = ((NiceBlock)block).getModelState(blockState, blockAccess, pos)
                .getValue(ModelStateComponents.FLOW_JOIN);
        return flowState.isEmpty();
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
    
    /** 
     * Looks for nearby dynamic blocks that might depend on this block for height state
     * and converts them to static blocks if possible. 
     */
    public static void freezeNeighbors(World worldIn, BlockPos pos, IBlockState state)
    {
        //only height blocks affect neighbors
        if(!isFlowHeight(state.getBlock())) return;
                
        IBlockState targetState;
        Block targetBlock;
        
        for(int x = -2; x <= 2; x++)
        {
            for(int z = -2; z <= 2; z++)
            {
                for(int y = -4; y <= 4; y++)
                {
//                    if(!(x == 0 && y == 0 && z == 0))
                    {
                        BlockPos targetPos = pos.add(x, y, z);
                        targetState = worldIn.getBlockState(targetPos);
                        targetBlock = targetState.getBlock();
                        if(targetBlock instanceof FlowDynamicBlock)
                        {
                            ((FlowDynamicBlock)targetBlock).makeStatic(targetState, worldIn, targetPos);
                        }
                    }
                }
            }
        }
    }
}
