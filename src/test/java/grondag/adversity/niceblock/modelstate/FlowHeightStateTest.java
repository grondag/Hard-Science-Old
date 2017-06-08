package grondag.adversity.niceblock.modelstate;

import static org.junit.Assert.*;

import org.junit.Test;

import grondag.adversity.library.NeighborBlocks.HorizontalCorner;
import grondag.adversity.library.NeighborBlocks.HorizontalFace;
import grondag.adversity.superblock.model.state.FlowHeightState;

public class FlowHeightStateTest
{

    @Test
    public void test()
    {
        FlowHeightState state = new FlowHeightState(FlowHeightState.computeStateKey(13, new int[] {-12, -15, -5, 12}, new int[] {18, 0, 13, -16}, 0 ));
        System.out.print(state.toString());
        assertTrue(state.getCenterHeight() == 13);
        assertTrue(state.getSideHeight(HorizontalFace.EAST) == -15);
        assertTrue(state.getCornerHeight(HorizontalCorner.SOUTH_WEST) == -16);
    }

}
