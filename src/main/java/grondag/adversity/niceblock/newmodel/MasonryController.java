package grondag.adversity.niceblock.newmodel;

import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import grondag.adversity.library.IBlockTest;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.niceblock.newmodel.BlockTests.TestForBigBlockMatch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

public class MasonryController extends ModelControllerNew
{
    protected final IAlternator alternator;

    protected MasonryController(String textureName, int alternateTextureCount, BlockRenderLayer renderLayer, boolean isShaded)
    {
        super(textureName, alternateTextureCount, renderLayer, isShaded, false);
        this.alternator = Alternator.getAlternator((byte)(alternateTextureCount));
        this.bakedModelFactory = new MasonryModelFactory(this);
        this.textureCount = 5;
        this.useCachedClientState = false;
    }

    @Override
    public int getClientShapeIndex(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        int colorIndex = block.blockModelHelper.getModelStateForBlock(state, world, pos, false).getColorIndex();
        TestForBigBlockMatch test = new TestForBigBlockMatch(block, colorIndex, state.getValue(NiceBlock.META));
        NeighborBlocks neighbors = new NeighborBlocks(world, pos);
        NeighborTestResults mates = neighbors.getNeighborTestResults(test);
        
        NeighborTestResults needsMortar = neighbors.getNeighborTestResults(
                new IBlockTest() {
                    @Override
                    public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos) {
                        return ibs.getBlock().isOpaqueCube(ibs);
                    }
                });

        return ModelReference.SIMPLE_JOIN_STATE_LOOKUP
                [needsMortar.result(EnumFacing.UP) && !mates.result(EnumFacing.UP) ? 1 : 0]
                [needsMortar.result(EnumFacing.DOWN) && !mates.result(EnumFacing.DOWN)  && (neighbors.getBlockState(EnumFacing.DOWN) != block) ? 1 : 0] 
                [needsMortar.result(EnumFacing.EAST) && !mates.result(EnumFacing.EAST) && (neighbors.getBlockState(EnumFacing.EAST) != block) ? 1 : 0]
                [needsMortar.result(EnumFacing.WEST) && !mates.result(EnumFacing.WEST) ? 1 : 0] 
                [needsMortar.result(EnumFacing.NORTH) && !mates.result(EnumFacing.NORTH) && (neighbors.getBlockState(EnumFacing.NORTH) != block) ? 1 : 0]
                [needsMortar.result(EnumFacing.SOUTH) && !mates.result(EnumFacing.SOUTH) ? 1 : 0]; 
    }

    protected int getAlternateTextureIndexFromModelState(ModelState modelState) 
    {
            return modelState.getClientShapeIndex(this.renderLayer.ordinal()) % alternateTextureCount;
    }

    @Override
    public int getShapeCount()
    {
        return 64 * alternateTextureCount;
    }
}
