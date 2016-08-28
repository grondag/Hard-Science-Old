package grondag.adversity.niceblock.modelstate;

import grondag.adversity.library.NeighborBlocks.HorizontalCorner;
import grondag.adversity.library.NeighborBlocks.HorizontalFace;
import grondag.adversity.niceblock.FlowHeightState;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.base.NiceBlock2;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ModelFlowJoinComponent extends ModelStateComponent<ModelFlowJoinComponent.ModelFlowJoin, FlowHeightState>
{    
    public ModelFlowJoinComponent(int ordinal, WorldRefreshType refreshType)
    {
        super(ordinal, refreshType, FlowHeightState.STATE_BIT_MASK);
    }

    @Override
    public ModelFlowJoin createValueFromBits(long bits)
    {
        return new ModelFlowJoin(new FlowHeightState(bits));
    }

    @Override
    public Class<ModelFlowJoin> getStateType()
    {
        return ModelFlowJoinComponent.ModelFlowJoin.class;
    }

    @Override
    public Class<FlowHeightState> getValueType()
    {
        return FlowHeightState.class;
    }
    
    @Override
    public long getBitsFromWorld(NiceBlock2 block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        int centerHeight;
        int sideHeight[] = new int[4];
        int cornerHeight[] = new int[4];
        int yOffset = 0;

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
            
            return FlowHeightState.computeStateKey(centerHeight, sideHeight, cornerHeight, yOffset);
        }
        else
        {
            return FlowHeightState.FULL_BLOCK_STATE_KEY;
        }
        
    }

    public class ModelFlowJoin extends ModelStateValue<ModelFlowJoinComponent.ModelFlowJoin, FlowHeightState>
    {
        ModelFlowJoin(FlowHeightState valueIn)
        {
            super(valueIn);
        }

        @Override
        public ModelStateComponent<ModelFlowJoinComponent.ModelFlowJoin, FlowHeightState> getComponent()
        {
            return ModelFlowJoinComponent.this;
        }

        @Override
        public long getBits()
        {
            return this.value.getStateKey();
        }
    }
}
