package grondag.adversity.feature.volcano;

import java.util.Map.Entry;
import java.util.TreeMap;

import grondag.adversity.Adversity;
import grondag.adversity.library.RelativeBlockPos;
import net.minecraft.util.math.BlockPos;

public class BlockManager 
{
    private final BlockPos pos;
    private TreeMap<Integer, BlockPlacement> placedBlocks;
    private int blockCount = 0;


    public BlockManager(BlockPos pos)
    {
        this.pos = pos;
        placedBlocks = new TreeMap<Integer, BlockPlacement>();
    }

    public BlockManager(BlockPos pos, int[] values)
    {
        
        this(pos);
        
        //to be valid, must have a multiple of three
        if(values.length % 3 != 0)
        {
            Adversity.log.warn("Invalid placement data loading volcano at " + pos.toString()
                    + ". Previously placed blocks may not be updated properly.");
            return;
        }
        
        int i = 0;
        while(i < values.length)
        {
            this.placedBlocks.put(values[i++], new BlockPlacement(values[i++], values[i++]));
            this.blockCount++;
        }
    }
    
    public int getCount() 
    {
        return this.blockCount;
    }

    public int[] getArray()
    {
        int[] result = new int[this.blockCount * 3];
        int i = 0;
        
        for(Entry<Integer, BlockPlacement> entry: this.placedBlocks.entrySet())
        {
            result[i++] = entry.getKey();
            result[i++] = entry.getValue().pos;
            result[i++] = entry.getValue().tick;          
        }       
        return result;
    }

    public void add(int sortKey, BlockPos pos, int tick)
    {
        this.placedBlocks.put(sortKey, new BlockPlacement(pos, tick));
        this.blockCount++;
    }
    
    public BlockPlacement pollLastEntryIfReady(int minTick)
    {
        BlockPlacement result = null;
        if(! placedBlocks.isEmpty())
        {
            result = placedBlocks.lastEntry().getValue();
            if(result.getTick() >= minTick)
            {
                placedBlocks.pollLastEntry();
                blockCount--;
            }
            else
            {
                result = null;
            }
        }
        return result;
    }

    public class BlockPlacement
    {
        private final int pos;
        private final int tick;

        protected BlockPlacement(BlockPos pos, int tick)
        {
            this(RelativeBlockPos.getKey(pos, BlockManager.this.pos), tick);
        }

        protected BlockPlacement(int pos, int tick)
        {
            this.pos= pos;
            this.tick = tick;
        }

        public BlockPos getPos()
        {
            return RelativeBlockPos.getPos(pos, BlockManager.this.pos);
        }

        public int getTick()
        {
            return tick;
        }
    }
}
