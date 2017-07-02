package grondag.adversity.superblock.terrain;

import grondag.adversity.library.render.QuadHelper;
import grondag.adversity.library.world.NeighborBlocks.HorizontalCorner;
import grondag.adversity.library.world.NeighborBlocks.HorizontalFace;
import grondag.adversity.superblock.block.SuperBlock;
import grondag.adversity.superblock.model.shape.ModelShape;
import grondag.adversity.superblock.model.state.ModelStateFactory;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IBlockAccess;

@SuppressWarnings("unused")
public class TerrainState
{

    public final static long FULL_BLOCK_STATE_KEY = TerrainState.computeStateKey(12, new int[] {12, 12, 12,12}, new int[] {12, 12, 12, 12}, 0 );
    public final static long EMPTY_BLOCK_STATE_KEY = TerrainState.computeStateKey(1, new int[] {1, 1, 1,1}, new int[] {1, 1, 1, 1}, 1 );

    /** Eight 6-bit blocks that store a corner and side value,
     * plus 4 bits for center height and 3 bits for offset */
    public final static long STATE_BIT_COUNT = 55;

    public final static long STATE_BIT_MASK = 0x7FFFFFFFFFFFFFL;

    public final static int BLOCK_LEVELS_INT = 12;
    public final static float BLOCK_LEVELS_FLOAT = (float) BLOCK_LEVELS_INT;
    public final static int MIN_HEIGHT = -23;
    public final static int NO_BLOCK = MIN_HEIGHT - 1;
    public final static int MAX_HEIGHT = 36;
    
    // Use these insted of magic number for filler block meta values
    /** This value is for a height block two below another height block, offset of 2 added to vertex heights*/
//    public final static int FILL_META_DOWN2 = 0;
//    public final static int FILL_META_DOWN1 = 1;
    
    /** This value indicates a top height block, means no offset, no effect on vertex calculations*/
//    public final static int FILL_META_LEVEL = 2;
//    public final static int FILL_META_UP1 = 3;
//    public final static int FILL_META_UP2 = 4;
    

//    /**
//     * Number of possible values for non-center blocks.
//     * Includes negative values, positive values, zero and NO_BLOCK values.
//     */
//    private final static int VALUE_COUNT = -MIN_HEIGHT + MAX_HEIGHT + 1 + 1;

    /** 
     * Returns values -2 through +2 from a triad (3 bits).
     */
    public static int getYOffsetFromTriad(int triad)
    {
        return Math.min(4, triad & 7) - 2;
    }

    /**
     * Stores values from -2  to +2 in a triad (3 bits).
     * Invalid values are handled same as +1.
     */
    public static int getTriadWithYOffset(int offset)
    {
        return Math.min(4, (offset + 2) & 7);
    }

    private final byte centerHeight;
    private final byte sideHeight[] = new byte[4];
    private final byte cornerHeight[] = new byte[4];
    private final byte yOffset;
    private final long stateKey;
    
    private static final byte SIMPLE_FLAG[] = new byte[5];
    private static final int SIMPLE_FLAG_TOP_ORDINAL = 4;
    
    static
    {
        SIMPLE_FLAG[HorizontalFace.EAST.ordinal()] = 1;
        SIMPLE_FLAG[HorizontalFace.WEST.ordinal()] = 2;
        SIMPLE_FLAG[HorizontalFace.NORTH.ordinal()] = 4;
        SIMPLE_FLAG[HorizontalFace.SOUTH.ordinal()] = 8;
        SIMPLE_FLAG[SIMPLE_FLAG_TOP_ORDINAL] = 16;
    }
    
    /** true if model vertex height calculations current */
    private boolean vertexCalcsDone = false;
    /** cache model vertex height calculations */
    private float midCornerHeight[] = new float[HorizontalCorner.values().length];
    /** cache model vertex height calculations */
    private float farCornerHeight[] = new float[HorizontalCorner.values().length];
    /** cache model vertex height calculations */
    private float midSideHeight[] = new float[HorizontalFace.values().length];
    /** cache model vertex height calculations */
    private float farSideHeight[] = new float[HorizontalFace.values().length];
    private byte simpleFlags = 0;
    
