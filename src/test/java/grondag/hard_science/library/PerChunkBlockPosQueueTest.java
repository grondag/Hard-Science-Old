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
        
        subject.queue(p1a);
        
        assert subject.dequeue(p2a) == null;
        assert subject.dequeue(p1b).equals(p1a);
        assert subject.dequeue(p1b) == null;
        
        subject.queue(p1a);
        subject.queue(p1a);
        subject.queue(p1b);
        
        subject.queue(p2b);
        subject.queue(p2a);
        subject.queue(p1b);
        
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
