package grondag.adversity.niceblock;

import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.niceblock.base.ModelController;
import grondag.adversity.niceblock.base.ModelState;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.support.CornerStateFinder;
import grondag.adversity.niceblock.support.ModelReference;
import grondag.adversity.niceblock.support.BlockTests.TestForBigBlockMatch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
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
    
    protected int getAlternateTextureIndexFromModelState(ModelState modelState) 
    {
            return modelState.getClientShapeIndex(this.getRenderLayer().ordinal()) % getAlternateTextureCount();
    }

    @Override
    public int getClientShapeIndex(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        int colorIndex = block.blockModelHelper.getModelStateForBlock(state, world, pos, false).getColorIndex();
        TestForBigBlockMatch test = new TestForBigBlockMatch(block, colorIndex, state.getValue(NiceBlock.META));
        NeighborBlocks neighbors = new NeighborBlocks(world, pos);
        NeighborTestResults mates = neighbors.getNeighborTestResults(test);

        CornerStateFinder finder = ModelReference.CONNECTED_CORNER_STATE_LOOKUP[mates.resultBit(EnumFacing.UP)][mates.resultBit(EnumFacing.DOWN)]
                [mates.resultBit(EnumFacing.EAST)][mates.resultBit(EnumFacing.WEST)]
                [mates.resultBit(EnumFacing.NORTH)][mates.resultBit(EnumFacing.SOUTH)];

        return (finder.getRecipe(test, world, pos) * this.getAlternateTextureCount() + alternator.getAlternate(pos));
    }

    @Override
    public int getShapeCount()
    {
        return 386 * getAlternateTextureCount();
    }

}