    public long getStateKey()
    {
        return stateKey;
    }
    
    public static long computeStateKey(int centerHeightIn, int[] sideHeightIn, int[] cornerHeightIn, int yOffsetIn)
    {
        long stateKey = (centerHeightIn - 1) | getTriadWithYOffset(yOffsetIn) << 4;
        
        int shift = 7;
        for(int i = 0; i < 4; i++)
        {
            stateKey |= ((long)((sideHeightIn[i] - NO_BLOCK)) << shift);
            shift += 6;
            stateKey |= ((long)((cornerHeightIn[i] - NO_BLOCK)) << shift);
            shift += 6;            
        }
        return stateKey;
    }
    
    public TerrainState(int centerHeightIn, int[] sideHeightIn, int[] cornerHeightIn, int yOffsetIn)
    {
        this(computeStateKey(centerHeightIn, sideHeightIn, cornerHeightIn, yOffsetIn));
    }

    public TerrainState(long stateKey)
    {
        this.stateKey = stateKey;
        centerHeight = (byte)((stateKey & 0xF) + 1);
        yOffset = (byte) getYOffsetFromTriad((int) ((stateKey >> 4) & 0x7));

        int shift = 7;
        for(int i = 0; i < 4; i++)
        {
            sideHeight[i] = (byte) (((stateKey >> shift) & 63) + NO_BLOCK);
            shift += 6;
            cornerHeight[i] = (byte) (((stateKey >> shift) & 63) + NO_BLOCK);
            shift += 6;
        }        
    }
    

    /**
     * Rendering height of center block ranges from 1 to 12
     * and is stored in state key as values 0-11.
     */
    public int getCenterHeight()
    {
        return this.centerHeight;
    }

    public int getYOffset()
    {
        return this.yOffset;
    }

    // Rendering height of corner and side neighbors ranges 
    // from -24 to 36. 
    public int getSideHeight(HorizontalFace side)
    {
        return this.sideHeight[side.ordinal()];
    }

    public int getCornerHeight(HorizontalCorner corner)
    {
        return this.cornerHeight[corner.ordinal()];
    }


    
    /**
     * Returns how many filler blocks are needed on top to cover a cut surface.
     * Possible return values are 0, 1 and 2.
     */
    public int topFillerNeeded()
    {
        //filler only applies to level blocks
        if(yOffset != 0) 
            return 0;
        refreshVertexCalculationsIfNeeded();

        double max = 0;
        
        // center vertex does not matter if top is simplified to a single quad
        if(!getSimpleFlag(SIMPLE_FLAG_TOP_ORDINAL))
        {
            max = Math.max(max,getCenterVertexHeight());
        }
        
        for(int i = 0; i < 4; i++)
        {
            // side does not matter if side geometry is simplified
            if(!getSimpleFlag(i))
            {
                max = Math.max(max, this.midSideHeight[i]);
            }
            max = Math.max(max, this.midCornerHeight[i]);
        }
        
        return max > 2.01  ? 2 : max > 1.01 ? 1 : 0;
    }
    
    public boolean isSideSimple(HorizontalFace face)
    {
        refreshVertexCalculationsIfNeeded();
        return this.getSimpleFlag(face.ordinal());
    }    
    
    public boolean isTopSimple()
    {
        refreshVertexCalculationsIfNeeded();
        return this.getSimpleFlag(SIMPLE_FLAG_TOP_ORDINAL);
    }
    
