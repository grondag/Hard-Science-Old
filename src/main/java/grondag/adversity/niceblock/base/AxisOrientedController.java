package grondag.adversity.niceblock.base;

import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.library.joinstate.CornerJoinBlockStateSelector;
import grondag.adversity.niceblock.support.BlockTests;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.IBlockAccess;

public abstract class AxisOrientedController extends ModelController
{

    /**
     * Locate models for each scenario.  The models for this type of block
     * are rotated as needed so noisy textures are best avoided.
     */
   // protected String[] modelNames = new String[AxisAlignedModel.values().length];
    
    protected final IAlternator alternator;

    /**
     * Cache collision box lists.
     * Override getCollisionHandler and getModelBounds if need something 
     * other than standard cubes.  Has to be here and not in parent because
     * the number of models is specific to cookbook.
     */
    public final ModelType modelType;
    
    protected AxisOrientedController(String textureName, int alternateTextureCount, ModelType modelType, boolean isShaded)
    {
        super(textureName, alternateTextureCount, modelType == ModelType.LAMP_OVERLAY ? BlockRenderLayer.CUTOUT_MIPPED : BlockRenderLayer.SOLID, isShaded, false);
        this.alternator = Alternator.getAlternator((byte)(alternateTextureCount));
        this.modelType = modelType;
    }
    
    
    @Override
    public long getDynamicShapeIndex(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        int axis = Math.max(0, Math.min(2, state.getValue(NiceBlock.META)));
        NeighborTestResults tests = new NeighborBlocks(world, pos).getNeighborTestResults(new BlockTests.TestForBlockMetaMatch(state));
        
        int shapeIndex = CornerJoinBlockStateSelector.findIndex(tests);
        
         return (this.alternator.getAlternate(pos) * 3 + axis) * CornerJoinBlockStateSelector.BLOCK_JOIN_STATE_COUNT + shapeIndex;
    }

//    @Override
//    public int getShapeCount()
//    {
//        return CornerJoinBlockStateSelector.BLOCK_JOIN_STATE_COUNT * 3 * getAlternateTextureCount();
//    }

    public int getAxisFromModelIndex(long clientShapeIndex)
    {
        return (int) (clientShapeIndex / CornerJoinBlockStateSelector.BLOCK_JOIN_STATE_COUNT) % 3;
    }

    @Override
    public int getAltTextureFromModelIndex(long clientShapeIndex)
    {
        return (int) (clientShapeIndex / CornerJoinBlockStateSelector.BLOCK_JOIN_STATE_COUNT) / 3;
    }
    
    public int getShapeFromModelIndex(long clientShapeIndex)
    {
        return (int) (clientShapeIndex % CornerJoinBlockStateSelector.BLOCK_JOIN_STATE_COUNT);
    }

    public static enum ModelType
    {
        NORMAL,
        LAMP_BASE,
        LAMP_OVERLAY;
    }
}
