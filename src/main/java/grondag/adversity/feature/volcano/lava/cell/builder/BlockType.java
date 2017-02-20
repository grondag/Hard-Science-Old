package grondag.adversity.feature.volcano.lava.cell.builder;

import grondag.adversity.feature.volcano.lava.LavaTerrainHelper;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.base.IFlowBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

// possible cell content
public enum BlockType
{
    SOLID_FLOW(true),
    SPACE(false),
    LAVA(true),
    BARRIER(false);
    
    public final boolean isFlow;
    
    private BlockType(boolean isFlow)
    {
        this.isFlow = isFlow;
    }
    
    public static BlockType getBlockTypeFromBlockState(IBlockState state)
    {
        if(state.getMaterial().isReplaceable()) return BlockType.SPACE;
        
        Block block = state.getBlock();
        if(IFlowBlock.isFlowHeight(block))
        {
            return block == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK ? BlockType.LAVA : BlockType.SOLID_FLOW;
        }
        else
        {
            return LavaTerrainHelper.canLavaDisplace(state) ? BlockType.SPACE : BlockType.BARRIER;
        }
    }
}