    public boolean isFullCube()
    {
        refreshVertexCalculationsIfNeeded();
        double top = 1.0 + yOffset + QuadHelper.EPSILON;
        
        // center vertex does not matter if top is simplified to a single quad
        if(!getSimpleFlag(SIMPLE_FLAG_TOP_ORDINAL))
        {
            if(getCenterVertexHeight() < top) return false;
        }
        
        for(int i = 0; i < 4; i++)
        {
            // side does not matter if side geometry is simplified
            if(!getSimpleFlag(i))
            {
                if(this.midSideHeight[i] < top) return false;
            }

            if(this.midCornerHeight[i] < top) return false;
        }
        return true;
    }

    public boolean isEmpty()
    {
        refreshVertexCalculationsIfNeeded();
        double bottom = 0.0 + yOffset;
        
        // center vertex does not matter if top is simplified to a single quad
        if(!getSimpleFlag(SIMPLE_FLAG_TOP_ORDINAL))
        {
            if(getCenterVertexHeight() > bottom) return false;
        }
        
        for(int i = 0; i < 4; i++)
        {
            // side does not matter if side geometry is simplified
            if(!getSimpleFlag(i))
            {
                if(this.midSideHeight[i] > bottom) return false;
            }
            
            if(this.midCornerHeight[i] > bottom) return false;
        }
        return true;
    }
    
    /** 
     * how much sky light is blocked by this shape. 
     * 0 = none, 14 = most, 255 = all
     */
    public int verticalOcclusion()
    {
        refreshVertexCalculationsIfNeeded();
        double bottom = 0.0 + yOffset;
        
        int aboveCount = 0;
        
        for(int i = 0; i < 4; i++)
        {
            if(this.midSideHeight[i] > bottom) aboveCount++;
            if(this.midCornerHeight[i] > bottom) aboveCount++;
        }        
        
        if(getCenterVertexHeight() > bottom) aboveCount *= 2;
        
        return aboveCount >= 16 ? 255 : aboveCount;
    }
    
    /**
     * Returns minimum corner vertex height of block.
     * Used for determining where the bottom of blocks starts.
     */
//    public float getMinCornerVertexHeight()
//    {
//        refreshVertexCalculationsIfNeeded();
//        return Math.min(Math.min(midCornerHeight[0], midCornerHeight[1]), Math.min(midCornerHeight[2], midCornerHeight[3]));
//    }
    
    public float getCenterVertexHeight()
    {
        return (float) getCenterHeight() / BLOCK_LEVELS_FLOAT;
    }

    public float getFarCornerVertexHeight(HorizontalCorner corner)
    {
        refreshVertexCalculationsIfNeeded();
        return farCornerHeight[corner.ordinal()];
    }
    
    public float getMidCornerVertexHeight(HorizontalCorner corner)
    {
        refreshVertexCalculationsIfNeeded();
        return midCornerHeight[corner.ordinal()];
    }
    
    public float getFarSideVertexHeight(HorizontalFace face)
    {
        refreshVertexCalculationsIfNeeded();
        return farSideHeight[face.ordinal()];
    }
    
    public float getMidSideVertexHeight(HorizontalFace face)
    {
        refreshVertexCalculationsIfNeeded();
        return midSideHeight[face.ordinal()];
    }

