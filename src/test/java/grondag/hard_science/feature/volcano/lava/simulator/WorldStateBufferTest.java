package grondag.hard_science.feature.volcano.lava.simulator;

import org.junit.Test;

import grondag.hard_science.library.world.PackedBlockPos;
import grondag.hard_science.volcano.lava.simulator.WorldStateBuffer;

public class WorldStateBufferTest
{

    @Test
    public void test()
    {
        WorldStateBuffer.AdjustmentTracker at = new WorldStateBuffer.AdjustmentTracker();
        
        at.setAdjustmentNeededAround(0, 0, 0);
        at.excludeAdjustmentNeededAt(0, 0, 0);
        
        at.setAdjustmentNeededAround(0, 255, 0);
        at.excludeAdjustmentNeededAt(0, 255, 0);
        
        at.setAdjustmentNeededAround(3, 10, 7);
        at.excludeAdjustmentNeededAt(3, 10, 7);

        at.setAdjustmentNeededAround(3, 11, 8);
        at.excludeAdjustmentNeededAt(3, 11, 8);
        
        at.excludeAdjustmentNeededAt(3, 12, 8);
        
        at.getAdjustmentPositions(PackedBlockPos.getPackedChunkPos(3, 7)).forEach(p -> System.out.println(p.toString()));
    
    }

}
