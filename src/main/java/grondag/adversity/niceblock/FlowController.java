package grondag.adversity.niceblock;

import java.util.BitSet;
import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.Adversity;
import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import grondag.adversity.library.NeighborBlocks.HorizontalCorner;
import grondag.adversity.library.NeighborBlocks.HorizontalFace;
import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.base.ModelController;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.support.ICollisionHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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
        FlowHeightState flowState = new FlowHeightState(0);
        flowState.setYOffset(0);
        
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
                    flowState.setYOffset(-1);
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
                flowState.setYOffset(offset);
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
        
        List<RawQuad> quads = ((FlowModelFactory)this.bakedModelFactory).makeRawQuads(collisionKey);

        // simple method
//        AxisAlignedBB simpleBox = null;
//        for(RawQuad quad : quads)
//        {
//            if(simpleBox == null)
//            {
//                simpleBox = quad.getAABB();
//            }
//            else
//            {
//                simpleBox = simpleBox.union(quad.getAABB());
//            }
//        }
        
        // voxel method
        ImmutableList.Builder<AxisAlignedBB> retVal = new ImmutableList.Builder<AxisAlignedBB>();
        Vec3d direction = new Vec3d(0, 1, 0);
        
        boolean corners[][][] = new boolean[9][9][9];
        for(int x = 0; x < 9; x++)
        {
            for(int y = 0; y < 9; y++)
            {
                for(int z = 0; z < 9; z++)
                {
                    int intersectionCount = 0;
                    for(RawQuad quad : quads)
                    {
                        if(quad.intersectsWithRay(new Vec3d(x/8.0, y/8.0, z/8.0), direction))
                        {
                            intersectionCount++;
                        }
                    }
                    corners[x][y][z] = (intersectionCount & 0x1) == 1;
                }
            }
        }
            
        boolean voxels[][][] = new boolean[8][8][8];
        VoxelBitField voxelBits = new VoxelBitField(3);
                                
        for(int x = 0; x < 8; x++)
        {
            for(int y = 0; y < 8; y++)
            {
                for(int z = 0; z < 8; z++)
                {
                    voxels[x][y][z] = corners[x][y][z] 
                            || corners[x+1][y][z]
                            || corners[x][y][z+1]
                            || corners[x+1][y][z+1]
                            || corners[x][y+1][z]
                            || corners[x+1][y+1][z]
                            || corners[x][y+1][z+1]
                            || corners[x+1][y+1][z+1];
                    
                    if(voxels[x][y][z])
                    {
                        voxelBits.setFilled(x, y, z, true);
                        //retVal.add(new AxisAlignedBB(x/8.0, y/8.0, z/8.0, (x+1.0)/8.0, (y+1.0)/8.0, (z+1.0)/8.0));
                          
                    }
                }
            }
        }
        
        for(int x = 0; x < 8; x++)
        {
            for(int z = 0; z < 8; z++)
            {
                for(int y = 0; y < 8; y++)
                {
                    if(voxelBits.isFilled(x, y, z))
                    {
                        int minX = x;
                        int minY = y;
                        int minZ = z;
                        int maxX = x;
                        int maxY = y;
                        int maxZ = z;
                        boolean xDone = false;
                        boolean yDone = false;
                        boolean zDone = false;
                        
                        while(!(xDone && yDone && zDone))
                        {
                            if(!xDone && maxX < 7 && voxelBits.isFilled(minX, minY, minZ, maxX + 1, maxY, maxZ))
                            {
                                maxX++;
                            }
                            else
                            {
                                xDone = true;
                            }
                                
                            
                            if(!zDone && maxZ < 7 && voxelBits.isFilled(minX, minY, minZ, maxX, maxY, maxZ + 1))
                            {
                                maxZ++;
                            }
                            else
                            {
                                zDone = true;
                            }
    
                            if(!yDone && maxY < 7 && voxelBits.isFilled(minX, minY, minZ, maxX, maxY + 1, maxZ))
                            {
                                maxY++;
                            }
                            else
                            {
                                yDone = true;
                            }
                        }
                        voxelBits.setFilled(minX, minY, minZ, maxX, maxY, maxZ, false);
                        retVal.add(new AxisAlignedBB(minX/8.0, minY/8.0, minZ/8.0, (maxX+1.0)/8.0, (maxY+1.0)/8.0, (maxZ+1.0)/8.0));
                    }
                }
            }
        }
        
 //       if(simpleBox == null) simpleBox = new AxisAlignedBB(0, 0, 0, 1, 1, 1);
        
        //Adversity.log.info("box " + simpleBox.minX + " " + simpleBox.minY + " " + simpleBox.minZ + " " + simpleBox.maxX + " " + simpleBox.maxY + " " + simpleBox.maxZ );

        //return new ImmutableList.Builder<AxisAlignedBB>().add(simpleBox).build();
        return retVal.build();
    }
}
