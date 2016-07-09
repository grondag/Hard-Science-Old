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
import grondag.adversity.library.model.quadfactory.QuadFactory.QuadInputs;
import grondag.adversity.library.model.quadfactory.QuadFactory.QuadInputs.LightingMode;
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
        super(textureName, alternateTextureCount, renderLayer, lightingMode == QuadInputs.LightingMode.SHADED, false);
        this.alternator = Alternator.getAlternator(alternateTextureCount);
        this.bakedModelFactory = new FlowModelFactory(this);
        this.lightingMode = lightingMode;
    }

    private static IBlockTest testIsFlowBlock = new IBlockTest() {
        @Override
        public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos) {
            return ibs.getBlock() instanceof IFlowBlock;
        }
    };

    public final static int NO_BLOCK = -33;

    @Override
    public long getClientShapeIndex(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        FlowHeightState flowState = new FlowHeightState(0);
        
        if(block instanceof IFlowBlock)
        {
 
            Block b;
            IBlockState bs;
            int[][] neighborHeight = new int[3][3];

            for(int x = 0; x < 3; x++)
            {
                for(int z = 0; z < 3; z++)
                {
                    neighborHeight[x][z] = NO_BLOCK;

                    for(int y = 2; y >= -2; y--)
                    {
                        bs = world.getBlockState(new BlockPos(pos.getX() - 1 + x, pos.getY() + y, pos.getZ() - 1 + z));
                        b = bs.getBlock();
                        if(b instanceof IFlowBlock)
                        {
                            neighborHeight[x][z] = (y + 1 ) * 16 - bs.getValue(NiceBlock.META);
                            break;
                        }
                    }
                }
            }

            int centerHeight = 16 - state.getValue(NiceBlock.META);
            flowState.setCenterHeight(centerHeight);

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
        
        return flowState.getStateKey() | (this.alternator.getAlternate(pos) << 60);
    }

    //@Override
    public long getClientShapeIndexOld(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
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

            NeighborBlocks neighbors = new NeighborBlocks(world, pos);



            NeighborTestResults isFlow =  neighbors.getNeighborTestResults(testIsFlowBlock);


            //            Block b;
            //            IBlockState bs;
            //            int[][] neighborHeight = new int[3][3];
            //                        
            //            if(isFlow.result(EnumFacing.UP))
            //            {
            //                flowState.setSideHeight(HorizontalFace.NORTH, 49);
            //                return flowState.getStateKey() | (this.alternator.getAlternate(pos) << 52);
            //            }
            //            
            //            bs = world.getBlockState(new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ()));
            //            b = bs.getBlock();
            //            boolean isOverFlow  = b instanceof IFlowBlock;
            //            
            //            for(int x = 0; x < 3; x++)
            //            {
            //                for(int z = 0; z < 3; z++)
            //                {
            ////                    if(isUnderFlow)
            ////                    {
            ////                        // if under another flow block, take the appearance of full cube
            ////                        neighborHeight[x][z] = 16;
            ////                    }
            ////                    else
            //                    {
            //                        neighborHeight[x][z] = 0;
            //                        boolean upPresent = false;
            //                        
            //                        bs = world.getBlockState(new BlockPos(pos.getX() - 1 + x, pos.getY() + 1, pos.getZ() - 1 + z));
            //                        b = bs.getBlock();
            //                        if(b instanceof IFlowBlock)
            //                        {
            //                            neighborHeight[x][z] = 32 - bs.getValue(NiceBlock.META);
            //                            upPresent = true;
            //                        }
            //                        
            //                        bs = world.getBlockState(new BlockPos(pos.getX() - 1 + x, pos.getY(), pos.getZ() - 1 + z));
            //                        b = bs.getBlock();
            //                        
            //                        if(b instanceof IFlowBlock)
            //                        {
            //                            if(!upPresent)
            //                            {
            //                                neighborHeight[x][z] = 16 - bs.getValue(NiceBlock.META);
            //                        
            //                            }
            //                        }
            //                        else 
            //                        {
            //                            if(upPresent)
            //                            {
            //                                // if we had an up block but no middle, then up block doesn't count
            //                                neighborHeight[x][z] = 0;
            //                            }
            //                            
            //                            // bottom doesn't count unless center is over a block
            //                            if(isOverFlow)
            //                                {    
            //                                bs = world.getBlockState(new BlockPos(pos.getX() - 1 + x, pos.getY() - 1, pos.getZ() - 1 + z));
            //                                b = bs.getBlock();
            //                                if(b instanceof IFlowBlock)
            //                                {
            //                                    neighborHeight[x][z] = 0 - bs.getValue(NiceBlock.META);
            //                                }
            //                                else
            //                                {
            //                                    neighborHeight[x][z] = -16;
            //                                }
            //                            }
            //                        }
            //                    }
            //                }
            //            }

            flowState.setCenterHeight(16 - state.getValue(NiceBlock.META));

            for(HorizontalFace side : HorizontalFace.values())
            {

                if(isFlow.resultUp(side))
                {
                    flowState.setSideHeight(side, 32 - neighbors.getBlockStateUp(side).getValue(NiceBlock.META));
                }
                else if(isFlow.result(side))
                {
                    flowState.setSideHeight(side, 16 - neighbors.getBlockState(side).getValue(NiceBlock.META));
                }
                else if(isFlow.resultDown(side))
                {
                    flowState.setSideHeight(side, 0 - neighbors.getBlockStateDown(side).getValue(NiceBlock.META));
                }
                else
                {
                    //                  if((isFlow.result(EnumFacing.DOWN, side.left, side.face) && !isFlow.result(side.left, side.face))
                    //                      || (isFlow.result(EnumFacing.DOWN, side.right, side.face) && !isFlow.result(side.right, side.face))
                    //                          )
                    //                  {
                    flowState.setSideHeight(side, -16);
                    //                  }
                    //                  else
                    //                  {
                    //                      flowState.setSideHeight(side, 0);
                    //                  }
                }
                //                 if(isFlow.result(side))
                //                {
                //                    if(isFlow.resultUp(side))
                //                    {
                //                        flowState.setSideHeight(side, 32 - neighbors.getBlockStateUp(side).getValue(NiceBlock.META));
                //                    }
                //                    else
                //                    {
                //                        flowState.setSideHeight(side, 16 - neighbors.getBlockState(side).getValue(NiceBlock.META));
                //                    }
                //                }
                //                else
                //                {
                //                    if(isFlow.result(EnumFacing.DOWN))
                //                    {
                //                        if(isFlow.resultDown(side))
                //                        {
                //                            flowState.setSideHeight(side, 0 - neighbors.getBlockStateDown(side).getValue(NiceBlock.META));
                //                        }
                //                        else
                //                        {
                //                            flowState.setSideHeight(side, -16);
                //                        }
                //                    }
                //                    else
                //                    {
                //                        flowState.setSideHeight(side, 0);
                //                    }
                //                }
            }

            for(HorizontalCorner corner : HorizontalCorner.values())
            {
                //   boolean cornerFound = false;

                if(isFlow.resultUp(corner))
                {
                    //                    if((isFlow.resultUp(corner.face1) && isFlow.result(corner.face1))
                    //                            || (isFlow.resultUp(corner.face2) && isFlow.result(corner.face2))
                    //                            || (isFlow.result(corner)))
                    //                    {
                    flowState.setCornerHeight(corner, 32 - neighbors.getBlockStateUp(corner).getValue(NiceBlock.META));
                    //  cornerFound = true;
                    //                    }
                }
                else if(isFlow.result(corner))
                {
                    //                    if(isFlow.result(corner.face1) || isFlow.result(corner.face2))
                    {
                        flowState.setCornerHeight(corner, 16 - neighbors.getBlockState(corner).getValue(NiceBlock.META));
                        //   cornerFound = true;
                    }
                }
                else if(isFlow.resultDown(corner))
                {
                    //                    if((isFlow.resultDown(corner.face1) && isFlow.result(corner.face1))
                    //                            || (isFlow.resultDown(corner.face2) && isFlow.result(corner.face2))
                    //                            || isFlow.resultDown(corner))
                    {
                        flowState.setCornerHeight(corner, 0 - neighbors.getBlockStateDown(corner).getValue(NiceBlock.META));
                        //  cornerFound = true;
                    }
                }
                else // if (!cornerFound)
                {
                    flowState.setCornerHeight(corner, -16);

                    //                    // corner down must be connected some way to count
                    //                    if((isFlow.resultDown(corner.face1) && (isFlow.result(corner.face1) || isFlow.result(EnumFacing.DOWN)))
                    //                            || (isFlow.resultDown(corner.face2)  && (isFlow.result(corner.face2) || isFlow.result(EnumFacing.DOWN))))
                    //
                    //                    {

                    // for corner to be -16, must have an adjacent flow block that is not covered
                    //                  if((isFlow.resultDown(corner.face1) && !isFlow.result(corner.face1))
                    //                          || (isFlow.resultDown(corner.face2) && !isFlow.result(corner.face2))
                    //                  )
                    //                  {
                    //                      flowState.setCornerHeight(corner, -16);
                    //                  }
                    //                  else if((isFlow.result(corner.face1) && !isFlow.resultUp(corner.face1))
                    //                          || (isFlow.result(corner.face2) && !isFlow.resultUp(corner.face2))
                    //                  )
                    //                  {
                    //                      flowState.setCornerHeight(corner, 0);
                    //                  }
                    //
                    //                  else if((isFlow.resultUp(corner.face1))
                    //                          || (isFlow.resultUp(corner.face2))
                    //                  )
                    //                  {
                    //                      flowState.setCornerHeight(corner, 16);
                    //                  }
                    //                  else
                    //                  {
                    //                      flowState.setCornerHeight(corner, 0);
                    //                  }
                    //                    }
                    //                    else // if(isFlow.result(corner.face1) || isFlow.result(corner.face2))
                    //                    {
                    //                        flowState.setCornerHeight(corner, 0);
                    //                    }
                    //                    else if(neighbors.getBlockState(corner.face1).isFullCube() || neighbors.getBlockState(corner.face2).isFullCube())
                    //                    {
                    //                        flowState.setCornerHeight(corner, 16 - state.getValue(NiceBlock.META));
                    //                    }
                    //                    else
                    //                    {
                    //                        flowState.setCornerHeight(corner, 0);
                    //                    }  
                }

            }

        }


        return flowState.getStateKey() | (this.alternator.getAlternate(pos) << 60);

    }


    @Override
    public int getAltTextureFromModelIndex(long clientShapeIndex)
    {
        return (int) (clientShapeIndex >>> 60);
    }

    public FlowHeightState getFlowHeightStateFromModelIndex(long clientShapeIndex)
    {
        return new FlowHeightState(clientShapeIndex & 0xFFFFFFFFFFFFFFFL);
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

    public static class FlowHeightState
    {
        private long stateKey;

        public long getStateKey()
        {
            return stateKey;
        }

        FlowHeightState(long stateKey)
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


        // Rendering height of corner and side neighbors ranges 
        // from -32 to 48. 

        public void setSideHeight(HorizontalFace side, int height)
        {
            stateKey |= ((long)(height - NO_BLOCK) << (4 + side.ordinal() * 7));
        }

        public int getSideHeight(HorizontalFace side)
        {
            return (int) ((stateKey >> (4 + side.ordinal() * 7)) & 0x7F) + NO_BLOCK;
        }


        public void setCornerHeight(HorizontalCorner corner, int height)
        {
            stateKey |= ((long)(height - NO_BLOCK) << (32 + corner.ordinal() * 7));
        }

        public int getCornerHeight(HorizontalCorner corner)
        {
            return (int) ((stateKey >> (32 + corner.ordinal() * 7)) & 0x7F) + NO_BLOCK;
        }

        public String toString()
        {
            String retval = "Center=" + this.getCenterHeight();
            for(HorizontalFace side: HorizontalFace.values())
            {
                retval += " " + side.name() + "=" + this.getSideHeight(side);
            }
            for(HorizontalCorner corner: HorizontalCorner.values())
            {
                retval += " " + corner.name() + "=" + this.getCornerHeight(corner);
            }
            return retval;
        }
    }
}
