package grondag.adversity.niceblock.modelstate;

import grondag.adversity.library.NeighborBlocks.HorizontalCorner;
import grondag.adversity.library.NeighborBlocks.HorizontalFace;
import grondag.adversity.library.model.quadfactory.QuadFactory;

public class FlowHeightState
{

    public final static long FULL_BLOCK_STATE_KEY = FlowHeightState.computeStateKey(12, new int[] {12, 12, 12,12}, new int[] {12, 12, 12, 12}, 0 );
    public final static long EMPTY_BLOCK_STATE_KEY = FlowHeightState.computeStateKey(1, new int[] {1, 1, 1,1}, new int[] {1, 1, 1, 1}, 1 );

    /** Eight 6-bit blocks that store a corner and side value,
     * plus 4 bits for center height and 3 bits for offset */
    public final static long STATE_BIT_COUNT = 55;

    public final static long STATE_BIT_MASK = 0x7FFFFFFFFFFFFFL;

    public final static int BLOCK_LEVELS_INT = 12;
    public final static float BLOCK_LEVELS_FLOAT = (float) BLOCK_LEVELS_INT;
    public final static int MIN_HEIGHT = -24;
    public final static int NO_BLOCK = MIN_HEIGHT - 1;
    public final static int MAX_HEIGHT = 36;
    
    // Use these insted of magic number for filler block meta values
    /** This value is for a height block two below another height block, offset of 2 added to vertex heights*/
    public final static int FILL_META_DOWN2 = 0;
    public final static int FILL_META_DOWN1 = 1;
    
    /** This value indicates a top height block, means no offset, no effect on vertex calculations*/
    public final static int FILL_META_LEVEL = 2;
    public final static int FILL_META_UP1 = 3;
    public final static int FILL_META_UP2 = 4;
    

    /**
     * Number of possible values for non-center blocks.
     * Includes negative values, positive values, zero and NO_BLOCK values.
     */
    private final static int VALUE_COUNT = -MIN_HEIGHT + MAX_HEIGHT + 1 + 1;

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
    
//    public FlowHeightState(byte centerHeightIn, int[] sideHeightIn, int[] cornerHeightIn, int yOffsetIn)
//    {
//        centerHeight = centerHeightIn;
//        yOffset = (byte) Math.min(2, Math.max(-2, yOffsetIn));
//        this.sideHeight = sideHeightIn;
//        this.cornerHeight = cornerHeightIn;
//    }

    public FlowHeightState(long stateKey)
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
    // Rendering height of center block ranges from 1 to 12
    // and is stored in state key as values 0-11.

//    public void setCenterHeight(int height)
//    {
//        this.centerHeight = (byte)height;
//        vertexCalcsDone = false;
//    }

    public int getCenterHeight()
    {
        return this.centerHeight;
    }

//    public void setYOffset(int offset)
//    {
//        this.yOffset = (byte) Math.min(2, Math.max(-2, offset));
//    }
 
    public int getYOffset()
    {
        return this.yOffset;
    }

    // Rendering height of corner and side neighbors ranges 
    // from -24 to 36. 

//    public void setSideHeight(HorizontalFace side, int height)
//    {
//        this.sideHeight[side.ordinal()] = (byte)height;
//        vertexCalcsDone = false;
//    }

    public int getSideHeight(HorizontalFace side)
    {
        return this.sideHeight[side.ordinal()];
    }

//    public void setCornerHeight(HorizontalCorner corner, int height)
//    {
//        this.cornerHeight[corner.ordinal()] = (byte)height;
//        vertexCalcsDone = false;
//    }

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
        double top = 1.0 + yOffset + QuadFactory.EPSILON;
        
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
        
        if(heightCorner == FlowHeightState.NO_BLOCK)
        {
            int max = Math.max(Math.max(getSideHeight(corner.face1), getSideHeight(corner.face2)), getCenterHeight());
            heightCorner = (max - 1) / BLOCK_LEVELS_INT * BLOCK_LEVELS_INT;
        }
       
        return ((float) heightCorner) / BLOCK_LEVELS_FLOAT;
    }
    
    /**
     * Doesn't use getFarCornerHeight because would be redundant check of neighbor heights
     */
    private float calcMidCornerVertexHeight(HorizontalCorner corner)
    {
        int heightSide1 = getSideHeight(corner.face1);
        int heightSide2 = getSideHeight(corner.face2);
        int heightCorner = getCornerHeight(corner);
        
        int max = Math.max(Math.max(heightSide1, heightSide2), Math.max(heightCorner, getCenterHeight()));
        max = (max - 1) / BLOCK_LEVELS_INT * BLOCK_LEVELS_INT;
                
        if(heightSide1 == FlowHeightState.NO_BLOCK) heightSide1 = max;
        if(heightSide2 == FlowHeightState.NO_BLOCK) heightSide2 = max;
        if(heightCorner == FlowHeightState.NO_BLOCK) heightCorner = max;
        
        float numerator = getCenterHeight() + heightSide1 + heightSide2 + heightCorner;
       
        return numerator / (BLOCK_LEVELS_FLOAT * 4F);
        
    }
    
    private float calcFarSideVertexHeight(HorizontalFace face)
    {
        return (getSideHeight(face) == FlowHeightState.NO_BLOCK ? 0 : ((float)getSideHeight(face)) / BLOCK_LEVELS_FLOAT);
    }

    private float calcMidSideVertexHeight(HorizontalFace face)
    {
        float sideHeight = getSideHeight(face) == FlowHeightState.NO_BLOCK ? 0 : (float)getSideHeight(face);
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
}