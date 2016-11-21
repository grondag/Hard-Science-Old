package grondag.adversity.feature.volcano;

import java.util.TreeSet;

import com.google.common.collect.ComparisonChain;

import grondag.adversity.Adversity;
import grondag.adversity.library.RelativeBlockPos;
import net.minecraft.util.math.BlockPos;

public class SpaceManager 
{
    private final BlockPos pos;
    private TreeSet<OpenSpace> spaces;

    public SpaceManager(BlockPos pos)
    {
        this.pos = pos;
        spaces = new TreeSet<OpenSpace>();
    }

    public int getCount() 
    {
        return spaces.size();
    }

    public SpaceManager(BlockPos pos, int[] values)
    {
        this(pos);

        //to be valid, must have a multiple of three
        if(values.length % 3 != 0)
        {
            Adversity.log.warn("Invalid open space data loading volcano at " + pos.toString()
            + ". Volcano may not place lava properly.");
            return;
        }

        int i = 0;
        while(i < values.length)
        {
            spaces.add(new OpenSpace(values[i++], values[i++], values[i++] == 1));
        }
    }

    public int[] getArray()
    {
        int[] result = new int[this.spaces.size() * 3];
        int i = 0;

        for(OpenSpace space: this.spaces)
        {
            result[i++] = space.pos;
            result[i++] = space.origin;
            result[i++] = space.isSupported ? 1 : 0;
        }	    
        return result;
    }

    public void add(BlockPos posIn, BlockPos origin, boolean isSupported)
    {
        //	    Adversity.log.info("adding space @ " + posIn.toString() + " with origin " + origin.toString());
        spaces.add(new OpenSpace(posIn, origin, isSupported));
    }

    public OpenSpace pollFirst()
    {
        return spaces.pollFirst();
    }

    public OpenSpace peekFirst()
    {
        return spaces.first();
    }
    
//    public OpenSpace pollLast()
//    {
//        return spaces.pollLast();
//    }

    public class OpenSpace implements Comparable<OpenSpace>
    {
        private final int pos;
        private final int origin;
        private final int distance;
        private final boolean isSupported;

        private OpenSpace(BlockPos pos, BlockPos origin, boolean isSupported)
        {
            this(RelativeBlockPos.getKey(pos, SpaceManager.this.pos), RelativeBlockPos.getKey(origin, SpaceManager.this.pos), isSupported);
        }

        private OpenSpace(int pos, int origin, boolean isSupported)
        {
            this.pos = pos;
            this.origin = origin;
            this.isSupported = isSupported;
            int dx = getPos().getX() - SpaceManager.this.pos.getX();
            int dz = getPos().getZ() - SpaceManager.this.pos.getZ();
            this.distance = dx * dx + dz * dz;
        }

        public BlockPos getPos()
        {
            return RelativeBlockPos.getPos(pos, SpaceManager.this.pos);
        }

        public BlockPos getOrigin()
        {
            return RelativeBlockPos.getPos(origin, SpaceManager.this.pos);
        }

        @Override
        public int compareTo(OpenSpace other)
        {
            return ComparisonChain.start()
                    .compareTrueFirst(this.isSupported, other.isSupported)
                    .compare(this.getPos().getY(), other.getPos().getY())
                    .compare(this.distance, other.distance)
                    .compare(this.pos, other.pos)
                    .result();
        }
    }
}
