package grondag.adversity.niceblock;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import grondag.adversity.library.NeighborBlocks.HorizontalCorner;
import grondag.adversity.library.NeighborBlocks.HorizontalFace;
import grondag.adversity.library.model.QuadFactory.QuadInputs;
import grondag.adversity.library.model.QuadFactory.QuadInputs.LightingMode;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.base.ModelController;
import grondag.adversity.niceblock.base.NiceBlock;
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
        super(textureName, alternateTextureCount, renderLayer, lightingMode == QuadInputs.LightingMode.SHADED, false);
        this.alternator = Alternator.getAlternator(alternateTextureCount);
        this.bakedModelFactory = new FlowModelFactory(this);
        this.lightingMode = lightingMode;
    }

    @Override
    public long getClientShapeIndex(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {

        /**
         * Height depends on flow height of surrounding blocks, with the complication
         * that height of the block above or below a neighbor block counts.
         * 
         * 4 bits are used for this block (height 1 to 15)
         * 4 bits are used for each of the surrounding blocks (0 to 15)
         */

        FlowHeightState flowState = new FlowHeightState(0);

        if(block instanceof IFlowBlock)
        {

            Block b;
            IBlockState bs;
            int[][] neighborHeight = new int[3][3];
            
            bs = world.getBlockState(new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ()));
            b = bs.getBlock();
            boolean isUnderFlow  = b instanceof IFlowBlock;
                
            for(int x = 0; x < 3; x++)
            {
                for(int z = 0; z < 3; z++)
                {
                    if(isUnderFlow)
                    {
                        neighborHeight[x][z] = 15;
                    }
                    else
                    {
                        neighborHeight[x][z] = 0;
                        
                        bs = world.getBlockState(new BlockPos(pos.getX() - 1 + x, pos.getY() + 1, pos.getZ() - 1 + z));
                        b = bs.getBlock();
                        if(b instanceof IFlowBlock)
                        {
                            neighborHeight[x][z] = 15;
                        }
                        else
                        {
                            bs = world.getBlockState(new BlockPos(pos.getX() - 1 + x, pos.getY(), pos.getZ() - 1 + z));
                            b = bs.getBlock();
                            if(b instanceof IFlowBlock)
                            {
                                neighborHeight[x][z] = ((IFlowBlock)b).getRenderHeightFromState(bs);
                            }
                        }
                    }
                }
            }
            
            flowState.setCenterHeight(neighborHeight[1][1]);

            for(HorizontalFace side : HorizontalFace.values())
            {
                flowState.setSideHeight(side, neighborHeight[side.directionVector.getX() + 1][side.directionVector.getZ() + 1]);
            }
            
            for(HorizontalCorner corner : HorizontalCorner.values())
            {
                flowState.setCornerHeight(corner, neighborHeight[corner.directionVector.getX() + 1][corner.directionVector.getZ() + 1]);
            }
        }

        return flowState.getStateKey() | (this.alternator.getAlternate(pos) << 36);

    }


    @Override
    public int getAltTextureFromModelIndex(long clientShapeIndex)
    {
        return (int) (clientShapeIndex >>> 36);
    }

    public FlowHeightState getFlowHeightStateFromModelIndex(long clientShapeIndex)
    {
        return new FlowHeightState(clientShapeIndex & 0xFFFFFFFFFL);
    }

    @Override
    public ICollisionHandler getCollisionHandler()
    {
        return this;
    }

    @Override
    public long getCollisionKey(World worldIn, BlockPos pos, IBlockState state)
    {
        return ((IFlowBlock)state.getBlock()).getRenderHeightFromState(state);

    }

    @Override
    public List<AxisAlignedBB> getModelBounds(long collisionKey)
    {
        ImmutableList<AxisAlignedBB> retVal = new ImmutableList.Builder<AxisAlignedBB>().add(new AxisAlignedBB(0, 0, 0, 1, (collisionKey + 1)/16.0, 1)).build();
        return retVal;
    }
    
    public class FlowHeightState
    {
        private long stateKey;

        public long getStateKey()
        {
            return stateKey;
        }

        private FlowHeightState(long stateKey)
        {
            this.stateKey = stateKey;
        }

        // Rendering height of center block ranges from 1 to 16
        // and is stored in state key as values 0-15.

        public void setCenterHeight(int height)
        {
            stateKey |= (height - 1);
        }

        public int getCenterHeight()
        {
            return (int) (stateKey & 0xF) + 1;
        }


        // Rendering height of corner and side neighbors ranges from 0 to 15

        public void setSideHeight(HorizontalFace side, int height)
        {
            stateKey |= ((long)(height) << (4 + side.ordinal() * 4));
        }

        public int getSideHeight(HorizontalFace side)
        {
            return (int) ((stateKey >> (4 + side.ordinal() * 4)) & 0xF);
        }


        public void setCornerHeight(HorizontalCorner corner, int height)
        {
            stateKey |= ((long)(height) << (20 + corner.ordinal() * 4));
        }

        public int getCornerHeight(HorizontalCorner corner)
        {
            return (int) ((stateKey >> (20 + corner.ordinal() * 4)) & 0xF);
        }
    }
}
