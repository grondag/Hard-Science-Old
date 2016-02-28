package grondag.adversity.niceblock.newmodel;

import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import grondag.adversity.library.IBlockTest;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.niceblock.newmodel.BlockTests.TestForBigBlockMatch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;

public class MasonryController extends ModelControllerNew
{
    protected final BakedModelFactory bakedModelFactory;
    protected final IAlternator alternator;

    protected MasonryController(String textureName, int alternateTextureCount, EnumWorldBlockLayer renderLayer, boolean isShaded)
    {
        super(textureName, alternateTextureCount, renderLayer, isShaded, false);
        this.alternator = Alternator.getAlternator((byte)(alternateTextureCount & 0xFF));
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
                        return ibs.getBlock().isOpaqueCube();
                    }
                });

        return ModelReference.SIMPLE_JOIN_STATE_LOOKUP
                [needsMortar.up() && !mates.up()? 1 : 0]
                [needsMortar.down() && !mates.down()  && (neighbors.down().getBlock() != block) ? 1 : 0] 
                [needsMortar.east() && !mates.east() && (neighbors.east().getBlock() != block) ? 1 : 0]
                [needsMortar.west() && !mates.west() ? 1 : 0] 
                [needsMortar.north() && !mates.north() && (neighbors.north().getBlock() != block) ? 1 : 0]
                [needsMortar.south() && !mates.south() ? 1 : 0]; 
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

    @Override
    public BakedModelFactory getBakedModelFactory()
    {
        return this.bakedModelFactory;
    }

}
