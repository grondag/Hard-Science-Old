package grondag.hard_science.library;

import org.junit.Test;

import grondag.hard_science.library.world.PerChunkBlockPosQueue;
import net.minecraft.util.math.BlockPos;

public class PerChunkBlockPosQueueTest
{

    @Test
    public void test()
    {
        BlockPos p1a = new BlockPos(1, 1, 1);
        BlockPos p1b = new BlockPos(1, 4, 2);
        
        BlockPos p2a = new BlockPos(27, 5, 3);
        BlockPos p2b = new BlockPos(26, 6, 4);
        
        PerChunkBlockPosQueue subject = new PerChunkBlockPosQueue();
        
        subject.enqueue(p1a);
        
        assert subject.dequeue(p2a) == null;
        assert subject.dequeue(p1b).equals(p1a);
        assert subject.dequeue(p1b) == null;
        
        subject.enqueue(p1a);
        subject.enqueue(p1a);
        subject.enqueue(p1b);
        
        subject.enqueue(p2b);
        subject.enqueue(p2a);
        subject.enqueue(p1b);
        
        assert subject.dequeue(p1b).equals(p1a);
        assert subject.dequeue(p2a).equals(p2b);
        assert subject.dequeue(p1b).equals(p1a);
        assert subject.dequeue(p1b).equals(p1b);
        assert subject.dequeue(p1b).equals(p1b);
        assert subject.dequeue(p1b) == null;
        
        subject.clear();
        
        assert subject.dequeue(p2b) == null;
        
    }

}
