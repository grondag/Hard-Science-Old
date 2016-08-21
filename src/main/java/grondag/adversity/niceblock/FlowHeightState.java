package grondag.adversity.niceblock;

import grondag.adversity.library.NeighborBlocks.HorizontalCorner;
import grondag.adversity.library.NeighborBlocks.HorizontalFace;

public class FlowHeightState
{

    public final static long FULL_BLOCK_STATE_KEY = FlowHeightState.computeStateKey(16, new int[] {16, 16, 16,16}, new int[] {16, 16, 16, 16}, 0 );

    /** Four 13-bit blocks that store a corner and side value,
     * plus 4 bits for center height and 3 bits for offset */
    public final static long STATE_BIT_COUNT = 59;

    public final static long STATE_BIT_MASK = 0x7FFFFFFFFFFFFFFL;

    public final static int MIN_HEIGHT = -32;
    public final static int NO_BLOCK = MIN_HEIGHT - 1;
    public final static int MAX_HEIGHT = 48;

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
    
    public long getStateKey()
    {
        return stateKey;
    }

    public static long computeStateKey(int centerHeightIn, int[] sideHeightIn, int[] cornerHeightIn, int yOffsetIn)
    {
        long stateKey = (centerHeightIn - 1) | getTriadWithYOffset(yOffsetIn) << 4;

        for(int i = 0; i < 4; i++)
        {
            long keyBlock = (cornerHeightIn[i] - NO_BLOCK) * VALUE_COUNT + (sideHeightIn[i] - NO_BLOCK); 
            stateKey |= keyBlock << (i * 13 + 7);
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

        for(int i = 0; i < 4; i++)
        {
            int keyBlock = (int) (stateKey >> (i * 13 + 7)) & 0x1FFF;
            cornerHeight[i] = (byte) (keyBlock / VALUE_COUNT + NO_BLOCK);
            sideHeight[i] = (byte) (keyBlock % VALUE_COUNT + NO_BLOCK);
        }
    }
    // Rendering height of center block ranges from 1 to 16
    // and is stored in state key as values 0-15.

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
    // from -32 to 48. 

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
     * Returns max of all heights on the block, including center.
     * Intended for determining collision box bounds.
     */
    public float getMaxVertexHeight()
    {
        refreshVertexCalculationsIfNeeded();
        float max = getCenterVertexHeight();
        for(int i = 0; i < 4; i++)
        {
            max = Math.max(max, this.midSideHeight[i]);
            max = Math.max(max, this.midCornerHeight[i]);
        }
        return max - this.yOffset;
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
        return (float) getCenterHeight() / 16f;
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
        vertexCalcsDone = true;

    }
    
    private float calcFarCornerVertexHeight(HorizontalCorner corner)
    {
        int heightCorner = getCornerHeight(corner);
        
        if(heightCorner == FlowHeightState.NO_BLOCK)
        {
            int max = Math.max(Math.max(getSideHeight(corner.face1), getSideHeight(corner.face2)), getCenterHeight());
            heightCorner = (max - 1) / 16 * 16;
        }
       
        return ((float) heightCorner) / 16f;
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
        max = (max - 1) / 16 * 16;
                
        if(heightSide1 == FlowHeightState.NO_BLOCK) heightSide1 = max;
        if(heightSide2 == FlowHeightState.NO_BLOCK) heightSide2 = max;
        if(heightCorner == FlowHeightState.NO_BLOCK) heightCorner = max;
        
        float numerator = getCenterHeight() + heightSide1 + heightSide2 + heightCorner;
       
        return numerator / 64f;
        
    }
    
    private float calcFarSideVertexHeight(HorizontalFace face)
    {
        return getSideHeight(face) == FlowHeightState.NO_BLOCK ? 0 : ((float)getSideHeight(face)) / 16f;
    }

    private float calcMidSideVertexHeight(HorizontalFace face)
    {
        float sideHeight = getSideHeight(face) == FlowHeightState.NO_BLOCK ? 0 : (float)getSideHeight(face);
        return (sideHeight + (float) getCenterHeight()) / 32F;
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