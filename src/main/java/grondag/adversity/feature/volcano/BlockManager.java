package grondag.adversity.feature.volcano;

import java.util.TreeSet;

import com.google.common.collect.ComparisonChain;
import grondag.adversity.Adversity;
import grondag.adversity.library.RelativeBlockPos;
import net.minecraft.util.math.BlockPos;

public class BlockManager 
{
    private final BlockPos pos;
    private final boolean distanceRanked;
    private TreeSet<BlockPlacement> placedBlocks;

    public BlockManager(BlockPos pos, boolean distanceRanked)
    {
        this.pos = pos;
        this.distanceRanked = distanceRanked;
        placedBlocks =  new TreeSet<BlockPlacement>();
          
    }

    public BlockManager(BlockPos pos, boolean distanceRanked, int[] values)
    {
        
        this(pos, distanceRanked);
        
        //to be valid, must have a multiple of two
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
            result[i++] = entry.index;  
        }       
        return result;
    }

    public void add(BlockPos pos, int index)
    {
        this.placedBlocks.add(new BlockPlacement(pos, index));
    }
    
    /**
     * Used to transfer laval blocks in distance-ranked order
     * to a block manager that is indexed by tick index only.
     * Allows us to start a new laval pass while cooling of last pass proceeds
     * and keep correct cooling order.
     */
//    public void transferWithOffset(BlockManager blocks, int startingIndex, int baseTicks, int randTicks)
//    {
//        int index = startingIndex;
//        
//        while(!blocks.placedBlocks.isEmpty())
//        {
//            index += baseTicks;
//            if(randTicks > 0)
//            {
//                index += Useful.SALT_SHAKER.nextInt(randTicks);
//            }   
//            this.placedBlocks.add(new BlockPlacement(blocks.placedBlocks.pollLast().pos, index));
//        }
//    }
    
    /** 
     * Gets ready block farthest from origin.
     * Only useful if distanceRanked = true;
     */
    public BlockPlacement pollLastEntry()
    {
        return placedBlocks.pollLast();
    }
    
    public BlockPlacement pollFirstReadyEntry(int threshold)
    {
        if(placedBlocks.isEmpty()) return null;
                
        if(placedBlocks.first().getIndex() > threshold) return null;
        
        return placedBlocks.pollFirst();
        
    }

//    public BlockPlacement pollLastReadyEntry(int threshold)
//    {
//        if(placedBlocks.isEmpty()) return null;
//            
//        if(placedBlocks.last().getIndex() < threshold) return null;
//        
//        return placedBlocks.pollLast();
//        
//    }
    
   
    public class BlockPlacement implements Comparable<BlockPlacement>
    {
        private final int pos;
        private final int index;
        private final int distance;

        protected BlockPlacement(BlockPos pos, int index)
        {
            this(RelativeBlockPos.getKey(pos, BlockManager.this.pos), index);
        }

        protected BlockPlacement(int pos, int index)
        {
            this.pos= pos;
            this.index = index;
            this.distance = BlockManager.this.distanceRanked ? (int) this.getPos().distanceSq(BlockManager.this.pos) : 0;
        }

        public BlockPos getPos()
        {
            return RelativeBlockPos.getPos(pos, BlockManager.this.pos);
        }

        public int getIndex()
        {
            return index;
        }
        
        @Override
        public int compareTo(BlockPlacement other)
        {
            return ComparisonChain.start()
                    .compare(this.distance, other.distance)
                    .compare(this.index, other.index)
                    .compare(this.pos, other.pos)
                    .result();
        }
    }
}
