package grondag.adversity.feature.volcano;

import java.util.Random;

import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.base.ModelDispatcher;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.block.FlowStaticBlock;
import grondag.adversity.niceblock.support.BaseMaterial;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CoolingBlock extends FlowStaticBlock
{
    protected FlowStaticBlock nextCoolingBlock;
    
    protected int heatLevel = 0;
    
    public CoolingBlock(ModelDispatcher dispatcher, BaseMaterial material, String styleName, boolean isFiller)
    {
        super(dispatcher, material, styleName, isFiller);
        this.setTickRandomly(true);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        super.updateTick(worldIn, pos, state, rand);
        if(state.getBlock() == this && canCool(worldIn, pos, state, rand))
        {
            long modelKey = this.getModelStateKey(state, worldIn, pos);
            worldIn.setBlockState(pos, this.nextCoolingBlock.getDefaultState().withProperty(NiceBlock.META, state.getValue(NiceBlock.META)), 3);
            nextCoolingBlock.setModelStateKey(state, worldIn, pos, modelKey);
        }
        
    }
    
    /** True if no adjacent blocks are hotter than me and at least four adjacent blocks are cooler.
     * Occasionally can cool if only three are cooler. */
    private boolean canCool(World worldIn, BlockPos pos, IBlockState state, Random rand)
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
                    if(heat > this.heatLevel) return false;
                    if(heat < this.heatLevel) coolerFaceCount++;
                }
                else
                {
                    coolerFaceCount++;
                }
            }
        }
        return coolerFaceCount >= 4 || (coolerFaceCount == 3 && rand.nextGaussian() < 0.2);
    }
    
    public CoolingBlock setCoolingBlockInfo(FlowStaticBlock nextCoolingBlock, int heatLevel)
    {
        this.nextCoolingBlock = nextCoolingBlock;
        this.heatLevel = heatLevel;
        return this;
    }
    

}
