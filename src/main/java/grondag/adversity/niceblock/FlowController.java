package grondag.adversity.niceblock;

import java.util.List;

import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import grondag.adversity.library.NeighborBlocks.HorizontalCorner;
import grondag.adversity.library.NeighborBlocks.HorizontalFace;
import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.base.ModelController;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.support.CollisionBoxGenerator;
import grondag.adversity.niceblock.support.ICollisionHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
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

    @Override
    public long getDynamicShapeIndex(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        int centerHeight;
        int sideHeight[] = new int[4];
        int cornerHeight[] = new int[4];
        int yOffset = 0;
        long key;
        
        if(block instanceof IFlowBlock)
        {
            int yOrigin = pos.getY();
            IBlockState originState = state;

            // If under another flow height block, handle similar to filler block.
            // Not a perfect fix if they are stacked, but shouldn't normally be.
            if(block instanceof IFlowBlock.IHeightBlock)
            {
                // try to use block above as height origin
                originState = world.getBlockState(pos.up());
                if(originState.getBlock() instanceof IFlowBlock.IHeightBlock)
                {
                    yOrigin++;
                    yOffset = -1;
                }
                else
                {
                    // didn't work, go back to using this block
                    originState = state;
                }
            }
            else if(block instanceof IFlowBlock.IFillerBlock)
            {
                int offset = IFlowBlock.IFillerBlock.getYOffsetFromState(state);
                yOrigin -= offset;
                yOffset = offset;
                originState = world.getBlockState(pos.down(offset));
                if(!(originState.getBlock() instanceof IFlowBlock.IHeightBlock))
                {
                    return FlowHeightState.FULL_BLOCK_STATE_KEY;
                }
            }
            
            int[][] neighborHeight = new int[3][3];
            neighborHeight[1][1] = IFlowBlock.IHeightBlock.getFlowHeightFromState(originState);

            Block b;
            IBlockState bs;

            for(int x = 0; x < 3; x++)
            {
                for(int z = 0; z < 3; z++)
                {
                    if(!(x == 1 && z == 1)) 
                    {
                        neighborHeight[x][z] = FlowHeightState.NO_BLOCK;
                        for(int y = 2; y >= -2; y--)
                        {
                            bs = world.getBlockState(new BlockPos(pos.getX() - 1 + x, yOrigin + y, pos.getZ() - 1 + z));
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
            
            centerHeight = neighborHeight[1][1];
            
            for(HorizontalFace side : HorizontalFace.values())
            {
                sideHeight[side.ordinal()] = neighborHeight[side.directionVector.getX() + 1][side.directionVector.getZ() + 1];
            }
            
            for(HorizontalCorner corner : HorizontalCorner.values())
            {
                cornerHeight[corner.ordinal()] = neighborHeight[corner.directionVector.getX() + 1][corner.directionVector.getZ() + 1];
            }
            
            key = FlowHeightState.computeStateKey(centerHeight, sideHeight, cornerHeight, yOffset);
        }
        else
        {
            key = FlowHeightState.FULL_BLOCK_STATE_KEY;
        }
        
         return key | (this.alternator.getAlternate(pos) << FlowHeightState.STATE_BIT_COUNT);
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
        Block block = state.getBlock();
        if(block instanceof IFlowBlock)
        {
            return this.getDynamicShapeIndex((NiceBlock) block, state, worldIn, pos);
        }
        else
        {
            return 0;
        }
    }

    @Override
    public List<AxisAlignedBB> getModelBounds(long collisionKey)
    {
        return CollisionBoxGenerator.makeCollisionBox(((FlowModelFactory)this.bakedModelFactory).makeRawQuads(collisionKey));
    }
}
