package grondag.adversity.niceblock;

import grondag.adversity.Adversity;
import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import grondag.adversity.library.NeighborBlocks.HorizontalCorner;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.base.ModelController;
import grondag.adversity.niceblock.base.NiceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class FlowController extends ModelController
{

    protected final IAlternator alternator;

    protected FlowController(String textureName, int alternateTextureCount, BlockRenderLayer renderLayer, boolean isShaded)
    {
        super(textureName, alternateTextureCount, renderLayer, isShaded, false);
        this.alternator = Alternator.getAlternator(alternateTextureCount);
        this.bakedModelFactory = new FlowModelFactory(this);
    }

    @Override
    public int getClientShapeIndex(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        
        if(!(block instanceof IFlowBlock)) return 0;
        
        float[][] neighborHeight = new float[3][3];
        float[] cornerHeight = new float[4];
        neighborHeight[1][1] = ((IFlowBlock)block).getRenderHeightFromState(state);
//        Adversity.log.info("neighborHeight 1, 1" + " = " + neighborHeight[1][1]);

        if(neighborHeight[1][1] == 1)
        {
            cornerHeight[0] = 1;
            cornerHeight[1] = 1;
            cornerHeight[2] = 1;
            cornerHeight[3] = 1;
        }
        else
        {
            Block b;
            IBlockState bs;
  
            for(int x = 0; x < 3; x++)
            {
                for(int z = 0; z < 3; z++)
                {
                    if(x != 1 || z != 1)
                    {
                        bs = world.getBlockState(pos.add(x - 1, 0, z - 1));
                        b = bs.getBlock();
                        if(b instanceof IFlowBlock)
                        {
                            neighborHeight[x][z] = ((IFlowBlock)b).getRenderHeightFromState(bs);
                        }
                        else
                        {
                            neighborHeight[x][z] = -1;
                        }
//                        Adversity.log.info("neighborHeight " + x + ", " + z + " = " + neighborHeight[x][z]);
                    }
                }
            }

            //TODO: less ugly way to handle this, ideally with less looping
            int cornerFlags = 0;

            
            // strong corner = corner w/ 2 or 3 neighbor flow blocks
            // weak corner = corner w/ 1 neighbor flow blocks
            // orphan corner = corner w/ no neighbor flow blocks
            
            // orphan corners go to zero if there is at least one weak or strong corner
            //weak corners go to zero if there is at least one strong corner
            for(HorizontalCorner corner : HorizontalCorner.values())
            {
                int flowCount = 0;
                if(neighborHeight[1 + corner.directionVector.getX()][1] != -1) flowCount++;
                if(neighborHeight[1][1 + corner.directionVector.getZ()] != -1) flowCount++;
                if(neighborHeight[1 + corner.directionVector.getX()][1 + corner.directionVector.getZ()] != -1) flowCount++;
                
                if(flowCount == 0)
                {
                    continue;
                }
                else if(flowCount == 1)
                {
                    // weak corner
                    cornerFlags |= (1 << corner.ordinal());
                }
                else
                {
                    // strong corner
                    cornerFlags |= (1 << (corner.ordinal() + 4));
                }
             }
            
            for(HorizontalCorner corner : HorizontalCorner.values())
            {
                if((cornerFlags > 0xF && (cornerFlags & (1 << (corner.ordinal() + 4))) == 0)
                        || (cornerFlags > 0 && cornerFlags <= 0xF && (cornerFlags & (1 << corner.ordinal())) == 0))
                {
                    cornerHeight[corner.ordinal()] = 0;
                }
                else
                {
                    cornerHeight[corner.ordinal()] = getAverageHeight(
                            neighborHeight[1][1], 
                            neighborHeight[1 + corner.directionVector.getX()][1], 
                            neighborHeight[1][1 + corner.directionVector.getZ()], 
                            neighborHeight[1 + corner.directionVector.getX()][1 + corner.directionVector.getZ()]);
                }
                //Adversity.log.info("cornerHeight " + corner.toString() + " = " + cornerHeight[corner.ordinal()]);
            }
        }
        
        int shapeIndex = Math.round(cornerHeight[0] * 15);
        shapeIndex |= (Math.round(cornerHeight[1] * 15) << 4);
        shapeIndex |= (Math.round(cornerHeight[2] * 15) << 8);
        shapeIndex |= (Math.round(cornerHeight[3] * 15) << 12);
        
//        Adversity.log.info("shapeIndex = " + shapeIndex);

        
        return (this.alternator.getAlternate(pos)  * this.getShapeCount()) + shapeIndex;

    }

    private float getAverageHeight(float... height)
    {
        float total = 0;
        int count = 0;

        for (int i = 0; i < height.length; i++)
        {
            if (height[i] == 1F) return 1F;

            if (height[i] >= 0)
            {
                total += height[i];
                count++;
            }
        }

        if (count > 0) return total / count;
        else return 0;
    }
    
//    @Override
//    public int getShapeCount()
//    {
//        return 0x10000; 
//    }

    @Override
    public int getAltTextureFromModelIndex(long clientShapeIndex)
    {
        return (int) (clientShapeIndex >>> 16);
    }
    
    public float getCornerHeightFromModelIndex(long clientShapeIndex, HorizontalCorner corner)
    {
        float retVal = (float)((clientShapeIndex >>> (corner.ordinal() * 4)) & 0xF) / 15;
//        Adversity.log.info("clientShapeIndex=" + clientShapeIndex + ", corner=" + corner.toString()
//        + ", output=" + retVal);
        return retVal;
    }
}
