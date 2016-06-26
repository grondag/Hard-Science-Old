package grondag.adversity.niceblock;

import java.util.Random;

import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.support.BaseMaterial;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StaticLavaBlock extends FlowBlock
{

    public StaticLavaBlock(FlowBlockHelper blockModelHelper, BaseMaterial material, String styleName)
    {
        super(blockModelHelper, material, styleName);
        this.setTickRandomly(true);
    }

    // Used to prevent updates on chunk generation
    @Override
    public boolean requiresUpdates()
    {
        return false;
    }
    
    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
    {
        world.setBlockState(pos, NiceBlockRegistrar.BLOCK_COOL_BASALT.getDefaultState().withProperty(NiceBlock.META, state.getValue(NiceBlock.META)));
    }
}
