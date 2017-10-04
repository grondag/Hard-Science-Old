package grondag.hard_science.library.superblock.terrain;

import static org.junit.Assert.*;

import org.junit.Test;

import grondag.hard_science.library.world.HorizontalCorner;
import grondag.hard_science.library.world.HorizontalFace;
import grondag.hard_science.superblock.terrain.TerrainState;

public class TerrainStateTest
{

    @Test
    public void test()
    {
        TerrainState state = new TerrainState(TerrainState.computeStateKey(13, new int[] {-12, -15, -5, 12}, new int[] {18, 0, 13, -16}, 0 ));
        System.out.print(state.toString());
        assertTrue(state.getCenterHeight() == 13);
        assertTrue(state.getSideHeight(HorizontalFace.EAST) == -15);
        assertTrue(state.getCornerHeight(HorizontalCorner.SOUTH_WEST) == -16);
    }

}