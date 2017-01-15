package grondag.adversity.feature.volcano;

import java.util.Random;

import grondag.adversity.feature.volcano.lava.WorldStateBuffer;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.base.ModelDispatcher;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.block.FlowDynamicBlock;
import grondag.adversity.niceblock.block.FlowStaticBlock;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import grondag.adversity.niceblock.modelstate.ModelStateComponents;
import grondag.adversity.niceblock.support.BaseMaterial;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class CoolingBlock extends FlowDynamicBlock
{
    protected FlowDynamicBlock nextCoolingBlock;
    
    protected int heatLevel = 0;
    
    //TODO: register with simulator on placement
    
    public CoolingBlock(ModelDispatcher dispatcher, BaseMaterial material, String styleName, boolean isFiller)
    {
        super(dispatcher, material, styleName, isFiller);
    }

    
    public static enum CoolingResult
    {
        /** means no more cooling can take place */
        COMPLETE,
        /** means one stage completed - more remain */
        PARTIAL,
        /** means block wan't ready to cool */
        UNREADY,
        /** means this isn't a cooling block*/
        INVALID
    }
    /**
     * Cools this block if ready and returns true if successful.
     */
    public CoolingResult tryCooling(WorldStateBuffer worldIn, BlockPos pos, IBlockState state)
    {
        if(state.getBlock() == this)
        {
            if(canCool(worldIn, pos, state))
            {
    //            long modelKey = this.getModelStateKey(state, worldIn, pos);
                
                if(this.nextCoolingBlock == NiceBlockRegistrar.COOL_FLOWING_BASALT_HEIGHT_BLOCK)
                {
                    if( IFlowBlock.shouldBeFullCube(state, worldIn, pos))
                    {
                        worldIn.setBlockState(pos, NiceBlockRegistrar.COOL_SQUARE_BASALT_BLOCK.getDefaultState().withProperty(NiceBlock.META, state.getValue(NiceBlock.META)));
                    }
                    else
                    {
                        worldIn.setBlockState(pos, this.nextCoolingBlock.getDefaultState().withProperty(NiceBlock.META, state.getValue(NiceBlock.META)));
                    }
                    return CoolingResult.COMPLETE;
                }
                else
                {
                    worldIn.setBlockState(pos, this.nextCoolingBlock.getDefaultState().withProperty(NiceBlock.META, state.getValue(NiceBlock.META)));
                    return CoolingResult.PARTIAL;
                }
            }
            else
            {
                return CoolingResult.UNREADY;
            }
            
        }
        else
        {
            return CoolingResult.INVALID;
        }
        
    }
    
    /** True if no adjacent blocks are hotter than me and at least four adjacent blocks are cooler.
     * Occasionally can cool if only three are cooler. */
    public boolean canCool(WorldStateBuffer worldIn, BlockPos pos, IBlockState state)
    {
        int coolerFaceCount = 0;
        for(EnumFacing face : EnumFacing.VALUES)
        {
            IBlockState testState = worldIn.getBlockState(pos.add(face.getDirectionVec()));
            if(testState != null)
            {
                Block neighbor = testState.getBlock();
                
                if(neighbor == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK 
                        || neighbor == NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK) return false;
                    
                
                if(neighbor instanceof CoolingBlock)
                {
                    int heat = ((CoolingBlock) neighbor).heatLevel;
                    if(heat > this.heatLevel + 1) return false;
                    if(heat < this.heatLevel) coolerFaceCount++;
                }
                else
                {
                    coolerFaceCount++;
                }
            }
        }
        return coolerFaceCount >= 3;
    }
    
    public CoolingBlock setCoolingBlockInfo(FlowDynamicBlock nextCoolingBlock, int heatLevel)
    {
        this.nextCoolingBlock = nextCoolingBlock;
        this.heatLevel = heatLevel;
        return this;
    }
    

}
