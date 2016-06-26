package grondag.adversity.niceblock;

import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import grondag.adversity.library.IBlockTest;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.library.joinstate.SimpleJoin;
import grondag.adversity.niceblock.base.ModelController;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.support.BlockTests.TestForBigBlockMatch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

public class MasonryController extends ModelController
{
    protected final IAlternator alternator;

    public MasonryController(String textureName, int alternateTextureCount, BlockRenderLayer renderLayer, boolean isShaded)
    {
        super(textureName, alternateTextureCount, renderLayer, isShaded, false);
        this.alternator = Alternator.getAlternator((byte)(alternateTextureCount));
        this.bakedModelFactory = new MasonryModelFactory(this);
        this.textureCount = 5;
        this.useCachedClientState = false;
    }

    @Override
    public long getClientShapeIndex(final NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
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
        
        NeighborTestResults siblings = neighbors.getNeighborTestResults(
                new IBlockTest() {
                    @Override
                    public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos) {
                        return ibs.getBlock() == block;
                    }
                });
        
       int joinIndex = new SimpleJoin(
        		needsMortar.result(EnumFacing.UP) && !siblings.result(EnumFacing.UP),
                needsMortar.result(EnumFacing.DOWN) && !mates.result(EnumFacing.DOWN)  && (neighbors.getBlockState(EnumFacing.DOWN) != block), 
                needsMortar.result(EnumFacing.EAST) && !mates.result(EnumFacing.EAST) && (neighbors.getBlockState(EnumFacing.EAST) != block),
                needsMortar.result(EnumFacing.WEST) && !siblings.result(EnumFacing.WEST),
                needsMortar.result(EnumFacing.NORTH) && !mates.result(EnumFacing.NORTH) && (neighbors.getBlockState(EnumFacing.NORTH) != block),
                needsMortar.result(EnumFacing.SOUTH) && !siblings.result(EnumFacing.SOUTH)
         ).getIndex();
       
       return this.alternator.getAlternate(pos) * SimpleJoin.STATE_COUNT + joinIndex;

    }

//    @Override
//    public int getShapeCount()
//    {
//        return 64 * getAlternateTextureCount();
//    }
    
    @Override
    public int getAltTextureFromModelIndex(long clientShapeIndex)
    {
        return (int) clientShapeIndex / SimpleJoin.STATE_COUNT;
    }
    
    public int getShapeFromModelIndex(int clientShapeIndex)
    {
        return clientShapeIndex % SimpleJoin.STATE_COUNT;
    }
}