    private void refreshVertexCalculationsIfNeeded()
    {
        if(vertexCalcsDone) return;
        for(HorizontalFace side : HorizontalFace.values())
        {
            midSideHeight[side.ordinal()] = calcMidSideVertexHeight(side);
            farSideHeight[side.ordinal()] = calcFarSideVertexHeight(side);
            
        }
        
        for(HorizontalCorner corner: HorizontalCorner.values())
        {
            midCornerHeight[corner.ordinal()] = calcMidCornerVertexHeight(corner);
            farCornerHeight[corner.ordinal()] = calcFarCornerVertexHeight(corner);
        }
        
        //determine if sides and top geometry can be simplified
        boolean topIsSimple = true;
        
        for(HorizontalFace side: HorizontalFace.values())
        {
            double avg = midCornerHeight[HorizontalCorner.find(side, side.getLeft()).ordinal()];
            avg += midCornerHeight[HorizontalCorner.find(side, side.getRight()).ordinal()];
            avg /= 2;
            boolean sideIsSimple = Math.abs(avg - midSideHeight[side.ordinal()]) < 2.0 / BLOCK_LEVELS_FLOAT;
            setSimpleFlag(side.ordinal(), sideIsSimple);
            topIsSimple = topIsSimple && sideIsSimple;
        }

        double cross1 = (midCornerHeight[HorizontalCorner.NORTH_EAST.ordinal()] + midCornerHeight[HorizontalCorner.SOUTH_WEST.ordinal()]) / 2.0;
        double cross2 = (midCornerHeight[HorizontalCorner.NORTH_WEST.ordinal()] + midCornerHeight[HorizontalCorner.SOUTH_EAST.ordinal()]) / 2.0;
        setSimpleFlag(SIMPLE_FLAG_TOP_ORDINAL, topIsSimple & (Math.abs(cross1 - cross2) < 2.0 / BLOCK_LEVELS_FLOAT));
        
        vertexCalcsDone = true;

    }
    
    private void setSimpleFlag(int ordinal, boolean value)
    {
        if(value)
        {
            this.simpleFlags |= SIMPLE_FLAG[ordinal];
        }
        else
        {
            this.simpleFlags &= ~SIMPLE_FLAG[ordinal];
        }
         
    }
    
    private boolean getSimpleFlag(int ordinal)
    {
        return (this.simpleFlags & SIMPLE_FLAG[ordinal]) == SIMPLE_FLAG[ordinal];
    }
    
    private float calcFarCornerVertexHeight(HorizontalCorner corner)
    {
        int heightCorner = getCornerHeight(corner);
        
        if(heightCorner == TerrainState.NO_BLOCK)
        {
            int max = Math.max(Math.max(getSideHeight(corner.face1), getSideHeight(corner.face2)), getCenterHeight());
            heightCorner = max - BLOCK_LEVELS_INT;
        }
       
        return ((float) heightCorner) / BLOCK_LEVELS_FLOAT;
    }
    
    
    private float calcMidCornerVertexHeight(HorizontalCorner corner)
    {
        int heightSide1 = getSideHeight(corner.face1);
        int heightSide2 = getSideHeight(corner.face2);
        int heightCorner = getCornerHeight(corner);
        
        int max = Math.max(Math.max(heightSide1, heightSide2), Math.max(heightCorner, getCenterHeight())) - BLOCK_LEVELS_INT;
                
        if(heightSide1 == TerrainState.NO_BLOCK) heightSide1 = max;
        if(heightSide2 == TerrainState.NO_BLOCK) heightSide2 = max;
        if(heightCorner == TerrainState.NO_BLOCK) heightCorner = max;
        
        float numerator = getCenterHeight() + heightSide1 + heightSide2 + heightCorner;
       
        return numerator / (BLOCK_LEVELS_FLOAT * 4F);
        
    }
    
    private float calcFarSideVertexHeight(HorizontalFace face)
    {
        return (getSideHeight(face) == TerrainState.NO_BLOCK ? getCenterHeight() - BLOCK_LEVELS_INT: ((float)getSideHeight(face)) / BLOCK_LEVELS_FLOAT);
    }

    private float calcMidSideVertexHeight(HorizontalFace face)
    {
        float sideHeight = getSideHeight(face) == TerrainState.NO_BLOCK ? getCenterHeight() - BLOCK_LEVELS_INT : (float)getSideHeight(face);
        return (sideHeight + (float) getCenterHeight()) / (BLOCK_LEVELS_FLOAT * 2F);
    }

    public String toString()
    {
        String retval = "CENTER=" + this.getCenterHeight();
        for(HorizontalFace side: HorizontalFace.values())
        {
            retval += " " + side.name() + "=" + this.getSideHeight(side);
        }
        for(HorizontalCorner corner: HorizontalCorner.values())
        {
            retval += " " + corner.name() + "=" + this.getCornerHeight(corner);
        }
        retval += " Y-OFFSET=" + yOffset;
        return retval;
    }

