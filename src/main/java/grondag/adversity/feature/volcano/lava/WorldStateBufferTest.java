package grondag.adversity.feature.volcano.lava;

import org.junit.Test;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public class WorldStateBufferTest
{

    @Test
    public void test()
    {
        WorldStateBuffer.AdjustmentTracker at = new WorldStateBuffer.AdjustmentTracker();
        
        at.setAdjustmentNeededAround(new BlockPos(0, 0, 0));
        at.excludeAdjustmentNeededAt(new BlockPos(0, 0, 0));
        
        at.setAdjustmentNeededAround(new BlockPos(0, 255, 0));
        at.excludeAdjustmentNeededAt(new BlockPos(0, 255, 0));
        
        at.setAdjustmentNeededAround(new BlockPos(3, 10, 7));
        at.excludeAdjustmentNeededAt(new BlockPos(3, 10, 7));

        at.setAdjustmentNeededAround(new BlockPos(3, 11, 8));
        at.excludeAdjustmentNeededAt(new BlockPos(3, 11, 8));
        
        at.excludeAdjustmentNeededAt(new BlockPos(3, 12, 8));
        
        at.getAdjustmentPositions(new ChunkPos(new BlockPos(3, 10, 7))).forEach(p -> System.out.println(p.toString()));
    
    }

}
