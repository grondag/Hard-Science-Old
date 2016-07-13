package grondag.adversity.niceblock;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import grondag.adversity.library.NeighborBlocks.HorizontalCorner;
import grondag.adversity.library.NeighborBlocks.HorizontalFace;
import junit.framework.Assert;

public class FlowHeightStateTest
{

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
    }

    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public void test()
    {
        FlowHeightState state = new FlowHeightState(0);
        state.setCenterHeight(13);
        for(int i = 0; i < 4; i++)
        {
            state.setSideHeight(HorizontalFace.values()[i], -12 + i * 15);
            state.setCornerHeight(HorizontalCorner.values()[i], -15 + i * 17);
        }
        long key = state.getStateKey();
        state = new FlowHeightState(key);
        

        assertTrue(state.getCenterHeight() == 13);
        for(int i = 0; i < 4; i++)
        {
            assertTrue(state.getSideHeight(HorizontalFace.values()[i]) ==  -12 + i * 15);
            assertTrue(state.getCornerHeight(HorizontalCorner.values()[i]) == -15 + i * 17);
        }
    }

}
