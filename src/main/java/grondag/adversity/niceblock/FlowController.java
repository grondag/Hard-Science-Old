package grondag.adversity.niceblock;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.BlockVolcanicLava;
import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import grondag.adversity.library.IBlockTest;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.HorizontalCorner;
import grondag.adversity.library.NeighborBlocks.HorizontalFace;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.base.ModelController;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.support.ICollisionHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class FlowController extends ModelController implements ICollisionHandler
{

    protected final IAlternator alternator;

    public final LightingMode lightingMode;

    protected FlowController(String textureName, int alternateTextureCount, BlockRenderLayer renderLayer, LightingMode lightingMode)
    {
        super(textureName, alternateTextureCount, renderLayer, lightingMode == LightingMode.SHADED, false);
        this.alternator = Alternator.getAlternator(alternateTextureCount);
        this.bakedModelFactory = new FlowModelFactory(this);
        this.lightingMode = lightingMode;
    }

    public final static int NO_BLOCK = -33;

    @Override
    public long getClientShapeIndex(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        FlowHeightState flowState = new FlowHeightState(0);
        
        
        if(block instanceof IFlowBlock.IHeightBlock)
        {
            // if under another flow height block, treat as full height
            if(world.getBlockState(pos.up()).getBlock() instanceof IFlowBlock.IHeightBlock)
            {
                return FlowHeightState.FULL_BLOCK_STATE_KEY;
            }

            int[][] neighborHeight = new int[3][3];
            neighborHeight[1][1] = IFlowBlock.IHeightBlock.getFlowHeightFromState(state);

            Block b;
            IBlockState bs;

            for(int x = 0; x < 3; x++)
            {
                for(int z = 0; z < 3; z++)
                {
                    if(!(x == 1 && z == 1)) 
                    {
                        neighborHeight[x][z] = NO_BLOCK;
                        for(int y = 2; y >= -2; y--)
                        {
                            bs = world.getBlockState(new BlockPos(pos.getX() - 1 + x, pos.getY() + y, pos.getZ() - 1 + z));
                            b = bs.getBlock();
                            if(b instanceof IFlowBlock.IHeightBlock)
                            {
                                neighborHeight[x][z] = y * 16 + IFlowBlock.IHeightBlock.getFlowHeightFromState(bs);
                                break;
                            }
                        }
                    }
                }
            }


            flowState.setCenterHeight(neighborHeight[1][1]);

            for(HorizontalFace side : HorizontalFace.values())
            {
                int h = neighborHeight[side.directionVector.getX() + 1][side.directionVector.getZ() + 1];
                flowState.setSideHeight(side, h);

            }
            
            for(HorizontalCorner corner : HorizontalCorner.values())
            {
                int c = neighborHeight[corner.directionVector.getX() + 1][corner.directionVector.getZ() + 1];
                flowState.setCornerHeight(corner, c);
            }
        }
        
        return flowState.getStateKey() | (this.alternator.getAlternate(pos) << FlowHeightState.STATE_BIT_COUNT);
    }

    @Override
    public int getAltTextureFromModelIndex(long clientShapeIndex)
    {
        return (int) (clientShapeIndex >>> FlowHeightState.STATE_BIT_COUNT);
    }

    public FlowHeightState getFlowHeightStateFromModelIndex(long clientShapeIndex)
    {
        return new FlowHeightState(clientShapeIndex & FlowHeightState.STATE_BIT_MASK);
    }

    @Override
    public ICollisionHandler getCollisionHandler()
    {
        return this;
    }

    @Override
    public long getCollisionKey(World worldIn, BlockPos pos, IBlockState state)
    {
        if(state.getBlock() instanceof IFlowBlock.IHeightBlock)
        {
            return IFlowBlock.IHeightBlock.getFlowHeightFromState(state);
        }
        else
        {
            return 16;
        }

    }

    @Override
    public List<AxisAlignedBB> getModelBounds(long collisionKey)
    {
        ImmutableList<AxisAlignedBB> retVal = new ImmutableList.Builder<AxisAlignedBB>().add(new AxisAlignedBB(0, 0, 0, 1, (collisionKey + 1)/16.0, 1)).build();
        return retVal;
    }
}