    public static long getBitsFromWorldStatically(SuperBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return getBitsFromWorldStatically(block.isFlowFiller(), state, world, pos);
    }
    
    public static long getBitsFromWorldStatically(ModelState modelState, SuperBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return getBitsFromWorldStatically(modelState.getShape() == ModelShape.TERRAIN_FILLER, state, world, pos);
    }
    
    private static long getBitsFromWorldStatically(boolean isFlowFiller, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        int centerHeight;
        int sideHeight[] = new int[4];
        int cornerHeight[] = new int[4];
        int yOffset = 0;
    
        //        Adversity.log.info("flowstate getBitsFromWorld @" + pos.toString());
    
        int yOrigin = pos.getY();
        IBlockState originState = state;
    
        if(isFlowFiller)
        {
            int offset = TerrainBlock.getYOffsetFromState(state);
            yOrigin -= offset;
            yOffset = offset;
            originState = world.getBlockState(pos.down(offset));
            if(!TerrainBlock.isFlowHeight(originState.getBlock()))
            {
                return EMPTY_BLOCK_STATE_KEY;
            }
        }
        else
        {
            // If under another flow height block, handle similar to filler block.
            // Not a perfect fix if they are stacked, but shouldn't normally be.
            //                Adversity.log.info("flowstate is height block");
    
            // try to use block above as height origin
            originState = world.getBlockState(pos.up().up());
            if(TerrainBlock.isFlowHeight(originState.getBlock()))
            {
                yOrigin += 2;
                yOffset = -2;
                //                    Adversity.log.info("origin 2 up");
            }
            else
            {
                originState = world.getBlockState(pos.up());
                if(TerrainBlock.isFlowHeight(originState.getBlock()))
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
        neighborHeight[1][1] = TerrainBlock.getFlowHeightFromState(originState);
    
        MutableBlockPos mutablePos = new MutableBlockPos();
        for(int x = 0; x < 3; x++)
        {
            for(int z = 0; z < 3; z++)
            {
                if(x == 1 && z == 1 ) continue;
                mutablePos.setPos(pos.getX() - 1 + x, yOrigin, pos.getZ() - 1 + z);
    
                neighborHeight[x][z] = getFlowHeight(world, mutablePos);
    
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
    
        return computeStateKey(centerHeight, sideHeight, cornerHeight, yOffset);
    
    }

    /** 
     * Pass in pos with Y of flow block for which we are getting data.
     * Returns relative flow height based on blocks 2 above through 2 down.
     * Gets called frequently, thus the use of mutable pos.
     */
    public static int getFlowHeight(IBlockAccess world, MutableBlockPos pos)
    {
        pos.setY(pos.getY() + 2);;
        IBlockState state = world.getBlockState(pos);
        int h = TerrainBlock.getFlowHeightFromState(state);
        if(h > 0) return 2 * BLOCK_LEVELS_INT + h;
    
        pos.setY(pos.getY() - 1);
        state = world.getBlockState(pos);
        h = TerrainBlock.getFlowHeightFromState(state);
        if(h > 0) return BLOCK_LEVELS_INT + h;
    
        pos.setY(pos.getY() - 1);
        state = world.getBlockState(pos);
        h = TerrainBlock.getFlowHeightFromState(state);
        if(h > 0) return h;
    
        pos.setY(pos.getY() - 1);
        state = world.getBlockState(pos);
        h = TerrainBlock.getFlowHeightFromState(state);
        if(h > 0) return -BLOCK_LEVELS_INT + h;
    
        pos.setY(pos.getY() - 1);
        state = world.getBlockState(pos);
        h = TerrainBlock.getFlowHeightFromState(state);
        if(h > 0) return -2 * BLOCK_LEVELS_INT + h;
    
        return NO_BLOCK;
    }
}