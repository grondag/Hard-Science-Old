package grondag.adversity.niceblock;

import grondag.adversity.library.NeighborBlocks.HorizontalCorner;
import grondag.adversity.library.NeighborBlocks.HorizontalFace;
import grondag.adversity.niceblock.base.NiceBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ClassInheritanceMultiMap;

public class FlowHeightState
{

    public final static long FULL_BLOCK_STATE_KEY;
    
    public final static long STATE_BIT_COUNT = 62;
    
    public final static long STATE_BIT_MASK = 0x3FFFFFFFFFFFFFFFL;

    static
    {
        FlowHeightState flowState = new FlowHeightState(0);
        flowState.setCenterHeight(16);
        for (HorizontalFace face : HorizontalFace.values())
        {
            flowState.setSideHeight(face, 16);
        }
        for (HorizontalCorner corner : HorizontalCorner.values())
        {
            flowState.setCornerHeight(corner, 16);
        }
        FULL_BLOCK_STATE_KEY = flowState.getStateKey();
    }
    
    /** 
     * Returns values -2, -1, +1 and +2 from a nibble.
     */
    public static int getYOffsetFromNibble(int nibble)
    {
        int clamped = nibble & 3;
        return clamped < 2 ? clamped + 1 : 1 - clamped;
    }
    
    /**
     * Stores values from -2 to -1 and +1 to +2 in a nibble.
     * Invalid values are handled same as +1.
     */
    public static int getNibbleWithYOffset(int offset)
    {
        if(offset == -1 || offset == -2) 
            return -offset + 1;
        else if(offset == 2)
            return  1;
        else return 0;
    }
    
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
        stateKey |= ((long)(height - FlowController.NO_BLOCK) << (4 + side.ordinal() * 7));
    }

    public int getSideHeight(HorizontalFace side)
    {
        return (int) ((stateKey >> (4 + side.ordinal() * 7)) & 0x7F) + FlowController.NO_BLOCK;
    }


    public void setCornerHeight(HorizontalCorner corner, int height)
    {
        stateKey |= ((long)(height - FlowController.NO_BLOCK) << (32 + corner.ordinal() * 7));
    }

    public int getCornerHeight(HorizontalCorner corner)
    {
        return (int) ((stateKey >> (32 + corner.ordinal() * 7)) & 0x7F) + FlowController.NO_BLOCK;
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