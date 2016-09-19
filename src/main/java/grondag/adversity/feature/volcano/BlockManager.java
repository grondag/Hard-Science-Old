package grondag.adversity.feature.volcano;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.TreeMultiset;

import grondag.adversity.Adversity;
import grondag.adversity.library.RelativeBlockPos;
import net.minecraft.util.math.BlockPos;

public class BlockManager 
{
    private final BlockPos pos;
    private TreeMultiset<BlockPlacement> placedBlocks;

    public BlockManager(BlockPos pos)
    {
        this.pos = pos;
        placedBlocks =  TreeMultiset.create();
          
    }

    public BlockManager(BlockPos pos, int[] values)
    {
        
        this(pos);
        
        //to be valid, must have a multiple of three
        if(values.length % 2 != 0)
        {
            Adversity.log.warn("Invalid placement data loading volcano at " + pos.toString()
                    + ". Previously placed blocks may not be updated properly.");
            return;
        }
        
        int i = 0;
        while(i < values.length)
        {
            this.placedBlocks.add(new BlockPlacement(values[i++], values[i++]));
        }
    }
    
    public int getCount() 
    {
        return placedBlocks.size();
    }

    public int[] getArray()
    {
        int[] result = new int[placedBlocks.size() * 2];
        int i = 0;
        
        for(BlockPlacement entry: this.placedBlocks)
        {
            result[i++] = entry.pos;
            result[i++] = entry.tick;          
        }       
        return result;
    }

    public void add(BlockPos pos, int tick)
    {
        this.placedBlocks.add(new BlockPlacement(pos, tick));
    }
    
    public BlockPlacement pollFirstReadyEntry(int minTick)
    {
        if(placedBlocks.isEmpty()) return null;
                
            
        if(placedBlocks.firstEntry().getElement().getTick() > minTick)
        {
            Adversity.log.info("too early " + placedBlocks.firstEntry().getElement().getTick() + " > " + minTick);
            return null;
        }
        
        return placedBlocks.pollFirstEntry().getElement();
        
    }

    public class BlockPlacement implements Comparable<BlockPlacement>
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

        @Override
        public int compareTo(BlockPlacement other)
        {
            return ComparisonChain.start()
                    .compare(this.tick, other.tick)
                    .compare(this.pos, other.pos)
                    .result();
        }
    }
}
