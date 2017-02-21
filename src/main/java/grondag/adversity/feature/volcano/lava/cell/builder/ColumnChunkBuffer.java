package grondag.adversity.feature.volcano.lava.cell.builder;

import java.util.Arrays;

import grondag.adversity.niceblock.base.IFlowBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.chunk.Chunk;

/** buffers block info for an entire chunk to improve locality of reference */
public class ColumnChunkBuffer
{
  
    BlockType blockType[] = new BlockType[0x10000];
    int blockLevel[] = new int[0x10000];
    
    private int xPosition;
    private int zPosition;
    
    void readChunk(Chunk chunk)
    {
        Arrays.fill(this.blockLevel, 0);
        
        this.xPosition = chunk.xPosition;
        this.zPosition = chunk.zPosition;
        
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
                    if(this.blockType[i].isFlow) this.blockLevel[i] = IFlowBlock.getFlowHeightFromState(state);
                }
            }
        }
    }
    
    public static int getIndex(int x, int y, int z)
    {
        return x << 12 | z << 8 | y;
    }
}
