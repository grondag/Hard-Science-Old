package grondag.adversity.niceblock.newmodel;

import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.niceblock.newmodel.BlockTests.TestForBigBlockMatch;
import grondag.adversity.niceblock.support.CornerStateFinder;
import grondag.adversity.niceblock.support.ICollisionHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;

public class BorderController extends ModelControllerNew
{

    protected final IAlternator alternator;

    protected BorderController(String textureName, int alternateTextureCount, EnumWorldBlockLayer renderLayer, boolean isShaded)
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
            return modelState.getClientShapeIndex(this.renderLayer.ordinal()) % alternateTextureCount;
    }

    @Override
    public int getClientShapeIndex(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        int colorIndex = block.blockModelHelper.getModelStateForBlock(state, world, pos, false).getColorIndex();
        TestForBigBlockMatch test = new TestForBigBlockMatch(block, colorIndex, state.getValue(NiceBlock.META));
        NeighborBlocks neighbors = new NeighborBlocks(world, pos);
        NeighborTestResults mates = neighbors.getNeighborTestResults(test);

        CornerStateFinder finder = ModelReference.CONNECTED_CORNER_STATE_LOOKUP[mates.upBit()][mates.downBit()]
                [mates.eastBit()][mates.westBit()]
                [mates.northBit()][mates.southBit()];

        return (finder.getRecipe(test, world, pos) * this.alternateTextureCount + alternator.getAlternate(pos));
    }

    @Override
    public int getShapeCount()
    {
        return 386 * alternateTextureCount;
    }

}
