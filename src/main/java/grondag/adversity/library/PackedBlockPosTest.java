package grondag.adversity.library;

import org.junit.Test;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public class PackedBlockPosTest
{

    @Test
    public void test()
    {
     BlockPos pos1 = new BlockPos(0, 0, 0);
        
        long long1 = PackedBlockPos.pack(pos1);
        
        long long2 = PackedBlockPos.pack(0, 0, 0);
        
        assert(long1 == long2);
        
        BlockPos pos2 = PackedBlockPos.unpack(long2);
        
        assert(pos1.equals(pos2));
        
        pos1 = new BlockPos(241524, 144, -58234);
        long1 = PackedBlockPos.pack(pos1);
        pos2 = PackedBlockPos.unpack(long1);
        assert(pos1.equals(pos2));
        
        pos1 = new BlockPos(30000000, 255, -30000000);
        long1 = PackedBlockPos.pack(pos1);
        pos2 = PackedBlockPos.unpack(long1);
        assert(pos1.equals(pos2));
        
        // position with extra bits
        pos1 = new BlockPos(-9572, 12, 5954);
        long1 = PackedBlockPos.pack(pos1, 1);
        assert PackedBlockPos.getX(long1) == pos1.getX();
        assert PackedBlockPos.getY(long1) == pos1.getY();
        assert PackedBlockPos.getZ(long1) == pos1.getZ();
        assert PackedBlockPos.getExtra(long1) == 1;
        
        // set extra bits
        pos1 = new BlockPos(241524, 144, -58234);
        long1 = PackedBlockPos.pack(pos1);
        long2 = PackedBlockPos.setExtra(long1, 7);
        pos2 = PackedBlockPos.unpack(long2);
        assert(pos1.equals(pos2));
        assert(PackedBlockPos.getExtra(long2) == 7);
        
        //setup for directions
        pos1 = new BlockPos(241524, 144, -58234);
        long1 = PackedBlockPos.pack(pos1);
        
        //up
        pos2 = PackedBlockPos.unpack(PackedBlockPos.up(long1));
        assert(pos1.up().equals(pos2));
        
        //down
        pos2 = PackedBlockPos.unpack(PackedBlockPos.down(long1));
        assert(pos1.down().equals(pos2));

        //east
        pos2 = PackedBlockPos.unpack(PackedBlockPos.east(long1));
        assert(pos1.east().equals(pos2));
        
        //west
        pos2 = PackedBlockPos.unpack(PackedBlockPos.west(long1));
        assert(pos1.west().equals(pos2));
        
        //north
        pos2 = PackedBlockPos.unpack(PackedBlockPos.north(long1));
        assert(pos1.north().equals(pos2));
        
        //south
        pos2 = PackedBlockPos.unpack(PackedBlockPos.south(long1));
        assert(pos1.south().equals(pos2));
        
        //chunk coords
        pos1 = new BlockPos(414, 2, -52234);
        ChunkPos cpos = new ChunkPos(pos1);
        long1 = PackedBlockPos.getPackedChunkPos(PackedBlockPos.pack(pos1));
        assert(PackedBlockPos.getChunkXPos(long1) == cpos.chunkXPos);
        assert(PackedBlockPos.getChunkZPos(long1) == cpos.chunkZPos);
        assert(PackedBlockPos.getChunkXStart(long1) == cpos.getXStart());
        assert(PackedBlockPos.getChunkZStart(long1) == cpos.getZStart());
        
        
    }

}
