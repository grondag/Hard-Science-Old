package grondag.adversity.niceblock.modelstate;

import grondag.adversity.library.NeighborBlocks.HorizontalCorner;
import grondag.adversity.library.NeighborBlocks.HorizontalFace;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.base.NiceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
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
    public long getBitsFromWorld(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return getBitsFromWorldStatically(block, state, world, pos);
    }

    public static FlowHeightState getFlowState(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return new FlowHeightState(getBitsFromWorldStatically(block, state, world, pos));
    }

    private static long getBitsFromWorldStatically(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        int centerHeight;
        int sideHeight[] = new int[4];
        int cornerHeight[] = new int[4];
        int yOffset = 0;

        //        Adversity.log.info("flowstate getBitsFromWorld @" + pos.toString());

        int yOrigin = pos.getY();
        IBlockState originState = state;

        if(block.isFlowFiller())
        {
            int offset = IFlowBlock.getYOffsetFromState(state);
            yOrigin -= offset;
            yOffset = offset;
            originState = world.getBlockState(pos.down(offset));
            if(!IFlowBlock.isFlowHeight(originState.getBlock()))
            {
                return FlowHeightState.EMPTY_BLOCK_STATE_KEY;
            }
        }
        else
        {
            // If under another flow height block, handle similar to filler block.
            // Not a perfect fix if they are stacked, but shouldn't normally be.
            //                Adversity.log.info("flowstate is height block");

            // try to use block above as height origin
            originState = world.getBlockState(pos.up().up());
            if(IFlowBlock.isFlowHeight(originState.getBlock()))
            {
                yOrigin += 2;
                yOffset = -2;
                //                    Adversity.log.info("origin 2 up");
            }
            else
            {
                originState = world.getBlockState(pos.up());
                if(IFlowBlock.isFlowHeight(originState.getBlock()))
                {
                    yOrigin += 1;
                    yOffset = -1;
                    //                        Adversity.log.info("origin 1 up");
                }
                else
                {
                    // didn't work, handle as normal height block
                    originState = state;
                    //                        Adversity.log.info("origin self");
                }
            }
        }

        int[][] neighborHeight = new int[3][3];
        neighborHeight[1][1] = IFlowBlock.getFlowHeightFromState(originState);

        for(int x = 0; x < 3; x++)
        {
            for(int z = 0; z < 3; z++)
            {
                if(x == 1 && z == 1 ) continue;

                neighborHeight[x][z] = getFlowHeight(world, new BlockPos(pos.getX() - 1 + x, yOrigin, pos.getZ() - 1 + z));

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

    private static int getFlowHeight(IBlockAccess world, BlockPos center)
    {
        MutableBlockPos pos = new MutableBlockPos(center.up(2));
        
        IBlockState state = world.getBlockState(pos);
        
        if(IFlowBlock.isFlowHeight(state.getBlock()))
            return 2 * FlowHeightState.BLOCK_LEVELS_INT + IFlowBlock.getFlowHeightFromState(state);

        pos.setY(pos.getY() - 1);
        state = world.getBlockState(pos);

        if(IFlowBlock.isFlowHeight(state.getBlock()))
            return FlowHeightState.BLOCK_LEVELS_INT + IFlowBlock.getFlowHeightFromState(state);

        pos.setY(pos.getY() - 1);
        state = world.getBlockState(pos);

        if(IFlowBlock.isFlowHeight(state.getBlock()))
            return IFlowBlock.getFlowHeightFromState(state);

        pos.setY(pos.getY() - 1);
        state = world.getBlockState(pos);

        if(IFlowBlock.isFlowHeight(state.getBlock()))
            return -FlowHeightState.BLOCK_LEVELS_INT + IFlowBlock.getFlowHeightFromState(state);

        pos.setY(pos.getY() - 1);
        state = world.getBlockState(pos);

        if(IFlowBlock.isFlowHeight(state.getBlock()))
            return -2 * FlowHeightState.BLOCK_LEVELS_INT + IFlowBlock.getFlowHeightFromState(state);

        return FlowHeightState.NO_BLOCK;
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
