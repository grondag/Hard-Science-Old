package grondag.adversity.niceblock;

import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.library.joinstate.CornerJoinBlockStateSelector;
import grondag.adversity.niceblock.base.ModelController;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.support.BlockTests.TestForBigBlockMatch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.IBlockAccess;

public class BorderController extends ModelController
{

    protected final IAlternator alternator;

    public BorderController(String textureName, int alternateTextureCount, BlockRenderLayer renderLayer, boolean isShaded)
    {
        super(textureName, alternateTextureCount, renderLayer, isShaded, false);
        this.alternator = Alternator.getAlternator((byte)(alternateTextureCount));
        this.bakedModelFactory = new BorderModelFactory(this);
        // Only the first 15 textures actually used. 
        // The padding is only to make texture file production easier.
        this.textureCount = 16;
    }
    
    @Override
    public int getClientShapeIndex(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        int colorIndex = block.blockModelHelper.getModelStateForBlock(state, world, pos, false).getColorIndex();
        TestForBigBlockMatch test = new TestForBigBlockMatch(block, colorIndex, state.getValue(NiceBlock.META));
        NeighborTestResults mates = new NeighborBlocks(world, pos).getNeighborTestResults(test);

        return (this.alternator.getAlternate(pos)  * CornerJoinBlockStateSelector.BLOCK_JOIN_STATE_COUNT) + CornerJoinBlockStateSelector.findIndex(mates);
    }

    @Override
    public int getShapeCount()
    {
        return CornerJoinBlockStateSelector.BLOCK_JOIN_STATE_COUNT * getAlternateTextureCount();
    }

    public int getTextureFromModelIndex(int clientShapeIndex)
    {
        return clientShapeIndex / CornerJoinBlockStateSelector.BLOCK_JOIN_STATE_COUNT;
    }
    
    public int getShapeFromModelIndex(int clientShapeIndex)
    {
        return clientShapeIndex % CornerJoinBlockStateSelector.BLOCK_JOIN_STATE_COUNT;
    }
}
