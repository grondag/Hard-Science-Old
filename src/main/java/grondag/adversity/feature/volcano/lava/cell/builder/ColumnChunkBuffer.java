package grondag.adversity.feature.volcano.lava.cell.builder;

import grondag.adversity.library.PackedBlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.chunk.Chunk;

/** buffers block info for an entire chunk to improve locality of reference */
public class ColumnChunkBuffer
{
  
    BlockType blockType[] = new BlockType[0x10000];
    
    private long packedChunkPos;
    
    void readChunk(Chunk chunk)
    {
        this.packedChunkPos = PackedBlockPos.getPackedChunkPos(chunk);
     
        //chunk data is optimized for horizontal plane access
        //we are optimized for column access
        for(int y = 0; y < 256; y++)
        {
            for(int x = 0; x < 16; x++)
            {
                for(int z = 0; z < 16; z++)
                {
                    int i = getIndex(x, y, z);
                    IBlockState state = chunk.getBlockState(x, y, z);
                    this.blockType[i] = BlockType.getBlockTypeFromBlockState(state);
                }
            }
        }
    }
    
    public long getPackedChunkPos()
    {
        return this.packedChunkPos;
    }
    
    public static int getIndex(int x, int y, int z)
    {
        return x << 12 | z << 8 | y;
    }
}
